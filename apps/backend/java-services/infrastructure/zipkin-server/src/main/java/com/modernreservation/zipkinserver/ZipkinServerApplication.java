package com.modernreservation.zipkinserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import zipkin2.server.internal.EnableZipkinServer;

/**
 * Zipkin Server Application - Distributed Tracing and Monitoring
 *
 * This service provides distributed tracing capabilities for the Modern Reservation System.
 * It collects, stores, and visualizes trace data from all microservices to help with
 * performance monitoring, debugging, and service dependency analysis.
 *
 * Features:
 * - Distributed request tracing
 * - Service dependency mapping
 * - Performance analysis and bottleneck identification
 * - Error tracking and debugging
 * - Interactive web UI for trace visualization
 * - Integration with Spring Cloud Sleuth
 *
 * @author Modern Reservation Team
 * @version 2.0.0
 */
@SpringBootApplication
@EnableZipkinServer
public class ZipkinServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZipkinServerApplication.class, args);
    }
}
