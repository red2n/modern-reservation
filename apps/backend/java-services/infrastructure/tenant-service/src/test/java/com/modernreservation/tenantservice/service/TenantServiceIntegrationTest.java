package com.modernreservation.tenantservice.service;

import com.modernreservation.tenant.commons.enums.SubscriptionPlan;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import com.modernreservation.tenantservice.dto.CreateTenantRequest;
import com.modernreservation.tenantservice.dto.TenantResponse;
import com.modernreservation.tenantservice.dto.UpdateTenantRequest;
import com.modernreservation.tenantservice.entity.Tenant;
import com.modernreservation.tenantservice.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for TenantService
 *
 * Tests comprehensive tenant operations including:
 * - CRUD operations
 * - Status management
 * - Search and filtering
 * - Validation logic
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TenantServiceIntegrationTest {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        tenantRepository.deleteAll();
    }

    // =========================================================================
    // CREATE TESTS
    // =========================================================================

    @Test
    void testCreateTenant_Success() {
        // Given
        CreateTenantRequest request = buildCreateTenantRequest(
                "Grand Hotel", "grand-hotel", TenantType.HOTEL
        );

        // When
        TenantResponse response = tenantService.createTenant(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Grand Hotel");
        assertThat(response.getSlug()).isEqualTo("grand-hotel");
        assertThat(response.getType()).isEqualTo(TenantType.HOTEL);
        assertThat(response.getStatus()).isEqualTo(TenantStatus.TRIAL);
        assertThat(response.getEmail()).isEqualTo("admin@grand-hotel.com");
        assertThat(response.isActive()).isFalse(); // TRIAL is not active
        assertThat(response.isDeleted()).isFalse();
    }

    @Test
    void testCreateTenant_DuplicateSlug_ThrowsException() {
        // Given
        CreateTenantRequest request1 = buildCreateTenantRequest(
                "Grand Hotel", "grand-hotel", TenantType.HOTEL
        );
        tenantService.createTenant(request1);

        CreateTenantRequest request2 = buildCreateTenantRequest(
                "Another Hotel", "grand-hotel", TenantType.HOTEL
        );

        // When & Then
        assertThatThrownBy(() -> tenantService.createTenant(request2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Slug already exists");
    }

    @Test
    void testCreateTenant_DuplicateEmail_ThrowsException() {
        // Given
        CreateTenantRequest request1 = buildCreateTenantRequest(
                "Grand Hotel", "grand-hotel", TenantType.HOTEL
        );
        tenantService.createTenant(request1);

        CreateTenantRequest request2 = buildCreateTenantRequest(
                "Another Hotel", "another-hotel", TenantType.HOTEL
        );
        request2.setEmail("admin@grand-hotel.com"); // Same email

        // When & Then
        assertThatThrownBy(() -> tenantService.createTenant(request2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
    }

    // =========================================================================
    // READ TESTS
    // =========================================================================

    @Test
    void testGetTenantById_Success() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );

        // When
        TenantResponse response = tenantService.getTenantById(created.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(created.getId());
        assertThat(response.getName()).isEqualTo("Grand Hotel");
    }

    @Test
    void testGetTenantById_NotFound_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> tenantService.getTenantById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    void testGetTenantBySlug_Success() {
        // Given
        tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );

        // When
        TenantResponse response = tenantService.getTenantBySlug("grand-hotel");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSlug()).isEqualTo("grand-hotel");
    }

    @Test
    void testGetAllTenants_WithPagination() {
        // Given
        tenantService.createTenant(buildCreateTenantRequest("Hotel 1", "hotel-1", TenantType.HOTEL));
        tenantService.createTenant(buildCreateTenantRequest("Hotel 2", "hotel-2", TenantType.HOTEL));
        tenantService.createTenant(buildCreateTenantRequest("Hotel 3", "hotel-3", TenantType.HOTEL));

        // When
        Page<TenantResponse> page = tenantService.getAllTenants(PageRequest.of(0, 2));

        // Then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testSearchTenants_ByType() {
        // Given
        tenantService.createTenant(buildCreateTenantRequest("Hotel 1", "hotel-1", TenantType.HOTEL));
        tenantService.createTenant(buildCreateTenantRequest("Hostel 1", "hostel-1", TenantType.HOSTEL));

        // When
        Page<TenantResponse> hotels = tenantService.searchTenants(
                TenantType.HOTEL, null, null, PageRequest.of(0, 10)
        );

        // Then
        assertThat(hotels.getContent()).hasSize(1);
        assertThat(hotels.getContent().get(0).getType()).isEqualTo(TenantType.HOTEL);
    }

    @Test
    void testSearchTenants_ByStatus() {
        // Given
        TenantResponse tenant1 = tenantService.createTenant(
                buildCreateTenantRequest("Hotel 1", "hotel-1", TenantType.HOTEL)
        );
        tenantService.activateTenant(tenant1.getId());

        tenantService.createTenant(buildCreateTenantRequest("Hotel 2", "hotel-2", TenantType.HOTEL));

        // When
        Page<TenantResponse> activeTenantsPage = tenantService.searchTenants(
                null, TenantStatus.ACTIVE, null, PageRequest.of(0, 10)
        );

        // Then
        assertThat(activeTenantsPage.getContent()).hasSize(1);
        assertThat(activeTenantsPage.getContent().get(0).getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void testSearchTenants_BySearchTerm() {
        // Given
        tenantService.createTenant(buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL));
        tenantService.createTenant(buildCreateTenantRequest("Small Hostel", "small-hostel", TenantType.HOSTEL));

        // When
        Page<TenantResponse> results = tenantService.searchTenants(
                null, null, "Grand", PageRequest.of(0, 10)
        );

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).contains("Grand");
    }

    // =========================================================================
    // UPDATE TESTS
    // =========================================================================

    @Test
    void testUpdateTenant_Success() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );

        UpdateTenantRequest updateRequest = UpdateTenantRequest.builder()
                .name("Grand Hotel Updated")
                .phone("+1-555-9999")
                .build();

        // When
        TenantResponse updated = tenantService.updateTenant(created.getId(), updateRequest);

        // Then
        assertThat(updated.getName()).isEqualTo("Grand Hotel Updated");
        assertThat(updated.getPhone()).isEqualTo("+1-555-9999");
        assertThat(updated.getSlug()).isEqualTo("grand-hotel"); // Unchanged
    }

    @Test
    void testUpdateTenant_ChangeSlug_Success() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );

        UpdateTenantRequest updateRequest = UpdateTenantRequest.builder()
                .slug("grand-hotel-new")
                .build();

        // When
        TenantResponse updated = tenantService.updateTenant(created.getId(), updateRequest);

        // Then
        assertThat(updated.getSlug()).isEqualTo("grand-hotel-new");
    }

    @Test
    void testUpdateTenant_DuplicateSlug_ThrowsException() {
        // Given
        tenantService.createTenant(buildCreateTenantRequest("Hotel 1", "hotel-1", TenantType.HOTEL));
        TenantResponse tenant2 = tenantService.createTenant(
                buildCreateTenantRequest("Hotel 2", "hotel-2", TenantType.HOTEL)
        );

        UpdateTenantRequest updateRequest = UpdateTenantRequest.builder()
                .slug("hotel-1") // Try to use existing slug
                .build();

        // When & Then
        assertThatThrownBy(() -> tenantService.updateTenant(tenant2.getId(), updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Slug already exists");
    }

    // =========================================================================
    // STATUS MANAGEMENT TESTS
    // =========================================================================

    @Test
    void testActivateTenant_Success() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );
        assertThat(created.getStatus()).isEqualTo(TenantStatus.TRIAL);

        // When
        TenantResponse activated = tenantService.activateTenant(created.getId());

        // Then
        assertThat(activated.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(activated.isActive()).isTrue();
    }

    @Test
    void testSuspendTenant_Success() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );
        tenantService.activateTenant(created.getId());

        // When
        TenantResponse suspended = tenantService.suspendTenant(created.getId());

        // Then
        assertThat(suspended.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
        assertThat(suspended.isActive()).isFalse();
    }

    @Test
    void testExpireTenant_Success() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );

        // When
        TenantResponse expired = tenantService.expireTenant(created.getId());

        // Then
        assertThat(expired.getStatus()).isEqualTo(TenantStatus.EXPIRED);
        assertThat(expired.isActive()).isFalse();
    }

    @Test
    void testUpdateTenantStatus_Success() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );

        // When
        TenantResponse updated = tenantService.updateTenantStatus(created.getId(), TenantStatus.ACTIVE);

        // Then
        assertThat(updated.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    // =========================================================================
    // DELETE/RESTORE TESTS
    // =========================================================================

    @Test
    void testDeleteTenant_SoftDelete() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );

        // When
        TenantResponse deleted = tenantService.deleteTenant(created.getId());

        // Then
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedAt()).isNotNull();

        // Verify still in database
        Tenant tenant = tenantRepository.findById(created.getId()).orElseThrow();
        assertThat(tenant.getDeletedAt()).isNotNull();
    }

    @Test
    void testRestoreTenant_Success() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );
        tenantService.deleteTenant(created.getId());

        // When
        TenantResponse restored = tenantService.restoreTenant(created.getId());

        // Then
        assertThat(restored.isDeleted()).isFalse();
        assertThat(restored.getDeletedAt()).isNull();
        assertThat(restored.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void testRestoreTenant_NotDeleted_ThrowsException() {
        // Given
        TenantResponse created = tenantService.createTenant(
                buildCreateTenantRequest("Grand Hotel", "grand-hotel", TenantType.HOTEL)
        );

        // When & Then
        assertThatThrownBy(() -> tenantService.restoreTenant(created.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not deleted");
    }

    // =========================================================================
    // STATISTICS TESTS
    // =========================================================================

    @Test
    void testGetTenantStatistics_Success() {
        // Given
        TenantResponse tenant1 = tenantService.createTenant(
                buildCreateTenantRequest("Hotel 1", "hotel-1", TenantType.HOTEL)
        );
        tenantService.activateTenant(tenant1.getId());

        tenantService.createTenant(buildCreateTenantRequest("Hotel 2", "hotel-2", TenantType.HOTEL));

        TenantResponse tenant3 = tenantService.createTenant(
                buildCreateTenantRequest("Hotel 3", "hotel-3", TenantType.HOTEL)
        );
        tenantService.suspendTenant(tenant3.getId());

        // When
        Map<String, Long> stats = tenantService.getTenantStatistics();

        // Then
        assertThat(stats.get("total")).isEqualTo(3);
        assertThat(stats.get("active")).isEqualTo(1);
        assertThat(stats.get("trial")).isEqualTo(1);
        assertThat(stats.get("suspended")).isEqualTo(1);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private CreateTenantRequest buildCreateTenantRequest(String name, String slug, TenantType type) {
        Map<String, Object> address = new HashMap<>();
        address.put("street", "123 Main St");
        address.put("city", "New York");
        address.put("state", "NY");
        address.put("zipCode", "10001");
        address.put("country", "USA");

        CreateTenantRequest.SubscriptionRequest subscription = CreateTenantRequest.SubscriptionRequest.builder()
                .plan(SubscriptionPlan.BASIC)
                .billingEmail("billing@" + slug + ".com")
                .autoRenew(true)
                .build();

        return CreateTenantRequest.builder()
                .name(name)
                .slug(slug)
                .type(type)
                .email("admin@" + slug + ".com")
                .phone("+1-555-0000")
                .website("https://" + slug + ".com")
                .address(address)
                .subscription(subscription)
                .build();
    }
}
