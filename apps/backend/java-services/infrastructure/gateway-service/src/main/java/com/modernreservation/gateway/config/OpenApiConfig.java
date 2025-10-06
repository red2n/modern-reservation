package com.modernreservation.gateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Gateway Service
 * Provides centralized API documentation for all business services
 */
@Configuration
public class OpenApiConfig {

    @Bean
    @Primary
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Modern Reservation System - API Gateway")
                        .version("2.0.0")
                        .description("Centralized API Gateway for Modern Reservation System - All Business Services Endpoints")
                        .contact(new Contact()
                                .name("Modern Reservation Team")
                                .email("support@modernreservation.com")
                                .url("https://modernreservation.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Gateway Server"),
                        new Server().url("http://localhost:8761").description("Eureka Server")
                ));
    }

    @Bean
    public GroupedOpenApi allServicesApi() {
        return GroupedOpenApi.builder()
                .group("00-all-services")
                .displayName("🌟 All Services Combined")
                .pathsToMatch("/reservation-engine/**", "/availability-calculator/**", 
                             "/payment-processor/**", "/rate-management/**", "/analytics-engine/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reservationEngineApi() {
        return GroupedOpenApi.builder()
                .group("01-reservation-engine")
                .displayName("🏨 Reservation Engine API")
                .pathsToMatch("/reservation-engine/**")
                .build();
    }

    @Bean
    public GroupedOpenApi availabilityCalculatorApi() {
        return GroupedOpenApi.builder()
                .group("02-availability-calculator")
                .displayName("📅 Availability Calculator API")
                .pathsToMatch("/availability-calculator/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentProcessorApi() {
        return GroupedOpenApi.builder()
                .group("03-payment-processor")
                .displayName("💳 Payment Processor API")
                .pathsToMatch("/payment-processor/**")
                .build();
    }

    @Bean
    public GroupedOpenApi rateManagementApi() {
        return GroupedOpenApi.builder()
                .group("04-rate-management")
                .displayName("💰 Rate Management API")
                .pathsToMatch("/rate-management/**")
                .build();
    }

    @Bean
    public GroupedOpenApi analyticsEngineApi() {
        return GroupedOpenApi.builder()
                .group("05-analytics-engine")
                .displayName("📊 Analytics Engine API")
                .pathsToMatch("/analytics-engine/**")
                .build();
    }

    @Bean
    public GroupedOpenApi gatewayApi() {
        return GroupedOpenApi.builder()
                .group("99-gateway")
                .displayName("🌐 Gateway Service API")
                .pathsToMatch("/fallback/**", "/actuator/**")
                .build();
    }
}
