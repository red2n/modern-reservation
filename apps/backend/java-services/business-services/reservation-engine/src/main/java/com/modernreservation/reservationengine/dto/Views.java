package com.modernreservation.reservationengine.dto;

/**
 * JSON View Definitions for Response DTOs
 *
 * Use these views with Jackson's @JsonView annotation to control
 * which fields are serialized based on the endpoint context.
 *
 * Benefits:
 * - Single DTO class can serve multiple use cases
 * - Reduces code duplication
 * - Flexible field filtering
 * - Clean API design
 *
 * Usage in Controllers:
 * <pre>
 * @JsonView(Views.Summary.class)
 * @GetMapping("/reservations/summary")
 * public Page&lt;ReservationResponseDTO&gt; getReservationsSummary() { ... }
 *
 * @JsonView(Views.Detail.class)
 * @GetMapping("/reservations/{id}")
 * public ReservationResponseDTO getReservationDetails(@PathVariable UUID id) { ... }
 * </pre>
 *
 * Usage in DTOs:
 * <pre>
 * public record ReservationResponseDTO(
 *     @JsonView(Views.Summary.class) UUID id,
 *     @JsonView(Views.Summary.class) String confirmationNumber,
 *     @JsonView(Views.Detail.class) String internalNotes,
 *     @JsonView(Views.Audit.class) LocalDateTime createdAt
 * ) { }
 * </pre>
 */
public class Views {

    /**
     * Summary View - Essential fields only
     *
     * Include fields that are necessary for:
     * - List views
     * - Tables and grids
     * - Search results
     * - Dashboard widgets
     * - Quick reference cards
     *
     * Typically includes:
     * - ID and primary identifiers
     * - Display names and titles
     * - Status indicators
     * - Key dates and amounts
     * - Essential foreign keys
     */
    public interface Summary {}

    /**
     * Detail View - Complete information
     *
     * Include all fields from Summary plus:
     * - Detailed descriptions
     * - Business rules and constraints
     * - Related entity details
     * - Calculated fields
     * - User preferences and settings
     *
     * Use for:
     * - Detail pages
     * - Edit forms
     * - Full record displays
     */
    public interface Detail extends Summary {}

    /**
     * Audit View - Audit trail and metadata
     *
     * Include fields for:
     * - Audit trails
     * - Modification history
     * - System metadata
     * - Version control
     * - Internal tracking
     *
     * Load on-demand via separate endpoints.
     * Not included in Summary or Detail by default.
     */
    public interface Audit {}

    /**
     * Public View - Client-safe fields
     *
     * Include only fields safe for:
     * - Public APIs
     * - Customer-facing portals
     * - Mobile applications
     * - Third-party integrations
     *
     * Excludes:
     * - Internal notes and flags
     * - Sensitive business data
     * - System implementation details
     */
    public interface Public extends Summary {}

    /**
     * Internal View - All fields including sensitive data
     *
     * Use for:
     * - Admin interfaces
     * - Internal tools
     * - System integration
     * - Debugging and diagnostics
     */
    public interface Internal extends Detail, Audit {}
}
