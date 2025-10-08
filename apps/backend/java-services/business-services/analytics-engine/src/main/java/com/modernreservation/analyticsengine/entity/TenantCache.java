package com.modernreservation.analyticsengine.entity;

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
 * This cache enables fast tenant validation without network calls during analytics processing.
 *
 * Purpose: Ensure analytics data is properly segmented and filtered per tenant
 * - Prevent deleted tenants from accessing analytics
 * - Block suspended tenants from viewing reports
 * - Filter analytics data to only include operational tenants
 *
 * Architecture:
 * - Source of truth: Tenant Service (master data)
 * - Synchronization: Kafka events (real-time updates)
 * - Local storage: PostgreSQL (durable, queryable)
 * - Performance layer: Redis cache via Spring @Cacheable
 *
 * @see com.modernreservation.analyticsengine.kafka.TenantEventConsumer
 * @see com.modernreservation.analyticsengine.service.TenantCacheService
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
    private UUID id;

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

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "cache_created_at", nullable = false, updatable = false)
    private Instant cacheCreatedAt;

    @UpdateTimestamp
    @Column(name = "cache_updated_at")
    private Instant cacheUpdatedAt;

    /**
     * Check if tenant can access analytics
     */
    public boolean canAccessAnalytics() {
        return deletedAt == null &&
               (status == TenantStatus.ACTIVE || status == TenantStatus.TRIAL);
    }

    /**
     * Check if tenant is operational (not deleted)
     */
    public boolean isOperational() {
        return deletedAt == null;
    }
}
