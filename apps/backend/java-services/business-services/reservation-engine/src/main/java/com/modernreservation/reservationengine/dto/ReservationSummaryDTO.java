package com.modernreservation.reservationengine.dto;

import com.modernreservation.reservationengine.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Reservation Summary DTO - Lightweight DTO for list views
 *
 * Contains only essential fields needed for displaying reservations in lists,
 * tables, and search results. Significantly reduces payload size compared to
 * full ReservationResponseDTO.
 *
 * Use this DTO for:
 * - List views (property reservations, search results)
 * - Table/grid displays
 * - Dashboard summaries
 * - Calendar views
 *
 * For complete details, use ReservationResponseDTO via the detail endpoint.
 */
@Schema(description = "Lightweight reservation summary for list views")
public record ReservationSummaryDTO(

    @Schema(description = "Reservation unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Tenant ID", example = "550e8400-e29b-41d4-a716-446655440099")
    UUID tenantId,

    @Schema(description = "Confirmation number", example = "MR2024001")
    String confirmationNumber,

    @Schema(description = "Guest full name", example = "John Doe")
    String guestName,

    @Schema(description = "Guest email", example = "john.doe@example.com")
    String guestEmail,

    @Schema(description = "Check-in date", example = "2024-12-25")
    LocalDate checkInDate,

    @Schema(description = "Check-out date", example = "2024-12-27")
    LocalDate checkOutDate,

    @Schema(description = "Number of nights", example = "2")
    Integer nights,

    @Schema(description = "Assigned room number", example = "101")
    String roomNumber,

    @Schema(description = "Current reservation status", example = "CONFIRMED")
    ReservationStatus status,

    @Schema(description = "Total amount", example = "275.78")
    BigDecimal totalAmount,

    @Schema(description = "Currency code", example = "USD")
    String currency,

    @Schema(description = "Number of guests", example = "3")
    Integer totalGuests

) {

    /**
     * Factory method to create from full ReservationResponseDTO
     */
    public static ReservationSummaryDTO from(ReservationResponseDTO full) {
        return new ReservationSummaryDTO(
            full.id(),
            full.tenantId(),
            full.confirmationNumber(),
            full.guestFullName(),
            full.guestEmail(),
            full.checkInDate(),
            full.checkOutDate(),
            full.nights(),
            full.roomNumber(),
            full.status(),
            full.totalAmount(),
            full.currency(),
            full.totalGuests()
        );
    }

    // Convenience methods
    public boolean isConfirmed() {
        return ReservationStatus.CONFIRMED.equals(status);
    }

    public boolean isCheckedIn() {
        return ReservationStatus.CHECKED_IN.equals(status);
    }

    public boolean isCancelled() {
        return ReservationStatus.CANCELLED.equals(status);
    }
}
