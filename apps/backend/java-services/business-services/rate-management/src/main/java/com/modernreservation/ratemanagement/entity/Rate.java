package com.modernreservation.ratemanagement.entity;

import com.modernreservation.ratemanagement.enums.RateStatus;
import com.modernreservation.ratemanagement.enums.RateStrategy;
import com.modernreservation.ratemanagement.enums.SeasonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Rate entity representing pricing information for hotel rooms
 */
@Entity
@Table(name = "rates", indexes = {
    @Index(name = "idx_rates_property_room_date", columnList = "property_id, room_type_id, effective_date"),
    @Index(name = "idx_rates_property_date_range", columnList = "property_id, effective_date, expiry_date"),
    @Index(name = "idx_rates_status_strategy", columnList = "rate_status, rate_strategy"),
    @Index(name = "idx_rates_season", columnList = "season_type, effective_date")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "room_type_id", nullable = false)
    private UUID roomTypeId;

    @Column(name = "rate_code", nullable = false, length = 50)
    private String rateCode;

    @Column(name = "rate_name", nullable = false, length = 100)
    private String rateName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_strategy", nullable = false)
    private RateStrategy rateStrategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_status", nullable = false)
    private RateStatus rateStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "season_type")
    private SeasonType seasonType;

    @Column(name = "base_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseRate;

    @Column(name = "current_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentRate;

    @Column(name = "minimum_rate", precision = 10, scale = 2)
    private BigDecimal minimumRate;

    @Column(name = "maximum_rate", precision = 10, scale = 2)
    private BigDecimal maximumRate;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "minimum_stay")
    private Integer minimumStay;

    @Column(name = "maximum_stay")
    private Integer maximumStay;

    @Column(name = "advance_booking_days")
    private Integer advanceBookingDays;

    @Column(name = "maximum_booking_days")
    private Integer maximumBookingDays;

    @Column(name = "is_refundable", nullable = false)
    private Boolean isRefundable;

    @Column(name = "is_modifiable", nullable = false)
    private Boolean isModifiable;

    @Column(name = "cancellation_hours")
    private Integer cancellationHours;

    @Column(name = "tax_inclusive", nullable = false)
    private Boolean taxInclusive;

    @Column(name = "service_fee_inclusive", nullable = false)
    private Boolean serviceFeeInclusive;

    @Column(name = "occupancy_multiplier", precision = 5, scale = 2)
    private BigDecimal occupancyMultiplier;

    @Column(name = "demand_multiplier", precision = 5, scale = 2)
    private BigDecimal demandMultiplier;

    @Column(name = "competitive_adjustment", precision = 5, scale = 2)
    private BigDecimal competitiveAdjustment;

    @Column(name = "priority_order", nullable = false)
    private Integer priorityOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Calculate the final rate with all adjustments
     */
    public BigDecimal calculateFinalRate() {
        BigDecimal finalRate = baseRate;

        // Apply season multiplier
        if (seasonType != null) {
            finalRate = finalRate.multiply(BigDecimal.valueOf(seasonType.getMultiplier()));
        }

        // Apply occupancy multiplier
        if (occupancyMultiplier != null) {
            finalRate = finalRate.multiply(occupancyMultiplier);
        }

        // Apply demand multiplier
        if (demandMultiplier != null) {
            finalRate = finalRate.multiply(demandMultiplier);
        }

        // Apply competitive adjustment
        if (competitiveAdjustment != null) {
            finalRate = finalRate.add(competitiveAdjustment);
        }

        // Ensure rate is within bounds
        if (minimumRate != null && finalRate.compareTo(minimumRate) < 0) {
            finalRate = minimumRate;
        }
        if (maximumRate != null && finalRate.compareTo(maximumRate) > 0) {
            finalRate = maximumRate;
        }

        return finalRate;
    }

    /**
     * Check if rate is valid for a given date
     */
    public boolean isValidForDate(LocalDate date) {
        if (!isActive || rateStatus != RateStatus.ACTIVE) {
            return false;
        }

        if (date.isBefore(effectiveDate)) {
            return false;
        }

        return expiryDate == null || !date.isAfter(expiryDate);
    }

    /**
     * Check if rate allows booking for given advance days
     */
    public boolean isValidForAdvanceBooking(int advanceDays) {
        if (advanceBookingDays != null && advanceDays < advanceBookingDays) {
            return false;
        }

        return maximumBookingDays == null || advanceDays <= maximumBookingDays;
    }

    /**
     * Get the effective priority for rate selection
     */
    public int getEffectivePriority() {
        int strategyPriority = rateStrategy.getPriority();
        int orderPriority = priorityOrder != null ? priorityOrder : 0;
        return (strategyPriority * 100) + orderPriority;
    }
}
