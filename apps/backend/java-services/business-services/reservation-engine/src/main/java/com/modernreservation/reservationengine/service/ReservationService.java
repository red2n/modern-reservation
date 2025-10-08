package com.modernreservation.reservationengine.service;

import com.modernreservation.reservationengine.context.TenantContext;
import com.modernreservation.reservationengine.dto.ReservationAuditDTO;
import com.modernreservation.reservationengine.dto.ReservationRequestDTO;
import com.modernreservation.reservationengine.dto.ReservationResponseDTO;
import com.modernreservation.reservationengine.dto.ReservationSummaryDTO;
import com.modernreservation.reservationengine.entity.Reservation;
import com.modernreservation.reservationengine.entity.ReservationAudit;
import com.modernreservation.reservationengine.enums.ReservationStatus;
import com.modernreservation.reservationengine.enums.ReservationSource;
import com.modernreservation.reservationengine.repository.ReservationRepository;
import com.modernreservation.reservationengine.repository.ReservationAuditRepository;
import com.reservation.shared.events.EventPublisher;
import com.reservation.shared.events.ReservationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reservation Service - Core business logic for reservation management
 *
 * Handles all reservation operations including creation, modification,
 * cancellation, and status management with comprehensive audit trails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationAuditRepository auditRepository;
    private final TenantService tenantService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventPublisher eventPublisher;

    /**
     * Create a new reservation
     */
    @CacheEvict(value = "reservations", allEntries = true)
    public ReservationResponseDTO createReservation(ReservationRequestDTO request) {
        log.info("Creating new reservation for guest: {} {}",
                request.guestFirstName(), request.guestLastName());

        // ✅ MULTI-TENANCY: Validate tenant context
        tenantService.validateCurrentTenantActive();
        UUID tenantId = TenantContext.getCurrentTenantId();

        // ✅ Validate request tenant matches context tenant
        if (!tenantId.equals(request.tenantId())) {
            log.error("Tenant mismatch: context={}, request={}", tenantId, request.tenantId());
            throw new IllegalArgumentException("Cannot create reservation for another tenant");
        }

        // Validate availability
        validateAvailability(request.propertyId(), request.roomTypeId(),
                           request.checkInDate(), request.checkOutDate());

        // Calculate nights
        int nights = Math.toIntExact(request.checkOutDate().toEpochDay() -
                                   request.checkInDate().toEpochDay());

        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(request.roomRate(),
                request.taxes(), request.fees(), nights);

        // Build reservation entity
        Reservation reservation = Reservation.builder()
                .tenantId(tenantId)  // ✅ Set tenant ID
                .confirmationNumber(generateConfirmationNumber())
                .propertyId(request.propertyId())
                .guestId(request.guestId())
                .guestFirstName(request.guestFirstName())
                .guestLastName(request.guestLastName())
                .guestEmail(request.guestEmail())
                .guestPhone(request.guestPhone())
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .nights(nights)
                .roomTypeId(request.roomTypeId())
                .adults(request.adults())
                .children(request.children())
                .infants(request.infants())
                .roomRate(request.roomRate())
                .taxes(request.taxes())
                .fees(request.fees())
                .totalAmount(totalAmount)
                .currency(request.currency())
                .status(ReservationStatus.CONFIRMED)
                .source(request.source())
                .specialRequests(request.specialRequests())
                .bookingDate(LocalDateTime.now())
                .arrivalTime(request.arrivalTime())
                .departureTime(request.departureTime())
                .paymentMethod(request.paymentMethod())
                .paymentStatus("PENDING")
                .depositAmount(request.depositAmount())
                .depositDueDate(request.depositDueDate())
                .channelReference(request.channelReference())
                .commissionRate(request.commissionRate())
                .createdBy("SYSTEM")
                .build();

        // Save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        // Create audit record
        createAuditRecord(savedReservation, "CREATED", null,
                         ReservationStatus.CONFIRMED, "Reservation created", "SYSTEM");

        // Publish reservation created event
        publishReservationEvent("reservation.created", savedReservation);

        log.info("Successfully created reservation with confirmation: {}",
                savedReservation.getConfirmationNumber());

        return mapToResponseDTO(savedReservation);
    }

    /**
     * Get reservation by ID (tenant-scoped)
     */
    @Cacheable(value = "reservations", key = "T(com.modernreservation.reservationengine.context.TenantContext).getCurrentTenantId() + ':' + #id")
    @Transactional(readOnly = true)
    public Optional<ReservationResponseDTO> getReservationById(UUID id) {
        // ✅ MULTI-TENANCY: Tenant-scoped query
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching reservation by ID: {} for tenant: {}", id, tenantId);

        return reservationRepository.findByTenantIdAndId(tenantId, id)
                .map(this::mapToResponseDTO);
    }

    /**
     * Get reservation by confirmation number (tenant-scoped)
     */
    @Cacheable(value = "reservations", key = "T(com.modernreservation.reservationengine.context.TenantContext).getCurrentTenantId() + ':' + #confirmationNumber")
    @Transactional(readOnly = true)
    public Optional<ReservationResponseDTO> getReservationByConfirmationNumber(String confirmationNumber) {
        // ✅ MULTI-TENANCY: Tenant-scoped query
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching reservation by confirmation number: {} for tenant: {}", confirmationNumber, tenantId);

        return reservationRepository.findByTenantIdAndConfirmationNumber(tenantId, confirmationNumber)
                .map(this::mapToResponseDTO);
    }

    /**
     * Update reservation (tenant-scoped)
     */
    @CacheEvict(value = "reservations", allEntries = true)
    public ReservationResponseDTO updateReservation(UUID id, ReservationRequestDTO request) {
        log.info("Updating reservation: {}", id);

        // ✅ MULTI-TENANCY: Validate tenant and fetch reservation
        UUID tenantId = TenantContext.getCurrentTenantId();
        tenantService.validateCurrentTenantActive();

        // ✅ Tenant-scoped query
        Reservation existingReservation = reservationRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));

        // ✅ Validate request tenant matches
        if (!tenantId.equals(request.tenantId())) {
            throw new IllegalArgumentException("Cannot update reservation for another tenant");
        }

        ReservationStatus oldStatus = existingReservation.getStatus();

        // Update fields
        existingReservation.setGuestFirstName(request.guestFirstName());
        existingReservation.setGuestLastName(request.guestLastName());
        existingReservation.setGuestEmail(request.guestEmail());
        existingReservation.setGuestPhone(request.guestPhone());
        existingReservation.setSpecialRequests(request.specialRequests());
        existingReservation.setArrivalTime(request.arrivalTime());
        existingReservation.setDepartureTime(request.departureTime());
        existingReservation.setUpdatedBy("SYSTEM");

        // Save updated reservation
        Reservation updatedReservation = reservationRepository.save(existingReservation);

        // Create audit record
        createAuditRecord(updatedReservation, "UPDATED", oldStatus,
                         updatedReservation.getStatus(), "Reservation updated", "SYSTEM");

        // Publish update event
        publishReservationEvent("reservation.updated", updatedReservation);

        log.info("Successfully updated reservation: {}", id);
        return mapToResponseDTO(updatedReservation);
    }

    /**
     * Cancel reservation
     */
    @CacheEvict(value = "reservations", allEntries = true)
    public ReservationResponseDTO cancelReservation(UUID id, String reason, String cancelledBy) {
        log.info("Cancelling reservation: {} by: {}", id, cancelledBy);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));

        ReservationStatus oldStatus = reservation.getStatus();

        // Update reservation status
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason(reason);
        reservation.setCancelledBy(cancelledBy);
        reservation.setUpdatedBy(cancelledBy);

        // Save cancelled reservation
        Reservation cancelledReservation = reservationRepository.save(reservation);

        // Create audit record
        createAuditRecord(cancelledReservation, "CANCELLED", oldStatus,
                         ReservationStatus.CANCELLED, reason, cancelledBy);

        // Publish cancellation event
        publishReservationEvent("reservation.cancelled", cancelledReservation);

        log.info("Successfully cancelled reservation: {}", id);
        return mapToResponseDTO(cancelledReservation);
    }

    /**
     * Check-in guest
     */
    @CacheEvict(value = "reservations", allEntries = true)
    public ReservationResponseDTO checkInGuest(UUID id, String roomNumber, String checkedInBy) {
        log.info("Checking in guest for reservation: {}", id);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));

        ReservationStatus oldStatus = reservation.getStatus();

        // Update reservation for check-in
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setRoomNumber(roomNumber);
        reservation.setUpdatedBy(checkedInBy);

        // Save checked-in reservation
        Reservation checkedInReservation = reservationRepository.save(reservation);

        // Create audit record
        createAuditRecord(checkedInReservation, "CHECKED_IN", oldStatus,
                         ReservationStatus.CHECKED_IN, "Guest checked in to room " + roomNumber, checkedInBy);

        // Publish check-in event
        publishReservationEvent("reservation.checkedin", checkedInReservation);

        log.info("Successfully checked in guest for reservation: {}", id);
        return mapToResponseDTO(checkedInReservation);
    }

    /**
     * Check-out guest
     */
    @CacheEvict(value = "reservations", allEntries = true)
    public ReservationResponseDTO checkOutGuest(UUID id, String checkedOutBy) {
        log.info("Checking out guest for reservation: {}", id);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));

        ReservationStatus oldStatus = reservation.getStatus();

        // Update reservation for check-out
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.setUpdatedBy(checkedOutBy);

        // Save checked-out reservation
        Reservation checkedOutReservation = reservationRepository.save(reservation);

        // Create audit record
        createAuditRecord(checkedOutReservation, "CHECKED_OUT", oldStatus,
                         ReservationStatus.CHECKED_OUT, "Guest checked out", checkedOutBy);

        // Publish check-out event
        publishReservationEvent("reservation.checkedout", checkedOutReservation);

        log.info("Successfully checked out guest for reservation: {}", id);
        return mapToResponseDTO(checkedOutReservation);
    }

    /**
     * Get reservations by property (full details)
     */
    @Cacheable(value = "reservations", key = "'property:' + #propertyId + ':' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> getReservationsByProperty(UUID propertyId, Pageable pageable) {
        log.debug("Fetching reservations for property: {}", propertyId);
        return reservationRepository.findByPropertyId(propertyId, pageable)
                .map(this::mapToResponseDTO);
    }

    /**
     * Get reservations by property (summary only - optimized for list views)
     */
    @Cacheable(value = "reservations", key = "'property:summary:' + #propertyId + ':' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<ReservationSummaryDTO> getReservationsSummaryByProperty(UUID propertyId, Pageable pageable) {
        log.debug("Fetching reservation summaries for property: {}", propertyId);
        return reservationRepository.findByPropertyId(propertyId, pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Get audit information for a specific reservation
     */
    @Transactional(readOnly = true)
    public Optional<ReservationAuditDTO> getReservationAuditInfo(UUID reservationId) {
        log.debug("Fetching audit information for reservation: {}", reservationId);
        return reservationRepository.findById(reservationId)
                .map(this::mapToResponseDTO)
                .map(ReservationAuditDTO::from);
    }

    /**
     * Get reservations by date range
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByDateRange(UUID propertyId,
                                                                  LocalDate startDate,
                                                                  LocalDate endDate) {
        log.debug("Fetching reservations for property: {} between {} and {}",
                 propertyId, startDate, endDate);
        return reservationRepository.findReservationsInDateRange(propertyId, startDate, endDate)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Get upcoming arrivals (full details)
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getUpcomingArrivals(UUID propertyId, LocalDate date) {
        log.debug("Fetching upcoming arrivals for property: {} on date: {}", propertyId, date);
        return reservationRepository.findUpcomingArrivals(propertyId, date)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Get upcoming arrivals (summary only - optimized for dashboard)
     */
    @Transactional(readOnly = true)
    public List<ReservationSummaryDTO> getUpcomingArrivalsSummary(UUID propertyId, LocalDate date) {
        log.debug("Fetching upcoming arrival summaries for property: {} on date: {}", propertyId, date);
        return reservationRepository.findUpcomingArrivals(propertyId, date)
                .stream()
                .map(this::mapToSummaryDTO)
                .toList();
    }

    /**
     * Get upcoming departures (full details)
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getUpcomingDepartures(UUID propertyId, LocalDate date) {
        log.debug("Fetching upcoming departures for property: {} on date: {}", propertyId, date);
        return reservationRepository.findUpcomingDepartures(propertyId, date)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Get upcoming departures (summary only - optimized for dashboard)
     */
    @Transactional(readOnly = true)
    public List<ReservationSummaryDTO> getUpcomingDeparturesSummary(UUID propertyId, LocalDate date) {
        log.debug("Fetching upcoming departure summaries for property: {} on date: {}", propertyId, date);
        return reservationRepository.findUpcomingDepartures(propertyId, date)
                .stream()
                .map(this::mapToSummaryDTO)
                .toList();
    }

    // Private helper methods

    private void validateAvailability(UUID propertyId, UUID roomTypeId,
                                    LocalDate checkIn, LocalDate checkOut) {
        // Check for overlapping reservations
        List<Reservation> overlapping = reservationRepository
                .findOverlappingReservations(propertyId, roomTypeId, checkIn, checkOut);

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Room type not available for selected dates");
        }
    }

    private BigDecimal calculateTotalAmount(BigDecimal roomRate, BigDecimal taxes,
                                          BigDecimal fees, int nights) {
        BigDecimal subtotal = roomRate.multiply(BigDecimal.valueOf(nights));
        return subtotal.add(taxes != null ? taxes : BigDecimal.ZERO)
                      .add(fees != null ? fees : BigDecimal.ZERO);
    }

    private String generateConfirmationNumber() {
        return "RES" + System.currentTimeMillis() +
               String.format("%04d", (int)(Math.random() * 10000));
    }

    private void createAuditRecord(Reservation reservation, String action,
                                 ReservationStatus oldStatus, ReservationStatus newStatus,
                                 String reason, String performedBy) {
        ReservationAudit audit = ReservationAudit.builder()
                .reservation(reservation)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .notes(action + " - Performed by: " + performedBy)
                .changedAt(LocalDateTime.now())
                .build();

        auditRepository.save(audit);
    }

    private void publishReservationEvent(String eventType, Reservation reservation) {
        try {
            // Keep old method for backwards compatibility with other event types
            // TODO: Migrate all event types to structured events
            if ("reservation.created".equals(eventType)) {
                publishReservationCreatedEvent(reservation);
            } else {
                // Fallback to old approach for other events
                kafkaTemplate.send("reservation-events", eventType, reservation);
                log.debug("Published {} event (legacy) for reservation: {}", eventType, reservation.getId());
            }
        } catch (Exception e) {
            log.error("Failed to publish {} event for reservation: {}", eventType, reservation.getId(), e);
        }
    }

    /**
     * Publish structured ReservationCreatedEvent using Avro-generated event class
     */
    private void publishReservationCreatedEvent(Reservation reservation) {
        // Build Avro event using the generated builder
        ReservationCreatedEvent event = ReservationCreatedEvent.newBuilder()
            .setEventId(java.util.UUID.randomUUID().toString())
            .setEventType("RESERVATION_CREATED")
            .setTimestamp(java.time.Instant.now())
            .setVersion(1)
            .setReservationId(reservation.getId() != null ? reservation.getId().toString() : "")
            .setGuestId(reservation.getGuestId() != null ? reservation.getGuestId().toString() : "")
            .setPropertyId(reservation.getPropertyId() != null ? reservation.getPropertyId().toString() : "")
            .setRoomTypeId(reservation.getRoomTypeId() != null ? reservation.getRoomTypeId().toString() : "")
            .setCheckInDate(reservation.getCheckInDate() != null ? reservation.getCheckInDate().toString() : "")
            .setCheckOutDate(reservation.getCheckOutDate() != null ? reservation.getCheckOutDate().toString() : "")
            .setTotalAmount(reservation.getTotalAmount() != null ? reservation.getTotalAmount().toString() : "0")
            .setStatus(reservation.getStatus().name())
            .setNumberOfGuests(reservation.getAdults() +
                (reservation.getChildren() != null ? reservation.getChildren() : 0) +
                (reservation.getInfants() != null ? reservation.getInfants() : 0))
            .build();

        // Publish asynchronously for better performance
        eventPublisher.publishAsync("reservation.created", event);
        log.info("Published Avro ReservationCreatedEvent for reservation: {}", reservation.getConfirmationNumber());
    }

    /**
     * Map Reservation entity to lightweight summary DTO
     * Only includes essential fields for list/grid displays
     */
    private ReservationSummaryDTO mapToSummaryDTO(Reservation reservation) {
        int totalGuests = reservation.getAdults() +
            (reservation.getChildren() != null ? reservation.getChildren() : 0) +
            (reservation.getInfants() != null ? reservation.getInfants() : 0);

        return new ReservationSummaryDTO(
            reservation.getId(),
            reservation.getConfirmationNumber(),
            reservation.getGuestFirstName() + " " + reservation.getGuestLastName(),
            reservation.getGuestEmail(),
            reservation.getCheckInDate(),
            reservation.getCheckOutDate(),
            reservation.getNights(),
            reservation.getRoomNumber(),
            reservation.getStatus(),
            reservation.getTotalAmount(),
            reservation.getCurrency(),
            totalGuests
        );
    }

    private ReservationResponseDTO mapToResponseDTO(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getConfirmationNumber(),
                reservation.getPropertyId(),
                reservation.getGuestId(),
                reservation.getGuestFirstName(),
                reservation.getGuestLastName(),
                reservation.getGuestEmail(),
                reservation.getGuestPhone(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getNights(),
                reservation.getRoomTypeId(),
                reservation.getRoomNumber(),
                reservation.getAdults(),
                reservation.getChildren(),
                reservation.getInfants(),
                reservation.getRoomRate(),
                reservation.getTaxes(),
                reservation.getFees(),
                reservation.getTotalAmount(),
                reservation.getCurrency(),
                reservation.getStatus(),
                reservation.getSource(),
                reservation.getSpecialRequests(),
                reservation.getInternalNotes(),
                reservation.getBookingDate(),
                reservation.getArrivalTime(),
                reservation.getDepartureTime(),
                reservation.getCancelledAt(),
                reservation.getCancellationReason(),
                reservation.getCancelledBy(),
                reservation.getPaymentMethod(),
                reservation.getPaymentStatus(),
                reservation.getDepositAmount(),
                reservation.getDepositDueDate(),
                reservation.getChannelReference(),
                reservation.getCommissionRate(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                reservation.getCreatedBy(),
                reservation.getUpdatedBy(),
                reservation.getVersion()
        );
    }
}
