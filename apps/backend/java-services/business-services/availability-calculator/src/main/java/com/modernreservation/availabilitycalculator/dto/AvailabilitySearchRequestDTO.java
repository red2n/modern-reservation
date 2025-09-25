package com.modernreservation.availabilitycalculator.dto;

import com.modernreservation.availabilitycalculator.enums.RoomCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for availability search
 */
@Schema(description = "Availability search request")
public record AvailabilitySearchRequestDTO(

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @NotNull(message = "Property ID is required")
    UUID propertyId,

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

    @Schema(description = "Room category filter", example = "DELUXE")
    RoomCategory roomCategory,

    @Schema(description = "Minimum rate filter", example = "100.00")
    @DecimalMin(value = "0.0", message = "Minimum rate cannot be negative")
    BigDecimal minRate,

    @Schema(description = "Maximum rate filter", example = "500.00")
    @DecimalMin(value = "0.0", message = "Maximum rate cannot be negative")
    BigDecimal maxRate,

    @Schema(description = "Include unavailable rooms in response", example = "false")
    Boolean includeUnavailable,

    @Schema(description = "Sort by field", example = "rate")
    String sortBy,

    @Schema(description = "Sort direction", example = "ASC")
    String sortDirection,

    @Schema(description = "Page number for pagination", example = "0")
    @Min(value = 0, message = "Page number cannot be negative")
    Integer page,

    @Schema(description = "Page size for pagination", example = "10")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    Integer size
) {
    // Custom validation logic can be added here
    public AvailabilitySearchRequestDTO {
        if (children == null) children = 0;
        if (includeUnavailable == null) includeUnavailable = false;
        if (sortBy == null) sortBy = "baseRate";
        if (sortDirection == null) sortDirection = "ASC";
        if (page == null) page = 0;
        if (size == null) size = 10;

        // Validation: check-out must be after check-in
        if (checkInDate != null && checkOutDate != null &&
            !checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        // Validation: min rate cannot be greater than max rate
        if (minRate != null && maxRate != null &&
            minRate.compareTo(maxRate) > 0) {
            throw new IllegalArgumentException("Minimum rate cannot be greater than maximum rate");
        }
    }
}
