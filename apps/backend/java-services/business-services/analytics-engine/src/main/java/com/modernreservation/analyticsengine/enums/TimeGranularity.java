package com.modernreservation.analyticsengine.enums;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * Time Granularity Enumeration
 *
 * Represents different time granularities for analytics aggregation.
 * Used for time-series analysis, reporting periods, and data bucketing.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public enum TimeGranularity {

    /**
     * Hourly aggregation - for real-time analytics
     */
    HOURLY(
        "Hourly",
        "Hour-by-hour analysis",
        ChronoUnit.HOURS,
        1,
        "yyyy-MM-dd HH:00",
        168 // 1 week max
    ),

    /**
     * Daily aggregation - most common for operational reporting
     */
    DAILY(
        "Daily",
        "Day-by-day analysis",
        ChronoUnit.DAYS,
        1,
        "yyyy-MM-dd",
        365 // 1 year max
    ),

    /**
     * Weekly aggregation - for trend analysis
     */
    WEEKLY(
        "Weekly",
        "Week-by-week analysis",
        ChronoUnit.WEEKS,
        1,
        "yyyy-'W'ww",
        104 // 2 years max
    ),

    /**
     * Monthly aggregation - for strategic planning
     */
    MONTHLY(
        "Monthly",
        "Month-by-month analysis",
        ChronoUnit.MONTHS,
        1,
        "yyyy-MM",
        60 // 5 years max
    ),

    /**
     * Quarterly aggregation - for executive reporting
     */
    QUARTERLY(
        "Quarterly",
        "Quarter-by-quarter analysis",
        ChronoUnit.MONTHS,
        3,
        "yyyy-'Q'Q",
        40 // 10 years max
    ),

    /**
     * Yearly aggregation - for long-term trends
     */
    YEARLY(
        "Yearly",
        "Year-by-year analysis",
        ChronoUnit.YEARS,
        1,
        "yyyy",
        20 // 20 years max
    ),

    /**
     * Real-time aggregation - for live dashboards
     */
    REAL_TIME(
        "Real-time",
        "Real-time streaming data",
        ChronoUnit.MINUTES,
        5,
        "yyyy-MM-dd HH:mm",
        288 // 1 day max (5-minute intervals)
    ),

    /**
     * Custom period - for flexible analysis
     */
    CUSTOM(
        "Custom Period",
        "User-defined period",
        ChronoUnit.DAYS,
        1,
        "yyyy-MM-dd",
        1000 // Flexible max
    );

    private final String displayName;
    private final String description;
    private final TemporalUnit temporalUnit;
    private final int unitAmount;
    private final String dateFormat;
    private final int maxPeriods;

    TimeGranularity(String displayName, String description, TemporalUnit temporalUnit,
                   int unitAmount, String dateFormat, int maxPeriods) {
        this.displayName = displayName;
        this.description = description;
        this.temporalUnit = temporalUnit;
        this.unitAmount = unitAmount;
        this.dateFormat = dateFormat;
        this.maxPeriods = maxPeriods;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public TemporalUnit getTemporalUnit() {
        return temporalUnit;
    }

    public int getUnitAmount() {
        return unitAmount;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public int getMaxPeriods() {
        return maxPeriods;
    }

    /**
     * Check if granularity is suitable for real-time analytics
     */
    public boolean isRealTime() {
        return this == REAL_TIME || this == HOURLY;
    }

    /**
     * Check if granularity is suitable for operational reporting
     */
    public boolean isOperational() {
        return this == DAILY || this == WEEKLY;
    }

    /**
     * Check if granularity is suitable for strategic analysis
     */
    public boolean isStrategic() {
        return this == MONTHLY || this == QUARTERLY || this == YEARLY;
    }

    /**
     * Get the next higher granularity level
     */
    public TimeGranularity getNextHigherGranularity() {
        return switch (this) {
            case REAL_TIME -> HOURLY;
            case HOURLY -> DAILY;
            case DAILY -> WEEKLY;
            case WEEKLY -> MONTHLY;
            case MONTHLY -> QUARTERLY;
            case QUARTERLY -> YEARLY;
            case YEARLY -> YEARLY; // Already highest
            case CUSTOM -> DAILY; // Default fallback
        };
    }

    /**
     * Get the next lower granularity level
     */
    public TimeGranularity getNextLowerGranularity() {
        return switch (this) {
            case YEARLY -> QUARTERLY;
            case QUARTERLY -> MONTHLY;
            case MONTHLY -> WEEKLY;
            case WEEKLY -> DAILY;
            case DAILY -> HOURLY;
            case HOURLY -> REAL_TIME;
            case REAL_TIME -> REAL_TIME; // Already lowest
            case CUSTOM -> DAILY; // Default fallback
        };
    }

    /**
     * Check if this granularity is finer than another
     */
    public boolean isFinerThan(TimeGranularity other) {
        return this.getOrdinalValue() < other.getOrdinalValue();
    }

    /**
     * Check if this granularity is coarser than another
     */
    public boolean isCoarserThan(TimeGranularity other) {
        return this.getOrdinalValue() > other.getOrdinalValue();
    }

    /**
     * Get ordinal value for comparison (lower = finer granularity)
     */
    private int getOrdinalValue() {
        return switch (this) {
            case REAL_TIME -> 0;
            case HOURLY -> 1;
            case DAILY -> 2;
            case WEEKLY -> 3;
            case MONTHLY -> 4;
            case QUARTERLY -> 5;
            case YEARLY -> 6;
            case CUSTOM -> 2; // Same as daily by default
        };
    }

    /**
     * Get suitable granularities for a given analysis type
     */
    public static TimeGranularity[] getSuitableGranularities(String analysisType) {
        return switch (analysisType.toUpperCase()) {
            case "REAL_TIME", "LIVE", "DASHBOARD" ->
                new TimeGranularity[]{REAL_TIME, HOURLY, DAILY};
            case "OPERATIONAL", "DAILY_REPORT" ->
                new TimeGranularity[]{DAILY, WEEKLY, MONTHLY};
            case "STRATEGIC", "PLANNING", "FORECAST" ->
                new TimeGranularity[]{MONTHLY, QUARTERLY, YEARLY};
            case "TREND", "ANALYSIS" ->
                new TimeGranularity[]{DAILY, WEEKLY, MONTHLY, QUARTERLY};
            default ->
                new TimeGranularity[]{DAILY, WEEKLY, MONTHLY};
        };
    }

    /**
     * Get default granularity for metric type
     */
    public static TimeGranularity getDefaultForMetric(MetricType metricType) {
        if (metricType.isRevenueMetric()) {
            return DAILY;
        } else if (metricType.isOccupancyMetric()) {
            return DAILY;
        } else if (metricType.isCustomerMetric()) {
            return MONTHLY;
        } else {
            return DAILY;
        }
    }

    /**
     * Calculate approximate number of data points for a time range
     */
    public long calculateDataPoints(java.time.Duration duration) {
        return switch (this) {
            case REAL_TIME -> duration.toMinutes() / 5;
            case HOURLY -> duration.toHours();
            case DAILY -> duration.toDays();
            case WEEKLY -> duration.toDays() / 7;
            case MONTHLY -> duration.toDays() / 30;
            case QUARTERLY -> duration.toDays() / 90;
            case YEARLY -> duration.toDays() / 365;
            case CUSTOM -> duration.toDays(); // Default to daily
        };
    }

    /**
     * Check if granularity is suitable for the given duration
     */
    public boolean isSuitableForDuration(java.time.Duration duration) {
        long dataPoints = calculateDataPoints(duration);
        return dataPoints <= maxPeriods && dataPoints >= 2; // Need at least 2 points for analysis
    }

    /**
     * Get recommended granularity for a given duration
     */
    public static TimeGranularity getRecommendedForDuration(java.time.Duration duration) {
        for (TimeGranularity granularity : values()) {
            if (granularity.isSuitableForDuration(duration)) {
                return granularity;
            }
        }
        return DAILY; // Fallback
    }
}
