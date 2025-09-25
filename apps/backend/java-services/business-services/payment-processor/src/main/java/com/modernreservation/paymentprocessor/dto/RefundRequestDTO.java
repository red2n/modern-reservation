package com.modernreservation.paymentprocessor.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Refund Request DTO
 *
 * Data Transfer Object for processing refund requests.
 * Supports both full and partial refunds with proper validation
 * and business logic enforcement.
 *
 * @param paymentId ID of the payment to refund
 * @param amount Amount to refund (null for full refund)
 * @param reason Reason for the refund
 * @param refundReference Custom refund reference
 * @param notifyCustomer Whether to notify customer of refund
 * @param processImmediately Whether to process refund immediately
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public record RefundRequestDTO(

    @NotNull(message = "Payment ID is required")
    @Positive(message = "Payment ID must be positive")
    Long paymentId,

    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid refund amount format")
    BigDecimal amount, // null for full refund

    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reason,

    @Size(max = 100, message = "Refund reference must not exceed 100 characters")
    String refundReference,

    Boolean notifyCustomer, // default true

    Boolean processImmediately // default true

) {

    /**
     * Check if this is a full refund request
     */
    public boolean isFullRefund() {
        return amount == null;
    }

    /**
     * Check if this is a partial refund request
     */
    public boolean isPartialRefund() {
        return amount != null;
    }

    /**
     * Get notification preference (default to true)
     */
    public boolean shouldNotifyCustomer() {
        return notifyCustomer != null ? notifyCustomer : true;
    }

    /**
     * Get processing preference (default to true)
     */
    public boolean shouldProcessImmediately() {
        return processImmediately != null ? processImmediately : true;
    }

    /**
     * Validate refund amount against payment amount
     */
    public boolean isValidRefundAmount(BigDecimal paymentAmount, BigDecimal alreadyRefunded) {
        if (isFullRefund()) {
            return true; // Full refund is always valid if payment allows it
        }

        if (paymentAmount == null) {
            return false;
        }

        BigDecimal currentRefunded = alreadyRefunded != null ? alreadyRefunded : BigDecimal.ZERO;
        BigDecimal remainingRefundable = paymentAmount.subtract(currentRefunded);

        return amount.compareTo(remainingRefundable) <= 0;
    }

    /**
     * Get the effective refund amount based on payment details
     */
    public BigDecimal getEffectiveRefundAmount(BigDecimal paymentAmount, BigDecimal alreadyRefunded) {
        if (isFullRefund()) {
            BigDecimal currentRefunded = alreadyRefunded != null ? alreadyRefunded : BigDecimal.ZERO;
            return paymentAmount.subtract(currentRefunded);
        }
        return amount;
    }

    /**
     * Check if refund reason indicates a chargeback
     */
    public boolean isChargebackRelated() {
        if (reason == null) return false;

        String lowerReason = reason.toLowerCase();
        return lowerReason.contains("chargeback") ||
               lowerReason.contains("dispute") ||
               lowerReason.contains("unauthorized");
    }

    /**
     * Check if refund reason indicates a cancellation
     */
    public boolean isCancellationRelated() {
        if (reason == null) return false;

        String lowerReason = reason.toLowerCase();
        return lowerReason.contains("cancel") ||
               lowerReason.contains("no-show") ||
               lowerReason.contains("booking error");
    }

    /**
     * Check if refund reason indicates a service issue
     */
    public boolean isServiceIssueRelated() {
        if (reason == null) return false;

        String lowerReason = reason.toLowerCase();
        return lowerReason.contains("service") ||
               lowerReason.contains("quality") ||
               lowerReason.contains("complaint") ||
               lowerReason.contains("unsatisfied");
    }

    /**
     * Get refund priority based on reason and amount
     */
    public String getRefundPriority() {
        if (isChargebackRelated()) {
            return "HIGH";
        }

        if (isServiceIssueRelated()) {
            return "MEDIUM";
        }

        if (amount != null && amount.compareTo(new BigDecimal("1000.00")) >= 0) {
            return "HIGH";
        }

        return "NORMAL";
    }

    /**
     * Get estimated processing time based on refund details
     */
    public int getEstimatedProcessingDays() {
        if (isChargebackRelated()) {
            return 1; // Process immediately for chargebacks
        }

        if (shouldProcessImmediately()) {
            return 1; // Same day processing
        }

        return 3; // Standard processing time
    }

    /**
     * Generate refund description for audit trail
     */
    public String generateRefundDescription() {
        StringBuilder description = new StringBuilder();

        if (isFullRefund()) {
            description.append("Full refund");
        } else {
            description.append("Partial refund of ").append(amount);
        }

        description.append(" - ").append(reason);

        if (refundReference != null && !refundReference.trim().isEmpty()) {
            description.append(" (Ref: ").append(refundReference).append(")");
        }

        return description.toString();
    }

    /**
     * Create a sanitized version for logging
     */
    public RefundRequestDTO sanitizeForLogging() {
        return new RefundRequestDTO(
            paymentId,
            amount,
            reason.length() > 100 ? reason.substring(0, 100) + "..." : reason,
            refundReference,
            notifyCustomer,
            processImmediately
        );
    }
}
