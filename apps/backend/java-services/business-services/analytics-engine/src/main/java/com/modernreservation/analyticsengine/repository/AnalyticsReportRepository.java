package com.modernreservation.analyticsengine.repository;

import com.modernreservation.analyticsengine.entity.AnalyticsReport;
import com.modernreservation.analyticsengine.enums.AnalyticsStatus;
import com.modernreservation.analyticsengine.enums.TimeGranularity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Analytics Report Repository
 *
 * Data access layer for analytics reports with scheduling and management capabilities.
 * Supports report lifecycle management, scheduling, and performance tracking.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Repository
public interface AnalyticsReportRepository extends JpaRepository<AnalyticsReport, UUID> {

    // ========== Basic Finder Methods ==========

    /**
     * Find reports by type and status
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.reportType = :reportType " +
           "AND (:status IS NULL OR ar.status = :status) " +
           "ORDER BY ar.createdAt DESC")
    List<AnalyticsReport> findByReportTypeAndStatus(
        @Param("reportType") String reportType,
        @Param("status") AnalyticsStatus status
    );

    /**
     * Find reports by property and date range
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE " +
           "(:propertyId IS NULL OR ar.propertyId = :propertyId OR :propertyId MEMBER OF ar.propertyIds) " +
           "AND ar.periodStart >= :startDate AND ar.periodEnd <= :endDate " +
           "ORDER BY ar.generatedAt DESC")
    List<AnalyticsReport> findByPropertyAndDateRange(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find reports by creator with pagination
     */
    Page<AnalyticsReport> findByCreatedByOrderByCreatedAtDesc(
        String createdBy,
        Pageable pageable
    );

    /**
     * Find reports by property with pagination
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE " +
           "(:propertyId IS NULL OR ar.propertyId = :propertyId OR :propertyId MEMBER OF ar.propertyIds) " +
           "ORDER BY ar.createdAt DESC")
    List<AnalyticsReport> findByPropertyIdOrderByCreatedAtDesc(
        @Param("propertyId") UUID propertyId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * Find scheduled reports for a property
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE " +
           "ar.isScheduled = true " +
           "AND (:propertyId IS NULL OR ar.propertyId = :propertyId OR :propertyId MEMBER OF ar.propertyIds) " +
           "AND ar.status IN ('PENDING', 'SCHEDULED') " +
           "ORDER BY ar.nextRunAt ASC")
    List<AnalyticsReport> findScheduledReports(@Param("propertyId") UUID propertyId);

    /**
     * Find public reports
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.isPublic = true " +
           "AND ar.status = 'COMPLETED' " +
           "AND (ar.expiresAt IS NULL OR ar.expiresAt > :currentTime) " +
           "ORDER BY ar.generatedAt DESC")
    List<AnalyticsReport> findPublicReports(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find reports accessible by user
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE " +
           "(ar.isPublic = true OR ar.createdBy = :userId OR :userEmail MEMBER OF ar.recipientEmails) " +
           "AND ar.status = 'COMPLETED' " +
           "AND (ar.expiresAt IS NULL OR ar.expiresAt > :currentTime) " +
           "ORDER BY ar.generatedAt DESC")
    List<AnalyticsReport> findAccessibleReports(
        @Param("userId") String userId,
        @Param("userEmail") String userEmail,
        @Param("currentTime") LocalDateTime currentTime
    );

    // ========== Scheduled Report Queries ==========

    /**
     * Find scheduled reports due for execution
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.isScheduled = true " +
           "AND ar.nextRunAt <= :currentTime " +
           "AND ar.status NOT IN ('PROCESSING', 'VALIDATING', 'AGGREGATING', 'CALCULATING', 'GENERATING', 'FINALIZING') " +
           "ORDER BY ar.priorityLevel DESC, ar.nextRunAt ASC")
    List<AnalyticsReport> findScheduledReportsDueForExecution(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find recurring reports
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.isRecurring = true " +
           "AND ar.isScheduled = true " +
           "ORDER BY ar.nextRunAt ASC")
    List<AnalyticsReport> findRecurringReports();

    /**
     * Find scheduled reports by pattern
     */
    List<AnalyticsReport> findByIsScheduledTrueAndRecurrencePattern(String recurrencePattern);

    /**
     * Update next run time for scheduled reports
     */
    @Modifying
    @Query("UPDATE AnalyticsReport ar SET ar.nextRunAt = :nextRunAt, ar.updatedAt = :updatedAt " +
           "WHERE ar.reportId = :reportId")
    int updateNextRunTime(
        @Param("reportId") UUID reportId,
        @Param("nextRunAt") LocalDateTime nextRunAt,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    // ========== Report Status and Lifecycle ==========

    /**
     * Find expired reports
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.expiresAt < :currentTime " +
           "AND ar.status NOT IN ('EXPIRED', 'FAILED')")
    List<AnalyticsReport> findExpiredReports(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find reports by status with filters
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.status = :status " +
           "AND (:reportType IS NULL OR ar.reportType = :reportType) " +
           "AND (:createdBy IS NULL OR ar.createdBy = :createdBy) " +
           "AND ar.createdAt >= :since " +
           "ORDER BY ar.createdAt DESC")
    List<AnalyticsReport> findByStatusWithFilters(
        @Param("status") AnalyticsStatus status,
        @Param("reportType") String reportType,
        @Param("createdBy") String createdBy,
        @Param("since") LocalDateTime since
    );

    /**
     * Count reports by status
     */
    @Query("SELECT ar.status, COUNT(ar) FROM AnalyticsReport ar " +
           "WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate " +
           "GROUP BY ar.status")
    List<Object[]> countReportsByStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find long-running report generations
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE " +
           "ar.status IN ('PROCESSING', 'VALIDATING', 'AGGREGATING', 'CALCULATING', 'GENERATING', 'FINALIZING') " +
           "AND ar.updatedAt < :cutoffTime " +
           "ORDER BY ar.updatedAt ASC")
    List<AnalyticsReport> findLongRunningReports(@Param("cutoffTime") LocalDateTime cutoffTime);

    // ========== Performance and Quality Queries ==========

    /**
     * Get report generation performance statistics
     */
    @Query("SELECT ar.reportType, " +
           "AVG(ar.generationDurationMs) as avgDuration, " +
           "MIN(ar.generationDurationMs) as minDuration, " +
           "MAX(ar.generationDurationMs) as maxDuration, " +
           "COUNT(ar) as totalReports, " +
           "SUM(CASE WHEN ar.status = 'COMPLETED' THEN 1 ELSE 0 END) as successfulReports " +
           "FROM AnalyticsReport ar " +
           "WHERE ar.generatedAt >= :since " +
           "AND ar.generationDurationMs IS NOT NULL " +
           "GROUP BY ar.reportType " +
           "ORDER BY avgDuration DESC")
    List<Object[]> getReportGenerationPerformanceStats(@Param("since") LocalDateTime since);

    /**
     * Find reports with low quality scores
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.overallQualityScore < :threshold " +
           "AND ar.status = 'COMPLETED' " +
           "AND ar.generatedAt >= :since " +
           "ORDER BY ar.overallQualityScore ASC")
    List<AnalyticsReport> findLowQualityReports(
        @Param("threshold") BigDecimal threshold,
        @Param("since") LocalDateTime since
    );

    /**
     * Get average data completeness by report type
     */
    @Query("SELECT ar.reportType, AVG(ar.dataCompletenessPercentage) " +
           "FROM AnalyticsReport ar " +
           "WHERE ar.status = 'COMPLETED' " +
           "AND ar.dataCompletenessPercentage IS NOT NULL " +
           "AND ar.generatedAt >= :since " +
           "GROUP BY ar.reportType " +
           "ORDER BY AVG(ar.dataCompletenessPercentage) DESC")
    List<Object[]> getAverageDataCompletenessByType(@Param("since") LocalDateTime since);

    // ========== Report Templates and Hierarchy ==========

    /**
     * Find reports using a specific template
     */
    List<AnalyticsReport> findByTemplateIdOrderByCreatedAtDesc(UUID templateId);

    /**
     * Find child reports (drill-down reports)
     */
    List<AnalyticsReport> findByParentReportIdOrderByCreatedAtDesc(UUID parentReportId);

    /**
     * Find reports with similar configuration
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.reportType = :reportType " +
           "AND ar.timeGranularity = :granularity " +
           "AND ar.reportId != :excludeReportId " +
           "ORDER BY ar.generatedAt DESC")
    List<AnalyticsReport> findSimilarReports(
        @Param("reportType") String reportType,
        @Param("granularity") TimeGranularity granularity,
        @Param("excludeReportId") UUID excludeReportId
    );

    // ========== Access and Security ==========

    /**
     * Find reports by access token
     */
    Optional<AnalyticsReport> findByAccessTokenAndIsPublicTrue(String accessToken);

    /**
     * Find reports shared with specific user
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE :userEmail MEMBER OF ar.recipientEmails " +
           "AND ar.status = 'COMPLETED' " +
           "ORDER BY ar.generatedAt DESC")
    List<AnalyticsReport> findReportsSharedWithUser(@Param("userEmail") String userEmail);

    /**
     * Check if user has access to report
     */
    @Query("SELECT COUNT(ar) > 0 FROM AnalyticsReport ar WHERE ar.reportId = :reportId " +
           "AND (ar.isPublic = true OR ar.createdBy = :userId OR :userEmail MEMBER OF ar.recipientEmails)")
    boolean hasUserAccessToReport(
        @Param("reportId") UUID reportId,
        @Param("userId") String userId,
        @Param("userEmail") String userEmail
    );

    // ========== Analytics and Insights ==========

    /**
     * Get report usage statistics
     */
    @Query("SELECT ar.reportType, " +
           "COUNT(ar) as totalReports, " +
           "SUM(CASE WHEN ar.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedReports, " +
           "SUM(CASE WHEN ar.isScheduled = true THEN 1 ELSE 0 END) as scheduledReports, " +
           "AVG(ar.reportSizeBytes) as avgSize " +
           "FROM AnalyticsReport ar " +
           "WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate " +
           "GROUP BY ar.reportType " +
           "ORDER BY totalReports DESC")
    List<Object[]> getReportUsageStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find most popular report types
     */
    @Query("SELECT ar.reportType, COUNT(ar) as reportCount " +
           "FROM AnalyticsReport ar " +
           "WHERE ar.createdAt >= :since " +
           "GROUP BY ar.reportType " +
           "ORDER BY reportCount DESC")
    List<Object[]> getMostPopularReportTypes(@Param("since") LocalDateTime since);

    /**
     * Get report size distribution
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN ar.reportSizeBytes < 1048576 THEN 'Small (<1MB)' " +
           "  WHEN ar.reportSizeBytes < 10485760 THEN 'Medium (1-10MB)' " +
           "  WHEN ar.reportSizeBytes < 104857600 THEN 'Large (10-100MB)' " +
           "  ELSE 'Very Large (>100MB)' " +
           "END as sizeCategory, " +
           "COUNT(ar) as reportCount " +
           "FROM AnalyticsReport ar " +
           "WHERE ar.reportSizeBytes IS NOT NULL " +
           "AND ar.status = 'COMPLETED' " +
           "GROUP BY " +
           "CASE " +
           "  WHEN ar.reportSizeBytes < 1048576 THEN 'Small (<1MB)' " +
           "  WHEN ar.reportSizeBytes < 10485760 THEN 'Medium (1-10MB)' " +
           "  WHEN ar.reportSizeBytes < 104857600 THEN 'Large (10-100MB)' " +
           "  ELSE 'Very Large (>100MB)' " +
           "END")
    List<Object[]> getReportSizeDistribution();

    // ========== Data Management ==========

    /**
     * Delete expired reports older than specified date
     */
    @Modifying
    @Query("DELETE FROM AnalyticsReport ar " +
           "WHERE ar.expiresAt < :expirationDate " +
           "AND ar.status IN ('EXPIRED', 'FAILED')")
    int deleteExpiredReports(@Param("expirationDate") LocalDateTime expirationDate);

    /**
     * Update report status
     */
    @Modifying
    @Query("UPDATE AnalyticsReport ar SET ar.status = :newStatus, ar.updatedAt = :updatedAt " +
           "WHERE ar.reportId = :reportId")
    int updateReportStatus(
        @Param("reportId") UUID reportId,
        @Param("newStatus") AnalyticsStatus newStatus,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Mark stale reports as expired
     */
    @Modifying
    @Query("UPDATE AnalyticsReport ar SET ar.status = 'EXPIRED', ar.expiresAt = :expiredAt " +
           "WHERE ar.expiresAt < :currentTime " +
           "AND ar.status NOT IN ('EXPIRED', 'FAILED')")
    int markStaleReportsAsExpired(
        @Param("currentTime") LocalDateTime currentTime,
        @Param("expiredAt") LocalDateTime expiredAt
    );

    /**
     * Update report generation metadata
     */
    @Modifying
    @Query("UPDATE AnalyticsReport ar SET " +
           "ar.generationDurationMs = :duration, " +
           "ar.totalMetricsCount = :totalMetrics, " +
           "ar.successfulMetricsCount = :successfulMetrics, " +
           "ar.failedMetricsCount = :failedMetrics, " +
           "ar.overallQualityScore = :qualityScore, " +
           "ar.generatedAt = :generatedAt, " +
           "ar.updatedAt = :updatedAt " +
           "WHERE ar.reportId = :reportId")
    int updateReportGenerationMetadata(
        @Param("reportId") UUID reportId,
        @Param("duration") Long duration,
        @Param("totalMetrics") Integer totalMetrics,
        @Param("successfulMetrics") Integer successfulMetrics,
        @Param("failedMetrics") Integer failedMetrics,
        @Param("qualityScore") BigDecimal qualityScore,
        @Param("generatedAt") LocalDateTime generatedAt,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    // ========== Search and Filtering ==========

    /**
     * Search reports by name or description
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE " +
           "(LOWER(ar.reportName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(ar.reportDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND ar.status = 'COMPLETED' " +
           "ORDER BY ar.generatedAt DESC")
    List<AnalyticsReport> searchReports(@Param("searchTerm") String searchTerm);

    /**
     * Find reports by tags
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.tags LIKE CONCAT('%', :tag, '%') " +
           "ORDER BY ar.generatedAt DESC")
    List<AnalyticsReport> findByTag(@Param("tag") String tag);

    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE " +
           "(:reportType IS NULL OR ar.reportType = :reportType) " +
           "AND (:createdBy IS NULL OR ar.createdBy = :createdBy) " +
           "AND (:startDate IS NULL OR ar.periodStart >= :startDate) " +
           "AND (:endDate IS NULL OR ar.periodEnd <= :endDate) " +
           "AND (:status IS NULL OR ar.status = :status) " +
           "AND (:isScheduled IS NULL OR ar.isScheduled = :isScheduled) " +
           "ORDER BY ar.generatedAt DESC")
    Page<AnalyticsReport> advancedSearch(
        @Param("reportType") String reportType,
        @Param("createdBy") String createdBy,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("status") AnalyticsStatus status,
        @Param("isScheduled") Boolean isScheduled,
        Pageable pageable
    );

    // ========== Duplicate Detection ==========

    /**
     * Find duplicate reports based on similar configuration
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE " +
           "ar.reportType = :reportType " +
           "AND ar.timeGranularity = :granularity " +
           "AND ar.periodStart = :periodStart " +
           "AND ar.periodEnd = :periodEnd " +
           "AND (:propertyId IS NULL OR ar.propertyId = :propertyId) " +
           "AND ar.status IN ('COMPLETED', 'PROCESSING') " +
           "ORDER BY ar.createdAt DESC")
    List<AnalyticsReport> findPotentialDuplicates(
        @Param("reportType") String reportType,
        @Param("granularity") TimeGranularity granularity,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd,
        @Param("propertyId") UUID propertyId
    );

    /**
     * Check if similar report exists
     */
    @Query("SELECT COUNT(ar) > 0 FROM AnalyticsReport ar WHERE " +
           "ar.reportType = :reportType " +
           "AND ar.timeGranularity = :granularity " +
           "AND ar.periodStart = :periodStart " +
           "AND ar.periodEnd = :periodEnd " +
           "AND (:propertyId IS NULL OR ar.propertyId = :propertyId) " +
           "AND ar.status IN ('COMPLETED', 'PROCESSING')")
    boolean existsSimilarReport(
        @Param("reportType") String reportType,
        @Param("granularity") TimeGranularity granularity,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd,
        @Param("propertyId") UUID propertyId
    );

    /**
     * Get usage statistics for a specific report
     */
    @Query("SELECT new map(" +
           "COUNT(*) as totalViews, " +
           "COUNT(DISTINCT ar.createdBy) as uniqueViewers, " +
           "0 as totalDownloads, " +
           "COALESCE(AVG(ar.generationDurationMs), 0) as avgGenerationTime " +
           ") FROM AnalyticsReport ar WHERE ar.reportId = :reportId")
    Map<String, Object> getReportUsageStatistics(@Param("reportId") UUID reportId);

    /**
     * Find scheduled reports that are due
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.status = 'SCHEDULED' " +
           "AND ar.nextRunAt <= :currentTime")
    List<AnalyticsReport> findDueScheduledReports(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find old reports for cleanup
     */
    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.createdAt < :cutoffDate " +
           "AND ar.status IN ('COMPLETED', 'FAILED')")
    List<AnalyticsReport> findOldReports(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get top viewers for a report
     */
    @Query("SELECT ar.createdBy FROM AnalyticsReport ar WHERE ar.reportId = :reportId " +
           "GROUP BY ar.createdBy ORDER BY COUNT(*) DESC")
    List<String> getTopViewers(@Param("reportId") UUID reportId);

    /**
     * Get access patterns for a report
     */
    @Query("SELECT new map(" +
           "ar.createdAt as accessTime, " +
           "ar.createdBy as userId " +
           ") FROM AnalyticsReport ar WHERE ar.reportId = :reportId " +
           "ORDER BY ar.createdAt DESC")
    Map<String, Object> getAccessPatterns(@Param("reportId") UUID reportId);
}
