package com.modernreservation.tenantservice.service;

import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import com.modernreservation.tenantservice.dto.CreateTenantRequest;
import com.modernreservation.tenantservice.dto.TenantResponse;
import com.modernreservation.tenantservice.dto.UpdateTenantRequest;
import com.modernreservation.tenantservice.entity.Tenant;
import com.modernreservation.tenantservice.kafka.TenantEventPublisher;
import com.modernreservation.tenantservice.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tenant Service - Business logic for tenant management
 *
 * Provides comprehensive tenant operations including:
 * - CRUD operations
 * - Status management (suspend, activate, expire)
 * - Subscription management
 * - Search and filtering
 * - Kafka event publishing
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantEventPublisher eventPublisher;

    // =========================================================================
    // CREATE OPERATIONS
    // =========================================================================

    /**
     * Create a new tenant
     *
     * @param request Create tenant request
     * @return Created tenant response
     */
    @CacheEvict(value = {"tenants", "tenant-slugs"}, allEntries = true)
    public TenantResponse createTenant(CreateTenantRequest request) {
        log.info("Creating new tenant: {}", request.getName());

        // Validate slug uniqueness
        if (tenantRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Slug already exists: " + request.getSlug());
        }

        // Validate email uniqueness
        if (tenantRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Build subscription map
        Map<String, Object> subscriptionMap = buildSubscriptionMap(request.getSubscription());

        // Create tenant entity
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .type(request.getType())
                .status(TenantStatus.TRIAL) // New tenants start in TRIAL
                .email(request.getEmail())
                .phone(request.getPhone())
                .website(request.getWebsite())
                .address(request.getAddress())
                .businessInfo(request.getBusinessInfo())
                .config(request.getConfig() != null ? request.getConfig() : getDefaultConfig())
                .subscription(subscriptionMap)
                .metadata(request.getMetadata())
                .build();

        // Save tenant
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant created successfully: {} (ID: {})", savedTenant.getName(), savedTenant.getId());

        // Publish Kafka event
        eventPublisher.publishTenantCreated(savedTenant);

        return mapToResponse(savedTenant);
    }

    // =========================================================================
    // READ OPERATIONS
    // =========================================================================

    /**
     * Get tenant by ID
     */
    @Cacheable(value = "tenants", key = "#id")
    @Transactional(readOnly = true)
    public TenantResponse getTenantById(UUID id) {
        log.debug("Fetching tenant by ID: {}", id);
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        return mapToResponse(tenant);
    }

    /**
     * Get tenant by slug
     */
    @Cacheable(value = "tenant-slugs", key = "#slug")
    @Transactional(readOnly = true)
    public TenantResponse getTenantBySlug(String slug) {
        log.debug("Fetching tenant by slug: {}", slug);
        Tenant tenant = tenantRepository.findBySlugAndNotDeleted(slug)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + slug));
        return mapToResponse(tenant);
    }

    /**
     * Get all active tenants with pagination
     */
    @Transactional(readOnly = true)
    public Page<TenantResponse> getAllTenants(Pageable pageable) {
        log.debug("Fetching all active tenants with pagination");
        Page<Tenant> tenants = tenantRepository.findAllActive(pageable);
        return tenants.map(this::mapToResponse);
    }

    /**
     * Search tenants
     */
    @Transactional(readOnly = true)
    public Page<TenantResponse> searchTenants(
            TenantType type,
            TenantStatus status,
            String search,
            Pageable pageable
    ) {
        log.debug("Searching tenants: type={}, status={}, search={}", type, status, search);
        Page<Tenant> tenants = tenantRepository.advancedSearch(type, status, search, pageable);
        return tenants.map(this::mapToResponse);
    }

    /**
     * Get tenants by status
     */
    @Transactional(readOnly = true)
    public Page<TenantResponse> getTenantsByStatus(TenantStatus status, Pageable pageable) {
        log.debug("Fetching tenants by status: {}", status);
        Page<Tenant> tenants = tenantRepository.findByStatus(status, pageable);
        return tenants.map(this::mapToResponse);
    }

    /**
     * Get tenants by type
     */
    @Transactional(readOnly = true)
    public Page<TenantResponse> getTenantsByType(TenantType type, Pageable pageable) {
        log.debug("Fetching tenants by type: {}", type);
        Page<Tenant> tenants = tenantRepository.findByType(type, pageable);
        return tenants.map(this::mapToResponse);
    }

    /**
     * Check if slug is available
     */
    @Transactional(readOnly = true)
    public boolean isSlugAvailable(String slug) {
        return !tenantRepository.existsBySlug(slug);
    }

    /**
     * Check if slug is available (excluding specific tenant)
     */
    @Transactional(readOnly = true)
    public boolean isSlugAvailable(String slug, UUID excludeTenantId) {
        return !tenantRepository.existsBySlugAndIdNot(slug, excludeTenantId);
    }

    // =========================================================================
    // UPDATE OPERATIONS
    // =========================================================================

    /**
     * Update tenant
     */
    @CacheEvict(value = {"tenants", "tenant-slugs"}, allEntries = true)
    public TenantResponse updateTenant(UUID id, UpdateTenantRequest request) {
        log.info("Updating tenant: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        // Validate slug uniqueness if changed
        if (request.getSlug() != null && !request.getSlug().equals(tenant.getSlug())) {
            if (tenantRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new IllegalArgumentException("Slug already exists: " + request.getSlug());
            }
            tenant.setSlug(request.getSlug());
        }

        // Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(tenant.getEmail())) {
            if (tenantRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            tenant.setEmail(request.getEmail());
        }

        // Update fields
        if (request.getName() != null) tenant.setName(request.getName());
        if (request.getPhone() != null) tenant.setPhone(request.getPhone());
        if (request.getWebsite() != null) tenant.setWebsite(request.getWebsite());
        if (request.getAddress() != null) tenant.setAddress(request.getAddress());
        if (request.getBusinessInfo() != null) tenant.setBusinessInfo(request.getBusinessInfo());
        if (request.getConfig() != null) tenant.setConfig(request.getConfig());
        if (request.getMetadata() != null) tenant.setMetadata(request.getMetadata());

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("Tenant updated successfully: {} (ID: {})", updatedTenant.getName(), updatedTenant.getId());

        // Publish Kafka event
        eventPublisher.publishTenantUpdated(updatedTenant);

        return mapToResponse(updatedTenant);
    }

    // =========================================================================
    // STATUS MANAGEMENT
    // =========================================================================

    /**
     * Update tenant status
     */
    @CacheEvict(value = {"tenants", "tenant-slugs"}, allEntries = true)
    public TenantResponse updateTenantStatus(UUID id, TenantStatus newStatus) {
        log.info("Updating tenant status: {} to {}", id, newStatus);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        TenantStatus oldStatus = tenant.getStatus();
        tenant.setStatus(newStatus);
        Tenant updatedTenant = tenantRepository.save(tenant);

        log.info("Tenant status updated: {} -> {} (ID: {})", oldStatus, newStatus, id);

        // Publish appropriate Kafka event based on status change
        publishStatusChangeEvent(updatedTenant, oldStatus, newStatus);

        return mapToResponse(updatedTenant);
    }

    /**
     * Suspend tenant
     */
    @CacheEvict(value = {"tenants", "tenant-slugs"}, allEntries = true)
    public TenantResponse suspendTenant(UUID id) {
        log.info("Suspending tenant: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        tenant.suspend();
        Tenant updatedTenant = tenantRepository.save(tenant);

        log.info("Tenant suspended: {} (ID: {})", updatedTenant.getName(), id);
        eventPublisher.publishTenantSuspended(updatedTenant);

        return mapToResponse(updatedTenant);
    }

    /**
     * Activate tenant
     */
    @CacheEvict(value = {"tenants", "tenant-slugs"}, allEntries = true)
    public TenantResponse activateTenant(UUID id) {
        log.info("Activating tenant: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        tenant.activate();
        Tenant updatedTenant = tenantRepository.save(tenant);

        log.info("Tenant activated: {} (ID: {})", updatedTenant.getName(), id);
        eventPublisher.publishTenantActivated(updatedTenant);

        return mapToResponse(updatedTenant);
    }

    /**
     * Expire tenant
     */
    @CacheEvict(value = {"tenants", "tenant-slugs"}, allEntries = true)
    public TenantResponse expireTenant(UUID id) {
        log.info("Expiring tenant: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        tenant.expire();
        Tenant updatedTenant = tenantRepository.save(tenant);

        log.info("Tenant expired: {} (ID: {})", updatedTenant.getName(), id);
        eventPublisher.publishTenantExpired(updatedTenant);

        return mapToResponse(updatedTenant);
    }

    // =========================================================================
    // DELETE OPERATIONS
    // =========================================================================

    /**
     * Soft delete tenant
     */
    @CacheEvict(value = {"tenants", "tenant-slugs"}, allEntries = true)
    public TenantResponse deleteTenant(UUID id) {
        log.info("Soft deleting tenant: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        tenant.softDelete();
        Tenant deletedTenant = tenantRepository.save(tenant);

        log.info("Tenant soft deleted: {} (ID: {})", deletedTenant.getName(), id);
        eventPublisher.publishTenantDeleted(deletedTenant);

        return mapToResponse(deletedTenant);
    }

    /**
     * Restore soft-deleted tenant
     */
    @CacheEvict(value = {"tenants", "tenant-slugs"}, allEntries = true)
    public TenantResponse restoreTenant(UUID id) {
        log.info("Restoring tenant: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));

        if (!tenant.isDeleted()) {
            throw new IllegalStateException("Tenant is not deleted: " + id);
        }

        tenant.restore();
        Tenant restoredTenant = tenantRepository.save(tenant);

        log.info("Tenant restored: {} (ID: {})", restoredTenant.getName(), id);
        eventPublisher.publishTenantActivated(restoredTenant);

        return mapToResponse(restoredTenant);
    }

    // =========================================================================
    // SUBSCRIPTION OPERATIONS
    // =========================================================================

    /**
     * Process expiring trials
     * Should be called by scheduled job
     */
    public void processExpiringTrials() {
        log.info("Processing expiring trials");
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(3); // 3 days notice
        List<Tenant> expiringTrials = tenantRepository.findExpiringTrials(expiryDate);

        log.info("Found {} expiring trials", expiringTrials.size());
        // Implementation for sending notifications, etc.
    }

    /**
     * Process expiring subscriptions
     * Should be called by scheduled job
     */
    public void processExpiringSubscriptions() {
        log.info("Processing expiring subscriptions");
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7); // 7 days notice
        List<Tenant> expiringSubscriptions = tenantRepository.findExpiringSubscriptions(expiryDate);

        log.info("Found {} expiring subscriptions", expiringSubscriptions.size());
        // Implementation for sending notifications, processing renewals, etc.
    }

    // =========================================================================
    // STATISTICS
    // =========================================================================

    /**
     * Get tenant statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getTenantStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", tenantRepository.count());
        stats.put("active", tenantRepository.countByStatus(TenantStatus.ACTIVE));
        stats.put("trial", tenantRepository.countByStatus(TenantStatus.TRIAL));
        stats.put("suspended", tenantRepository.countByStatus(TenantStatus.SUSPENDED));
        stats.put("expired", tenantRepository.countByStatus(TenantStatus.EXPIRED));
        stats.put("cancelled", tenantRepository.countByStatus(TenantStatus.CANCELLED));
        return stats;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Map Tenant entity to TenantResponse DTO
     */
    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .type(tenant.getType())
                .status(tenant.getStatus())
                .email(tenant.getEmail())
                .phone(tenant.getPhone())
                .website(tenant.getWebsite())
                .address(tenant.getAddress())
                .businessInfo(tenant.getBusinessInfo())
                .config(tenant.getConfig())
                .subscription(tenant.getSubscription())
                .metadata(tenant.getMetadata())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .createdBy(tenant.getCreatedBy())
                .updatedBy(tenant.getUpdatedBy())
                .deletedAt(tenant.getDeletedAt())
                .isDeleted(tenant.isDeleted())
                .isActive(tenant.isActive())
                .subscriptionPlan(tenant.getSubscriptionPlan())
                .build();
    }

    /**
     * Build subscription map from request
     */
    private Map<String, Object> buildSubscriptionMap(CreateTenantRequest.SubscriptionRequest subscription) {
        Map<String, Object> subscriptionMap = new HashMap<>();
        subscriptionMap.put("plan", subscription.getPlan().name());
        subscriptionMap.put("billingEmail", subscription.getBillingEmail());
        subscriptionMap.put("billingAddress", subscription.getBillingAddress());
        subscriptionMap.put("autoRenew", subscription.getAutoRenew());
        subscriptionMap.put("startDate", LocalDateTime.now().toString());
        subscriptionMap.put("trialEndsAt", LocalDateTime.now().plusDays(14).toString());
        subscriptionMap.put("isActive", true);

        if (subscription.getPricing() != null) {
            subscriptionMap.put("pricing", subscription.getPricing());
        }

        return subscriptionMap;
    }

    /**
     * Get default configuration
     */
    private Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();

        // Default limits
        Map<String, Object> limits = new HashMap<>();
        limits.put("maxProperties", 5);
        limits.put("maxUsers", 10);
        limits.put("maxReservationsPerMonth", 1000);
        config.put("limits", limits);

        // Default features
        Map<String, Object> features = new HashMap<>();
        features.put("enableMultiProperty", true);
        features.put("enableChannelManager", false);
        features.put("enableAdvancedReporting", false);
        features.put("enablePaymentProcessing", true);
        features.put("enableLoyaltyProgram", false);
        config.put("features", features);

        // Default localization
        Map<String, Object> localization = new HashMap<>();
        localization.put("defaultCurrency", "USD");
        localization.put("defaultLanguage", "en");
        localization.put("defaultTimezone", "America/New_York");
        config.put("localization", localization);

        return config;
    }

    /**
     * Publish appropriate Kafka event based on status change
     */
    private void publishStatusChangeEvent(Tenant tenant, TenantStatus oldStatus, TenantStatus newStatus) {
        if (newStatus == TenantStatus.SUSPENDED) {
            eventPublisher.publishTenantSuspended(tenant);
        } else if (newStatus == TenantStatus.ACTIVE && oldStatus == TenantStatus.SUSPENDED) {
            eventPublisher.publishTenantActivated(tenant);
        } else if (newStatus == TenantStatus.EXPIRED) {
            eventPublisher.publishTenantExpired(tenant);
        } else {
            eventPublisher.publishTenantUpdated(tenant);
        }
    }
}
