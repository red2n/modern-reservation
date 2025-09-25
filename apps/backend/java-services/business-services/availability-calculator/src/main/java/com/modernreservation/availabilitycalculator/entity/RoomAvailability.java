package com.modernreservation.availabilitycalculator.entity;

import com.modernreservation.availabilitycalculator.enums.AvailabilityStatus;
import com.modernreservation.availabilitycalculator.enums.RoomCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing room availability and pricing information
 *
 * Modern Java implementation using Lombok for clean, maintainable code
 */
@Entity
@Table(name = "room_availability", schema = "availability",
       indexes = {
           @Index(name = "idx_availability_property_date",
                  columnList = "property_id, availability_date"),
           @Index(name = "idx_availability_room_type_date",
                  columnList = "room_type_id, availability_date"),
           @Index(name = "idx_availability_status",
                  columnList = "availability_status")
       })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "property_id", nullable = false)
    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    @Column(name = "room_type_id", nullable = false)
    @NotNull(message = "Room type ID is required")
    private UUID roomTypeId;

    @Column(name = "room_number", length = 20)
    @Size(max = 20, message = "Room number must not exceed 20 characters")
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_category", nullable = false, length = 20)
    @NotNull(message = "Room category is required")
    private RoomCategory roomCategory;

    @Column(name = "availability_date", nullable = false)
    @NotNull(message = "Availability date is required")
    private LocalDate availabilityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 20)
    @NotNull(message = "Availability status is required")
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    @Column(name = "base_rate", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Base rate cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid base rate format")
    private BigDecimal baseRate;

    @Column(name = "current_rate", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Current rate cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid current rate format")
    private BigDecimal currentRate;

    @Column(name = "min_rate", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Minimum rate cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid minimum rate format")
    private BigDecimal minRate;

    @Column(name = "max_rate", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Maximum rate cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid maximum rate format")
    private BigDecimal maxRate;

    @Column(name = "total_rooms", nullable = false)
    @NotNull(message = "Total rooms count is required")
    @Min(value = 1, message = "Total rooms must be at least 1")
    @Max(value = 1000, message = "Total rooms cannot exceed 1000")
    private Integer totalRooms;

    @Column(name = "available_rooms", nullable = false)
    @NotNull(message = "Available rooms count is required")
    @Min(value = 0, message = "Available rooms cannot be negative")
    private Integer availableRooms;

    @Column(name = "occupied_rooms", nullable = false)
    @Builder.Default
    private Integer occupiedRooms = 0;

    @Column(name = "maintenance_rooms", nullable = false)
    @Builder.Default
    private Integer maintenanceRooms = 0;

    @Column(name = "blocked_rooms", nullable = false)
    @Builder.Default
    private Integer blockedRooms = 0;

    @Column(name = "minimum_stay", nullable = false)
    @Builder.Default
    private Integer minimumStay = 1;

    @Column(name = "maximum_stay")
    private Integer maximumStay;

    @Column(name = "closed_to_arrival", nullable = false)
    @Builder.Default
    private Boolean closedToArrival = false;

    @Column(name = "closed_to_departure", nullable = false)
    @Builder.Default
    private Boolean closedToDeparture = false;

    @Column(name = "stop_sell", nullable = false)
    @Builder.Default
    private Boolean stopSell = false;

    @Column(name = "currency", length = 3, nullable = false)
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
    @Builder.Default
    private String currency = "USD";

    @Column(name = "notes", length = 500)
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    // Business methods
    public boolean isAvailable() {
        return availabilityStatus == AvailabilityStatus.AVAILABLE &&
               availableRooms > 0 &&
               !stopSell;
    }

    public boolean canAcceptArrival() {
        return isAvailable() && !closedToArrival;
    }

    public boolean canAcceptDeparture() {
        return !closedToDeparture;
    }

    public double getOccupancyPercentage() {
        if (totalRooms == 0) return 0.0;
        return (double) occupiedRooms / totalRooms * 100.0;
    }

    public int getUnavailableRooms() {
        return occupiedRooms + maintenanceRooms + blockedRooms;
    }

    public boolean hasRateRestrictions() {
        return minRate != null || maxRate != null;
    }

    public boolean isWithinRateRange(BigDecimal proposedRate) {
        if (proposedRate == null) return false;

        boolean withinMin = minRate == null || proposedRate.compareTo(minRate) >= 0;
        boolean withinMax = maxRate == null || proposedRate.compareTo(maxRate) <= 0;

        return withinMin && withinMax;
    }

    public void updateAvailability(int roomsBooked) {
        this.availableRooms = Math.max(0, this.availableRooms - roomsBooked);
        this.occupiedRooms += roomsBooked;
    }

    public void releaseRooms(int roomsReleased) {
        this.availableRooms = Math.min(this.totalRooms - getUnavailableRooms() + roomsReleased,
                                      this.availableRooms + roomsReleased);
        this.occupiedRooms = Math.max(0, this.occupiedRooms - roomsReleased);
    }
}
