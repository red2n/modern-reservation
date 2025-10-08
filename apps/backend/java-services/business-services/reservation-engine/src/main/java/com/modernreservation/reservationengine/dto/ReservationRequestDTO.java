package com.modernreservation.reservationengine.dto;

import com.modernreservation.reservationengine.enums.ReservationSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Reservation Request DTO - Used for creating and updating reservations
 *
 * Modern Java Record implementation with validation annotations
 */
@Schema(description = "Reservation creation and update request")
public record ReservationRequestDTO(

    @Schema(description = "Tenant ID - identifies which organization owns this reservation", example = "550e8400-e29b-41d4-a716-446655440099")
    @NotNull(message = "Tenant ID is required")
    UUID tenantId,

    @Schema(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "Property ID is required")
    UUID propertyId,

    @Schema(description = "Guest ID (optional for walk-in bookings)", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID guestId,

    @Schema(description = "Guest first name", example = "John")
    @NotBlank(message = "Guest first name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String guestFirstName,

    @Schema(description = "Guest last name", example = "Doe")
    @NotBlank(message = "Guest last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String guestLastName,

    @Schema(description = "Guest email address", example = "john.doe@example.com")
    @Email(message = "Valid email address is required")
    @NotBlank(message = "Guest email is required")
    String guestEmail,

    @Schema(description = "Guest phone number", example = "+1-555-123-4567")
    @Pattern(regexp = "^[+]?[\\d\\-\\s\\(\\)]{10,20}$", message = "Valid phone number is required")
    String guestPhone,

    @Schema(description = "Check-in date", example = "2024-12-25")
    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    LocalDate checkInDate,

    @Schema(description = "Check-out date", example = "2024-12-27")
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    LocalDate checkOutDate,

    @Schema(description = "Room type ID", example = "550e8400-e29b-41d4-a716-446655440002")
    UUID roomTypeId,

    @Schema(description = "Number of adults", example = "2")
    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "At least 1 adult is required")
    @Max(value = 10, message = "Maximum 10 adults allowed")
    Integer adults,

    @Schema(description = "Number of children", example = "1")
    @Min(value = 0, message = "Children count cannot be negative")
    @Max(value = 10, message = "Maximum 10 children allowed")
    Integer children,

    @Schema(description = "Number of infants", example = "0")
    @Min(value = 0, message = "Infants count cannot be negative")
    @Max(value = 5, message = "Maximum 5 infants allowed")
    Integer infants,

    @Schema(description = "Room rate per night", example = "129.99")
    @NotNull(message = "Room rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Room rate must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid room rate format")
    BigDecimal roomRate,

    @Schema(description = "Currency code", example = "USD")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
    String currency,

    @Schema(description = "Reservation source", example = "DIRECT")
    @NotNull(message = "Reservation source is required")
    ReservationSource source,

    @Schema(description = "Special requests from guest", example = "Late check-in requested")
    @Size(max = 1000, message = "Special requests must not exceed 1000 characters")
    String specialRequests,

    @Schema(description = "Expected arrival time", example = "15:30")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format (HH:MM)")
    String arrivalTime,

    @Schema(description = "Payment method", example = "CREDIT_CARD")
    String paymentMethod,

    @Schema(description = "Deposit amount", example = "50.00")
    @DecimalMin(value = "0.0", message = "Deposit amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid deposit amount format")
    BigDecimal depositAmount,

    @Schema(description = "Channel reference number", example = "BDC-123456789")
    @Size(max = 100, message = "Channel reference must not exceed 100 characters")
    String channelReference,

    @Schema(description = "Commission rate (0.0 to 1.0)", example = "0.15")
    @DecimalMin(value = "0.0", message = "Commission rate cannot be negative")
    @DecimalMax(value = "1.0", message = "Commission rate cannot exceed 100%")
    BigDecimal commissionRate,

    @Schema(description = "Tax amount", example = "15.99")
    @DecimalMin(value = "0.0", message = "Tax amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid tax amount format")
    BigDecimal taxes,

    @Schema(description = "Additional fees", example = "10.00")
    @DecimalMin(value = "0.0", message = "Fees amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid fees amount format")
    BigDecimal fees,

    @Schema(description = "Expected departure time", example = "11:00")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format (HH:MM)")
    String departureTime,

    @Schema(description = "Deposit due date", example = "2024-12-20")
    LocalDate depositDueDate

) {

    // Default values
    public ReservationRequestDTO {
        if (currency == null) {
            currency = "USD";
        }
    }

    // Custom validation methods for complex business rules
    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return checkOutDate.isAfter(checkInDate);
    }

    // Convenience methods
    public String guestFullName() {
        return guestFirstName + " " + guestLastName;
    }

    public int totalGuests() {
        return adults + (children != null ? children : 0) + (infants != null ? infants : 0);
    }
}
