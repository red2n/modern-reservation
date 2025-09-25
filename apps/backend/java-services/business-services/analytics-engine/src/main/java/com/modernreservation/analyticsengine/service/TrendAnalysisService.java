package com.modernreservation.analyticsengine.service;

import com.modernreservation.analyticsengine.dto.AnalyticsResponseDTO;
import com.modernreservation.analyticsengine.entity.AnalyticsMetric;
import com.modernreservation.analyticsengine.enums.MetricType;
import com.modernreservation.analyticsengine.enums.TimeGranularity;
import com.modernreservation.analyticsengine.repository.AnalyticsMetricRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Trend Analysis Service
 *
 * Service for analyzing trends, patterns, and statistical relationships
 * in analytics data over time periods.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrendAnalysisService {

    private final AnalyticsMetricRepository analyticsMetricRepository;

    /**
     * Analyze trends for multiple metrics
     */
    public AnalyticsResponseDTO.TrendAnalysisDTO analyzeTrends(
            List<MetricType> metricTypes, UUID propertyId,
            LocalDateTime periodStart, LocalDateTime periodEnd,
            TimeGranularity granularity) {

        log.info("Analyzing trends for {} metrics", metricTypes.size());

        Map<MetricType, List<AnalyticsMetric>> metricData = new HashMap<>();

        // Collect data for all metrics
        for (MetricType metricType : metricTypes) {
            List<AnalyticsMetric> metrics = analyticsMetricRepository.findByMetricTypeAndPropertyAndDateRange(
                metricType, propertyId, periodStart, periodEnd
            ).stream()
            .filter(m -> m.getTimeGranularity() == granularity)
            .sorted(Comparator.comparing(AnalyticsMetric::getPeriodStart))
            .collect(Collectors.toList());

            metricData.put(metricType, metrics);
        }

        // Analyze trends for each metric
        List<AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO> metricTrends = new ArrayList<>();

        for (Map.Entry<MetricType, List<AnalyticsMetric>> entry : metricData.entrySet()) {
            MetricType metricType = entry.getKey();
            List<AnalyticsMetric> metrics = entry.getValue();

            if (!metrics.isEmpty()) {
                AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO trend = analyzeMetricTrend(metricType, metrics);
                metricTrends.add(trend);
            }
        }

        // Calculate overall trend summary
        String overallTrend = calculateOverallTrend(metricTrends);
        BigDecimal trendStrength = calculateTrendStrength(metricTrends);

        return AnalyticsResponseDTO.TrendAnalysisDTO.builder()
            .analysisId(UUID.randomUUID())
            .metricTrends(metricTrends)
            .overallTrend(overallTrend)
            .trendStrength(trendStrength)
            .analysisDate(LocalDateTime.now())
            .periodCovered(java.time.Duration.between(periodStart, periodEnd).toDays())
            .granularity(granularity)
            .confidence(calculateTrendConfidence(metricTrends))
            .insights(generateTrendInsights(metricTrends))
            .build();
    }

    /**
     * Analyze trend for a single metric
     */
    private AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO analyzeMetricTrend(
            MetricType metricType, List<AnalyticsMetric> metrics) {

        if (metrics.size() < 2) {
            return AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO.builder()
                .metricType(metricType)
                .trendDirection("INSUFFICIENT_DATA")
                .trendStrength(BigDecimal.ZERO)
                .significance(BigDecimal.ZERO)
                .build();
        }

        // Extract values and timestamps
        List<BigDecimal> values = metrics.stream()
            .filter(m -> m.getMetricValue() != null)
            .map(AnalyticsMetric::getMetricValue)
            .collect(Collectors.toList());

        if (values.size() < 2) {
            return AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO.builder()
                .metricType(metricType)
                .trendDirection("NO_DATA")
                .trendStrength(BigDecimal.ZERO)
                .significance(BigDecimal.ZERO)
                .build();
        }

        // Calculate linear regression
        LinearRegressionResult regression = calculateLinearRegression(values);

        // Determine trend direction
        String trendDirection = determineTrendDirection(regression.slope);

        // Calculate trend strength (R-squared)
        BigDecimal trendStrength = regression.rSquared;

        // Calculate statistical significance
        BigDecimal significance = calculateStatisticalSignificance(regression, values.size());

        // Calculate percentage change
        BigDecimal firstValue = values.get(0);
        BigDecimal lastValue = values.get(values.size() - 1);
        BigDecimal percentageChange = firstValue.compareTo(BigDecimal.ZERO) != 0 ?
            lastValue.subtract(firstValue)
                .divide(firstValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) :
            BigDecimal.ZERO;

        // Detect seasonality
        SeasonalityResult seasonality = detectSeasonality(values);

        // Find outliers
        List<Integer> outlierIndices = findOutliers(values);

        return AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO.builder()
            .metricType(metricType)
            .trendDirection(trendDirection)
            .trendStrength(trendStrength)
            .slope(regression.slope)
            .intercept(regression.intercept)
            .rSquared(regression.rSquared)
            .significance(significance)
            .percentageChange(percentageChange)
            .averageValue(calculateMean(values))
            .volatility(calculateVolatility(values))
            .seasonalityDetected(seasonality.detected)
            .seasonalityStrength(seasonality.strength)
            .outliersCount(outlierIndices.size())
            .dataPoints(values.size())
            .confidenceInterval(calculateConfidenceInterval(regression, values))
            .build();
    }

    /**
     * Calculate linear regression
     */
    private LinearRegressionResult calculateLinearRegression(List<BigDecimal> values) {
        int n = values.size();

        // Create x values (time indices)
        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            x.add((double) i);
            y.add(values.get(i).doubleValue());
        }

        // Calculate means
        double xMean = x.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double yMean = y.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Calculate slope and intercept
        double numerator = 0.0;
        double denominator = 0.0;
        double totalSumSquares = 0.0;
        double residualSumSquares = 0.0;

        for (int i = 0; i < n; i++) {
            double xi = x.get(i);
            double yi = y.get(i);

            numerator += (xi - xMean) * (yi - yMean);
            denominator += (xi - xMean) * (xi - xMean);
            totalSumSquares += (yi - yMean) * (yi - yMean);
        }

        double slope = denominator != 0 ? numerator / denominator : 0.0;
        double intercept = yMean - slope * xMean;

        // Calculate R-squared
        for (int i = 0; i < n; i++) {
            double predicted = slope * x.get(i) + intercept;
            double actual = y.get(i);
            residualSumSquares += (actual - predicted) * (actual - predicted);
        }

        double rSquared = totalSumSquares != 0 ? 1.0 - (residualSumSquares / totalSumSquares) : 0.0;

        return new LinearRegressionResult(
            BigDecimal.valueOf(slope),
            BigDecimal.valueOf(intercept),
            BigDecimal.valueOf(Math.max(0.0, rSquared))
        );
    }

    /**
     * Determine trend direction based on slope
     */
    private String determineTrendDirection(BigDecimal slope) {
        BigDecimal threshold = BigDecimal.valueOf(0.01);

        if (slope.compareTo(threshold) > 0) {
            return "INCREASING";
        } else if (slope.compareTo(threshold.negate()) < 0) {
            return "DECREASING";
        } else {
            return "STABLE";
        }
    }

    /**
     * Calculate statistical significance
     */
    private BigDecimal calculateStatisticalSignificance(LinearRegressionResult regression, int dataPoints) {
        if (dataPoints < 3) {
            return BigDecimal.ZERO;
        }

        // Simplified t-test calculation
        double standardError = Math.sqrt((1.0 - regression.rSquared.doubleValue()) / (dataPoints - 2));
        double tStatistic = Math.abs(regression.slope.doubleValue()) / standardError;

        // Convert t-statistic to p-value (simplified)
        double pValue = Math.max(0.0, Math.min(1.0, 2.0 * (1.0 - tStatistic / 10.0)));

        return BigDecimal.valueOf(1.0 - pValue).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Detect seasonality in the data
     */
    private SeasonalityResult detectSeasonality(List<BigDecimal> values) {
        if (values.size() < 12) {
            return new SeasonalityResult(false, BigDecimal.ZERO);
        }

        // Simple seasonality detection using autocorrelation
        List<Double> doubleValues = values.stream()
            .map(BigDecimal::doubleValue)
            .collect(Collectors.toList());

        // Check for weekly (7-day) and monthly (30-day) patterns
        double weeklyCorr = calculateAutocorrelation(doubleValues, 7);
        double monthlyCorr = calculateAutocorrelation(doubleValues, 30);

        double maxCorrelation = Math.max(weeklyCorr, monthlyCorr);
        boolean detected = maxCorrelation > 0.3; // Threshold for seasonality

        return new SeasonalityResult(detected, BigDecimal.valueOf(maxCorrelation));
    }

    /**
     * Calculate autocorrelation at a given lag
     */
    private double calculateAutocorrelation(List<Double> values, int lag) {
        if (values.size() <= lag) {
            return 0.0;
        }

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double numerator = 0.0;
        double denominator = 0.0;

        for (int i = lag; i < values.size(); i++) {
            numerator += (values.get(i) - mean) * (values.get(i - lag) - mean);
        }

        for (double value : values) {
            denominator += (value - mean) * (value - mean);
        }

        return denominator != 0 ? numerator / denominator : 0.0;
    }

    /**
     * Find outliers using IQR method
     */
    private List<Integer> findOutliers(List<BigDecimal> values) {
        if (values.size() < 4) {
            return Collections.emptyList();
        }

        List<BigDecimal> sorted = values.stream()
            .sorted()
            .collect(Collectors.toList());

        int n = sorted.size();
        BigDecimal q1 = sorted.get(n / 4);
        BigDecimal q3 = sorted.get(3 * n / 4);
        BigDecimal iqr = q3.subtract(q1);

        BigDecimal lowerBound = q1.subtract(iqr.multiply(BigDecimal.valueOf(1.5)));
        BigDecimal upperBound = q3.add(iqr.multiply(BigDecimal.valueOf(1.5)));

        List<Integer> outlierIndices = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            BigDecimal value = values.get(i);
            if (value.compareTo(lowerBound) < 0 || value.compareTo(upperBound) > 0) {
                outlierIndices.add(i);
            }
        }

        return outlierIndices;
    }

    /**
     * Calculate mean of values
     */
    private BigDecimal calculateMean(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return values.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculate volatility (standard deviation)
     */
    private BigDecimal calculateVolatility(List<BigDecimal> values) {
        if (values.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal mean = calculateMean(values);

        BigDecimal sumSquaredDiffs = values.stream()
            .map(value -> value.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal variance = sumSquaredDiffs.divide(BigDecimal.valueOf(values.size() - 1), 4, RoundingMode.HALF_UP);

        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Calculate confidence interval for regression
     */
    private List<BigDecimal> calculateConfidenceInterval(LinearRegressionResult regression, List<BigDecimal> values) {
        // Simplified 95% confidence interval
        BigDecimal margin = calculateVolatility(values).multiply(BigDecimal.valueOf(1.96));

        return Arrays.asList(
            regression.slope.subtract(margin),
            regression.slope.add(margin)
        );
    }

    /**
     * Calculate overall trend across all metrics
     */
    private String calculateOverallTrend(List<AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO> metricTrends) {
        if (metricTrends.isEmpty()) {
            return "NO_DATA";
        }

        Map<String, Long> trendCounts = metricTrends.stream()
            .collect(Collectors.groupingBy(
                AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO::getTrendDirection,
                Collectors.counting()
            ));

        return trendCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("MIXED");
    }

    /**
     * Calculate overall trend strength
     */
    private BigDecimal calculateTrendStrength(List<AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO> metricTrends) {
        if (metricTrends.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return metricTrends.stream()
            .filter(trend -> trend.getTrendStrength() != null)
            .map(AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO::getTrendStrength)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(metricTrends.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculate trend confidence
     */
    private BigDecimal calculateTrendConfidence(List<AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO> metricTrends) {
        if (metricTrends.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return metricTrends.stream()
            .filter(trend -> trend.getSignificance() != null)
            .map(AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO::getSignificance)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(metricTrends.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Generate trend insights
     */
    private List<String> generateTrendInsights(List<AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO> metricTrends) {
        List<String> insights = new ArrayList<>();

        // Find strongest trends
        metricTrends.stream()
            .filter(trend -> trend.getTrendStrength() != null)
            .filter(trend -> trend.getTrendStrength().compareTo(BigDecimal.valueOf(0.7)) > 0)
            .forEach(trend -> {
                insights.add(String.format("%s shows a strong %s trend with R² = %.2f",
                    trend.getMetricType().name(),
                    trend.getTrendDirection().toLowerCase(),
                    trend.getTrendStrength().doubleValue()
                ));
            });

        // Find metrics with high volatility
        metricTrends.stream()
            .filter(trend -> trend.getVolatility() != null)
            .filter(trend -> trend.getVolatility().compareTo(BigDecimal.valueOf(0.3)) > 0)
            .forEach(trend -> {
                insights.add(String.format("%s shows high volatility (σ = %.2f)",
                    trend.getMetricType().name(),
                    trend.getVolatility().doubleValue()
                ));
            });

        // Find seasonal patterns
        metricTrends.stream()
            .filter(AnalyticsResponseDTO.TrendAnalysisDTO.MetricTrendDTO::getSeasonalityDetected)
            .forEach(trend -> {
                insights.add(String.format("%s exhibits seasonal patterns",
                    trend.getMetricType().name()
                ));
            });

        // Find metrics with outliers
        metricTrends.stream()
            .filter(trend -> trend.getOutliersCount() != null && trend.getOutliersCount() > 0)
            .forEach(trend -> {
                insights.add(String.format("%s has %d outlier(s) detected",
                    trend.getMetricType().name(),
                    trend.getOutliersCount()
                ));
            });

        if (insights.isEmpty()) {
            insights.add("No significant trends or patterns detected in the analyzed period");
        }

        return insights;
    }

    // Helper classes

    private static class LinearRegressionResult {
        final BigDecimal slope;
        final BigDecimal intercept;
        final BigDecimal rSquared;

        LinearRegressionResult(BigDecimal slope, BigDecimal intercept, BigDecimal rSquared) {
            this.slope = slope;
            this.intercept = intercept;
            this.rSquared = rSquared;
        }
    }

    private static class SeasonalityResult {
        final boolean detected;
        final BigDecimal strength;

        SeasonalityResult(boolean detected, BigDecimal strength) {
            this.detected = detected;
            this.strength = strength;
        }
    }
}
