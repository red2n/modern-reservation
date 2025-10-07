package com.modernreservation.reservationengine.controller;

import com.reservation.shared.events.EventPublisher;
import com.reservation.shared.events.ReservationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller for Kafka Event Publishing
 * This is a simple controller to test Kafka without authentication
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class KafkaTestController {

    private final EventPublisher eventPublisher;

    @GetMapping("/kafka")
    public Map<String, String> testKafkaEvent() {
        log.info("Testing Kafka event publishing with Avro...");

        try {
            String eventId = java.util.UUID.randomUUID().toString();

            // Create a test event using Avro builder
            ReservationCreatedEvent event = ReservationCreatedEvent.newBuilder()
                .setEventId(eventId)
                .setEventType("RESERVATION_CREATED")
                .setTimestamp(java.time.Instant.now())
                .setVersion(1)
                .setReservationId("test-reservation-123")
                .setGuestId("test-guest-456")
                .setPropertyId("test-property-789")
                .setRoomTypeId("test-room-101")
                .setCheckInDate(LocalDate.of(2025, 12, 25).toString())
                .setCheckOutDate(LocalDate.of(2025, 12, 27).toString())
                .setTotalAmount(new BigDecimal("439.98").toString())
                .setStatus("CONFIRMED")
                .setNumberOfGuests(3)
                .build();

            // Publish the event
            eventPublisher.publishAsync("reservation.created", event);

            log.info("✅ Test Avro event published successfully!");

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Kafka Avro event published successfully");
            response.put("eventId", eventId);
            response.put("topic", "reservation.created");
            response.put("checkKafkaUI", "http://localhost:8090");
            response.put("checkSchemaRegistry", "http://localhost:8085/subjects");

            return response;

        } catch (Exception e) {
            log.error("❌ Failed to publish test event", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to publish event: " + e.getMessage());
            return response;
        }
    }
}
