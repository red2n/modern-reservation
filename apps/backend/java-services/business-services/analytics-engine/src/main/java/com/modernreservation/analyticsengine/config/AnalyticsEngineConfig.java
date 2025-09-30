package com.modernreservation.analyticsengine.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * Analytics Engine Configuration
 *
 * Configuration for analytics engine services including caching,
 * async processing, and scheduling.
 *
 * @author Modern Reservation System
 * @version 3.2.0
 * @since 2024-01-01
 */
@Configuration
@EnableCaching
@EnableAsync
@EnableScheduling
@Slf4j
public class AnalyticsEngineConfig {

    /**
     * Cache manager for analytics data with Caffeine
     * Fixes memory leak by implementing proper eviction policies
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000)  // Limit cache size to prevent unbounded growth
                .expireAfterWrite(30, TimeUnit.MINUTES)  // Expire entries after 30 minutes
                .expireAfterAccess(15, TimeUnit.MINUTES)  // Expire if not accessed for 15 minutes
                .recordStats()  // Enable statistics for monitoring
                .removalListener((key, value, cause) -> {
                    // Log cache evictions for monitoring
                    log.debug("Cache entry removed: key={}, cause={}", key, cause);
                }));
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }

    /**
     * Task executor for analytics calculations
     */
    @Bean(name = "analyticsExecutor")
    public Executor analyticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Analytics-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Task executor for report generation
     */
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Report-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }
}
