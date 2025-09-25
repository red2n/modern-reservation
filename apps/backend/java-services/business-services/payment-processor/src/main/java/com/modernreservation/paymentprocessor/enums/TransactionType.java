package com.modernreservation.paymentprocessor.enums;

import java.util.Set;

/**
 * Transaction Type Enumeration
 *
 * Represents different types of financial transactions
 * that can be processed by the payment system.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public enum TransactionType {

    /**
     * Payment for reservation booking
     */
    RESERVATION_PAYMENT(
        "Reservation Payment",
        "Payment for hotel reservation",
        true,
        false,
        Set.of("AUTHORIZATION", "CAPTURE", "SETTLEMENT")
    ),

    /**
     * Security deposit hold
     */
    SECURITY_DEPOSIT(
        "Security Deposit",
        "Hold for incidental charges",
        false,
        true,
        Set.of("AUTHORIZATION", "HOLD")
    ),

    /**
     * Additional charges (room service, minibar, etc.)
     */
    INCIDENTAL_CHARGE(
        "Incidental Charge",
        "Additional charges during stay",
        true,
        false,
        Set.of("AUTHORIZATION", "CAPTURE", "SETTLEMENT")
    ),

    /**
     * Full refund of payment
     */
    REFUND(
        "Refund",
        "Full refund of payment",
        false,
        false,
        Set.of("REFUND_PROCESSING")
    ),

    /**
     * Partial refund of payment
     */
    PARTIAL_REFUND(
        "Partial Refund",
        "Partial refund of payment",
        false,
        false,
        Set.of("REFUND_PROCESSING")
    ),

    /**
     * Chargeback initiated by customer
     */
    CHARGEBACK(
        "Chargeback",
        "Disputed transaction chargeback",
        false,
        false,
        Set.of("DISPUTE_HANDLING")
    ),

    /**
     * Adjustment for billing corrections
     */
    ADJUSTMENT(
        "Adjustment",
        "Billing adjustment or correction",
        false,
        false,
        Set.of("MANUAL_PROCESSING")
    ),

    /**
     * Cancellation fee charge
     */
    CANCELLATION_FEE(
        "Cancellation Fee",
        "Fee for reservation cancellation",
        true,
        false,
        Set.of("AUTHORIZATION", "CAPTURE", "SETTLEMENT")
    ),

    /**
     * No-show penalty charge
     */
    NO_SHOW_FEE(
        "No-Show Fee",
        "Penalty for no-show",
        true,
        false,
        Set.of("AUTHORIZATION", "CAPTURE", "SETTLEMENT")
    ),

    /**
     * Damage charge for property damage
     */
    DAMAGE_CHARGE(
        "Damage Charge",
        "Charge for property damage",
        true,
        false,
        Set.of("AUTHORIZATION", "CAPTURE", "SETTLEMENT")
    ),

    /**
     * Processing fee charge
     */
    PROCESSING_FEE(
        "Processing Fee",
        "Payment processing fee",
        true,
        false,
        Set.of("AUTHORIZATION", "CAPTURE", "SETTLEMENT")
    ),

    /**
     * Tax collection
     */
    TAX_COLLECTION(
        "Tax Collection",
        "Tax amount collection",
        true,
        false,
        Set.of("AUTHORIZATION", "CAPTURE", "SETTLEMENT")
    ),

    /**
     * Gratuity/tip processing
     */
    GRATUITY(
        "Gratuity",
        "Tip or gratuity payment",
        true,
        false,
        Set.of("AUTHORIZATION", "CAPTURE", "SETTLEMENT")
    ),

    /**
     * Loyalty points redemption
     */
    LOYALTY_REDEMPTION(
        "Loyalty Redemption",
        "Loyalty points redemption",
        false,
        false,
        Set.of("POINTS_PROCESSING")
    ),

    /**
     * Gift card purchase
     */
    GIFT_CARD_PURCHASE(
        "Gift Card Purchase",
        "Purchase of gift card",
        true,
        false,
        Set.of("AUTHORIZATION", "CAPTURE", "SETTLEMENT")
    );

    private final String displayName;
    private final String description;
    private final boolean isDebit;
    private final boolean isHold;
    private final Set<String> requiredOperations;

    TransactionType(String displayName, String description, boolean isDebit,
                   boolean isHold, Set<String> requiredOperations) {
        this.displayName = displayName;
        this.description = description;
        this.isDebit = isDebit;
        this.isHold = isHold;
        this.requiredOperations = requiredOperations;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDebit() {
        return isDebit;
    }

    public boolean isCredit() {
        return !isDebit;
    }

    public boolean isHold() {
        return isHold;
    }

    public Set<String> getRequiredOperations() {
        return requiredOperations;
    }

    /**
     * Check if transaction type represents a refund
     */
    public boolean isRefund() {
        return this == REFUND || this == PARTIAL_REFUND;
    }

    /**
     * Check if transaction type represents a fee
     */
    public boolean isFee() {
        return this == CANCELLATION_FEE || this == NO_SHOW_FEE ||
               this == PROCESSING_FEE || this == DAMAGE_CHARGE;
    }

    /**
     * Check if transaction type requires immediate settlement
     */
    public boolean requiresImmediateSettlement() {
        return this == INCIDENTAL_CHARGE || this == DAMAGE_CHARGE ||
               this == NO_SHOW_FEE || this == PROCESSING_FEE;
    }

    /**
     * Check if transaction type can be disputed
     */
    public boolean canBeDisputed() {
        return isDebit && this != SECURITY_DEPOSIT;
    }

    /**
     * Check if transaction type requires authorization
     */
    public boolean requiresAuthorization() {
        return requiredOperations.contains("AUTHORIZATION");
    }

    /**
     * Check if transaction type supports partial amounts
     */
    public boolean supportsPartialAmount() {
        return this == INCIDENTAL_CHARGE || this == DAMAGE_CHARGE ||
               this == ADJUSTMENT || this == PARTIAL_REFUND;
    }

    /**
     * Get risk level for transaction type
     */
    public String getRiskLevel() {
        return switch (this) {
            case SECURITY_DEPOSIT, TAX_COLLECTION, PROCESSING_FEE -> "LOW";
            case RESERVATION_PAYMENT, INCIDENTAL_CHARGE, GRATUITY -> "MEDIUM";
            case DAMAGE_CHARGE, CHARGEBACK, ADJUSTMENT -> "HIGH";
            default -> "MEDIUM";
        };
    }

    /**
     * Get transaction category for reporting
     */
    public String getCategory() {
        return switch (this) {
            case RESERVATION_PAYMENT -> "BOOKING";
            case SECURITY_DEPOSIT -> "DEPOSIT";
            case INCIDENTAL_CHARGE, DAMAGE_CHARGE -> "CHARGES";
            case REFUND, PARTIAL_REFUND -> "REFUNDS";
            case CANCELLATION_FEE, NO_SHOW_FEE -> "PENALTIES";
            case CHARGEBACK -> "DISPUTES";
            case TAX_COLLECTION -> "TAXES";
            case GRATUITY -> "TIPS";
            case LOYALTY_REDEMPTION -> "LOYALTY";
            case GIFT_CARD_PURCHASE -> "GIFT_CARDS";
            default -> "OTHER";
        };
    }
}
