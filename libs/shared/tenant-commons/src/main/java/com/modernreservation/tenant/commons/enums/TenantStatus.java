package com.modernreservation.tenant.commons.enums;

/**
 * TenantStatus - Account status for tenants
 *
 * Represents the current state of a tenant's subscription/account.
 * Used across all microservices for access control and validation.
 */
public enum TenantStatus {
    /**
     * Active, paid account with full access
     */
    ACTIVE,

    /**
     * Account suspended (payment issue, policy violation)
     * Access is blocked until issue is resolved
     */
    SUSPENDED,

    /**
     * Trial period - limited features or time-bound access
     */
    TRIAL,

    /**
     * Subscription expired - needs renewal
     */
    EXPIRED,

    /**
     * Account cancelled by user or admin
     * Soft deleted, data retained for recovery period
     */
    CANCELLED
}
