package com.modernreservation.reservationengine.entity;

import com.modernreservation.reservationengine.enums.ReservationStatus;
import com.modernreservation.reservationengine.enums.ReservationSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Reservation Entity - Core booking domain object
 *
 * Represents a hotel reservation with all associated details including
 * guest information, room assignments, dates, rates, and status tracking.
 *
 * Uses Lombok for cleaner, more maintainable code.
 */
@Entity
@Table(name = "reservations")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"auditHistory"}) // Exclude collections from toString to avoid lazy loading issues
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Reservation {    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private UUID id;

    // Multi-tenancy: Which tenant owns this reservation
    @NotNull
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @NotNull
    @Column(name = "confirmation_number", unique = true, nullable = false, length = 20)
    @EqualsAndHashCode.Include
    private String confirmationNumber;

    @NotNull
    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "guest_id")
    private UUID guestId;

    // Guest Information (can be stored locally for non-registered guests)
    @NotNull
    @Column(name = "guest_first_name", nullable = false, length = 100)
    private String guestFirstName;

    @NotNull
    @Column(name = "guest_last_name", nullable = false, length = 100)
    private String guestLastName;

    @Email
    @NotNull
    @Column(name = "guest_email", nullable = false, length = 255)
    private String guestEmail;

    @Column(name = "guest_phone", length = 20)
    private String guestPhone;

    // Reservation Dates
    @NotNull
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @NotNull
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @NotNull
    @Positive
    @Column(name = "nights", nullable = false)
    private Integer nights;

    // Room Details
    @Column(name = "room_type_id")
    private UUID roomTypeId;

    @Column(name = "room_number", length = 10)
    private String roomNumber;

    @NotNull
    @Positive
    @Column(name = "adults", nullable = false)
    private Integer adults;

    @Column(name = "children")
    private Integer children;

    @Column(name = "infants")
    private Integer infants;

    // Pricing
    @NotNull
    @Column(name = "room_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal roomRate;

    @Column(name = "taxes", precision = 10, scale = 2)
    private BigDecimal taxes;

    @Column(name = "fees", precision = 10, scale = 2)
    private BigDecimal fees;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    // Status and Source
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private ReservationSource source;

    // Special Requests and Notes
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    // Booking Details
    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "arrival_time", length = 5)
    private String arrivalTime;

    @Column(name = "departure_time", length = 5)
    private String departureTime;

    @Column(name = "actual_check_in_time")
    private LocalDateTime actualCheckInTime;

    @Column(name = "actual_check_out_time")
    private LocalDateTime actualCheckOutTime;

    // Cancellation
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    // Payment Information
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "deposit_due_date")
    private LocalDate depositDueDate;

    // Channel Management
    @Column(name = "channel_reference", length = 100)
    private String channelReference;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate;

    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Version for optimistic locking
    @Version
    @Column(name = "version")
    private Long version;

    // One-to-Many relationships
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReservationAudit> auditHistory = new ArrayList<>();

    // Remove duplicate field declarations - these are already defined above with @Builder.Default

    // Business Methods
    public boolean isPending() {
        return ReservationStatus.PENDING.equals(this.status);
    }

    public boolean isConfirmed() {
        return ReservationStatus.CONFIRMED.equals(this.status);
    }

    public boolean isCancelled() {
        return ReservationStatus.CANCELLED.equals(this.status);
    }

    public boolean isCheckedIn() {
        return ReservationStatus.CHECKED_IN.equals(this.status);
    }

    public boolean isCheckedOut() {
        return ReservationStatus.CHECKED_OUT.equals(this.status);
    }

    public boolean isNoShow() {
        return ReservationStatus.NO_SHOW.equals(this.status);
    }

    public String getGuestFullName() {
        return guestFirstName + " " + guestLastName;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", confirmationNumber='" + confirmationNumber + '\'' +
                ", guestFullName='" + getGuestFullName() + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", status=" + status +
                '}';
    }
}
