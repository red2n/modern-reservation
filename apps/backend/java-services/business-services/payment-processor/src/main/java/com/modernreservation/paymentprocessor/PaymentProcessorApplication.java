package com.modernreservation.paymentprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Payment Processor Service - Main Application Class
 *
 * Handles payment processing, refunds, and payment method management
 * with comprehensive security, audit trails, and integration capabilities.
 *
 * Features:
 * - Payment transaction processing (authorization, capture, settlement)
 * - Refund processing (full and partial refunds)
 * - Payment method management (cards, digital wallets, bank transfers)
 * - PCI compliance and security features
 * - Fraud detection and risk assessment
 * - Real-time payment notifications via Kafka
 * - Comprehensive audit trails and reporting
 * - Integration with multiple payment gateways
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableJpaRepositories
@EnableCaching
@EnableKafka
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class PaymentProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProcessorApplication.class, args);
    }
}
