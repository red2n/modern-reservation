package com.modernreservation.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger Resource Provider for Gateway Service
 * Aggregates API documentation from all business services
 */
@Component
@Primary
public class SwaggerResourceProvider {

    public static final String API_URI = "/v3/api-docs";
    public static final String SERVICE_API_URI = "/api-docs";

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();

        // Add gateway's own swagger resource
        resources.add(createSwaggerResource("🌐 Gateway Service", API_URI, "2.0.0"));

        // Add business services swagger resources - they use /api-docs path
        resources.add(createSwaggerResource("🏨 Reservation Engine", "/reservation-engine" + SERVICE_API_URI, "2.0.0"));
        resources.add(createSwaggerResource("📅 Availability Calculator", "/availability-calculator" + SERVICE_API_URI, "2.0.0"));
        resources.add(createSwaggerResource("💳 Payment Processor", "/payment-processor" + SERVICE_API_URI, "3.2.0"));
        resources.add(createSwaggerResource("💰 Rate Management", "/rate-management" + SERVICE_API_URI, "3.2.0"));
        resources.add(createSwaggerResource("📊 Analytics Engine", "/analytics-engine" + SERVICE_API_URI, "1.0.0"));

        return resources;
    }

    private SwaggerResource createSwaggerResource(String name, String location, String version) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;
    }

    /**
     * Swagger Resource DTO
     */
    public static class SwaggerResource {
        private String name;
        private String location;
        private String swaggerVersion;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getSwaggerVersion() {
            return swaggerVersion;
        }

        public void setSwaggerVersion(String swaggerVersion) {
            this.swaggerVersion = swaggerVersion;
        }
    }
}
