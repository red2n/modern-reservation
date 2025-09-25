package com.modernreservation.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback Controller for Circuit Breaker
 *
 * Provides fallback responses when downstream services are unavailable
 * or experiencing issues.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/reservation")
    public ResponseEntity<Map<String, Object>> reservationFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Reservation service is temporarily unavailable",
                "message", "Please try again later",
                "timestamp", LocalDateTime.now(),
                "service", "reservation-engine"
            ));
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> availabilityFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Availability service is temporarily unavailable",
                "message", "Please try again later",
                "timestamp", LocalDateTime.now(),
                "service", "availability-calculator"
            ));
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Payment service is temporarily unavailable",
                "message", "Please try again later or contact support",
                "timestamp", LocalDateTime.now(),
                "service", "payment-processor"
            ));
    }

    @GetMapping("/rate")
    public ResponseEntity<Map<String, Object>> rateFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Rate management service is temporarily unavailable",
                "message", "Default rates may apply",
                "timestamp", LocalDateTime.now(),
                "service", "rate-management"
            ));
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> analyticsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Analytics service is temporarily unavailable",
                "message", "Analytics data may be delayed",
                "timestamp", LocalDateTime.now(),
                "service", "analytics-engine"
            ));
    }

    @GetMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Batch processing service is temporarily unavailable",
                "message", "Batch jobs may be delayed",
                "timestamp", LocalDateTime.now(),
                "service", "batch-processor"
            ));
    }
}
