package com.modernreservation.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Gateway Application - API Gateway Service
 *
 * This service acts as the single entry point for all client requests to the
 * Modern Reservation System. It provides routing, load balancing, security,
 * rate limiting, and other cross-cutting concerns.
 *
 * Features:
 * - Dynamic routing based on service discovery
 * - Load balancing across service instances
 * - Authentication and authorization
 * - Rate limiting and throttling
 * - Circuit breaker pattern
 * - Request/response transformation
 * - Metrics and monitoring
 *
 * @author Modern Reservation Team
 * @version 2.0.0
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
