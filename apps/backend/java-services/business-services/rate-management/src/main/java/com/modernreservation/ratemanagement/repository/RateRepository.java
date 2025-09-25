package com.modernreservation.ratemanagement.repository;

import com.modernreservation.ratemanagement.entity.Rate;
import com.modernreservation.ratemanagement.enums.RateStatus;
import com.modernreservation.ratemanagement.enums.RateStrategy;
import com.modernreservation.ratemanagement.enums.SeasonType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Rate entity operations
 *
 * Provides comprehensive data access methods for rate management,
 * pricing calculations, and revenue optimization.
 */
@Repository
public interface RateRepository extends JpaRepository<Rate, UUID> {

    // Basic rate queries
    @Query("SELECT r FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.roomTypeId = :roomTypeId " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "AND :date BETWEEN r.effectiveDate AND COALESCE(r.expiryDate, :date) " +
           "ORDER BY r.priorityOrder ASC, r.currentRate ASC")
    List<Rate> findActiveRatesForRoomAndDate(@Param("propertyId") UUID propertyId,
                                           @Param("roomTypeId") UUID roomTypeId,
                                           @Param("date") LocalDate date);

    @Query("SELECT r FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "AND :date BETWEEN r.effectiveDate AND COALESCE(r.expiryDate, :date) " +
           "ORDER BY r.roomTypeId, r.priorityOrder ASC")
    List<Rate> findActiveRatesForPropertyAndDate(@Param("propertyId") UUID propertyId,
                                               @Param("date") LocalDate date);

    // Rate search queries
    @Query("SELECT r FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND (:roomTypeId IS NULL OR r.roomTypeId = :roomTypeId) " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "AND :checkInDate BETWEEN r.effectiveDate AND COALESCE(r.expiryDate, :checkInDate) " +
           "AND (:rateStrategy IS NULL OR r.rateStrategy = :rateStrategy) " +
           "AND (:seasonType IS NULL OR r.seasonType = :seasonType) " +
           "AND (:minRate IS NULL OR r.currentRate >= :minRate) " +
           "AND (:maxRate IS NULL OR r.currentRate <= :maxRate) " +
           "AND (:refundableOnly = false OR r.isRefundable = true) " +
           "AND (:modifiableOnly = false OR r.isModifiable = true) " +
           "AND (:advanceBookingDays IS NULL OR r.advanceBookingDays IS NULL OR r.advanceBookingDays <= :advanceBookingDays) " +
           "ORDER BY r.priorityOrder ASC, r.currentRate ASC")
    List<Rate> searchRates(@Param("propertyId") UUID propertyId,
                          @Param("roomTypeId") UUID roomTypeId,
                          @Param("checkInDate") LocalDate checkInDate,
                          @Param("rateStrategy") RateStrategy rateStrategy,
                          @Param("seasonType") SeasonType seasonType,
                          @Param("minRate") BigDecimal minRate,
                          @Param("maxRate") BigDecimal maxRate,
                          @Param("refundableOnly") Boolean refundableOnly,
                          @Param("modifiableOnly") Boolean modifiableOnly,
                          @Param("advanceBookingDays") Integer advanceBookingDays);

    // Best rate queries
    @Query("SELECT r FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.roomTypeId = :roomTypeId " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "AND :date BETWEEN r.effectiveDate AND COALESCE(r.expiryDate, :date) " +
           "AND (:minStay IS NULL OR r.minimumStay IS NULL OR r.minimumStay <= :minStay) " +
           "AND (:maxStay IS NULL OR r.maximumStay IS NULL OR r.maximumStay >= :maxStay) " +
           "ORDER BY r.currentRate ASC " +
           "LIMIT 1")
    Optional<Rate> findBestRateForRoomAndDate(@Param("propertyId") UUID propertyId,
                                            @Param("roomTypeId") UUID roomTypeId,
                                            @Param("date") LocalDate date,
                                            @Param("minStay") Integer minStay,
                                            @Param("maxStay") Integer maxStay);

    // Rate statistics queries
    @Query("SELECT MIN(r.currentRate) FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "AND :date BETWEEN r.effectiveDate AND COALESCE(r.expiryDate, :date)")
    Optional<BigDecimal> findMinRateForPropertyAndDate(@Param("propertyId") UUID propertyId,
                                                      @Param("date") LocalDate date);

    @Query("SELECT MAX(r.currentRate) FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "AND :date BETWEEN r.effectiveDate AND COALESCE(r.expiryDate, :date)")
    Optional<BigDecimal> findMaxRateForPropertyAndDate(@Param("propertyId") UUID propertyId,
                                                      @Param("date") LocalDate date);

    @Query("SELECT AVG(r.currentRate) FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "AND :date BETWEEN r.effectiveDate AND COALESCE(r.expiryDate, :date)")
    Optional<BigDecimal> findAverageRateForPropertyAndDate(@Param("propertyId") UUID propertyId,
                                                          @Param("date") LocalDate date);

    // Strategy-based queries
    @Query("SELECT r FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.rateStrategy = :strategy " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "ORDER BY r.priorityOrder ASC")
    List<Rate> findRatesByStrategy(@Param("propertyId") UUID propertyId,
                                  @Param("strategy") RateStrategy strategy);

    @Query("SELECT r FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.seasonType = :seasonType " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "AND :date BETWEEN r.effectiveDate AND COALESCE(r.expiryDate, :date) " +
           "ORDER BY r.currentRate ASC")
    List<Rate> findRatesBySeasonType(@Param("propertyId") UUID propertyId,
                                    @Param("seasonType") SeasonType seasonType,
                                    @Param("date") LocalDate date);

    // Date range queries
    @Query("SELECT r FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.roomTypeId = :roomTypeId " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "AND ((r.effectiveDate <= :endDate AND COALESCE(r.expiryDate, :endDate) >= :startDate)) " +
           "ORDER BY r.effectiveDate ASC")
    List<Rate> findRatesForDateRange(@Param("propertyId") UUID propertyId,
                                   @Param("roomTypeId") UUID roomTypeId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    // Expiry and maintenance queries
    @Query("SELECT r FROM Rate r WHERE r.expiryDate IS NOT NULL " +
           "AND r.expiryDate BETWEEN :startDate AND :endDate " +
           "AND r.rateStatus = 'ACTIVE' " +
           "ORDER BY r.expiryDate ASC")
    List<Rate> findRatesExpiringInRange(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM Rate r WHERE r.rateStatus = :status " +
           "ORDER BY r.updatedAt DESC")
    List<Rate> findRatesByStatus(@Param("status") RateStatus status);

    // Duplicate and validation queries
    @Query("SELECT COUNT(r) > 0 FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.roomTypeId = :roomTypeId " +
           "AND r.rateCode = :rateCode " +
           "AND r.id != :excludeId")
    Boolean existsDuplicateRateCode(@Param("propertyId") UUID propertyId,
                                   @Param("roomTypeId") UUID roomTypeId,
                                   @Param("rateCode") String rateCode,
                                   @Param("excludeId") UUID excludeId);

    // Update operations
    @Modifying
    @Query("UPDATE Rate r SET r.rateStatus = :newStatus, " +
           "r.updatedBy = :updatedBy, " +
           "r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.id = :rateId")
    int updateRateStatus(@Param("rateId") UUID rateId,
                        @Param("newStatus") RateStatus newStatus,
                        @Param("updatedBy") String updatedBy);

    @Modifying
    @Query("UPDATE Rate r SET r.currentRate = :newRate, " +
           "r.occupancyMultiplier = :occupancyMultiplier, " +
           "r.demandMultiplier = :demandMultiplier, " +
           "r.competitiveAdjustment = :competitiveAdjustment, " +
           "r.updatedBy = :updatedBy, " +
           "r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.id = :rateId")
    int updateRatePricing(@Param("rateId") UUID rateId,
                         @Param("newRate") BigDecimal newRate,
                         @Param("occupancyMultiplier") BigDecimal occupancyMultiplier,
                         @Param("demandMultiplier") BigDecimal demandMultiplier,
                         @Param("competitiveAdjustment") BigDecimal competitiveAdjustment,
                         @Param("updatedBy") String updatedBy);

    @Modifying
    @Query("UPDATE Rate r SET r.rateStatus = 'EXPIRED', " +
           "r.isActive = false, " +
           "r.updatedBy = 'SYSTEM', " +
           "r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.expiryDate < CURRENT_DATE " +
           "AND r.rateStatus = 'ACTIVE'")
    int expireOutdatedRates();

    // Pagination queries
    @Query("SELECT r FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND (:status IS NULL OR r.rateStatus = :status) " +
           "ORDER BY r.createdAt DESC")
    Page<Rate> findRatesWithPagination(@Param("propertyId") UUID propertyId,
                                      @Param("status") RateStatus status,
                                      Pageable pageable);

    // Revenue optimization queries
    @Query("SELECT r FROM Rate r WHERE r.propertyId = :propertyId " +
           "AND r.rateStrategy = 'REVENUE_OPTIMIZATION' " +
           "AND r.rateStatus = 'ACTIVE' " +
           "AND r.isActive = true " +
           "ORDER BY (r.currentRate * COALESCE(r.occupancyMultiplier, 1.0) * COALESCE(r.demandMultiplier, 1.0)) DESC")
    List<Rate> findRevenueOptimizedRates(@Param("propertyId") UUID propertyId);

    // Custom finder methods
    Optional<Rate> findByPropertyIdAndRoomTypeIdAndRateCode(UUID propertyId, UUID roomTypeId, String rateCode);

    List<Rate> findByPropertyIdAndRateStrategyAndRateStatus(UUID propertyId, RateStrategy rateStrategy, RateStatus rateStatus);

    List<Rate> findByCreatedByAndRateStatus(String createdBy, RateStatus rateStatus);
}
