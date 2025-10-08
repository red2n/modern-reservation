package com.modernreservation.paymentprocessor.service;

import com.modernreservation.paymentprocessor.entity.TenantCache;
import com.modernreservation.paymentprocessor.repository.TenantCacheRepository;
import com.modernreservation.tenant.commons.dto.TenantCacheDTO;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TenantCacheService - Manages local tenant cache for Payment Processor
 *
 * CRITICAL: This service is essential for financial data security and isolation.
 * All payment operations MUST validate tenant status through this service.
 *
 * This service manages the local read-only tenant cache that is synchronized
 * from the Tenant Service via Kafka events.
 *
 * Responsibilities:
 * - Read tenant data from local cache (fast!)
 * - Validate tenant status and payment eligibility
 * - Provide tenant information to payment processing logic
 * - SECURITY: Prevent payments for suspended/cancelled tenants
 *
 * DO NOT use this service to:
 * - Create/update/delete tenants (use Tenant Service REST API)
 * - Modify tenant data directly (updates via Kafka only)
 *
 * Cache Updates:
 * - Handled by TenantEventConsumer (Kafka listener)
 * - Automatic synchronization with master Tenant Service
 * - Eventual consistency model
 *
 * Financial Security:
 * - All payment transactions MUST validate tenant eligibility
 * - Only ACTIVE or TRIAL tenants can process payments
 * - Suspended/Expired/Cancelled tenants are blocked
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TenantCacheService {

    private final TenantCacheRepository tenantCacheRepository;

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
     * Get all operational tenants (ACTIVE or TRIAL)
     */
    public List<TenantCache> getAllOperationalTenants() {
        log.debug("Fetching all operational tenants from cache");
        return tenantCacheRepository.findAllOperational();
    }

    /**
     * CRITICAL: Validate tenant can process payments
     * Throws exception if tenant is not eligible for payment processing
     *
     * @param tenantId The tenant UUID to validate
     * @throws IllegalStateException if tenant cannot process payments
     * @throws IllegalArgumentException if tenant not found
     */
    public void validateTenantCanProcessPayments(UUID tenantId) {
        log.debug("Validating payment eligibility for tenant: {}", tenantId);

        TenantCache tenant = getTenantByIdOrThrow(tenantId);

        if (!tenant.canProcessPayments()) {
            String message = String.format(
                "SECURITY VIOLATION: Tenant %s cannot process payments (status: %s, deleted: %s)",
                tenantId,
                tenant.getStatus(),
                tenant.isDeleted()
            );
            log.error(message);
            throw new IllegalStateException(message);
        }

        log.debug("Tenant {} validated for payment processing (status: {})",
                  tenantId, tenant.getStatus());
    }

    /**
     * Validate tenant exists and is operational
     */
    public void validateTenantOperational(UUID tenantId) {
        TenantCache tenant = getTenantByIdOrThrow(tenantId);
        if (!tenant.isOperational()) {
            throw new IllegalStateException(
                    "Tenant is not operational: " + tenantId + " (status: " + tenant.getStatus() + ")"
            );
        }
    }

    /**
     * Check if slug is available
     */
    public boolean isSlugAvailable(String slug) {
        return !tenantCacheRepository.existsBySlug(slug);
    }

    /**
     * Get count of payment-eligible tenants (for monitoring)
     */
    public long getPaymentEligibleCount() {
        return tenantCacheRepository.countPaymentEligible();
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
     *
     * IMPORTANT: Existing payments are NOT affected, but new payments will be blocked
     */
    @Transactional
    public void markAsDeleted(UUID tenantId, LocalDateTime deletedAt) {
        log.warn("SECURITY: Marking tenant cache as deleted - blocking new payments: {}", tenantId);

        tenantCacheRepository.findById(tenantId).ifPresent(cache -> {
            cache.setDeletedAt(deletedAt);
            cache.setStatus(TenantStatus.CANCELLED);
            cache.markSynced();
            tenantCacheRepository.save(cache);
            log.warn("SECURITY: Tenant cache marked as deleted - new payments blocked: {}", tenantId);
        });
    }

    /**
     * Update tenant status
     * Called by TenantEventConsumer when status change events arrive
     *
     * CRITICAL: Status changes immediately affect payment processing eligibility
     */
    @Transactional
    public void updateStatus(UUID tenantId, TenantStatus newStatus) {
        log.info("Updating tenant cache status: {} to {}", tenantId, newStatus);

        tenantCacheRepository.findById(tenantId).ifPresent(cache -> {
            TenantStatus oldStatus = cache.getStatus();
            cache.setStatus(newStatus);
            cache.markSynced();
            tenantCacheRepository.save(cache);

            // Log security-relevant status changes
            if (oldStatus != newStatus) {
                boolean wasEligible = (oldStatus == TenantStatus.ACTIVE || oldStatus == TenantStatus.TRIAL);
                boolean isEligible = (newStatus == TenantStatus.ACTIVE || newStatus == TenantStatus.TRIAL);

                if (wasEligible && !isEligible) {
                    log.warn("SECURITY: Tenant {} payment eligibility REVOKED (status: {} -> {})",
                             tenantId, oldStatus, newStatus);
                } else if (!wasEligible && isEligible) {
                    log.info("SECURITY: Tenant {} payment eligibility GRANTED (status: {} -> {})",
                             tenantId, oldStatus, newStatus);
                }
            }

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

    /**
     * Get cache statistics including payment eligibility
     */
    public CacheStatistics getCacheStatistics() {
        long totalActive = tenantCacheRepository.countActive();
        long activeCount = tenantCacheRepository.countByStatus(TenantStatus.ACTIVE);
        long trialCount = tenantCacheRepository.countByStatus(TenantStatus.TRIAL);
        long suspendedCount = tenantCacheRepository.countByStatus(TenantStatus.SUSPENDED);
        long expiredCount = tenantCacheRepository.countByStatus(TenantStatus.EXPIRED);
        long paymentEligible = tenantCacheRepository.countPaymentEligible();

        return new CacheStatistics(
                totalActive,
                activeCount,
                trialCount,
                suspendedCount,
                expiredCount,
                paymentEligible
        );
    }

    /**
     * Cache statistics holder with payment eligibility count
     */
    public record CacheStatistics(
            long totalActive,
            long activeCount,
            long trialCount,
            long suspendedCount,
            long expiredCount,
            long paymentEligibleCount  // CRITICAL: Number of tenants that can process payments
    ) {}
}
