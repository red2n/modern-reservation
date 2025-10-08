package com.modernreservation.tenantservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Tenant Service Application
 *
 * Master service for tenant management that:
 * - Owns all tenant data (single source of truth)
 * - Provides CRUD operations via REST API and GraphQL
 * - Publishes Kafka events for tenant changes
 * - Manages tenant lifecycle (creation, suspension, expiration)
 * - Handles subscription management
 *
 * @author Modern Reservation Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableKafka
@EnableCaching
public class TenantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenantServiceApplication.class, args);
    }
}
