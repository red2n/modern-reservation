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
        log.info("Testing Kafka event publishing...");

        try {
            // Create a test event
            ReservationCreatedEvent event = new ReservationCreatedEvent();
            event.setReservationId("test-reservation-123");
            event.setGuestId("test-guest-456");
            event.setPropertyId("test-property-789");
            event.setRoomTypeId("test-room-101");
            event.setCheckInDate(LocalDate.of(2025, 12, 25));
            event.setCheckOutDate(LocalDate.of(2025, 12, 27));
            event.setTotalAmount(new BigDecimal("439.98"));
            event.setStatus("CONFIRMED");
            event.setNumberOfGuests(3);

            // Publish the event
            eventPublisher.publishAsync("reservation.created", event);

            log.info("✅ Test event published successfully!");

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Kafka event published successfully");
            response.put("eventId", event.getEventId());
            response.put("topic", "reservation.created");
            response.put("checkKafkaUI", "http://localhost:8090");

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
