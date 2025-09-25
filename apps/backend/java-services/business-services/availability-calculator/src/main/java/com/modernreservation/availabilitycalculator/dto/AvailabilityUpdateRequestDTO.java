package com.modernreservation.availabilitycalculator.dto;

import com.modernreservation.availabilitycalculator.enums.AvailabilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for updating room availability
 */
@Schema(description = "Availability update request")
public record AvailabilityUpdateRequestDTO(

    @Schema(description = "New availability status", example = "AVAILABLE")
    AvailabilityStatus availabilityStatus,

    @Schema(description = "New base rate", example = "200.00")
    @DecimalMin(value = "0.0", message = "Base rate cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Base rate must have at most 2 decimal places")
    BigDecimal baseRate,

    @Schema(description = "Total number of rooms", example = "10")
    @Min(value = 0, message = "Total rooms cannot be negative")
    Integer totalRooms,

    @Schema(description = "Number of available rooms", example = "7")
    @Min(value = 0, message = "Available rooms cannot be negative")
    Integer availableRooms,

    @Schema(description = "Number of occupied rooms", example = "3")
    @Min(value = 0, message = "Occupied rooms cannot be negative")
    Integer occupiedRooms,

    @Schema(description = "Number of maintenance rooms", example = "0")
    @Min(value = 0, message = "Maintenance rooms cannot be negative")
    Integer maintenanceRooms,

    @Schema(description = "Number of blocked rooms", example = "0")
    @Min(value = 0, message = "Blocked rooms cannot be negative")
    Integer blockedRooms,

    @Schema(description = "Minimum stay requirement", example = "2")
    @Min(value = 1, message = "Minimum stay must be at least 1 night")
    @Max(value = 365, message = "Minimum stay cannot exceed 365 nights")
    Integer minimumStay,

    @Schema(description = "Maximum stay allowed", example = "30")
    @Min(value = 1, message = "Maximum stay must be at least 1 night")
    @Max(value = 365, message = "Maximum stay cannot exceed 365 nights")
    Integer maximumStay,

    @Schema(description = "Close to arrival", example = "false")
    Boolean closedToArrival,

    @Schema(description = "Close to departure", example = "false")
    Boolean closedToDeparture,

    @Schema(description = "Stop sell flag", example = "false")
    Boolean stopSell,

    @Schema(description = "Update reason", example = "Maintenance scheduled")
    @Size(max = 500, message = "Update reason cannot exceed 500 characters")
    String updateReason,

    @Schema(description = "Updated by user", example = "admin@hotel.com")
    @Size(max = 100, message = "Updated by cannot exceed 100 characters")
    String updatedBy
) {

    public AvailabilityUpdateRequestDTO {
        // Validation: available + occupied + maintenance + blocked should not exceed total
        if (totalRooms != null) {
            int usedRooms = 0;
            if (availableRooms != null) usedRooms += availableRooms;
            if (occupiedRooms != null) usedRooms += occupiedRooms;
            if (maintenanceRooms != null) usedRooms += maintenanceRooms;
            if (blockedRooms != null) usedRooms += blockedRooms;

            if (usedRooms > totalRooms) {
                throw new IllegalArgumentException(
                    "Sum of room counts cannot exceed total rooms"
                );
            }
        }

        // Validation: minimum stay should not be greater than maximum stay
        if (minimumStay != null && maximumStay != null &&
            minimumStay > maximumStay) {
            throw new IllegalArgumentException(
                "Minimum stay cannot be greater than maximum stay"
            );
        }
    }

    /**
     * Checks if any field is provided for update
     */
    public boolean hasUpdates() {
        return availabilityStatus != null || baseRate != null ||
               totalRooms != null || availableRooms != null ||
               occupiedRooms != null || maintenanceRooms != null ||
               blockedRooms != null || minimumStay != null ||
               maximumStay != null || closedToArrival != null ||
               closedToDeparture != null || stopSell != null;
    }

    /**
     * Checks if the request has valid data for updating (alias for hasUpdates)
     */
    public boolean hasValidData() {
        return hasUpdates();
    }
}
