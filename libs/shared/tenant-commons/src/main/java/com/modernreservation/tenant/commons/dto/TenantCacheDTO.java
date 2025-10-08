package com.modernreservation.tenant.commons.dto;

import com.modernreservation.tenant.commons.enums.SubscriptionPlan;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantCacheDTO - Lightweight tenant data for local caching
 *
 * Used by business services to cache tenant information locally
 * without calling the Tenant Service for every request.
 *
 * Contains only essential fields needed for validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantCacheDTO {

    /**
     * Tenant ID (primary key)
     */
    private UUID tenantId;

    /**
     * Tenant name
     */
    private String name;

    /**
     * Tenant slug (URL-friendly identifier)
     */
    private String slug;

    /**
     * Tenant type
     */
    private TenantType type;

    /**
     * Current status (used for validation)
     */
    private TenantStatus status;

    /**
     * Subscription plan (for feature checking)
     */
    private SubscriptionPlan subscriptionPlan;

    /**
     * When tenant was last synced from Tenant Service
     */
    private LocalDateTime lastSyncedAt;

    /**
     * When tenant was deleted (soft delete)
     */
    private LocalDateTime deletedAt;

    /**
     * Check if tenant is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if tenant is active (status = ACTIVE and not deleted)
     */
    public boolean isActive() {
        return TenantStatus.ACTIVE.equals(status) && !isDeleted();
    }

    /**
     * Check if tenant cache is stale (older than 24 hours)
     */
    public boolean isStale() {
        if (lastSyncedAt == null) {
            return true;
        }
        return lastSyncedAt.isBefore(LocalDateTime.now().minusHours(24));
    }
}
