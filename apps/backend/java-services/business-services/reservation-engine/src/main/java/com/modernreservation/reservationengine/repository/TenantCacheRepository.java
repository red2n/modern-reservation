package com.modernreservation.reservationengine.repository;

import com.modernreservation.reservationengine.entity.TenantCache;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TenantCache Repository - Data access layer for local tenant cache
 *
 * Provides data access methods for the local tenant cache that is
 * synchronized from the Tenant Service via Kafka events.
 *
 * This is READ-ONLY from the perspective of business logic.
 * Updates happen ONLY via TenantEventConsumer (Kafka listener).
 */
@Repository
public interface TenantCacheRepository extends JpaRepository<TenantCache, UUID> {

    // =========================================================================
    // BASIC FINDERS
    // =========================================================================

    /**
     * Find tenant by slug (excluding deleted)
     */
    @Query("SELECT t FROM TenantCache t WHERE t.slug = :slug AND t.deletedAt IS NULL")
    Optional<TenantCache> findBySlugAndNotDeleted(@Param("slug") String slug);

    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);

    // =========================================================================
    // ACTIVE TENANTS (NOT DELETED)
    // =========================================================================

    /**
     * Find all active tenants (not soft-deleted)
     */
    @Query("SELECT t FROM TenantCache t WHERE t.deletedAt IS NULL")
    List<TenantCache> findAllActive();

    /**
     * Find all active tenants with pagination
     */
    @Query("SELECT t FROM TenantCache t WHERE t.deletedAt IS NULL")
    Page<TenantCache> findAllActive(Pageable pageable);

    // =========================================================================
    // STATUS-BASED QUERIES
    // =========================================================================

    /**
     * Find tenants by status (excluding deleted)
     */
    @Query("SELECT t FROM TenantCache t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<TenantCache> findByStatus(@Param("status") TenantStatus status);

    /**
     * Find tenants by status with pagination
     */
    @Query("SELECT t FROM TenantCache t WHERE t.status = :status AND t.deletedAt IS NULL")
    Page<TenantCache> findByStatus(@Param("status") TenantStatus status, Pageable pageable);

    /**
     * Find all operational tenants (ACTIVE or TRIAL)
     */
    @Query("SELECT t FROM TenantCache t WHERE " +
           "(t.status = 'ACTIVE' OR t.status = 'TRIAL') AND t.deletedAt IS NULL")
    List<TenantCache> findAllOperational();

    /**
     * Find all suspended tenants
     */
    @Query("SELECT t FROM TenantCache t WHERE t.status = 'SUSPENDED' AND t.deletedAt IS NULL")
    List<TenantCache> findAllSuspended();

    /**
     * Find all expired tenants
     */
    @Query("SELECT t FROM TenantCache t WHERE t.status = 'EXPIRED' AND t.deletedAt IS NULL")
    List<TenantCache> findAllExpired();

    // =========================================================================
    // TYPE-BASED QUERIES
    // =========================================================================

    /**
     * Find tenants by type (excluding deleted)
     */
    @Query("SELECT t FROM TenantCache t WHERE t.type = :type AND t.deletedAt IS NULL")
    List<TenantCache> findByType(@Param("type") TenantType type);

    /**
     * Find tenants by type with pagination
     */
    @Query("SELECT t FROM TenantCache t WHERE t.type = :type AND t.deletedAt IS NULL")
    Page<TenantCache> findByType(@Param("type") TenantType type, Pageable pageable);

    // =========================================================================
    // VALIDATION QUERIES
    // =========================================================================

    /**
     * Check if tenant is active (exists and not deleted)
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM TenantCache t WHERE t.id = :tenantId AND t.deletedAt IS NULL")
    boolean existsAndNotDeleted(@Param("tenantId") UUID tenantId);

    /**
     * Check if tenant is active and operational
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM TenantCache t WHERE t.id = :tenantId " +
           "AND (t.status = 'ACTIVE' OR t.status = 'TRIAL') " +
           "AND t.deletedAt IS NULL")
    boolean isOperational(@Param("tenantId") UUID tenantId);

    // =========================================================================
    // SEARCH QUERIES
    // =========================================================================

    /**
     * Search tenants by name, email, or slug
     */
    @Query("SELECT t FROM TenantCache t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND t.deletedAt IS NULL")
    Page<TenantCache> search(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Advanced search with filters
     */
    @Query("SELECT t FROM TenantCache t WHERE " +
           "(:type IS NULL OR t.type = :type) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:searchTerm IS NULL OR " +
           "    LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "    LOWER(t.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND t.deletedAt IS NULL")
    Page<TenantCache> advancedSearch(
        @Param("type") TenantType type,
        @Param("status") TenantStatus status,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // =========================================================================
    // CACHE MANAGEMENT
    // =========================================================================

    /**
     * Find stale cache entries (not synced in X minutes)
     */
    @Query("SELECT t FROM TenantCache t WHERE " +
           "t.lastSyncedAt IS NULL OR t.lastSyncedAt < :threshold")
    List<TenantCache> findStaleCacheEntries(@Param("threshold") LocalDateTime threshold);

    /**
     * Count cache entries
     */
    @Query("SELECT COUNT(t) FROM TenantCache t WHERE t.deletedAt IS NULL")
    long countActive();

    /**
     * Count cache entries by status
     */
    @Query("SELECT COUNT(t) FROM TenantCache t WHERE t.status = :status AND t.deletedAt IS NULL")
    long countByStatus(@Param("status") TenantStatus status);

    /**
     * Count cache entries by type
     */
    @Query("SELECT COUNT(t) FROM TenantCache t WHERE t.type = :type AND t.deletedAt IS NULL")
    long countByType(@Param("type") TenantType type);

    // =========================================================================
    // SOFT DELETE QUERIES
    // =========================================================================

    /**
     * Find all deleted tenants
     */
    @Query("SELECT t FROM TenantCache t WHERE t.deletedAt IS NOT NULL")
    List<TenantCache> findAllDeleted();

    /**
     * Find deleted tenants after specific date
     */
    @Query("SELECT t FROM TenantCache t WHERE t.deletedAt IS NOT NULL AND t.deletedAt > :afterDate")
    List<TenantCache> findDeletedAfter(@Param("afterDate") LocalDateTime afterDate);
}
