package com.modernreservation.ratemanagement.repository;

import com.modernreservation.ratemanagement.entity.TenantCache;
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
 * TenantCache Repository - Data access for local tenant cache in Rate Management
 *
 * Provides data access methods for the local tenant cache that is
 * synchronized from the Tenant Service via Kafka events.
 *
 * This is READ-ONLY from the perspective of business logic.
 * Updates happen ONLY via TenantEventConsumer (Kafka listener).
 */
@Repository
public interface TenantCacheRepository extends JpaRepository<TenantCache, UUID> {

    /**
     * Find tenant by slug (excluding deleted)
     */
    @Query("SELECT t FROM TenantCache t WHERE t.slug = :slug AND t.deletedAt IS NULL")
    Optional<TenantCache> findBySlugAndNotDeleted(@Param("slug") String slug);

    /**
     * Check if slug exists
     */
    boolean existsBySlug(String slug);

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

    /**
     * Find tenants by status (excluding deleted)
     */
    @Query("SELECT t FROM TenantCache t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<TenantCache> findByStatus(@Param("status") TenantStatus status);

    /**
     * Find all operational tenants (ACTIVE or TRIAL)
     */
    @Query("SELECT t FROM TenantCache t WHERE " +
           "(t.status = 'ACTIVE' OR t.status = 'TRIAL') AND t.deletedAt IS NULL")
    List<TenantCache> findAllOperational();

    /**
     * Find tenants by type (excluding deleted)
     */
    @Query("SELECT t FROM TenantCache t WHERE t.type = :type AND t.deletedAt IS NULL")
    List<TenantCache> findByType(@Param("type") TenantType type);

    /**
     * Search tenants by name or slug
     */
    @Query("SELECT t FROM TenantCache t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND t.deletedAt IS NULL")
    Page<TenantCache> search(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find stale cache entries (not synced in X minutes)
     */
    @Query("SELECT t FROM TenantCache t WHERE " +
           "t.lastSyncedAt IS NULL OR t.lastSyncedAt < :threshold")
    List<TenantCache> findStaleCacheEntries(@Param("threshold") LocalDateTime threshold);

    /**
     * Count active cache entries
     */
    @Query("SELECT COUNT(t) FROM TenantCache t WHERE t.deletedAt IS NULL")
    long countActive();

    /**
     * Count by status
     */
    @Query("SELECT COUNT(t) FROM TenantCache t WHERE t.status = :status AND t.deletedAt IS NULL")
    long countByStatus(@Param("status") TenantStatus status);
}
