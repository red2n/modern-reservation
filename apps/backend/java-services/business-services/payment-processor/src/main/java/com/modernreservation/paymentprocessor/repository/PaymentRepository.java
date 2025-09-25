package com.modernreservation.paymentprocessor.repository;

import com.modernreservation.paymentprocessor.entity.Payment;
import com.modernreservation.paymentprocessor.enums.PaymentMethod;
import com.modernreservation.paymentprocessor.enums.PaymentStatus;
import com.modernreservation.paymentprocessor.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 *
 * Repository interface for Payment entity operations.
 * Provides comprehensive data access methods for payment processing,
 * reporting, and analytics with optimized queries.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Basic finders

    /**
     * Find payment by payment reference
     */
    Optional<Payment> findByPaymentReference(String paymentReference);

    /**
     * Find payment by gateway transaction ID
     */
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    /**
     * Find all payments for a reservation
     */
    List<Payment> findByReservationIdOrderByCreatedDateDesc(Long reservationId);

    /**
     * Find all payments for a customer
     */
    Page<Payment> findByCustomerIdOrderByCreatedDateDesc(Long customerId, Pageable pageable);

    /**
     * Find payments by status
     */
    List<Payment> findByStatusOrderByCreatedDateDesc(PaymentStatus status);

    /**
     * Find payments by payment method
     */
    Page<Payment> findByPaymentMethodOrderByCreatedDateDesc(PaymentMethod paymentMethod, Pageable pageable);

    /**
     * Find payments by transaction type
     */
    Page<Payment> findByTransactionTypeOrderByCreatedDateDesc(TransactionType transactionType, Pageable pageable);

    // Advanced queries

    /**
     * Find payments by multiple statuses
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN :statuses ORDER BY p.createdDate DESC")
    List<Payment> findByStatusIn(@Param("statuses") List<PaymentStatus> statuses);

    /**
     * Find payments by date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdDate BETWEEN :startDate AND :endDate ORDER BY p.createdDate DESC")
    Page<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);

    /**
     * Find payments by amount range
     */
    @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount ORDER BY p.amount DESC")
    Page<Payment> findByAmountRange(@Param("minAmount") BigDecimal minAmount,
                                    @Param("maxAmount") BigDecimal maxAmount,
                                    Pageable pageable);

    /**
     * Find payments by customer and status
     */
    @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.status = :status ORDER BY p.createdDate DESC")
    List<Payment> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                           @Param("status") PaymentStatus status);

    /**
     * Find payments by reservation and transaction type
     */
    @Query("SELECT p FROM Payment p WHERE p.reservationId = :reservationId AND p.transactionType = :transactionType ORDER BY p.createdDate DESC")
    List<Payment> findByReservationIdAndTransactionType(@Param("reservationId") Long reservationId,
                                                        @Param("transactionType") TransactionType transactionType);

    /**
     * Find expired payments that are still pending
     */
    @Query("SELECT p FROM Payment p WHERE p.expiresAt < :currentTime AND p.status IN :pendingStatuses")
    List<Payment> findExpiredPendingPayments(@Param("currentTime") LocalDateTime currentTime,
                                           @Param("pendingStatuses") List<PaymentStatus> pendingStatuses);

    /**
     * Find high-risk payments requiring review
     */
    @Query("SELECT p FROM Payment p WHERE p.riskScore >= :riskThreshold AND p.status = :status ORDER BY p.riskScore DESC")
    List<Payment> findHighRiskPayments(@Param("riskThreshold") BigDecimal riskThreshold,
                                      @Param("status") PaymentStatus status);

    /**
     * Find payments with failed fraud checks
     */
    @Query("SELECT p FROM Payment p WHERE p.fraudCheckPassed = false ORDER BY p.createdDate DESC")
    List<Payment> findFailedFraudCheckPayments();

    // Aggregation queries

    /**
     * Get total payment amount by status
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") PaymentStatus status);

    /**
     * Get total payment amount by date range
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.createdDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Get payment count by payment method
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentMethod = :paymentMethod")
    Long getCountByPaymentMethod(@Param("paymentMethod") PaymentMethod paymentMethod);

    /**
     * Get average payment amount by customer
     */
    @Query("SELECT AVG(p.amount) FROM Payment p WHERE p.customerId = :customerId AND p.status = :status")
    BigDecimal getAverageAmountByCustomer(@Param("customerId") Long customerId,
                                         @Param("status") PaymentStatus status);

    /**
     * Get payment statistics by date range
     */
    @Query("""
        SELECT NEW com.modernreservation.paymentprocessor.dto.PaymentStatsDTO(
            COUNT(p),
            SUM(p.amount),
            AVG(p.amount),
            MIN(p.amount),
            MAX(p.amount),
            SUM(p.processingFee)
        )
        FROM Payment p
        WHERE p.createdDate BETWEEN :startDate AND :endDate
        AND p.status = :status
        """)
    Object getPaymentStatsByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("status") PaymentStatus status);

    /**
     * Get refund statistics
     */
    @Query("""
        SELECT NEW com.modernreservation.paymentprocessor.dto.RefundStatsDTO(
            COUNT(p),
            SUM(p.refundedAmount),
            AVG(p.refundedAmount)
        )
        FROM Payment p
        WHERE p.refundedAmount > 0
        AND p.createdDate BETWEEN :startDate AND :endDate
        """)
    Object getRefundStatsByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // Revenue and financial queries

    /**
     * Get total revenue by date range (settled payments only)
     */
    @Query("""
        SELECT SUM(p.amount)
        FROM Payment p
        WHERE p.settledAt BETWEEN :startDate AND :endDate
        AND p.status = 'SETTLED'
        """)
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Get total processing fees by date range
     */
    @Query("""
        SELECT SUM(p.processingFee)
        FROM Payment p
        WHERE p.createdDate BETWEEN :startDate AND :endDate
        AND p.status IN ('SETTLED', 'CAPTURED')
        """)
    BigDecimal getTotalProcessingFeesByDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Get net revenue (after refunds) by date range
     */
    @Query("""
        SELECT SUM(p.amount - COALESCE(p.refundedAmount, 0))
        FROM Payment p
        WHERE p.settledAt BETWEEN :startDate AND :endDate
        AND p.status IN ('SETTLED', 'PARTIALLY_REFUNDED')
        """)
    BigDecimal getNetRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Get payment method revenue breakdown
     */
    @Query("""
        SELECT p.paymentMethod, SUM(p.amount), COUNT(p)
        FROM Payment p
        WHERE p.settledAt BETWEEN :startDate AND :endDate
        AND p.status = 'SETTLED'
        GROUP BY p.paymentMethod
        ORDER BY SUM(p.amount) DESC
        """)
    List<Object[]> getPaymentMethodRevenueBreakdown(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // Update operations

    /**
     * Update payment status
     */
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status, p.lastModifiedDate = :modifiedDate WHERE p.id = :paymentId")
    int updatePaymentStatus(@Param("paymentId") Long paymentId,
                           @Param("status") PaymentStatus status,
                           @Param("modifiedDate") LocalDateTime modifiedDate);

    /**
     * Update gateway transaction details
     */
    @Modifying
    @Query("""
        UPDATE Payment p SET
        p.gatewayTransactionId = :gatewayTransactionId,
        p.authorizationCode = :authorizationCode,
        p.lastModifiedDate = :modifiedDate
        WHERE p.id = :paymentId
        """)
    int updateGatewayDetails(@Param("paymentId") Long paymentId,
                           @Param("gatewayTransactionId") String gatewayTransactionId,
                           @Param("authorizationCode") String authorizationCode,
                           @Param("modifiedDate") LocalDateTime modifiedDate);

    /**
     * Update refunded amount
     */
    @Modifying
    @Query("""
        UPDATE Payment p SET
        p.refundedAmount = :refundedAmount,
        p.status = :status,
        p.lastModifiedDate = :modifiedDate
        WHERE p.id = :paymentId
        """)
    int updateRefundedAmount(@Param("paymentId") Long paymentId,
                           @Param("refundedAmount") BigDecimal refundedAmount,
                           @Param("status") PaymentStatus status,
                           @Param("modifiedDate") LocalDateTime modifiedDate);

    /**
     * Bulk update expired payments
     */
    @Modifying
    @Query("""
        UPDATE Payment p SET
        p.status = 'FAILED',
        p.failureReason = 'Payment expired',
        p.lastModifiedDate = :modifiedDate
        WHERE p.expiresAt < :currentTime
        AND p.status IN :pendingStatuses
        """)
    int markExpiredPaymentsAsFailed(@Param("currentTime") LocalDateTime currentTime,
                                  @Param("pendingStatuses") List<PaymentStatus> pendingStatuses,
                                  @Param("modifiedDate") LocalDateTime modifiedDate);

    // Custom search queries

    /**
     * Search payments by multiple criteria
     */
    @Query("""
        SELECT p FROM Payment p WHERE
        (:customerId IS NULL OR p.customerId = :customerId) AND
        (:reservationId IS NULL OR p.reservationId = :reservationId) AND
        (:status IS NULL OR p.status = :status) AND
        (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) AND
        (:startDate IS NULL OR p.createdDate >= :startDate) AND
        (:endDate IS NULL OR p.createdDate <= :endDate) AND
        (:minAmount IS NULL OR p.amount >= :minAmount) AND
        (:maxAmount IS NULL OR p.amount <= :maxAmount)
        ORDER BY p.createdDate DESC
        """)
    Page<Payment> searchPayments(@Param("customerId") Long customerId,
                               @Param("reservationId") Long reservationId,
                               @Param("status") PaymentStatus status,
                               @Param("paymentMethod") PaymentMethod paymentMethod,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate,
                               @Param("minAmount") BigDecimal minAmount,
                               @Param("maxAmount") BigDecimal maxAmount,
                               Pageable pageable);

    /**
     * Find payments requiring attention (high risk, expired, etc.)
     */
    @Query("""
        SELECT p FROM Payment p WHERE
        (p.riskScore >= 70.0 AND p.status = 'UNDER_REVIEW') OR
        (p.fraudCheckPassed = false) OR
        (p.expiresAt < :currentTime AND p.status IN :pendingStatuses) OR
        (p.status = 'REQUIRES_VERIFICATION')
        ORDER BY p.riskScore DESC, p.createdDate DESC
        """)
    List<Payment> findPaymentsRequiringAttention(@Param("currentTime") LocalDateTime currentTime,
                                               @Param("pendingStatuses") List<PaymentStatus> pendingStatuses);
}
