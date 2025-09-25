package com.modernreservation.availabilitycalculator.service;

import com.modernreservation.availabilitycalculator.dto.*;
import com.modernreservation.availabilitycalculator.entity.RoomAvailability;
import com.modernreservation.availabilitycalculator.repository.RoomAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for availability calculations and management
 *
 * Simplified implementation focusing on core functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AvailabilityService {

    private final RoomAvailabilityRepository availabilityRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Search for available rooms based on criteria
     */
    @Cacheable(value = "availability-search", key = "#request.propertyId + '-' + #request.checkInDate + '-' + #request.checkOutDate")
    public List<AvailabilityResponseDTO> searchAvailability(AvailabilitySearchRequestDTO request) {
        log.info("Searching availability for property: {}, dates: {} to {}",
                request.propertyId(), request.checkInDate(), request.checkOutDate());

        // Get availability data
        List<RoomAvailability> availabilities = availabilityRepository
                .findAvailableRoomsByPropertyAndDateRange(
                        request.propertyId(),
                        request.checkInDate(),
                        request.checkOutDate()
                );

        // Convert to response DTOs
        return availabilities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Calculate pricing for a specific request
     */
    @Cacheable(value = "pricing-calculation", key = "#request.propertyId + '-' + #request.roomTypeId + '-' + #request.checkInDate")
    public PricingCalculationResponseDTO calculatePricing(PricingCalculationRequestDTO request) {
        log.info("Calculating pricing for property: {}, room type: {}",
                request.propertyId(), request.roomTypeId());

        int nights = request.getNights();

        // Get base rate
        Optional<RoomAvailability> availabilityOpt = availabilityRepository
                .findByPropertyIdAndRoomTypeIdAndAvailabilityDate(
                        request.propertyId(),
                        request.roomTypeId(),
                        request.checkInDate()
                );

        if (availabilityOpt.isEmpty()) {
            throw new RuntimeException("Room availability not found");
        }

        RoomAvailability availability = availabilityOpt.get();
        BigDecimal baseRate = availability.getBaseRate();
        BigDecimal currentRate = availability.getCurrentRate();

        // Calculate totals
        BigDecimal subtotal = currentRate.multiply(BigDecimal.valueOf(nights));
        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.12)); // 12% tax
        BigDecimal serviceFeeAmount = subtotal.multiply(BigDecimal.valueOf(0.05)); // 5% service fee
        BigDecimal totalAmount = subtotal.add(taxAmount).add(serviceFeeAmount);

        return new PricingCalculationResponseDTO(
                baseRate,
                currentRate,
                nights,
                subtotal,
                BigDecimal.ZERO, // discount
                BigDecimal.ZERO, // discount percentage
                taxAmount,
                BigDecimal.valueOf(12.0), // tax percentage
                serviceFeeAmount,
                BigDecimal.valueOf(5.0), // service fee percentage
                totalAmount,
                request.currency(),
                new ArrayList<>(), // promo codes
                request.pricingMethod().name(),
                new ArrayList<>() // price breakdown
        );
    }

    /**
     * Update room availability
     */
    @CacheEvict(value = {"availability-search", "pricing-calculation"}, allEntries = true)
    public AvailabilityResponseDTO updateAvailability(UUID availabilityId, AvailabilityUpdateRequestDTO request, String updatedBy) {
        log.info("Updating availability: {} by user: {}", availabilityId, updatedBy);

        Optional<RoomAvailability> availabilityOpt = availabilityRepository.findById(availabilityId);
        if (availabilityOpt.isEmpty()) {
            throw new RuntimeException("Availability record not found");
        }

        RoomAvailability availability = availabilityOpt.get();

        // Update fields if provided
        if (request.availabilityStatus() != null) {
            availability.setAvailabilityStatus(request.availabilityStatus());
        }
        if (request.baseRate() != null) {
            availability.setBaseRate(request.baseRate());
            availability.setCurrentRate(request.baseRate()); // Simple implementation
        }
        if (request.totalRooms() != null) {
            availability.setTotalRooms(request.totalRooms());
        }
        if (request.availableRooms() != null) {
            availability.setAvailableRooms(request.availableRooms());
        }
        if (request.occupiedRooms() != null) {
            availability.setOccupiedRooms(request.occupiedRooms());
        }
        if (request.minimumStay() != null) {
            availability.setMinimumStay(request.minimumStay());
        }
        if (request.maximumStay() != null) {
            availability.setMaximumStay(request.maximumStay());
        }
        if (request.closedToArrival() != null) {
            availability.setClosedToArrival(request.closedToArrival());
        }
        if (request.closedToDeparture() != null) {
            availability.setClosedToDeparture(request.closedToDeparture());
        }
        if (request.stopSell() != null) {
            availability.setStopSell(request.stopSell());
        }

        // Save updated availability
        RoomAvailability saved = availabilityRepository.save(availability);

        // Publish event
        publishAvailabilityUpdatedEvent(saved);

        return convertToResponseDTO(saved);
    }

    /**
     * Get availability by ID
     */
    @Cacheable(value = "availability-details", key = "#availabilityId")
    public AvailabilityResponseDTO getAvailabilityById(UUID availabilityId) {
        log.info("Getting availability by ID: {}", availabilityId);

        Optional<RoomAvailability> availabilityOpt = availabilityRepository.findById(availabilityId);
        if (availabilityOpt.isEmpty()) {
            throw new RuntimeException("Availability record not found");
        }

        return convertToResponseDTO(availabilityOpt.get());
    }

    /**
     * Get occupancy statistics
     */
    @Cacheable(value = "occupancy-stats", key = "#propertyId + '-' + #date")
    public OccupancyStatsDTO getOccupancyStats(UUID propertyId, LocalDate date) {
        log.info("Getting occupancy stats for property: {}, date: {}", propertyId, date);

        List<RoomAvailability> availabilities = availabilityRepository
                .findByPropertyIdAndAvailabilityDate(propertyId, date);

        if (availabilities.isEmpty()) {
            throw new RuntimeException("No availability data found");
        }

        // Calculate totals
        int totalRooms = availabilities.stream().mapToInt(RoomAvailability::getTotalRooms).sum();
        int occupiedRooms = availabilities.stream().mapToInt(RoomAvailability::getOccupiedRooms).sum();
        int availableRooms = availabilities.stream().mapToInt(RoomAvailability::getAvailableRooms).sum();

        BigDecimal occupancyPercentage = totalRooms > 0 ?
                BigDecimal.valueOf((double) occupiedRooms / totalRooms * 100) : BigDecimal.ZERO;

        BigDecimal availabilityPercentage = totalRooms > 0 ?
                BigDecimal.valueOf((double) availableRooms / totalRooms * 100) : BigDecimal.ZERO;

        // Calculate average daily rate
        BigDecimal totalRevenue = availabilities.stream()
                .map(a -> a.getCurrentRate().multiply(BigDecimal.valueOf(a.getOccupiedRooms())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal adr = occupiedRooms > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(occupiedRooms), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal revPAR = totalRooms > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalRooms), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return new OccupancyStatsDTO(
                propertyId,
                date,
                totalRooms,
                occupiedRooms,
                availableRooms,
                0, // maintenance rooms
                0, // blocked rooms
                occupancyPercentage,
                availabilityPercentage,
                adr,
                revPAR,
                totalRevenue,
                "USD",
                occupancyPercentage, // pickup percentage (simplified)
                0, // no show count
                0, // walk-in count
                0  // cancellation count
        );
    }

    /**
     * Convert entity to response DTO
     */
    private AvailabilityResponseDTO convertToResponseDTO(RoomAvailability availability) {
        return new AvailabilityResponseDTO(
                availability.getId(),
                availability.getPropertyId(),
                availability.getRoomTypeId(),
                availability.getRoomCategory(),
                availability.getAvailabilityDate(),
                availability.getAvailabilityStatus(),
                availability.getBaseRate(),
                availability.getCurrentRate(),
                availability.getTotalRooms(),
                availability.getAvailableRooms(),
                availability.getOccupiedRooms(),
                availability.getMinimumStay(),
                availability.getMaximumStay(),
                availability.getClosedToArrival(),
                availability.getClosedToDeparture(),
                availability.getStopSell(),
                availability.getCurrency(),
                null, // total price - calculated separately
                availability.getCurrentRate(), // price per night
                BigDecimal.ZERO, // discount
                BigDecimal.ZERO, // taxes
                availability.getCurrentRate() // final price (simplified)
        );
    }

    /**
     * Get daily availability for a property (used by controller)
     */
    @Cacheable(value = "daily-availability", key = "#propertyId + '-' + #date")
    public List<AvailabilityResponseDTO> getDailyAvailability(UUID propertyId, LocalDate date) {
        log.info("Getting daily availability for property: {}, date: {}", propertyId, date);

        List<RoomAvailability> availabilities = availabilityRepository
                .findByPropertyIdAndAvailabilityDate(propertyId, date);

        return availabilities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get availability with pagination (used by controller)
     */
    @Cacheable(value = "paginated-availability", key = "#propertyId + '-' + #date + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<AvailabilityResponseDTO> getAvailabilityWithPagination(UUID propertyId, LocalDate date, Pageable pageable) {
        log.info("Getting paginated availability for property: {}, date: {}", propertyId, date);

        List<RoomAvailability> allAvailabilities = availabilityRepository
                .findByPropertyIdAndAvailabilityDate(propertyId, date);

        List<AvailabilityResponseDTO> dtos = allAvailabilities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        List<AvailabilityResponseDTO> pageContent = dtos.subList(start, end);

        return new PageImpl<>(pageContent, pageable, dtos.size());
    }

    /**
     * Book rooms (simplified implementation)
     */
    @CacheEvict(value = {"availability-search", "daily-availability", "paginated-availability"}, allEntries = true)
    public AvailabilityResponseDTO bookRooms(UUID propertyId, UUID roomTypeId, LocalDate checkInDate,
                                           LocalDate checkOutDate, int roomCount, String reservationId) {
        log.info("Booking {} rooms for property: {}, room type: {}, dates: {} to {}",
                roomCount, propertyId, roomTypeId, checkInDate, checkOutDate);

        // Get availability for check-in date
        Optional<RoomAvailability> availabilityOpt = availabilityRepository
                .findByPropertyIdAndRoomTypeIdAndAvailabilityDate(propertyId, roomTypeId, checkInDate);

        if (availabilityOpt.isEmpty()) {
            throw new RuntimeException("No availability found for booking");
        }

        RoomAvailability availability = availabilityOpt.get();

        if (availability.getAvailableRooms() < roomCount) {
            throw new RuntimeException("Not enough rooms available");
        }

        // Update availability
        availability.setAvailableRooms(availability.getAvailableRooms() - roomCount);
        availability.setOccupiedRooms(availability.getOccupiedRooms() + roomCount);

        RoomAvailability saved = availabilityRepository.save(availability);

        // Publish booking event
        publishBookingEvent(saved, roomCount, reservationId);

        return convertToResponseDTO(saved);
    }

    /**
     * Get occupancy statistics for date range (used by controller)
     */
    @Cacheable(value = "occupancy-statistics", key = "#propertyId + '-' + #startDate + '-' + #endDate")
    public OccupancyStatsDTO getOccupancyStatistics(UUID propertyId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting occupancy statistics for property: {}, dates: {} to {}", propertyId, startDate, endDate);

        // For simplicity, return stats for start date
        return getOccupancyStats(propertyId, startDate);
    }

    /**
     * Publish booking event
     */
    private void publishBookingEvent(RoomAvailability availability, int roomCount, String reservationId) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", "ROOMS_BOOKED",
                    "availabilityId", availability.getId(),
                    "propertyId", availability.getPropertyId(),
                    "roomTypeId", availability.getRoomTypeId(),
                    "availabilityDate", availability.getAvailabilityDate(),
                    "roomCount", roomCount,
                    "reservationId", reservationId,
                    "timestamp", System.currentTimeMillis()
            );

            kafkaTemplate.send("booking-events", event);
            log.info("Published booking event for: {}", availability.getId());
        } catch (Exception e) {
            log.error("Failed to publish booking event", e);
        }
    }

    /**
     * Publish availability updated event
     */
    private void publishAvailabilityUpdatedEvent(RoomAvailability availability) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", "AVAILABILITY_UPDATED",
                    "availabilityId", availability.getId(),
                    "propertyId", availability.getPropertyId(),
                    "roomTypeId", availability.getRoomTypeId(),
                    "availabilityDate", availability.getAvailabilityDate(),
                    "timestamp", System.currentTimeMillis()
            );

            kafkaTemplate.send("availability-events", event);
            log.info("Published availability updated event for: {}", availability.getId());
        } catch (Exception e) {
            log.error("Failed to publish availability updated event", e);
        }
    }
}
