package com.blueant_crm_erp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.blueant_crm_erp.auth.jwt.JwtAccessDeniedHandler;
import com.blueant_crm_erp.auth.jwt.JwtAuthenticationEntryPoint;
import com.blueant_crm_erp.auth.jwt.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /*
     * =========================================================================
     * NOTE
     * =========================================================================
     *
     * JWT Authentication Filter will be added after Auth Module implementation.
     *
     * Example:
     *
     * private final JwtAuthenticationFilter jwtAuthenticationFilter;
     *
     * http.addFilterBefore(
     *      jwtAuthenticationFilter,
     *      UsernamePasswordAuthenticationFilter.class
     * );
     *
     * =========================================================================
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // Disable CSRF for REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Stateless Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization Rules
                .authorizeHttpRequests(auth -> auth

                        /*
                         * ---------------------------------------------------------
                         * Public Endpoints
                         * ---------------------------------------------------------
                         */

                        .requestMatchers(

                                // Authentication
                                "/auth/**",

                                // Swagger
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",

                                // Health
                                "/actuator/health",
                                "/actuator/info",
                                
                                // Error
                                "/error"

                        ).permitAll()

                        /*
                         * ---------------------------------------------------------
                         * Admin APIs
                         * ---------------------------------------------------------
                         */
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        /*
                         * ---------------------------------------------------------
                         * Operations APIs
                         * ---------------------------------------------------------
                         */
                        .requestMatchers("/operations/**")
                        .hasAnyRole("ADMIN", "OPERATIONS")

                        /*
                         * ---------------------------------------------------------
                         * All Remaining APIs
                         * ---------------------------------------------------------
                         */
                        .anyRequest().authenticated()

                )
                
                // Exception Handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        // JWT Filter
        http.addFilterBefore(
             jwtAuthenticationFilter,
             UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    /**
     * BCrypt Password Encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication Manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

}