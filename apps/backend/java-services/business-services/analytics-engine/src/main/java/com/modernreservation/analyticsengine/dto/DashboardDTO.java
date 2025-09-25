package com.modernreservation.analyticsengine.dto;

import com.modernreservation.analyticsengine.enums.MetricType;
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
 * Dashboard DTO
 *
 * Data transfer object for real-time dashboard displays.
 * Contains KPIs, charts, alerts, and interactive components.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    /**
     * Dashboard identifier
     */
    private UUID dashboardId;

    /**
     * Dashboard name
     */
    private String dashboardName;

    /**
     * Dashboard description
     */
    private String description;

    /**
     * Dashboard type
     */
    private String dashboardType; // EXECUTIVE, OPERATIONAL, FINANCIAL, PROPERTY, etc.

    /**
     * Property scope (null for all properties)
     */
    private UUID propertyId;

    /**
     * Multiple properties scope
     */
    private List<UUID> propertyIds;

    /**
     * Last refresh timestamp
     */
    private LocalDateTime lastRefreshedAt;

    /**
     * Next scheduled refresh
     */
    private LocalDateTime nextRefreshAt;

    /**
     * Refresh interval in minutes
     */
    private Integer refreshIntervalMinutes;

    /**
     * Overall data quality score
     */
    private BigDecimal dataQualityScore;

    /**
     * Data freshness indicator
     */
    private String dataFreshness; // REAL_TIME, FRESH, STALE, OUTDATED

    /**
     * Key Performance Indicators
     */
    private List<KPIWidgetDTO> kpis;

    /**
     * Chart visualizations
     */
    private List<ChartWidgetDTO> charts;

    /**
     * Data tables
     */
    private List<TableWidgetDTO> tables;

    /**
     * Alert notifications
     */
    private List<AlertDTO> alerts;

    /**
     * Recent activities or events
     */
    private List<ActivityDTO> recentActivities;

    /**
     * Quick filters available
     */
    private List<FilterOptionDTO> filters;

    /**
     * Dashboard layout configuration
     */
    private LayoutConfigDTO layout;

    /**
     * Interactive features configuration
     */
    private InteractivityConfigDTO interactivity;

    /**
     * Export options
     */
    private List<ExportOptionDTO> exportOptions;

    /**
     * User preferences
     */
    private Map<String, String> userPreferences;

    /**
     * Dashboard metadata
     */
    private DashboardMetadataDTO metadata;

    /**
     * KPI Widget DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPIWidgetDTO {
        private String widgetId;
        private String title;
        private MetricType metricType;
        private BigDecimal value;
        private String formattedValue;
        private String unit;
        private String trend; // UP, DOWN, STABLE
        private BigDecimal changeValue;
        private BigDecimal changePercentage;
        private String changeFormatted;
        private String comparisonPeriod;
        private String color; // GREEN, RED, YELLOW, BLUE
        private String icon;
        private BigDecimal target;
        private BigDecimal threshold;
        private String status; // ON_TARGET, ABOVE_TARGET, BELOW_TARGET, CRITICAL
        private LocalDateTime calculatedAt;
        private Map<String, String> metadata;
    }

    /**
     * Chart Widget DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartWidgetDTO {
        private String widgetId;
        private String title;
        private String subtitle;
        private String chartType; // LINE, BAR, PIE, AREA, GAUGE, HEATMAP
        private List<ChartSeriesDTO> series;
        private List<String> categories;
        private ChartConfigurationDTO configuration;
        private Boolean isInteractive;
        private String drillDownUrl;
        private LocalDateTime lastUpdated;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ChartSeriesDTO {
            private String name;
            private List<BigDecimal> data;
            private String color;
            private String type; // For mixed charts
            private Map<String, Object> metadata;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ChartConfigurationDTO {
            private String xAxisTitle;
            private String yAxisTitle;
            private Boolean showLegend;
            private Boolean showTooltip;
            private Boolean showDataLabels;
            private String colorScheme;
            private Map<String, Object> customOptions;
        }
    }

    /**
     * Table Widget DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableWidgetDTO {
        private String widgetId;
        private String title;
        private List<TableColumnDTO> columns;
        private List<Map<String, Object>> rows;
        private Integer totalRows;
        private Integer currentPage;
        private Integer pageSize;
        private Boolean hasPagination;
        private String sortColumn;
        private String sortDirection;
        private Boolean isInteractive;
        private LocalDateTime lastUpdated;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TableColumnDTO {
            private String key;
            private String title;
            private String dataType; // STRING, NUMBER, DATE, CURRENCY, PERCENTAGE
            private Boolean sortable;
            private Boolean filterable;
            private String format;
            private String alignment; // LEFT, CENTER, RIGHT
        }
    }

    /**
     * Alert DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertDTO {
        private UUID alertId;
        private String title;
        private String message;
        private String severity; // INFO, WARNING, ERROR, CRITICAL
        private String category; // PERFORMANCE, REVENUE, OCCUPANCY, SYSTEM
        private MetricType relatedMetric;
        private BigDecimal currentValue;
        private BigDecimal thresholdValue;
        private String comparison; // ABOVE, BELOW, EQUAL
        private LocalDateTime triggeredAt;
        private LocalDateTime acknowledgedAt;
        private String status; // ACTIVE, ACKNOWLEDGED, RESOLVED
        private String actionUrl;
        private Map<String, String> metadata;
    }

    /**
     * Activity DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDTO {
        private UUID activityId;
        private String type; // REPORT_GENERATED, ALERT_TRIGGERED, DATA_UPDATED, etc.
        private String title;
        private String description;
        private String status; // SUCCESS, FAILED, IN_PROGRESS
        private LocalDateTime timestamp;
        private String userId;
        private String userDisplayName;
        private String icon;
        private String detailUrl;
        private Map<String, String> metadata;
    }

    /**
     * Filter Option DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterOptionDTO {
        private String filterId;
        private String label;
        private String type; // DATE_RANGE, PROPERTY, METRIC_TYPE, CATEGORY
        private List<FilterValueDTO> options;
        private Object defaultValue;
        private Boolean isMultiSelect;
        private Boolean isRequired;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FilterValueDTO {
            private String value;
            private String label;
            private String description;
            private Boolean selected;
        }
    }

    /**
     * Layout Configuration DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayoutConfigDTO {
        private String layoutType; // GRID, FLEX, TABS
        private Integer columns;
        private List<WidgetPositionDTO> widgets;
        private String theme; // LIGHT, DARK, AUTO
        private Boolean isResponsive;
        private Map<String, Object> customStyles;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WidgetPositionDTO {
            private String widgetId;
            private String widgetType;
            private Integer row;
            private Integer column;
            private Integer width;
            private Integer height;
            private Integer order;
            private Boolean isResizable;
            private Boolean isDraggable;
        }
    }

    /**
     * Interactivity Configuration DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractivityConfigDTO {
        private Boolean enableDrillDown;
        private Boolean enableFiltering;
        private Boolean enableTimeRangeSelection;
        private Boolean enableDataExport;
        private Boolean enableAlertManagement;
        private Boolean enableRealTimeUpdates;
        private String updateFrequency; // REAL_TIME, EVERY_MINUTE, EVERY_5_MINUTES, etc.
        private List<String> availableActions;
    }

    /**
     * Export Option DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportOptionDTO {
        private String exportId;
        private String label;
        private String format; // PDF, EXCEL, CSV, PNG, JSON
        private String description;
        private Boolean includesCharts;
        private Boolean includesData;
        private String exportUrl;
    }

    /**
     * Dashboard Metadata DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardMetadataDTO {
        private String version;
        private String createdBy;
        private LocalDateTime createdAt;
        private String lastModifiedBy;
        private LocalDateTime lastModifiedAt;
        private List<String> tags;
        private String category;
        private Integer viewCount;
        private LocalDateTime lastViewedAt;
        private Boolean isShared;
        private List<String> sharedWith;
        private String accessLevel; // PUBLIC, PRIVATE, RESTRICTED
    }

    // Business Logic Methods

    /**
     * Check if dashboard data is fresh
     */
    public boolean isDataFresh() {
        return "REAL_TIME".equals(dataFreshness) || "FRESH".equals(dataFreshness);
    }

    /**
     * Check if dashboard needs refresh
     */
    public boolean needsRefresh() {
        if (refreshIntervalMinutes == null || lastRefreshedAt == null) {
            return true;
        }

        LocalDateTime nextRefresh = lastRefreshedAt.plusMinutes(refreshIntervalMinutes);
        return LocalDateTime.now().isAfter(nextRefresh);
    }

    /**
     * Get active alerts count
     */
    public int getActiveAlertsCount() {
        if (alerts == null) {
            return 0;
        }
        return (int) alerts.stream()
                          .filter(alert -> "ACTIVE".equals(alert.getStatus()))
                          .count();
    }

    /**
     * Get critical alerts count
     */
    public int getCriticalAlertsCount() {
        if (alerts == null) {
            return 0;
        }
        return (int) alerts.stream()
                          .filter(alert -> "CRITICAL".equals(alert.getSeverity()) &&
                                         "ACTIVE".equals(alert.getStatus()))
                          .count();
    }

    /**
     * Get KPI by metric type
     */
    public KPIWidgetDTO getKPI(MetricType metricType) {
        if (kpis == null) {
            return null;
        }
        return kpis.stream()
                  .filter(kpi -> kpi.getMetricType() == metricType)
                  .findFirst()
                  .orElse(null);
    }

    /**
     * Get chart by widget ID
     */
    public ChartWidgetDTO getChart(String widgetId) {
        if (charts == null) {
            return null;
        }
        return charts.stream()
                    .filter(chart -> widgetId.equals(chart.getWidgetId()))
                    .findFirst()
                    .orElse(null);
    }

    /**
     * Get overall health status based on alerts and data quality
     */
    public String getOverallHealthStatus() {
        int criticalAlerts = getCriticalAlertsCount();
        int activeAlerts = getActiveAlertsCount();

        if (criticalAlerts > 0) {
            return "CRITICAL";
        } else if (activeAlerts > 5) {
            return "WARNING";
        } else if (dataQualityScore != null && dataQualityScore.compareTo(BigDecimal.valueOf(0.8)) < 0) {
            return "WARNING";
        } else if (!isDataFresh()) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Get data freshness indicator text
     */
    public String getDataFreshnessText() {
        if (lastRefreshedAt == null) {
            return "Never updated";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesAgo = java.time.Duration.between(lastRefreshedAt, now).toMinutes();

        if (minutesAgo < 1) {
            return "Just now";
        } else if (minutesAgo < 60) {
            return minutesAgo + " minutes ago";
        } else if (minutesAgo < 1440) { // 24 hours
            return (minutesAgo / 60) + " hours ago";
        } else {
            return (minutesAgo / 1440) + " days ago";
        }
    }

    /**
     * Check if user can export dashboard
     */
    public boolean canExport() {
        return exportOptions != null && !exportOptions.isEmpty();
    }

    /**
     * Check if dashboard is interactive
     */
    public boolean isInteractive() {
        return interactivity != null &&
               (Boolean.TRUE.equals(interactivity.getEnableDrillDown()) ||
                Boolean.TRUE.equals(interactivity.getEnableFiltering()) ||
                Boolean.TRUE.equals(interactivity.getEnableTimeRangeSelection()));
    }

    /**
     * Get widgets count by type
     */
    public Map<String, Integer> getWidgetCounts() {
        Map<String, Integer> counts = new java.util.HashMap<>();
        counts.put("KPIs", kpis != null ? kpis.size() : 0);
        counts.put("Charts", charts != null ? charts.size() : 0);
        counts.put("Tables", tables != null ? tables.size() : 0);
        counts.put("Alerts", alerts != null ? alerts.size() : 0);
        return counts;
    }

    /**
     * Add user preference
     */
    public void addUserPreference(String key, String value) {
        if (userPreferences == null) {
            userPreferences = new java.util.HashMap<>();
        }
        userPreferences.put(key, value);
    }

    /**
     * Get formatted refresh interval
     */
    public String getFormattedRefreshInterval() {
        if (refreshIntervalMinutes == null) {
            return "Manual";
        }

        if (refreshIntervalMinutes < 60) {
            return refreshIntervalMinutes + " minutes";
        } else {
            return (refreshIntervalMinutes / 60) + " hours";
        }
    }

    /**
     * Check if dashboard is shared
     */
    public boolean isShared() {
        return metadata != null && Boolean.TRUE.equals(metadata.getIsShared());
    }

    /**
     * Get sharing status text
     */
    public String getSharingStatusText() {
        if (metadata == null) {
            return "Private";
        }

        if (Boolean.TRUE.equals(metadata.getIsShared())) {
            int sharedCount = metadata.getSharedWith() != null ? metadata.getSharedWith().size() : 0;
            return "Shared with " + sharedCount + " users";
        } else {
            return "Private";
        }
    }
}
