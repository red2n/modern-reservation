package com.modernreservation.analyticsengine.dto;

import com.modernreservation.analyticsengine.enums.MetricType;
import com.modernreservation.analyticsengine.enums.TimeGranularity;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Analytics Request DTO
 *
 * Request object for analytics calculations and report generation.
 * Supports flexible filtering, grouping, and aggregation options.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRequestDTO {

    /**
     * Specific metrics to calculate (if null, all applicable metrics will be calculated)
     */
    private List<MetricType> metricTypes;

    /**
     * Time granularity for aggregation
     */
    @NotNull(message = "Time granularity is required")
    private TimeGranularity timeGranularity;

    /**
     * Start of analysis period
     */
    @NotNull(message = "Period start is required")
    private LocalDateTime periodStart;

    /**
     * End of analysis period
     */
    @NotNull(message = "Period end is required")
    private LocalDateTime periodEnd;

    /**
     * Specific property to analyze (if null, all properties will be included)
     */
    private UUID propertyId;

    /**
     * Multiple properties to analyze
     */
    private List<UUID> propertyIds;

    /**
     * Specific room types to include in analysis
     */
    private List<UUID> roomTypeIds;

    /**
     * Specific rate plans to include in analysis
     */
    private List<UUID> ratePlanIds;

    /**
     * Specific booking channels to include in analysis
     */
    private List<UUID> channelIds;

    /**
     * Customer segments to analyze
     */
    private List<String> customerSegments;

    /**
     * Market segments to analyze
     */
    private List<String> marketSegments;

    /**
     * Geographic regions to include
     */
    private List<String> regions;

    /**
     * Currency for financial metrics
     */
    @Size(max = 3, message = "Currency code must be 3 characters or less")
    private String currencyCode;

    /**
     * Whether to include comparative analysis with previous period
     */
    @Builder.Default
    private Boolean includeComparison = false;

    /**
     * Whether to include trend analysis
     */
    @Builder.Default
    private Boolean includeTrends = false;

    /**
     * Whether to include forecasting
     */
    @Builder.Default
    private Boolean includeForecast = false;

    /**
     * Number of periods to forecast (if forecasting is enabled)
     */
    @Min(value = 1, message = "Forecast periods must be at least 1")
    @Max(value = 365, message = "Forecast periods cannot exceed 365")
    private Integer forecastPeriods;

    /**
     * Whether to apply seasonality adjustments
     */
    @Builder.Default
    private Boolean applySeasonality = true;

    /**
     * Minimum confidence threshold for results (0.0 to 1.0)
     */
    @DecimalMin(value = "0.0", message = "Confidence threshold cannot be negative")
    @DecimalMax(value = "1.0", message = "Confidence threshold cannot exceed 1.0")
    @Builder.Default
    private Double confidenceThreshold = 0.7;

    /**
     * Minimum data quality threshold (0.0 to 1.0)
     */
    @DecimalMin(value = "0.0", message = "Quality threshold cannot be negative")
    @DecimalMax(value = "1.0", message = "Quality threshold cannot exceed 1.0")
    @Builder.Default
    private Double qualityThreshold = 0.8;

    /**
     * Custom filters for advanced querying
     */
    private Map<String, String> filters;

    /**
     * Grouping dimensions for multi-dimensional analysis
     */
    private List<String> groupBy;

    /**
     * Sorting specifications
     */
    private List<SortSpecification> sortBy;

    /**
     * Maximum number of results to return
     */
    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 10000, message = "Limit cannot exceed 10000")
    private Integer limit;

    /**
     * Number of results to skip (for pagination)
     */
    @Min(value = 0, message = "Offset cannot be negative")
    private Integer offset;

    /**
     * Whether to include detailed breakdown by dimensions
     */
    @Builder.Default
    private Boolean includeBreakdown = false;

    /**
     * Whether to include statistical measures (min, max, avg, std dev, etc.)
     */
    @Builder.Default
    private Boolean includeStatistics = false;

    /**
     * Whether to include data quality metrics
     */
    @Builder.Default
    private Boolean includeQualityMetrics = false;

    /**
     * Whether to cache results for faster subsequent access
     */
    @Builder.Default
    private Boolean enableCaching = true;

    /**
     * Cache TTL in minutes (if caching is enabled)
     */
    @Min(value = 1, message = "Cache TTL must be at least 1 minute")
    @Max(value = 10080, message = "Cache TTL cannot exceed 1 week")
    @Builder.Default
    private Integer cacheTtlMinutes = 60;

    /**
     * Whether to run calculation asynchronously
     */
    @Builder.Default
    private Boolean asyncExecution = false;

    /**
     * Callback URL for async execution completion notification
     */
    private String callbackUrl;

    /**
     * Additional parameters for custom analytics
     */
    private Map<String, Object> parameters;

    /**
     * Tags for organizing and categorizing requests
     */
    private List<String> tags;

    /**
     * Request priority (1-5, where 5 is highest)
     */
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 5, message = "Priority cannot exceed 5")
    @Builder.Default
    private Integer priority = 3;

    /**
     * User ID making the request
     */
    private UUID userId;

    /**
     * Session ID for tracking related requests
     */
    private String sessionId;

    /**
     * Client information for audit purposes
     */
    private String clientInfo;

    /**
     * Sort Specification inner class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortSpecification {

        @NotBlank(message = "Sort field is required")
        private String field;

        @Pattern(regexp = "ASC|DESC", message = "Sort direction must be ASC or DESC")
        @Builder.Default
        private String direction = "ASC";

        @Min(value = 1, message = "Sort order must be at least 1")
        private Integer order;
    }

    // Validation and Business Logic Methods

    /**
     * Validate the time period
     */
    public boolean isValidTimePeriod() {
        return periodStart != null && periodEnd != null &&
               periodStart.isBefore(periodEnd);
    }

    /**
     * Calculate the duration of the analysis period
     */
    public java.time.Duration getPeriodDuration() {
        if (!isValidTimePeriod()) {
            return null;
        }
        return java.time.Duration.between(periodStart, periodEnd);
    }

    /**
     * Check if the request is for a single property
     */
    public boolean isSinglePropertyAnalysis() {
        return propertyId != null ||
               (propertyIds != null && propertyIds.size() == 1);
    }

    /**
     * Get the effective property ID for single property analysis
     */
    public UUID getEffectivePropertyId() {
        if (propertyId != null) {
            return propertyId;
        }
        if (propertyIds != null && propertyIds.size() == 1) {
            return propertyIds.get(0);
        }
        return null;
    }

    /**
     * Check if forecasting is requested and valid
     */
    public boolean isValidForecastRequest() {
        return includeForecast && forecastPeriods != null && forecastPeriods > 0;
    }

    /**
     * Add a filter
     */
    public void addFilter(String key, String value) {
        if (filters == null) {
            filters = new java.util.HashMap<>();
        }
        filters.put(key, value);
    }

    /**
     * Add a parameter
     */
    public void addParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new java.util.HashMap<>();
        }
        parameters.put(key, value);
    }

    /**
     * Add a grouping dimension
     */
    public void addGroupBy(String dimension) {
        if (groupBy == null) {
            groupBy = new java.util.ArrayList<>();
        }
        if (!groupBy.contains(dimension)) {
            groupBy.add(dimension);
        }
    }

    /**
     * Add a sort specification
     */
    public void addSortBy(String field, String direction) {
        if (sortBy == null) {
            sortBy = new java.util.ArrayList<>();
        }
        sortBy.add(SortSpecification.builder()
                  .field(field)
                  .direction(direction)
                  .order(sortBy.size() + 1)
                  .build());
    }

    /**
     * Add a tag
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new java.util.ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Check if specific metric type is requested
     */
    public boolean includesMetricType(MetricType metricType) {
        return metricTypes == null || metricTypes.contains(metricType);
    }

    /**
     * Check if property is included in the analysis
     */
    public boolean includesProperty(UUID propertyId) {
        if (this.propertyId != null) {
            return this.propertyId.equals(propertyId);
        }
        return propertyIds == null || propertyIds.contains(propertyId);
    }

    /**
     * Get effective limit with default
     */
    public int getEffectiveLimit() {
        return limit != null ? limit : 1000;
    }

    /**
     * Get effective offset with default
     */
    public int getEffectiveOffset() {
        return offset != null ? offset : 0;
    }

    /**
     * Check if pagination is requested
     */
    public boolean isPaginated() {
        return limit != null || offset != null;
    }

    /**
     * Calculate recommended time granularity based on period duration
     */
    public TimeGranularity getRecommendedGranularity() {
        java.time.Duration duration = getPeriodDuration();
        if (duration == null) {
            return TimeGranularity.DAILY;
        }

        return TimeGranularity.getRecommendedForDuration(duration);
    }

    /**
     * Check if the requested granularity is suitable for the period
     */
    public boolean isSuitableGranularity() {
        java.time.Duration duration = getPeriodDuration();
        return duration != null && timeGranularity.isSuitableForDuration(duration);
    }

    /**
     * Get estimated processing time in minutes
     */
    public int getEstimatedProcessingMinutes() {
        int baseTime = 1;

        // Adjust based on period duration
        java.time.Duration duration = getPeriodDuration();
        if (duration != null) {
            long days = duration.toDays();
            baseTime += Math.max(1, (int) (days / 30)); // +1 minute per month
        }

        // Adjust based on number of metrics
        if (metricTypes != null) {
            baseTime += metricTypes.size() / 10; // +1 minute per 10 metrics
        }

        // Adjust for complex features
        if (includeForecast) baseTime *= 2;
        if (includeTrends) baseTime += 1;
        if (includeComparison) baseTime += 1;
        if (includeBreakdown) baseTime += 2;

        return Math.max(1, baseTime);
    }
}
