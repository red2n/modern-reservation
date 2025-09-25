package com.modernreservation.paymentprocessor.dto;

import com.modernreservation.paymentprocessor.enums.PaymentMethod;
import com.modernreservation.paymentprocessor.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Payment Request DTO
 *
 * Data Transfer Object for creating new payment transactions.
 * Contains all necessary information for payment processing with
 * comprehensive validation and security features.
 *
 * @param reservationId ID of the reservation this payment is for
 * @param customerId ID of the customer making the payment
 * @param amount Payment amount (must be positive)
 * @param currency Payment currency (3-letter ISO code)
 * @param paymentMethod Method of payment (card, wallet, etc.)
 * @param transactionType Type of transaction (payment, refund, etc.)
 * @param cardNumber Credit card number (will be tokenized)
 * @param cardExpiry Card expiry date (MM/YY format)
 * @param cardCvv Card security code
 * @param cardHolderName Name on the card
 * @param billingAddress Billing address information
 * @param description Transaction description
 * @param returnUrl URL to redirect after payment (for web flows)
 * @param webhookUrl URL for payment status notifications
 * @param metadata Additional metadata for the payment
 * @param savePaymentMethod Whether to save payment method for future use
 * @param customTransactionId Custom transaction identifier
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
public record PaymentRequestDTO(

    @NotNull(message = "Reservation ID is required")
    @Positive(message = "Reservation ID must be positive")
    Long reservationId,

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    Long customerId,

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid amount format")
    BigDecimal amount,

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    String currency,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,

    @NotNull(message = "Transaction type is required")
    TransactionType transactionType,

    // Card details (for card payments)
    @Size(min = 13, max = 19, message = "Card number must be between 13 and 19 digits")
    @Pattern(regexp = "^\\d{13,19}$", message = "Card number must contain only digits")
    String cardNumber,

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Card expiry must be in MM/YY format")
    String cardExpiry,

    @Pattern(regexp = "^\\d{3,4}$", message = "CVV must be 3 or 4 digits")
    String cardCvv,

    @Size(max = 100, message = "Card holder name must not exceed 100 characters")
    String cardHolderName,

    // Billing information
    BillingAddressDTO billingAddress,

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    // Payment flow URLs
    @Pattern(regexp = "^https?://.*", message = "Return URL must be a valid HTTP/HTTPS URL")
    String returnUrl,

    @Pattern(regexp = "^https?://.*", message = "Webhook URL must be a valid HTTP/HTTPS URL")
    String webhookUrl,

    // Additional options
    Map<String, Object> metadata,

    Boolean savePaymentMethod,

    @Size(max = 100, message = "Custom transaction ID must not exceed 100 characters")
    String customTransactionId

) {

    /**
     * Billing address nested DTO
     */
    public record BillingAddressDTO(
        @NotBlank(message = "Billing name is required")
        @Size(max = 100, message = "Billing name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
        String addressLine1,

        @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
        String addressLine2,

        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Size(max = 100, message = "State must not exceed 100 characters")
        String state,

        @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid postal code format")
        String postalCode,

        @NotBlank(message = "Country is required")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a valid 2-letter ISO code")
        String country,

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        String phone
    ) {}

    // Business validation methods

    /**
     * Validate if card details are provided for card payments
     */
    public boolean hasRequiredCardDetails() {
        if (!paymentMethod.requiresCardDetails()) {
            return true;
        }

        return cardNumber != null && !cardNumber.trim().isEmpty() &&
               cardExpiry != null && !cardExpiry.trim().isEmpty() &&
               cardCvv != null && !cardCvv.trim().isEmpty() &&
               cardHolderName != null && !cardHolderName.trim().isEmpty();
    }

    /**
     * Validate if billing address is complete
     */
    public boolean hasCompleteBillingAddress() {
        return billingAddress != null &&
               billingAddress.name() != null && !billingAddress.name().trim().isEmpty() &&
               billingAddress.email() != null && !billingAddress.email().trim().isEmpty() &&
               billingAddress.country() != null && !billingAddress.country().trim().isEmpty();
    }

    /**
     * Check if payment method supports the requested transaction type
     */
    public boolean isPaymentMethodCompatible() {
        return switch (transactionType) {
            case SECURITY_DEPOSIT -> paymentMethod.isCardBased() || paymentMethod.isDigitalWallet();
            case LOYALTY_REDEMPTION -> paymentMethod == PaymentMethod.LOYALTY_POINTS;
            case GIFT_CARD_PURCHASE -> paymentMethod != PaymentMethod.GIFT_CARD &&
                                      paymentMethod != PaymentMethod.LOYALTY_POINTS;
            default -> true;
        };
    }

    /**
     * Check if amount is suitable for the payment method
     */
    public boolean isAmountSuitableForPaymentMethod() {
        return paymentMethod.isSuitableForHighValue(amount);
    }

    /**
     * Get masked card number for logging (shows only last 4 digits)
     */
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Calculate estimated processing fee
     */
    public BigDecimal getEstimatedProcessingFee() {
        return paymentMethod.calculateProcessingFee(amount);
    }

    /**
     * Get total amount including processing fee
     */
    public BigDecimal getTotalAmount() {
        return amount.add(getEstimatedProcessingFee());
    }

    /**
     * Check if transaction requires immediate authorization
     */
    public boolean requiresImmediateAuthorization() {
        return transactionType.requiresAuthorization() &&
               !transactionType.isHold();
    }

    /**
     * Check if payment request is for a high-risk transaction
     */
    public boolean isHighRiskTransaction() {
        return "HIGH".equals(paymentMethod.getRiskLevel()) ||
               "HIGH".equals(transactionType.getRiskLevel()) ||
               amount.compareTo(new BigDecimal("1000.00")) >= 0;
    }

    /**
     * Validate if webhook URL is required and provided
     */
    public boolean hasRequiredWebhookUrl() {
        // Webhook required for async payment methods
        boolean webhookRequired = paymentMethod == PaymentMethod.BANK_TRANSFER ||
                                 paymentMethod == PaymentMethod.CRYPTOCURRENCY ||
                                 paymentMethod == PaymentMethod.BNPL;

        return !webhookRequired || (webhookUrl != null && !webhookUrl.trim().isEmpty());
    }

    /**
     * Get risk assessment score based on payment details
     */
    public int getRiskAssessmentScore() {
        int score = 0;

        // Payment method risk
        switch (paymentMethod.getRiskLevel()) {
            case "HIGH" -> score += 30;
            case "MEDIUM" -> score += 15;
            case "LOW" -> score += 5;
        }

        // Transaction type risk
        switch (transactionType.getRiskLevel()) {
            case "HIGH" -> score += 25;
            case "MEDIUM" -> score += 10;
            case "LOW" -> score += 2;
        }

        // Amount-based risk
        if (amount.compareTo(new BigDecimal("5000.00")) >= 0) {
            score += 20;
        } else if (amount.compareTo(new BigDecimal("1000.00")) >= 0) {
            score += 10;
        }

        // Billing information completeness (reduces risk)
        if (hasCompleteBillingAddress()) {
            score -= 5;
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Create a sanitized version for logging (removes sensitive data)
     */
    public PaymentRequestDTO sanitizeForLogging() {
        return new PaymentRequestDTO(
            reservationId,
            customerId,
            amount,
            currency,
            paymentMethod,
            transactionType,
            getMaskedCardNumber(),
            null, // Remove expiry
            null, // Remove CVV
            cardHolderName,
            billingAddress,
            description,
            returnUrl,
            webhookUrl,
            metadata,
            savePaymentMethod,
            customTransactionId
        );
    }
}
