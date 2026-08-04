package com.blueant_crm_erp.config;


import com.blueant_crm_erp.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final AppProperties appProperties;

    /**
     * CORS configuration — allows your React/Flutter frontend to call the backend
     *
     * Without this: browser blocks all API calls with "CORS policy" error
     * With this: only allowed origins (your app URLs) can call the API
     *
     * CorsConfigurationSource is used by SecurityConfig
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed frontend URLs — from application.yml
        configuration.setAllowedOrigins(
                appProperties.getCorsAllowedOrigins());

        // Allowed HTTP methods
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allowed headers in request
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept",
                        "X-Requested-With", "Cache-Control"));

        // Headers exposed to frontend (e.g. pagination info)
        configuration.setExposedHeaders(
                List.of("X-Total-Count", "X-Page-Number", "X-Page-Size"));

        // Allow cookies and Authorization header
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour (reduces OPTIONS calls)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
