package com.modernreservation.tenantservice.repository;

import com.modernreservation.tenantservice.entity.Tenant;
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
 * Tenant Repository - Data access for tenant operations
 *
 * Provides comprehensive queries for tenant management including:
 * - CRUD operations
 * - Search and filtering
 * - Status management
 * - Subscription queries
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    // =========================================================================
    // BASIC FINDER METHODS
    // =========================================================================

    /**
     * Find tenant by slug (unique identifier)
     */
    Optional<Tenant> findBySlug(String slug);

    /**
     * Find tenant by slug (excluding deleted)
     */
    @Query("SELECT t FROM Tenant t WHERE t.slug = :slug AND t.deletedAt IS NULL")
    Optional<Tenant> findBySlugAndNotDeleted(@Param("slug") String slug);

    /**
     * Find tenant by email
     */
    Optional<Tenant> findByEmail(String email);

    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Check if slug exists (excluding specific tenant ID)
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM Tenant t WHERE t.slug = :slug AND t.id != :excludeId")
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("excludeId") UUID excludeId);

    // =========================================================================
    // FILTERED QUERIES
    // =========================================================================

    /**
     * Find all active tenants (not deleted)
     */
    @Query("SELECT t FROM Tenant t WHERE t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Tenant> findAllActive();

    /**
     * Find all active tenants with pagination
     */
    @Query("SELECT t FROM Tenant t WHERE t.deletedAt IS NULL")
    Page<Tenant> findAllActive(Pageable pageable);

    /**
     * Find tenants by status
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<Tenant> findByStatus(@Param("status") TenantStatus status);

    /**
     * Find tenants by status with pagination
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = :status AND t.deletedAt IS NULL")
    Page<Tenant> findByStatus(@Param("status") TenantStatus status, Pageable pageable);

    /**
     * Find tenants by type
     */
    @Query("SELECT t FROM Tenant t WHERE t.type = :type AND t.deletedAt IS NULL")
    List<Tenant> findByType(@Param("type") TenantType type);

    /**
     * Find tenants by type with pagination
     */
    @Query("SELECT t FROM Tenant t WHERE t.type = :type AND t.deletedAt IS NULL")
    Page<Tenant> findByType(@Param("type") TenantType type, Pageable pageable);

    // =========================================================================
    // SEARCH QUERIES
    // =========================================================================

    /**
     * Search tenants by name or slug
     */
    @Query("SELECT t FROM Tenant t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.slug) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND t.deletedAt IS NULL")
    Page<Tenant> search(@Param("search") String search, Pageable pageable);

    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT t FROM Tenant t WHERE " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR " +
           " LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(t.slug) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(t.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "t.deletedAt IS NULL")
    Page<Tenant> advancedSearch(
        @Param("type") TenantType type,
        @Param("status") TenantStatus status,
        @Param("search") String search,
        Pageable pageable
    );

    // =========================================================================
    // STATUS MANAGEMENT
    // =========================================================================

    /**
     * Find all active (operational) tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'ACTIVE' AND t.deletedAt IS NULL")
    List<Tenant> findAllOperational();

    /**
     * Find all suspended tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'SUSPENDED' AND t.deletedAt IS NULL")
    List<Tenant> findAllSuspended();

    /**
     * Find all trial tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'TRIAL' AND t.deletedAt IS NULL")
    List<Tenant> findAllTrial();

    /**
     * Find all expired tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'EXPIRED' AND t.deletedAt IS NULL")
    List<Tenant> findAllExpired();

    // =========================================================================
    // SUBSCRIPTION QUERIES
    // =========================================================================

    /**
     * Find tenants with expiring subscriptions
     * Uses JSONB query to check subscription.endDate
     */
    @Query(value = "SELECT * FROM tenants t " +
                   "WHERE t.deleted_at IS NULL " +
                   "AND t.status = 'ACTIVE' " +
                   "AND (t.subscription->>'endDate')::timestamp < :expiryDate",
           nativeQuery = true)
    List<Tenant> findExpiringSubscriptions(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Find tenants with expiring trials
     * Uses JSONB query to check subscription.trialEndsAt
     */
    @Query(value = "SELECT * FROM tenants t " +
                   "WHERE t.deleted_at IS NULL " +
                   "AND t.status = 'TRIAL' " +
                   "AND (t.subscription->>'trialEndsAt')::timestamp < :expiryDate",
           nativeQuery = true)
    List<Tenant> findExpiringTrials(@Param("expiryDate") LocalDateTime expiryDate);

    // =========================================================================
    // STATISTICS
    // =========================================================================

    /**
     * Count active tenants
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.status = 'ACTIVE' AND t.deletedAt IS NULL")
    long countActive();

    /**
     * Count tenants by status
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.status = :status AND t.deletedAt IS NULL")
    long countByStatus(@Param("status") TenantStatus status);

    /**
     * Count tenants by type
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.type = :type AND t.deletedAt IS NULL")
    long countByType(@Param("type") TenantType type);

    /**
     * Count tenants created in date range
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE " +
           "t.createdAt BETWEEN :startDate AND :endDate AND t.deletedAt IS NULL")
    long countCreatedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // =========================================================================
    // SOFT DELETE QUERIES
    // =========================================================================

    /**
     * Find all soft-deleted tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.deletedAt IS NOT NULL")
    List<Tenant> findAllDeleted();

    /**
     * Find soft-deleted tenants with pagination
     */
    @Query("SELECT t FROM Tenant t WHERE t.deletedAt IS NOT NULL")
    Page<Tenant> findAllDeleted(Pageable pageable);

    /**
     * Find tenants deleted after specific date
     */
    @Query("SELECT t FROM Tenant t WHERE t.deletedAt > :date")
    List<Tenant> findDeletedAfter(@Param("date") LocalDateTime date);
}
