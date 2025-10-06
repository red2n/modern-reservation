package com.modernreservation.availabilitycalculator.dto;

import com.modernreservation.availabilitycalculator.enums.AvailabilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Availability Summary DTO - Lightweight DTO for search results and list views
 *
 * Contains only core availability information needed for displaying
 * room availability in search results, calendars, and quick views.
 * Excludes detailed pricing breakdown and calculated fields.
 *
 * Use this DTO for:
 * - Availability search results
 * - Calendar views
 * - Room type selection lists
 * - Quick availability checks
 * - Dashboard availability widgets
 *
 * For complete pricing details and calculated fields,
 * use AvailabilityResponseDTO via the detail endpoint.
 *
 * Payload reduction: ~55% compared to full AvailabilityResponseDTO
 */
@Schema(description = "Lightweight availability summary for search results")
public record AvailabilitySummaryDTO(

    @Schema(description = "Availability record ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID propertyId,

    @Schema(description = "Room type ID", example = "660e8400-e29b-41d4-a716-446655440001")
    UUID roomTypeId,

    @Schema(description = "Availability date", example = "2024-12-25")
    LocalDate availabilityDate,

    @Schema(description = "Current availability status", example = "AVAILABLE")
    AvailabilityStatus availabilityStatus,

    @Schema(description = "Current rate after adjustments", example = "180.00")
    BigDecimal currentRate,

    @Schema(description = "Number of available rooms", example = "7")
    Integer availableRooms,

    @Schema(description = "Total number of rooms of this type", example = "10")
    Integer totalRooms,

    @Schema(description = "Minimum stay requirement in nights", example = "2")
    Integer minimumStay,

    @Schema(description = "Is room type stop sell", example = "false")
    Boolean stopSell,

    @Schema(description = "Currency code", example = "USD")
    String currency

) {

    /**
     * Factory method to create from full AvailabilityResponseDTO
     */
    public static AvailabilitySummaryDTO from(AvailabilityResponseDTO full) {
        return new AvailabilitySummaryDTO(
            full.id(),
            full.propertyId(),
            full.roomTypeId(),
            full.availabilityDate(),
            full.availabilityStatus(),
            full.currentRate(),
            full.availableRooms(),
            full.totalRooms(),
            full.minimumStay(),
            full.stopSell(),
            full.currency()
        );
    }

    // Convenience methods
    public boolean isBookable() {
        return availabilityStatus == AvailabilityStatus.AVAILABLE &&
               !stopSell &&
               availableRooms != null &&
               availableRooms > 0;
    }

    public boolean isLimitedAvailability() {
        if (totalRooms == null || availableRooms == null) {
            return false;
        }
        return availableRooms <= (totalRooms * 0.2); // Less than 20% available
    }

    public double getOccupancyPercentage() {
        if (totalRooms == null || totalRooms == 0) {
            return 0.0;
        }
        int occupiedRooms = totalRooms - (availableRooms != null ? availableRooms : 0);
        return ((double) occupiedRooms / totalRooms) * 100.0;
    }
}
