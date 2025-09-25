package com.modernreservation.reservationengine.controller;

import com.modernreservation.reservationengine.dto.ReservationRequestDTO;
import com.modernreservation.reservationengine.dto.ReservationResponseDTO;
import com.modernreservation.reservationengine.service.ReservationService;
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
 * Reservation Controller - REST API endpoints for reservation management
 *
 * Provides comprehensive reservation operations including booking,
 * modification, cancellation, check-in/out, and reporting.
 */
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservations", description = "Reservation management API")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Create a new reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Room not available for selected dates",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody ReservationRequestDTO request) {

        log.info("Creating reservation for guest: {} {}",
                request.guestFirstName(), request.guestLastName());

        try {
            ReservationResponseDTO response = reservationService.createReservation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Failed to create reservation: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Get reservation by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(
            @Parameter(description = "Reservation ID") @PathVariable UUID id) {

        log.debug("Fetching reservation by ID: {}", id);

        return reservationService.getReservationById(id)
                .map(reservation -> ResponseEntity.ok(reservation))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get reservation by confirmation number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content)
    })
    @GetMapping("/confirmation/{confirmationNumber}")
    public ResponseEntity<ReservationResponseDTO> getReservationByConfirmationNumber(
            @Parameter(description = "Confirmation number") @PathVariable String confirmationNumber) {

        log.debug("Fetching reservation by confirmation number: {}", confirmationNumber);

        return reservationService.getReservationByConfirmationNumber(confirmationNumber)
                .map(reservation -> ResponseEntity.ok(reservation))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update an existing reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @Parameter(description = "Reservation ID") @PathVariable UUID id,
            @Valid @RequestBody ReservationRequestDTO request) {

        log.info("Updating reservation: {}", id);

        try {
            ReservationResponseDTO response = reservationService.updateReservation(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to update reservation {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Cancel a reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation cancelled successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content)
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(
            @Parameter(description = "Reservation ID") @PathVariable UUID id,
            @Parameter(description = "Cancellation reason") @RequestParam String reason,
            @Parameter(description = "User who cancelled") @RequestParam String cancelledBy) {

        log.info("Cancelling reservation: {} by: {}", id, cancelledBy);

        try {
            ReservationResponseDTO response = reservationService.cancelReservation(id, reason, cancelledBy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to cancel reservation {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Check-in a guest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guest checked in successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content)
    })
    @PostMapping("/{id}/checkin")
    public ResponseEntity<ReservationResponseDTO> checkInGuest(
            @Parameter(description = "Reservation ID") @PathVariable UUID id,
            @Parameter(description = "Room number") @RequestParam String roomNumber,
            @Parameter(description = "User who checked in guest") @RequestParam String checkedInBy) {

        log.info("Checking in guest for reservation: {}", id);

        try {
            ReservationResponseDTO response = reservationService.checkInGuest(id, roomNumber, checkedInBy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to check in guest for reservation {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Check-out a guest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guest checked out successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content)
    })
    @PostMapping("/{id}/checkout")
    public ResponseEntity<ReservationResponseDTO> checkOutGuest(
            @Parameter(description = "Reservation ID") @PathVariable UUID id,
            @Parameter(description = "User who checked out guest") @RequestParam String checkedOutBy) {

        log.info("Checking out guest for reservation: {}", id);

        try {
            ReservationResponseDTO response = reservationService.checkOutGuest(id, checkedOutBy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to check out guest for reservation {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Get reservations by property")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<Page<ReservationResponseDTO>> getReservationsByProperty(
            @Parameter(description = "Property ID") @PathVariable UUID propertyId,
            Pageable pageable) {

        log.debug("Fetching reservations for property: {}", propertyId);

        Page<ReservationResponseDTO> reservations = reservationService
                .getReservationsByProperty(propertyId, pageable);
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "Get reservations by date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/property/{propertyId}/daterange")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByDateRange(
            @Parameter(description = "Property ID") @PathVariable UUID propertyId,
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Fetching reservations for property: {} between {} and {}",
                 propertyId, startDate, endDate);

        List<ReservationResponseDTO> reservations = reservationService
                .getReservationsByDateRange(propertyId, startDate, endDate);
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "Get upcoming arrivals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upcoming arrivals retrieved successfully",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/property/{propertyId}/arrivals")
    public ResponseEntity<List<ReservationResponseDTO>> getUpcomingArrivals(
            @Parameter(description = "Property ID") @PathVariable UUID propertyId,
            @Parameter(description = "Date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.debug("Fetching upcoming arrivals for property: {} on date: {}", propertyId, date);

        List<ReservationResponseDTO> arrivals = reservationService
                .getUpcomingArrivals(propertyId, date);
        return ResponseEntity.ok(arrivals);
    }

    @Operation(summary = "Get upcoming departures")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upcoming departures retrieved successfully",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/property/{propertyId}/departures")
    public ResponseEntity<List<ReservationResponseDTO>> getUpcomingDepartures(
            @Parameter(description = "Property ID") @PathVariable UUID propertyId,
            @Parameter(description = "Date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.debug("Fetching upcoming departures for property: {} on date: {}", propertyId, date);

        List<ReservationResponseDTO> departures = reservationService
                .getUpcomingDepartures(propertyId, date);
        return ResponseEntity.ok(departures);
    }

    @Operation(summary = "Health check endpoint")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Reservation Engine is healthy");
    }
}
