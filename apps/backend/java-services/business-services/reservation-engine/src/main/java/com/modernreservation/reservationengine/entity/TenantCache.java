package com.modernreservation.reservationengine.entity;

import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantCache Entity - Local Read-Only Tenant Cache
 *
 * This is a LOCAL CACHE of tenant data synchronized from the Tenant Service via Kafka events.
 * This is NOT the source of truth - the Tenant Service owns all tenant data.
 *
 * Purpose:
 * - Fast local access to tenant information
 * - No cross-service HTTP calls needed
 * - Eventual consistency via Kafka events
 * - Read-only - updates happen ONLY via Kafka consumers
 *
 * Synchronization:
 * - TenantEventConsumer listens to Kafka topics (tenant.*)
 * - Updates this cache when events arrive
 * - Cache is automatically kept in sync with master Tenant Service
 */
@Entity
@Table(name = "tenant_cache", indexes = {
    @Index(name = "idx_tenant_cache_slug", columnList = "slug"),
    @Index(name = "idx_tenant_cache_type", columnList = "type"),
    @Index(name = "idx_tenant_cache_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TenantCache {

    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private UUID id; // Same ID as in Tenant Service

    @NotNull
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Column(name = "slug", unique = true, nullable = false, length = 100)
    @EqualsAndHashCode.Include
    private String slug;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private TenantType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private TenantStatus status = TenantStatus.TRIAL;

    // Subscription Plan - matches TenantCacheDTO structure
    @Column(name = "subscription_plan", length = 50)
    private String subscriptionPlan;

    // Cache Sync and Audit Fields - matches TenantCacheDTO structure
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt; // When this cache entry was last updated from Kafka

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Soft delete timestamp from Tenant Service

    // Local Cache Management
    @CreatedDate
    @Column(name = "cache_created_at", nullable = false, updatable = false)
    private LocalDateTime cacheCreatedAt; // When cache entry was created locally

    @LastModifiedDate
    @Column(name = "cache_updated_at")
    private LocalDateTime cacheUpdatedAt; // When cache entry was last updated locally

    /**
     * Check if tenant is soft deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if tenant is active
     */
    public boolean isActive() {
        return status == TenantStatus.ACTIVE && !isDeleted();
    }

    /**
     * Check if tenant is operational (ACTIVE or TRIAL)
     */
    public boolean isOperational() {
        return (status == TenantStatus.ACTIVE || status == TenantStatus.TRIAL) && !isDeleted();
    }

    /**
     * Mark as synced
     */
    public void markSynced() {
        this.lastSyncedAt = LocalDateTime.now();
    }

    /**
     * Check if cache is stale (older than specified minutes)
     */
    public boolean isStale(int minutes) {
        if (lastSyncedAt == null) {
            return true;
        }
        return lastSyncedAt.plusMinutes(minutes).isBefore(LocalDateTime.now());
    }
}
