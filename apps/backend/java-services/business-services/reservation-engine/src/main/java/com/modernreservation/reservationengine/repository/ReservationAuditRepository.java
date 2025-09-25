package com.modernreservation.reservationengine.repository;

import com.modernreservation.reservationengine.entity.ReservationAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ReservationAudit entity operations
 *
 * Provides comprehensive data access methods for audit trail management
 * including querying by reservation, date ranges, and audit actions.
 */
@Repository
public interface ReservationAuditRepository extends JpaRepository<ReservationAudit, UUID> {

    /**
     * Find audit records for a specific reservation
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.reservation.id = :reservationId ORDER BY ra.createdAt DESC")
    List<ReservationAudit> findByReservationId(@Param("reservationId") UUID reservationId);

    /**
     * Find audit records for a specific reservation with pagination
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.reservation.id = :reservationId ORDER BY ra.createdAt DESC")
    Page<ReservationAudit> findByReservationId(@Param("reservationId") UUID reservationId, Pageable pageable);

    /**
     * Find audit records by action
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.action = :action ORDER BY ra.createdAt DESC")
    List<ReservationAudit> findByAction(@Param("action") String action);

    /**
     * Find audit records by date range
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.createdAt BETWEEN :startDate AND :endDate ORDER BY ra.createdAt DESC")
    List<ReservationAudit> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit records by user
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.performedBy = :performedBy ORDER BY ra.createdAt DESC")
    List<ReservationAudit> findByPerformedBy(@Param("performedBy") String performedBy);

    /**
     * Find recent audit activities
     */
    @Query("SELECT ra FROM ReservationAudit ra ORDER BY ra.createdAt DESC")
    Page<ReservationAudit> findRecentAudits(Pageable pageable);

    /**
     * Count audit records for a reservation
     */
    @Query("SELECT COUNT(ra) FROM ReservationAudit ra WHERE ra.reservation.id = :reservationId")
    Long countByReservationId(@Param("reservationId") UUID reservationId);

    /**
     * Find audit records by property (through reservation)
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.reservation.propertyId = :propertyId ORDER BY ra.createdAt DESC")
    List<ReservationAudit> findByPropertyId(@Param("propertyId") UUID propertyId);

    /**
     * Find audit records by multiple actions
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.action IN :actions ORDER BY ra.createdAt DESC")
    List<ReservationAudit> findByActionIn(@Param("actions") List<String> actions);

    /**
     * Delete old audit records before a specific date
     */
    @Query("DELETE FROM ReservationAudit ra WHERE ra.createdAt < :cutoffDate")
    void deleteOldAudits(@Param("cutoffDate") LocalDateTime cutoffDate);
}
