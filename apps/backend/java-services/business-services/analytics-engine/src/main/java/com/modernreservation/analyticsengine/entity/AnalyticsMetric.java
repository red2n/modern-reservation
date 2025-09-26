package com.modernreservation.analyticsengine.entity;

import com.modernreservation.analyticsengine.enums.MetricType;
import com.modernreservation.analyticsengine.enums.TimeGranularity;
import com.modernreservation.analyticsengine.enums.AnalyticsStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Analytics Metric Entity
 *
 * Represents calculated metrics and KPIs for business analytics.
 * Stores time-series data for various business metrics with flexible aggregation support.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Entity
@Table(
    name = "analytics_metrics",
    indexes = {
        @Index(name = "idx_metric_type_period", columnList = "metric_type, period_start, period_end"),
        @Index(name = "idx_property_granularity", columnList = "property_id, time_granularity"),
        @Index(name = "idx_calculation_time", columnList = "calculated_at"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_period_range", columnList = "period_start, period_end")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "metric_id")
    private UUID metricId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 50)
    @NotNull(message = "Metric type is required")
    private MetricType metricType;

    @Column(name = "property_id")
    private UUID propertyId;

    @Column(name = "room_type_id")
    private UUID roomTypeId;

    @Column(name = "rate_plan_id")
    private UUID ratePlanId;

    @Column(name = "channel_id")
    private UUID channelId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_granularity", nullable = false, length = 20)
    @NotNull(message = "Time granularity is required")
    private TimeGranularity timeGranularity;

    @Column(name = "period_start", nullable = false)
    @NotNull(message = "Period start is required")
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    @NotNull(message = "Period end is required")
    private LocalDateTime periodEnd;

    @Column(name = "metric_value", precision = 19, scale = 4)
    private BigDecimal metricValue;

    @Column(name = "count_value")
    private Long countValue;

    @Column(name = "percentage_value", precision = 5, scale = 2)
    private BigDecimal percentageValue;

    @Column(name = "currency_code", length = 3)
    @Size(max = 3, message = "Currency code must be 3 characters or less")
    private String currencyCode;

    @Column(name = "formatted_value", length = 255)
    private String formattedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    @Builder.Default
    private AnalyticsStatus status = AnalyticsStatus.PENDING;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "calculation_duration_ms")
    private Long calculationDurationMs;

    @Column(name = "data_points_count")
    @Min(value = 0, message = "Data points count cannot be negative")
    private Integer dataPointsCount;

    @Column(name = "confidence_score", precision = 3, scale = 2)
    @DecimalMin(value = "0.00", message = "Confidence score cannot be negative")
    @DecimalMax(value = "1.00", message = "Confidence score cannot exceed 1.00")
    private BigDecimal confidenceScore;

    @Column(name = "baseline_value", precision = 19, scale = 4)
    private BigDecimal baselineValue;

    @Column(name = "target_value", precision = 19, scale = 4)
    private BigDecimal targetValue;

    @Column(name = "variance_percentage", precision = 5, scale = 2)
    private BigDecimal variancePercentage;

    @Column(name = "trend_direction", length = 10)
    @Pattern(regexp = "UP|DOWN|STABLE|UNKNOWN", message = "Trend direction must be UP, DOWN, STABLE, or UNKNOWN")
    private String trendDirection;

    @Column(name = "seasonality_factor", precision = 5, scale = 4)
    private BigDecimal seasonalityFactor;

    @ElementCollection
    @CollectionTable(
        name = "analytics_metric_dimensions",
        joinColumns = @JoinColumn(name = "metric_id")
    )
    @MapKeyColumn(name = "dimension_key", length = 50)
    @Column(name = "dimension_value", length = 255)
    private Map<String, String> dimensions;

    @ElementCollection
    @CollectionTable(
        name = "analytics_metric_metadata",
        joinColumns = @JoinColumn(name = "metric_id")
    )
    @MapKeyColumn(name = "metadata_key", length = 50)
    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private Map<String, String> metadata;

    @Column(name = "calculation_method", length = 100)
    private String calculationMethod;

    @Column(name = "data_sources", columnDefinition = "TEXT")
    private String dataSources;

    @Column(name = "quality_score", precision = 3, scale = 2)
    @DecimalMin(value = "0.00", message = "Quality score cannot be negative")
    @DecimalMax(value = "1.00", message = "Quality score cannot exceed 1.00")
    private BigDecimal qualityScore;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "warning_messages", columnDefinition = "TEXT")
    private String warningMessages;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "version", nullable = false)
    @NotNull(message = "Version is required")
    @Builder.Default
    private Integer version = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Business Logic Methods

    /**
     * Check if metric calculation is complete
     */
    public boolean isCalculationComplete() {
        return status.isTerminal() && calculatedAt != null;
    }

    /**
     * Check if metric data is still valid (not expired)
     */
    public boolean isValid() {
        return expiresAt == null || LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Check if metric meets quality threshold
     */
    public boolean meetsQualityThreshold(BigDecimal threshold) {
        return qualityScore != null && qualityScore.compareTo(threshold) >= 0;
    }

    /**
     * Check if metric has sufficient confidence
     */
    public boolean hasSufficientConfidence(BigDecimal threshold) {
        return confidenceScore != null && confidenceScore.compareTo(threshold) >= 0;
    }

    /**
     * Calculate variance from baseline
     */
    public BigDecimal calculateVarianceFromBaseline() {
        if (baselineValue == null || metricValue == null || baselineValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return metricValue.subtract(baselineValue)
                         .divide(baselineValue, 4, BigDecimal.ROUND_HALF_UP)
                         .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculate variance from target
     */
    public BigDecimal calculateVarianceFromTarget() {
        if (targetValue == null || metricValue == null || targetValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return metricValue.subtract(targetValue)
                         .divide(targetValue, 4, BigDecimal.ROUND_HALF_UP)
                         .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if metric is trending positively (based on metric type)
     */
    public boolean isTrendingPositively() {
        if (trendDirection == null) {
            return false;
        }

        // For revenue metrics, UP is positive
        // For cost metrics, DOWN is positive
        // This can be customized based on metric type
        return switch (trendDirection) {
            case "UP" -> metricType.isRevenueMetric() || metricType.isOccupancyMetric();
            case "DOWN" -> metricType.name().contains("COST") || metricType.name().contains("CANCELLATION");
            case "STABLE" -> true; // Stable can be positive depending on context
            default -> false;
        };
    }

    /**
     * Get formatted metric value with currency if applicable
     */
    public String getFormattedValue() {
        if (metricValue == null) {
            return countValue != null ? countValue.toString() : "N/A";
        }

        if (currencyCode != null && metricType.isRevenueMetric()) {
            return String.format("%s %.2f", currencyCode, metricValue);
        }

        if (percentageValue != null) {
            return String.format("%.2f%%", percentageValue);
        }

        return metricValue.toString();
    }

    /**
     * Get display name for the metric period
     */
    public String getPeriodDisplayName() {
        return switch (timeGranularity) {
            case HOURLY -> periodStart.toLocalDate() + " " + periodStart.getHour() + ":00";
            case DAILY -> periodStart.toLocalDate().toString();
            case WEEKLY -> "Week of " + periodStart.toLocalDate();
            case MONTHLY -> periodStart.getYear() + "-" + String.format("%02d", periodStart.getMonthValue());
            case QUARTERLY -> periodStart.getYear() + " Q" + ((periodStart.getMonthValue() - 1) / 3 + 1);
            case YEARLY -> String.valueOf(periodStart.getYear());
            default -> periodStart.toLocalDate() + " to " + periodEnd.toLocalDate();
        };
    }

    /**
     * Add a dimension
     */
    public void addDimension(String key, String value) {
        if (dimensions == null) {
            dimensions = new java.util.HashMap<>();
        }
        dimensions.put(key, value);
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Check if metric has specific dimension
     */
    public boolean hasDimension(String key) {
        return dimensions != null && dimensions.containsKey(key);
    }

    /**
     * Get dimension value
     */
    public String getDimensionValue(String key) {
        return dimensions != null ? dimensions.get(key) : null;
    }

    /**
     * Mark metric as expired
     */
    public void markAsExpired() {
        this.status = AnalyticsStatus.EXPIRED;
        this.expiresAt = LocalDateTime.now();
    }

    /**
     * Update calculation metadata
     */
    public void updateCalculationMetadata(long durationMs, int dataPoints, BigDecimal confidence, BigDecimal quality) {
        this.calculationDurationMs = durationMs;
        this.dataPointsCount = dataPoints;
        this.confidenceScore = confidence;
        this.qualityScore = quality;
        this.calculatedAt = LocalDateTime.now();
    }
}
