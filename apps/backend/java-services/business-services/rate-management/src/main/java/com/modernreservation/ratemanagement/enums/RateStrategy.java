package com.modernreservation.ratemanagement.enums;

/**
 * Rate strategy types for different pricing approaches
 */
public enum RateStrategy {
    /**
     * Fixed rate that doesn't change based on demand
     */
    FIXED("Fixed Rate", "Static pricing with no demand adjustments"),

    /**
     * Dynamic pricing based on demand and occupancy
     */
    DYNAMIC("Dynamic Rate", "Price adjusts based on demand and occupancy levels"),

    /**
     * Seasonal rate adjustments
     */
    SEASONAL("Seasonal Rate", "Price varies by season and special periods"),

    /**
     * Last-minute pricing strategy
     */
    LAST_MINUTE("Last Minute Rate", "Special pricing for last-minute bookings"),

    /**
     * Early bird pricing strategy
     */
    EARLY_BIRD("Early Bird Rate", "Discounted rates for advance bookings"),

    /**
     * Revenue optimization strategy
     */
    REVENUE_OPTIMIZATION("Revenue Optimization", "AI-driven pricing for maximum revenue"),

    /**
     * Competitive pricing based on market rates
     */
    COMPETITIVE("Competitive Rate", "Pricing based on competitor analysis"),

    /**
     * Package deal pricing
     */
    PACKAGE("Package Rate", "Bundled pricing with additional services");

    private final String displayName;
    private final String description;

    RateStrategy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this strategy supports dynamic adjustments
     */
    public boolean isDynamic() {
        return this == DYNAMIC || this == REVENUE_OPTIMIZATION || this == COMPETITIVE;
    }

    /**
     * Check if this strategy is time-sensitive
     */
    public boolean isTimeSensitive() {
        return this == LAST_MINUTE || this == EARLY_BIRD || this == SEASONAL;
    }

    /**
     * Get the priority level for rate calculation (higher number = higher priority)
     */
    public int getPriority() {
        return switch (this) {
            case REVENUE_OPTIMIZATION -> 10;
            case DYNAMIC -> 8;
            case COMPETITIVE -> 7;
            case PACKAGE -> 6;
            case SEASONAL -> 5;
            case LAST_MINUTE -> 4;
            case EARLY_BIRD -> 3;
            case FIXED -> 1;
        };
    }
}
