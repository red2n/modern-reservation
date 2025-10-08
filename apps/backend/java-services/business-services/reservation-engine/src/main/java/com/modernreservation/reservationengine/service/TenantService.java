package com.modernreservation.reservationengine.service;

import com.modernreservation.reservationengine.context.TenantContext;
import com.modernreservation.reservationengine.entity.Tenant;
import com.modernreservation.reservationengine.enums.TenantStatus;
import com.modernreservation.reservationengine.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
 * TenantService - Business logic for tenant management
 *
 * Handles tenant operations including validation, status checks,
 * and tenant lifecycle management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;

    /**
     * Get tenant by ID
     */
    @Cacheable(value = "tenants", key = "#tenantId")
    @Transactional(readOnly = true)
    public Optional<Tenant> getTenant(UUID tenantId) {
        log.debug("Fetching tenant: {}", tenantId);
        return tenantRepository.findById(tenantId);
    }

    /**
     * Get tenant by slug
     */
    @Cacheable(value = "tenants", key = "'slug:' + #slug")
    @Transactional(readOnly = true)
    public Optional<Tenant> getTenantBySlug(String slug) {
        log.debug("Fetching tenant by slug: {}", slug);
        return tenantRepository.findBySlug(slug);
    }

    /**
     * Get current tenant from context
     */
    @Cacheable(value = "tenants", key = "#root.method.name + ':' + T(com.modernreservation.reservationengine.context.TenantContext).getCurrentTenantId()")
    @Transactional(readOnly = true)
    public Tenant getCurrentTenant() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching current tenant from context: {}", tenantId);

        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException(
                "Tenant not found for ID: " + tenantId
            ));
    }

    /**
     * Check if tenant is active (not deleted, status = ACTIVE)
     */
    @Transactional(readOnly = true)
    public boolean isTenantActive(UUID tenantId) {
        return tenantRepository.isActiveTenant(tenantId);
    }

    /**
     * Validate current tenant is active
     *
     * @throws IllegalStateException if tenant is not active
     */
    @Transactional(readOnly = true)
    public void validateCurrentTenantActive() {
        UUID tenantId = TenantContext.getCurrentTenantId();

        if (!isTenantActive(tenantId)) {
            log.error("Inactive tenant attempted operation: {}", tenantId);
            throw new IllegalStateException(
                "Tenant account is not active. Please contact support."
            );
        }
    }

    /**
     * Get all active tenants
     */
    @Cacheable(value = "tenants", key = "'all-active'")
    @Transactional(readOnly = true)
    public List<Tenant> getAllActiveTenants() {
        log.debug("Fetching all active tenants");
        return tenantRepository.findAllActive();
    }

    /**
     * Get all active tenants (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Tenant> getAllActiveTenants(Pageable pageable) {
        log.debug("Fetching active tenants (paginated)");
        return tenantRepository.findAllActive(pageable);
    }

    /**
     * Get tenants by status
     */
    @Cacheable(value = "tenants", key = "'status:' + #status")
    @Transactional(readOnly = true)
    public List<Tenant> getTenantsByStatus(TenantStatus status) {
        log.debug("Fetching tenants by status: {}", status);
        return tenantRepository.findByStatus(status);
    }

    /**
     * Check if slug is available
     */
    @Transactional(readOnly = true)
    public boolean isSlugAvailable(String slug) {
        return !tenantRepository.existsBySlug(slug);
    }

    /**
     * Check if email is available
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !tenantRepository.existsByEmail(email);
    }

    /**
     * Create new tenant
     */
    @CacheEvict(value = "tenants", allEntries = true)
    public Tenant createTenant(Tenant tenant) {
        log.info("Creating new tenant: {}", tenant.getName());

        // Validate slug uniqueness
        if (!isSlugAvailable(tenant.getSlug())) {
            throw new IllegalArgumentException("Slug already exists: " + tenant.getSlug());
        }

        // Validate email uniqueness
        if (!isEmailAvailable(tenant.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + tenant.getEmail());
        }

        // Set defaults
        if (tenant.getStatus() == null) {
            tenant.setStatus(TenantStatus.TRIAL);
        }

        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant created successfully: {} (ID: {})", savedTenant.getName(), savedTenant.getId());

        return savedTenant;
    }

    /**
     * Update tenant
     */
    @CacheEvict(value = "tenants", allEntries = true)
    public Tenant updateTenant(UUID tenantId, Tenant updatedTenant) {
        log.info("Updating tenant: {}", tenantId);

        Tenant existingTenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        // Validate slug uniqueness (if changed)
        if (!existingTenant.getSlug().equals(updatedTenant.getSlug())
            && !isSlugAvailable(updatedTenant.getSlug())) {
            throw new IllegalArgumentException("Slug already exists: " + updatedTenant.getSlug());
        }

        // Validate email uniqueness (if changed)
        if (!existingTenant.getEmail().equals(updatedTenant.getEmail())
            && !isEmailAvailable(updatedTenant.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + updatedTenant.getEmail());
        }

        // Update fields
        existingTenant.setName(updatedTenant.getName());
        existingTenant.setSlug(updatedTenant.getSlug());
        existingTenant.setType(updatedTenant.getType());
        existingTenant.setStatus(updatedTenant.getStatus());
        existingTenant.setEmail(updatedTenant.getEmail());
        existingTenant.setPhone(updatedTenant.getPhone());
        existingTenant.setAddress(updatedTenant.getAddress());
        existingTenant.setBusinessInfo(updatedTenant.getBusinessInfo());
        existingTenant.setConfig(updatedTenant.getConfig());
        existingTenant.setSubscription(updatedTenant.getSubscription());
        existingTenant.setMetadata(updatedTenant.getMetadata());

        Tenant savedTenant = tenantRepository.save(existingTenant);
        log.info("Tenant updated successfully: {}", tenantId);

        return savedTenant;
    }

    /**
     * Soft delete tenant
     */
    @CacheEvict(value = "tenants", allEntries = true)
    public void softDeleteTenant(UUID tenantId, String deletedBy) {
        log.info("Soft deleting tenant: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        if (tenant.isDeleted()) {
            log.warn("Tenant already deleted: {}", tenantId);
            return;
        }

        tenant.softDelete(deletedBy);
        tenantRepository.save(tenant);

        log.info("Tenant soft deleted successfully: {}", tenantId);
    }

    /**
     * Activate tenant (change status to ACTIVE)
     */
    @CacheEvict(value = "tenants", allEntries = true)
    public void activateTenant(UUID tenantId) {
        log.info("Activating tenant: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        if (tenant.isDeleted()) {
            throw new IllegalStateException("Cannot activate deleted tenant: " + tenantId);
        }

        tenant.setStatus(TenantStatus.ACTIVE);
        tenantRepository.save(tenant);

        log.info("Tenant activated successfully: {}", tenantId);
    }

    /**
     * Suspend tenant (change status to SUSPENDED)
     */
    @CacheEvict(value = "tenants", allEntries = true)
    public void suspendTenant(UUID tenantId, String reason) {
        log.info("Suspending tenant: {} (reason: {})", tenantId, reason);

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        if (tenant.isDeleted()) {
            throw new IllegalStateException("Cannot suspend deleted tenant: " + tenantId);
        }

        tenant.setStatus(TenantStatus.SUSPENDED);

        // Store suspension reason in metadata
        if (tenant.getMetadata() != null) {
            tenant.getMetadata().put("suspensionReason", reason);
            tenant.getMetadata().put("suspensionDate", LocalDateTime.now().toString());
        }

        tenantRepository.save(tenant);

        log.info("Tenant suspended successfully: {}", tenantId);
    }

    /**
     * Search tenants
     */
    @Transactional(readOnly = true)
    public Page<Tenant> searchTenants(String searchTerm, Pageable pageable) {
        log.debug("Searching tenants: {}", searchTerm);
        return tenantRepository.searchTenants(searchTerm, pageable);
    }
}
