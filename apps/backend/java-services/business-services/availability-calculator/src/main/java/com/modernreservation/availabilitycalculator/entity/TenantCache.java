package com.modernreservation.availabilitycalculator.entity;

import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import com.modernreservation.tenant.commons.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Local cache of tenant data synchronized from Tenant Service via Kafka events.
 * This cache enables fast tenant validation without network calls during availability calculations.
 *
 * Purpose: Ensure availability calculations are properly isolated per tenant
 * - Prevent deleted tenants from checking availability
 * - Block suspended tenants from accessing availability data
 * - Validate tenant status before performing availability calculations
 *
 * Architecture:
 * - Source of truth: Tenant Service (master data)
 * - Synchronization: Kafka events (real-time updates)
 * - Local storage: PostgreSQL (durable, queryable)
 * - Performance layer: Redis cache via Spring @Cacheable
 *
 * Data Flow:
 * Tenant Service → Kafka Event → TenantEventConsumer → TenantCache (PostgreSQL) → Redis Cache → Availability Logic
 *
 * @see com.modernreservation.availabilitycalculator.kafka.TenantEventConsumer
 * @see com.modernreservation.availabilitycalculator.service.TenantCacheService
 */
@Entity
@Table(name = "tenant_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantCache {

    @Id
    private UUID id;  // Same UUID as in Tenant Service

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TenantType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TenantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", length = 50)
    private SubscriptionPlan subscriptionPlan;

    /**
     * Timestamp when this cache entry was last synchronized from Kafka event
     */
    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    /**
     * Soft delete timestamp from Tenant Service
     * When set, tenant should be treated as deleted
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Local cache management timestamps (auto-managed by JPA)
     */
    @CreationTimestamp
    @Column(name = "cache_created_at", nullable = false, updatable = false)
    private Instant cacheCreatedAt;

    @UpdateTimestamp
    @Column(name = "cache_updated_at")
    private Instant cacheUpdatedAt;

    /**
     * Check if tenant can perform availability calculations
     *
     * @return true if tenant is ACTIVE or TRIAL and not deleted
     */
    public boolean canCalculateAvailability() {
        return deletedAt == null &&
               (status == TenantStatus.ACTIVE || status == TenantStatus.TRIAL);
    }

    /**
     * Check if tenant is operational (not deleted)
     *
     * @return true if tenant is not soft-deleted
     */
    public boolean isOperational() {
        return deletedAt == null;
    }
}
