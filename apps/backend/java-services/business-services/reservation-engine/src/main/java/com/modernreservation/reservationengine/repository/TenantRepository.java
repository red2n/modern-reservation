package com.modernreservation.reservationengine.repository;

import com.modernreservation.reservationengine.entity.Tenant;
import com.modernreservation.reservationengine.enums.TenantStatus;
import com.modernreservation.reservationengine.enums.TenantType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tenant Repository - Data access layer for tenant operations
 *
 * Provides data access methods for multi-tenancy management including
 * tenant lookup, status checks, and soft-delete handling.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    // Basic finder methods
    Optional<Tenant> findBySlug(String slug);

    Optional<Tenant> findByEmail(String email);

    // Active tenants only (not soft-deleted)
    @Query("SELECT t FROM Tenant t WHERE t.deletedAt IS NULL")
    List<Tenant> findAllActive();

    @Query("SELECT t FROM Tenant t WHERE t.deletedAt IS NULL")
    Page<Tenant> findAllActive(Pageable pageable);

    // Status-based queries
    @Query("SELECT t FROM Tenant t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<Tenant> findByStatus(@Param("status") TenantStatus status);

    @Query("SELECT t FROM Tenant t WHERE t.type = :type AND t.deletedAt IS NULL")
    List<Tenant> findByType(@Param("type") TenantType type);

    // Existence checks
    boolean existsBySlug(String slug);

    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM Tenant t WHERE t.id = :tenantId AND t.deletedAt IS NULL")
    boolean isActiveTenant(@Param("tenantId") UUID tenantId);

    // Subscription checks
    @Query("SELECT t FROM Tenant t WHERE t.status = 'ACTIVE' " +
           "AND jsonb_extract_path_text(CAST(t.subscription AS text), 'endDate') < :currentDate " +
           "AND t.deletedAt IS NULL")
    List<Tenant> findExpiredSubscriptions(@Param("currentDate") String currentDate);

    // Search functionality
    @Query("SELECT t FROM Tenant t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND t.deletedAt IS NULL")
    Page<Tenant> searchTenants(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Soft delete lookup
    @Query("SELECT t FROM Tenant t WHERE t.id = :id")
    Optional<Tenant> findByIdIncludingDeleted(@Param("id") UUID id);
}
