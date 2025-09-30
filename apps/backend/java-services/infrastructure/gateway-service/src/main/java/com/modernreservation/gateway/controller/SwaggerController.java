package com.modernreservation.gateway.controller;

import com.modernreservation.gateway.config.SwaggerResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swagger Controller for Gateway Service
 * Provides endpoints for Swagger UI configuration and resources
 */
@RestController
public class SwaggerController {

    @Autowired
    private SwaggerResourceProvider swaggerResourceProvider;

    /**
     * Get swagger resources for all services
     */
    @GetMapping("/swagger-resources")
    public Mono<ResponseEntity<List<SwaggerResourceProvider.SwaggerResource>>> swaggerResources() {
        return Mono.just(ResponseEntity.ok(swaggerResourceProvider.get()));
    }

    /**
     * Get Swagger UI configuration
     */
    @GetMapping("/swagger-resources/configuration/ui")
    public Mono<ResponseEntity<Map<String, Object>>> uiConfiguration() {
        Map<String, Object> uiConfig = new HashMap<>();
        uiConfig.put("deepLinking", true);
        uiConfig.put("displayOperationId", false);
        uiConfig.put("defaultModelsExpandDepth", 1);
        uiConfig.put("defaultModelExpandDepth", 1);
        uiConfig.put("defaultModelRendering", "example");
        uiConfig.put("displayRequestDuration", true);
        uiConfig.put("docExpansion", "none");
        uiConfig.put("filter", false);
        uiConfig.put("operationsSorter", "alpha");
        uiConfig.put("showExtensions", true);
        uiConfig.put("showCommonExtensions", true);
        uiConfig.put("tagsSorter", "alpha");
        uiConfig.put("validatorUrl", null);
        uiConfig.put("oauth2RedirectUrl", "http://localhost:8080/swagger-ui/oauth2-redirect.html");

        return Mono.just(ResponseEntity.ok(uiConfig));
    }

    /**
     * Get Swagger security configuration
     */
    @GetMapping("/swagger-resources/configuration/security")
    public Mono<ResponseEntity<Map<String, Object>>> securityConfiguration() {
        Map<String, Object> securityConfig = new HashMap<>();
        securityConfig.put("clientId", "modern-reservation-client");
        securityConfig.put("clientSecret", null);
        securityConfig.put("realm", "modern-reservation");
        securityConfig.put("appName", "Modern Reservation API Gateway");
        securityConfig.put("scopeSeparator", " ");
        securityConfig.put("additionalQueryStringParams", new HashMap<>());
        securityConfig.put("useBasicAuthenticationWithAccessCodeGrant", false);

        return Mono.just(ResponseEntity.ok(securityConfig));
    }

    /**
     * Health check endpoint for gateway
     */
    @GetMapping("/gateway/health")
    public Mono<ResponseEntity<Map<String, String>>> gatewayHealth() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Modern Reservation API Gateway");
        health.put("version", "2.0.0");
        health.put("swagger-ui", "http://localhost:8080/swagger-ui.html");

        return Mono.just(ResponseEntity.ok(health));
    }
}
