package com.modernreservation.availabilitycalculator.repository;

import com.modernreservation.availabilitycalculator.entity.RoomAvailability;
import com.modernreservation.availabilitycalculator.enums.AvailabilityStatus;
import com.modernreservation.availabilitycalculator.enums.RoomCategory;
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
 * Repository interface for RoomAvailability entity operations
 *
 * Provides comprehensive data access methods for availability management,
 * pricing calculations, and room inventory tracking.
 */
@Repository
public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, UUID> {

    // Basic availability queries
    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate = :date " +
           "ORDER BY ra.roomCategory ASC, ra.currentRate ASC")
    List<RoomAvailability> findByPropertyAndDate(@Param("propertyId") UUID propertyId,
                                                 @Param("date") LocalDate date);

    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ra.availabilityDate ASC, ra.roomCategory ASC")
    List<RoomAvailability> findByPropertyAndDateRange(@Param("propertyId") UUID propertyId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    // Availability search queries
    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.roomTypeId = :roomTypeId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND ra.availabilityStatus = 'AVAILABLE' " +
           "AND ra.availableRooms > 0 " +
           "AND ra.stopSell = false " +
           "ORDER BY ra.availabilityDate ASC")
    List<RoomAvailability> findAvailableRoomsByTypeAndDateRange(
            @Param("propertyId") UUID propertyId,
            @Param("roomTypeId") UUID roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND ra.roomCategory = :category " +
           "AND ra.availabilityStatus = 'AVAILABLE' " +
           "AND ra.availableRooms >= :roomsNeeded " +
           "AND ra.stopSell = false " +
           "AND ra.closedToArrival = false " +
           "ORDER BY ra.availabilityDate ASC, ra.currentRate ASC")
    List<RoomAvailability> findAvailableRoomsByCategory(
            @Param("propertyId") UUID propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") RoomCategory category,
            @Param("roomsNeeded") Integer roomsNeeded);

    // Pricing queries
    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND ra.currentRate BETWEEN :minRate AND :maxRate " +
           "AND ra.availabilityStatus = 'AVAILABLE' " +
           "ORDER BY ra.currentRate ASC")
    List<RoomAvailability> findByPropertyDateRangeAndPriceRange(
            @Param("propertyId") UUID propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minRate") BigDecimal minRate,
            @Param("maxRate") BigDecimal maxRate);

    @Query("SELECT MIN(ra.currentRate) FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND ra.availabilityStatus = 'AVAILABLE' " +
           "AND ra.availableRooms > 0")
    Optional<BigDecimal> findMinRateForDateRange(@Param("propertyId") UUID propertyId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT MAX(ra.currentRate) FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND ra.availabilityStatus = 'AVAILABLE' " +
           "AND ra.availableRooms > 0")
    Optional<BigDecimal> findMaxRateForDateRange(@Param("propertyId") UUID propertyId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // Occupancy and statistics queries
    @Query("SELECT AVG(CAST(ra.occupiedRooms AS double) / ra.totalRooms * 100) " +
           "FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate")
    Optional<Double> findAverageOccupancyForDateRange(@Param("propertyId") UUID propertyId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(ra.availableRooms) FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate = :date " +
           "AND ra.availabilityStatus = 'AVAILABLE'")
    Optional<Integer> findTotalAvailableRoomsForDate(@Param("propertyId") UUID propertyId,
                                                    @Param("date") LocalDate date);

    @Query("SELECT COUNT(ra) FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND ra.availableRooms = 0")
    Long countSoldOutDates(@Param("propertyId") UUID propertyId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    // Room type specific queries
    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.roomTypeId = :roomTypeId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ra.availabilityDate ASC")
    List<RoomAvailability> findByRoomTypeAndDateRange(@Param("roomTypeId") UUID roomTypeId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT ra.roomCategory FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate = :date " +
           "AND ra.availabilityStatus = 'AVAILABLE' " +
           "AND ra.availableRooms > 0")
    List<RoomCategory> findAvailableRoomCategoriesForDate(@Param("propertyId") UUID propertyId,
                                                         @Param("date") LocalDate date);

    // Restriction queries
    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND (ra.closedToArrival = true OR ra.closedToDeparture = true OR ra.stopSell = true)")
    List<RoomAvailability> findRestrictedDates(@Param("propertyId") UUID propertyId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(ra) > 0 FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.roomTypeId = :roomTypeId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND (ra.minimumStay > :nightsStay OR ra.stopSell = true)")
    Boolean hasStayRestrictions(@Param("propertyId") UUID propertyId,
                               @Param("roomTypeId") UUID roomTypeId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               @Param("nightsStay") Integer nightsStay);

    // Update operations
    @Modifying
    @Query("UPDATE RoomAvailability ra SET ra.availableRooms = ra.availableRooms - :roomsBooked, " +
           "ra.occupiedRooms = ra.occupiedRooms + :roomsBooked, " +
           "ra.updatedBy = :updatedBy " +
           "WHERE ra.propertyId = :propertyId " +
           "AND ra.roomTypeId = :roomTypeId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate")
    int updateRoomCounts(@Param("propertyId") UUID propertyId,
                        @Param("roomTypeId") UUID roomTypeId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("roomsBooked") Integer roomsBooked,
                        @Param("updatedBy") String updatedBy);

    @Modifying
    @Query("UPDATE RoomAvailability ra SET ra.currentRate = :newRate, " +
           "ra.updatedBy = :updatedBy " +
           "WHERE ra.propertyId = :propertyId " +
           "AND ra.roomTypeId = :roomTypeId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate")
    int updateRatesForDateRange(@Param("propertyId") UUID propertyId,
                               @Param("roomTypeId") UUID roomTypeId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               @Param("newRate") BigDecimal newRate,
                               @Param("updatedBy") String updatedBy);

    // Specific date and room lookups
        // Basic single record retrieval
    Optional<RoomAvailability> findByPropertyIdAndRoomTypeIdAndAvailabilityDate(
            UUID propertyId, UUID roomTypeId, LocalDate availabilityDate);

    // Service layer specific methods
    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "AND ra.availabilityStatus IN ('AVAILABLE', 'LIMITED') " +
           "AND ra.availableRooms > 0 " +
           "ORDER BY ra.availabilityDate ASC, ra.roomCategory ASC")
    List<RoomAvailability> findAvailableRoomsByPropertyAndDateRange(
            @Param("propertyId") UUID propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate = :date " +
           "ORDER BY ra.roomCategory ASC")
    List<RoomAvailability> findByPropertyIdAndAvailabilityDate(
            @Param("propertyId") UUID propertyId,
            @Param("date") LocalDate date);

    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate = :date " +
           "ORDER BY ra.currentRate ASC")
    Page<RoomAvailability> findByPropertyAndDateWithPagination(@Param("propertyId") UUID propertyId,
                                                              @Param("date") LocalDate date,
                                                              Pageable pageable);

    // Maintenance and blocked room queries
    @Query("SELECT SUM(ra.maintenanceRooms + ra.blockedRooms) FROM RoomAvailability ra " +
           "WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate = :date")
    Optional<Integer> findTotalOutOfOrderRoomsForDate(@Param("propertyId") UUID propertyId,
                                                     @Param("date") LocalDate date);

    // Revenue optimization queries
    @Query("SELECT ra FROM RoomAvailability ra WHERE ra.propertyId = :propertyId " +
           "AND ra.availabilityDate BETWEEN :startDate AND :endDate " +
           "ORDER BY (ra.currentRate * ra.availableRooms) DESC")
    List<RoomAvailability> findByRevenuePotential(@Param("propertyId") UUID propertyId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}
