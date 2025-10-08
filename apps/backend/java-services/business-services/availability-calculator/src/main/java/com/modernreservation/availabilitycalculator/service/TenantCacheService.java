package com.modernreservation.availabilitycalculator.service;

import com.modernreservation.availabilitycalculator.entity.TenantCache;
import com.modernreservation.availabilitycalculator.repository.TenantCacheRepository;
import com.modernreservation.tenant.commons.dto.TenantCacheDTO;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing tenant cache in Availability Calculator.
 *
 * Responsibilities:
 * - Validate tenant eligibility for availability calculations
 * - Synchronize tenant data from Kafka events
 * - Provide cached tenant information for fast lookups
 * - Maintain consistency between Tenant Service and local cache
 *
 * Caching Strategy:
 * - Level 1: Redis cache (@Cacheable) for sub-millisecond lookups
 * - Level 2: PostgreSQL (source of truth) for durability
 * - Cache invalidation: Automatic on tenant updates via @CacheEvict
 *
 * Data Flow:
 * Kafka Event → handleTenantEvent() → PostgreSQL → @CacheEvict → Next read repopulates Redis
 *
 * @see TenantCache
 * @see TenantCacheRepository
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantCacheService {

    private final TenantCacheRepository repository;

    /**
     * Get tenant by ID with caching.
     * First checks Redis cache, then falls back to PostgreSQL.
     *
     * @param id Tenant UUID
     * @return TenantCache entity
     * @throws EntityNotFoundException if tenant not found or deleted
     */
    @Cacheable(value = "tenants", key = "#id")
    public TenantCache getById(UUID id) {
        log.debug("Fetching tenant from cache: {}", id);
        return repository.findById(id)
                .filter(tenant -> tenant.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + id));
    }

    /**
     * Get tenant by slug with caching.
     *
     * @param slug Tenant slug (unique identifier)
     * @return TenantCache entity
     * @throws EntityNotFoundException if tenant not found
     */
    @Cacheable(value = "tenants", key = "#slug")
    public TenantCache getBySlug(String slug) {
        log.debug("Fetching tenant by slug: {}", slug);
        return repository.findBySlugAndNotDeleted(slug)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + slug));
    }

    /**
     * Validate that tenant can perform availability calculations.
     * Throws exception if tenant is not eligible.
     *
     * @param tenantId Tenant UUID
     * @throws IllegalStateException if tenant cannot calculate availability
     */
    public void validateTenantCanCalculateAvailability(UUID tenantId) {
        TenantCache tenant = getById(tenantId);

        if (!tenant.canCalculateAvailability()) {
            log.warn("Availability calculation blocked for tenant: {} (status: {})",
                     tenantId, tenant.getStatus());
            throw new IllegalStateException(
                "Tenant cannot calculate availability. Status: " + tenant.getStatus()
            );
        }

        log.debug("Tenant validated for availability calculation: {}", tenantId);
    }

    /**
     * Get all active tenants (ACTIVE or TRIAL status)
     */
    public List<TenantCache> getAllActive() {
        return repository.findAllActive();
    }

    /**
     * Get all operational tenants (not deleted)
     */
    public List<TenantCache> getAllOperational() {
        return repository.findAllOperational();
    }

    /**
     * Get count of tenants eligible for availability calculations
     */
    public long getAvailabilityEligibleCount() {
        return repository.countAvailabilityEligible();
    }

    /**
     * Create or update tenant cache from Tenant Service DTO.
     * Called by TenantEventConsumer when receiving Kafka events.
     *
     * @param dto Tenant cache DTO from Kafka event
     */
    @Transactional
    @CacheEvict(value = "tenants", key = "#dto.tenantId")
    public void saveOrUpdate(TenantCacheDTO dto) {
        log.info("Updating tenant cache: {} ({})", dto.getName(), dto.getTenantId());

        TenantCache tenant = repository.findById(dto.getTenantId())
                .orElse(new TenantCache());

        tenant.setId(dto.getTenantId());
        tenant.setName(dto.getName());
        tenant.setSlug(dto.getSlug());
        tenant.setType(dto.getType());
        tenant.setStatus(dto.getStatus());
        tenant.setSubscriptionPlan(dto.getSubscriptionPlan());
        tenant.setLastSyncedAt(Instant.now());

        if (dto.getDeletedAt() != null) {
            tenant.setDeletedAt(dto.getDeletedAt().toInstant(java.time.ZoneOffset.UTC));
        }

        repository.save(tenant);
        log.debug("Tenant cache updated: {}", dto.getTenantId());
    }

    /**
     * Update tenant status and invalidate cache.
     *
     * @param tenantId Tenant UUID
     * @param newStatus New tenant status
     */
    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public void updateStatus(UUID tenantId, TenantStatus newStatus) {
        TenantCache tenant = repository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + tenantId));

        TenantStatus oldStatus = tenant.getStatus();
        tenant.setStatus(newStatus);
        tenant.setLastSyncedAt(Instant.now());

        repository.save(tenant);

        // Log if availability eligibility changed
        boolean wasEligible = (oldStatus == TenantStatus.ACTIVE || oldStatus == TenantStatus.TRIAL);
        boolean isEligible = (newStatus == TenantStatus.ACTIVE || newStatus == TenantStatus.TRIAL);

        if (wasEligible && !isEligible) {
            log.warn("Tenant {} status changed to {}. Availability calculations now BLOCKED.",
                     tenantId, newStatus);
        } else if (!wasEligible && isEligible) {
            log.info("Tenant {} status changed to {}. Availability calculations now ENABLED.",
                     tenantId, newStatus);
        }

        log.info("Tenant status updated: {} -> {}", oldStatus, newStatus);
    }

    /**
     * Mark tenant as deleted (soft delete).
     *
     * @param tenantId Tenant UUID
     */
    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public void markAsDeleted(UUID tenantId) {
        TenantCache tenant = repository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + tenantId));

        tenant.setDeletedAt(Instant.now());
        tenant.setLastSyncedAt(Instant.now());

        repository.save(tenant);

        log.warn("Tenant {} marked as deleted. Availability calculations BLOCKED.", tenantId);
    }

    /**
     * Convert entity to DTO for external exposure.
     */
    public TenantCacheDTO toDTO(TenantCache tenant) {
        return TenantCacheDTO.builder()
                .tenantId(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .type(tenant.getType())
                .status(tenant.getStatus())
                .subscriptionPlan(tenant.getSubscriptionPlan())
                .build();
    }

    /**
     * Get cache statistics for monitoring.
     */
    public CacheStatistics getStatistics() {
        long total = repository.count();
        long operational = repository.findAllOperational().size();
        long active = repository.findAllActive().size();
        long availabilityEligible = repository.countAvailabilityEligible();

        return new CacheStatistics(total, operational, active, availabilityEligible);
    }

    /**
     * DTO for cache statistics
     */
    public record CacheStatistics(
        long totalTenants,
        long operationalTenants,
        long activeTenants,
        long availabilityEligibleTenants
    ) {}
}
