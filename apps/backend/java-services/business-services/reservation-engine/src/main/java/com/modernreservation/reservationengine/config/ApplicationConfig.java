package com.modernreservation.reservationengine.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.Optional;

/**
 * Application Configuration
 *
 * Enables JPA auditing, caching, and Kafka messaging for the reservation engine.
 */
@Configuration
@EnableJpaAuditing
@EnableCaching
@EnableKafka
public class ApplicationConfig {

    /**
     * Auditor provider for JPA auditing
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("SYSTEM"); // In production, get from security context
    }

    /**
     * Redis cache manager configuration
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(org.springframework.data.redis.cache.RedisCacheConfiguration
                        .defaultCacheConfig()
                        .entryTtl(java.time.Duration.ofMinutes(30)))
                .build();
    }
}
