package com.modernreservation.analyticsengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for Analytics Engine Service
 * Configures Swagger UI documentation and API specifications
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Analytics Engine API")
                        .version("3.2.0")
                        .description("Advanced analytics and business intelligence service for Modern Reservation System")
                        .contact(new Contact()
                                .name("Modern Reservation Analytics Team")
                                .email("analytics@modernreservation.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/analytics-engine").description("Gateway Server (Use this for testing)"),
                        new Server().url("http://localhost:8086/analytics-engine").description("Direct Service (Internal only)")
                ));
    }
}
