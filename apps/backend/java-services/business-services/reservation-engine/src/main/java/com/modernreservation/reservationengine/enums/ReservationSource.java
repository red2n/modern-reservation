package com.modernreservation.reservationengine.enums;

/**
 * Reservation Source Enumeration
 *
 * Defines the channel or source through which a reservation was made.
 */
public enum ReservationSource {
    DIRECT("Direct booking"),
    PHONE("Phone booking"),
    EMAIL("Email booking"),
    WALK_IN("Walk-in booking"),
    BOOKING_COM("Booking.com"),
    EXPEDIA("Expedia"),
    AIRBNB("Airbnb"),
    HOTELS_COM("Hotels.com"),
    AGODA("Agoda"),
    PRICELINE("Priceline"),
    KAYAK("Kayak"),
    TRIVAGO("Trivago"),
    CORPORATE("Corporate booking"),
    TRAVEL_AGENT("Travel agent"),
    GROUP_BOOKING("Group booking"),
    EVENT_BOOKING("Event booking"),
    MOBILE_APP("Mobile app"),
    SOCIAL_MEDIA("Social media"),
    API("API integration"),
    IMPORT("Data import"),
    ADMIN("Admin created"),
    OTHER("Other");

    private final String description;

    ReservationSource(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOTA() {
        return this == BOOKING_COM || this == EXPEDIA || this == AIRBNB ||
               this == HOTELS_COM || this == AGODA || this == PRICELINE ||
               this == KAYAK || this == TRIVAGO;
    }

    public boolean isDirect() {
        return this == DIRECT || this == PHONE || this == EMAIL ||
               this == WALK_IN || this == MOBILE_APP;
    }

    public boolean requiresCommission() {
        return isOTA() || this == TRAVEL_AGENT;
    }
}
