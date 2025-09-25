package com.modernreservation.reservationengine.dto;

import com.modernreservation.reservationengine.enums.ReservationStatus;
import com.modernreservation.reservationengine.enums.ReservationSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reservation Response DTO - Modern Java Record implementation
 *
 * Immutable data transfer object for returning reservation data
 */
@Schema(description = "Reservation details response")
public record ReservationResponseDTO(

    @Schema(description = "Reservation unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Confirmation number", example = "MR2024001")
    String confirmationNumber,

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID propertyId,

    @Schema(description = "Guest ID", example = "550e8400-e29b-41d4-a716-446655440002")
    UUID guestId,

    @Schema(description = "Guest first name", example = "John")
    String guestFirstName,

    @Schema(description = "Guest last name", example = "Doe")
    String guestLastName,

    @Schema(description = "Guest email", example = "john.doe@example.com")
    String guestEmail,

    @Schema(description = "Guest phone", example = "+1-555-123-4567")
    String guestPhone,

    @Schema(description = "Check-in date", example = "2024-12-25")
    LocalDate checkInDate,

    @Schema(description = "Check-out date", example = "2024-12-27")
    LocalDate checkOutDate,

    @Schema(description = "Number of nights", example = "2")
    Integer nights,

    @Schema(description = "Room type ID", example = "550e8400-e29b-41d4-a716-446655440003")
    UUID roomTypeId,

    @Schema(description = "Assigned room number", example = "101")
    String roomNumber,

    @Schema(description = "Number of adults", example = "2")
    Integer adults,

    @Schema(description = "Number of children", example = "1")
    Integer children,

    @Schema(description = "Number of infants", example = "0")
    Integer infants,

    @Schema(description = "Room rate per night", example = "129.99")
    BigDecimal roomRate,

    @Schema(description = "Taxes amount", example = "20.80")
    BigDecimal taxes,

    @Schema(description = "Additional fees", example = "15.00")
    BigDecimal fees,

    @Schema(description = "Total amount", example = "275.78")
    BigDecimal totalAmount,

    @Schema(description = "Currency code", example = "USD")
    String currency,

    @Schema(description = "Current reservation status", example = "CONFIRMED")
    ReservationStatus status,

    @Schema(description = "Reservation source", example = "DIRECT")
    ReservationSource source,

    @Schema(description = "Special requests", example = "Late check-in requested")
    String specialRequests,

    @Schema(description = "Internal notes", example = "VIP guest - provide room upgrade if available")
    String internalNotes,

    @Schema(description = "Booking date and time", example = "2024-12-20T10:30:00")
    LocalDateTime bookingDate,

    @Schema(description = "Expected arrival time", example = "15:30")
    String arrivalTime,

    @Schema(description = "Expected departure time", example = "11:00")
    String departureTime,

    @Schema(description = "Cancellation date and time", example = "2024-12-22T14:15:00")
    LocalDateTime cancelledAt,

    @Schema(description = "Cancellation reason", example = "Guest cancelled due to travel restrictions")
    String cancellationReason,

    @Schema(description = "Who cancelled the reservation", example = "guest")
    String cancelledBy,

    @Schema(description = "Payment method", example = "CREDIT_CARD")
    String paymentMethod,

    @Schema(description = "Payment status", example = "PAID")
    String paymentStatus,

    @Schema(description = "Deposit amount", example = "50.00")
    BigDecimal depositAmount,

    @Schema(description = "Deposit due date", example = "2024-12-23")
    LocalDate depositDueDate,

    @Schema(description = "Channel reference number", example = "BDC-123456789")
    String channelReference,

    @Schema(description = "Commission rate", example = "0.15")
    BigDecimal commissionRate,

    @Schema(description = "Creation timestamp", example = "2024-12-20T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Last modification timestamp", example = "2024-12-21T16:45:00")
    LocalDateTime updatedAt,

    @Schema(description = "Created by user", example = "john.smith@hotel.com")
    String createdBy,

    @Schema(description = "Last updated by user", example = "jane.doe@hotel.com")
    String updatedBy,

    @Schema(description = "Version for optimistic locking", example = "1")
    Long version

) {

    // Convenience methods (computed properties)
    @Schema(description = "Guest full name", example = "John Doe")
    public String guestFullName() {
        return guestFirstName + " " + guestLastName;
    }

    public int totalGuests() {
        return adults + (children != null ? children : 0) + (infants != null ? infants : 0);
    }

    public boolean isPending() {
        return ReservationStatus.PENDING.equals(status);
    }

    public boolean isConfirmed() {
        return ReservationStatus.CONFIRMED.equals(status);
    }

    public boolean isCancelled() {
        return ReservationStatus.CANCELLED.equals(status);
    }

    public boolean isCheckedIn() {
        return ReservationStatus.CHECKED_IN.equals(status);
    }

    public boolean isCheckedOut() {
        return ReservationStatus.CHECKED_OUT.equals(status);
    }

    public boolean isNoShow() {
        return ReservationStatus.NO_SHOW.equals(status);
    }
}
