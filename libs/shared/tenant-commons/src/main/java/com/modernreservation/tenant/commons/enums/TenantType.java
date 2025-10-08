package com.modernreservation.tenant.commons.enums;

/**
 * TenantType - Types of organizations using the reservation system
 *
 * Represents different business models for hospitality organizations.
 * Used across all microservices for consistent tenant categorization.
 */
public enum TenantType {
    /**
     * Hotel chain with multiple properties
     * Example: Taj Hotels, Marriott, ITC Hotels
     */
    CHAIN,

    /**
     * Independent single property or small group
     * Example: Boutique hotels, independent resorts
     */
    INDEPENDENT,

    /**
     * Franchise of a larger brand
     * Example: Holiday Inn franchise, Hilton Garden Inn franchise
     */
    FRANCHISE,

    /**
     * Management company operating multiple properties
     * Example: Property management companies
     */
    MANAGEMENT_COMPANY,

    /**
     * Vacation rental management
     * Example: Airbnb-style property managers, vacation home rentals
     */
    VACATION_RENTAL
}
