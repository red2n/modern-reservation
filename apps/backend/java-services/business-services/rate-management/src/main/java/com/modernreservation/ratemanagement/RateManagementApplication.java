package com.modernreservation.ratemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Rate Management Microservice Application
 *
 * Handles rate calculation, pricing strategies, seasonal adjustments,
 * demand-based pricing, and revenue optimization for hotel properties.
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableCaching
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class RateManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateManagementApplication.class, args);
    }
}
