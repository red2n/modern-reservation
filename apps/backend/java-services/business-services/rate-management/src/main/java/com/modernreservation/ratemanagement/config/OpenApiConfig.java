package com.modernreservation.ratemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for Rate Management Service
 * Configures Swagger UI documentation and API specifications
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rate Management API")
                        .version("1.0.0")
                        .description("Rate and pricing management service for Modern Reservation System")
                        .contact(new Contact()
                                .name("Modern Reservation Rate Team")
                                .email("rates@modernreservation.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/rate-management").description("Gateway Server (Use this for testing)"),
                        new Server().url("http://localhost:8087/rate-management").description("Direct Service (Internal only)")
                ));
    }
}
