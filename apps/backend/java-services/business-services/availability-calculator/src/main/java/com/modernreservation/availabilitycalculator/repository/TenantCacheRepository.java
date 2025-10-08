package com.modernreservation.availabilitycalculator.repository;

import com.modernreservation.availabilitycalculator.entity.TenantCache;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TenantCache entity.
 * Provides data access methods for tenant cache operations.
 *
 * @see TenantCache
 */
@Repository
public interface TenantCacheRepository extends JpaRepository<TenantCache, UUID> {

    /**
     * Find tenant by slug, excluding soft-deleted tenants
     */
    @Query("SELECT t FROM TenantCache t WHERE t.slug = :slug AND t.deletedAt IS NULL")
    Optional<TenantCache> findBySlugAndNotDeleted(@Param("slug") String slug);

    /**
     * Find all active tenants (ACTIVE or TRIAL status, not deleted)
     */
    @Query("SELECT t FROM TenantCache t WHERE (t.status = 'ACTIVE' OR t.status = 'TRIAL') AND t.deletedAt IS NULL")
    List<TenantCache> findAllActive();

    /**
     * Find tenants by status, excluding deleted
     */
    @Query("SELECT t FROM TenantCache t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<TenantCache> findByStatus(@Param("status") TenantStatus status);

    /**
     * Find all operational tenants (not deleted, regardless of status)
     */
    @Query("SELECT t FROM TenantCache t WHERE t.deletedAt IS NULL")
    List<TenantCache> findAllOperational();

    /**
     * Count tenants that can calculate availability (ACTIVE or TRIAL)
     */
    @Query("SELECT COUNT(t) FROM TenantCache t WHERE (t.status = 'ACTIVE' OR t.status = 'TRIAL') AND t.deletedAt IS NULL")
    long countAvailabilityEligible();
}
