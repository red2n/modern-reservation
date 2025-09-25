package com.modernreservation.analyticsengine.service;

import com.modernreservation.analyticsengine.dto.AnalyticsRequestDTO;
import com.modernreservation.analyticsengine.dto.AnalyticsResponseDTO;
import com.modernreservation.analyticsengine.dto.DashboardDTO;
import com.modernreservation.analyticsengine.entity.AnalyticsMetric;
import com.modernreservation.analyticsengine.enums.MetricType;
import com.modernreservation.analyticsengine.enums.TimeGranularity;
import com.modernreservation.analyticsengine.enums.AnalyticsStatus;
import com.modernreservation.analyticsengine.repository.AnalyticsMetricRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Analytics Service
 *
 * Core service for analytics calculations, metric processing, and business intelligence.
 * Handles complex analytics operations, caching, and asynchronous processing.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AnalyticsMetricRepository analyticsMetricRepository;
    private final MetricCalculationService metricCalculationService;
    private final TrendAnalysisService trendAnalysisService;
    private final ForecastingService forecastingService;
    private final DataQualityService dataQualityService;
    private final CacheService cacheService;

    /**
     * Calculate analytics metrics based on request parameters
     */
    @Transactional
    public AnalyticsResponseDTO calculateAnalytics(AnalyticsRequestDTO request) {
        log.info("Starting analytics calculation for request: {}", request.getSessionId());

        try {
            // Validate request
            validateAnalyticsRequest(request);

            // Check cache if enabled
            if (Boolean.TRUE.equals(request.getEnableCaching())) {
                AnalyticsResponseDTO cachedResponse = getCachedResponse(request);
                if (cachedResponse != null) {
                    log.info("Returning cached analytics response");
                    return cachedResponse;
                }
            }

            // Handle async execution
            if (Boolean.TRUE.equals(request.getAsyncExecution())) {
                return handleAsyncCalculation(request);
            }

            // Perform synchronous calculation
            return performSynchronousCalculation(request);

        } catch (Exception e) {
            log.error("Error calculating analytics: {}", e.getMessage(), e);
            return AnalyticsResponseDTO.createErrorResponse(
                UUID.randomUUID(),
                "Analytics calculation failed: " + e.getMessage()
            );
        }
    }

    /**
     * Get dashboard data for real-time analytics display
     */
    @Cacheable(value = "dashboard", key = "#dashboardType + '_' + #propertyId + '_' + #refreshIntervalMinutes")
    public DashboardDTO getDashboardData(String dashboardType, UUID propertyId, Integer refreshIntervalMinutes) {
        log.info("Getting dashboard data for type: {} and property: {}", dashboardType, propertyId);

        try {
            DashboardDTO.DashboardDTOBuilder dashboardBuilder = DashboardDTO.builder()
                .dashboardId(UUID.randomUUID())
                .dashboardName(getDashboardName(dashboardType))
                .dashboardType(dashboardType)
                .propertyId(propertyId)
                .lastRefreshedAt(LocalDateTime.now())
                .refreshIntervalMinutes(refreshIntervalMinutes != null ? refreshIntervalMinutes : 5)
                .dataFreshness("REAL_TIME");

            // Calculate next refresh time
            if (refreshIntervalMinutes != null) {
                dashboardBuilder.nextRefreshAt(LocalDateTime.now().plusMinutes(refreshIntervalMinutes));
            }

            // Get KPIs based on dashboard type
            List<DashboardDTO.KPIWidgetDTO> kpis = getKPIsForDashboard(dashboardType, propertyId);
            dashboardBuilder.kpis(kpis);

            // Get charts
            List<DashboardDTO.ChartWidgetDTO> charts = getChartsForDashboard(dashboardType, propertyId);
            dashboardBuilder.charts(charts);

            // Get tables
            List<DashboardDTO.TableWidgetDTO> tables = getTablesForDashboard(dashboardType, propertyId);
            dashboardBuilder.tables(tables);

            // Get alerts
            List<DashboardDTO.AlertDTO> alerts = getActiveAlerts(propertyId);
            dashboardBuilder.alerts(alerts);

            // Get recent activities
            List<DashboardDTO.ActivityDTO> activities = getRecentActivities(propertyId);
            dashboardBuilder.recentActivities(activities);

            // Calculate overall data quality
            BigDecimal dataQuality = calculateOverallDataQuality(propertyId);
            dashboardBuilder.dataQualityScore(dataQuality);

            // Add metadata
            DashboardDTO.DashboardMetadataDTO metadata = DashboardDTO.DashboardMetadataDTO.builder()
                .version("3.2.0")
                .createdAt(LocalDateTime.now())
                .category(dashboardType)
                .accessLevel("PRIVATE")
                .build();
            dashboardBuilder.metadata(metadata);

            return dashboardBuilder.build();

        } catch (Exception e) {
            log.error("Error getting dashboard data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get dashboard data", e);
        }
    }

    /**
     * Get metric history for trend analysis
     */
    public List<AnalyticsMetric> getMetricHistory(MetricType metricType, UUID propertyId,
                                                  LocalDateTime startDate, LocalDateTime endDate,
                                                  TimeGranularity granularity) {
        log.info("Getting metric history for type: {} and property: {}", metricType, propertyId);

        return analyticsMetricRepository.findByMetricTypeAndPropertyAndDateRange(
            metricType, propertyId, startDate, endDate
        ).stream()
        .filter(metric -> metric.getTimeGranularity() == granularity)
        .sorted(Comparator.comparing(AnalyticsMetric::getPeriodStart))
        .collect(Collectors.toList());
    }

    /**
     * Calculate comparative analytics between two periods
     */
    public AnalyticsResponseDTO.ComparisonResultDTO calculateComparison(
            List<MetricType> metricTypes, UUID propertyId,
            LocalDateTime currentStart, LocalDateTime currentEnd,
            LocalDateTime previousStart, LocalDateTime previousEnd) {

        log.info("Calculating comparison analytics for {} metrics", metricTypes.size());

        try {
            List<Object[]> comparisonData = analyticsMetricRepository.compareMetricsBetweenPeriods(
                metricTypes, currentStart, currentEnd, previousStart, previousEnd
            );

            List<AnalyticsResponseDTO.ComparisonResultDTO.MetricComparisonDTO> comparisons =
                comparisonData.stream()
                    .map(this::mapToMetricComparison)
                    .collect(Collectors.toList());

            return AnalyticsResponseDTO.ComparisonResultDTO.builder()
                .comparisonPeriodStart(previousStart)
                .comparisonPeriodEnd(previousEnd)
                .comparisons(comparisons)
                .build();

        } catch (Exception e) {
            log.error("Error calculating comparison: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate comparison", e);
        }
    }

    /**
     * Get analytics insights and recommendations
     */
    public List<String> getAnalyticsInsights(UUID propertyId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating analytics insights for property: {}", propertyId);

        List<String> insights = new ArrayList<>();

        try {
            // Get key metrics for insight generation
            List<AnalyticsMetric> revenueMetrics = getMetricHistory(
                MetricType.TOTAL_REVENUE, propertyId, startDate, endDate, TimeGranularity.DAILY
            );

            List<AnalyticsMetric> occupancyMetrics = getMetricHistory(
                MetricType.OCCUPANCY_RATE, propertyId, startDate, endDate, TimeGranularity.DAILY
            );

            // Generate revenue insights
            if (!revenueMetrics.isEmpty()) {
                insights.addAll(generateRevenueInsights(revenueMetrics));
            }

            // Generate occupancy insights
            if (!occupancyMetrics.isEmpty()) {
                insights.addAll(generateOccupancyInsights(occupancyMetrics));
            }

            // Generate trend insights
            insights.addAll(generateTrendInsights(propertyId, startDate, endDate));

            // Generate seasonal insights
            insights.addAll(generateSeasonalInsights(propertyId));

            return insights;

        } catch (Exception e) {
            log.error("Error generating insights: {}", e.getMessage(), e);
            return Collections.singletonList("Unable to generate insights at this time");
        }
    }

    /**
     * Perform asynchronous analytics calculation
     */
    @Async("analyticsExecutor")
    public CompletableFuture<AnalyticsResponseDTO> calculateAnalyticsAsync(AnalyticsRequestDTO request) {
        log.info("Starting async analytics calculation");

        try {
            AnalyticsResponseDTO response = performSynchronousCalculation(request);

            // Send callback notification if provided
            if (request.getCallbackUrl() != null) {
                sendCallbackNotification(request.getCallbackUrl(), response);
            }

            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            log.error("Error in async analytics calculation: {}", e.getMessage(), e);
            AnalyticsResponseDTO errorResponse = AnalyticsResponseDTO.createErrorResponse(
                UUID.randomUUID(),
                "Async analytics calculation failed: " + e.getMessage()
            );
            return CompletableFuture.completedFuture(errorResponse);
        }
    }

    /**
     * Validate analytics request
     */
    private void validateAnalyticsRequest(AnalyticsRequestDTO request) {
        if (request.getTimeGranularity() == null) {
            throw new IllegalArgumentException("Time granularity is required");
        }

        if (request.getPeriodStart() == null || request.getPeriodEnd() == null) {
            throw new IllegalArgumentException("Period start and end dates are required");
        }

        if (!request.isValidTimePeriod()) {
            throw new IllegalArgumentException("Invalid time period: start date must be before end date");
        }

        if (!request.isSuitableGranularity()) {
            TimeGranularity recommended = request.getRecommendedGranularity();
            log.warn("Requested granularity {} may not be suitable for period duration. Recommended: {}",
                request.getTimeGranularity(), recommended);
        }
    }

    /**
     * Get cached response if available
     */
    private AnalyticsResponseDTO getCachedResponse(AnalyticsRequestDTO request) {
        try {
            String cacheKey = generateCacheKey(request);
            return cacheService.get(cacheKey, AnalyticsResponseDTO.class);
        } catch (Exception e) {
            log.warn("Error getting cached response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Handle asynchronous calculation
     */
    private AnalyticsResponseDTO handleAsyncCalculation(AnalyticsRequestDTO request) {
        // Start async processing
        calculateAnalyticsAsync(request);

        // Return pending response
        return AnalyticsResponseDTO.createPendingResponse(UUID.randomUUID());
    }

    /**
     * Perform synchronous calculation
     */
    private AnalyticsResponseDTO performSynchronousCalculation(AnalyticsRequestDTO request) {
        long startTime = System.currentTimeMillis();
        UUID responseId = UUID.randomUUID();

        AnalyticsResponseDTO.AnalyticsResponseDTOBuilder responseBuilder = AnalyticsResponseDTO.builder()
            .responseId(responseId)
            .requestId(UUID.randomUUID())
            .status(AnalyticsStatus.PROCESSING)
            .timeGranularity(request.getTimeGranularity())
            .periodStart(request.getPeriodStart())
            .periodEnd(request.getPeriodEnd());

        try {
            // Calculate metrics
            List<AnalyticsResponseDTO.MetricResultDTO> metrics = calculateMetrics(request);
            responseBuilder.metrics(metrics);

            int successfulMetrics = (int) metrics.stream()
                .filter(m -> m.getConfidenceScore() != null &&
                           m.getConfidenceScore().compareTo(BigDecimal.valueOf(request.getConfidenceThreshold())) >= 0)
                .count();

            responseBuilder
                .successfulMetrics(successfulMetrics)
                .failedMetrics(metrics.size() - successfulMetrics)
                .totalDataPoints(metrics.size());

            // Add comparison if requested
            if (Boolean.TRUE.equals(request.getIncludeComparison())) {
                AnalyticsResponseDTO.ComparisonResultDTO comparison = calculateComparisonForRequest(request);
                responseBuilder.comparison(comparison);
            }

            // Add trend analysis if requested
            if (Boolean.TRUE.equals(request.getIncludeTrends())) {
                AnalyticsResponseDTO.TrendAnalysisDTO trendAnalysis = calculateTrendsForRequest(request);
                responseBuilder.trendAnalysis(trendAnalysis);
            }

            // Add forecast if requested
            if (Boolean.TRUE.equals(request.getIncludeForecast())) {
                AnalyticsResponseDTO.ForecastResultDTO forecast = calculateForecastForRequest(request);
                responseBuilder.forecast(forecast);
            }

            // Calculate overall scores
            BigDecimal overallQuality = calculateOverallQualityScore(metrics);
            BigDecimal overallConfidence = calculateOverallConfidenceScore(metrics);

            responseBuilder
                .overallQualityScore(overallQuality)
                .overallConfidenceScore(overallConfidence)
                .status(AnalyticsStatus.COMPLETED);

            long duration = System.currentTimeMillis() - startTime;
            responseBuilder
                .calculationDurationMs(duration)
                .calculatedAt(LocalDateTime.now());

            AnalyticsResponseDTO response = responseBuilder.build();

            // Cache response if enabled
            if (Boolean.TRUE.equals(request.getEnableCaching())) {
                cacheResponse(request, response);
            }

            log.info("Analytics calculation completed in {}ms", duration);
            return response;

        } catch (Exception e) {
            log.error("Error in synchronous calculation: {}", e.getMessage(), e);
            return AnalyticsResponseDTO.createErrorResponse(responseId, e.getMessage());
        }
    }

    /**
     * Calculate metrics based on request
     */
    private List<AnalyticsResponseDTO.MetricResultDTO> calculateMetrics(AnalyticsRequestDTO request) {
        List<MetricType> metricTypes = request.getMetricTypes();
        if (metricTypes == null || metricTypes.isEmpty()) {
            metricTypes = getDefaultMetricTypes(request);
        }

        List<AnalyticsResponseDTO.MetricResultDTO> results = new ArrayList<>();

        for (MetricType metricType : metricTypes) {
            try {
                AnalyticsResponseDTO.MetricResultDTO result = metricCalculationService.calculateMetric(
                    metricType, request.getEffectivePropertyId(),
                    request.getPeriodStart(), request.getPeriodEnd(),
                    request.getTimeGranularity()
                );

                if (result != null) {
                    results.add(result);
                }

            } catch (Exception e) {
                log.warn("Failed to calculate metric {}: {}", metricType, e.getMessage());

                // Create error result
                AnalyticsResponseDTO.MetricResultDTO errorResult = AnalyticsResponseDTO.MetricResultDTO.builder()
                    .metricType(metricType)
                    .value(null)
                    .confidenceScore(BigDecimal.ZERO)
                    .qualityScore(BigDecimal.ZERO)
                    .calculatedAt(LocalDateTime.now())
                    .formattedValue("Error: " + e.getMessage())
                    .build();

                results.add(errorResult);
            }
        }

        return results;
    }

    // Helper methods for dashboard data

    private List<DashboardDTO.KPIWidgetDTO> getKPIsForDashboard(String dashboardType, UUID propertyId) {
        List<DashboardDTO.KPIWidgetDTO> kpis = new ArrayList<>();

        switch (dashboardType.toUpperCase()) {
            case "EXECUTIVE":
                kpis.addAll(getExecutiveKPIs(propertyId));
                break;
            case "OPERATIONAL":
                kpis.addAll(getOperationalKPIs(propertyId));
                break;
            case "FINANCIAL":
                kpis.addAll(getFinancialKPIs(propertyId));
                break;
            case "REVENUE":
                kpis.addAll(getRevenueKPIs(propertyId));
                break;
            case "OCCUPANCY":
                kpis.addAll(getOccupancyKPIs(propertyId));
                break;
            default:
                kpis.addAll(getDefaultKPIs(propertyId));
        }

        return kpis;
    }

    private List<DashboardDTO.KPIWidgetDTO> getExecutiveKPIs(UUID propertyId) {
        List<DashboardDTO.KPIWidgetDTO> kpis = new ArrayList<>();

        // Total Revenue KPI
        Optional<AnalyticsMetric> revenueMetric = analyticsMetricRepository.findLatestMetric(
            MetricType.TOTAL_REVENUE, propertyId
        );

        if (revenueMetric.isPresent()) {
            kpis.add(DashboardDTO.KPIWidgetDTO.builder()
                .widgetId("total-revenue")
                .title("Total Revenue")
                .metricType(MetricType.TOTAL_REVENUE)
                .value(revenueMetric.get().getMetricValue())
                .formattedValue(revenueMetric.get().getFormattedValue())
                .unit("USD")
                .trend(revenueMetric.get().getTrendDirection())
                .calculatedAt(revenueMetric.get().getCalculatedAt())
                .status("ON_TARGET")
                .color("GREEN")
                .icon("💰")
                .build());
        }

        // Add more executive KPIs
        return kpis;
    }

    private List<DashboardDTO.KPIWidgetDTO> getOperationalKPIs(UUID propertyId) {
        // Implementation for operational KPIs
        return new ArrayList<>();
    }

    private List<DashboardDTO.KPIWidgetDTO> getFinancialKPIs(UUID propertyId) {
        // Implementation for financial KPIs
        return new ArrayList<>();
    }

    private List<DashboardDTO.KPIWidgetDTO> getRevenueKPIs(UUID propertyId) {
        // Implementation for revenue KPIs
        return new ArrayList<>();
    }

    private List<DashboardDTO.KPIWidgetDTO> getOccupancyKPIs(UUID propertyId) {
        // Implementation for occupancy KPIs
        return new ArrayList<>();
    }

    private List<DashboardDTO.KPIWidgetDTO> getDefaultKPIs(UUID propertyId) {
        // Implementation for default KPIs
        return new ArrayList<>();
    }

    private List<DashboardDTO.ChartWidgetDTO> getChartsForDashboard(String dashboardType, UUID propertyId) {
        // Implementation for charts
        return new ArrayList<>();
    }

    private List<DashboardDTO.TableWidgetDTO> getTablesForDashboard(String dashboardType, UUID propertyId) {
        // Implementation for tables
        return new ArrayList<>();
    }

    private List<DashboardDTO.AlertDTO> getActiveAlerts(UUID propertyId) {
        // Implementation for alerts
        return new ArrayList<>();
    }

    private List<DashboardDTO.ActivityDTO> getRecentActivities(UUID propertyId) {
        // Implementation for activities
        return new ArrayList<>();
    }

    // Helper methods

    private String getDashboardName(String dashboardType) {
        return switch (dashboardType.toUpperCase()) {
            case "EXECUTIVE" -> "Executive Dashboard";
            case "OPERATIONAL" -> "Operational Dashboard";
            case "FINANCIAL" -> "Financial Dashboard";
            case "REVENUE" -> "Revenue Dashboard";
            case "OCCUPANCY" -> "Occupancy Dashboard";
            default -> "Analytics Dashboard";
        };
    }

    private BigDecimal calculateOverallDataQuality(UUID propertyId) {
        return dataQualityService.calculateOverallQuality(propertyId);
    }

    private BigDecimal calculateOverallQualityScore(List<AnalyticsResponseDTO.MetricResultDTO> metrics) {
        if (metrics.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = metrics.stream()
            .filter(m -> m.getQualityScore() != null)
            .map(AnalyticsResponseDTO.MetricResultDTO::getQualityScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int count = (int) metrics.stream()
            .filter(m -> m.getQualityScore() != null)
            .count();

        if (count == 0) {
            return BigDecimal.ZERO;
        }

        return sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOverallConfidenceScore(List<AnalyticsResponseDTO.MetricResultDTO> metrics) {
        if (metrics.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = metrics.stream()
            .filter(m -> m.getConfidenceScore() != null)
            .map(AnalyticsResponseDTO.MetricResultDTO::getConfidenceScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int count = (int) metrics.stream()
            .filter(m -> m.getConfidenceScore() != null)
            .count();

        if (count == 0) {
            return BigDecimal.ZERO;
        }

        return sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
    }

    private List<MetricType> getDefaultMetricTypes(AnalyticsRequestDTO request) {
        // Return default metrics based on request context
        return Arrays.asList(
            MetricType.TOTAL_REVENUE,
            MetricType.OCCUPANCY_RATE,
            MetricType.ADR,
            MetricType.REVPAR,
            MetricType.TOTAL_BOOKINGS
        );
    }

    private AnalyticsResponseDTO.ComparisonResultDTO calculateComparisonForRequest(AnalyticsRequestDTO request) {
        // Calculate previous period dates
        long periodDays = java.time.Duration.between(request.getPeriodStart(), request.getPeriodEnd()).toDays();
        LocalDateTime previousStart = request.getPeriodStart().minusDays(periodDays);
        LocalDateTime previousEnd = request.getPeriodStart();

        return calculateComparison(
            request.getMetricTypes(),
            request.getEffectivePropertyId(),
            request.getPeriodStart(),
            request.getPeriodEnd(),
            previousStart,
            previousEnd
        );
    }

    private AnalyticsResponseDTO.TrendAnalysisDTO calculateTrendsForRequest(AnalyticsRequestDTO request) {
        return trendAnalysisService.analyzeTrends(
            request.getMetricTypes(),
            request.getEffectivePropertyId(),
            request.getPeriodStart(),
            request.getPeriodEnd(),
            request.getTimeGranularity()
        );
    }

    private AnalyticsResponseDTO.ForecastResultDTO calculateForecastForRequest(AnalyticsRequestDTO request) {
        return forecastingService.generateForecast(
            request.getMetricTypes(),
            request.getEffectivePropertyId(),
            request.getPeriodStart(),
            request.getPeriodEnd(),
            request.getForecastPeriods()
        );
    }

    private AnalyticsResponseDTO.ComparisonResultDTO.MetricComparisonDTO mapToMetricComparison(Object[] data) {
        return AnalyticsResponseDTO.ComparisonResultDTO.MetricComparisonDTO.builder()
            .metricType((MetricType) data[0])
            .currentValue((BigDecimal) data[2])
            .previousValue((BigDecimal) data[3])
            .absoluteChange((BigDecimal) data[4])
            .build();
    }

    private List<String> generateRevenueInsights(List<AnalyticsMetric> revenueMetrics) {
        List<String> insights = new ArrayList<>();

        if (revenueMetrics.size() >= 2) {
            AnalyticsMetric latest = revenueMetrics.get(revenueMetrics.size() - 1);
            AnalyticsMetric previous = revenueMetrics.get(revenueMetrics.size() - 2);

            if (latest.getMetricValue() != null && previous.getMetricValue() != null) {
                BigDecimal change = latest.getMetricValue().subtract(previous.getMetricValue());
                if (change.compareTo(BigDecimal.ZERO) > 0) {
                    insights.add("Revenue increased by " + change + " compared to previous period");
                } else if (change.compareTo(BigDecimal.ZERO) < 0) {
                    insights.add("Revenue decreased by " + change.abs() + " compared to previous period");
                }
            }
        }

        return insights;
    }

    private List<String> generateOccupancyInsights(List<AnalyticsMetric> occupancyMetrics) {
        // Implementation for occupancy insights
        return new ArrayList<>();
    }

    private List<String> generateTrendInsights(UUID propertyId, LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for trend insights
        return new ArrayList<>();
    }

    private List<String> generateSeasonalInsights(UUID propertyId) {
        // Implementation for seasonal insights
        return new ArrayList<>();
    }

    private String generateCacheKey(AnalyticsRequestDTO request) {
        return String.format("analytics_%s_%s_%s_%s_%s",
            request.getTimeGranularity(),
            request.getPeriodStart(),
            request.getPeriodEnd(),
            request.getEffectivePropertyId(),
            request.getMetricTypes() != null ? request.getMetricTypes().hashCode() : "all"
        );
    }

    private void cacheResponse(AnalyticsRequestDTO request, AnalyticsResponseDTO response) {
        try {
            String cacheKey = generateCacheKey(request);
            int ttlMinutes = request.getCacheTtlMinutes() != null ? request.getCacheTtlMinutes() : 60;
            cacheService.put(cacheKey, response, ttlMinutes);
        } catch (Exception e) {
            log.warn("Error caching response: {}", e.getMessage());
        }
    }

    private void sendCallbackNotification(String callbackUrl, AnalyticsResponseDTO response) {
        // Implementation for callback notification
        log.info("Sending callback notification to: {}", callbackUrl);
    }
}
