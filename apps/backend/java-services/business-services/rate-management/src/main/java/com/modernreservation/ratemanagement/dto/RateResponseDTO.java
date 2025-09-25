package com.modernreservation.ratemanagement.dto;

import com.modernreservation.ratemanagement.enums.RateStatus;
import com.modernreservation.ratemanagement.enums.RateStrategy;
import com.modernreservation.ratemanagement.enums.SeasonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for rate information
 */
@Schema(description = "Rate information response")
public record RateResponseDTO(

    @Schema(description = "Rate ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID propertyId,

    @Schema(description = "Room type ID", example = "550e8400-e29b-41d4-a716-446655440002")
    UUID roomTypeId,

    @Schema(description = "Rate code", example = "STD-RACK")
    String rateCode,

    @Schema(description = "Rate name", example = "Standard Rack Rate")
    String rateName,

    @Schema(description = "Rate description", example = "Standard published rate for walk-in guests")
    String description,

    @Schema(description = "Rate strategy", example = "DYNAMIC")
    RateStrategy rateStrategy,

    @Schema(description = "Rate status", example = "ACTIVE")
    RateStatus rateStatus,

    @Schema(description = "Season type", example = "REGULAR")
    SeasonType seasonType,

    @Schema(description = "Base rate amount", example = "200.00")
    BigDecimal baseRate,

    @Schema(description = "Current calculated rate", example = "220.00")
    BigDecimal currentRate,

    @Schema(description = "Minimum allowed rate", example = "150.00")
    BigDecimal minimumRate,

    @Schema(description = "Maximum allowed rate", example = "500.00")
    BigDecimal maximumRate,

    @Schema(description = "Currency code", example = "USD")
    String currency,

    @Schema(description = "Effective date", example = "2024-01-01")
    LocalDate effectiveDate,

    @Schema(description = "Expiry date", example = "2024-12-31")
    LocalDate expiryDate,

    @Schema(description = "Minimum stay nights", example = "1")
    Integer minimumStay,

    @Schema(description = "Maximum stay nights", example = "30")
    Integer maximumStay,

    @Schema(description = "Advance booking days required", example = "0")
    Integer advanceBookingDays,

    @Schema(description = "Maximum booking days allowed", example = "365")
    Integer maximumBookingDays,

    @Schema(description = "Is refundable", example = "true")
    Boolean isRefundable,

    @Schema(description = "Is modifiable", example = "true")
    Boolean isModifiable,

    @Schema(description = "Cancellation hours before check-in", example = "24")
    Integer cancellationHours,

    @Schema(description = "Tax inclusive", example = "false")
    Boolean taxInclusive,

    @Schema(description = "Service fee inclusive", example = "false")
    Boolean serviceFeeInclusive,

    @Schema(description = "Occupancy multiplier", example = "1.2")
    BigDecimal occupancyMultiplier,

    @Schema(description = "Demand multiplier", example = "1.1")
    BigDecimal demandMultiplier,

    @Schema(description = "Competitive adjustment", example = "10.00")
    BigDecimal competitiveAdjustment,

    @Schema(description = "Priority order", example = "1")
    Integer priorityOrder,

    @Schema(description = "Is active", example = "true")
    Boolean isActive,

    @Schema(description = "Created by user", example = "admin@hotel.com")
    String createdBy,

    @Schema(description = "Updated by user", example = "manager@hotel.com")
    String updatedBy,

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp", example = "2024-01-02T15:30:00")
    LocalDateTime updatedAt,

    @Schema(description = "Version for optimistic locking", example = "1")
    Long version

) {

    /**
     * Calculate the final rate with all adjustments
     */
    public BigDecimal getFinalRate() {
        return currentRate != null ? currentRate : baseRate;
    }

    /**
     * Get the discount percentage from base rate
     */
    public BigDecimal getDiscountPercentage() {
        if (baseRate == null || currentRate == null || baseRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal difference = baseRate.subtract(currentRate);
        return difference.divide(baseRate, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Get the markup percentage from base rate
     */
    public BigDecimal getMarkupPercentage() {
        if (baseRate == null || currentRate == null || baseRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal difference = currentRate.subtract(baseRate);
        return difference.divide(baseRate, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if rate is discounted
     */
    public boolean isDiscounted() {
        return currentRate != null && baseRate != null &&
               currentRate.compareTo(baseRate) < 0;
    }

    /**
     * Check if rate has markup
     */
    public boolean hasMarkup() {
        return currentRate != null && baseRate != null &&
               currentRate.compareTo(baseRate) > 0;
    }

    /**
     * Check if rate is available for booking
     */
    public boolean isBookable() {
        return isActive && rateStatus == RateStatus.ACTIVE;
    }

    /**
     * Get days until expiry (-1 if no expiry)
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return -1;
        }
        return LocalDate.now().until(expiryDate).getDays();
    }

    /**
     * Check if rate is expiring soon (within 30 days)
     */
    public boolean isExpiringSoon() {
        long daysUntilExpiry = getDaysUntilExpiry();
        return daysUntilExpiry >= 0 && daysUntilExpiry <= 30;
    }

    /**
     * Get revenue impact level based on rate strategy and season
     */
    public String getRevenueImpact() {
        if (seasonType != null && seasonType.isPremium()) {
            return "HIGH";
        }
        if (rateStrategy == RateStrategy.REVENUE_OPTIMIZATION) {
            return "VERY_HIGH";
        }
        if (rateStrategy.isDynamic()) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
