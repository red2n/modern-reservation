package com.modernreservation.reservationengine.repository;

import com.modernreservation.reservationengine.entity.Reservation;
import com.modernreservation.reservationengine.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reservation Repository - Data access layer for reservation operations
 *
 * Provides comprehensive data access methods for reservation management
 * including queries, updates, and business-specific operations.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    // Basic finder methods
    Optional<Reservation> findByConfirmationNumber(String confirmationNumber);

    List<Reservation> findByGuestId(UUID guestId);

    List<Reservation> findByPropertyId(UUID propertyId);

    Page<Reservation> findByPropertyIdAndStatus(UUID propertyId, ReservationStatus status, Pageable pageable);

    // Date-based queries
    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate <= :endDate AND r.checkOutDate > :startDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'EXPIRED')")
    List<Reservation> findOverlappingReservations(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Additional method overloads for service compatibility
    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate <= :endDate AND r.checkOutDate > :startDate " +
           "AND r.id != :excludeReservationId " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'EXPIRED')")
    List<Reservation> findOverlappingReservations(
        @Param("propertyId") UUID propertyId,
        @Param("excludeReservationId") UUID excludeReservationId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Property reservations with pagination
    Page<Reservation> findByPropertyId(UUID propertyId, Pageable pageable);

    // Date range queries
    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND ((r.checkInDate BETWEEN :startDate AND :endDate) " +
           "OR (r.checkOutDate BETWEEN :startDate AND :endDate) " +
           "OR (r.checkInDate <= :startDate AND r.checkOutDate >= :endDate)) " +
           "ORDER BY r.checkInDate ASC")
    List<Reservation> findReservationsInDateRange(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate >= :date " +
           "AND r.status IN ('CONFIRMED', 'PENDING') " +
           "ORDER BY r.checkInDate ASC")
    List<Reservation> findUpcomingArrivals(
        @Param("propertyId") UUID propertyId,
        @Param("date") LocalDate date
    );

    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkOutDate >= :date " +
           "AND r.status = 'CHECKED_IN' " +
           "ORDER BY r.checkOutDate ASC")
    List<Reservation> findUpcomingDepartures(
        @Param("propertyId") UUID propertyId,
        @Param("date") LocalDate date
    );

    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate BETWEEN :startDate AND :endDate " +
           "ORDER BY r.checkInDate ASC")
    List<Reservation> findByPropertyIdAndCheckInDateBetween(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkOutDate BETWEEN :startDate AND :endDate " +
           "ORDER BY r.checkOutDate ASC")
    List<Reservation> findByPropertyIdAndCheckOutDateBetween(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Today's operations
    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate = :today AND r.status IN ('CONFIRMED', 'PENDING')")
    List<Reservation> findTodayArrivals(@Param("propertyId") UUID propertyId, @Param("today") LocalDate today);

    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkOutDate = :today AND r.status = 'CHECKED_IN'")
    List<Reservation> findTodayDepartures(@Param("propertyId") UUID propertyId, @Param("today") LocalDate today);

    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate = :today AND r.status = 'CHECKED_IN'")
    List<Reservation> findTodayStayovers(@Param("propertyId") UUID propertyId, @Param("today") LocalDate today);

    // Room-specific queries
    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.roomNumber = :roomNumber " +
           "AND r.checkInDate <= :endDate AND r.checkOutDate > :startDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'EXPIRED')")
    List<Reservation> findByRoomNumberAndDateRange(
        @Param("propertyId") UUID propertyId,
        @Param("roomNumber") String roomNumber,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.roomTypeId = :roomTypeId " +
           "AND r.checkInDate <= :endDate AND r.checkOutDate > :startDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'EXPIRED')")
    List<Reservation> findByRoomTypeAndDateRange(
        @Param("propertyId") UUID propertyId,
        @Param("roomTypeId") UUID roomTypeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Guest-related queries
    @Query("SELECT r FROM Reservation r WHERE r.guestEmail = :email " +
           "ORDER BY r.createdAt DESC")
    List<Reservation> findByGuestEmail(@Param("email") String email);

    @Query("SELECT r FROM Reservation r WHERE " +
           "LOWER(r.guestFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(r.guestLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(r.guestEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR r.confirmationNumber LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    Page<Reservation> searchByGuestOrConfirmation(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Status-based operations
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.status = :status")
    long countByPropertyIdAndStatus(@Param("propertyId") UUID propertyId, @Param("status") ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' " +
           "AND r.createdAt < :timeThreshold")
    List<Reservation> findExpiredPendingReservations(@Param("timeThreshold") LocalDateTime timeThreshold);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' " +
           "AND r.checkInDate < :today " +
           "AND r.checkInDate >= :yesterday")
    List<Reservation> findPotentialNoShows(@Param("today") LocalDate today, @Param("yesterday") LocalDate yesterday);

    // Business analytics queries
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate BETWEEN :startDate AND :endDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'EXPIRED')")
    long countReservationsByDateRange(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(r.totalAmount) FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate BETWEEN :startDate AND :endDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'EXPIRED')")
    Double calculateRevenueByDateRange(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT AVG(r.totalAmount) FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate BETWEEN :startDate AND :endDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'EXPIRED')")
    Double calculateAverageRateByDateRange(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Update operations
    @Modifying
    @Query("UPDATE Reservation r SET r.status = :newStatus, r.updatedBy = :updatedBy, r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.id = :reservationId AND r.version = :version")
    int updateReservationStatus(
        @Param("reservationId") UUID reservationId,
        @Param("newStatus") ReservationStatus newStatus,
        @Param("updatedBy") String updatedBy,
        @Param("version") Long version
    );

    @Modifying
    @Query("UPDATE Reservation r SET r.roomNumber = :roomNumber, r.updatedBy = :updatedBy, r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.id = :reservationId")
    int assignRoom(
        @Param("reservationId") UUID reservationId,
        @Param("roomNumber") String roomNumber,
        @Param("updatedBy") String updatedBy
    );

    @Modifying
    @Query("UPDATE Reservation r SET r.status = 'CANCELLED', r.cancelledAt = CURRENT_TIMESTAMP, " +
           "r.cancellationReason = :reason, r.cancelledBy = :cancelledBy, " +
           "r.updatedBy = :updatedBy, r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.id = :reservationId")
    int cancelReservation(
        @Param("reservationId") UUID reservationId,
        @Param("reason") String reason,
        @Param("cancelledBy") String cancelledBy,
        @Param("updatedBy") String updatedBy
    );

    // Channel-specific queries
    @Query("SELECT r FROM Reservation r WHERE r.source = :source " +
           "AND r.checkInDate BETWEEN :startDate AND :endDate")
    List<Reservation> findBySourceAndDateRange(
        @Param("source") String source,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r.source, COUNT(r) FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.checkInDate BETWEEN :startDate AND :endDate " +
           "GROUP BY r.source")
    List<Object[]> getReservationCountBySource(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Check availability helper
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.roomTypeId = :roomTypeId " +
           "AND r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'EXPIRED')")
    long countConflictingReservations(
        @Param("propertyId") UUID propertyId,
        @Param("roomTypeId") UUID roomTypeId,
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate
    );

    // Custom queries for reporting
    @Query("SELECT r FROM Reservation r WHERE r.propertyId = :propertyId " +
           "AND r.updatedAt >= :since " +
           "ORDER BY r.updatedAt DESC")
    List<Reservation> findRecentlyModified(
        @Param("propertyId") UUID propertyId,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT r FROM Reservation r WHERE r.depositDueDate <= :date " +
           "AND r.depositAmount > 0 " +
           "AND r.paymentStatus != 'PAID' " +
           "AND r.status IN ('CONFIRMED', 'PENDING')")
    List<Reservation> findReservationsWithOverdueDeposits(@Param("date") LocalDate date);

    // ==========================================
    // MULTI-TENANCY: Tenant-scoped query methods
    // ==========================================

    // Basic tenant-scoped finders
    Optional<Reservation> findByTenantIdAndId(UUID tenantId, UUID id);

    Optional<Reservation> findByTenantIdAndConfirmationNumber(UUID tenantId, String confirmationNumber);

    List<Reservation> findByTenantIdAndGuestId(UUID tenantId, UUID guestId);

    Page<Reservation> findByTenantIdAndPropertyId(UUID tenantId, UUID propertyId, Pageable pageable);

    Page<Reservation> findByTenantIdAndPropertyIdAndStatus(
        UUID tenantId,
        UUID propertyId,
        ReservationStatus status,
        Pageable pageable
    );

    // Tenant-scoped date range queries
    @Query("SELECT r FROM Reservation r WHERE r.tenantId = :tenantId " +
           "AND r.propertyId = :propertyId " +
           "AND r.checkInDate <= :endDate AND r.checkOutDate > :startDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'EXPIRED')")
    List<Reservation> findOverlappingReservationsByTenant(
        @Param("tenantId") UUID tenantId,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Reservation r WHERE r.tenantId = :tenantId " +
           "AND r.propertyId = :propertyId " +
           "AND r.checkInDate BETWEEN :startDate AND :endDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Reservation> findArrivingGuestsByTenant(
        @Param("tenantId") UUID tenantId,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Reservation r WHERE r.tenantId = :tenantId " +
           "AND r.propertyId = :propertyId " +
           "AND r.checkOutDate BETWEEN :startDate AND :endDate " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT')")
    List<Reservation> findDepartingGuestsByTenant(
        @Param("tenantId") UUID tenantId,
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Tenant-scoped status queries
    @Query("SELECT r FROM Reservation r WHERE r.tenantId = :tenantId " +
           "AND r.status = :status")
    Page<Reservation> findByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") ReservationStatus status,
        Pageable pageable
    );

    // Tenant-scoped count queries
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.tenantId = :tenantId " +
           "AND r.propertyId = :propertyId " +
           "AND r.status = :status")
    long countByTenantIdAndPropertyIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("propertyId") UUID propertyId,
        @Param("status") ReservationStatus status
    );

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.tenantId = :tenantId " +
           "AND r.checkInDate = :date " +
           "AND r.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countTodayArrivals(
        @Param("tenantId") UUID tenantId,
        @Param("date") LocalDate date
    );
}

