package com.modernreservation.availabilitycalculator.dto;

import com.modernreservation.availabilitycalculator.enums.PricingMethod;
import com.modernreservation.availabilitycalculator.enums.RoomCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for pricing calculations
 */
@Schema(description = "Pricing calculation request")
public record PricingCalculationRequestDTO(

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @NotNull(message = "Property ID is required")
    UUID propertyId,

    @Schema(description = "Room type ID", example = "660e8400-e29b-41d4-a716-446655440001", required = true)
    @NotNull(message = "Room type ID is required")
    UUID roomTypeId,

    @Schema(description = "Check-in date", example = "2024-12-25", required = true)
    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    LocalDate checkInDate,

    @Schema(description = "Check-out date", example = "2024-12-27", required = true)
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    LocalDate checkOutDate,

    @Schema(description = "Number of adults", example = "2", required = true)
    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "At least 1 adult is required")
    @Max(value = 20, message = "Maximum 20 adults allowed")
    Integer adults,

    @Schema(description = "Number of children", example = "1")
    @Min(value = 0, message = "Children count cannot be negative")
    @Max(value = 20, message = "Maximum 20 children allowed")
    Integer children,

    @Schema(description = "Room category", example = "DELUXE")
    RoomCategory roomCategory,

    @Schema(description = "Pricing method to use", example = "DYNAMIC")
    PricingMethod pricingMethod,

    @Schema(description = "Promotional code", example = "SAVE20")
    @Size(max = 50, message = "Promotional code cannot exceed 50 characters")
    String promoCode,

    @Schema(description = "Corporate rate code", example = "CORP123")
    @Size(max = 50, message = "Corporate rate code cannot exceed 50 characters")
    String corporateCode,

    @Schema(description = "Market segment", example = "BUSINESS")
    @Size(max = 50, message = "Market segment cannot exceed 50 characters")
    String marketSegment,

    @Schema(description = "Booking channel", example = "DIRECT")
    @Size(max = 50, message = "Booking channel cannot exceed 50 characters")
    String channel,

    @Schema(description = "Include taxes in calculation", example = "true")
    Boolean includeTaxes,

    @Schema(description = "Tax rate override", example = "0.12")
    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "1.0", message = "Tax rate cannot exceed 100%")
    BigDecimal taxRate,

    @Schema(description = "Service fee rate", example = "0.05")
    @DecimalMin(value = "0.0", message = "Service fee rate cannot be negative")
    @DecimalMax(value = "1.0", message = "Service fee rate cannot exceed 100%")
    BigDecimal serviceFeeRate,

    @Schema(description = "Currency code", example = "USD")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    String currency
) {

    public PricingCalculationRequestDTO {
        if (children == null) children = 0;
        if (pricingMethod == null) pricingMethod = PricingMethod.BASE_RATE;
        if (includeTaxes == null) includeTaxes = true;
        if (currency == null) currency = "USD";

        // Validation: check-out must be after check-in
        if (checkInDate != null && checkOutDate != null &&
            !checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
    }

    /**
     * Calculates the number of nights
     */
    public int getNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        return (int) checkInDate.until(checkOutDate).getDays();
    }

    /**
     * Gets the total number of guests
     */
    public int getTotalGuests() {
        return adults + (children != null ? children : 0);
    }
}
