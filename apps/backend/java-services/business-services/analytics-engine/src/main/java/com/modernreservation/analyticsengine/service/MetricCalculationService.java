package com.modernreservation.analyticsengine.service;

import com.modernreservation.analyticsengine.dto.AnalyticsResponseDTO;
import com.modernreservation.analyticsengine.entity.AnalyticsMetric;
import com.modernreservation.analyticsengine.enums.MetricType;
import com.modernreservation.analyticsengine.enums.TimeGranularity;
import com.modernreservation.analyticsengine.repository.AnalyticsMetricRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Metric Calculation Service
 *
 * Service responsible for calculating individual metrics, aggregations,
 * and performing complex metric computations with validation.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MetricCalculationService {

    private final AnalyticsMetricRepository analyticsMetricRepository;
    private final ExternalDataService externalDataService;
    private final ValidationService validationService;

    /**
     * Calculate a specific metric for the given parameters
     */
    @Transactional
    public AnalyticsResponseDTO.MetricResultDTO calculateMetric(
            MetricType metricType, UUID propertyId,
            LocalDateTime periodStart, LocalDateTime periodEnd,
            TimeGranularity granularity) {

        log.info("Calculating metric {} for property {} from {} to {}",
            metricType, propertyId, periodStart, periodEnd);

        try {
            // Get base data for calculation
            List<AnalyticsMetric> baseMetrics = getBaseMetricsForCalculation(
                metricType, propertyId, periodStart, periodEnd, granularity
            );

            // Perform calculation based on metric type
            BigDecimal calculatedValue = performMetricCalculation(metricType, baseMetrics,
                propertyId, periodStart, periodEnd);

            // Calculate quality and confidence scores
            BigDecimal qualityScore = calculateQualityScore(baseMetrics, metricType);
            BigDecimal confidenceScore = calculateConfidenceScore(baseMetrics, metricType, calculatedValue);

            // Format value
            String formattedValue = formatMetricValue(calculatedValue, metricType);

            // Create metric result
            AnalyticsResponseDTO.MetricResultDTO result = AnalyticsResponseDTO.MetricResultDTO.builder()
                .metricType(metricType)
                .value(calculatedValue)
                .formattedValue(formattedValue)
                .unit(getMetricUnit(metricType))
                .qualityScore(qualityScore)
                .confidenceScore(confidenceScore)
                .calculatedAt(LocalDateTime.now())
                .calculationMethod(getCalculationMethod(metricType))
                .dataPointsCount(baseMetrics.size())
                .notes(generateCalculationNotes(metricType, baseMetrics))
                .build();

            // Validate result
            if (validationService.isValidMetricResult(result)) {
                // Store calculated metric if valid
                storeCalculatedMetric(result, propertyId, periodStart, periodEnd, granularity);
                log.info("Successfully calculated metric {} with value {}", metricType, calculatedValue);
                return result;
            } else {
                log.warn("Calculated metric {} failed validation", metricType);
                return createErrorResult(metricType, "Metric validation failed");
            }

        } catch (Exception e) {
            log.error("Error calculating metric {}: {}", metricType, e.getMessage(), e);
            return createErrorResult(metricType, "Calculation error: " + e.getMessage());
        }
    }

    /**
     * Calculate aggregated metrics for multiple properties
     */
    public List<AnalyticsResponseDTO.MetricResultDTO> calculateAggregatedMetrics(
            List<MetricType> metricTypes, List<UUID> propertyIds,
            LocalDateTime periodStart, LocalDateTime periodEnd,
            TimeGranularity granularity, String aggregationType) {

        log.info("Calculating aggregated metrics for {} properties and {} metrics",
            propertyIds.size(), metricTypes.size());

        List<AnalyticsResponseDTO.MetricResultDTO> results = new ArrayList<>();

        for (MetricType metricType : metricTypes) {
            try {
                List<BigDecimal> propertyValues = new ArrayList<>();
                List<AnalyticsMetric> allMetrics = new ArrayList<>();

                // Collect values from all properties
                for (UUID propertyId : propertyIds) {
                    List<AnalyticsMetric> propertyMetrics = getBaseMetricsForCalculation(
                        metricType, propertyId, periodStart, periodEnd, granularity
                    );

                    if (!propertyMetrics.isEmpty()) {
                        BigDecimal propertyValue = performMetricCalculation(
                            metricType, propertyMetrics, propertyId, periodStart, periodEnd
                        );

                        if (propertyValue != null) {
                            propertyValues.add(propertyValue);
                            allMetrics.addAll(propertyMetrics);
                        }
                    }
                }

                // Calculate aggregated value
                if (!propertyValues.isEmpty()) {
                    BigDecimal aggregatedValue = calculateAggregation(propertyValues, aggregationType);

                    AnalyticsResponseDTO.MetricResultDTO result = AnalyticsResponseDTO.MetricResultDTO.builder()
                        .metricType(metricType)
                        .value(aggregatedValue)
                        .formattedValue(formatMetricValue(aggregatedValue, metricType))
                        .unit(getMetricUnit(metricType))
                        .qualityScore(calculateQualityScore(allMetrics, metricType))
                        .confidenceScore(calculateConfidenceScore(allMetrics, metricType, aggregatedValue))
                        .calculatedAt(LocalDateTime.now())
                        .calculationMethod("AGGREGATED_" + aggregationType.toUpperCase())
                        .dataPointsCount(allMetrics.size())
                        .notes("Aggregated from " + propertyIds.size() + " properties using " + aggregationType)
                        .build();

                    results.add(result);
                }

            } catch (Exception e) {
                log.error("Error calculating aggregated metric {}: {}", metricType, e.getMessage());
                results.add(createErrorResult(metricType, "Aggregation error: " + e.getMessage()));
            }
        }

        return results;
    }

    /**
     * Calculate moving averages for metrics
     */
    public List<AnalyticsResponseDTO.MetricResultDTO> calculateMovingAverages(
            MetricType metricType, UUID propertyId,
            LocalDateTime periodStart, LocalDateTime periodEnd,
            TimeGranularity granularity, int windowSize) {

        log.info("Calculating {}-period moving average for metric {}", windowSize, metricType);

        List<AnalyticsMetric> metrics = analyticsMetricRepository.findByMetricTypeAndPropertyAndDateRange(
            metricType, propertyId, periodStart, periodEnd
        ).stream()
        .filter(m -> m.getTimeGranularity() == granularity)
        .sorted(Comparator.comparing(AnalyticsMetric::getPeriodStart))
        .collect(Collectors.toList());

        List<AnalyticsResponseDTO.MetricResultDTO> results = new ArrayList<>();

        for (int i = windowSize - 1; i < metrics.size(); i++) {
            List<AnalyticsMetric> window = metrics.subList(i - windowSize + 1, i + 1);

            BigDecimal movingAverage = window.stream()
                .filter(m -> m.getMetricValue() != null)
                .map(AnalyticsMetric::getMetricValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(window.size()), 4, RoundingMode.HALF_UP);

            AnalyticsResponseDTO.MetricResultDTO result = AnalyticsResponseDTO.MetricResultDTO.builder()
                .metricType(metricType)
                .value(movingAverage)
                .formattedValue(formatMetricValue(movingAverage, metricType))
                .unit(getMetricUnit(metricType))
                .calculatedAt(LocalDateTime.now())
                .calculationMethod("MOVING_AVERAGE_" + windowSize)
                .dataPointsCount(window.size())
                .notes("Moving average over " + windowSize + " periods")
                .build();

            results.add(result);
        }

        return results;
    }

    /**
     * Calculate variance and standard deviation for metrics
     */
    public AnalyticsResponseDTO.StatisticalSummaryDTO calculateStatisticalAnalysis(
            MetricType metricType, UUID propertyId,
            LocalDateTime periodStart, LocalDateTime periodEnd,
            TimeGranularity granularity) {

        log.info("Calculating statistical analysis for metric {}", metricType);

        List<AnalyticsMetric> metrics = analyticsMetricRepository.findByMetricTypeAndPropertyAndDateRange(
            metricType, propertyId, periodStart, periodEnd
        ).stream()
        .filter(m -> m.getTimeGranularity() == granularity && m.getMetricValue() != null)
        .collect(Collectors.toList());

        if (metrics.isEmpty()) {
            return AnalyticsResponseDTO.StatisticalSummaryDTO.builder()
                .sampleSize(0)
                .build();
        }

        List<BigDecimal> values = metrics.stream()
            .map(AnalyticsMetric::getMetricValue)
            .collect(Collectors.toList());

        // Calculate statistical measures
        BigDecimal mean = calculateMean(values);
        BigDecimal median = calculateMedian(values);
        BigDecimal mode = calculateMode(values);
        BigDecimal variance = calculateVariance(values, mean);
        BigDecimal standardDeviation = calculateStandardDeviation(variance);
        BigDecimal min = values.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal range = max.subtract(min);

        // Calculate percentiles
        BigDecimal q1 = calculatePercentile(values, 25);
        BigDecimal q3 = calculatePercentile(values, 75);
        BigDecimal iqr = q3.subtract(q1);

        // Calculate skewness and kurtosis
        BigDecimal skewness = calculateSkewness(values, mean, standardDeviation);
        BigDecimal kurtosis = calculateKurtosis(values, mean, standardDeviation);

        return AnalyticsResponseDTO.StatisticalSummaryDTO.builder()
            .sampleSize(values.size())
            .mean(mean)
            .median(median)
            .mode(mode)
            .variance(variance)
            .standardDeviation(standardDeviation)
            .minimum(min)
            .maximum(max)
            .range(range)
            .q1(q1)
            .q3(q3)
            .iqr(iqr)
            .skewness(skewness)
            .kurtosis(kurtosis)
            .calculatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Get base metrics for calculation
     */
    private List<AnalyticsMetric> getBaseMetricsForCalculation(
            MetricType metricType, UUID propertyId,
            LocalDateTime periodStart, LocalDateTime periodEnd,
            TimeGranularity granularity) {

        // Check if we need to calculate derived metrics
        if (metricType.isDerived()) {
            return calculateDerivedMetricBase(metricType, propertyId, periodStart, periodEnd, granularity);
        }

        // Get stored metrics
        List<AnalyticsMetric> storedMetrics = analyticsMetricRepository.findByMetricTypeAndPropertyAndDateRange(
            metricType, propertyId, periodStart, periodEnd
        ).stream()
        .filter(m -> m.getTimeGranularity() == granularity)
        .collect(Collectors.toList());

        // If no stored metrics, get from external sources
        if (storedMetrics.isEmpty()) {
            return getMetricsFromExternalSources(metricType, propertyId, periodStart, periodEnd, granularity);
        }

        return storedMetrics;
    }

    /**
     * Perform metric calculation based on type
     */
    private BigDecimal performMetricCalculation(
            MetricType metricType, List<AnalyticsMetric> baseMetrics,
            UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {

        if (baseMetrics.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return switch (metricType.getCalculationStrategy()) {
            case SUM -> baseMetrics.stream()
                .filter(m -> m.getMetricValue() != null)
                .map(AnalyticsMetric::getMetricValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            case AVERAGE -> {
                List<BigDecimal> values = baseMetrics.stream()
                    .filter(m -> m.getMetricValue() != null)
                    .map(AnalyticsMetric::getMetricValue)
                    .collect(Collectors.toList());

                if (values.isEmpty()) {
                    yield BigDecimal.ZERO;
                }

                yield values.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
            }

            case LATEST -> baseMetrics.stream()
                .max(Comparator.comparing(AnalyticsMetric::getPeriodStart))
                .map(AnalyticsMetric::getMetricValue)
                .orElse(BigDecimal.ZERO);

            case COUNT -> BigDecimal.valueOf(baseMetrics.size());

            case PERCENTAGE -> calculatePercentageMetric(baseMetrics, metricType, propertyId, periodStart, periodEnd);

            case RATIO -> calculateRatioMetric(baseMetrics, metricType, propertyId, periodStart, periodEnd);

            case COMPLEX -> calculateComplexMetric(baseMetrics, metricType, propertyId, periodStart, periodEnd);

            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Calculate derived metric base data
     */
    private List<AnalyticsMetric> calculateDerivedMetricBase(
            MetricType metricType, UUID propertyId,
            LocalDateTime periodStart, LocalDateTime periodEnd,
            TimeGranularity granularity) {

        // Get component metrics based on derived metric type
        List<MetricType> componentTypes = metricType.getComponentMetrics();
        List<AnalyticsMetric> allComponents = new ArrayList<>();

        for (MetricType componentType : componentTypes) {
            List<AnalyticsMetric> componentMetrics = analyticsMetricRepository.findByMetricTypeAndPropertyAndDateRange(
                componentType, propertyId, periodStart, periodEnd
            ).stream()
            .filter(m -> m.getTimeGranularity() == granularity)
            .collect(Collectors.toList());

            allComponents.addAll(componentMetrics);
        }

        return allComponents;
    }

    /**
     * Get metrics from external sources
     */
    private List<AnalyticsMetric> getMetricsFromExternalSources(
            MetricType metricType, UUID propertyId,
            LocalDateTime periodStart, LocalDateTime periodEnd,
            TimeGranularity granularity) {

        try {
            return externalDataService.fetchMetrics(metricType, propertyId, periodStart, periodEnd, granularity);
        } catch (Exception e) {
            log.warn("Failed to fetch metrics from external sources: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Calculate percentage metric
     */
    private BigDecimal calculatePercentageMetric(
            List<AnalyticsMetric> baseMetrics, MetricType metricType,
            UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {

        // Implementation depends on specific percentage metric
        return switch (metricType) {
            case OCCUPANCY_RATE -> calculateOccupancyRate(propertyId, periodStart, periodEnd);
            case CANCELLATION_RATE -> calculateCancellationRate(propertyId, periodStart, periodEnd);
            case NO_SHOW_RATE -> calculateNoShowRate(propertyId, periodStart, periodEnd);
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Calculate ratio metric
     */
    private BigDecimal calculateRatioMetric(
            List<AnalyticsMetric> baseMetrics, MetricType metricType,
            UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {

        return switch (metricType) {
            case REVPAR -> calculateRevPAR(propertyId, periodStart, periodEnd);
            case ADR -> calculateADR(propertyId, periodStart, periodEnd);
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Calculate complex metric
     */
    private BigDecimal calculateComplexMetric(
            List<AnalyticsMetric> baseMetrics, MetricType metricType,
            UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {

        return switch (metricType) {
            case CUSTOMER_LIFETIME_VALUE -> calculateCLV(propertyId, periodStart, periodEnd);
            case GUEST_SATISFACTION_SCORE -> calculateGuestSatisfaction(propertyId, periodStart, periodEnd);
            default -> BigDecimal.ZERO;
        };
    }

    // Specific metric calculations

    private BigDecimal calculateOccupancyRate(UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        // Get occupied rooms and total rooms
        BigDecimal occupiedRooms = externalDataService.getOccupiedRoomsCount(propertyId, periodStart, periodEnd);
        BigDecimal totalRooms = externalDataService.getTotalRoomsCount(propertyId);

        if (totalRooms.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return occupiedRooms.divide(totalRooms, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateCancellationRate(UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        BigDecimal cancelledBookings = externalDataService.getCancelledBookingsCount(propertyId, periodStart, periodEnd);
        BigDecimal totalBookings = externalDataService.getTotalBookingsCount(propertyId, periodStart, periodEnd);

        if (totalBookings.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return cancelledBookings.divide(totalBookings, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateNoShowRate(UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        BigDecimal noShowBookings = externalDataService.getNoShowBookingsCount(propertyId, periodStart, periodEnd);
        BigDecimal totalBookings = externalDataService.getTotalBookingsCount(propertyId, periodStart, periodEnd);

        if (totalBookings.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return noShowBookings.divide(totalBookings, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateRevPAR(UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        BigDecimal totalRevenue = externalDataService.getTotalRevenue(propertyId, periodStart, periodEnd);
        BigDecimal totalRooms = externalDataService.getTotalRoomsCount(propertyId);
        long nights = Duration.between(periodStart, periodEnd).toDays();

        if (totalRooms.compareTo(BigDecimal.ZERO) == 0 || nights == 0) {
            return BigDecimal.ZERO;
        }

        return totalRevenue.divide(totalRooms.multiply(BigDecimal.valueOf(nights)), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateADR(UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        BigDecimal totalRevenue = externalDataService.getTotalRevenue(propertyId, periodStart, periodEnd);
        BigDecimal occupiedRooms = externalDataService.getOccupiedRoomsCount(propertyId, periodStart, periodEnd);

        if (occupiedRooms.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalRevenue.divide(occupiedRooms, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCLV(UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        // Simplified CLV calculation
        BigDecimal averageRevenue = externalDataService.getAverageRevenuePerGuest(propertyId, periodStart, periodEnd);
        BigDecimal averageStayFrequency = externalDataService.getAverageStayFrequency(propertyId, periodStart, periodEnd);
        BigDecimal averageLifespan = externalDataService.getAverageGuestLifespan(propertyId);

        return averageRevenue.multiply(averageStayFrequency).multiply(averageLifespan);
    }

    private BigDecimal calculateGuestSatisfaction(UUID propertyId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return externalDataService.getAverageGuestRating(propertyId, periodStart, periodEnd);
    }

    // Quality and confidence calculations

    private BigDecimal calculateQualityScore(List<AnalyticsMetric> metrics, MetricType metricType) {
        if (metrics.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Factors affecting quality: completeness, recency, accuracy
        double completenessScore = calculateCompleteness(metrics);
        double recencyScore = calculateRecency(metrics);
        double accuracyScore = calculateAccuracy(metrics);

        double overallScore = (completenessScore * 0.4) + (recencyScore * 0.3) + (accuracyScore * 0.3);

        return BigDecimal.valueOf(overallScore).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateConfidenceScore(List<AnalyticsMetric> metrics, MetricType metricType, BigDecimal calculatedValue) {
        if (metrics.isEmpty() || calculatedValue == null) {
            return BigDecimal.ZERO;
        }

        // Factors affecting confidence: sample size, variance, outliers
        double sampleSizeScore = calculateSampleSizeScore(metrics.size());
        double varianceScore = calculateVarianceScore(metrics);
        double outlierScore = calculateOutlierScore(metrics, calculatedValue);

        double overallConfidence = (sampleSizeScore * 0.4) + (varianceScore * 0.3) + (outlierScore * 0.3);

        return BigDecimal.valueOf(overallConfidence).setScale(4, RoundingMode.HALF_UP);
    }

    // Helper calculation methods

    private BigDecimal calculateAggregation(List<BigDecimal> values, String aggregationType) {
        return switch (aggregationType.toLowerCase()) {
            case "sum" -> values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            case "average", "mean" -> calculateMean(values);
            case "median" -> calculateMedian(values);
            case "min" -> values.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            case "max" -> values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            case "count" -> BigDecimal.valueOf(values.size());
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal calculateMean(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return values.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMedian(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> sorted = values.stream()
            .sorted()
            .collect(Collectors.toList());

        int size = sorted.size();
        if (size % 2 == 0) {
            return sorted.get(size / 2 - 1)
                .add(sorted.get(size / 2))
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        } else {
            return sorted.get(size / 2);
        }
    }

    private BigDecimal calculateMode(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<BigDecimal, Long> frequencies = values.stream()
            .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        return frequencies.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateVariance(List<BigDecimal> values, BigDecimal mean) {
        if (values.size() <= 1) {
            return BigDecimal.ZERO;
        }

        BigDecimal sumSquaredDiffs = values.stream()
            .map(value -> value.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sumSquaredDiffs.divide(BigDecimal.valueOf(values.size() - 1), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateStandardDeviation(BigDecimal variance) {
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
            .setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentile(List<BigDecimal> values, int percentile) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> sorted = values.stream()
            .sorted()
            .collect(Collectors.toList());

        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));

        return sorted.get(index);
    }

    private BigDecimal calculateSkewness(List<BigDecimal> values, BigDecimal mean, BigDecimal stdDev) {
        if (values.size() < 3 || stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal n = BigDecimal.valueOf(values.size());
        BigDecimal sumCubedDiffs = values.stream()
            .map(value -> value.subtract(mean).divide(stdDev, 4, RoundingMode.HALF_UP).pow(3))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sumCubedDiffs.divide(n, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateKurtosis(List<BigDecimal> values, BigDecimal mean, BigDecimal stdDev) {
        if (values.size() < 4 || stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal n = BigDecimal.valueOf(values.size());
        BigDecimal sumFourthPowers = values.stream()
            .map(value -> value.subtract(mean).divide(stdDev, 4, RoundingMode.HALF_UP).pow(4))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sumFourthPowers.divide(n, 4, RoundingMode.HALF_UP).subtract(BigDecimal.valueOf(3));
    }

    // Quality scoring methods

    private double calculateCompleteness(List<AnalyticsMetric> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        long validMetrics = metrics.stream()
            .filter(m -> m.getMetricValue() != null && m.getQualityScore() != null)
            .count();

        return (double) validMetrics / metrics.size();
    }

    private double calculateRecency(List<AnalyticsMetric> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();
        double totalRecencyScore = metrics.stream()
            .mapToDouble(m -> {
                long hoursOld = Duration.between(m.getCalculatedAt(), now).toHours();
                return Math.max(0.0, 1.0 - (hoursOld / 168.0)); // Decay over 1 week
            })
            .sum();

        return totalRecencyScore / metrics.size();
    }

    private double calculateAccuracy(List<AnalyticsMetric> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        return metrics.stream()
            .filter(m -> m.getQualityScore() != null)
            .mapToDouble(m -> m.getQualityScore().doubleValue())
            .average()
            .orElse(0.0);
    }

    private double calculateSampleSizeScore(int sampleSize) {
        // Scoring based on sample size adequacy
        if (sampleSize >= 100) return 1.0;
        if (sampleSize >= 30) return 0.8;
        if (sampleSize >= 10) return 0.6;
        if (sampleSize >= 5) return 0.4;
        if (sampleSize >= 1) return 0.2;
        return 0.0;
    }

    private double calculateVarianceScore(List<AnalyticsMetric> metrics) {
        if (metrics.size() < 2) {
            return 0.5; // Neutral score for insufficient data
        }

        List<BigDecimal> values = metrics.stream()
            .filter(m -> m.getMetricValue() != null)
            .map(AnalyticsMetric::getMetricValue)
            .collect(Collectors.toList());

        if (values.size() < 2) {
            return 0.5;
        }

        BigDecimal mean = calculateMean(values);
        BigDecimal variance = calculateVariance(values, mean);
        BigDecimal coefficientOfVariation = mean.compareTo(BigDecimal.ZERO) != 0 ?
            calculateStandardDeviation(variance).divide(mean, 4, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        // Lower variance = higher confidence
        double cv = coefficientOfVariation.doubleValue();
        return Math.max(0.0, 1.0 - Math.min(cv, 1.0));
    }

    private double calculateOutlierScore(List<AnalyticsMetric> metrics, BigDecimal calculatedValue) {
        if (metrics.size() < 3) {
            return 1.0; // Cannot detect outliers with insufficient data
        }

        List<BigDecimal> values = metrics.stream()
            .filter(m -> m.getMetricValue() != null)
            .map(AnalyticsMetric::getMetricValue)
            .collect(Collectors.toList());

        if (values.size() < 3) {
            return 1.0;
        }

        BigDecimal q1 = calculatePercentile(values, 25);
        BigDecimal q3 = calculatePercentile(values, 75);
        BigDecimal iqr = q3.subtract(q1);

        BigDecimal lowerBound = q1.subtract(iqr.multiply(BigDecimal.valueOf(1.5)));
        BigDecimal upperBound = q3.add(iqr.multiply(BigDecimal.valueOf(1.5)));

        // Check if calculated value is within normal range
        if (calculatedValue.compareTo(lowerBound) >= 0 && calculatedValue.compareTo(upperBound) <= 0) {
            return 1.0;
        } else {
            return 0.5; // Penalize outliers
        }
    }

    // Utility methods

    private String getCalculationMethod(MetricType metricType) {
        return switch (metricType.getCalculationStrategy()) {
            case SUM -> "SUMMATION";
            case AVERAGE -> "ARITHMETIC_MEAN";
            case LATEST -> "LATEST_VALUE";
            case COUNT -> "COUNT_AGGREGATION";
            case PERCENTAGE -> "PERCENTAGE_CALCULATION";
            case RATIO -> "RATIO_CALCULATION";
            case COMPLEX -> "COMPLEX_ALGORITHM";
            default -> "UNKNOWN";
        };
    }

    private String getMetricUnit(MetricType metricType) {
        String category = metricType.getCategory();
        return switch (category) {
            case "REVENUE", "FINANCIAL" -> "USD";
            case "OCCUPANCY" -> "%";
            case "BOOKING" -> "count";
            case "CUSTOMER" -> "score";
            case "CHANNEL" -> "count";
            case "MARKET" -> "index";
            default -> "value";
        };
    }

    private String formatMetricValue(BigDecimal value, MetricType metricType) {
        if (value == null) {
            return "N/A";
        }

        String category = metricType.getCategory();
        return switch (category) {
            case "REVENUE", "FINANCIAL" -> String.format("$%,.2f", value);
            case "OCCUPANCY" -> String.format("%.1f%%", value);
            case "BOOKING", "CUSTOMER", "CHANNEL" -> String.format("%,d", value.intValue());
            case "MARKET" -> String.format("%.2f", value);
            default -> value.toString();
        };
    }

    private String generateCalculationNotes(MetricType metricType, List<AnalyticsMetric> baseMetrics) {
        StringBuilder notes = new StringBuilder();
        notes.append("Calculated using ").append(baseMetrics.size()).append(" data points. ");

        if (metricType.isDerived()) {
            notes.append("Derived metric calculated from component metrics. ");
        }

        if (baseMetrics.stream().anyMatch(m -> m.getQualityScore() != null &&
                                                m.getQualityScore().compareTo(BigDecimal.valueOf(0.8)) < 0)) {
            notes.append("Some data points have lower quality scores. ");
        }

        return notes.toString().trim();
    }

    private AnalyticsResponseDTO.MetricResultDTO createErrorResult(MetricType metricType, String errorMessage) {
        return AnalyticsResponseDTO.MetricResultDTO.builder()
            .metricType(metricType)
            .value(null)
            .formattedValue("Error")
            .unit(getMetricUnit(metricType))
            .qualityScore(BigDecimal.ZERO)
            .confidenceScore(BigDecimal.ZERO)
            .calculatedAt(LocalDateTime.now())
            .calculationMethod("ERROR")
            .dataPointsCount(0)
            .notes(errorMessage)
            .build();
    }

    private void storeCalculatedMetric(
            AnalyticsResponseDTO.MetricResultDTO result, UUID propertyId,
            LocalDateTime periodStart, LocalDateTime periodEnd,
            TimeGranularity granularity) {

        try {
            AnalyticsMetric metric = AnalyticsMetric.builder()
                .metricId(UUID.randomUUID())
                .metricType(result.getMetricType())
                .propertyId(propertyId)
                .metricValue(result.getValue())
                .timeGranularity(granularity)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .calculatedAt(LocalDateTime.now())
                .qualityScore(result.getQualityScore())
                .confidenceScore(result.getConfidenceScore())
                .formattedValue(result.getFormattedValue())
                .calculationMethod(result.getCalculationMethod())
                .dataPointsCount(result.getDataPoints() != null ? result.getDataPoints().size() : 0)
                .notes(result.getNotes())
                .build();

            analyticsMetricRepository.save(metric);
            log.debug("Stored calculated metric: {}", metric.getMetricId());

        } catch (Exception e) {
            log.warn("Failed to store calculated metric: {}", e.getMessage());
        }
    }
}
