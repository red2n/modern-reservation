package com.modernreservation.analyticsengine.service;

import com.modernreservation.analyticsengine.dto.AnalyticsResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Validation Service for Analytics
 *
 * Service for validating analytics data, calculations, and results
 * to ensure data quality and accuracy.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class ValidationService {

    private static final BigDecimal MIN_QUALITY_THRESHOLD = BigDecimal.valueOf(0.5);
    private static final BigDecimal MIN_CONFIDENCE_THRESHOLD = BigDecimal.valueOf(0.6);

    /**
     * Validate metric result
     */
    public boolean isValidMetricResult(AnalyticsResponseDTO.MetricResultDTO result) {
        if (result == null) {
            log.warn("Metric result is null");
            return false;
        }

        if (result.getMetricType() == null) {
            log.warn("Metric type is null");
            return false;
        }

        if (result.getValue() == null) {
            log.warn("Metric value is null for type: {}", result.getMetricType());
            return false;
        }

        // Check for reasonable value ranges
        if (!isReasonableValue(result.getValue(), result.getMetricType())) {
            log.warn("Unreasonable value {} for metric type: {}", result.getValue(), result.getMetricType());
            return false;
        }

        // Check quality score
        if (result.getQualityScore() != null &&
            result.getQualityScore().compareTo(MIN_QUALITY_THRESHOLD) < 0) {
            log.warn("Quality score {} below threshold for metric: {}",
                result.getQualityScore(), result.getMetricType());
            return false;
        }

        // Check confidence score
        if (result.getConfidenceScore() != null &&
            result.getConfidenceScore().compareTo(MIN_CONFIDENCE_THRESHOLD) < 0) {
            log.warn("Confidence score {} below threshold for metric: {}",
                result.getConfidenceScore(), result.getMetricType());
            return false;
        }

        return true;
    }

    /**
     * Check if value is reasonable for metric type
     */
    private boolean isReasonableValue(BigDecimal value, com.modernreservation.analyticsengine.enums.MetricType metricType) {
        // Check for negative values where they shouldn't exist
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return switch (metricType.getCategory()) {
                case REVENUE, BOOKING, CUSTOMER -> false; // These should not be negative
                case OCCUPANCY -> false; // Occupancy rates should not be negative
                default -> true; // Other metrics might have negative values
            };
        }

        // Check for percentage values
        if (metricType.getCategory() == com.modernreservation.analyticsengine.enums.MetricType.MetricCategory.OCCUPANCY) {
            return value.compareTo(BigDecimal.valueOf(100)) <= 0; // Should not exceed 100%
        }

        // Check for extremely large values (potential data errors)
        if (value.compareTo(BigDecimal.valueOf(1_000_000_000)) > 0) {
            log.warn("Extremely large value detected: {}", value);
            return false;
        }

        return true;
    }

    /**
     * Validate data completeness
     */
    public boolean isDataComplete(List<?> data, double requiredCompleteness) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        long nonNullCount = data.stream()
            .filter(item -> item != null)
            .count();

        double completeness = (double) nonNullCount / data.size();
        return completeness >= requiredCompleteness;
    }

    /**
     * Validate time series data consistency
     */
    public boolean isTimeSeriesConsistent(List<? extends com.modernreservation.analyticsengine.entity.AnalyticsMetric> metrics) {
        if (metrics == null || metrics.size() < 2) {
            return true; // Too few data points to validate consistency
        }

        // Check for proper time ordering
        for (int i = 1; i < metrics.size(); i++) {
            if (metrics.get(i).getPeriodStart().isBefore(metrics.get(i - 1).getPeriodStart())) {
                log.warn("Time series data is not properly ordered");
                return false;
            }
        }

        // Check for reasonable time gaps
        for (int i = 1; i < metrics.size(); i++) {
            java.time.Duration gap = java.time.Duration.between(
                metrics.get(i - 1).getPeriodEnd(),
                metrics.get(i).getPeriodStart()
            );

            // Allow up to 1 day gap
            if (gap.toDays() > 1) {
                log.debug("Large time gap detected: {} days", gap.toDays());
            }
        }

        return true;
    }
}
