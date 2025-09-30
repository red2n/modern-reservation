package com.modernreservation.paymentprocessor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for Payment Processor Service
 * Configures Swagger UI documentation and API specifications
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Processor Service API")
                        .version("3.2.0")
                        .description("Payment processing and management microservice for Modern Reservation System")
                        .contact(new Contact()
                                .name("Modern Reservation Payment Team")
                                .email("payments@modernreservation.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/payment-processor").description("Gateway Server (Use this for testing)"),
                        new Server().url("http://localhost:8084/payment-processor").description("Direct Service (Internal only)")
                ));
    }
}
