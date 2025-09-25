package com.modernreservation.paymentprocessor.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Payment Status Enumeration
 *
 * Represents all possible states of a payment transaction
 * throughout its lifecycle from initiation to completion.
 *
 * Payment Flow:
 * PENDING → AUTHORIZED → CAPTURED → SETTLED
 * PENDING → FAILED
 * AUTHORIZED → DECLINED
 * CAPTURED → REFUNDED (partial or full)
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public enum PaymentStatus {

    /**
     * Payment has been initiated but not yet processed
     */
    PENDING("Payment initiated and awaiting processing", true, false),

    /**
     * Payment has been authorized but not yet captured
     */
    AUTHORIZED("Payment authorized but not captured", true, false),

    /**
     * Payment has been captured and funds are reserved
     */
    CAPTURED("Payment captured, funds reserved", false, false),

    /**
     * Payment has been settled and funds transferred
     */
    SETTLED("Payment settled, funds transferred", false, true),

    /**
     * Payment authorization was declined
     */
    DECLINED("Payment authorization declined", false, false),

    /**
     * Payment processing failed due to technical error
     */
    FAILED("Payment processing failed", false, false),

    /**
     * Payment has been cancelled before processing
     */
    CANCELLED("Payment cancelled before processing", false, false),

    /**
     * Payment has been fully refunded
     */
    REFUNDED("Payment fully refunded", false, false),

    /**
     * Payment has been partially refunded
     */
    PARTIALLY_REFUNDED("Payment partially refunded", false, true),

    /**
     * Payment is under review for fraud detection
     */
    UNDER_REVIEW("Payment under fraud review", true, false),

    /**
     * Payment is being processed (temporary state)
     */
    PROCESSING("Payment currently being processed", true, false),

    /**
     * Payment requires additional verification
     */
    REQUIRES_VERIFICATION("Payment requires additional verification", true, false);

    private final String description;
    private final boolean isPending;
    private final boolean isSuccessful;

    PaymentStatus(String description, boolean isPending, boolean isSuccessful) {
        this.description = description;
        this.isPending = isPending;
        this.isSuccessful = isSuccessful;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPending() {
        return isPending;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * Check if payment is in a failed state
     */
    public boolean isFailed() {
        return this == DECLINED || this == FAILED || this == CANCELLED;
    }

    /**
     * Check if payment can be refunded
     */
    public boolean canBeRefunded() {
        return this == SETTLED || this == CAPTURED || this == PARTIALLY_REFUNDED;
    }

    /**
     * Check if payment can be cancelled
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == AUTHORIZED || this == UNDER_REVIEW || this == REQUIRES_VERIFICATION;
    }

    /**
     * Check if payment can be captured
     */
    public boolean canBeCaptured() {
        return this == AUTHORIZED;
    }

    /**
     * Get all possible next states from current status
     */
    public Set<PaymentStatus> getNextPossibleStates() {
        return switch (this) {
            case PENDING -> Set.of(AUTHORIZED, DECLINED, FAILED, CANCELLED, UNDER_REVIEW, PROCESSING);
            case AUTHORIZED -> Set.of(CAPTURED, DECLINED, CANCELLED, FAILED);
            case CAPTURED -> Set.of(SETTLED, REFUNDED, PARTIALLY_REFUNDED, FAILED);
            case SETTLED -> Set.of(REFUNDED, PARTIALLY_REFUNDED);
            case PARTIALLY_REFUNDED -> Set.of(REFUNDED);
            case PROCESSING -> Set.of(AUTHORIZED, CAPTURED, DECLINED, FAILED);
            case UNDER_REVIEW -> Set.of(AUTHORIZED, DECLINED, CANCELLED);
            case REQUIRES_VERIFICATION -> Set.of(AUTHORIZED, DECLINED, CANCELLED);
            default -> Set.of(); // Terminal states
        };
    }

    /**
     * Get all final states that indicate payment completion
     */
    public static Set<PaymentStatus> getFinalStates() {
        return Arrays.stream(values())
            .filter(status -> status.getNextPossibleStates().isEmpty())
            .collect(Collectors.toSet());
    }

    /**
     * Get all active states that require monitoring
     */
    public static Set<PaymentStatus> getActiveStates() {
        return Arrays.stream(values())
            .filter(PaymentStatus::isPending)
            .collect(Collectors.toSet());
    }

    /**
     * Check if transition from current status to target status is valid
     */
    public boolean canTransitionTo(PaymentStatus targetStatus) {
        return getNextPossibleStates().contains(targetStatus);
    }
}
