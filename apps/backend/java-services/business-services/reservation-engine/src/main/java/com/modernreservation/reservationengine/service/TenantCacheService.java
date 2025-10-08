package com.modernreservation.reservationengine.service;

import com.modernreservation.reservationengine.entity.TenantCache;
import com.modernreservation.reservationengine.repository.TenantCacheRepository;
import com.modernreservation.tenant.commons.dto.TenantCacheDTO;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TenantCacheService - Manages local tenant cache
 *
 * This service manages the local read-only tenant cache that is synchronized
 * from the Tenant Service via Kafka events.
 *
 * Responsibilities:
 * - Read tenant data from local cache (fast!)
 * - Validate tenant status and access
 * - Provide tenant information to business logic
 *
 * DO NOT use this service to:
 * - Create/update/delete tenants (use Tenant Service REST API)
 * - Modify tenant data directly (updates via Kafka only)
 *
 * Cache Updates:
 * - Handled by TenantEventConsumer (Kafka listener)
 * - Automatic synchronization with master Tenant Service
 * - Eventual consistency model
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TenantCacheService {

    private final TenantCacheRepository tenantCacheRepository;

    // =========================================================================
    // READ OPERATIONS (LOCAL CACHE)
    // =========================================================================

    /**
     * Get tenant by ID from local cache
     */
    @Cacheable(value = "tenant-cache", key = "#tenantId")
    public Optional<TenantCache> getTenantById(UUID tenantId) {
        log.debug("Fetching tenant from local cache: {}", tenantId);
        return tenantCacheRepository.findById(tenantId);
    }

    /**
     * Get tenant by ID (throws exception if not found)
     */
    @Cacheable(value = "tenant-cache", key = "#tenantId")
    public TenantCache getTenantByIdOrThrow(UUID tenantId) {
        return getTenantById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tenant not found in cache: " + tenantId
                ));
    }

    /**
     * Get tenant by slug from local cache
     */
    @Cacheable(value = "tenant-cache", key = "'slug:' + #slug")
    public Optional<TenantCache> getTenantBySlug(String slug) {
        log.debug("Fetching tenant by slug from local cache: {}", slug);
        return tenantCacheRepository.findBySlugAndNotDeleted(slug);
    }

    /**
     * Get all active tenants
     */
    public List<TenantCache> getAllActiveTenants() {
        log.debug("Fetching all active tenants from cache");
        return tenantCacheRepository.findAllActive();
    }

    /**
     * Get all active tenants with pagination
     */
    public Page<TenantCache> getAllActiveTenants(Pageable pageable) {
        log.debug("Fetching all active tenants with pagination");
        return tenantCacheRepository.findAllActive(pageable);
    }

    // =========================================================================
    // STATUS & TYPE QUERIES
    // =========================================================================

    /**
     * Get tenants by status
     */
    public List<TenantCache> getTenantsByStatus(TenantStatus status) {
        log.debug("Fetching tenants by status from cache: {}", status);
        return tenantCacheRepository.findByStatus(status);
    }

    /**
     * Get tenants by status with pagination
     */
    public Page<TenantCache> getTenantsByStatus(TenantStatus status, Pageable pageable) {
        log.debug("Fetching tenants by status with pagination: {}", status);
        return tenantCacheRepository.findByStatus(status, pageable);
    }

    /**
     * Get tenants by type
     */
    public List<TenantCache> getTenantsByType(TenantType type) {
        log.debug("Fetching tenants by type from cache: {}", type);
        return tenantCacheRepository.findByType(type);
    }

    /**
     * Get tenants by type with pagination
     */
    public Page<TenantCache> getTenantsByType(TenantType type, Pageable pageable) {
        log.debug("Fetching tenants by type with pagination: {}", type);
        return tenantCacheRepository.findByType(type, pageable);
    }

    /**
     * Get all operational tenants (ACTIVE or TRIAL)
     */
    public List<TenantCache> getAllOperationalTenants() {
        log.debug("Fetching all operational tenants from cache");
        return tenantCacheRepository.findAllOperational();
    }

    // =========================================================================
    // VALIDATION OPERATIONS
    // =========================================================================

    /**
     * Check if tenant exists and is not deleted
     */
    public boolean tenantExists(UUID tenantId) {
        return tenantCacheRepository.existsAndNotDeleted(tenantId);
    }

    /**
     * Check if tenant is operational (ACTIVE or TRIAL)
     */
    public boolean isTenantOperational(UUID tenantId) {
        return tenantCacheRepository.isOperational(tenantId);
    }

    /**
     * Validate tenant is operational
     *
     * @throws IllegalStateException if tenant is not operational
     */
    public void validateTenantOperational(UUID tenantId) {
        if (!isTenantOperational(tenantId)) {
            log.error("Non-operational tenant attempted operation: {}", tenantId);
            throw new IllegalStateException(
                    "Tenant account is not operational. Please contact support."
            );
        }
    }

    /**
     * Check if slug is available
     */
    public boolean isSlugAvailable(String slug) {
        return !tenantCacheRepository.existsBySlug(slug);
    }

    // =========================================================================
    // SEARCH OPERATIONS
    // =========================================================================

    /**
     * Search tenants
     */
    public Page<TenantCache> searchTenants(String searchTerm, Pageable pageable) {
        log.debug("Searching tenants in cache: {}", searchTerm);
        return tenantCacheRepository.search(searchTerm, pageable);
    }

    /**
     * Advanced search with filters
     */
    public Page<TenantCache> advancedSearch(
            TenantType type,
            TenantStatus status,
            String searchTerm,
            Pageable pageable
    ) {
        log.debug("Advanced search in cache - type: {}, status: {}, search: {}",
                  type, status, searchTerm);
        return tenantCacheRepository.advancedSearch(type, status, searchTerm, pageable);
    }

    // =========================================================================
    // CACHE MANAGEMENT (INTERNAL - CALLED BY KAFKA CONSUMER)
    // =========================================================================

    /**
     * Update or create tenant cache entry
     * Called by TenantEventConsumer when TENANT_CREATED or TENANT_UPDATED events arrive
     */
    @Transactional
    public void saveOrUpdate(TenantCacheDTO dto) {
        log.info("Updating tenant cache: {} ({})", dto.getName(), dto.getTenantId());

        TenantCache cache = tenantCacheRepository.findById(dto.getTenantId())
                .orElse(new TenantCache());

        // Update all fields from DTO (only fields that exist in TenantCacheDTO)
        cache.setId(dto.getTenantId());
        cache.setName(dto.getName());
        cache.setSlug(dto.getSlug());
        cache.setType(dto.getType());
        cache.setStatus(dto.getStatus());
        cache.setSubscriptionPlan(dto.getSubscriptionPlan() != null ? dto.getSubscriptionPlan().name() : null);
        cache.setDeletedAt(dto.getDeletedAt());
        cache.markSynced();

        tenantCacheRepository.save(cache);
        log.info("Tenant cache updated successfully: {} ({})", cache.getName(), cache.getId());
    }

    /**
     * Delete tenant cache entry (soft delete)
     * Called by TenantEventConsumer when TENANT_DELETED event arrives
     */
    @Transactional
    public void markAsDeleted(UUID tenantId, LocalDateTime deletedAt) {
        log.info("Marking tenant cache as deleted: {}", tenantId);

        tenantCacheRepository.findById(tenantId).ifPresent(cache -> {
            cache.setDeletedAt(deletedAt);
            cache.setStatus(TenantStatus.CANCELLED);
            cache.markSynced();
            tenantCacheRepository.save(cache);
            log.info("Tenant cache marked as deleted: {}", tenantId);
        });
    }

    /**
     * Update tenant status
     * Called by TenantEventConsumer when status change events arrive
     */
    @Transactional
    public void updateStatus(UUID tenantId, TenantStatus newStatus) {
        log.info("Updating tenant cache status: {} to {}", tenantId, newStatus);

        tenantCacheRepository.findById(tenantId).ifPresent(cache -> {
            cache.setStatus(newStatus);
            cache.markSynced();
            tenantCacheRepository.save(cache);
            log.info("Tenant cache status updated: {} to {}", tenantId, newStatus);
        });
    }

    /**
     * Get stale cache entries (for monitoring/cleanup)
     */
    public List<TenantCache> getStaleCacheEntries(int minutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        return tenantCacheRepository.findStaleCacheEntries(threshold);
    }

    // =========================================================================
    // STATISTICS
    // =========================================================================

    /**
     * Get cache statistics
     */
    public CacheStatistics getCacheStatistics() {
        long totalActive = tenantCacheRepository.countActive();
        long activeCount = tenantCacheRepository.countByStatus(TenantStatus.ACTIVE);
        long trialCount = tenantCacheRepository.countByStatus(TenantStatus.TRIAL);
        long suspendedCount = tenantCacheRepository.countByStatus(TenantStatus.SUSPENDED);
        long expiredCount = tenantCacheRepository.countByStatus(TenantStatus.EXPIRED);

        return new CacheStatistics(
                totalActive,
                activeCount,
                trialCount,
                suspendedCount,
                expiredCount
        );
    }

    /**
     * Cache statistics holder
     */
    public record CacheStatistics(
            long totalActive,
            long activeCount,
            long trialCount,
            long suspendedCount,
            long expiredCount
    ) {}
}
