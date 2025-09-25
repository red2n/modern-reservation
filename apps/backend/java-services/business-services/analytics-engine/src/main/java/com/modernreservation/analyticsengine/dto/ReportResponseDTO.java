package com.modernreservation.analyticsengine.dto;

import com.modernreservation.analyticsengine.enums.AnalyticsStatus;
import com.modernreservation.analyticsengine.enums.TimeGranularity;

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
 * Report Response DTO
 *
 * Response object for analytics report generation and retrieval.
 * Contains report metadata, status, and access information.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {

    /**
     * Unique report identifier
     */
    private UUID reportId;

    /**
     * Report name
     */
    private String reportName;

    /**
     * Report type
     */
    private String reportType;

    /**
     * Report description
     */
    private String reportDescription;

    /**
     * Generation status
     */
    private AnalyticsStatus status;

    /**
     * Time granularity used
     */
    private TimeGranularity timeGranularity;

    /**
     * Report period start
     */
    private LocalDateTime periodStart;

    /**
     * Report period end
     */
    private LocalDateTime periodEnd;

    /**
     * Timestamp when report was generated
     */
    private LocalDateTime generatedAt;

    /**
     * Report generation duration in milliseconds
     */
    private Long generationDurationMs;

    /**
     * Report format
     */
    private String reportFormat;

    /**
     * Report file size in bytes
     */
    private Long reportSizeBytes;

    /**
     * Download URL for the report
     */
    private String downloadUrl;

    /**
     * Direct access URL for dashboard reports
     */
    private String dashboardUrl;

    /**
     * Report preview URL
     */
    private String previewUrl;

    /**
     * Whether report is publicly accessible
     */
    private Boolean isPublic;

    /**
     * Access token for secure access
     */
    private String accessToken;

    /**
     * Report expiration timestamp
     */
    private LocalDateTime expiresAt;

    /**
     * Total number of metrics included
     */
    private Integer totalMetricsCount;

    /**
     * Number of successfully calculated metrics
     */
    private Integer successfulMetricsCount;

    /**
     * Number of failed metric calculations
     */
    private Integer failedMetricsCount;

    /**
     * Overall quality score of the report
     */
    private BigDecimal overallQualityScore;

    /**
     * Data completeness percentage
     */
    private BigDecimal dataCompletenessPercentage;

    /**
     * Report summary statistics
     */
    private Map<String, String> summaryStatistics;

    /**
     * Key insights from the report
     */
    private List<String> keyInsights;

    /**
     * Executive summary
     */
    private ExecutiveSummaryDTO executiveSummary;

    /**
     * Report sections metadata
     */
    private List<ReportSectionDTO> sections;

    /**
     * Visualizations included in the report
     */
    private List<VisualizationMetadataDTO> visualizations;

    /**
     * Schedule information (if scheduled report)
     */
    private ScheduleInfoDTO scheduleInfo;

    /**
     * Delivery information
     */
    private DeliveryInfoDTO deliveryInfo;

    /**
     * Recipients list
     */
    private List<String> recipients;

    /**
     * Warning messages
     */
    private List<String> warnings;

    /**
     * Error message (if generation failed)
     */
    private String errorMessage;

    /**
     * Generation metadata
     */
    private GenerationMetadataDTO metadata;

    /**
     * Related reports or drill-down options
     */
    private List<RelatedReportDTO> relatedReports;

    /**
     * Actions available for this report
     */
    private List<ReportActionDTO> availableActions;

    /**
     * Tags associated with the report
     */
    private List<String> tags;

    /**
     * Created by user information
     */
    private String createdBy;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last updated timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Report version
     */
    private Integer version;

    /**
     * Executive Summary DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutiveSummaryDTO {
        private String title;
        private String overview;
        private List<KeyMetricDTO> keyMetrics;
        private List<String> highlights;
        private List<String> concerns;
        private List<String> recommendations;
        private String conclusion;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class KeyMetricDTO {
            private String name;
            private String value;
            private String change;
            private String trend;
            private String significance;
        }
    }

    /**
     * Report Section DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportSectionDTO {
        private String sectionId;
        private String title;
        private String description;
        private Integer order;
        private String type; // METRICS, CHARTS, TABLE, TEXT, etc.
        private Integer itemCount;
        private Boolean hasData;
        private Map<String, String> metadata;
    }

    /**
     * Visualization Metadata DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualizationMetadataDTO {
        private String visualizationId;
        private String chartType;
        private String title;
        private String subtitle;
        private Integer dataPoints;
        private List<String> metrics;
        private String thumbnailUrl;
        private Boolean isInteractive;
        private Map<String, String> configuration;
    }

    /**
     * Schedule Information DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleInfoDTO {
        private Boolean isScheduled;
        private Boolean isRecurring;
        private String recurrencePattern;
        private String scheduleExpression;
        private LocalDateTime nextRunAt;
        private LocalDateTime lastRunAt;
        private Integer runCount;
        private LocalDateTime lastSuccessfulRun;
    }

    /**
     * Delivery Information DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInfoDTO {
        private Boolean emailDelivered;
        private LocalDateTime emailDeliveredAt;
        private Boolean downloadLinkGenerated;
        private Boolean dashboardPublished;
        private Integer downloadCount;
        private LocalDateTime lastAccessedAt;
        private List<String> deliveryErrors;
    }

    /**
     * Generation Metadata DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationMetadataDTO {
        private String version;
        private String environment;
        private List<String> dataSources;
        private LocalDateTime dataAsOfTime;
        private String computeInstanceId;
        private Long memoryUsedMb;
        private Integer cpuCores;
        private Map<String, String> parameters;
    }

    /**
     * Related Report DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedReportDTO {
        private UUID reportId;
        private String reportName;
        private String reportType;
        private String relationship; // DRILL_DOWN, PARENT, SIMILAR, etc.
        private String description;
        private String accessUrl;
    }

    /**
     * Report Action DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportActionDTO {
        private String actionId;
        private String actionName;
        private String description;
        private String actionUrl;
        private String method; // GET, POST, etc.
        private Boolean requiresAuth;
        private Map<String, String> parameters;
    }

    // Business Logic Methods

    /**
     * Check if report generation was successful
     */
    public boolean isSuccessful() {
        return status != null && status.isSuccess();
    }

    /**
     * Check if report is ready for access
     */
    public boolean isReady() {
        return isSuccessful() && (downloadUrl != null || dashboardUrl != null);
    }

    /**
     * Check if report has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if report has warnings
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Get success rate of metrics
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
     * Get formatted generation duration
     */
    public String getFormattedGenerationDuration() {
        if (generationDurationMs == null) {
            return "Unknown";
        }

        long seconds = generationDurationMs / 1000;
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }

    /**
     * Get period display name
     */
    public String getPeriodDisplayName() {
        if (periodStart == null || periodEnd == null) {
            return "Unknown Period";
        }

        return switch (timeGranularity) {
            case DAILY -> periodStart.toLocalDate() + " to " + periodEnd.toLocalDate();
            case WEEKLY -> "Week of " + periodStart.toLocalDate();
            case MONTHLY -> periodStart.getYear() + "-" + String.format("%02d", periodStart.getMonthValue());
            case QUARTERLY -> periodStart.getYear() + " Q" + ((periodStart.getMonthValue() - 1) / 3 + 1);
            case YEARLY -> String.valueOf(periodStart.getYear());
            default -> periodStart.toLocalDate() + " to " + periodEnd.toLocalDate();
        };
    }

    /**
     * Check if report is scheduled
     */
    public boolean isScheduledReport() {
        return scheduleInfo != null && Boolean.TRUE.equals(scheduleInfo.getIsScheduled());
    }

    /**
     * Check if report is recurring
     */
    public boolean isRecurringReport() {
        return scheduleInfo != null && Boolean.TRUE.equals(scheduleInfo.getIsRecurring());
    }

    /**
     * Get next run display text
     */
    public String getNextRunDisplayText() {
        if (!isScheduledReport() || scheduleInfo.getNextRunAt() == null) {
            return "Not scheduled";
        }

        LocalDateTime nextRun = scheduleInfo.getNextRunAt();
        if (nextRun.isBefore(LocalDateTime.now())) {
            return "Overdue";
        }

        return "Next run: " + nextRun.toLocalDate() + " " + nextRun.toLocalTime();
    }

    /**
     * Check if user has access to this report
     */
    public boolean hasAccess(String userEmail, String token) {
        if (Boolean.TRUE.equals(isPublic)) {
            return true;
        }

        if (accessToken != null && accessToken.equals(token)) {
            return true;
        }

        return recipients != null && recipients.contains(userEmail);
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
     * Add warning
     */
    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new java.util.ArrayList<>();
        }
        warnings.add(warning);
    }

    /**
     * Add key insight
     */
    public void addKeyInsight(String insight) {
        if (keyInsights == null) {
            keyInsights = new java.util.ArrayList<>();
        }
        keyInsights.add(insight);
    }

    /**
     * Check if report meets quality threshold
     */
    public boolean meetsQualityThreshold(BigDecimal threshold) {
        return overallQualityScore != null && overallQualityScore.compareTo(threshold) >= 0;
    }

    /**
     * Get status display information
     */
    public String getStatusDisplay() {
        if (status == null) {
            return "Unknown";
        }

        String display = status.getDisplayName();
        if (status.isInProgress() && generationDurationMs != null) {
            display += " (" + getFormattedGenerationDuration() + ")";
        }

        return display;
    }

    /**
     * Create error response
     */
    public static ReportResponseDTO createErrorResponse(UUID reportId, String reportName, String errorMessage) {
        return ReportResponseDTO.builder()
                               .reportId(reportId)
                               .reportName(reportName)
                               .status(AnalyticsStatus.FAILED)
                               .errorMessage(errorMessage)
                               .generatedAt(LocalDateTime.now())
                               .build();
    }

    /**
     * Create pending response for async generation
     */
    public static ReportResponseDTO createPendingResponse(UUID reportId, String reportName) {
        return ReportResponseDTO.builder()
                               .reportId(reportId)
                               .reportName(reportName)
                               .status(AnalyticsStatus.PENDING)
                               .createdAt(LocalDateTime.now())
                               .build();
    }

    /**
     * Report content DTO for actual report data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportContentDTO {
        private String textContent;
        private Map<String, Object> structuredData;
        private List<String> charts;
        private List<String> tables;
        private String htmlContent;
        private String executiveSummary;
    }

    /**
     * Report analytics DTO for performance metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportAnalyticsDTO {
        private Long viewCount;
        private Long downloadCount;
        private Double averageViewDuration;
        private Map<String, Integer> deviceStats;
        private Map<String, Integer> locationStats;
        private LocalDateTime lastAccessed;
    }

    /**
     * Report template DTO for template information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportTemplateDTO {
        private UUID templateId;
        private String templateName;
        private String templateVersion;
        private Map<String, Object> templateConfig;
        private String templateDescription;
        private LocalDateTime lastModified;
    }
}
