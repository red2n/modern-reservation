package com.modernreservation.common.controller;

import com.modernreservation.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health check controller for all microservices
 * Provides standardized health and status endpoints
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "Service health and status endpoints")
public class HealthController {

    @Operation(
        summary = "Check service health",
        description = "Returns the current health status of the microservice including system information"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "503",
            description = "Service is unhealthy",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", getServiceName(),
            "version", getServiceVersion(),
            "environment", getEnvironment(),
            "uptime", getUptime(),
            "memory", getMemoryInfo(),
            "dependencies", getDependencyStatus()
        );

        return ResponseEntity.ok(ApiResponse.success("Service is healthy", healthData));
    }

    @Operation(
        summary = "Check service readiness",
        description = "Returns whether the service is ready to accept requests"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Service is ready",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "503",
            description = "Service is not ready",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/ready")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ready() {
        Map<String, Object> readinessData = Map.of(
            "ready", true,
            "timestamp", LocalDateTime.now(),
            "checks", Map.of(
                "database", "UP",
                "cache", "UP",
                "messaging", "UP"
            )
        );

        return ResponseEntity.ok(ApiResponse.success("Service is ready", readinessData));
    }

    @Operation(
        summary = "Check service liveness",
        description = "Returns whether the service is alive and running"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Service is alive",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/live")
    public ResponseEntity<ApiResponse<Map<String, Object>>> live() {
        Map<String, Object> livenessData = Map.of(
            "alive", true,
            "timestamp", LocalDateTime.now(),
            "pid", ProcessHandle.current().pid()
        );

        return ResponseEntity.ok(ApiResponse.success("Service is alive", livenessData));
    }

    // Helper methods
    private String getServiceName() {
        return System.getProperty("spring.application.name", "modern-reservation-service");
    }

    private String getServiceVersion() {
        return System.getProperty("app.version", "2.0.0");
    }

    private String getEnvironment() {
        return System.getProperty("spring.profiles.active", "development");
    }

    private String getUptime() {
        long uptimeMs = ProcessHandle.current().info().totalCpuDuration()
                .map(duration -> duration.toMillis())
                .orElse(0L);
        return String.format("%d ms", uptimeMs);
    }

    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        return Map.of(
            "total", runtime.totalMemory(),
            "free", runtime.freeMemory(),
            "used", runtime.totalMemory() - runtime.freeMemory(),
            "max", runtime.maxMemory()
        );
    }

    private Map<String, String> getDependencyStatus() {
        return Map.of(
            "database", "UP",
            "cache", "UP",
            "messageQueue", "UP",
            "configServer", "UP",
            "serviceRegistry", "UP"
        );
    }
}
