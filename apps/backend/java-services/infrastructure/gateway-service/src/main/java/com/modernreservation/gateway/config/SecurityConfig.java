package com.modernreservation.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for Gateway Service
 *
 * Configures CORS, authentication, and authorization for the API Gateway.
 * Handles security concerns for all incoming requests before routing to services.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())

            // Security headers will be added via application.yml or other means
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints
                .pathMatchers("/actuator/**", "/fallback/**").permitAll()
                .pathMatchers("/api/v1/auth/**").permitAll()
                .pathMatchers("/api/v1/public/**").permitAll()
                .pathMatchers("/ws/**").permitAll()

                // Swagger UI and API Documentation endpoints
                .pathMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll()
                .pathMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
                .pathMatchers("/swagger-resources/**").permitAll()
                .pathMatchers("/webjars/**").permitAll()
                .pathMatchers("/gateway/health").permitAll()

                // Service-specific API documentation endpoints
                .pathMatchers("/reservation-engine/v3/api-docs/**").permitAll()
                .pathMatchers("/availability-calculator/v3/api-docs/**").permitAll()
                .pathMatchers("/payment-processor/v3/api-docs/**").permitAll()
                .pathMatchers("/rate-management/v3/api-docs/**").permitAll()
                .pathMatchers("/analytics-engine/v3/api-docs/**").permitAll()

                // Protected endpoints - require authentication
                .pathMatchers("/api/v1/reservations/**").authenticated()
                .pathMatchers("/api/v1/payments/**").authenticated()
                .pathMatchers("/api/v1/analytics/**").hasRole("ADMIN")
                .pathMatchers("/api/v1/batch/**").hasRole("ADMIN")

                // Allow all other requests for now (will be restricted later)
                .anyExchange().permitAll()
            )
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // Configure based on environment
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
