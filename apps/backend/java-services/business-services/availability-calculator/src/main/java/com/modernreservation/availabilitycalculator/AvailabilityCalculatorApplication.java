package com.modernreservation.availabilitycalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Availability Calculator Microservice
 *
 * Handles room availability calculations, pricing, and booking validations
 * for the Modern Reservation System.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableCaching
@EnableKafka
public class AvailabilityCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AvailabilityCalculatorApplication.class, args);
    }
}
