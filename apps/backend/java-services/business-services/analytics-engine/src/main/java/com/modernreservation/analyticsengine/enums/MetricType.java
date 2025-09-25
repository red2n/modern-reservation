package com.modernreservation.analyticsengine.enums;

import java.util.List;
import java.util.Set;
import java.util.Arrays;

/**
 * Metric Type Enumeration
 *
 * Represents different types of business metrics and KPIs
 * that can be calculated and tracked by the analytics engine.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public enum MetricType {

    // Revenue Metrics
    TOTAL_REVENUE(
        "Total Revenue",
        "Sum of all revenue generated",
        "REVENUE",
        "CURRENCY",
        true,
        Set.of("SUM")
    ),

    REVENUE_TOTAL(
        "Total Revenue",
        "Sum of all revenue generated",
        "REVENUE",
        "CURRENCY",
        true,
        Set.of("SUM")
    ),

    REVPAR(
        "Revenue Per Available Room (RevPAR)",
        "Revenue divided by total available rooms",
        "REVENUE",
        "CURRENCY",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    REVENUE_PER_AVAILABLE_ROOM(
        "Revenue Per Available Room (RevPAR)",
        "Revenue divided by total available rooms",
        "REVENUE",
        "CURRENCY",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    ADR(
        "Average Daily Rate (ADR)",
        "Average rate charged per occupied room",
        "REVENUE",
        "CURRENCY",
        false,
        Set.of("AVERAGE", "TREND", "FORECAST")
    ),

    AVERAGE_DAILY_RATE(
        "Average Daily Rate (ADR)",
        "Average rate charged per occupied room",
        "REVENUE",
        "CURRENCY",
        false,
        Set.of("AVERAGE", "TREND", "FORECAST")
    ),

    REVENUE_PER_OCCUPIED_ROOM(
        "Revenue Per Occupied Room (RevPOR)",
        "Revenue divided by occupied rooms",
        "REVENUE",
        "CURRENCY",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    // Occupancy Metrics
    OCCUPANCY_RATE(
        "Occupancy Rate",
        "Percentage of rooms occupied",
        "OCCUPANCY",
        "PERCENTAGE",
        false,
        Set.of("AVERAGE", "TREND", "FORECAST")
    ),

    AVAILABLE_ROOMS(
        "Available Rooms",
        "Total number of rooms available",
        "OCCUPANCY",
        "COUNT",
        true,
        Set.of("SUM", "AVERAGE")
    ),

    OCCUPIED_ROOMS(
        "Occupied Rooms",
        "Number of rooms occupied",
        "OCCUPANCY",
        "COUNT",
        true,
        Set.of("SUM", "AVERAGE", "TREND")
    ),

    ROOM_NIGHTS_SOLD(
        "Room Nights Sold",
        "Total room nights sold",
        "OCCUPANCY",
        "COUNT",
        true,
        Set.of("SUM", "TREND")
    ),

    // Booking Metrics
    TOTAL_BOOKINGS(
        "Total Bookings",
        "Number of bookings made",
        "BOOKING",
        "COUNT",
        true,
        Set.of("SUM", "TREND")
    ),

    BOOKINGS_COUNT(
        "Total Bookings",
        "Number of bookings made",
        "BOOKING",
        "COUNT",
        true,
        Set.of("SUM", "TREND")
    ),

    BOOKING_CONVERSION_RATE(
        "Booking Conversion Rate",
        "Percentage of inquiries converted to bookings",
        "BOOKING",
        "PERCENTAGE",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    AVERAGE_BOOKING_VALUE(
        "Average Booking Value",
        "Average value per booking",
        "BOOKING",
        "CURRENCY",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    CANCELLATION_RATE(
        "Cancellation Rate",
        "Percentage of bookings cancelled",
        "BOOKING",
        "PERCENTAGE",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    NO_SHOW_RATE(
        "No-Show Rate",
        "Percentage of bookings with no-shows",
        "BOOKING",
        "PERCENTAGE",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    // Customer Metrics
    UNIQUE_GUESTS(
        "Unique Guests",
        "Number of unique guests",
        "CUSTOMER",
        "COUNT",
        true,
        Set.of("SUM", "TREND")
    ),

    TOTAL_GUESTS(
        "Total Guests",
        "Total number of guests",
        "CUSTOMER",
        "COUNT",
        true,
        Set.of("SUM", "TREND")
    ),

    GUEST_SATISFACTION_SCORE(
        "Guest Satisfaction Score",
        "Average guest satisfaction rating",
        "CUSTOMER",
        "SCORE",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    CUSTOMER_LIFETIME_VALUE(
        "Customer Lifetime Value (CLV)",
        "Predicted revenue from customer relationship",
        "CUSTOMER",
        "CURRENCY",
        false,
        Set.of("AVERAGE", "TREND", "SEGMENT")
    ),

    CUSTOMER_ACQUISITION_COST(
        "Customer Acquisition Cost (CAC)",
        "Cost to acquire a new customer",
        "CUSTOMER",
        "CURRENCY",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    REPEAT_CUSTOMER_RATE(
        "Repeat Customer Rate",
        "Percentage of returning customers",
        "CUSTOMER",
        "PERCENTAGE",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    CUSTOMER_SATISFACTION_SCORE(
        "Customer Satisfaction Score",
        "Average customer satisfaction rating",
        "CUSTOMER",
        "SCORE",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    // Financial Metrics
    GROSS_OPERATING_PROFIT(
        "Gross Operating Profit (GOP)",
        "Revenue minus operating expenses",
        "FINANCIAL",
        "CURRENCY",
        true,
        Set.of("SUM", "TREND")
    ),

    PROFIT_MARGIN(
        "Profit Margin",
        "Profit as percentage of revenue",
        "FINANCIAL",
        "PERCENTAGE",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    COST_PER_OCCUPIED_ROOM(
        "Cost Per Occupied Room (CPOR)",
        "Operating costs per occupied room",
        "FINANCIAL",
        "CURRENCY",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    // Channel Metrics
    CHANNEL_REVENUE_CONTRIBUTION(
        "Channel Revenue Contribution",
        "Revenue contribution by booking channel",
        "CHANNEL",
        "CURRENCY",
        true,
        Set.of("SUM", "PERCENTAGE", "TREND")
    ),

    CHANNEL_BOOKING_COUNT(
        "Channel Booking Count",
        "Number of bookings by channel",
        "CHANNEL",
        "COUNT",
        true,
        Set.of("SUM", "TREND")
    ),

    CHANNEL_CONVERSION_RATE(
        "Channel Conversion Rate",
        "Conversion rate by booking channel",
        "CHANNEL",
        "PERCENTAGE",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    // Market Metrics
    MARKET_PENETRATION_INDEX(
        "Market Penetration Index (MPI)",
        "Market share relative to competition",
        "MARKET",
        "INDEX",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    REVENUE_GENERATION_INDEX(
        "Revenue Generation Index (RGI)",
        "Revenue performance vs market",
        "MARKET",
        "INDEX",
        false,
        Set.of("AVERAGE", "TREND")
    ),

    COMPETITIVE_SET_PERFORMANCE(
        "Competitive Set Performance",
        "Performance vs competitive set",
        "MARKET",
        "INDEX",
        false,
        Set.of("AVERAGE", "TREND")
    );

    private final String displayName;
    private final String description;
    private final String category;
    private final String unit;
    private final boolean isAdditive;
    private final Set<String> supportedAggregations;

    MetricType(String displayName, String description, String category, String unit,
               boolean isAdditive, Set<String> supportedAggregations) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.unit = unit;
        this.isAdditive = isAdditive;
        this.supportedAggregations = supportedAggregations;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isAdditive() {
        return isAdditive;
    }

    public Set<String> getSupportedAggregations() {
        return supportedAggregations;
    }

    /**
     * Check if metric is a revenue-related metric
     */
    public boolean isRevenueMetric() {
        return "REVENUE".equals(category) || "FINANCIAL".equals(category);
    }

    /**
     * Check if metric is an occupancy-related metric
     */
    public boolean isOccupancyMetric() {
        return "OCCUPANCY".equals(category);
    }

    /**
     * Check if metric is a customer-related metric
     */
    public boolean isCustomerMetric() {
        return "CUSTOMER".equals(category);
    }

    /**
     * Check if metric supports trend analysis
     */
    public boolean supportsTrendAnalysis() {
        return supportedAggregations.contains("TREND");
    }

    /**
     * Check if metric supports forecasting
     */
    public boolean supportsForecasting() {
        return supportedAggregations.contains("FORECAST");
    }

    /**
     * Check if metric can be segmented
     */
    public boolean supportsSegmentation() {
        return supportedAggregations.contains("SEGMENT");
    }

    /**
     * Check if metric is a ratio or percentage
     */
    public boolean isRatio() {
        return "PERCENTAGE".equals(unit) || "INDEX".equals(unit) || "SCORE".equals(unit);
    }

    /**
     * Check if metric is currency-based
     */
    public boolean isCurrencyBased() {
        return "CURRENCY".equals(unit);
    }

    /**
     * Get default aggregation method
     */
    public String getDefaultAggregation() {
        if (supportedAggregations.contains("SUM")) {
            return "SUM";
        } else if (supportedAggregations.contains("AVERAGE")) {
            return "AVERAGE";
        } else {
            return supportedAggregations.iterator().next();
        }
    }

    /**
     * Get metrics by category
     */
    public static Set<MetricType> getMetricsByCategory(String category) {
        return Set.of(values()).stream()
            .filter(metric -> metric.getCategory().equals(category))
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Get all revenue metrics
     */
    public static Set<MetricType> getRevenueMetrics() {
        return getMetricsByCategory("REVENUE");
    }

    /**
     * Get all occupancy metrics
     */
    public static Set<MetricType> getOccupancyMetrics() {
        return getMetricsByCategory("OCCUPANCY");
    }

    /**
     * Get all customer metrics
     */
    public static Set<MetricType> getCustomerMetrics() {
        return getMetricsByCategory("CUSTOMER");
    }

    /**
     * Get metrics suitable for forecasting
     */
    public static Set<MetricType> getForecastableMetrics() {
        return Set.of(values()).stream()
            .filter(MetricType::supportsForecasting)
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Check if this metric is derived from other metrics
     */
    public boolean isDerived() {
        return this == REVENUE_PER_AVAILABLE_ROOM ||
               this == AVERAGE_DAILY_RATE ||
               this == REVENUE_PER_OCCUPIED_ROOM ||
               this == OCCUPANCY_RATE ||
               this == CUSTOMER_LIFETIME_VALUE;
    }

    /**
     * Get component metrics for derived metrics
     */
    public List<MetricType> getComponentMetrics() {
        return switch (this) {
            case REVPAR, REVENUE_PER_AVAILABLE_ROOM -> Arrays.asList(TOTAL_REVENUE, AVAILABLE_ROOMS);
            case ADR, AVERAGE_DAILY_RATE -> Arrays.asList(TOTAL_REVENUE, OCCUPIED_ROOMS);
            case REVENUE_PER_OCCUPIED_ROOM -> Arrays.asList(TOTAL_REVENUE, OCCUPIED_ROOMS);
            case OCCUPANCY_RATE -> Arrays.asList(OCCUPIED_ROOMS, AVAILABLE_ROOMS);
            case CUSTOMER_LIFETIME_VALUE -> Arrays.asList(TOTAL_REVENUE, UNIQUE_GUESTS);
            default -> Arrays.asList();
        };
    }

    /**
     * Get calculation strategy for this metric
     */
    public CalculationStrategy getCalculationStrategy() {
        return switch (this) {
            case TOTAL_REVENUE, REVENUE_TOTAL, TOTAL_BOOKINGS, BOOKINGS_COUNT, TOTAL_GUESTS, OCCUPIED_ROOMS -> CalculationStrategy.SUM;
            case OCCUPANCY_RATE, CANCELLATION_RATE, NO_SHOW_RATE -> CalculationStrategy.PERCENTAGE;
            case ADR, AVERAGE_DAILY_RATE, REVPAR, REVENUE_PER_AVAILABLE_ROOM, REVENUE_PER_OCCUPIED_ROOM -> CalculationStrategy.RATIO;
            case CUSTOMER_LIFETIME_VALUE, GUEST_SATISFACTION_SCORE -> CalculationStrategy.COMPLEX;
            case AVAILABLE_ROOMS -> CalculationStrategy.LATEST;
            default -> CalculationStrategy.AVERAGE;
        };
    }

    /**
     * Metric categories for grouping
     */
    public enum MetricCategory {
        REVENUE, OCCUPANCY, BOOKING, CUSTOMER, FINANCIAL, CHANNEL, MARKET
    }

    /**
     * Calculation strategies for metrics
     */
    public enum CalculationStrategy {
        SUM, AVERAGE, LATEST, COUNT, PERCENTAGE, RATIO, COMPLEX
    }
}
