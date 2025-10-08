package com.modernreservation.tenant.commons.enums;

/**
 * TenantRole - User roles within a tenant organization
 *
 * Matches TypeScript TenantRoleSchema
 * Used for user-tenant associations (multi-tenant user access)
 */
public enum TenantRole {
    /**
     * Tenant owner - Full access to all features
     */
    OWNER,

    /**
     * Tenant administrator - Full administrative access
     */
    ADMIN,

    /**
     * Manager - Property/operations management
     */
    MANAGER,

    /**
     * Staff member - Limited operational access
     */
    STAFF,

    /**
     * Accountant - Financial and reporting access
     */
    ACCOUNTANT,

    /**
     * Viewer - Read-only access
     */
    VIEWER
}
