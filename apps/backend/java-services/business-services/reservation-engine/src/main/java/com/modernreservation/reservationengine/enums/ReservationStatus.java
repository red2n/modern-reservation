package com.modernreservation.reservationengine.enums;

/**
 * Reservation Status Enumeration
 *
 * Defines all possible states of a reservation throughout its lifecycle.
 */
public enum ReservationStatus {
    PENDING("Pending confirmation"),
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked in"),
    CHECKED_OUT("Checked out"),
    CANCELLED("Cancelled"),
    NO_SHOW("No show"),
    EXPIRED("Expired"),
    WAITLISTED("Waitlisted"),
    MODIFIED("Modified"),
    ON_HOLD("On hold");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == PENDING || this == CONFIRMED || this == CHECKED_IN || this == ON_HOLD;
    }

    public boolean isFinal() {
        return this == CHECKED_OUT || this == CANCELLED || this == NO_SHOW || this == EXPIRED;
    }

    public boolean canBeModified() {
        return this == PENDING || this == CONFIRMED || this == ON_HOLD;
    }

    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED || this == ON_HOLD;
    }
}
