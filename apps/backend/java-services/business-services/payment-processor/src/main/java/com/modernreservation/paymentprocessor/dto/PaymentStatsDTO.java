package com.modernreservation.paymentprocessor.dto;

import java.math.BigDecimal;

/**
 * Payment Statistics DTO
 *
 * Data Transfer Object for payment statistics aggregation.
 * Used in repository queries for reporting and analytics.
 *
 * @param count Total number of payments
 * @param totalAmount Sum of all payment amounts
 * @param averageAmount Average payment amount
 * @param minAmount Minimum payment amount
 * @param maxAmount Maximum payment amount
 * @param totalProcessingFees Sum of all processing fees
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public record PaymentStatsDTO(
    Long count,
    BigDecimal totalAmount,
    BigDecimal averageAmount,
    BigDecimal minAmount,
    BigDecimal maxAmount,
    BigDecimal totalProcessingFees
) {

    /**
     * Get net amount after processing fees
     */
    public BigDecimal getNetAmount() {
        if (totalAmount == null || totalProcessingFees == null) {
            return totalAmount;
        }
        return totalAmount.subtract(totalProcessingFees);
    }

    /**
     * Get processing fee percentage
     */
    public BigDecimal getProcessingFeePercentage() {
        if (totalAmount == null || totalProcessingFees == null ||
            totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalProcessingFees.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP)
                                 .multiply(new BigDecimal("100"));
    }
}

/**
 * Refund Statistics DTO
 *
 * Data Transfer Object for refund statistics aggregation.
 *
 * @param count Total number of refunds
 * @param totalRefunded Sum of all refunded amounts
 * @param averageRefund Average refund amount
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
record RefundStatsDTO(
    Long count,
    BigDecimal totalRefunded,
    BigDecimal averageRefund
) {}
