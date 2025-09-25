package com.modernreservation.paymentprocessor.service;

import com.modernreservation.paymentprocessor.dto.*;
import com.modernreservation.paymentprocessor.entity.Payment;
import com.modernreservation.paymentprocessor.enums.PaymentMethod;
import com.modernreservation.paymentprocessor.enums.PaymentStatus;
import com.modernreservation.paymentprocessor.enums.TransactionType;
import com.modernreservation.paymentprocessor.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Service
 *
 * Core business logic service for payment processing operations.
 * Handles payment creation, authorization, capture, settlement,
 * refunds, and comprehensive payment management with security
 * and audit capabilities.
 *
 * Features:
 * - Payment processing lifecycle management
 * - Multi-gateway payment processing
 * - Fraud detection and risk assessment
 * - PCI compliance and data security
 * - Real-time payment notifications
 * - Comprehensive audit trails
 * - Revenue analytics and reporting
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Payment Creation and Processing

    /**
     * Create a new payment transaction
     */
    @CacheEvict(value = {"payments", "payment-stats"}, allEntries = true)
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        logger.info("Creating payment for reservation {} with amount {} {}",
                   request.reservationId(), request.amount(), request.currency());

        // Validate payment request
        validatePaymentRequest(request);

        // Generate payment reference
        String paymentReference = generatePaymentReference();

        // Create payment entity
        Payment payment = new Payment(
            paymentReference,
            request.reservationId(),
            request.customerId(),
            request.amount(),
            request.currency(),
            request.paymentMethod(),
            request.transactionType()
        );

        // Set additional details
        if (request.billingAddress() != null) {
            payment.setBillingName(request.billingAddress().name());
            payment.setBillingEmail(request.billingAddress().email());
            payment.setBillingAddress(formatBillingAddress(request.billingAddress()));
        }

        payment.setDescription(request.description());
        payment.setCreatedBy("SYSTEM"); // TODO: Get from security context

        // Set card details (last 4 digits only for security)
        if (request.cardNumber() != null && request.cardNumber().length() >= 4) {
            payment.setCardLastFour(request.cardNumber().substring(request.cardNumber().length() - 4));
        }

        // Calculate risk score
        BigDecimal riskScore = calculateRiskScore(request);
        payment.setRiskScore(riskScore);

        // Set expiration time
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(
            request.paymentMethod().getAverageProcessingTimeMinutes() + 30));

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        // Process payment based on type
        PaymentResponseDTO response = processPaymentTransaction(savedPayment, request);

        // Send payment created event
        publishPaymentEvent("payment.created", response);

        logger.info("Payment created successfully with reference: {}", paymentReference);
        return response;
    }

    /**
     * Authorize a payment (for card payments)
     */
    @CacheEvict(value = {"payments", "payment-stats"}, allEntries = true)
    public PaymentResponseDTO authorizePayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);

        if (!payment.canBeCaptured() && payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment cannot be authorized in current state: " + payment.getStatus());
        }

        logger.info("Authorizing payment: {}", payment.getPaymentReference());

        // Simulate gateway authorization
        String authorizationCode = generateAuthorizationCode();
        payment.setAuthorizationCode(authorizationCode);
        payment.setGatewayProvider("STRIPE"); // TODO: Make configurable
        payment.setGatewayTransactionId(UUID.randomUUID().toString());

        // Update status
        payment.updateStatus(PaymentStatus.AUTHORIZED);
        payment.setLastModifiedBy("SYSTEM");

        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponseDTO response = convertToResponseDTO(savedPayment);

        // Send authorization event
        publishPaymentEvent("payment.authorized", response);

        logger.info("Payment authorized successfully: {}", payment.getPaymentReference());
        return response;
    }

    /**
     * Capture an authorized payment
     */
    @CacheEvict(value = {"payments", "payment-stats"}, allEntries = true)
    public PaymentResponseDTO capturePayment(Long paymentId, BigDecimal captureAmount) {
        Payment payment = findPaymentById(paymentId);

        if (!payment.canBeCaptured()) {
            throw new IllegalStateException("Payment cannot be captured in current state: " + payment.getStatus());
        }

        // Validate capture amount
        if (captureAmount != null && captureAmount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("Capture amount cannot exceed authorized amount");
        }

        logger.info("Capturing payment: {} for amount: {}",
                   payment.getPaymentReference(), captureAmount != null ? captureAmount : payment.getAmount());

        // Update payment status
        payment.updateStatus(PaymentStatus.CAPTURED);
        payment.setLastModifiedBy("SYSTEM");

        // If partial capture, update amount
        if (captureAmount != null && captureAmount.compareTo(payment.getAmount()) < 0) {
            payment.setAmount(captureAmount);
            payment.setProcessingFee(payment.getPaymentMethod().calculateProcessingFee(captureAmount));
        }

        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponseDTO response = convertToResponseDTO(savedPayment);

        // Send capture event
        publishPaymentEvent("payment.captured", response);

        logger.info("Payment captured successfully: {}", payment.getPaymentReference());
        return response;
    }

    /**
     * Settle a captured payment
     */
    @CacheEvict(value = {"payments", "payment-stats"}, allEntries = true)
    public PaymentResponseDTO settlePayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Only captured payments can be settled");
        }

        logger.info("Settling payment: {}", payment.getPaymentReference());

        // Update payment status
        payment.updateStatus(PaymentStatus.SETTLED);
        payment.setLastModifiedBy("SYSTEM");

        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponseDTO response = convertToResponseDTO(savedPayment);

        // Send settlement event
        publishPaymentEvent("payment.settled", response);

        logger.info("Payment settled successfully: {}", payment.getPaymentReference());
        return response;
    }

    // Refund Operations

    /**
     * Process a refund request
     */
    @CacheEvict(value = {"payments", "payment-stats"}, allEntries = true)
    public PaymentResponseDTO processRefund(RefundRequestDTO refundRequest) {
        Payment payment = findPaymentById(refundRequest.paymentId());

        if (!payment.canBeRefunded()) {
            throw new IllegalStateException("Payment cannot be refunded in current state: " + payment.getStatus());
        }

        // Calculate refund amount
        BigDecimal refundAmount = refundRequest.getEffectiveRefundAmount(
            payment.getAmount(), payment.getRefundedAmount());

        // Validate refund amount
        if (!refundRequest.isValidRefundAmount(payment.getAmount(), payment.getRefundedAmount())) {
            throw new IllegalArgumentException("Invalid refund amount");
        }

        logger.info("Processing refund for payment: {} amount: {} reason: {}",
                   payment.getPaymentReference(), refundAmount, refundRequest.reason());

        // Add refund to payment
        payment.addRefund(refundAmount);
        payment.setLastModifiedBy("SYSTEM");
        payment.setNotes("Refund processed: " + refundRequest.reason());

        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponseDTO response = convertToResponseDTO(savedPayment);

        // Send refund event
        publishRefundEvent("payment.refunded", response, refundRequest);

        logger.info("Refund processed successfully for payment: {}", payment.getPaymentReference());
        return response;
    }

    // Payment Retrieval and Search

    /**
     * Get payment by ID
     */
    @Cacheable(value = "payments", key = "#paymentId")
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        return convertToResponseDTO(payment);
    }

    /**
     * Get payment by reference
     */
    @Cacheable(value = "payments", key = "#paymentReference")
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByReference(String paymentReference) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found with reference: " + paymentReference));
        return convertToResponseDTO(payment);
    }

    /**
     * Get all payments for a reservation
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByReservation(Long reservationId) {
        List<Payment> payments = paymentRepository.findByReservationIdOrderByCreatedDateDesc(reservationId);
        return payments.stream().map(this::convertToResponseDTO).toList();
    }

    /**
     * Get paginated payments for a customer
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPaymentsByCustomer(Long customerId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByCustomerIdOrderByCreatedDateDesc(customerId, pageable);
        return payments.map(this::convertToResponseDTO);
    }

    /**
     * Search payments with multiple criteria
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> searchPayments(Long customerId, Long reservationId,
                                                  PaymentStatus status, PaymentMethod paymentMethod,
                                                  LocalDateTime startDate, LocalDateTime endDate,
                                                  BigDecimal minAmount, BigDecimal maxAmount,
                                                  Pageable pageable) {
        Page<Payment> payments = paymentRepository.searchPayments(
            customerId, reservationId, status, paymentMethod,
            startDate, endDate, minAmount, maxAmount, pageable);
        return payments.map(this::convertToResponseDTO);
    }

    // Analytics and Reporting

    /**
     * Get payment statistics for date range
     */
    @Cacheable(value = "payment-stats", key = "#startDate + '-' + #endDate + '-' + #status")
    @Transactional(readOnly = true)
    public PaymentStatsDTO getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate, PaymentStatus status) {
        Object stats = paymentRepository.getPaymentStatsByDateRange(startDate, endDate, status);
        return (PaymentStatsDTO) stats;
    }

    /**
     * Get total revenue for date range
     */
    @Cacheable(value = "payment-stats", key = "'revenue-' + #startDate + '-' + #endDate")
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.getTotalRevenueByDateRange(startDate, endDate);
    }

    /**
     * Get net revenue (after refunds) for date range
     */
    @Cacheable(value = "payment-stats", key = "'net-revenue-' + #startDate + '-' + #endDate")
    @Transactional(readOnly = true)
    public BigDecimal getNetRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.getNetRevenueByDateRange(startDate, endDate);
    }

    /**
     * Get payments requiring attention
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsRequiringAttention() {
        List<PaymentStatus> pendingStatuses = PaymentStatus.getActiveStates().stream().toList();
        List<Payment> payments = paymentRepository.findPaymentsRequiringAttention(
            LocalDateTime.now(), pendingStatuses);
        return payments.stream().map(this::convertToResponseDTO).toList();
    }

    // Batch Operations

    /**
     * Process expired payments
     */
    @CacheEvict(value = {"payments", "payment-stats"}, allEntries = true)
    public int processExpiredPayments() {
        List<PaymentStatus> pendingStatuses = PaymentStatus.getActiveStates().stream().toList();
        int updatedCount = paymentRepository.markExpiredPaymentsAsFailed(
            LocalDateTime.now(), pendingStatuses, LocalDateTime.now());

        if (updatedCount > 0) {
            logger.info("Marked {} expired payments as failed", updatedCount);
        }

        return updatedCount;
    }

    // Private helper methods

    private Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));
    }

    private void validatePaymentRequest(PaymentRequestDTO request) {
        if (!request.hasRequiredCardDetails()) {
            throw new IllegalArgumentException("Card details are required for card payments");
        }

        if (!request.hasCompleteBillingAddress()) {
            throw new IllegalArgumentException("Complete billing address is required");
        }

        if (!request.isPaymentMethodCompatible()) {
            throw new IllegalArgumentException("Payment method is not compatible with transaction type");
        }

        if (!request.isAmountSuitableForPaymentMethod()) {
            throw new IllegalArgumentException("Payment amount is not suitable for selected payment method");
        }

        if (!request.hasRequiredWebhookUrl()) {
            throw new IllegalArgumentException("Webhook URL is required for this payment method");
        }
    }

    private String generatePaymentReference() {
        return "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateAuthorizationCode() {
        return "AUTH-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private BigDecimal calculateRiskScore(PaymentRequestDTO request) {
        return new BigDecimal(request.getRiskAssessmentScore());
    }

    private String formatBillingAddress(PaymentRequestDTO.BillingAddressDTO address) {
        StringBuilder sb = new StringBuilder();
        if (address.addressLine1() != null) sb.append(address.addressLine1());
        if (address.addressLine2() != null) sb.append(", ").append(address.addressLine2());
        if (address.city() != null) sb.append(", ").append(address.city());
        if (address.state() != null) sb.append(", ").append(address.state());
        if (address.postalCode() != null) sb.append(" ").append(address.postalCode());
        if (address.country() != null) sb.append(", ").append(address.country());
        return sb.toString();
    }

    private PaymentResponseDTO processPaymentTransaction(Payment payment, PaymentRequestDTO request) {
        // For now, auto-process based on payment method
        switch (request.paymentMethod()) {
            case CASH, LOYALTY_POINTS, GIFT_CARD -> {
                payment.updateStatus(PaymentStatus.SETTLED);
                payment.setGatewayProvider("INTERNAL");
            }
            case CREDIT_CARD, DEBIT_CARD -> {
                payment.updateStatus(PaymentStatus.AUTHORIZED);
                payment.setGatewayProvider("STRIPE");
                payment.setAuthorizationCode(generateAuthorizationCode());
            }
            default -> {
                payment.updateStatus(PaymentStatus.PENDING);
                payment.setGatewayProvider("PENDING");
            }
        }

        Payment savedPayment = paymentRepository.save(payment);
        return convertToResponseDTO(savedPayment);
    }

    private PaymentResponseDTO convertToResponseDTO(Payment payment) {
        return new PaymentResponseDTO(
            payment.getId(),
            payment.getPaymentReference(),
            payment.getReservationId(),
            payment.getCustomerId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getProcessingFee(),
            payment.getNetAmount(),
            payment.getPaymentMethod(),
            payment.getTransactionType(),
            payment.getStatus(),
            payment.getGatewayProvider(),
            payment.getGatewayTransactionId(),
            payment.getAuthorizationCode(),
            payment.getCardLastFour(),
            payment.getCardBrand(),
            payment.getBillingName(),
            payment.getBillingEmail(),
            payment.getDescription(),
            payment.getFailureReason(),
            payment.getRefundedAmount(),
            payment.getRefundableAmount(),
            payment.getAuthorizedAt(),
            payment.getCapturedAt(),
            payment.getSettledAt(),
            payment.getExpiresAt(),
            payment.getRiskScore(),
            payment.getFraudCheckPassed(),
            payment.getThreeDsAuthenticated(),
            null, // metadata - parse from JSON string if needed
            payment.getCreatedDate(),
            payment.getLastModifiedDate()
        );
    }

    private void publishPaymentEvent(String eventType, PaymentResponseDTO payment) {
        try {
            kafkaTemplate.send("payment-events", eventType, payment);
        } catch (Exception e) {
            logger.error("Failed to publish payment event: " + eventType, e);
        }
    }

    private void publishRefundEvent(String eventType, PaymentResponseDTO payment, RefundRequestDTO refundRequest) {
        try {
            kafkaTemplate.send("payment-events", eventType, Map.of(
                "payment", payment,
                "refund", refundRequest.sanitizeForLogging()
            ));
        } catch (Exception e) {
            logger.error("Failed to publish refund event: " + eventType, e);
        }
    }
}
