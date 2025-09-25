package com.modernreservation.analyticsengine.enums;

/**
 * Analytics Status Enumeration
 *
 * Represents the status of analytics calculations, reports, and data processing tasks.
 * Used for tracking calculation progress, report generation, and error handling.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public enum AnalyticsStatus {

    /**
     * Calculation/Report is queued for processing
     */
    PENDING(
        "Pending",
        "Waiting to be processed",
        false,
        false,
        0
    ),

    /**
     * Calculation/Report is currently being processed
     */
    PROCESSING(
        "Processing",
        "Currently being calculated",
        false,
        false,
        1
    ),

    /**
     * Calculation/Report completed successfully
     */
    COMPLETED(
        "Completed",
        "Successfully processed",
        true,
        false,
        100
    ),

    /**
     * Calculation/Report failed with errors
     */
    FAILED(
        "Failed",
        "Processing failed with errors",
        true,
        true,
        0
    ),

    /**
     * Calculation/Report was cancelled by user or system
     */
    CANCELLED(
        "Cancelled",
        "Processing was cancelled",
        true,
        false,
        0
    ),

    /**
     * Calculation/Report is being retried after failure
     */
    RETRYING(
        "Retrying",
        "Retrying after failure",
        false,
        false,
        1
    ),

    /**
     * Calculation/Report is scheduled for future execution
     */
    SCHEDULED(
        "Scheduled",
        "Scheduled for future processing",
        false,
        false,
        0
    ),

    /**
     * Data is being validated before processing
     */
    VALIDATING(
        "Validating",
        "Validating input data",
        false,
        false,
        10
    ),

    /**
     * Data aggregation in progress
     */
    AGGREGATING(
        "Aggregating",
        "Aggregating data from sources",
        false,
        false,
        30
    ),

    /**
     * Complex calculations in progress
     */
    CALCULATING(
        "Calculating",
        "Performing calculations",
        false,
        false,
        60
    ),

    /**
     * Report generation in progress
     */
    GENERATING(
        "Generating",
        "Generating report output",
        false,
        false,
        80
    ),

    /**
     * Final validation and formatting
     */
    FINALIZING(
        "Finalizing",
        "Final validation and formatting",
        false,
        false,
        90
    ),

    /**
     * Partial completion with warnings
     */
    COMPLETED_WITH_WARNINGS(
        "Completed with Warnings",
        "Completed but with some warnings",
        true,
        false,
        100
    ),

    /**
     * Expired data or stale calculation
     */
    EXPIRED(
        "Expired",
        "Data or calculation has expired",
        true,
        false,
        0
    ),

    /**
     * Insufficient data for calculation
     */
    INSUFFICIENT_DATA(
        "Insufficient Data",
        "Not enough data for reliable calculation",
        true,
        true,
        0
    ),

    /**
     * System timeout during processing
     */
    TIMEOUT(
        "Timeout",
        "Processing timed out",
        true,
        true,
        0
    );

    private final String displayName;
    private final String description;
    private final boolean isTerminal;
    private final boolean isError;
    private final int progressPercentage;

    AnalyticsStatus(String displayName, String description, boolean isTerminal,
                   boolean isError, int progressPercentage) {
        this.displayName = displayName;
        this.description = description;
        this.isTerminal = isTerminal;
        this.isError = isError;
        this.progressPercentage = progressPercentage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public boolean isError() {
        return isError;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    /**
     * Check if status indicates successful completion
     */
    public boolean isSuccess() {
        return this == COMPLETED || this == COMPLETED_WITH_WARNINGS;
    }

    /**
     * Check if status indicates processing is in progress
     */
    public boolean isInProgress() {
        return !isTerminal && this != PENDING && this != SCHEDULED;
    }

    /**
     * Check if status allows retry
     */
    public boolean canRetry() {
        return this == FAILED || this == TIMEOUT || this == INSUFFICIENT_DATA;
    }

    /**
     * Check if status allows cancellation
     */
    public boolean canCancel() {
        return !isTerminal;
    }

    /**
     * Get the next expected status in the processing pipeline
     */
    public AnalyticsStatus getNextStatus() {
        return switch (this) {
            case PENDING -> VALIDATING;
            case SCHEDULED -> PENDING;
            case VALIDATING -> AGGREGATING;
            case AGGREGATING -> CALCULATING;
            case CALCULATING -> GENERATING;
            case GENERATING -> FINALIZING;
            case FINALIZING -> COMPLETED;
            case PROCESSING -> COMPLETED;  // Generic processing
            case RETRYING -> VALIDATING;
            default -> this; // Terminal states stay the same
        };
    }

    /**
     * Get status priority for queue ordering (lower = higher priority)
     */
    public int getPriority() {
        return switch (this) {
            case RETRYING -> 1;      // Highest priority
            case PROCESSING -> 2;
            case VALIDATING -> 3;
            case AGGREGATING -> 4;
            case CALCULATING -> 5;
            case GENERATING -> 6;
            case FINALIZING -> 7;
            case PENDING -> 8;
            case SCHEDULED -> 9;     // Lowest priority
            default -> 10;           // Terminal states
        };
    }

    /**
     * Get CSS class for UI display
     */
    public String getCssClass() {
        if (isSuccess()) {
            return "status-success";
        } else if (isError()) {
            return "status-error";
        } else if (isInProgress()) {
            return "status-processing";
        } else if (this == PENDING || this == SCHEDULED) {
            return "status-pending";
        } else {
            return "status-neutral";
        }
    }

    /**
     * Get color code for status visualization
     */
    public String getColorCode() {
        return switch (this) {
            case COMPLETED -> "#28a745";                    // Green
            case COMPLETED_WITH_WARNINGS -> "#ffc107";      // Yellow
            case FAILED, TIMEOUT, INSUFFICIENT_DATA -> "#dc3545"; // Red
            case CANCELLED, EXPIRED -> "#6c757d";           // Gray
            case PROCESSING, CALCULATING, GENERATING -> "#007bff"; // Blue
            case VALIDATING, AGGREGATING, FINALIZING -> "#17a2b8"; // Cyan
            case RETRYING -> "#fd7e14";                     // Orange
            case PENDING, SCHEDULED -> "#6f42c1";           // Purple
        };
    }

    /**
     * Get icon for status display
     */
    public String getIcon() {
        return switch (this) {
            case COMPLETED -> "✓";
            case COMPLETED_WITH_WARNINGS -> "⚠";
            case FAILED -> "✗";
            case CANCELLED -> "⊘";
            case TIMEOUT -> "⏱";
            case INSUFFICIENT_DATA -> "📊";
            case EXPIRED -> "⏰";
            case PROCESSING, CALCULATING, GENERATING,
                 VALIDATING, AGGREGATING, FINALIZING -> "⟳";
            case RETRYING -> "↻";
            case PENDING -> "⋯";
            case SCHEDULED -> "📅";
        };
    }

    /**
     * Check if status transition is valid
     */
    public boolean canTransitionTo(AnalyticsStatus newStatus) {
        // Terminal states cannot transition (except for retry)
        if (this.isTerminal && !this.canRetry()) {
            return false;
        }

        // Allow retry transitions
        if (this.canRetry() && newStatus == RETRYING) {
            return true;
        }

        // Allow cancellation from non-terminal states
        if (!this.isTerminal && newStatus == CANCELLED) {
            return true;
        }

        // Standard forward progression
        return switch (this) {
            case PENDING -> newStatus == VALIDATING || newStatus == PROCESSING;
            case SCHEDULED -> newStatus == PENDING;
            case VALIDATING -> newStatus == AGGREGATING || newStatus == FAILED;
            case AGGREGATING -> newStatus == CALCULATING || newStatus == FAILED;
            case CALCULATING -> newStatus == GENERATING || newStatus == FAILED;
            case GENERATING -> newStatus == FINALIZING || newStatus == FAILED;
            case FINALIZING -> newStatus == COMPLETED || newStatus == COMPLETED_WITH_WARNINGS || newStatus == FAILED;
            case PROCESSING -> newStatus == COMPLETED || newStatus == FAILED;
            case RETRYING -> newStatus == VALIDATING || newStatus == FAILED;
            default -> false;
        };
    }

    /**
     * Get estimated processing time in minutes for this status
     */
    public int getEstimatedProcessingMinutes() {
        return switch (this) {
            case VALIDATING -> 1;
            case AGGREGATING -> 5;
            case CALCULATING -> 10;
            case GENERATING -> 3;
            case FINALIZING -> 1;
            case PROCESSING -> 15; // Generic processing
            default -> 0;
        };
    }

    /**
     * Get statuses that are actively processing
     */
    public static AnalyticsStatus[] getActiveStatuses() {
        return new AnalyticsStatus[] {
            PROCESSING, VALIDATING, AGGREGATING,
            CALCULATING, GENERATING, FINALIZING, RETRYING
        };
    }

    /**
     * Get statuses that indicate completion (success or failure)
     */
    public static AnalyticsStatus[] getCompletedStatuses() {
        return new AnalyticsStatus[] {
            COMPLETED, COMPLETED_WITH_WARNINGS, FAILED,
            CANCELLED, EXPIRED, INSUFFICIENT_DATA, TIMEOUT
        };
    }

    /**
     * Get statuses that can be queued for processing
     */
    public static AnalyticsStatus[] getQueueableStatuses() {
        return new AnalyticsStatus[] {
            PENDING, SCHEDULED, RETRYING
        };
    }
}
