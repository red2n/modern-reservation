package com.modernreservation.ratemanagement.enums;

/**
 * Rate status for tracking rate lifecycle
 */
public enum RateStatus {
    /**
     * Draft rate not yet published
     */
    DRAFT("Draft", "Rate is being prepared and not yet active"),

    /**
     * Active rate available for booking
     */
    ACTIVE("Active", "Rate is live and available for bookings"),

    /**
     * Suspended rate temporarily unavailable
     */
    SUSPENDED("Suspended", "Rate is temporarily unavailable"),

    /**
     * Expired rate no longer valid
     */
    EXPIRED("Expired", "Rate has passed its validity period"),

    /**
     * Archived rate kept for historical purposes
     */
    ARCHIVED("Archived", "Rate archived for historical reference"),

    /**
     * Cancelled rate that was removed
     */
    CANCELLED("Cancelled", "Rate was cancelled and removed from system");

    private final String displayName;
    private final String description;

    RateStatus(String displayName, String description) {
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
     * Check if rate is available for booking
     */
    public boolean isBookable() {
        return this == ACTIVE;
    }

    /**
     * Check if rate can be modified
     */
    public boolean isModifiable() {
        return this == DRAFT || this == SUSPENDED;
    }

    /**
     * Check if rate is in a final state
     */
    public boolean isFinal() {
        return this == EXPIRED || this == ARCHIVED || this == CANCELLED;
    }

    /**
     * Get the next possible states from current state
     */
    public RateStatus[] getNextPossibleStates() {
        return switch (this) {
            case DRAFT -> new RateStatus[]{ACTIVE, CANCELLED};
            case ACTIVE -> new RateStatus[]{SUSPENDED, EXPIRED, ARCHIVED};
            case SUSPENDED -> new RateStatus[]{ACTIVE, CANCELLED, ARCHIVED};
            case EXPIRED -> new RateStatus[]{ARCHIVED};
            case ARCHIVED -> new RateStatus[]{}; // No transitions from archived
            case CANCELLED -> new RateStatus[]{}; // No transitions from cancelled
        };
    }
}
