package com.modernreservation.analyticsengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Analytics Engine Service - Main Application Class
 *
 * Advanced analytics and business intelligence microservice for the Modern Reservation System.
 * Provides comprehensive data analysis, reporting, forecasting, and optimization capabilities.
 *
 * Core Features:
 * - Revenue analytics and financial performance tracking
 * - Occupancy analysis and optimization recommendations
 * - Customer behavior analytics and segmentation
 * - Predictive analytics and demand forecasting
 * - Real-time KPI monitoring and alerting
 * - Business intelligence dashboards and reporting
 * - Data aggregation and time-series analysis
 * - Performance optimization recommendations
 *
 * Analytics Capabilities:
 * - Revenue per available room (RevPAR) calculation
 * - Average daily rate (ADR) optimization
 * - Occupancy rate analysis and forecasting
 * - Customer lifetime value (CLV) computation
 * - Seasonal trend analysis and predictions
 * - Competitive pricing recommendations
 * - Demand pattern recognition and forecasting
 * - Channel performance analytics
 *
 * Technical Features:
 * - Time-series data processing and analysis
 * - Statistical modeling and machine learning
 * - Real-time data streaming and aggregation
 * - Multi-dimensional data cube operations
 * - Advanced reporting and visualization support
 * - Data export and integration capabilities
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableJpaRepositories
@EnableCaching
@EnableKafka
@EnableScheduling
@EnableAsync
public class AnalyticsEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsEngineApplication.class, args);
    }
}
