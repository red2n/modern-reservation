package com.modernreservation.analyticsengine.service;

import com.modernreservation.analyticsengine.entity.TenantCache;
import com.modernreservation.analyticsengine.repository.TenantCacheRepository;
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
 * Service for managing tenant cache in Analytics Engine.
 *
 * Responsibilities:
 * - Validate tenant eligibility for analytics access
 * - Synchronize tenant data from Kafka events
 * - Provide cached tenant information for fast lookups
 * - Filter analytics data by tenant status
 *
 * @see TenantCache
 * @see TenantCacheRepository
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantCacheService {

    private final TenantCacheRepository repository;

    @Cacheable(value = "tenants", key = "#id")
    public TenantCache getById(UUID id) {
        log.debug("Fetching tenant from cache: {}", id);
        return repository.findById(id)
                .filter(tenant -> tenant.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + id));
    }

    @Cacheable(value = "tenants", key = "#slug")
    public TenantCache getBySlug(String slug) {
        log.debug("Fetching tenant by slug: {}", slug);
        return repository.findBySlugAndNotDeleted(slug)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + slug));
    }

    /**
     * Validate that tenant can access analytics.
     */
    public void validateTenantCanAccessAnalytics(UUID tenantId) {
        TenantCache tenant = getById(tenantId);

        if (!tenant.canAccessAnalytics()) {
            log.warn("Analytics access blocked for tenant: {} (status: {})",
                     tenantId, tenant.getStatus());
            throw new IllegalStateException(
                "Tenant cannot access analytics. Status: " + tenant.getStatus()
            );
        }
    }

    public List<TenantCache> getAllActive() {
        return repository.findAllActive();
    }

    public List<TenantCache> getAllOperational() {
        return repository.findAllOperational();
    }

    public long getAnalyticsEligibleCount() {
        return repository.countAnalyticsEligible();
    }

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
    }

    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public void updateStatus(UUID tenantId, TenantStatus newStatus) {
        TenantCache tenant = repository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + tenantId));

        TenantStatus oldStatus = tenant.getStatus();
        tenant.setStatus(newStatus);
        tenant.setLastSyncedAt(Instant.now());

        repository.save(tenant);

        boolean wasEligible = (oldStatus == TenantStatus.ACTIVE || oldStatus == TenantStatus.TRIAL);
        boolean isEligible = (newStatus == TenantStatus.ACTIVE || newStatus == TenantStatus.TRIAL);

        if (wasEligible && !isEligible) {
            log.warn("Tenant {} status changed to {}. Analytics access now BLOCKED.",
                     tenantId, newStatus);
        } else if (!wasEligible && isEligible) {
            log.info("Tenant {} status changed to {}. Analytics access now ENABLED.",
                     tenantId, newStatus);
        }
    }

    @Transactional
    @CacheEvict(value = "tenants", key = "#tenantId")
    public void markAsDeleted(UUID tenantId) {
        TenantCache tenant = repository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + tenantId));

        tenant.setDeletedAt(Instant.now());
        tenant.setLastSyncedAt(Instant.now());

        repository.save(tenant);

        log.warn("Tenant {} marked as deleted. Analytics access BLOCKED.", tenantId);
    }

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

    public CacheStatistics getStatistics() {
        long total = repository.count();
        long operational = repository.findAllOperational().size();
        long active = repository.findAllActive().size();
        long analyticsEligible = repository.countAnalyticsEligible();

        return new CacheStatistics(total, operational, active, analyticsEligible);
    }

    public record CacheStatistics(
        long totalTenants,
        long operationalTenants,
        long activeTenants,
        long analyticsEligibleTenants
    ) {}
}
