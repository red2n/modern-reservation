package com.modernreservation.reservationengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Reservation Audit DTO - Audit trail information
 *
 * Separate DTO for audit-related fields that are typically not needed
 * in list views but important for detail views and audit trails.
 *
 * Load this data on-demand via a dedicated endpoint to reduce
 * main response payload size.
 */
@Schema(description = "Reservation audit trail information")
public record ReservationAuditDTO(

    @Schema(description = "Creation timestamp", example = "2024-12-20T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Last modification timestamp", example = "2024-12-21T16:45:00")
    LocalDateTime updatedAt,

    @Schema(description = "Created by user", example = "john.smith@hotel.com")
    String createdBy,

    @Schema(description = "Last updated by user", example = "jane.doe@hotel.com")
    String updatedBy,

    @Schema(description = "Version for optimistic locking", example = "1")
    Long version,

    @Schema(description = "Internal notes", example = "VIP guest - provide room upgrade if available")
    String internalNotes,

    @Schema(description = "Cancellation date and time", example = "2024-12-22T14:15:00")
    LocalDateTime cancelledAt,

    @Schema(description = "Cancellation reason", example = "Guest cancelled due to travel restrictions")
    String cancellationReason,

    @Schema(description = "Who cancelled the reservation", example = "guest")
    String cancelledBy

) {

    /**
     * Factory method to create from full ReservationResponseDTO
     */
    public static ReservationAuditDTO from(ReservationResponseDTO full) {
        return new ReservationAuditDTO(
            full.createdAt(),
            full.updatedAt(),
            full.createdBy(),
            full.updatedBy(),
            full.version(),
            full.internalNotes(),
            full.cancelledAt(),
            full.cancellationReason(),
            full.cancelledBy()
        );
    }
}
