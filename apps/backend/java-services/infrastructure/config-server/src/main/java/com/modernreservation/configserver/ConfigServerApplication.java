package com.modernreservation.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application - Centralized Configuration Management
 *
 * This service provides centralized configuration management for all microservices
 * in the Modern Reservation System. It serves configuration files from a Git repository
 * and supports multiple profiles and environments.
 *
 * Features:
 * - Git-based configuration repository
 * - Environment-specific configurations
 * - Real-time configuration refresh
 * - Secure configuration access
 * - Health monitoring and metrics
 *
 * @author Modern Reservation Team
 * @version 2.0.0
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
})
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
