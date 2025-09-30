package com.modernreservation.reservationengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for Reservation Engine Service
 * Configures Swagger UI documentation and API specifications
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reservation Engine API")
                        .version("2.0.0")
                        .description("Core Reservation Management Service for Modern Reservation System")
                        .contact(new Contact()
                                .name("Modern Reservation Team")
                                .email("reservations@modernreservation.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/reservation-engine").description("Gateway Server (Use this for testing)"),
                        new Server().url("http://localhost:8081/reservation-engine").description("Direct Service (Internal only)")
                ));
    }
}
