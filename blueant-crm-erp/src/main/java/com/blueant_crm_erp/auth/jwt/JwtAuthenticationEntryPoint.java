package com.blueant_crm_erp.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * =============================================================================
 * JWT Authentication Entry Point
 * =============================================================================
 *
 * Handles unauthorized requests.
 *
 * This class is invoked whenever an unauthenticated user tries
 * to access a protected resource.
 *
 * HTTP Status
 * -----------------------------------------------------------------------------
 * 401 Unauthorized
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Invalid JWT
 * • Expired JWT
 * • Missing JWT
 * • Invalid Authentication
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        log.warn(
                "Authentication Failed | URI={} | Method={} | Message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getMessage()
        );

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("success", false);
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put("message", "Authentication is required to access this resource.");
        body.put("path", request.getRequestURI());
        body.put("timestamp", LocalDateTime.now());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), body);
    }

}