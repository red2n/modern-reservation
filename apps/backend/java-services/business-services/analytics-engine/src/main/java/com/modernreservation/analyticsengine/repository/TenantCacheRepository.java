package com.modernreservation.analyticsengine.repository;

import com.modernreservation.analyticsengine.entity.TenantCache;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TenantCache entity in Analytics Engine.
 *
 * @see TenantCache
 */
@Repository
public interface TenantCacheRepository extends JpaRepository<TenantCache, UUID> {

    @Query("SELECT t FROM TenantCache t WHERE t.slug = :slug AND t.deletedAt IS NULL")
    Optional<TenantCache> findBySlugAndNotDeleted(@Param("slug") String slug);

    @Query("SELECT t FROM TenantCache t WHERE (t.status = 'ACTIVE' OR t.status = 'TRIAL') AND t.deletedAt IS NULL")
    List<TenantCache> findAllActive();

    @Query("SELECT t FROM TenantCache t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<TenantCache> findByStatus(@Param("status") TenantStatus status);

    @Query("SELECT t FROM TenantCache t WHERE t.deletedAt IS NULL")
    List<TenantCache> findAllOperational();

    @Query("SELECT COUNT(t) FROM TenantCache t WHERE (t.status = 'ACTIVE' OR t.status = 'TRIAL') AND t.deletedAt IS NULL")
    long countAnalyticsEligible();
}
