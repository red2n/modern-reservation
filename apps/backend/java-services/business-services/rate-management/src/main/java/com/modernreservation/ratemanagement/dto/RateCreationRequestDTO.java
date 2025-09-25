package com.modernreservation.ratemanagement.dto;

import com.modernreservation.ratemanagement.enums.RateStrategy;
import com.modernreservation.ratemanagement.enums.SeasonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a new rate
 */
@Schema(description = "Rate creation request")
public record RateCreationRequestDTO(

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "Property ID is required")
    UUID propertyId,

    @Schema(description = "Room type ID", example = "550e8400-e29b-41d4-a716-446655440001")
    @NotNull(message = "Room type ID is required")
    UUID roomTypeId,

    @Schema(description = "Rate code", example = "STD-RACK")
    @NotBlank(message = "Rate code is required")
    @Size(max = 50, message = "Rate code must not exceed 50 characters")
    String rateCode,

    @Schema(description = "Rate name", example = "Standard Rack Rate")
    @NotBlank(message = "Rate name is required")
    @Size(max = 100, message = "Rate name must not exceed 100 characters")
    String rateName,

    @Schema(description = "Rate description", example = "Standard published rate for walk-in guests")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @Schema(description = "Rate strategy", example = "DYNAMIC")
    @NotNull(message = "Rate strategy is required")
    RateStrategy rateStrategy,

    @Schema(description = "Season type", example = "REGULAR")
    SeasonType seasonType,

    @Schema(description = "Base rate amount", example = "200.00")
    @NotNull(message = "Base rate is required")
    @DecimalMin(value = "0.01", message = "Base rate must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Base rate must have at most 2 decimal places")
    BigDecimal baseRate,

    @Schema(description = "Minimum allowed rate", example = "150.00")
    @DecimalMin(value = "0.01", message = "Minimum rate must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Minimum rate must have at most 2 decimal places")
    BigDecimal minimumRate,

    @Schema(description = "Maximum allowed rate", example = "500.00")
    @DecimalMin(value = "0.01", message = "Maximum rate must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Maximum rate must have at most 2 decimal places")
    BigDecimal maximumRate,

    @Schema(description = "Currency code", example = "USD")
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,

    @Schema(description = "Effective date", example = "2024-01-01")
    @NotNull(message = "Effective date is required")
    @FutureOrPresent(message = "Effective date must be today or in the future")
    LocalDate effectiveDate,

    @Schema(description = "Expiry date", example = "2024-12-31")
    @Future(message = "Expiry date must be in the future")
    LocalDate expiryDate,

    @Schema(description = "Minimum stay nights", example = "1")
    @Min(value = 1, message = "Minimum stay must be at least 1 night")
    @Max(value = 365, message = "Minimum stay cannot exceed 365 nights")
    Integer minimumStay,

    @Schema(description = "Maximum stay nights", example = "30")
    @Min(value = 1, message = "Maximum stay must be at least 1 night")
    @Max(value = 365, message = "Maximum stay cannot exceed 365 nights")
    Integer maximumStay,

    @Schema(description = "Advance booking days required", example = "0")
    @Min(value = 0, message = "Advance booking days cannot be negative")
    Integer advanceBookingDays,

    @Schema(description = "Maximum booking days allowed", example = "365")
    @Min(value = 1, message = "Maximum booking days must be at least 1")
    Integer maximumBookingDays,

    @Schema(description = "Is refundable", example = "true")
    @NotNull(message = "Refundable flag is required")
    Boolean isRefundable,

    @Schema(description = "Is modifiable", example = "true")
    @NotNull(message = "Modifiable flag is required")
    Boolean isModifiable,

    @Schema(description = "Cancellation hours before check-in", example = "24")
    @Min(value = 0, message = "Cancellation hours cannot be negative")
    Integer cancellationHours,

    @Schema(description = "Tax inclusive", example = "false")
    @NotNull(message = "Tax inclusive flag is required")
    Boolean taxInclusive,

    @Schema(description = "Service fee inclusive", example = "false")
    @NotNull(message = "Service fee inclusive flag is required")
    Boolean serviceFeeInclusive,

    @Schema(description = "Priority order for rate selection", example = "1")
    @Min(value = 1, message = "Priority order must be at least 1")
    @Max(value = 100, message = "Priority order cannot exceed 100")
    Integer priorityOrder,

    @Schema(description = "Created by user", example = "admin@hotel.com")
    @NotBlank(message = "Created by is required")
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    String createdBy

) {

    /**
     * Validate minimum and maximum rates relationship
     */
    public boolean isRateRangeValid() {
        if (minimumRate != null && maximumRate != null) {
            return minimumRate.compareTo(maximumRate) <= 0;
        }
        return true;
    }

    /**
     * Validate stay duration relationship
     */
    public boolean isStayDurationValid() {
        if (minimumStay != null && maximumStay != null) {
            return minimumStay <= maximumStay;
        }
        return true;
    }

    /**
     * Validate booking window relationship
     */
    public boolean isBookingWindowValid() {
        if (advanceBookingDays != null && maximumBookingDays != null) {
            return advanceBookingDays <= maximumBookingDays;
        }
        return true;
    }

    /**
     * Validate date range
     */
    public boolean isDateRangeValid() {
        if (expiryDate != null) {
            return effectiveDate.isBefore(expiryDate);
        }
        return true;
    }

    /**
     * Validate base rate against minimum/maximum
     */
    public boolean isBaseRateInRange() {
        if (minimumRate != null && baseRate.compareTo(minimumRate) < 0) {
            return false;
        }
        if (maximumRate != null && baseRate.compareTo(maximumRate) > 0) {
            return false;
        }
        return true;
    }

    /**
     * Comprehensive validation
     */
    public boolean isValid() {
        return isRateRangeValid() &&
               isStayDurationValid() &&
               isBookingWindowValid() &&
               isDateRangeValid() &&
               isBaseRateInRange();
    }
}
