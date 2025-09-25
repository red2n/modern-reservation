package com.modernreservation.ratemanagement.service;

import com.modernreservation.ratemanagement.dto.*;
import com.modernreservation.ratemanagement.entity.Rate;
import com.modernreservation.ratemanagement.enums.RateStatus;
import com.modernreservation.ratemanagement.enums.RateStrategy;
import com.modernreservation.ratemanagement.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for rate management and pricing calculations
 *
 * Handles rate creation, updates, pricing strategies, and revenue optimization
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RateService {

    private final RateRepository rateRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Create a new rate
     */
    @CacheEvict(value = {"rate-search", "best-rates", "rate-statistics"}, allEntries = true)
    public RateResponseDTO createRate(RateCreationRequestDTO request) {
        log.info("Creating new rate: {} for property: {}", request.rateCode(), request.propertyId());

        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid rate creation request");
        }

        // Check for duplicate rate code
        if (rateRepository.existsDuplicateRateCode(
                request.propertyId(),
                request.roomTypeId(),
                request.rateCode(),
                UUID.randomUUID())) {
            throw new IllegalArgumentException("Rate code already exists for this property and room type");
        }

        // Create rate entity
        Rate rate = Rate.builder()
                .propertyId(request.propertyId())
                .roomTypeId(request.roomTypeId())
                .rateCode(request.rateCode())
                .rateName(request.rateName())
                .description(request.description())
                .rateStrategy(request.rateStrategy())
                .rateStatus(RateStatus.DRAFT)
                .seasonType(request.seasonType())
                .baseRate(request.baseRate())
                .currentRate(request.baseRate())
                .minimumRate(request.minimumRate())
                .maximumRate(request.maximumRate())
                .currency(request.currency())
                .effectiveDate(request.effectiveDate())
                .expiryDate(request.expiryDate())
                .minimumStay(request.minimumStay())
                .maximumStay(request.maximumStay())
                .advanceBookingDays(request.advanceBookingDays())
                .maximumBookingDays(request.maximumBookingDays())
                .isRefundable(request.isRefundable())
                .isModifiable(request.isModifiable())
                .cancellationHours(request.cancellationHours())
                .taxInclusive(request.taxInclusive())
                .serviceFeeInclusive(request.serviceFeeInclusive())
                .priorityOrder(request.priorityOrder())
                .isActive(true)
                .createdBy(request.createdBy())
                .build();

        // Save rate
        Rate savedRate = rateRepository.save(rate);

        // Publish event
        publishRateCreatedEvent(savedRate);

        log.info("Successfully created rate: {} with ID: {}", savedRate.getRateCode(), savedRate.getId());
        return convertToResponseDTO(savedRate);
    }

    /**
     * Search for rates based on criteria
     */
    @Cacheable(value = "rate-search", key = "#request.propertyId + '-' + #request.roomTypeId + '-' + #request.checkInDate")
    public List<RateResponseDTO> searchRates(RateSearchRequestDTO request) {
        log.info("Searching rates for property: {}, room type: {}, dates: {} to {}",
                request.propertyId(), request.roomTypeId(), request.checkInDate(), request.checkOutDate());

        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid rate search request");
        }

        // Search rates
        List<Rate> rates = rateRepository.searchRates(
                request.propertyId(),
                request.roomTypeId(),
                request.checkInDate(),
                request.rateStrategy(),
                request.seasonType(),
                request.minRate(),
                request.maxRate(),
                request.refundableOnly(),
                request.modifiableOnly(),
                request.advanceBookingDays()
        );

        // Convert to DTOs and apply dynamic pricing
        return rates.stream()
                .map(rate -> {
                    // Apply dynamic pricing adjustments
                    applyDynamicPricing(rate, request.checkInDate(), request.getNights());
                    return convertToResponseDTO(rate);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get the best available rate for specific criteria
     */
    @Cacheable(value = "best-rates", key = "#propertyId + '-' + #roomTypeId + '-' + #date")
    public Optional<RateResponseDTO> getBestRate(UUID propertyId, UUID roomTypeId, LocalDate date, Integer nights) {
        log.info("Finding best rate for property: {}, room type: {}, date: {}", propertyId, roomTypeId, date);

        Optional<Rate> bestRate = rateRepository.findBestRateForRoomAndDate(propertyId, roomTypeId, date, nights, nights);

        return bestRate.map(rate -> {
            // Apply dynamic pricing
            applyDynamicPricing(rate, date, nights);
            return convertToResponseDTO(rate);
        });
    }

    /**
     * Get rate by ID
     */
    @Cacheable(value = "rate-details", key = "#rateId")
    public RateResponseDTO getRateById(UUID rateId) {
        log.info("Getting rate by ID: {}", rateId);

        Optional<Rate> rateOpt = rateRepository.findById(rateId);
        if (rateOpt.isEmpty()) {
            throw new RuntimeException("Rate not found");
        }

        return convertToResponseDTO(rateOpt.get());
    }

    /**
     * Update rate status
     */
    @CacheEvict(value = {"rate-search", "best-rates", "rate-details"}, allEntries = true)
    public RateResponseDTO updateRateStatus(UUID rateId, RateStatus newStatus, String updatedBy) {
        log.info("Updating rate status: {} to {}", rateId, newStatus);

        Optional<Rate> rateOpt = rateRepository.findById(rateId);
        if (rateOpt.isEmpty()) {
            throw new RuntimeException("Rate not found");
        }

        Rate rate = rateOpt.get();

        // Validate status transition
        if (!isValidStatusTransition(rate.getRateStatus(), newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " +
                    rate.getRateStatus() + " to " + newStatus);
        }

        // Update status
        rate.setRateStatus(newStatus);
        rate.setUpdatedBy(updatedBy);

        // If activating, set active flag
        if (newStatus == RateStatus.ACTIVE) {
            rate.setIsActive(true);
        } else if (newStatus == RateStatus.EXPIRED || newStatus == RateStatus.CANCELLED) {
            rate.setIsActive(false);
        }

        Rate savedRate = rateRepository.save(rate);

        // Publish event
        publishRateStatusUpdatedEvent(savedRate);

        return convertToResponseDTO(savedRate);
    }

    /**
     * Get rate statistics for a property
     */
    @Cacheable(value = "rate-statistics", key = "#propertyId + '-' + #date")
    public Map<String, Object> getRateStatistics(UUID propertyId, LocalDate date) {
        log.info("Getting rate statistics for property: {}, date: {}", propertyId, date);

        Map<String, Object> statistics = new HashMap<>();

        // Get basic statistics
        Optional<BigDecimal> minRate = rateRepository.findMinRateForPropertyAndDate(propertyId, date);
        Optional<BigDecimal> maxRate = rateRepository.findMaxRateForPropertyAndDate(propertyId, date);
        Optional<BigDecimal> avgRate = rateRepository.findAverageRateForPropertyAndDate(propertyId, date);

        statistics.put("minimumRate", minRate.orElse(BigDecimal.ZERO));
        statistics.put("maximumRate", maxRate.orElse(BigDecimal.ZERO));
        statistics.put("averageRate", avgRate.orElse(BigDecimal.ZERO));

        // Get rate count by strategy
        List<Rate> allRates = rateRepository.findActiveRatesForPropertyAndDate(propertyId, date);
        Map<RateStrategy, Long> ratesByStrategy = allRates.stream()
                .collect(Collectors.groupingBy(Rate::getRateStrategy, Collectors.counting()));

        statistics.put("ratesByStrategy", ratesByStrategy);
        statistics.put("totalActiveRates", allRates.size());

        return statistics;
    }

    /**
     * Get rates with pagination
     */
    public Page<RateResponseDTO> getRatesWithPagination(UUID propertyId, RateStatus status, Pageable pageable) {
        log.info("Getting paginated rates for property: {}, status: {}", propertyId, status);

        Page<Rate> ratePage = rateRepository.findRatesWithPagination(propertyId, status, pageable);

        List<RateResponseDTO> dtos = ratePage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, ratePage.getTotalElements());
    }

    /**
     * Expire outdated rates (scheduled task)
     */
    @CacheEvict(value = {"rate-search", "best-rates", "rate-statistics"}, allEntries = true)
    public int expireOutdatedRates() {
        log.info("Expiring outdated rates");

        int expiredCount = rateRepository.expireOutdatedRates();

        if (expiredCount > 0) {
            log.info("Expired {} outdated rates", expiredCount);
            publishRatesExpiredEvent(expiredCount);
        }

        return expiredCount;
    }

    /**
     * Apply dynamic pricing adjustments
     */
    private void applyDynamicPricing(Rate rate, LocalDate date, Integer nights) {
        if (!rate.getRateStrategy().isDynamic()) {
            return;
        }

        BigDecimal adjustedRate = rate.getBaseRate();

        // Apply season multiplier
        if (rate.getSeasonType() != null) {
            adjustedRate = adjustedRate.multiply(BigDecimal.valueOf(rate.getSeasonType().getMultiplier()));
        }

        // Apply occupancy-based pricing (simplified)
        if (rate.getOccupancyMultiplier() != null) {
            adjustedRate = adjustedRate.multiply(rate.getOccupancyMultiplier());
        }

        // Apply demand-based pricing
        if (rate.getDemandMultiplier() != null) {
            adjustedRate = adjustedRate.multiply(rate.getDemandMultiplier());
        }

        // Apply competitive adjustments
        if (rate.getCompetitiveAdjustment() != null) {
            adjustedRate = adjustedRate.add(rate.getCompetitiveAdjustment());
        }

        // Ensure rate is within bounds
        if (rate.getMinimumRate() != null && adjustedRate.compareTo(rate.getMinimumRate()) < 0) {
            adjustedRate = rate.getMinimumRate();
        }
        if (rate.getMaximumRate() != null && adjustedRate.compareTo(rate.getMaximumRate()) > 0) {
            adjustedRate = rate.getMaximumRate();
        }

        rate.setCurrentRate(adjustedRate);
    }

    /**
     * Validate status transition
     */
    private boolean isValidStatusTransition(RateStatus current, RateStatus target) {
        RateStatus[] allowedTransitions = current.getNextPossibleStates();
        return Arrays.asList(allowedTransitions).contains(target);
    }

    /**
     * Convert entity to response DTO
     */
    private RateResponseDTO convertToResponseDTO(Rate rate) {
        return new RateResponseDTO(
                rate.getId(),
                rate.getPropertyId(),
                rate.getRoomTypeId(),
                rate.getRateCode(),
                rate.getRateName(),
                rate.getDescription(),
                rate.getRateStrategy(),
                rate.getRateStatus(),
                rate.getSeasonType(),
                rate.getBaseRate(),
                rate.getCurrentRate(),
                rate.getMinimumRate(),
                rate.getMaximumRate(),
                rate.getCurrency(),
                rate.getEffectiveDate(),
                rate.getExpiryDate(),
                rate.getMinimumStay(),
                rate.getMaximumStay(),
                rate.getAdvanceBookingDays(),
                rate.getMaximumBookingDays(),
                rate.getIsRefundable(),
                rate.getIsModifiable(),
                rate.getCancellationHours(),
                rate.getTaxInclusive(),
                rate.getServiceFeeInclusive(),
                rate.getOccupancyMultiplier(),
                rate.getDemandMultiplier(),
                rate.getCompetitiveAdjustment(),
                rate.getPriorityOrder(),
                rate.getIsActive(),
                rate.getCreatedBy(),
                rate.getUpdatedBy(),
                rate.getCreatedAt(),
                rate.getUpdatedAt(),
                rate.getVersion()
        );
    }

    /**
     * Publish rate created event
     */
    private void publishRateCreatedEvent(Rate rate) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", "RATE_CREATED",
                    "rateId", rate.getId(),
                    "propertyId", rate.getPropertyId(),
                    "roomTypeId", rate.getRoomTypeId(),
                    "rateCode", rate.getRateCode(),
                    "rateStrategy", rate.getRateStrategy().name(),
                    "baseRate", rate.getBaseRate(),
                    "currency", rate.getCurrency(),
                    "timestamp", System.currentTimeMillis()
            );

            kafkaTemplate.send("rate-events", event);
            log.debug("Published rate created event for: {}", rate.getId());
        } catch (Exception e) {
            log.error("Failed to publish rate created event", e);
        }
    }

    /**
     * Publish rate status updated event
     */
    private void publishRateStatusUpdatedEvent(Rate rate) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", "RATE_STATUS_UPDATED",
                    "rateId", rate.getId(),
                    "propertyId", rate.getPropertyId(),
                    "rateCode", rate.getRateCode(),
                    "oldStatus", "UNKNOWN", // Could be tracked if needed
                    "newStatus", rate.getRateStatus().name(),
                    "timestamp", System.currentTimeMillis()
            );

            kafkaTemplate.send("rate-events", event);
            log.debug("Published rate status updated event for: {}", rate.getId());
        } catch (Exception e) {
            log.error("Failed to publish rate status updated event", e);
        }
    }

    /**
     * Publish rates expired event
     */
    private void publishRatesExpiredEvent(int expiredCount) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", "RATES_EXPIRED",
                    "expiredCount", expiredCount,
                    "timestamp", System.currentTimeMillis()
            );

            kafkaTemplate.send("rate-events", event);
            log.debug("Published rates expired event: {} rates expired", expiredCount);
        } catch (Exception e) {
            log.error("Failed to publish rates expired event", e);
        }
    }
}
