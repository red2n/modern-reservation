package com.modernreservation.paymentprocessor.dto;

import com.modernreservation.paymentprocessor.enums.PaymentMethod;
import com.modernreservation.paymentprocessor.enums.PaymentStatus;
import com.modernreservation.paymentprocessor.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Summary DTO - Lightweight DTO for list views
 *
 * Contains only essential payment information needed for displaying
 * payments in lists, tables, and transaction histories.
 * Excludes sensitive gateway details, full metadata, and verbose fields.
 *
 * Use this DTO for:
 * - Payment transaction lists
 * - Reservation payment history summaries
 * - Dashboard payment widgets
 * - Quick payment status checks
 *
 * For complete details including gateway info and metadata,
 * use PaymentResponseDTO via the detail endpoint.
 *
 * Payload reduction: ~70% compared to full PaymentResponseDTO
 */
public record PaymentSummaryDTO(
    Long id,
    String paymentReference,
    Long reservationId,
    BigDecimal amount,
    String currency,
    PaymentMethod paymentMethod,
    TransactionType transactionType,
    PaymentStatus status,
    String cardLastFour,
    String cardBrand,
    LocalDateTime createdDate
) {

    /**
     * Factory method to create from full PaymentResponseDTO
     */
    public static PaymentSummaryDTO from(PaymentResponseDTO full) {
        return new PaymentSummaryDTO(
            full.id(),
            full.paymentReference(),
            full.reservationId(),
            full.amount(),
            full.currency(),
            full.paymentMethod(),
            full.transactionType(),
            full.status(),
            full.cardLastFour(),
            full.cardBrand(),
            full.createdDate()
        );
    }

    // Convenience methods
    public boolean isPending() {
        return status.isPending();
    }

    public boolean isSuccessful() {
        return status.isSuccessful();
    }

    public boolean isFailed() {
        return status.isFailed();
    }

    public boolean isRefunded() {
        return status == PaymentStatus.REFUNDED || status == PaymentStatus.PARTIALLY_REFUNDED;
    }
}
