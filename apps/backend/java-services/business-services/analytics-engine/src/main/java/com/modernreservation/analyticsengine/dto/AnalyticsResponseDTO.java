package com.modernreservation.analyticsengine.dto;

import com.modernreservation.analyticsengine.enums.MetricType;
import com.modernreservation.analyticsengine.enums.TimeGranularity;
import com.modernreservation.analyticsengine.enums.AnalyticsStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Analytics Response DTO
 *
 * Response object containing calculated analytics metrics, reports, and metadata.
 * Supports both synchronous and asynchronous response patterns.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponseDTO {

    /**
     * Unique identifier for this response/calculation
     */
    private UUID responseId;

    /**
     * Original request ID for correlation
     */
    private UUID requestId;

    /**
     * Processing status
     */
    private AnalyticsStatus status;

    /**
     * Time granularity used for calculations
     */
    private TimeGranularity timeGranularity;

    /**
     * Analysis period start
     */
    private LocalDateTime periodStart;

    /**
     * Analysis period end
     */
    private LocalDateTime periodEnd;

    /**
     * Timestamp when calculation was completed
     */
    private LocalDateTime calculatedAt;

    /**
     * Duration of calculation in milliseconds
     */
    private Long calculationDurationMs;

    /**
     * Overall quality score of the results (0.0 to 1.0)
     */
    private BigDecimal overallQualityScore;

    /**
     * Data completeness percentage (0.0 to 100.0)
     */
    private BigDecimal dataCompletenessPercentage;

    /**
     * Overall confidence score (0.0 to 1.0)
     */
    private BigDecimal overallConfidenceScore;

    /**
     * Calculated metrics
     */
    private List<MetricResultDTO> metrics;

    /**
     * Comparison data (if comparison was requested)
     */
    private ComparisonResultDTO comparison;

    /**
     * Trend analysis (if trends were requested)
     */
    private TrendAnalysisDTO trendAnalysis;

    /**
     * Forecast data (if forecasting was requested)
     */
    private ForecastResultDTO forecast;

    /**
     * Breakdown by dimensions (if breakdown was requested)
     */
    private Map<String, List<MetricResultDTO>> breakdown;

    /**
     * Statistical measures (if statistics were requested)
     */
    private StatisticalSummaryDTO statistics;

    /**
     * Data quality metrics (if quality metrics were requested)
     */
    private DataQualityDTO dataQuality;

    /**
     * Summary statistics for quick overview
     */
    private Map<String, String> summaryStatistics;

    /**
     * Metadata about the calculation
     */
    private CalculationMetadataDTO metadata;

    /**
     * Warning messages (if any)
     */
    private List<String> warnings;

    /**
     * Error message (if calculation failed)
     */
    private String errorMessage;

    /**
     * Total number of data points processed
     */
    private Integer totalDataPoints;

    /**
     * Number of successful metric calculations
     */
    private Integer successfulMetrics;

    /**
     * Number of failed metric calculations
     */
    private Integer failedMetrics;

    /**
     * Cache information
     */
    private CacheInfoDTO cacheInfo;

    /**
     * Pagination information (if applicable)
     */
    private PaginationDTO pagination;

    /**
     * Links for related resources or actions
     */
    private Map<String, String> links;

    /**
     * Metric Result DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricResultDTO {
        private MetricType metricType;
        private BigDecimal value;
        private Long countValue;
        private BigDecimal percentageValue;
        private String currencyCode;
        private BigDecimal confidenceScore;
        private BigDecimal qualityScore;
        private String trendDirection;
        private BigDecimal varianceFromBaseline;
        private BigDecimal varianceFromTarget;
        private Map<String, String> dimensions;
        private Map<String, String> metadata;
        private LocalDateTime calculatedAt;
        private String formattedValue;
        private String unit;
        private String description;
        private String calculationMethod;
        private List<Object> dataPoints;
        private String notes;
    }

    /**
     * Comparison Result DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonResultDTO {
        private LocalDateTime comparisonPeriodStart;
        private LocalDateTime comparisonPeriodEnd;
        private List<MetricResultDTO> comparisonMetrics;
        private List<MetricComparisonDTO> comparisons;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MetricComparisonDTO {
            private MetricType metricType;
            private BigDecimal currentValue;
            private BigDecimal previousValue;
            private BigDecimal absoluteChange;
            private BigDecimal percentageChange;
            private String changeDirection;
            private String significance;
            private String interpretation;
        }
    }

    /**
     * Trend Analysis DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendAnalysisDTO {
        private UUID analysisId;
        private List<TrendPointDTO> trendPoints;
        private String overallTrend;
        private BigDecimal trendStrength;
        private BigDecimal correlation;
        private String seasonalityPattern;
        private List<String> insights;
        private List<MetricTrendDTO> metricTrends;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TrendPointDTO {
            private LocalDateTime timestamp;
            private BigDecimal value;
            private BigDecimal trendValue;
            private BigDecimal seasonalAdjustment;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MetricTrendDTO {
            private MetricType metricType;
            private String metricName;
            private Double trendValue;
            private String trendDirection;
            private Double slope;
            private Double correlation;
            private LocalDateTime timestamp;
            private Double trendStrength;
            private Double significance;
            private Double volatility;
            private Boolean seasonalityDetected;
            private Integer outliersCount;
        }
    }

    /**
     * Forecast Result DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastResultDTO {
        private UUID forecastId;
        private List<ForecastPointDTO> forecastPoints;
        private String forecastMethod;
        private BigDecimal accuracy;
        private BigDecimal confidenceInterval;
        private Map<String, BigDecimal> modelParameters;
        private List<String> assumptions;
        private List<MetricForecastDTO> metricForecasts;
        private ConfidenceIntervalDTO confidenceIntervals;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ForecastPointDTO {
            private LocalDateTime timestamp;
            private BigDecimal forecastValue;
            private BigDecimal lowerBound;
            private BigDecimal upperBound;
            private BigDecimal confidence;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MetricForecastDTO {
            private MetricType metricType;
            private String metricName;
            private BigDecimal forecastValue;
            private LocalDateTime forecastDate;
            private Double confidence;
            private Double accuracy;
            private String forecastMethod;
            private Map<String, Object> forecastMetadata;
            private List<Object> historicalDataPoints;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ConfidenceIntervalDTO {
            private Double lowerBound;
            private Double upperBound;
            private Double confidence;
            private String intervalType;
            private Integer period;
        }
    }

    /**
     * Statistical Summary DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticalSummaryDTO {
        private BigDecimal mean;
        private BigDecimal median;
        private BigDecimal mode;
        private BigDecimal standardDeviation;
        private BigDecimal variance;
        private BigDecimal skewness;
        private BigDecimal kurtosis;
        private BigDecimal minimum;
        private BigDecimal maximum;
        private BigDecimal range;
        private BigDecimal q1;
        private BigDecimal q3;
        private BigDecimal iqr;
        private List<BigDecimal> outliers;
        private Integer sampleSize;
    }

    /**
     * Data Quality DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataQualityDTO {
        private BigDecimal completenessScore;
        private BigDecimal accuracyScore;
        private BigDecimal consistencyScore;
        private BigDecimal timelinessScore;
        private BigDecimal validityScore;
        private Integer missingDataPoints;
        private Integer invalidDataPoints;
        private Integer duplicateDataPoints;
        private List<String> qualityIssues;
        private Map<String, Integer> issueBreakdown;
    }

    /**
     * Calculation Metadata DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculationMetadataDTO {
        private String calculationMethod;
        private String version;
        private List<String> dataSources;
        private Map<String, String> parameters;
        private LocalDateTime dataAsOfTime;
        private String computeEnvironment;
        private Integer cpuCores;
        private Long memoryUsedMb;
        private String cacheStrategy;
    }

    /**
     * Cache Information DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheInfoDTO {
        private Boolean fromCache;
        private LocalDateTime cachedAt;
        private LocalDateTime expiresAt;
        private String cacheKey;
        private Integer ttlMinutes;
        private String cacheHitRate;
    }

    /**
     * Pagination DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationDTO {
        private Integer page;
        private Integer size;
        private Integer totalElements;
        private Integer totalPages;
        private Boolean hasNext;
        private Boolean hasPrevious;
        private String nextPageUrl;
        private String previousPageUrl;
    }

    // Business Logic Methods

    /**
     * Check if calculation was successful
     */
    public boolean isSuccessful() {
        return status != null && status.isSuccess();
    }

    /**
     * Check if response has warnings
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Check if response has errors
     */
    public boolean hasErrors() {
        return status != null && status.isError();
    }

    /**
     * Get metric by type
     */
    public MetricResultDTO getMetric(MetricType metricType) {
        if (metrics == null) {
            return null;
        }
        return metrics.stream()
                     .filter(m -> m.getMetricType() == metricType)
                     .findFirst()
                     .orElse(null);
    }

    /**
     * Get metric value by type
     */
    public BigDecimal getMetricValue(MetricType metricType) {
        MetricResultDTO metric = getMetric(metricType);
        return metric != null ? metric.getValue() : null;
    }

    /**
     * Get formatted summary
     */
    public String getFormattedSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append("Analytics Results Summary:\n");
        summary.append("Period: ").append(periodStart.toLocalDate())
               .append(" to ").append(periodEnd.toLocalDate()).append("\n");
        summary.append("Status: ").append(status.getDisplayName()).append("\n");

        if (isSuccessful()) {
            summary.append("Metrics Calculated: ").append(successfulMetrics).append("\n");
            summary.append("Quality Score: ").append(overallQualityScore).append("\n");
            summary.append("Confidence Score: ").append(overallConfidenceScore).append("\n");
        }

        if (hasWarnings()) {
            summary.append("Warnings: ").append(warnings.size()).append("\n");
        }

        return summary.toString();
    }

    /**
     * Get success rate
     */
    public BigDecimal getSuccessRate() {
        if (successfulMetrics == null || failedMetrics == null) {
            return null;
        }

        int total = successfulMetrics + failedMetrics;
        if (total == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(successfulMetrics)
                         .divide(BigDecimal.valueOf(total), 4, BigDecimal.ROUND_HALF_UP)
                         .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Add warning message
     */
    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new java.util.ArrayList<>();
        }
        warnings.add(warning);
    }

    /**
     * Add summary statistic
     */
    public void addSummaryStatistic(String key, String value) {
        if (summaryStatistics == null) {
            summaryStatistics = new java.util.HashMap<>();
        }
        summaryStatistics.put(key, value);
    }

    /**
     * Add link
     */
    public void addLink(String rel, String href) {
        if (links == null) {
            links = new java.util.HashMap<>();
        }
        links.put(rel, href);
    }

    /**
     * Check if response is cached
     */
    public boolean isCached() {
        return cacheInfo != null && Boolean.TRUE.equals(cacheInfo.getFromCache());
    }

    /**
     * Check if forecast is available
     */
    public boolean hasForecast() {
        return forecast != null && forecast.getForecastPoints() != null &&
               !forecast.getForecastPoints().isEmpty();
    }

    /**
     * Check if trend analysis is available
     */
    public boolean hasTrendAnalysis() {
        return trendAnalysis != null && trendAnalysis.getTrendPoints() != null &&
               !trendAnalysis.getTrendPoints().isEmpty();
    }

    /**
     * Check if comparison is available
     */
    public boolean hasComparison() {
        return comparison != null && comparison.getComparisons() != null &&
               !comparison.getComparisons().isEmpty();
    }

    /**
     * Get period duration in days
     */
    public long getPeriodDurationDays() {
        if (periodStart == null || periodEnd == null) {
            return 0;
        }
        return java.time.Duration.between(periodStart, periodEnd).toDays();
    }

    /**
     * Get calculation speed (data points per second)
     */
    public Double getCalculationSpeed() {
        if (calculationDurationMs == null || totalDataPoints == null ||
            calculationDurationMs == 0) {
            return null;
        }

        double seconds = calculationDurationMs / 1000.0;
        return totalDataPoints / seconds;
    }

    /**
     * Create error response
     */
    public static AnalyticsResponseDTO createErrorResponse(UUID requestId, String errorMessage) {
        return AnalyticsResponseDTO.builder()
                                  .responseId(UUID.randomUUID())
                                  .requestId(requestId)
                                  .status(AnalyticsStatus.FAILED)
                                  .errorMessage(errorMessage)
                                  .calculatedAt(LocalDateTime.now())
                                  .build();
    }

    /**
     * Create pending response for async processing
     */
    public static AnalyticsResponseDTO createPendingResponse(UUID requestId) {
        return AnalyticsResponseDTO.builder()
                                  .responseId(UUID.randomUUID())
                                  .requestId(requestId)
                                  .status(AnalyticsStatus.PENDING)
                                  .calculatedAt(LocalDateTime.now())
                                  .build();
    }

    /**
     * Statistical analysis DTO for advanced analytics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticalAnalysisDTO {
        private Double mean;
        private Double median;
        private Double standardDeviation;
        private Double variance;
        private Double correlation;
        private Double regression;
        private Map<String, Double> distribution;
        private Integer sampleSize;
    }

    /**
     * Metric trend DTO for trend analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricTrendDTO {
        private String metricName;
        private Double trendValue;
        private String trendDirection;
        private Double slope;
        private Double correlation;
        private LocalDateTime timestamp;
    }

    /**
     * Metric forecast DTO for forecasting
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricForecastDTO {
        private String metricName;
        private BigDecimal forecastValue;
        private LocalDateTime forecastDate;
        private Double confidence;
        private String forecastMethod;
        private Map<String, Object> forecastMetadata;
    }

    /**
     * Confidence interval DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfidenceIntervalDTO {
        private Double lowerBound;
        private Double upperBound;
        private Double confidence;
        private String intervalType;
    }
}
