package com.modernreservation.tenant.commons.context;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * TenantContext - Thread-local storage for current tenant ID
 *
 * Provides thread-safe access to the current tenant context throughout
 * the request lifecycle. The tenant ID is extracted from JWT token and
 * stored in ThreadLocal for use in service/repository layers.
 *
 * CRITICAL: Must be cleared after request completes to prevent
 * thread pool pollution and cross-request data leaks.
 *
 * This is a SHARED LIBRARY component used by ALL microservices.
 *
 * Usage:
 * <pre>
 * // Set tenant (typically in filter/interceptor)
 * TenantContext.setCurrentTenantId(tenantId);
 *
 * // Get tenant (in service/repository)
 * UUID tenantId = TenantContext.getCurrentTenantId();
 *
 * // Clear tenant (in finally block)
 * TenantContext.clear();
 * </pre>
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    /**
     * Set the current tenant ID for this thread
     *
     * @param tenantId The tenant UUID from JWT token
     */
    public static void setCurrentTenantId(UUID tenantId) {
        if (tenantId == null) {
            log.warn("Attempted to set null tenant ID in context");
            return;
        }

        currentTenant.set(tenantId);
        log.debug("Tenant context set: {}", tenantId);
    }

    /**
     * Get the current tenant ID for this thread
     *
     * @return The current tenant UUID
     * @throws IllegalStateException if no tenant context is set
     */
    public static UUID getCurrentTenantId() {
        UUID tenantId = currentTenant.get();

        if (tenantId == null) {
            log.error("No tenant context found - this should not happen in authenticated requests");
            throw new IllegalStateException(
                "No tenant context available. Ensure TenantContext is set in filter/interceptor."
            );
        }

        return tenantId;
    }

    /**
     * Get the current tenant ID or null if not set
     *
     * Useful for optional tenant context scenarios (e.g., public endpoints)
     *
     * @return The current tenant UUID or null
     */
    public static UUID getCurrentTenantIdOrNull() {
        return currentTenant.get();
    }

    /**
     * Check if tenant context is currently set
     *
     * @return true if tenant context exists, false otherwise
     */
    public static boolean hasTenantContext() {
        return currentTenant.get() != null;
    }

    /**
     * Clear the tenant context for this thread
     *
     * MUST be called after request completes to prevent thread pool pollution.
     * Typically called in finally block of filter/interceptor.
     */
    public static void clear() {
        UUID tenantId = currentTenant.get();
        if (tenantId != null) {
            log.debug("Clearing tenant context: {}", tenantId);
        }
        currentTenant.remove();
    }

    /**
     * Special tenant ID for system operations (superadmin)
     */
    public static final UUID SYSTEM_TENANT_ID =
        UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Check if current tenant is system tenant (superadmin)
     *
     * @return true if current tenant is system tenant
     */
    public static boolean isSystemTenant() {
        UUID tenantId = getCurrentTenantIdOrNull();
        return SYSTEM_TENANT_ID.equals(tenantId);
    }
}
