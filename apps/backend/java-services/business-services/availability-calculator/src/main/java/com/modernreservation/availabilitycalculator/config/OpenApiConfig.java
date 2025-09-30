package com.modernreservation.availabilitycalculator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Availability Calculator API
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8083}")
    private String serverPort;

    @Bean
    public OpenAPI availabilityCalculatorOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Availability Calculator API")
                .description("Comprehensive room availability and pricing management service for Modern Reservation System")
                .version("2.0.0")
                .contact(new Contact()
                    .name("Modern Reservation Team")
                    .email("support@modernreservation.com")
                    .url("https://modernreservation.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080/availability-calculator")
                    .description("Gateway Server (Use this for testing)"),
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Direct Service (Internal only)")
            ));
    }
}
