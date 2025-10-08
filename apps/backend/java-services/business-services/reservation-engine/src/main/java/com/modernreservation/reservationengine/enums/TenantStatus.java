package com.modernreservation.reservationengine.enums;

/**
 * Tenant Status Enum - Account status for tenants
 */
public enum TenantStatus {
    ACTIVE,         // Active tenant account
    SUSPENDED,      // Temporarily suspended
    TRIAL,          // Trial period
    EXPIRED,        // Subscription expired
    CANCELLED       // Account cancelled
}
