package com.modernreservation.analyticsengine.entity;

import com.modernreservation.analyticsengine.enums.TimeGranularity;
import com.modernreservation.analyticsengine.enums.AnalyticsStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Analytics Report Entity
 *
 * Represents generated analytics reports containing multiple metrics and visualizations.
 * Supports scheduled reports, custom reports, and dashboard data aggregation.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Entity
@Table(
    name = "analytics_reports",
    indexes = {
        @Index(name = "idx_report_type_status", columnList = "report_type, status"),
        @Index(name = "idx_property_period", columnList = "property_id, period_start, period_end"),
        @Index(name = "idx_generated_at", columnList = "generated_at"),
        @Index(name = "idx_scheduled_at", columnList = "scheduled_at"),
        @Index(name = "idx_created_by", columnList = "created_by")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id")
    private UUID reportId;

    @Column(name = "report_name", nullable = false, length = 200)
    @NotBlank(message = "Report name is required")
    @Size(max = 200, message = "Report name must be 200 characters or less")
    private String reportName;

    @Column(name = "report_type", nullable = false, length = 50)
    @NotBlank(message = "Report type is required")
    @Pattern(
        regexp = "DASHBOARD|EXECUTIVE|OPERATIONAL|FINANCIAL|OCCUPANCY|REVENUE|CUSTOMER|CUSTOM|SCHEDULED",
        message = "Invalid report type"
    )
    private String reportType;

    @Column(name = "report_description", columnDefinition = "TEXT")
    private String reportDescription;

    @Column(name = "property_id")
    private UUID propertyId;

    @ElementCollection
    @CollectionTable(
        name = "report_property_ids",
        joinColumns = @JoinColumn(name = "report_id")
    )
    @Column(name = "property_id")
    private List<UUID> propertyIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_granularity", nullable = false, length = 20)
    @NotNull(message = "Time granularity is required")
    private TimeGranularity timeGranularity;

    @Column(name = "period_start", nullable = false)
    @NotNull(message = "Period start is required")
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    @NotNull(message = "Period end is required")
    private LocalDateTime periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    @Builder.Default
    private AnalyticsStatus status = AnalyticsStatus.PENDING;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "generation_duration_ms")
    private Long generationDurationMs;

    @Column(name = "total_metrics_count")
    @Min(value = 0, message = "Total metrics count cannot be negative")
    private Integer totalMetricsCount;

    @Column(name = "successful_metrics_count")
    @Min(value = 0, message = "Successful metrics count cannot be negative")
    private Integer successfulMetricsCount;

    @Column(name = "failed_metrics_count")
    @Min(value = 0, message = "Failed metrics count cannot be negative")
    private Integer failedMetricsCount;

    @Column(name = "overall_quality_score", precision = 3, scale = 2)
    @DecimalMin(value = "0.00", message = "Quality score cannot be negative")
    @DecimalMax(value = "1.00", message = "Quality score cannot exceed 1.00")
    private BigDecimal overallQualityScore;

    @Column(name = "data_completeness_percentage", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Data completeness cannot be negative")
    @DecimalMax(value = "100.00", message = "Data completeness cannot exceed 100%")
    private BigDecimal dataCompletenessPercentage;

    @ElementCollection
    @CollectionTable(
        name = "report_metric_types",
        joinColumns = @JoinColumn(name = "report_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type")
    private List<String> includedMetricTypes;

    @ElementCollection
    @CollectionTable(
        name = "report_filters",
        joinColumns = @JoinColumn(name = "report_id")
    )
    @MapKeyColumn(name = "filter_key", length = 50)
    @Column(name = "filter_value", length = 255)
    private Map<String, String> filters;

    @ElementCollection
    @CollectionTable(
        name = "report_parameters",
        joinColumns = @JoinColumn(name = "report_id")
    )
    @MapKeyColumn(name = "parameter_key", length = 50)
    @Column(name = "parameter_value", columnDefinition = "TEXT")
    private Map<String, String> parameters;

    @Column(name = "report_format", length = 20)
    @Pattern(
        regexp = "JSON|PDF|EXCEL|CSV|HTML|DASHBOARD",
        message = "Invalid report format"
    )
    @Builder.Default
    private String reportFormat = "JSON";

    @Column(name = "report_size_bytes")
    private Long reportSizeBytes;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "download_url", length = 1000)
    private String downloadUrl;

    @Column(name = "is_scheduled", nullable = false)
    @Builder.Default
    private Boolean isScheduled = false;

    @Column(name = "schedule_expression", length = 100)
    private String scheduleExpression; // Cron expression for scheduled reports

    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "recurrence_pattern", length = 50)
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY, QUARTERLY

    @ElementCollection
    @CollectionTable(
        name = "report_recipients",
        joinColumns = @JoinColumn(name = "report_id")
    )
    @Column(name = "recipient_email", length = 255)
    private List<String> recipientEmails;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "access_token", length = 100)
    private String accessToken;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "parent_report_id")
    private UUID parentReportId; // For drill-down reports

    @ElementCollection
    @CollectionTable(
        name = "report_visualizations",
        joinColumns = @JoinColumn(name = "report_id")
    )
    @MapKeyColumn(name = "chart_type", length = 50)
    @Column(name = "chart_config", columnDefinition = "TEXT")
    private Map<String, String> visualizations;

    @ElementCollection
    @CollectionTable(
        name = "report_summary_stats",
        joinColumns = @JoinColumn(name = "report_id")
    )
    @MapKeyColumn(name = "stat_key", length = 50)
    @Column(name = "stat_value", length = 255)
    private Map<String, String> summaryStatistics;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "warning_messages", columnDefinition = "TEXT")
    private String warningMessages;

    @Column(name = "generation_notes", columnDefinition = "TEXT")
    private String generationNotes;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "priority_level")
    @Min(value = 1, message = "Priority level must be at least 1")
    @Max(value = 5, message = "Priority level must be at most 5")
    @Builder.Default
    private Integer priorityLevel = 3;

    @Column(name = "version", nullable = false)
    @NotNull(message = "Version is required")
    @Builder.Default
    private Integer version = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Business Logic Methods

    /**
     * Check if report generation is complete
     */
    public boolean isGenerationComplete() {
        return status.isTerminal() && generatedAt != null;
    }

    /**
     * Check if report is still valid (not expired)
     */
    public boolean isValid() {
        return expiresAt == null || LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Check if report is ready for download
     */
    public boolean isReadyForDownload() {
        return isGenerationComplete() && status.isSuccess() &&
               (filePath != null || downloadUrl != null);
    }

    /**
     * Calculate success rate of metrics in the report
     */
    public BigDecimal getMetricsSuccessRate() {
        if (totalMetricsCount == null || totalMetricsCount == 0) {
            return BigDecimal.ZERO;
        }

        int successful = successfulMetricsCount != null ? successfulMetricsCount : 0;
        return BigDecimal.valueOf(successful)
                         .divide(BigDecimal.valueOf(totalMetricsCount), 4, BigDecimal.ROUND_HALF_UP)
                         .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if report meets quality threshold
     */
    public boolean meetsQualityThreshold(BigDecimal threshold) {
        return overallQualityScore != null && overallQualityScore.compareTo(threshold) >= 0;
    }

    /**
     * Check if data completeness meets threshold
     */
    public boolean meetsCompletenessThreshold(BigDecimal threshold) {
        return dataCompletenessPercentage != null &&
               dataCompletenessPercentage.compareTo(threshold) >= 0;
    }

    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (reportSizeBytes == null) {
            return "Unknown";
        }

        double size = reportSizeBytes.doubleValue();
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * Check if report is scheduled to run
     */
    public boolean isDueForExecution() {
        return isScheduled && nextRunAt != null &&
               LocalDateTime.now().isAfter(nextRunAt);
    }

    /**
     * Calculate next run time based on recurrence pattern
     */
    public LocalDateTime calculateNextRunTime() {
        if (!isRecurring || recurrencePattern == null) {
            return null;
        }

        LocalDateTime baseTime = lastRunAt != null ? lastRunAt : LocalDateTime.now();

        return switch (recurrencePattern.toUpperCase()) {
            case "DAILY" -> baseTime.plusDays(1);
            case "WEEKLY" -> baseTime.plusWeeks(1);
            case "MONTHLY" -> baseTime.plusMonths(1);
            case "QUARTERLY" -> baseTime.plusMonths(3);
            case "YEARLY" -> baseTime.plusYears(1);
            default -> null;
        };
    }

    /**
     * Update execution metadata
     */
    public void updateExecutionMetadata(long durationMs, int totalMetrics,
                                      int successfulMetrics, int failedMetrics) {
        this.generationDurationMs = durationMs;
        this.totalMetricsCount = totalMetrics;
        this.successfulMetricsCount = successfulMetrics;
        this.failedMetricsCount = failedMetrics;
        this.generatedAt = LocalDateTime.now();
        this.lastRunAt = LocalDateTime.now();

        if (isRecurring) {
            this.nextRunAt = calculateNextRunTime();
        }
    }

    /**
     * Add filter
     */
    public void addFilter(String key, String value) {
        if (filters == null) {
            filters = new java.util.HashMap<>();
        }
        filters.put(key, value);
    }

    /**
     * Add parameter
     */
    public void addParameter(String key, String value) {
        if (parameters == null) {
            parameters = new java.util.HashMap<>();
        }
        parameters.put(key, value);
    }

    /**
     * Add visualization configuration
     */
    public void addVisualization(String chartType, String config) {
        if (visualizations == null) {
            visualizations = new java.util.HashMap<>();
        }
        visualizations.put(chartType, config);
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
     * Add recipient email
     */
    public void addRecipient(String email) {
        if (recipientEmails == null) {
            recipientEmails = new java.util.ArrayList<>();
        }
        if (!recipientEmails.contains(email)) {
            recipientEmails.add(email);
        }
    }

    /**
     * Check if user has access to this report
     */
    public boolean hasAccess(String userEmail, String token) {
        if (isPublic) {
            return true;
        }

        if (accessToken != null && accessToken.equals(token)) {
            return true;
        }

        return recipientEmails != null && recipientEmails.contains(userEmail);
    }

    /**
     * Mark report as expired
     */
    public void markAsExpired() {
        this.status = AnalyticsStatus.EXPIRED;
        this.expiresAt = LocalDateTime.now();
    }

    /**
     * Get period display name
     */
    public String getPeriodDisplayName() {
        return switch (timeGranularity) {
            case DAILY -> periodStart.toLocalDate() + " to " + periodEnd.toLocalDate();
            case WEEKLY -> "Week of " + periodStart.toLocalDate() + " to " + periodEnd.toLocalDate();
            case MONTHLY -> periodStart.getYear() + "-" + String.format("%02d", periodStart.getMonthValue());
            case QUARTERLY -> periodStart.getYear() + " Q" + ((periodStart.getMonthValue() - 1) / 3 + 1);
            case YEARLY -> String.valueOf(periodStart.getYear());
            default -> periodStart.toLocalDate() + " to " + periodEnd.toLocalDate();
        };
    }

    /**
     * Get priority display name
     */
    public String getPriorityDisplayName() {
        return switch (priorityLevel) {
            case 1 -> "Very Low";
            case 2 -> "Low";
            case 3 -> "Medium";
            case 4 -> "High";
            case 5 -> "Very High";
            default -> "Medium";
        };
    }
}
