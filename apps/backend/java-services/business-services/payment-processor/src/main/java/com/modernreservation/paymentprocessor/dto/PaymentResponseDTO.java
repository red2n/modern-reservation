package com.modernreservation.paymentprocessor.dto;

import com.modernreservation.paymentprocessor.enums.PaymentMethod;
import com.modernreservation.paymentprocessor.enums.PaymentStatus;
import com.modernreservation.paymentprocessor.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Payment Response DTO
 *
 * Data Transfer Object for payment transaction responses.
 * Contains comprehensive payment information while ensuring
 * sensitive data is properly masked or excluded.
 *
 * @param id Unique payment identifier
 * @param paymentReference Human-readable payment reference
 * @param reservationId Associated reservation ID
 * @param customerId Customer ID who made the payment
 * @param amount Payment amount
 * @param currency Payment currency
 * @param processingFee Fee charged for processing
 * @param netAmount Amount after processing fees
 * @param paymentMethod Method used for payment
 * @param transactionType Type of transaction
 * @param status Current payment status
 * @param gatewayProvider Payment gateway used
 * @param gatewayTransactionId Gateway's transaction ID
 * @param authorizationCode Authorization code from gateway
 * @param cardLastFour Last four digits of card (if applicable)
 * @param cardBrand Card brand (Visa, MasterCard, etc.)
 * @param billingName Name on billing account
 * @param billingEmail Billing email address
 * @param description Transaction description
 * @param failureReason Reason for failure (if applicable)
 * @param refundedAmount Total refunded amount
 * @param refundableAmount Remaining refundable amount
 * @param authorizedAt Authorization timestamp
 * @param capturedAt Capture timestamp
 * @param settledAt Settlement timestamp
 * @param expiresAt Expiration timestamp
 * @param riskScore Risk assessment score
 * @param fraudCheckPassed Whether fraud checks passed
 * @param threeDsAuthenticated Whether 3DS authentication was used
 * @param metadata Additional payment metadata
 * @param createdDate Payment creation timestamp
 * @param lastModifiedDate Last modification timestamp
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public record PaymentResponseDTO(
    Long id,
    String paymentReference,
    Long reservationId,
    Long customerId,
    BigDecimal amount,
    String currency,
    BigDecimal processingFee,
    BigDecimal netAmount,
    PaymentMethod paymentMethod,
    TransactionType transactionType,
    PaymentStatus status,
    String gatewayProvider,
    String gatewayTransactionId,
    String authorizationCode,
    String cardLastFour,
    String cardBrand,
    String billingName,
    String billingEmail,
    String description,
    String failureReason,
    BigDecimal refundedAmount,
    BigDecimal refundableAmount,
    LocalDateTime authorizedAt,
    LocalDateTime capturedAt,
    LocalDateTime settledAt,
    LocalDateTime expiresAt,
    BigDecimal riskScore,
    Boolean fraudCheckPassed,
    Boolean threeDsAuthenticated,
    Map<String, Object> metadata,
    LocalDateTime createdDate,
    LocalDateTime lastModifiedDate
) {

    // Business query methods

    /**
     * Check if payment is in a pending state
     */
    public boolean isPending() {
        return status.isPending();
    }

    /**
     * Check if payment was successful
     */
    public boolean isSuccessful() {
        return status.isSuccessful();
    }

    /**
     * Check if payment failed
     */
    public boolean isFailed() {
        return status.isFailed();
    }

    /**
     * Check if payment can be refunded
     */
    public boolean canBeRefunded() {
        return status.canBeRefunded() &&
               refundableAmount != null &&
               refundableAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if payment can be captured
     */
    public boolean canBeCaptured() {
        return status.canBeCaptured();
    }

    /**
     * Check if payment can be cancelled
     */
    public boolean canBeCancelled() {
        return status.canBeCancelled();
    }

    /**
     * Check if payment is fully refunded
     */
    public boolean isFullyRefunded() {
        return refundedAmount != null &&
               refundedAmount.compareTo(amount) >= 0;
    }

    /**
     * Check if payment is partially refunded
     */
    public boolean isPartiallyRefunded() {
        return refundedAmount != null &&
               refundedAmount.compareTo(BigDecimal.ZERO) > 0 &&
               refundedAmount.compareTo(amount) < 0;
    }

    /**
     * Check if payment has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if payment is high risk
     */
    public boolean isHighRisk() {
        return riskScore != null && riskScore.compareTo(new BigDecimal("70.00")) >= 0;
    }

    /**
     * Get payment method display information
     */
    public String getPaymentMethodDisplay() {
        if (cardBrand != null && cardLastFour != null) {
            return String.format("%s ending in %s", cardBrand, cardLastFour);
        }
        return paymentMethod.getDisplayName();
    }

    /**
     * Get formatted amount with currency
     */
    public String getFormattedAmount() {
        return String.format("%s %.2f", currency, amount);
    }

    /**
     * Get formatted processing fee with currency
     */
    public String getFormattedProcessingFee() {
        if (processingFee == null) {
            return String.format("%s 0.00", currency);
        }
        return String.format("%s %.2f", currency, processingFee);
    }

    /**
     * Get formatted net amount with currency
     */
    public String getFormattedNetAmount() {
        return String.format("%s %.2f", currency, netAmount);
    }

    /**
     * Get formatted refunded amount with currency
     */
    public String getFormattedRefundedAmount() {
        if (refundedAmount == null || refundedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return String.format("%s 0.00", currency);
        }
        return String.format("%s %.2f", currency, refundedAmount);
    }

    /**
     * Get formatted refundable amount with currency
     */
    public String getFormattedRefundableAmount() {
        if (refundableAmount == null) {
            return String.format("%s 0.00", currency);
        }
        return String.format("%s %.2f", currency, refundableAmount);
    }

    /**
     * Get transaction timeline information
     */
    public TransactionTimelineDTO getTimeline() {
        return new TransactionTimelineDTO(
            createdDate,
            authorizedAt,
            capturedAt,
            settledAt,
            expiresAt,
            lastModifiedDate
        );
    }

    /**
     * Get security information summary
     */
    public SecuritySummaryDTO getSecuritySummary() {
        return new SecuritySummaryDTO(
            fraudCheckPassed,
            threeDsAuthenticated,
            riskScore,
            paymentMethod.getSecurityFeatures()
        );
    }

    /**
     * Get financial summary
     */
    public FinancialSummaryDTO getFinancialSummary() {
        return new FinancialSummaryDTO(
            amount,
            processingFee,
            netAmount,
            refundedAmount,
            refundableAmount,
            currency
        );
    }

    /**
     * Check if payment requires attention
     */
    public boolean requiresAttention() {
        return isHighRisk() ||
               (fraudCheckPassed != null && !fraudCheckPassed) ||
               isExpired() ||
               (status == PaymentStatus.UNDER_REVIEW) ||
               (status == PaymentStatus.REQUIRES_VERIFICATION);
    }

    /**
     * Get status description with additional context
     */
    public String getStatusDescription() {
        String baseDescription = status.getDescription();

        if (failureReason != null && !failureReason.trim().isEmpty()) {
            return baseDescription + " - " + failureReason;
        }

        if (isExpired()) {
            return baseDescription + " (Expired)";
        }

        if (isHighRisk()) {
            return baseDescription + " (High Risk)";
        }

        return baseDescription;
    }

    /**
     * Get processing time in minutes (if completed)
     */
    public Long getProcessingTimeMinutes() {
        if (createdDate == null) return null;

        LocalDateTime endTime = settledAt != null ? settledAt :
                               capturedAt != null ? capturedAt :
                               authorizedAt != null ? authorizedAt : null;

        if (endTime == null) return null;

        return java.time.Duration.between(createdDate, endTime).toMinutes();
    }

    /**
     * Nested DTO for transaction timeline
     */
    public record TransactionTimelineDTO(
        LocalDateTime created,
        LocalDateTime authorized,
        LocalDateTime captured,
        LocalDateTime settled,
        LocalDateTime expires,
        LocalDateTime lastModified
    ) {}

    /**
     * Nested DTO for security summary
     */
    public record SecuritySummaryDTO(
        Boolean fraudCheckPassed,
        Boolean threeDsAuthenticated,
        BigDecimal riskScore,
        java.util.Set<String> securityFeatures
    ) {
        public String getRiskLevel() {
            if (riskScore == null) return "UNKNOWN";
            if (riskScore.compareTo(new BigDecimal("70.00")) >= 0) return "HIGH";
            if (riskScore.compareTo(new BigDecimal("30.00")) >= 0) return "MEDIUM";
            return "LOW";
        }
    }

    /**
     * Nested DTO for financial summary
     */
    public record FinancialSummaryDTO(
        BigDecimal amount,
        BigDecimal processingFee,
        BigDecimal netAmount,
        BigDecimal refundedAmount,
        BigDecimal refundableAmount,
        String currency
    ) {
        public BigDecimal getEffectiveAmount() {
            if (refundedAmount == null) return amount;
            return amount.subtract(refundedAmount);
        }

        public BigDecimal getRefundPercentage() {
            if (refundedAmount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return refundedAmount.divide(amount, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(new BigDecimal("100"));
        }
    }
}
