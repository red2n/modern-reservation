package com.modernreservation.paymentprocessor.controller;

import com.modernreservation.paymentprocessor.dto.*;
import com.modernreservation.paymentprocessor.enums.PaymentMethod;
import com.modernreservation.paymentprocessor.enums.PaymentStatus;
import com.modernreservation.paymentprocessor.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Controller
 *
 * REST API controller for payment processing operations.
 * Provides comprehensive payment management endpoints including
 * creation, authorization, capture, settlement, refunds, and analytics.
 *
 * Security Features:
 * - Request validation and sanitization
 * - Rate limiting and fraud detection
 * - PCI compliance measures
 * - Audit logging and monitoring
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Management", description = "Payment processing and management operations")
@Validated
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Payment Creation and Processing

    @PostMapping
    @Operation(
        summary = "Create a new payment",
        description = "Creates a new payment transaction with the specified details. " +
                     "Supports various payment methods including cards, digital wallets, and alternative payments."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payment request"),
        @ApiResponse(responseCode = "422", description = "Payment validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody PaymentRequestDTO paymentRequest) {

        logger.info("Creating payment for reservation: {} amount: {} {}",
                   paymentRequest.reservationId(), paymentRequest.amount(), paymentRequest.currency());

        try {
            PaymentResponseDTO response = paymentService.createPayment(paymentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Payment creation failed due to validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Payment creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment", e);
        }
    }

    @PostMapping("/{paymentId}/authorize")
    @Operation(
        summary = "Authorize a payment",
        description = "Authorizes a pending payment, reserving funds without capturing them. " +
                     "Typically used for card payments that need to be captured later."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment authorized successfully"),
        @ApiResponse(responseCode = "400", description = "Payment cannot be authorized"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "409", description = "Payment in invalid state for authorization")
    })
    public ResponseEntity<PaymentResponseDTO> authorizePayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable @Positive Long paymentId) {

        logger.info("Authorizing payment: {}", paymentId);

        try {
            PaymentResponseDTO response = paymentService.authorizePayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Payment authorization failed - not found: {}", paymentId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.error("Payment authorization failed - invalid state: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{paymentId}/capture")
    @Operation(
        summary = "Capture an authorized payment",
        description = "Captures an authorized payment, transferring funds from customer to merchant. " +
                     "Supports full or partial capture amounts."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment captured successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid capture amount"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "409", description = "Payment cannot be captured")
    })
    public ResponseEntity<PaymentResponseDTO> capturePayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable @Positive Long paymentId,

            @Parameter(description = "Amount to capture (null for full amount)")
            @RequestParam(required = false)
            @DecimalMin(value = "0.01", message = "Capture amount must be greater than 0") BigDecimal amount) {

        logger.info("Capturing payment: {} amount: {}", paymentId, amount);

        try {
            PaymentResponseDTO response = paymentService.capturePayment(paymentId, amount);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Payment capture failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.error("Payment capture failed - invalid state: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{paymentId}/settle")
    @Operation(
        summary = "Settle a captured payment",
        description = "Settles a captured payment, completing the payment lifecycle. " +
                     "This represents the final transfer of funds to the merchant account."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment settled successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "409", description = "Payment cannot be settled")
    })
    public ResponseEntity<PaymentResponseDTO> settlePayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable @Positive Long paymentId) {

        logger.info("Settling payment: {}", paymentId);

        try {
            PaymentResponseDTO response = paymentService.settlePayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Payment settlement failed - not found: {}", paymentId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.error("Payment settlement failed - invalid state: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // Refund Operations

    @PostMapping("/refunds")
    @Operation(
        summary = "Process a refund",
        description = "Processes a full or partial refund for a completed payment. " +
                     "Supports various refund reasons and notification options."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid refund request"),
        @ApiResponse(responseCode = "409", description = "Payment cannot be refunded")
    })
    public ResponseEntity<PaymentResponseDTO> processRefund(
            @Valid @RequestBody RefundRequestDTO refundRequest) {

        logger.info("Processing refund for payment: {} amount: {} reason: {}",
                   refundRequest.paymentId(), refundRequest.amount(), refundRequest.reason());

        try {
            PaymentResponseDTO response = paymentService.processRefund(refundRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Refund processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.error("Refund processing failed - invalid state: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // Payment Retrieval

    @GetMapping("/{paymentId}")
    @Operation(
        summary = "Get payment by ID",
        description = "Retrieves detailed information about a specific payment transaction."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponseDTO> getPaymentById(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable @Positive Long paymentId) {

        try {
            PaymentResponseDTO response = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reference/{paymentReference}")
    @Operation(
        summary = "Get payment by reference",
        description = "Retrieves payment information using the human-readable payment reference."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponseDTO> getPaymentByReference(
            @Parameter(description = "Payment reference", required = true)
            @PathVariable String paymentReference) {

        try {
            PaymentResponseDTO response = paymentService.getPaymentByReference(paymentReference);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reservation/{reservationId}")
    @Operation(
        summary = "Get payments by reservation (full details)",
        description = "Retrieves all payments associated with a specific reservation with complete details."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
    })
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByReservation(
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable @Positive Long reservationId) {

        List<PaymentResponseDTO> payments = paymentService.getPaymentsByReservation(reservationId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/reservation/{reservationId}/summary")
    @Operation(
        summary = "Get payments by reservation (summary only - optimized)",
        description = "Retrieves lightweight payment summaries for a reservation. " +
                "Reduces payload size by ~70% compared to full details. " +
                "Perfect for transaction lists and payment history tables."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment summaries retrieved successfully")
    })
    public ResponseEntity<List<PaymentSummaryDTO>> getPaymentsSummaryByReservation(
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable @Positive Long reservationId) {

        List<PaymentSummaryDTO> payments = paymentService.getPaymentsSummaryByReservation(reservationId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(
        summary = "Get payments by customer (full details)",
        description = "Retrieves paginated payments for a specific customer with complete details."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
    })
    public ResponseEntity<Page<PaymentResponseDTO>> getPaymentsByCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable @Positive Long customerId,
            Pageable pageable) {

        Page<PaymentResponseDTO> payments = paymentService.getPaymentsByCustomer(customerId, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/customer/{customerId}/summary")
    @Operation(
        summary = "Get payments by customer (summary only - optimized)",
        description = "Retrieves paginated lightweight payment summaries for a customer. " +
                "Reduces payload size by ~70% compared to full details. " +
                "Perfect for customer payment history lists."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment summaries retrieved successfully")
    })
    public ResponseEntity<Page<PaymentSummaryDTO>> getPaymentsSummaryByCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable @Positive Long customerId,
            Pageable pageable) {

        Page<PaymentSummaryDTO> payments = paymentService.getPaymentsSummaryByCustomer(customerId, pageable);
        return ResponseEntity.ok(payments);
    }

    // Payment Search

    @GetMapping("/search")
    @Operation(
        summary = "Search payments",
        description = "Search payments using multiple criteria with pagination support."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<PaymentResponseDTO>> searchPayments(
            @Parameter(description = "Customer ID filter")
            @RequestParam(required = false) Long customerId,

            @Parameter(description = "Reservation ID filter")
            @RequestParam(required = false) Long reservationId,

            @Parameter(description = "Payment status filter")
            @RequestParam(required = false) PaymentStatus status,

            @Parameter(description = "Payment method filter")
            @RequestParam(required = false) PaymentMethod paymentMethod,

            @Parameter(description = "Start date filter (ISO format)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "End date filter (ISO format)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "Minimum amount filter")
            @RequestParam(required = false)
            @DecimalMin(value = "0.00") BigDecimal minAmount,

            @Parameter(description = "Maximum amount filter")
            @RequestParam(required = false)
            @DecimalMin(value = "0.00") BigDecimal maxAmount,

            Pageable pageable) {

        Page<PaymentResponseDTO> payments = paymentService.searchPayments(
            customerId, reservationId, status, paymentMethod,
            startDate, endDate, minAmount, maxAmount, pageable);

        return ResponseEntity.ok(payments);
    }

    // Analytics and Reporting

    @GetMapping("/statistics")
    @Operation(
        summary = "Get payment statistics",
        description = "Retrieves payment statistics for a specified date range and status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<PaymentStatsDTO> getPaymentStatistics(
            @Parameter(description = "Start date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "End date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "Payment status filter", required = true)
            @RequestParam PaymentStatus status) {

        PaymentStatsDTO stats = paymentService.getPaymentStatistics(startDate, endDate, status);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue/total")
    @Operation(
        summary = "Get total revenue",
        description = "Retrieves total revenue (settled payments) for a specified date range."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue retrieved successfully")
    })
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @Parameter(description = "Start date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "End date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        BigDecimal revenue = paymentService.getTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/net")
    @Operation(
        summary = "Get net revenue",
        description = "Retrieves net revenue (after refunds) for a specified date range."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Net revenue retrieved successfully")
    })
    public ResponseEntity<BigDecimal> getNetRevenue(
            @Parameter(description = "Start date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "End date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        BigDecimal netRevenue = paymentService.getNetRevenue(startDate, endDate);
        return ResponseEntity.ok(netRevenue);
    }

    // Administrative Operations

    @GetMapping("/attention")
    @Operation(
        summary = "Get payments requiring attention",
        description = "Retrieves payments that require manual review or attention (high risk, expired, etc.)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
    })
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsRequiringAttention() {
        List<PaymentResponseDTO> payments = paymentService.getPaymentsRequiringAttention();
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/maintenance/expired")
    @Operation(
        summary = "Process expired payments",
        description = "Batch operation to mark expired pending payments as failed."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expired payments processed successfully")
    })
    public ResponseEntity<Integer> processExpiredPayments() {
        int processedCount = paymentService.processExpiredPayments();
        return ResponseEntity.ok(processedCount);
    }

    // Exception Handling

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Validation error: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        logger.error("State error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                           .body("An unexpected error occurred");
    }
}
