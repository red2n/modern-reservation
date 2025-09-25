package com.modernreservation.paymentprocessor.entity;

import com.modernreservation.paymentprocessor.enums.PaymentMethod;
import com.modernreservation.paymentprocessor.enums.PaymentStatus;
import com.modernreservation.paymentprocessor.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Payment Entity
 *
 * Represents a payment transaction in the system with comprehensive
 * payment details, security features, and audit trails.
 *
 * Features:
 * - Complete payment lifecycle tracking
 * - Multiple payment method support
 * - Security and encryption capabilities
 * - Audit trail and compliance features
 * - Integration with payment gateways
 * - Fraud detection support
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_reservation_id", columnList = "reservation_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_method", columnList = "payment_method"),
    @Index(name = "idx_payment_created_date", columnList = "created_date"),
    @Index(name = "idx_payment_gateway_transaction_id", columnList = "gateway_transaction_id"),
    @Index(name = "idx_payment_customer_id", columnList = "customer_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_reference", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Payment reference is required")
    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    private String paymentReference;

    @Column(name = "reservation_id", nullable = false)
    @NotNull(message = "Reservation ID is required")
    @Positive(message = "Reservation ID must be positive")
    private Long reservationId;

    @Column(name = "customer_id", nullable = false)
    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency;

    @Column(name = "processing_fee", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Processing fee cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid processing fee format")
    private BigDecimal processingFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    @Column(name = "gateway_provider", length = 50)
    @Size(max = 50, message = "Gateway provider name must not exceed 50 characters")
    private String gatewayProvider;

    @Column(name = "gateway_transaction_id", length = 100)
    @Size(max = 100, message = "Gateway transaction ID must not exceed 100 characters")
    private String gatewayTransactionId;

    @Column(name = "authorization_code", length = 50)
    @Size(max = 50, message = "Authorization code must not exceed 50 characters")
    private String authorizationCode;

    @Column(name = "card_last_four", length = 4)
    @Pattern(regexp = "^\\d{4}$", message = "Card last four must be exactly 4 digits")
    private String cardLastFour;

    @Column(name = "card_brand", length = 20)
    @Size(max = 20, message = "Card brand must not exceed 20 characters")
    private String cardBrand;

    @Column(name = "billing_name", length = 100)
    @Size(max = 100, message = "Billing name must not exceed 100 characters")
    private String billingName;

    @Column(name = "billing_email", length = 100)
    @Email(message = "Invalid billing email format")
    @Size(max = 100, message = "Billing email must not exceed 100 characters")
    private String billingEmail;

    @Column(name = "billing_address", length = 255)
    @Size(max = 255, message = "Billing address must not exceed 255 characters")
    private String billingAddress;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Column(name = "failure_reason", length = 255)
    @Size(max = 255, message = "Failure reason must not exceed 255 characters")
    private String failureReason;

    @Column(name = "refunded_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Refunded amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid refunded amount format")
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "risk_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Risk score cannot be negative")
    @DecimalMax(value = "100.00", message = "Risk score cannot exceed 100")
    private BigDecimal riskScore;

    @Column(name = "fraud_check_passed")
    private Boolean fraudCheckPassed;

    @Column(name = "three_ds_authenticated")
    private Boolean threeDsAuthenticated;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Audit fields
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "created_by", length = 100)
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;

    @Column(name = "last_modified_by", length = 100)
    @Size(max = 100, message = "Last modified by must not exceed 100 characters")
    private String lastModifiedBy;

    // Constructors
    public Payment() {}

    public Payment(String paymentReference, Long reservationId, Long customerId,
                   BigDecimal amount, String currency, PaymentMethod paymentMethod,
                   TransactionType transactionType) {
        this.paymentReference = paymentReference;
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.transactionType = transactionType;
        this.status = PaymentStatus.PENDING;
        this.processingFee = paymentMethod.calculateProcessingFee(amount);
        this.refundedAmount = BigDecimal.ZERO;
    }

    // Business methods

    /**
     * Get the net amount after processing fees
     */
    public BigDecimal getNetAmount() {
        if (processingFee == null) {
            return amount;
        }
        return amount.subtract(processingFee);
    }

    /**
     * Get the remaining refundable amount
     */
    public BigDecimal getRefundableAmount() {
        if (refundedAmount == null) {
            return amount;
        }
        return amount.subtract(refundedAmount);
    }

    /**
     * Check if payment can be refunded
     */
    public boolean canBeRefunded() {
        return status.canBeRefunded() &&
               getRefundableAmount().compareTo(BigDecimal.ZERO) > 0;
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
     * Update payment status with timestamp
     */
    public void updateStatus(PaymentStatus newStatus) {
        if (this.status.canTransitionTo(newStatus)) {
            this.status = newStatus;
            LocalDateTime now = LocalDateTime.now();

            switch (newStatus) {
                case AUTHORIZED -> this.authorizedAt = now;
                case CAPTURED -> this.capturedAt = now;
                case SETTLED -> this.settledAt = now;
            }
        } else {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", this.status, newStatus));
        }
    }

    /**
     * Add refund amount
     */
    public void addRefund(BigDecimal refundAmount) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }

        BigDecimal newRefundedAmount = (this.refundedAmount != null)
            ? this.refundedAmount.add(refundAmount)
            : refundAmount;

        if (newRefundedAmount.compareTo(this.amount) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds payment amount");
        }

        this.refundedAmount = newRefundedAmount;

        if (isFullyRefunded()) {
            updateStatus(PaymentStatus.REFUNDED);
        } else {
            updateStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getProcessingFee() { return processingFee; }
    public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getGatewayProvider() { return gatewayProvider; }
    public void setGatewayProvider(String gatewayProvider) { this.gatewayProvider = gatewayProvider; }

    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }

    public String getAuthorizationCode() { return authorizationCode; }
    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }

    public String getBillingName() { return billingName; }
    public void setBillingName(String billingName) { this.billingName = billingName; }

    public String getBillingEmail() { return billingEmail; }
    public void setBillingEmail(String billingEmail) { this.billingEmail = billingEmail; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }

    public LocalDateTime getAuthorizedAt() { return authorizedAt; }
    public void setAuthorizedAt(LocalDateTime authorizedAt) { this.authorizedAt = authorizedAt; }

    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

    public Boolean getFraudCheckPassed() { return fraudCheckPassed; }
    public void setFraudCheckPassed(Boolean fraudCheckPassed) { this.fraudCheckPassed = fraudCheckPassed; }

    public Boolean getThreeDsAuthenticated() { return threeDsAuthenticated; }
    public void setThreeDsAuthenticated(Boolean threeDsAuthenticated) { this.threeDsAuthenticated = threeDsAuthenticated; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    // equals, hashCode, and toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;
        return Objects.equals(paymentReference, payment.paymentReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentReference);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", paymentReference='" + paymentReference + '\'' +
                ", reservationId=" + reservationId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", createdDate=" + createdDate +
                '}';
    }
}
