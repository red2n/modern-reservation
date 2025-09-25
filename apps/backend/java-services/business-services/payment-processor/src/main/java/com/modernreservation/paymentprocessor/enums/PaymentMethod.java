package com.modernreservation.paymentprocessor.enums;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Payment Method Enumeration
 *
 * Represents all supported payment methods with their characteristics,
 * processing fees, security requirements, and operational constraints.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public enum PaymentMethod {

    /**
     * Credit Card payments (Visa, MasterCard, American Express, etc.)
     */
    CREDIT_CARD(
        "Credit Card",
        "Pay with credit card",
        new BigDecimal("2.9"),
        new BigDecimal("0.30"),
        true,
        true,
        Set.of("PCI_COMPLIANCE", "CVV_VERIFICATION", "3DS_AUTHENTICATION"),
        30
    ),

    /**
     * Debit Card payments
     */
    DEBIT_CARD(
        "Debit Card",
        "Pay with debit card",
        new BigDecimal("1.9"),
        new BigDecimal("0.30"),
        true,
        true,
        Set.of("PCI_COMPLIANCE", "PIN_VERIFICATION"),
        15
    ),

    /**
     * PayPal digital wallet
     */
    PAYPAL(
        "PayPal",
        "Pay with PayPal account",
        new BigDecimal("3.49"),
        new BigDecimal("0.49"),
        true,
        false,
        Set.of("OAUTH_AUTHENTICATION", "TWO_FACTOR_AUTH"),
        60
    ),

    /**
     * Apple Pay digital wallet
     */
    APPLE_PAY(
        "Apple Pay",
        "Pay with Apple Pay",
        new BigDecimal("2.9"),
        new BigDecimal("0.30"),
        true,
        false,
        Set.of("TOUCH_ID", "FACE_ID", "DEVICE_AUTHENTICATION"),
        10
    ),

    /**
     * Google Pay digital wallet
     */
    GOOGLE_PAY(
        "Google Pay",
        "Pay with Google Pay",
        new BigDecimal("2.9"),
        new BigDecimal("0.30"),
        true,
        false,
        Set.of("BIOMETRIC_AUTH", "DEVICE_AUTHENTICATION"),
        10
    ),

    /**
     * Bank transfer (ACH/Wire transfer)
     */
    BANK_TRANSFER(
        "Bank Transfer",
        "Pay via bank transfer",
        new BigDecimal("0.8"),
        new BigDecimal("0.00"),
        false,
        false,
        Set.of("ACCOUNT_VERIFICATION", "ROUTING_VERIFICATION"),
        1440 // 24 hours
    ),

    /**
     * Cryptocurrency payments
     */
    CRYPTOCURRENCY(
        "Cryptocurrency",
        "Pay with cryptocurrency",
        new BigDecimal("1.0"),
        new BigDecimal("0.00"),
        false,
        false,
        Set.of("BLOCKCHAIN_VERIFICATION", "WALLET_AUTHENTICATION"),
        60
    ),

    /**
     * Buy Now Pay Later services (Klarna, Afterpay, etc.)
     */
    BNPL(
        "Buy Now Pay Later",
        "Pay in installments",
        new BigDecimal("4.0"),
        new BigDecimal("0.30"),
        true,
        false,
        Set.of("CREDIT_CHECK", "IDENTITY_VERIFICATION"),
        120
    ),

    /**
     * Gift cards and store credit
     */
    GIFT_CARD(
        "Gift Card",
        "Pay with gift card",
        new BigDecimal("0.0"),
        new BigDecimal("0.00"),
        true,
        false,
        Set.of("CARD_VERIFICATION", "BALANCE_CHECK"),
        5
    ),

    /**
     * Loyalty points redemption
     */
    LOYALTY_POINTS(
        "Loyalty Points",
        "Pay with loyalty points",
        new BigDecimal("0.0"),
        new BigDecimal("0.00"),
        true,
        false,
        Set.of("ACCOUNT_VERIFICATION", "POINTS_VALIDATION"),
        5
    ),

    /**
     * Cash payments (for on-site transactions)
     */
    CASH(
        "Cash",
        "Pay with cash",
        new BigDecimal("0.0"),
        new BigDecimal("0.00"),
        true,
        false,
        Set.of("RECEIPT_VERIFICATION"),
        0
    );

    private final String displayName;
    private final String description;
    private final BigDecimal processingFeePercentage;
    private final BigDecimal fixedFee;
    private final boolean supportsInstantPayment;
    private final boolean requiresCardDetails;
    private final Set<String> securityFeatures;
    private final int averageProcessingTimeMinutes;

    PaymentMethod(String displayName, String description, BigDecimal processingFeePercentage,
                  BigDecimal fixedFee, boolean supportsInstantPayment, boolean requiresCardDetails,
                  Set<String> securityFeatures, int averageProcessingTimeMinutes) {
        this.displayName = displayName;
        this.description = description;
        this.processingFeePercentage = processingFeePercentage;
        this.fixedFee = fixedFee;
        this.supportsInstantPayment = supportsInstantPayment;
        this.requiresCardDetails = requiresCardDetails;
        this.securityFeatures = securityFeatures;
        this.averageProcessingTimeMinutes = averageProcessingTimeMinutes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getProcessingFeePercentage() {
        return processingFeePercentage;
    }

    public BigDecimal getFixedFee() {
        return fixedFee;
    }

    public boolean supportsInstantPayment() {
        return supportsInstantPayment;
    }

    public boolean requiresCardDetails() {
        return requiresCardDetails;
    }

    public Set<String> getSecurityFeatures() {
        return securityFeatures;
    }

    public int getAverageProcessingTimeMinutes() {
        return averageProcessingTimeMinutes;
    }

    /**
     * Calculate processing fee for given amount
     */
    public BigDecimal calculateProcessingFee(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal percentageFee = amount.multiply(processingFeePercentage)
            .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);

        return percentageFee.add(fixedFee);
    }

    /**
     * Check if payment method is digital wallet
     */
    public boolean isDigitalWallet() {
        return this == PAYPAL || this == APPLE_PAY || this == GOOGLE_PAY;
    }

    /**
     * Check if payment method is card-based
     */
    public boolean isCardBased() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == GIFT_CARD;
    }

    /**
     * Check if payment method requires PCI compliance
     */
    public boolean requiresPCICompliance() {
        return securityFeatures.contains("PCI_COMPLIANCE");
    }

    /**
     * Check if payment method supports refunds
     */
    public boolean supportsRefunds() {
        return this != CASH && this != LOYALTY_POINTS;
    }

    /**
     * Check if payment method supports partial payments
     */
    public boolean supportsPartialPayments() {
        return this != LOYALTY_POINTS && this != GIFT_CARD;
    }

    /**
     * Check if payment method is suitable for high-value transactions
     */
    public boolean isSuitableForHighValue(BigDecimal amount) {
        BigDecimal threshold = new BigDecimal("1000.00");

        return switch (this) {
            case CREDIT_CARD, BANK_TRANSFER, CRYPTOCURRENCY -> true;
            case CASH, GIFT_CARD -> amount.compareTo(threshold) <= 0;
            default -> amount.compareTo(threshold.divide(new BigDecimal("2"))) <= 0;
        };
    }

    /**
     * Get risk level for fraud detection
     */
    public String getRiskLevel() {
        return switch (this) {
            case CASH, LOYALTY_POINTS, GIFT_CARD -> "LOW";
            case APPLE_PAY, GOOGLE_PAY, BANK_TRANSFER -> "LOW";
            case CREDIT_CARD, DEBIT_CARD, PAYPAL -> "MEDIUM";
            case CRYPTOCURRENCY, BNPL -> "HIGH";
        };
    }
}
