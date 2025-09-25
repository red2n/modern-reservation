package com.modernreservation.availabilitycalculator.controller;

import com.modernreservation.availabilitycalculator.dto.*;
import com.modernreservation.availabilitycalculator.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Availability Calculator Controller - REST API endpoints for room availability management
 *
 * Provides comprehensive availability search, pricing calculations, and inventory management
 * operations with modern Java patterns and OpenAPI documentation.
 */
@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Availability Calculator", description = "Room availability and pricing management API")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @Operation(
        summary = "Search room availability",
        description = "Search for available rooms based on criteria including dates, property, category, and pricing filters"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved availability results"),
        @ApiResponse(responseCode = "400", description = "Invalid search criteria", content = @Content),
        @ApiResponse(responseCode = "404", description = "No availability found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/search")
    public ResponseEntity<List<AvailabilityResponseDTO>> searchAvailability(
            @Valid @RequestBody AvailabilitySearchRequestDTO request) {

        log.info("Availability search request for property {} from {} to {}",
                request.propertyId(), request.checkInDate(), request.checkOutDate());

        try {
            List<AvailabilityResponseDTO> results = availabilityService.searchAvailability(request);

            if (results.isEmpty()) {
                log.info("No availability found for search criteria");
                return ResponseEntity.noContent().build();
            }

            log.info("Found {} available room types", results.size());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Error during availability search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Calculate pricing for stay",
        description = "Calculate detailed pricing including taxes, fees, and discounts for a specific room type and date range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated pricing"),
        @ApiResponse(responseCode = "400", description = "Invalid pricing request", content = @Content),
        @ApiResponse(responseCode = "404", description = "Room type not available", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/calculate-pricing")
    public ResponseEntity<PricingCalculationResponseDTO> calculatePricing(
            @Valid @RequestBody PricingCalculationRequestDTO request) {

        log.info("Pricing calculation request for room type {} from {} to {}",
                request.roomTypeId(), request.checkInDate(), request.checkOutDate());

        try {
            PricingCalculationResponseDTO pricing = availabilityService.calculatePricing(request);
            log.info("Successfully calculated pricing: total amount {}", pricing.totalAmount());
            return ResponseEntity.ok(pricing);

        } catch (RuntimeException e) {
            log.error("Error calculating pricing: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Unexpected error during pricing calculation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Get daily availability",
        description = "Retrieve availability information for all room types at a property on a specific date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved daily availability"),
        @ApiResponse(responseCode = "400", description = "Invalid property ID or date", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/daily/{propertyId}")
    public ResponseEntity<List<AvailabilityResponseDTO>> getDailyAvailability(
            @Parameter(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID propertyId,

            @Parameter(description = "Date for availability check", example = "2024-12-25")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Getting daily availability for property {} on {}", propertyId, date);

        try {
            List<AvailabilityResponseDTO> availability = availabilityService.getDailyAvailability(propertyId, date);
            log.info("Found availability for {} room types", availability.size());
            return ResponseEntity.ok(availability);

        } catch (Exception e) {
            log.error("Error retrieving daily availability: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Get availability with pagination",
        description = "Retrieve paginated availability information for a property on a specific date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated availability"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/daily/{propertyId}/paginated")
    public ResponseEntity<Page<AvailabilityResponseDTO>> getAvailabilityWithPagination(
            @Parameter(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID propertyId,

            @Parameter(description = "Date for availability check", example = "2024-12-25")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            Pageable pageable) {

        log.info("Getting paginated availability for property {} on {} - page {}, size {}",
                propertyId, date, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<AvailabilityResponseDTO> availabilityPage = availabilityService
                .getAvailabilityWithPagination(propertyId, date, pageable);

            log.info("Retrieved page {} of {} with {} items",
                    availabilityPage.getNumber(), availabilityPage.getTotalPages(),
                    availabilityPage.getNumberOfElements());

            return ResponseEntity.ok(availabilityPage);

        } catch (Exception e) {
            log.error("Error retrieving paginated availability: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Update room availability",
        description = "Update availability information for a specific room/date combination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated availability"),
        @ApiResponse(responseCode = "400", description = "Invalid update request", content = @Content),
        @ApiResponse(responseCode = "404", description = "Availability record not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponseDTO> updateAvailability(
            @Parameter(description = "Availability record ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID availabilityId,

            @Valid @RequestBody AvailabilityUpdateRequestDTO request,

            @Parameter(description = "User making the update", example = "admin@hotel.com")
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String updatedBy) {

        log.info("Updating availability {} by user {}", availabilityId, updatedBy);

        try {
            if (!request.hasValidData()) {
                return ResponseEntity.badRequest().build();
            }

            AvailabilityResponseDTO updated = availabilityService.updateAvailability(
                availabilityId, request, updatedBy);

            log.info("Successfully updated availability {}", availabilityId);
            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            log.error("Error updating availability: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Unexpected error updating availability: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Book rooms",
        description = "Update availability counts when rooms are booked"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully booked rooms"),
        @ApiResponse(responseCode = "400", description = "Invalid booking request", content = @Content),
        @ApiResponse(responseCode = "404", description = "Room type not found", content = @Content),
        @ApiResponse(responseCode = "409", description = "Insufficient availability", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/book")
    public ResponseEntity<Void> bookRooms(
            @Parameter(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID propertyId,

            @Parameter(description = "Room type ID", example = "550e8400-e29b-41d4-a716-446655440001")
            @RequestParam UUID roomTypeId,

            @Parameter(description = "Check-in date", example = "2024-12-25")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,

            @Parameter(description = "Check-out date", example = "2024-12-27")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,

            @Parameter(description = "Number of rooms to book", example = "2")
            @RequestParam int roomsBooked,

            @Parameter(description = "User making the booking", example = "reservations@hotel.com")
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String bookedBy) {

        log.info("Booking {} rooms for room type {} from {} to {} by {}",
                roomsBooked, roomTypeId, checkInDate, checkOutDate, bookedBy);

        try {
            availabilityService.bookRooms(propertyId, roomTypeId, checkInDate,
                                        checkOutDate, roomsBooked, bookedBy);

            log.info("Successfully booked {} rooms", roomsBooked);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            log.error("Error booking rooms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            log.error("Unexpected error booking rooms: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Get occupancy statistics",
        description = "Retrieve occupancy statistics for a property over a date range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved occupancy statistics"),
        @ApiResponse(responseCode = "400", description = "Invalid date range", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/occupancy/{propertyId}")
    public ResponseEntity<OccupancyStatsDTO> getOccupancyStatistics(
            @Parameter(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID propertyId,

            @Parameter(description = "Start date for statistics", example = "2024-12-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date for statistics", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting occupancy statistics for property {} from {} to {}",
                propertyId, startDate, endDate);

        try {
            if (endDate.isBefore(startDate)) {
                return ResponseEntity.badRequest().build();
            }

            OccupancyStatsDTO stats = availabilityService.getOccupancyStatistics(
                propertyId, startDate, endDate);

            log.info("Retrieved occupancy statistics: avg occupancy {}%",
                    stats.averageOccupancy());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error retrieving occupancy statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Health check",
        description = "Check if the availability calculator service is healthy"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Availability Calculator Service is running");
    }
}
