package com.modernreservation.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application - Service Discovery and Registration
 *
 * This service provides service discovery and registration capabilities for all
 * microservices in the Modern Reservation System. It maintains a registry of
 * available services and enables service-to-service communication.
 *
 * Features:
 * - Service registration and discovery
 * - Health monitoring of registered services
 * - Load balancing support
 * - Failover and fault tolerance
 * - Web dashboard for service monitoring
 *
 * @author Modern Reservation Team
 * @version 2.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
