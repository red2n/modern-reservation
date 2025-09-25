package com.modernreservation.ratemanagement.controller;

import com.modernreservation.ratemanagement.dto.*;
import com.modernreservation.ratemanagement.enums.RateStatus;
import com.modernreservation.ratemanagement.service.RateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for Rate Management operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rates")
@RequiredArgsConstructor
@Tag(name = "Rate Management", description = "Rate creation, pricing strategies, and revenue optimization")
public class RateController {

    private final RateService rateService;

    @Operation(
        summary = "Create a new rate",
        description = "Create a new rate with pricing strategy and terms"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Rate created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Rate code already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<RateResponseDTO> createRate(
            @Valid @RequestBody RateCreationRequestDTO request) {

        log.info("Creating rate: {} for property: {}", request.rateCode(), request.propertyId());

        try {
            if (!request.isValid()) {
                log.warn("Invalid rate creation request: {}", request);
                return ResponseEntity.badRequest().build();
            }

            RateResponseDTO createdRate = rateService.createRate(request);
            log.info("Successfully created rate: {}", createdRate.id());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdRate);

        } catch (IllegalArgumentException e) {
            log.error("Invalid rate creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating rate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Search rates",
        description = "Search for rates based on property, dates, and other criteria"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rates found successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search criteria"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/search")
    public ResponseEntity<List<RateResponseDTO>> searchRates(
            @Valid @RequestBody RateSearchRequestDTO request) {

        log.info("Searching rates for property: {}, dates: {} to {}",
                request.propertyId(), request.checkInDate(), request.checkOutDate());

        try {
            if (!request.isValid()) {
                log.warn("Invalid rate search request: {}", request);
                return ResponseEntity.badRequest().build();
            }

            List<RateResponseDTO> rates = rateService.searchRates(request);
            log.info("Found {} rates matching criteria", rates.size());

            return ResponseEntity.ok(rates);

        } catch (IllegalArgumentException e) {
            log.error("Invalid rate search request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error searching rates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Get best rate",
        description = "Find the best available rate for specific criteria"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Best rate found"),
        @ApiResponse(responseCode = "404", description = "No rates found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/best")
    public ResponseEntity<RateResponseDTO> getBestRate(
            @Parameter(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID propertyId,

            @Parameter(description = "Room type ID", example = "550e8400-e29b-41d4-a716-446655440001")
            @RequestParam UUID roomTypeId,

            @Parameter(description = "Date for rate lookup", example = "2024-12-25")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(description = "Number of nights", example = "3")
            @RequestParam(defaultValue = "1") Integer nights) {

        log.info("Finding best rate for property: {}, room type: {}, date: {}, nights: {}",
                propertyId, roomTypeId, date, nights);

        try {
            Optional<RateResponseDTO> bestRate = rateService.getBestRate(propertyId, roomTypeId, date, nights);

            if (bestRate.isPresent()) {
                log.info("Found best rate: {} at {}", bestRate.get().rateCode(), bestRate.get().currentRate());
                return ResponseEntity.ok(bestRate.get());
            } else {
                log.info("No rates found for criteria");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error finding best rate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Get rate by ID",
        description = "Retrieve rate details by rate ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rate found successfully"),
        @ApiResponse(responseCode = "404", description = "Rate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{rateId}")
    public ResponseEntity<RateResponseDTO> getRateById(
            @Parameter(description = "Rate ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID rateId) {

        log.info("Getting rate by ID: {}", rateId);

        try {
            RateResponseDTO rate = rateService.getRateById(rateId);
            log.info("Found rate: {}", rate.rateCode());

            return ResponseEntity.ok(rate);

        } catch (RuntimeException e) {
            log.error("Rate not found: {}", rateId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving rate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Update rate status",
        description = "Update the status of a rate (activate, suspend, expire, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rate status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "404", description = "Rate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{rateId}/status")
    public ResponseEntity<RateResponseDTO> updateRateStatus(
            @Parameter(description = "Rate ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID rateId,

            @Parameter(description = "New rate status", example = "ACTIVE")
            @RequestParam RateStatus status,

            @Parameter(description = "User making the update", example = "admin@hotel.com")
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String updatedBy) {

        log.info("Updating rate status: {} to {} by user: {}", rateId, status, updatedBy);

        try {
            RateResponseDTO updatedRate = rateService.updateRateStatus(rateId, status, updatedBy);
            log.info("Successfully updated rate status: {}", rateId);

            return ResponseEntity.ok(updatedRate);

        } catch (IllegalArgumentException e) {
            log.error("Invalid status transition: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Rate not found: {}", rateId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating rate status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Get rate statistics",
        description = "Get statistical information about rates for a property and date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRateStatistics(
            @Parameter(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID propertyId,

            @Parameter(description = "Date for statistics", example = "2024-12-25")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Getting rate statistics for property: {}, date: {}", propertyId, date);

        try {
            Map<String, Object> statistics = rateService.getRateStatistics(propertyId, date);
            log.info("Retrieved rate statistics for property: {}", propertyId);

            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Error retrieving rate statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Get rates with pagination",
        description = "Retrieve rates for a property with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rates retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<RateResponseDTO>> getRatesWithPagination(
            @Parameter(description = "Property ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID propertyId,

            @Parameter(description = "Rate status filter")
            @RequestParam(required = false) RateStatus status,

            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        log.info("Getting paginated rates for property: {}, status: {}, page: {}, size: {}",
                propertyId, status, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<RateResponseDTO> ratesPage = rateService.getRatesWithPagination(propertyId, status, pageable);

            log.info("Retrieved page {} of {} with {} rates",
                    ratesPage.getNumber(), ratesPage.getTotalPages(), ratesPage.getNumberOfElements());

            return ResponseEntity.ok(ratesPage);

        } catch (Exception e) {
            log.error("Error retrieving paginated rates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Expire outdated rates",
        description = "System operation to expire rates that have passed their expiry date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rates expired successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/expire")
    public ResponseEntity<Map<String, Object>> expireOutdatedRates() {
        log.info("Expiring outdated rates");

        try {
            int expiredCount = rateService.expireOutdatedRates();

            Map<String, Object> result = Map.of(
                    "expiredCount", expiredCount,
                    "message", expiredCount > 0 ?
                            "Successfully expired " + expiredCount + " rates" :
                            "No rates needed expiring"
            );

            log.info("Expired {} outdated rates", expiredCount);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error expiring outdated rates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
