package com.modernreservation.availabilitycalculator.enums;

/**
 * Room type categories for availability calculations
 */
public enum RoomCategory {
    STANDARD("Standard Room"),
    DELUXE("Deluxe Room"),
    SUITE("Suite"),
    PRESIDENTIAL("Presidential Suite"),
    PENTHOUSE("Penthouse"),
    FAMILY("Family Room"),
    BUSINESS("Business Room"),
    ACCESSIBLE("Accessible Room"),
    VILLA("Villa"),
    STUDIO("Studio");

    private final String description;

    RoomCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
