package com.modernreservation.shared.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP Client Configuration
 *
 * Configuration for RestTemplate and HTTP clients with proper timeout
 * and connection pool management to prevent connection leaks.
 *
 * @author Modern Reservation System
 * @version 2.0.0
 * @since 2024-01-01
 */
@Configuration
public class HttpClientConfig {

    /**
     * RestTemplate with proper timeout configuration
     * Prevents hanging connections that can cause memory leaks
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5 seconds connection timeout
        factory.setReadTimeout(10000);    // 10 seconds read timeout

        return new RestTemplate(factory);
    }

    /**
     * RestTemplate with RestTemplateBuilder (alternative approach)
     */
    @Bean("builderRestTemplate")
    public RestTemplate builderRestTemplate(RestTemplateBuilder builder) {
        return builder
            .rootUri("http://localhost")  // Default base URI
            .build();
    }
}
