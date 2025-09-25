package com.modernreservation.ratemanagement.enums;

/**
 * Season types for seasonal pricing
 */
public enum SeasonType {
    /**
     * Peak season with highest rates
     */
    PEAK("Peak Season", "Highest demand period with premium pricing", 1.5),

    /**
     * High season with elevated rates
     */
    HIGH("High Season", "High demand period with increased pricing", 1.3),

    /**
     * Regular season with standard rates
     */
    REGULAR("Regular Season", "Standard demand period with base pricing", 1.0),

    /**
     * Low season with reduced rates
     */
    LOW("Low Season", "Lower demand period with discounted pricing", 0.8),

    /**
     * Off season with lowest rates
     */
    OFF("Off Season", "Lowest demand period with minimum pricing", 0.6),

    /**
     * Special event season with premium rates
     */
    SPECIAL_EVENT("Special Event", "Special events and holidays with premium pricing", 2.0),

    /**
     * Corporate season for business travelers
     */
    CORPORATE("Corporate Season", "Business travel focused periods", 1.1);

    private final String displayName;
    private final String description;
    private final double multiplier;

    SeasonType(String displayName, String description, double multiplier) {
        this.displayName = displayName;
        this.description = description;
        this.multiplier = multiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Check if this is a premium season
     */
    public boolean isPremium() {
        return multiplier > 1.2;
    }

    /**
     * Check if this is a discount season
     */
    public boolean isDiscount() {
        return multiplier < 1.0;
    }

    /**
     * Get the revenue impact level
     */
    public String getRevenueImpact() {
        if (multiplier >= 1.5) return "VERY_HIGH";
        if (multiplier >= 1.2) return "HIGH";
        if (multiplier > 0.9) return "NORMAL";
        if (multiplier > 0.7) return "LOW";
        return "VERY_LOW";
    }
}
