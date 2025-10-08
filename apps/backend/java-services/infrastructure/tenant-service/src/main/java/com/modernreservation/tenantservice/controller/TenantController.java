package com.modernreservation.tenantservice.controller;

import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import com.modernreservation.tenantservice.dto.CreateTenantRequest;
import com.modernreservation.tenantservice.dto.TenantResponse;
import com.modernreservation.tenantservice.dto.UpdateTenantRequest;
import com.modernreservation.tenantservice.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Tenant Controller - REST API endpoints for tenant management
 *
 * Provides comprehensive RESTful API for:
 * - Tenant CRUD operations
 * - Status management
 * - Search and filtering
 * - Statistics
 */
@RestController
@RequestMapping("/api/tenants")
@Slf4j
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    // =========================================================================
    // CREATE ENDPOINTS
    // =========================================================================

    /**
     * Create a new tenant
     *
     * POST /api/tenants
     *
     * @param request Create tenant request
     * @return Created tenant response
     */
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        log.info("REST: Create tenant request: {}", request.getName());
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // READ ENDPOINTS
    // =========================================================================

    /**
     * Get all tenants with pagination and filtering
     *
     * GET /api/tenants
     *
     * Query params:
     * - type: Filter by tenant type (HOTEL, HOSTEL, etc.)
     * - status: Filter by status (ACTIVE, TRIAL, etc.)
     * - search: Search in name, email, slug
     * - page: Page number (0-based)
     * - size: Page size
     * - sort: Sort field and direction (e.g., "name,asc")
     */
    @GetMapping
    public ResponseEntity<Page<TenantResponse>> getAllTenants(
            @RequestParam(required = false) TenantType type,
            @RequestParam(required = false) TenantStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("REST: Get all tenants - type: {}, status: {}, search: {}", type, status, search);

        Page<TenantResponse> tenants;
        if (type != null || status != null || search != null) {
            tenants = tenantService.searchTenants(type, status, search, pageable);
        } else {
            tenants = tenantService.getAllTenants(pageable);
        }

        return ResponseEntity.ok(tenants);
    }

    /**
     * Get tenant by ID
     *
     * GET /api/tenants/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable UUID id) {
        log.info("REST: Get tenant by ID: {}", id);
        TenantResponse tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Get tenant by slug
     *
     * GET /api/tenants/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<TenantResponse> getTenantBySlug(@PathVariable String slug) {
        log.info("REST: Get tenant by slug: {}", slug);
        TenantResponse tenant = tenantService.getTenantBySlug(slug);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Get tenants by status
     *
     * GET /api/tenants/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TenantResponse>> getTenantsByStatus(
            @PathVariable TenantStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("REST: Get tenants by status: {}", status);
        Page<TenantResponse> tenants = tenantService.getTenantsByStatus(status, pageable);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Get tenants by type
     *
     * GET /api/tenants/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<TenantResponse>> getTenantsByType(
            @PathVariable TenantType type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("REST: Get tenants by type: {}", type);
        Page<TenantResponse> tenants = tenantService.getTenantsByType(type, pageable);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Check if slug is available
     *
     * GET /api/tenants/check-slug/{slug}
     */
    @GetMapping("/check-slug/{slug}")
    public ResponseEntity<Map<String, Boolean>> checkSlugAvailability(
            @PathVariable String slug,
            @RequestParam(required = false) UUID excludeTenantId
    ) {
        log.info("REST: Check slug availability: {}", slug);
        boolean available;
        if (excludeTenantId != null) {
            available = tenantService.isSlugAvailable(slug, excludeTenantId);
        } else {
            available = tenantService.isSlugAvailable(slug);
        }
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * Get tenant statistics
     *
     * GET /api/tenants/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getTenantStatistics() {
        log.info("REST: Get tenant statistics");
        Map<String, Long> stats = tenantService.getTenantStatistics();
        return ResponseEntity.ok(stats);
    }

    // =========================================================================
    // UPDATE ENDPOINTS
    // =========================================================================

    /**
     * Update tenant
     *
     * PUT /api/tenants/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        log.info("REST: Update tenant: {}", id);
        TenantResponse tenant = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Update tenant status
     *
     * PATCH /api/tenants/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TenantResponse> updateTenantStatus(
            @PathVariable UUID id,
            @RequestParam TenantStatus status
    ) {
        log.info("REST: Update tenant status: {} to {}", id, status);
        TenantResponse tenant = tenantService.updateTenantStatus(id, status);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Suspend tenant
     *
     * POST /api/tenants/{id}/suspend
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<TenantResponse> suspendTenant(@PathVariable UUID id) {
        log.info("REST: Suspend tenant: {}", id);
        TenantResponse tenant = tenantService.suspendTenant(id);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Activate tenant
     *
     * POST /api/tenants/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<TenantResponse> activateTenant(@PathVariable UUID id) {
        log.info("REST: Activate tenant: {}", id);
        TenantResponse tenant = tenantService.activateTenant(id);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Expire tenant
     *
     * POST /api/tenants/{id}/expire
     */
    @PostMapping("/{id}/expire")
    public ResponseEntity<TenantResponse> expireTenant(@PathVariable UUID id) {
        log.info("REST: Expire tenant: {}", id);
        TenantResponse tenant = tenantService.expireTenant(id);
        return ResponseEntity.ok(tenant);
    }

    // =========================================================================
    // DELETE ENDPOINTS
    // =========================================================================

    /**
     * Soft delete tenant
     *
     * DELETE /api/tenants/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<TenantResponse> deleteTenant(@PathVariable UUID id) {
        log.info("REST: Delete tenant: {}", id);
        TenantResponse tenant = tenantService.deleteTenant(id);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Restore soft-deleted tenant
     *
     * POST /api/tenants/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<TenantResponse> restoreTenant(@PathVariable UUID id) {
        log.info("REST: Restore tenant: {}", id);
        TenantResponse tenant = tenantService.restoreTenant(id);
        return ResponseEntity.ok(tenant);
    }

    // =========================================================================
    // SUBSCRIPTION ENDPOINTS
    // =========================================================================

    /**
     * Trigger processing of expiring trials
     *
     * POST /api/tenants/process-expiring-trials
     * (Should be called by scheduled job or admin)
     */
    @PostMapping("/process-expiring-trials")
    public ResponseEntity<Map<String, String>> processExpiringTrials() {
        log.info("REST: Process expiring trials");
        tenantService.processExpiringTrials();
        return ResponseEntity.ok(Map.of("message", "Expiring trials processed"));
    }

    /**
     * Trigger processing of expiring subscriptions
     *
     * POST /api/tenants/process-expiring-subscriptions
     * (Should be called by scheduled job or admin)
     */
    @PostMapping("/process-expiring-subscriptions")
    public ResponseEntity<Map<String, String>> processExpiringSubscriptions() {
        log.info("REST: Process expiring subscriptions");
        tenantService.processExpiringSubscriptions();
        return ResponseEntity.ok(Map.of("message", "Expiring subscriptions processed"));
    }

    // =========================================================================
    // EXCEPTION HANDLING
    // =========================================================================

    /**
     * Handle IllegalArgumentException (e.g., tenant not found, slug exists)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handle IllegalStateException (e.g., invalid status transition)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Internal server error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error: " + ex.getMessage()));
    }
}
