package com.modernreservation.availabilitycalculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for occupancy statistics
 */
@Schema(description = "Occupancy statistics")
public record OccupancyStatsDTO(

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID propertyId,

    @Schema(description = "Statistics date", example = "2024-12-25")
    LocalDate date,

    @Schema(description = "Total rooms in property", example = "100")
    Integer totalRooms,

    @Schema(description = "Number of occupied rooms", example = "85")
    Integer occupiedRooms,

    @Schema(description = "Number of available rooms", example = "12")
    Integer availableRooms,

    @Schema(description = "Number of maintenance rooms", example = "2")
    Integer maintenanceRooms,

    @Schema(description = "Number of blocked rooms", example = "1")
    Integer blockedRooms,

    @Schema(description = "Occupancy percentage", example = "85.0")
    BigDecimal occupancyPercentage,

    @Schema(description = "Available percentage", example = "12.0")
    BigDecimal availabilityPercentage,

    @Schema(description = "Average daily rate", example = "250.00")
    BigDecimal averageDailyRate,

    @Schema(description = "Revenue per available room", example = "212.50")
    BigDecimal revPAR,

    @Schema(description = "Total revenue for the date", example = "21250.00")
    BigDecimal totalRevenue,

    @Schema(description = "Currency code", example = "USD")
    String currency,

    @Schema(description = "Pick-up percentage (bookings vs capacity)", example = "78.5")
    BigDecimal pickupPercentage,

    @Schema(description = "No-show count", example = "3")
    Integer noShowCount,

    @Schema(description = "Walk-in count", example = "5")
    Integer walkInCount,

    @Schema(description = "Cancellation count", example = "2")
    Integer cancellationCount
) {

    /**
     * Calculates utilization percentage (occupied + maintenance + blocked)
     */
    public BigDecimal getUtilizationPercentage() {
        if (totalRooms == null || totalRooms == 0) {
            return BigDecimal.ZERO;
        }
        int utilized = (occupiedRooms != null ? occupiedRooms : 0) +
                      (maintenanceRooms != null ? maintenanceRooms : 0) +
                      (blockedRooms != null ? blockedRooms : 0);
        return BigDecimal.valueOf(utilized)
                         .divide(BigDecimal.valueOf(totalRooms), 4, java.math.RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculates revenue per occupied room
     */
    public BigDecimal getRevPOR() {
        if (occupiedRooms == null || occupiedRooms == 0 || totalRevenue == null) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(occupiedRooms), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Checks if occupancy is high (>= 90%)
     */
    public boolean isHighOccupancy() {
        return occupancyPercentage != null &&
               occupancyPercentage.compareTo(BigDecimal.valueOf(90)) >= 0;
    }

    /**
     * Checks if occupancy is low (<= 30%)
     */
    public boolean isLowOccupancy() {
        return occupancyPercentage != null &&
               occupancyPercentage.compareTo(BigDecimal.valueOf(30)) <= 0;
    }

    /**
     * Returns the average occupancy percentage (alias for occupancyPercentage)
     */
    public BigDecimal averageOccupancy() {
        return occupancyPercentage != null ? occupancyPercentage : BigDecimal.ZERO;
    }
}
