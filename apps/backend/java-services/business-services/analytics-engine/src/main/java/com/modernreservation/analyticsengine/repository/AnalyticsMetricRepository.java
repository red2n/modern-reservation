package com.modernreservation.analyticsengine.repository;

import com.modernreservation.analyticsengine.entity.AnalyticsMetric;
import com.modernreservation.analyticsengine.enums.MetricType;
import com.modernreservation.analyticsengine.enums.TimeGranularity;
import com.modernreservation.analyticsengine.enums.AnalyticsStatus;

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
import java.util.Optional;
import java.util.UUID;

/**
 * Analytics Metric Repository
 *
 * Data access layer for analytics metrics with complex querying capabilities.
 * Supports time-series analysis, aggregations, and performance-optimized queries.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Repository
public interface AnalyticsMetricRepository extends JpaRepository<AnalyticsMetric, UUID> {

    // ========== Basic Finder Methods ==========

    /**
     * Find metrics by metric type and property within date range
     */
    @Query("SELECT am FROM AnalyticsMetric am WHERE am.metricType = :metricType " +
           "AND (:propertyId IS NULL OR am.propertyId = :propertyId) " +
           "AND am.periodStart >= :startDate AND am.periodEnd <= :endDate " +
           "AND am.status = 'COMPLETED' " +
           "ORDER BY am.periodStart ASC")
    List<AnalyticsMetric> findByMetricTypeAndPropertyAndDateRange(
        @Param("metricType") MetricType metricType,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find metrics by multiple metric types and properties
     */
    @Query("SELECT am FROM AnalyticsMetric am WHERE am.metricType IN :metricTypes " +
           "AND (:propertyIds IS NULL OR am.propertyId IN :propertyIds) " +
           "AND am.periodStart >= :startDate AND am.periodEnd <= :endDate " +
           "AND am.timeGranularity = :granularity " +
           "AND am.status = 'COMPLETED' " +
           "ORDER BY am.propertyId, am.metricType, am.periodStart")
    List<AnalyticsMetric> findByMetricTypesAndPropertiesAndDateRange(
        @Param("metricTypes") List<MetricType> metricTypes,
        @Param("propertyIds") List<UUID> propertyIds,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("granularity") TimeGranularity granularity
    );

    /**
     * Find the latest metric for a specific type and property
     */
    @Query("SELECT am FROM AnalyticsMetric am WHERE am.metricType = :metricType " +
           "AND am.propertyId = :propertyId " +
           "AND am.status = 'COMPLETED' " +
           "ORDER BY am.periodEnd DESC LIMIT 1")
    Optional<AnalyticsMetric> findLatestMetric(
        @Param("metricType") MetricType metricType,
        @Param("propertyId") UUID propertyId
    );

    /**
     * Find metrics by status with pagination
     */
    Page<AnalyticsMetric> findByStatusOrderByCreatedAtDesc(
        AnalyticsStatus status,
        Pageable pageable
    );

    /**
     * Find expired metrics
     */
    @Query("SELECT am FROM AnalyticsMetric am WHERE am.expiresAt < :currentTime " +
           "AND am.status NOT IN ('EXPIRED', 'FAILED')")
    List<AnalyticsMetric> findExpiredMetrics(@Param("currentTime") LocalDateTime currentTime);

    // ========== Aggregation Queries ==========

    /**
     * Calculate average metric value by type and time range
     */
    @Query("SELECT AVG(am.metricValue) FROM AnalyticsMetric am " +
           "WHERE am.metricType = :metricType " +
           "AND (:propertyId IS NULL OR am.propertyId = :propertyId) " +
           "AND am.periodStart >= :startDate AND am.periodEnd <= :endDate " +
           "AND am.status = 'COMPLETED' AND am.metricValue IS NOT NULL")
    Optional<BigDecimal> calculateAverageMetricValue(
        @Param("metricType") MetricType metricType,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get metric statistics (min, max, avg, count) for a specific metric type
     */
    @Query("SELECT MIN(am.metricValue) as minValue, " +
           "MAX(am.metricValue) as maxValue, " +
           "AVG(am.metricValue) as avgValue, " +
           "COUNT(am) as countValue " +
           "FROM AnalyticsMetric am " +
           "WHERE am.metricType = :metricType " +
           "AND (:propertyId IS NULL OR am.propertyId = :propertyId) " +
           "AND am.periodStart >= :startDate AND am.periodEnd <= :endDate " +
           "AND am.status = 'COMPLETED' AND am.metricValue IS NOT NULL")
    Object[] getMetricStatistics(
        @Param("metricType") MetricType metricType,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Sum metric values by property and date range
     */
    @Query("SELECT am.propertyId, SUM(am.metricValue) " +
           "FROM AnalyticsMetric am " +
           "WHERE am.metricType = :metricType " +
           "AND am.propertyId IN :propertyIds " +
           "AND am.periodStart >= :startDate AND am.periodEnd <= :endDate " +
           "AND am.status = 'COMPLETED' AND am.metricValue IS NOT NULL " +
           "GROUP BY am.propertyId")
    List<Object[]> sumMetricValuesByProperty(
        @Param("metricType") MetricType metricType,
        @Param("propertyIds") List<UUID> propertyIds,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get time series data for trending analysis
     */
    @Query("SELECT am.periodStart, am.metricValue, am.countValue " +
           "FROM AnalyticsMetric am " +
           "WHERE am.metricType = :metricType " +
           "AND (:propertyId IS NULL OR am.propertyId = :propertyId) " +
           "AND am.periodStart >= :startDate AND am.periodEnd <= :endDate " +
           "AND am.timeGranularity = :granularity " +
           "AND am.status = 'COMPLETED' " +
           "ORDER BY am.periodStart ASC")
    List<Object[]> getTimeSeriesData(
        @Param("metricType") MetricType metricType,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("granularity") TimeGranularity granularity
    );

    // ========== Comparison Queries ==========

    /**
     * Compare metrics between two time periods
     */
    @Query("SELECT current.metricType, " +
           "current.propertyId, " +
           "current.metricValue as currentValue, " +
           "previous.metricValue as previousValue, " +
           "(current.metricValue - previous.metricValue) as difference " +
           "FROM AnalyticsMetric current " +
           "LEFT JOIN AnalyticsMetric previous ON " +
           "    current.metricType = previous.metricType " +
           "    AND current.propertyId = previous.propertyId " +
           "    AND current.timeGranularity = previous.timeGranularity " +
           "WHERE current.periodStart >= :currentStartDate " +
           "AND current.periodEnd <= :currentEndDate " +
           "AND previous.periodStart >= :previousStartDate " +
           "AND previous.periodEnd <= :previousEndDate " +
           "AND current.status = 'COMPLETED' " +
           "AND previous.status = 'COMPLETED' " +
           "AND current.metricType IN :metricTypes")
    List<Object[]> compareMetricsBetweenPeriods(
        @Param("metricTypes") List<MetricType> metricTypes,
        @Param("currentStartDate") LocalDateTime currentStartDate,
        @Param("currentEndDate") LocalDateTime currentEndDate,
        @Param("previousStartDate") LocalDateTime previousStartDate,
        @Param("previousEndDate") LocalDateTime previousEndDate
    );

    /**
     * Get period-over-period growth rates
     */
    @Query("SELECT am.metricType, am.propertyId, " +
           "am.metricValue, " +
           "LAG(am.metricValue) OVER (PARTITION BY am.metricType, am.propertyId ORDER BY am.periodStart) as previousValue " +
           "FROM AnalyticsMetric am " +
           "WHERE am.metricType IN :metricTypes " +
           "AND (:propertyId IS NULL OR am.propertyId = :propertyId) " +
           "AND am.periodStart >= :startDate " +
           "AND am.timeGranularity = :granularity " +
           "AND am.status = 'COMPLETED' " +
           "ORDER BY am.metricType, am.propertyId, am.periodStart")
    List<Object[]> getPeriodOverPeriodGrowth(
        @Param("metricTypes") List<MetricType> metricTypes,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDateTime startDate,
        @Param("granularity") TimeGranularity granularity
    );

    // ========== Quality and Performance Queries ==========

    /**
     * Find metrics with low quality scores
     */
    @Query("SELECT am FROM AnalyticsMetric am " +
           "WHERE am.qualityScore < :threshold " +
           "AND am.status = 'COMPLETED' " +
           "AND am.calculatedAt >= :since " +
           "ORDER BY am.qualityScore ASC")
    List<AnalyticsMetric> findLowQualityMetrics(
        @Param("threshold") BigDecimal threshold,
        @Param("since") LocalDateTime since
    );

    /**
     * Get calculation performance statistics
     */
    @Query("SELECT am.metricType, " +
           "AVG(am.calculationDurationMs) as avgDuration, " +
           "MIN(am.calculationDurationMs) as minDuration, " +
           "MAX(am.calculationDurationMs) as maxDuration, " +
           "COUNT(am) as totalCalculations " +
           "FROM AnalyticsMetric am " +
           "WHERE am.status = 'COMPLETED' " +
           "AND am.calculatedAt >= :since " +
           "AND am.calculationDurationMs IS NOT NULL " +
           "GROUP BY am.metricType " +
           "ORDER BY avgDuration DESC")
    List<Object[]> getCalculationPerformanceStats(@Param("since") LocalDateTime since);

    /**
     * Count metrics by status and date range
     */
    @Query("SELECT am.status, COUNT(am) " +
           "FROM AnalyticsMetric am " +
           "WHERE am.createdAt >= :startDate " +
           "AND am.createdAt <= :endDate " +
           "GROUP BY am.status")
    List<Object[]> countMetricsByStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // ========== Data Management Queries ==========

    /**
     * Delete expired metrics older than specified date
     */
    @Modifying
    @Query("DELETE FROM AnalyticsMetric am " +
           "WHERE am.expiresAt < :expirationDate " +
           "AND am.status IN ('EXPIRED', 'FAILED')")
    int deleteExpiredMetrics(@Param("expirationDate") LocalDateTime expirationDate);

    /**
     * Update metrics status by criteria
     */
    @Modifying
    @Query("UPDATE AnalyticsMetric am SET am.status = :newStatus, am.updatedAt = :updatedAt " +
           "WHERE am.status = :currentStatus " +
           "AND am.createdAt < :cutoffDate")
    int updateMetricsStatus(
        @Param("currentStatus") AnalyticsStatus currentStatus,
        @Param("newStatus") AnalyticsStatus newStatus,
        @Param("cutoffDate") LocalDateTime cutoffDate,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Mark stale metrics as expired
     */
    @Modifying
    @Query("UPDATE AnalyticsMetric am SET am.status = 'EXPIRED', am.expiresAt = :expiredAt " +
           "WHERE am.expiresAt < :currentTime " +
           "AND am.status NOT IN ('EXPIRED', 'FAILED')")
    int markStaleMetricsAsExpired(
        @Param("currentTime") LocalDateTime currentTime,
        @Param("expiredAt") LocalDateTime expiredAt
    );

    // ========== Dimension-based Queries ==========

    /**
     * Find metrics by dimension filter
     */
    @Query("SELECT am FROM AnalyticsMetric am " +
           "WHERE am.metricType = :metricType " +
           "AND (:propertyId IS NULL OR am.propertyId = :propertyId) " +
           "AND am.periodStart >= :startDate AND am.periodEnd <= :endDate " +
           "AND am.status = 'COMPLETED' " +
           "AND EXISTS (SELECT 1 FROM am.dimensions d WHERE KEY(d) = :dimensionKey AND VALUE(d) = :dimensionValue)")
    List<AnalyticsMetric> findByDimension(
        @Param("metricType") MetricType metricType,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("dimensionKey") String dimensionKey,
        @Param("dimensionValue") String dimensionValue
    );

    /**
     * Get unique dimension values for a metric type
     */
    @Query("SELECT DISTINCT VALUE(d) FROM AnalyticsMetric am JOIN am.dimensions d " +
           "WHERE am.metricType = :metricType " +
           "AND KEY(d) = :dimensionKey " +
           "AND am.status = 'COMPLETED'")
    List<String> getUniqueDimensionValues(
        @Param("metricType") MetricType metricType,
        @Param("dimensionKey") String dimensionKey
    );

    // ========== Advanced Analytics Queries ==========

    /**
     * Calculate moving averages for trending
     */
    @Query(value = "SELECT period_start, metric_value, " +
                   "AVG(metric_value) OVER (ORDER BY period_start ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) as moving_avg_7, " +
                   "AVG(metric_value) OVER (ORDER BY period_start ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) as moving_avg_30 " +
                   "FROM analytics_metrics " +
                   "WHERE metric_type = :metricType " +
                   "AND (:propertyId IS NULL OR property_id = :propertyId) " +
                   "AND period_start >= :startDate " +
                   "AND status = 'COMPLETED' " +
                   "AND metric_value IS NOT NULL " +
                   "ORDER BY period_start",
           nativeQuery = true)
    List<Object[]> getMovingAverages(
        @Param("metricType") String metricType,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDateTime startDate
    );

    /**
     * Detect outliers using statistical methods
     */
    @Query(value = "WITH stats AS ( " +
                   "  SELECT AVG(metric_value) as mean, " +
                   "         STDDEV(metric_value) as stddev " +
                   "  FROM analytics_metrics " +
                   "  WHERE metric_type = :metricType " +
                   "  AND property_id = :propertyId " +
                   "  AND status = 'COMPLETED' " +
                   "  AND metric_value IS NOT NULL " +
                   ") " +
                   "SELECT metric_id, period_start, metric_value, " +
                   "       ABS(metric_value - stats.mean) / stats.stddev as z_score " +
                   "FROM analytics_metrics, stats " +
                   "WHERE metric_type = :metricType " +
                   "AND property_id = :propertyId " +
                   "AND status = 'COMPLETED' " +
                   "AND ABS(metric_value - stats.mean) / stats.stddev > :zScoreThreshold " +
                   "ORDER BY z_score DESC",
           nativeQuery = true)
    List<Object[]> detectOutliers(
        @Param("metricType") String metricType,
        @Param("propertyId") UUID propertyId,
        @Param("zScoreThreshold") double zScoreThreshold
    );

    /**
     * Get seasonal patterns (for monthly/yearly analysis)
     */
    @Query(value = "SELECT EXTRACT(MONTH FROM period_start) as month, " +
                   "       AVG(metric_value) as avg_value, " +
                   "       STDDEV(metric_value) as stddev_value, " +
                   "       COUNT(*) as data_points " +
                   "FROM analytics_metrics " +
                   "WHERE metric_type = :metricType " +
                   "AND (:propertyId IS NULL OR property_id = :propertyId) " +
                   "AND status = 'COMPLETED' " +
                   "AND metric_value IS NOT NULL " +
                   "GROUP BY EXTRACT(MONTH FROM period_start) " +
                   "ORDER BY month",
           nativeQuery = true)
    List<Object[]> getSeasonalPatterns(
        @Param("metricType") String metricType,
        @Param("propertyId") UUID propertyId
    );

    // ========== Caching and Performance ==========

    /**
     * Find recently calculated metrics for caching
     */
    @Query("SELECT am FROM AnalyticsMetric am " +
           "WHERE am.metricType IN :metricTypes " +
           "AND am.calculatedAt >= :since " +
           "AND am.status = 'COMPLETED' " +
           "ORDER BY am.calculatedAt DESC")
    List<AnalyticsMetric> findRecentlyCalculatedMetrics(
        @Param("metricTypes") List<MetricType> metricTypes,
        @Param("since") LocalDateTime since
    );

    /**
     * Check if metric exists for specific criteria (for avoiding duplicate calculations)
     */
    @Query("SELECT COUNT(am) > 0 FROM AnalyticsMetric am " +
           "WHERE am.metricType = :metricType " +
           "AND am.propertyId = :propertyId " +
           "AND am.timeGranularity = :granularity " +
           "AND am.periodStart = :periodStart " +
           "AND am.periodEnd = :periodEnd " +
           "AND am.status IN ('COMPLETED', 'PROCESSING')")
    boolean existsMetricForCriteria(
        @Param("metricType") MetricType metricType,
        @Param("propertyId") UUID propertyId,
        @Param("granularity") TimeGranularity granularity,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
}
