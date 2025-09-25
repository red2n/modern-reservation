package com.modernreservation.availabilitycalculator.dto;

import com.modernreservation.availabilitycalculator.enums.AvailabilityStatus;
import com.modernreservation.availabilitycalculator.enums.RoomCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for availability information
 */
@Schema(description = "Room availability response")
public record AvailabilityResponseDTO(

    @Schema(description = "Availability record ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID propertyId,

    @Schema(description = "Room type ID", example = "660e8400-e29b-41d4-a716-446655440001")
    UUID roomTypeId,

    @Schema(description = "Room category", example = "DELUXE")
    RoomCategory roomCategory,

    @Schema(description = "Availability date", example = "2024-12-25")
    LocalDate availabilityDate,

    @Schema(description = "Current availability status", example = "AVAILABLE")
    AvailabilityStatus availabilityStatus,

    @Schema(description = "Base rate for the room", example = "200.00")
    BigDecimal baseRate,

    @Schema(description = "Current rate after adjustments", example = "180.00")
    BigDecimal currentRate,

    @Schema(description = "Total number of rooms of this type", example = "10")
    Integer totalRooms,

    @Schema(description = "Number of available rooms", example = "7")
    Integer availableRooms,

    @Schema(description = "Number of occupied rooms", example = "3")
    Integer occupiedRooms,

    @Schema(description = "Minimum stay requirement in nights", example = "2")
    Integer minimumStay,

    @Schema(description = "Maximum stay allowed in nights", example = "30")
    Integer maximumStay,

    @Schema(description = "Is arrival closed on this date", example = "false")
    Boolean closedToArrival,

    @Schema(description = "Is departure closed on this date", example = "false")
    Boolean closedToDeparture,

    @Schema(description = "Is room type stop sell", example = "false")
    Boolean stopSell,

    @Schema(description = "Currency code", example = "USD")
    String currency,

    @Schema(description = "Calculated total price for the stay", example = "360.00")
    BigDecimal totalPrice,

    @Schema(description = "Calculated price per night", example = "180.00")
    BigDecimal pricePerNight,

    @Schema(description = "Applied discounts", example = "20.00")
    BigDecimal discount,

    @Schema(description = "Applied taxes", example = "21.60")
    BigDecimal taxes,

    @Schema(description = "Final price including taxes", example = "381.60")
    BigDecimal finalPrice
) {

    /**
     * Calculates occupancy percentage
     */
    public double getOccupancyPercentage() {
        if (totalRooms == null || totalRooms == 0) {
            return 0.0;
        }
        return ((double) occupiedRooms / totalRooms) * 100.0;
    }

    /**
     * Checks if the room type is available for booking
     */
    public boolean isBookable() {
        return availabilityStatus == AvailabilityStatus.AVAILABLE &&
               !stopSell &&
               availableRooms != null &&
               availableRooms > 0;
    }

    /**
     * Checks if the room type has limited availability
     */
    public boolean isLimitedAvailability() {
        if (totalRooms == null || availableRooms == null) {
            return false;
        }
        return availableRooms <= (totalRooms * 0.2); // Less than 20% available
    }
}
