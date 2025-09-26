package com.modernreservation.reservationengine.repository;

import com.modernreservation.reservationengine.entity.ReservationAudit;
import com.modernreservation.reservationengine.enums.ReservationStatus;
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
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.reservation.id = :reservationId ORDER BY ra.changedAt DESC")
    List<ReservationAudit> findByReservationId(@Param("reservationId") UUID reservationId);

    /**
     * Find audit records for a specific reservation with pagination
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.reservation.id = :reservationId ORDER BY ra.changedAt DESC")
    Page<ReservationAudit> findByReservationId(@Param("reservationId") UUID reservationId, Pageable pageable);

    /**
     * Find audit records by status change (old status to new status)
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.oldStatus = :oldStatus AND ra.newStatus = :newStatus ORDER BY ra.changedAt DESC")
    List<ReservationAudit> findByStatusChange(@Param("oldStatus") ReservationStatus oldStatus, @Param("newStatus") ReservationStatus newStatus);

    /**
     * Find audit records by date range
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.changedAt BETWEEN :startDate AND :endDate ORDER BY ra.changedAt DESC")
    List<ReservationAudit> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit records by user
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.changedBy = :changedBy ORDER BY ra.changedAt DESC")
    List<ReservationAudit> findByChangedBy(@Param("changedBy") UUID changedBy);

    /**
     * Find recent audit activities
     */
    @Query("SELECT ra FROM ReservationAudit ra ORDER BY ra.changedAt DESC")
    Page<ReservationAudit> findRecentAudits(Pageable pageable);

    /**
     * Count audit records for a reservation
     */
    @Query("SELECT COUNT(ra) FROM ReservationAudit ra WHERE ra.reservation.id = :reservationId")
    Long countByReservationId(@Param("reservationId") UUID reservationId);

    /**
     * Find audit records by property (through reservation)
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.reservation.propertyId = :propertyId ORDER BY ra.changedAt DESC")
    List<ReservationAudit> findByPropertyId(@Param("propertyId") UUID propertyId);

    /**
     * Find audit records by multiple actions
     */
    @Query("SELECT ra FROM ReservationAudit ra WHERE ra.oldStatus IN :statuses OR ra.newStatus IN :statuses ORDER BY ra.changedAt DESC")
    List<ReservationAudit> findByStatusIn(@Param("statuses") List<ReservationStatus> statuses);

    /**
     * Delete old audit records before a specific date
     */
    @Query("DELETE FROM ReservationAudit ra WHERE ra.changedAt < :cutoffDate")
    void deleteOldAudits(@Param("cutoffDate") LocalDateTime cutoffDate);
}
