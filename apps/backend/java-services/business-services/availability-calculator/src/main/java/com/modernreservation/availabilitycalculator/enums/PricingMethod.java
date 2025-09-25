package com.modernreservation.availabilitycalculator.enums;

/**
 * Pricing calculation methods
 */
public enum PricingMethod {
    BASE_RATE("Use base rate only"),
    DYNAMIC("Dynamic pricing based on demand"),
    SEASONAL("Seasonal pricing adjustment"),
    PROMOTIONAL("Promotional pricing"),
    LAST_MINUTE("Last minute pricing"),
    ADVANCE_BOOKING("Advance booking discount"),
    LENGTH_OF_STAY("Length of stay pricing"),
    OCCUPANCY_BASED("Occupancy-based pricing"),
    MARKET_SEGMENT("Market segment pricing"),
    CHANNEL_SPECIFIC("Channel-specific pricing");

    private final String description;

    PricingMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresDemandAnalysis() {
        return this == DYNAMIC || this == OCCUPANCY_BASED;
    }

    public boolean isTimeDependent() {
        return this == SEASONAL || this == LAST_MINUTE || this == ADVANCE_BOOKING;
    }
}
