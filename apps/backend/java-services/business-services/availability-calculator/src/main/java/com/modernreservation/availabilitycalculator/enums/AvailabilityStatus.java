package com.modernreservation.availabilitycalculator.enums;

/**
 * Room availability status enumeration
 */
public enum AvailabilityStatus {
    AVAILABLE("Available for booking"),
    OCCUPIED("Currently occupied"),
    MAINTENANCE("Under maintenance"),
    OUT_OF_ORDER("Out of order"),
    BLOCKED("Blocked for booking"),
    RESERVED("Reserved but not confirmed"),
    CONFIRMED("Confirmed reservation"),
    CHECKED_IN("Guest checked in"),
    CHECKED_OUT("Guest checked out"),
    NO_SHOW("Guest did not show"),
    CANCELLED("Reservation cancelled");

    private final String description;

    AvailabilityStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBookable() {
        return this == AVAILABLE;
    }

    public boolean isOccupied() {
        return this == OCCUPIED || this == CHECKED_IN;
    }
}
