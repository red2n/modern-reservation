package com.modernreservation.analyticsengine.dto;

import com.modernreservation.analyticsengine.enums.MetricType;
import com.modernreservation.analyticsengine.enums.TimeGranularity;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Report Request DTO
 *
 * Request object for generating analytics reports.
 * Supports scheduled reports, custom templates, and multiple output formats.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {

    /**
     * Report name
     */
    @NotBlank(message = "Report name is required")
    @Size(max = 200, message = "Report name must be 200 characters or less")
    private String reportName;

    /**
     * Report type
     */
    @NotBlank(message = "Report type is required")
    @Pattern(
        regexp = "DASHBOARD|EXECUTIVE|OPERATIONAL|FINANCIAL|OCCUPANCY|REVENUE|CUSTOMER|CUSTOM|SCHEDULED",
        message = "Invalid report type"
    )
    private String reportType;

    /**
     * Report description
     */
    private String reportDescription;

    /**
     * Specific property to analyze (if null, all properties will be included)
     */
    private UUID propertyId;

    /**
     * Multiple properties to analyze
     */
    private List<UUID> propertyIds;

    /**
     * Time granularity for the report
     */
    @NotNull(message = "Time granularity is required")
    private TimeGranularity timeGranularity;

    /**
     * Start of report period
     */
    @NotNull(message = "Period start is required")
    private LocalDateTime periodStart;

    /**
     * End of report period
     */
    @NotNull(message = "Period end is required")
    private LocalDateTime periodEnd;

    /**
     * Specific metrics to include in the report
     */
    private List<MetricType> includedMetrics;

    /**
     * Report output format
     */
    @Pattern(
        regexp = "JSON|PDF|EXCEL|CSV|HTML|DASHBOARD",
        message = "Invalid report format"
    )
    @Builder.Default
    private String reportFormat = "JSON";

    /**
     * Template ID for report formatting
     */
    private UUID templateId;

    /**
     * Custom filters for the report
     */
    private Map<String, String> filters;

    /**
     * Report parameters
     */
    private Map<String, String> parameters;

    /**
     * Visualization configurations
     */
    private List<VisualizationConfigDTO> visualizations;

    /**
     * Whether to generate report asynchronously
     */
    @Builder.Default
    private Boolean asyncGeneration = false;

    /**
     * Whether this is a scheduled report
     */
    @Builder.Default
    private Boolean isScheduled = false;

    /**
     * Schedule expression (cron format)
     */
    private String scheduleExpression;

    /**
     * Specific time to schedule the report
     */
    private LocalDateTime scheduleTime;

    /**
     * Whether the report should recur
     */
    @Builder.Default
    private Boolean isRecurring = false;

    /**
     * Recurrence pattern
     */
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|QUARTERLY|YEARLY", message = "Invalid recurrence pattern")
    private String recurrencePattern;

    /**
     * Recurring schedule configuration
     */
    private String recurringSchedule;

    /**
     * Recipient email addresses
     */
    private List<String> recipientEmails;

    /**
     * Whether the report should be publicly accessible
     */
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Report priority (1-5, where 5 is highest)
     */
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 5, message = "Priority cannot exceed 5")
    @Builder.Default
    private Integer priority = 3;

    /**
     * Whether to include comparison with previous period
     */
    @Builder.Default
    private Boolean includeComparison = false;

    /**
     * Whether to include trend analysis
     */
    @Builder.Default
    private Boolean includeTrends = false;

    /**
     * Whether to include forecasting
     */
    @Builder.Default
    private Boolean includeForecast = false;

    /**
     * Number of periods to forecast
     */
    @Min(value = 1, message = "Forecast periods must be at least 1")
    @Max(value = 12, message = "Forecast periods cannot exceed 12")
    private Integer forecastPeriods;

    /**
     * Whether to include executive summary
     */
    @Builder.Default
    private Boolean includeExecutiveSummary = true;

    /**
     * Whether to include detailed breakdowns
     */
    @Builder.Default
    private Boolean includeDetailedBreakdown = false;

    /**
     * Whether to include data quality metrics
     */
    @Builder.Default
    private Boolean includeDataQuality = false;

    /**
     * Currency code for financial metrics
     */
    @Size(max = 3, message = "Currency code must be 3 characters or less")
    private String currencyCode;

    /**
     * Custom branding options
     */
    private BrandingOptionsDTO brandingOptions;

    /**
     * Delivery options
     */
    private DeliveryOptionsDTO deliveryOptions;

    /**
     * Tags for organizing reports
     */
    private List<String> tags;

    /**
     * User ID creating the report
     */
    private UUID userId;

    /**
     * Additional metadata
     */
    private Map<String, String> metadata;

    /**
     * Visualization Configuration DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualizationConfigDTO {

        @NotBlank(message = "Chart type is required")
        private String chartType; // LINE, BAR, PIE, TABLE, GAUGE, HEAT_MAP, etc.

        private String title;
        private String subtitle;
        private List<MetricType> metrics;
        private Map<String, String> configuration;
        private String position; // TOP, MIDDLE, BOTTOM
        private Integer order;
        private Boolean isInteractive;
        private String colorScheme;
        private Map<String, String> styling;
    }

    /**
     * Branding Options DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandingOptionsDTO {
        private String logoUrl;
        private String companyName;
        private String primaryColor;
        private String secondaryColor;
        private String fontFamily;
        private String headerTemplate;
        private String footerTemplate;
        private Boolean includeWatermark;
        private String customCss;
    }

    /**
     * Delivery Options DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryOptionsDTO {

        @Builder.Default
        private Boolean emailDelivery = false;

        @Builder.Default
        private Boolean downloadLink = true;

        @Builder.Default
        private Boolean dashboardView = false;

        private String emailSubject;
        private String emailBody;
        private String callbackUrl;
        private Integer retentionDays;
        private Boolean requiresAuthentication;
        private String accessToken;
    }

    // Validation and Business Logic Methods

    /**
     * Validate the time period
     */
    public boolean isValidTimePeriod() {
        return periodStart != null && periodEnd != null &&
               periodStart.isBefore(periodEnd);
    }

    /**
     * Check if forecasting is requested and valid
     */
    public boolean isValidForecastRequest() {
        return includeForecast && forecastPeriods != null && forecastPeriods > 0;
    }

    /**
     * Check if report is scheduled
     */
    public boolean hasSchedule() {
        return isScheduled && (scheduleExpression != null ||
               (isRecurring && recurrencePattern != null));
    }

    /**
     * Check if report has recipients
     */
    public boolean hasRecipients() {
        return recipientEmails != null && !recipientEmails.isEmpty();
    }

    /**
     * Check if report has visualizations
     */
    public boolean hasVisualizations() {
        return visualizations != null && !visualizations.isEmpty();
    }

    /**
     * Add a filter
     */
    public void addFilter(String key, String value) {
        if (filters == null) {
            filters = new java.util.HashMap<>();
        }
        filters.put(key, value);
    }

    /**
     * Add a parameter
     */
    public void addParameter(String key, String value) {
        if (parameters == null) {
            parameters = new java.util.HashMap<>();
        }
        parameters.put(key, value);
    }

    /**
     * Add a recipient
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
     * Add a visualization
     */
    public void addVisualization(VisualizationConfigDTO visualization) {
        if (visualizations == null) {
            visualizations = new java.util.ArrayList<>();
        }
        visualizations.add(visualization);
    }

    /**
     * Add a tag
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new java.util.ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Get effective property ID for single property reports
     */
    public UUID getEffectivePropertyId() {
        if (propertyId != null) {
            return propertyId;
        }
        if (propertyIds != null && propertyIds.size() == 1) {
            return propertyIds.get(0);
        }
        return null;
    }

    /**
     * Check if this is a single property report
     */
    public boolean isSinglePropertyReport() {
        return getEffectivePropertyId() != null;
    }

    /**
     * Check if this is a multi-property report
     */
    public boolean isMultiPropertyReport() {
        return propertyIds != null && propertyIds.size() > 1;
    }

    /**
     * Get estimated generation time in minutes
     */
    public int getEstimatedGenerationMinutes() {
        int baseTime = 2; // Base time for report generation

        // Adjust based on report complexity
        if (includeForecast) baseTime += 3;
        if (includeTrends) baseTime += 2;
        if (includeComparison) baseTime += 1;
        if (includeDetailedBreakdown) baseTime += 2;

        // Adjust based on visualizations
        if (hasVisualizations()) {
            baseTime += visualizations.size();
        }

        // Adjust based on output format
        switch (reportFormat.toUpperCase()) {
            case "PDF": baseTime += 2; break;
            case "EXCEL": baseTime += 3; break;
            case "HTML": baseTime += 1; break;
        }

        // Adjust based on period duration
        long days = java.time.Duration.between(periodStart, periodEnd).toDays();
        if (days > 365) baseTime += 2;
        else if (days > 90) baseTime += 1;

        return Math.max(1, baseTime);
    }

    /**
     * Check if report format supports visualizations
     */
    public boolean formatSupportsVisualizations() {
        return switch (reportFormat.toUpperCase()) {
            case "PDF", "HTML", "DASHBOARD" -> true;
            default -> false;
        };
    }

    /**
     * Check if report format supports interactivity
     */
    public boolean formatSupportsInteractivity() {
        return "DASHBOARD".equalsIgnoreCase(reportFormat) ||
               "HTML".equalsIgnoreCase(reportFormat);
    }

    /**
     * Get file extension for the report format
     */
    public String getFileExtension() {
        return switch (reportFormat.toUpperCase()) {
            case "PDF" -> ".pdf";
            case "EXCEL" -> ".xlsx";
            case "CSV" -> ".csv";
            case "HTML" -> ".html";
            case "JSON" -> ".json";
            default -> ".txt";
        };
    }

    /**
     * Get MIME type for the report format
     */
    public String getMimeType() {
        return switch (reportFormat.toUpperCase()) {
            case "PDF" -> "application/pdf";
            case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "CSV" -> "text/csv";
            case "HTML" -> "text/html";
            case "JSON" -> "application/json";
            default -> "text/plain";
        };
    }
}
