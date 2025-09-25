package com.modernreservation.ratemanagement.dto;

import com.modernreservation.ratemanagement.enums.RateStrategy;
import com.modernreservation.ratemanagement.enums.SeasonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for searching rates
 */
@Schema(description = "Rate search request")
public record RateSearchRequestDTO(

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "Property ID is required")
    UUID propertyId,

    @Schema(description = "Room type ID", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID roomTypeId,

    @Schema(description = "Check-in date", example = "2024-12-25")
    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    LocalDate checkInDate,

    @Schema(description = "Check-out date", example = "2024-12-27")
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    LocalDate checkOutDate,

    @Schema(description = "Number of guests", example = "2")
    @Min(value = 1, message = "Number of guests must be at least 1")
    @Max(value = 10, message = "Number of guests cannot exceed 10")
    Integer numberOfGuests,

    @Schema(description = "Rate strategy filter")
    RateStrategy rateStrategy,

    @Schema(description = "Season type filter")
    SeasonType seasonType,

    @Schema(description = "Minimum rate", example = "100.00")
    @DecimalMin(value = "0.01", message = "Minimum rate must be greater than zero")
    BigDecimal minRate,

    @Schema(description = "Maximum rate", example = "1000.00")
    @DecimalMin(value = "0.01", message = "Maximum rate must be greater than zero")
    BigDecimal maxRate,

    @Schema(description = "Currency code", example = "USD")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,

    @Schema(description = "Include inactive rates", example = "false")
    Boolean includeInactive,

    @Schema(description = "Refundable rates only", example = "true")
    Boolean refundableOnly,

    @Schema(description = "Modifiable rates only", example = "false")
    Boolean modifiableOnly,

    @Schema(description = "Advance booking days", example = "7")
    @Min(value = 0, message = "Advance booking days cannot be negative")
    Integer advanceBookingDays

) {

    /**
     * Get the number of nights for the stay
     */
    public int getNights() {
        if (checkInDate != null && checkOutDate != null) {
            return (int) checkInDate.until(checkOutDate).getDays();
        }
        return 0;
    }

    /**
     * Validate check-in and check-out dates
     */
    public boolean isDateRangeValid() {
        if (checkInDate != null && checkOutDate != null) {
            return checkInDate.isBefore(checkOutDate);
        }
        return true;
    }

    /**
     * Validate rate range
     */
    public boolean isRateRangeValid() {
        if (minRate != null && maxRate != null) {
            return minRate.compareTo(maxRate) <= 0;
        }
        return true;
    }

    /**
     * Check if this is a last-minute booking (within 3 days)
     */
    public boolean isLastMinuteBooking() {
        if (checkInDate == null) return false;
        return LocalDate.now().until(checkInDate).getDays() <= 3;
    }

    /**
     * Check if this is an advance booking (more than 30 days)
     */
    public boolean isAdvanceBooking() {
        if (checkInDate == null) return false;
        return LocalDate.now().until(checkInDate).getDays() > 30;
    }

    /**
     * Get booking lead time in days
     */
    public long getBookingLeadTime() {
        if (checkInDate == null) return 0;
        return LocalDate.now().until(checkInDate).getDays();
    }

    /**
     * Check if stay duration is valid (at least 1 night)
     */
    public boolean isStayDurationValid() {
        return getNights() >= 1;
    }

    /**
     * Get season based on check-in date
     */
    public SeasonType inferSeasonType() {
        if (checkInDate == null) return SeasonType.REGULAR;

        int month = checkInDate.getMonthValue();

        // Basic seasonal inference (can be customized per property)
        return switch (month) {
            case 12, 1, 2 -> SeasonType.PEAK;      // Winter holidays
            case 6, 7, 8 -> SeasonType.HIGH;       // Summer
            case 3, 4, 5, 9, 10 -> SeasonType.REGULAR; // Spring/Fall
            case 11 -> SeasonType.LOW;              // November
            default -> SeasonType.REGULAR;
        };
    }

    /**
     * Comprehensive validation
     */
    public boolean isValid() {
        return isDateRangeValid() &&
               isRateRangeValid() &&
               isStayDurationValid();
    }
}
