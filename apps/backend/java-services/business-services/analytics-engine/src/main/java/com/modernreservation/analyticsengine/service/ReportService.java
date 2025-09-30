package com.modernreservation.analyticsengine.service;

import com.modernreservation.analyticsengine.dto.ReportRequestDTO;
import com.modernreservation.analyticsengine.dto.ReportResponseDTO;
import com.modernreservation.analyticsengine.entity.AnalyticsReport;
import com.modernreservation.analyticsengine.enums.AnalyticsStatus;
import com.modernreservation.analyticsengine.repository.AnalyticsReportRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Report Service
 *
 * Service for generating, managing, and delivering analytics reports.
 * Handles report scheduling, template management, and automated distribution.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final AnalyticsReportRepository analyticsReportRepository;
    private final AnalyticsService analyticsService;
    private final TemplateService templateService;
    private final ExportService exportService;
    private final StorageService storageService;

    // Removed unused fields: NotificationService, EmailService

    /**
     * Generate a report based on request parameters
     */
    @Transactional
    public ReportResponseDTO generateReport(ReportRequestDTO request) {
        log.info("Starting report generation for: {}", request.getReportName());

        try {
            // Validate request
            validateReportRequest(request);

            // Create report entity
            AnalyticsReport report = createReportEntity(request);
            report = analyticsReportRepository.save(report);

            // Handle async generation
            if (Boolean.TRUE.equals(request.getAsyncGeneration())) {
                generateReportAsync(report.getReportId(), request);
                return createPendingResponse(report);
            }

            // Generate synchronously
            return generateReportSync(report, request);

        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            return ReportResponseDTO.createErrorResponse(
                UUID.randomUUID(),
                "Error Report",
                "Report generation failed: " + e.getMessage()
            );
        }
    }

    /**
     * Get report by ID
     */
    public ReportResponseDTO getReport(UUID reportId) {
        log.info("Getting report: {}", reportId);

        Optional<AnalyticsReport> reportOpt = analyticsReportRepository.findById(reportId);

        if (reportOpt.isEmpty()) {
            throw new RuntimeException("Report not found: " + reportId);
        }

        AnalyticsReport report = reportOpt.get();
        return mapToReportResponse(report);
    }

    /**
     * Get reports for a property
     */
    public List<ReportResponseDTO> getReportsForProperty(UUID propertyId, int page, int size) {
        log.info("Getting reports for property: {} (page: {}, size: {})", propertyId, page, size);

        List<AnalyticsReport> reports = analyticsReportRepository.findByPropertyIdOrderByCreatedAtDesc(
            propertyId, page * size, size
        );

        return reports.stream()
            .map(this::mapToReportResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get scheduled reports
     */
    public List<ReportResponseDTO> getScheduledReports(UUID propertyId) {
        log.info("Getting scheduled reports for property: {}", propertyId);

        List<AnalyticsReport> reports = analyticsReportRepository.findScheduledReports(propertyId);

        return reports.stream()
            .map(this::mapToReportResponse)
            .collect(Collectors.toList());
    }

    /**
     * Schedule a report
     */
    @Transactional
    public ReportResponseDTO scheduleReport(ReportRequestDTO request) {
        log.info("Scheduling report: {}", request.getReportName());

        try {
            validateSchedulingRequest(request);

            AnalyticsReport report = createReportEntity(request);
            report.setStatus(AnalyticsStatus.SCHEDULED);
            report.setScheduledAt(request.getScheduleTime());
            report.setRecurringSchedule(request.getRecurringSchedule());

            report = analyticsReportRepository.save(report);

            log.info("Report scheduled successfully: {}", report.getReportId());
            return mapToReportResponse(report);

        } catch (Exception e) {
            log.error("Error scheduling report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to schedule report", e);
        }
    }

    /**
     * Cancel scheduled report
     */
    @Transactional
    public void cancelScheduledReport(UUID reportId) {
        log.info("Cancelling scheduled report: {}", reportId);

        AnalyticsReport report = analyticsReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        if (report.getStatus() != AnalyticsStatus.SCHEDULED) {
            throw new RuntimeException("Report is not scheduled: " + reportId);
        }

        report.setStatus(AnalyticsStatus.CANCELLED);
        report.setUpdatedAt(LocalDateTime.now());
        analyticsReportRepository.save(report);

        log.info("Report cancelled successfully: {}", reportId);
    }

    /**
     * Update report
     */
    @Transactional
    public ReportResponseDTO updateReport(UUID reportId, ReportRequestDTO updateRequest) {
        log.info("Updating report: {}", reportId);

        AnalyticsReport report = analyticsReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        // Update report fields
        updateReportFromRequest(report, updateRequest);
        report.setUpdatedAt(LocalDateTime.now());

        report = analyticsReportRepository.save(report);

        log.info("Report updated successfully: {}", reportId);
        return mapToReportResponse(report);
    }

    /**
     * Delete report
     */
    @Transactional
    public void deleteReport(UUID reportId) {
        log.info("Deleting report: {}", reportId);

        AnalyticsReport report = analyticsReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        // Delete report files
        if (report.getFileUrl() != null) {
            try {
                storageService.deleteFile(report.getFileUrl());
            } catch (Exception e) {
                log.warn("Failed to delete report file: {}", e.getMessage());
            }
        }

        analyticsReportRepository.delete(report);
        log.info("Report deleted successfully: {}", reportId);
    }

    /**
     * Export report in different formats
     */
    public byte[] exportReport(UUID reportId, String format) {
        log.info("Exporting report: {} in format: {}", reportId, format);

        AnalyticsReport report = analyticsReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        if (report.getStatus() != AnalyticsStatus.COMPLETED) {
            throw new RuntimeException("Report is not completed: " + reportId);
        }

        try {
            return exportService.exportReport(report, format);
        } catch (Exception e) {
            log.error("Error exporting report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to export report", e);
        }
    }

    /**
     * Share report with users
     */
    @Transactional
    public void shareReport(UUID reportId, List<String> recipients, String message) {
        log.info("Sharing report: {} with {} recipients", reportId, recipients.size());

        AnalyticsReport report = analyticsReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        if (report.getStatus() != AnalyticsStatus.COMPLETED) {
            throw new RuntimeException("Report is not completed: " + reportId);
        }

        try {
            // Generate share links
            String shareUrl = generateShareUrl(report);

            // Send notifications
            for (String recipient : recipients) {
                sendShareNotification(recipient, report, shareUrl, message);
            }

            // Update report access log
            updateReportAccessLog(report, "SHARED", recipients.size());

            log.info("Report shared successfully with {} recipients", recipients.size());

        } catch (Exception e) {
            log.error("Error sharing report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to share report", e);
        }
    }

    /**
     * Get report analytics and usage statistics
     */
    public ReportResponseDTO.ReportAnalyticsDTO getReportAnalytics(UUID reportId) {
        log.info("Getting analytics for report: {}", reportId);

        AnalyticsReport report = analyticsReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        // Get usage statistics
        Map<String, Object> usageStats = analyticsReportRepository.getReportUsageStatistics(reportId);

        return ReportResponseDTO.ReportAnalyticsDTO.builder()
            .reportId(reportId)
            .totalViews((Long) usageStats.getOrDefault("totalViews", 0L))
            .uniqueViewers((Long) usageStats.getOrDefault("uniqueViewers", 0L))
            .totalDownloads((Long) usageStats.getOrDefault("totalDownloads", 0L))
            .averageViewDuration((Double) usageStats.getOrDefault("avgViewDuration", 0.0))
            .lastAccessed((LocalDateTime) usageStats.get("lastAccessed"))
            .topViewers(getTopViewers(reportId))
            .accessPatterns(getAccessPatterns(reportId))
            .build();
    }

    /**
     * Process scheduled reports
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void processScheduledReports() {
        log.debug("Processing scheduled reports");

        LocalDateTime now = LocalDateTime.now();
        List<AnalyticsReport> dueReports = analyticsReportRepository.findDueScheduledReports(now);

        log.info("Found {} reports due for processing", dueReports.size());

        for (AnalyticsReport report : dueReports) {
            try {
                processScheduledReport(report);
            } catch (Exception e) {
                log.error("Error processing scheduled report {}: {}", report.getReportId(), e.getMessage());
                markReportAsFailed(report, e.getMessage());
            }
        }
    }

    /**
     * Clean up old reports
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupOldReports() {
        log.info("Starting cleanup of old reports");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        List<AnalyticsReport> oldReports = analyticsReportRepository.findOldReports(cutoffDate);

        log.info("Found {} old reports to cleanup", oldReports.size());

        for (AnalyticsReport report : oldReports) {
            try {
                // Delete files
                if (report.getFileUrl() != null) {
                    storageService.deleteFile(report.getFileUrl());
                }

                // Archive or delete report
                if (report.isArchivable()) {
                    archiveReport(report);
                } else {
                    analyticsReportRepository.delete(report);
                }

            } catch (Exception e) {
                log.warn("Error cleaning up report {}: {}", report.getReportId(), e.getMessage());
            }
        }

        log.info("Cleanup completed");
    }

    /**
     * Generate report asynchronously
     */
    @Async("reportExecutor")
    public CompletableFuture<ReportResponseDTO> generateReportAsync(UUID reportId, ReportRequestDTO request) {
        log.info("Starting async report generation: {}", reportId);

        try {
            AnalyticsReport report = analyticsReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

            ReportResponseDTO response = generateReportSync(report, request);

            // Send completion notification
            if (request.getNotificationEmail() != null) {
                sendCompletionNotification(request.getNotificationEmail(), report, response);
            }

            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            log.error("Error in async report generation: {}", e.getMessage(), e);

            // Mark report as failed
            analyticsReportRepository.findById(reportId).ifPresent(report -> {
                markReportAsFailed(report, e.getMessage());
            });

            ReportResponseDTO errorResponse = ReportResponseDTO.createErrorResponse(
                reportId, "Analytics Report", "Async report generation failed: " + e.getMessage()
            );
            return CompletableFuture.completedFuture(errorResponse);
        }
    }

    /**
     * Get report templates
     */
    public List<ReportResponseDTO.ReportTemplateDTO> getReportTemplates() {
        log.info("Getting available report templates");

        return templateService.getAvailableTemplates().stream()
            .map(template -> ReportResponseDTO.ReportTemplateDTO.builder()
                .templateId(UUID.fromString(template.getId()))
                .templateName(template.getName())
                .templateType(template.getType())
                .description(template.getDescription())
                .supportedFormats(template.getSupportedFormats())
                .requiredParameters(template.getRequiredParameters())
                .previewUrl(template.getPreviewUrl())
                .build())
            .collect(Collectors.toList());
    }

    // Private helper methods

    private void validateReportRequest(ReportRequestDTO request) {
        if (request.getReportName() == null || request.getReportName().trim().isEmpty()) {
            throw new IllegalArgumentException("Report name is required");
        }

        if (request.getReportType() == null) {
            throw new IllegalArgumentException("Report type is required");
        }

        if (request.getPeriodStart() == null || request.getPeriodEnd() == null) {
            throw new IllegalArgumentException("Report period is required");
        }

        if (!request.isValidTimePeriod()) {
            throw new IllegalArgumentException("Invalid time period: start date must be before end date");
        }

        if (request.getOutputFormats() == null || request.getOutputFormats().isEmpty()) {
            request.setOutputFormats(Arrays.asList("PDF")); // Default format
        }
    }

    private void validateSchedulingRequest(ReportRequestDTO request) {
        validateReportRequest(request);

        if (request.getScheduleTime() == null) {
            throw new IllegalArgumentException("Schedule time is required for scheduled reports");
        }

        if (request.getScheduleTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Schedule time must be in the future");
        }
    }

    private AnalyticsReport createReportEntity(ReportRequestDTO request) {
        return AnalyticsReport.builder()
            .reportId(UUID.randomUUID())
            .reportName(request.getReportName())
            .reportType(request.getReportType())
            .propertyId(request.getEffectivePropertyId())
            .reportDescription(request.getDescription())
            .status(AnalyticsStatus.CREATED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .parameters(Collections.emptyMap())
            .templateId(request.getTemplateId())
            .outputFormats(request.getOutputFormats())
            .isScheduled(request.getScheduleTime() != null)
            .deliveryOptions(convertDeliveryOptionsToJson(request))
            .accessLevel(request.getAccessLevel() != null ? request.getAccessLevel() : "PRIVATE")
            .build();
    }

    private ReportResponseDTO generateReportSync(AnalyticsReport report, ReportRequestDTO request) {
        log.info("Generating report synchronously: {}", report.getReportId());

        try {
            // Update status
            report.setStatus(AnalyticsStatus.PROCESSING);
            report.setGenerationStartedAt(LocalDateTime.now());
            analyticsReportRepository.save(report);

            // Generate report content
            ReportResponseDTO.ReportContentDTO content = generateReportContent(request);

            // Apply template if specified
            if (request.getTemplateId() != null) {
                content = templateService.applyTemplate(content, request.getTemplateId().toString(), Collections.emptyMap());
            }

            // Export in requested formats
            Map<String, String> exportedFiles = new HashMap<>();
            for (String format : request.getOutputFormats()) {
                byte[] exportedData = exportService.exportReportContent(content, format);
                String fileUrl = storageService.storeFile(exportedData, generateFileName(report, format));
                exportedFiles.put(format, fileUrl);
            }

            // Update report with results
            report.setStatus(AnalyticsStatus.COMPLETED);
            report.setGenerationCompletedAt(LocalDateTime.now());
            report.setFileUrl(exportedFiles.get(request.getOutputFormats().get(0))); // Primary format
            report.setFileSizeBytes(calculateTotalFileSize(exportedFiles));
            analyticsReportRepository.save(report);

            // Send delivery notifications
            if (request.getDeliveryOptions() != null && !request.getDeliveryOptions().isEmpty()) {
                sendDeliveryNotifications(report, request, exportedFiles);
            }

            // Create response
            ReportResponseDTO response = mapToReportResponse(report);
            response.setContent(content);
            response.setExportedFiles(exportedFiles);

            long duration = java.time.Duration.between(report.getGenerationStartedAt(), report.getGenerationCompletedAt()).toMillis();
            log.info("Report generated successfully in {}ms: {}", duration, report.getReportId());

            return response;

        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            markReportAsFailed(report, e.getMessage());
            throw new RuntimeException("Report generation failed", e);
        }
    }

    private ReportResponseDTO.ReportContentDTO generateReportContent(ReportRequestDTO request) {
        log.info("Generating report content for type: {}", request.getReportType());

        // Create analytics request
        com.modernreservation.analyticsengine.dto.AnalyticsRequestDTO analyticsRequest =
            createAnalyticsRequest(request);

        // Get analytics data
        com.modernreservation.analyticsengine.dto.AnalyticsResponseDTO analyticsResponse =
            analyticsService.calculateAnalytics(analyticsRequest);

        // Get dashboard data if requested
        com.modernreservation.analyticsengine.dto.DashboardDTO dashboardData = null;
        if (request.getIncludeDashboard()) {
            dashboardData = analyticsService.getDashboardData(
                request.getReportType(),
                request.getEffectivePropertyId(),
                5 // 5 minute refresh
            );
        }

        // Generate executive summary
        String executiveSummary = generateExecutiveSummary(analyticsResponse, request);

        return ReportResponseDTO.ReportContentDTO.builder()
            .title(request.getReportName())
            .subtitle(generateSubtitle(request))
            .executiveSummary(executiveSummary)
            .analyticsData(analyticsResponse)
            .dashboardData(null) // TODO: Convert DashboardDTO types
            .insights(analyticsService.getAnalyticsInsights(
                request.getEffectivePropertyId(),
                request.getPeriodStart(),
                request.getPeriodEnd()
            ))
            .generatedAt(LocalDateTime.now())
            .reportPeriod(request.getPeriodStart() + " to " + request.getPeriodEnd())
            .metadata(generateContentMetadata(request))
            .build();
    }

    private void processScheduledReport(AnalyticsReport report) {
        log.info("Processing scheduled report: {}", report.getReportId());

        try {
            // Reconstruct request from stored parameters
            ReportRequestDTO request = reconstructRequestFromReport(report);

            // Update period for current execution
            updatePeriodForScheduledReport(request, report);

            // Generate report
            generateReportSync(report, request);

            // Update next execution time if recurring
            if (report.getRecurringSchedule() != null && !report.getRecurringSchedule().isEmpty()) {
                updateNextExecutionTime(report);
            } else {
                report.setStatus(AnalyticsStatus.COMPLETED);
                analyticsReportRepository.save(report);
            }

        } catch (Exception e) {
            log.error("Error processing scheduled report: {}", e.getMessage(), e);
            markReportAsFailed(report, e.getMessage());
        }
    }

    private void markReportAsFailed(AnalyticsReport report, String errorMessage) {
        report.setStatus(AnalyticsStatus.ERROR);
        report.setErrorMessage(errorMessage);
        report.setUpdatedAt(LocalDateTime.now());
        analyticsReportRepository.save(report);
    }

    private ReportResponseDTO mapToReportResponse(AnalyticsReport report) {
        return ReportResponseDTO.builder()
            .reportId(report.getReportId())
            .reportName(report.getReportName())
            .reportType(report.getReportType())
            .status(report.getStatus())
            .createdAt(report.getCreatedAt())
            .generationStartedAt(report.getGenerationStartedAt())
            .generationCompletedAt(report.getGenerationCompletedAt())
            .fileUrl(report.getFileUrl())
            .fileSizeBytes(report.getFileSizeBytes())
            .outputFormats(report.getOutputFormats() != null ?
                report.getOutputFormats() : Collections.emptyList())
            .isScheduled(report.isScheduled())
            .scheduledAt(report.getScheduledAt())
            .accessLevel(report.getAccessLevel())
            .shareUrl(generateShareUrl(report))
            .metadata(ReportResponseDTO.GenerationMetadataDTO.builder()
                .version("3.2.0")
                .category(report.getReportType())
                .tags(Collections.emptyList())
                .createdBy("system")
                .build())
            .build();
    }

    private ReportResponseDTO createPendingResponse(AnalyticsReport report) {
        return ReportResponseDTO.builder()
            .reportId(report.getReportId())
            .reportName(report.getReportName())
            .reportType(report.getReportType())
            .status(AnalyticsStatus.PROCESSING)
            .createdAt(report.getCreatedAt())
            .message("Report generation started. You will be notified when complete.")
            .build();
    }

    // Additional helper methods would be implemented here...

    // Removed unused method: convertParametersToJson

    private String convertDeliveryOptionsToJson(ReportRequestDTO request) {
        // Convert delivery options to JSON string
        return "{}"; // Simplified implementation
    }

    private String generateFileName(AnalyticsReport report, String format) {
        return String.format("%s_%s.%s",
            report.getReportName().replaceAll("[^a-zA-Z0-9]", "_"),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
            format.toLowerCase()
        );
    }

    private long calculateTotalFileSize(Map<String, String> exportedFiles) {
        // Calculate total file size of all exported files
        return 0L; // Simplified implementation
    }

    private void sendDeliveryNotifications(AnalyticsReport report, ReportRequestDTO request, Map<String, String> exportedFiles) {
        // Send delivery notifications via email, SMS, etc.
        log.info("Sending delivery notifications for report: {}", report.getReportId());
    }

    private com.modernreservation.analyticsengine.dto.AnalyticsRequestDTO createAnalyticsRequest(ReportRequestDTO request) {
        // Convert report request to analytics request
        return com.modernreservation.analyticsengine.dto.AnalyticsRequestDTO.builder()
            .sessionId(UUID.randomUUID().toString())
            .periodStart(request.getPeriodStart())
            .periodEnd(request.getPeriodEnd())
            .timeGranularity(request.getTimeGranularity())
            .build();
    }

    private String generateExecutiveSummary(com.modernreservation.analyticsengine.dto.AnalyticsResponseDTO analyticsResponse, ReportRequestDTO request) {
        return "Executive summary for " + request.getReportName() + " report.";
    }

    private String generateSubtitle(ReportRequestDTO request) {
        return String.format("Analytics report for period %s to %s",
            request.getPeriodStart().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
            request.getPeriodEnd().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        );
    }

    private Map<String, Object> generateContentMetadata(ReportRequestDTO request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reportType", request.getReportType());
        metadata.put("generatedAt", LocalDateTime.now());
        metadata.put("properties", request.getPropertyIds() != null ? request.getPropertyIds().size() :
                     (request.getPropertyId() != null ? 1 : 0));
        return metadata;
    }

    private String generateShareUrl(AnalyticsReport report) {
        return "/reports/shared/" + report.getReportId();
    }

    private void sendShareNotification(String recipient, AnalyticsReport report, String shareUrl, String message) {
        log.info("Sending share notification to: {}", recipient);
        // Implementation for sending share notifications
    }

    private void updateReportAccessLog(AnalyticsReport report, String action, int count) {
        log.debug("Updating access log for report: {} with action: {}", report.getReportId(), action);
        // Implementation for updating access logs
    }

    private List<String> getTopViewers(UUID reportId) {
        return analyticsReportRepository.getTopViewers(reportId);
    }

    private Map<String, Object> getAccessPatterns(UUID reportId) {
        return analyticsReportRepository.getAccessPatterns(reportId);
    }

    private void sendCompletionNotification(String email, AnalyticsReport report, ReportResponseDTO response) {
        log.info("Sending completion notification to: {}", email);
        // Implementation for sending completion notifications
    }

    private void updateReportFromRequest(AnalyticsReport report, ReportRequestDTO request) {
        if (request.getReportName() != null) {
            report.setReportName(request.getReportName());
        }
        if (request.getDescription() != null) {
            report.setDescription(request.getDescription());
        }
        // Update other fields as needed
    }

    private ReportRequestDTO reconstructRequestFromReport(AnalyticsReport report) {
        // Reconstruct request from stored report parameters
        return ReportRequestDTO.builder()
            .reportName(report.getReportName())
            .reportType(report.getReportType())
            .build();
    }

    private void updatePeriodForScheduledReport(ReportRequestDTO request, AnalyticsReport report) {
        // Update the reporting period based on the recurring schedule
        LocalDateTime now = LocalDateTime.now();
        request.setPeriodStart(now.minusDays(7)); // Default to last week
        request.setPeriodEnd(now);
    }

    private void updateNextExecutionTime(AnalyticsReport report) {
        // Calculate next execution time based on recurring schedule
        LocalDateTime nextExecution = calculateNextExecution(report.getRecurringSchedule(), report.getScheduledAt());
        report.setScheduledAt(nextExecution);
        report.setStatus(AnalyticsStatus.SCHEDULED);
        analyticsReportRepository.save(report);
    }

    private LocalDateTime calculateNextExecution(String recurringSchedule, LocalDateTime lastExecution) {
        return switch (recurringSchedule.toLowerCase()) {
            case "daily" -> lastExecution.plusDays(1);
            case "weekly" -> lastExecution.plusWeeks(1);
            case "monthly" -> lastExecution.plusMonths(1);
            case "quarterly" -> lastExecution.plusMonths(3);
            case "yearly" -> lastExecution.plusYears(1);
            default -> lastExecution.plusDays(1);
        };
    }

    private void archiveReport(AnalyticsReport report) {
        log.info("Archiving report: {}", report.getReportId());
        // Implementation for archiving reports
    }
}
