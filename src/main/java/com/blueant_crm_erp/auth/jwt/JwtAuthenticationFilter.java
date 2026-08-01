package com.blueant_crm_erp.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * =============================================================================
 * JWT Authentication Filter
 * =============================================================================
 *
 * Validates JWT access tokens for protected APIs.
 * Public authentication endpoints are excluded from JWT validation.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 * =============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Public endpoints that should bypass JWT authentication.
     */
    private static final List<String> PUBLIC_ENDPOINTS = List.of(

            // Authentication APIs
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-login-otp",
            "/api/auth/change-password",

            // Swagger
            "/api/v3/api-docs",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",

            // Health
            "/actuator/health",
            "/actuator/info"
    );

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {

        String path = request.getRequestURI();

        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            String rawHeader = request.getHeader("Authorization");
            log.info("Authorization Header = [{}]", rawHeader);

            String token = jwtTokenProvider.resolveToken(request);
            log.info("Resolved Token = [{}]", token);

            boolean isValid = false;
            if (token != null) {
                isValid = jwtTokenProvider.validateToken(token);
            }
            log.info("Token Valid = {}", isValid);

            if (isValid) {

                String username = jwtTokenProvider.extractUsername(token);

                if (username != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    log.debug("JWT authenticated user: {}", username);
                }
            }

        } catch (Exception ex) {

            log.error("JWT Authentication failed", ex);

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}