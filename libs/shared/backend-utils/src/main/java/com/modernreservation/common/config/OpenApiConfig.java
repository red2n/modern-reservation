package com.modernreservation.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Shared OpenAPI configuration for Modern Reservation System
 * Provides standardized API documentation setup across all microservices
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Modern Reservation Service}")
    private String applicationName;

    @Value("${app.version:2.0.0}")
    private String appVersion;

    @Value("${app.description:Microservice for Modern Reservation System}")
    private String appDescription;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .components(buildComponents())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    private Info buildApiInfo() {
        return new Info()
                .title(applicationName + " API")
                .description(appDescription + "\n\n" +
                        "This API is part of the Modern Reservation System - a comprehensive " +
                        "hotel management platform built with microservices architecture.")
                .version(appVersion)
                .contact(buildContact())
                .license(buildLicense());
    }

    private Contact buildContact() {
        return new Contact()
                .name("Modern Reservation Team")
                .email("support@modernreservation.com")
                .url("https://github.com/your-org/modern-reservation");
    }

    private License buildLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    private List<Server> buildServers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
                new Server()
                        .url("https://api-dev.modernreservation.com")
                        .description("Development Environment"),
                new Server()
                        .url("https://api-staging.modernreservation.com")
                        .description("Staging Environment"),
                new Server()
                        .url("https://api.modernreservation.com")
                        .description("Production Environment")
        );
    }

    private Components buildComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token for API authentication"));
    }
}
