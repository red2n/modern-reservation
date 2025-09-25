package com.modernreservation.reservationengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Reservation Engine Application - Core Booking Management Service
 *
 * This service handles the complete reservation lifecycle including:
 * - Reservation creation, modification, and cancellation
 * - Room assignment and availability checking
 * - Guest management and preferences
 * - Booking status tracking and notifications
 * - Integration with payment and availability services
 *
 * Features:
 * - Complete CRUD operations for reservations
 * - Real-time availability integration
 * - Event-driven architecture with Kafka
 * - Redis caching for performance
 * - Comprehensive audit logging
 * - RESTful API with OpenAPI documentation
 *
 * @author Modern Reservation Team
 * @version 2.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableKafka
@EnableCaching
public class ReservationEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationEngineApplication.class, args);
    }
}
