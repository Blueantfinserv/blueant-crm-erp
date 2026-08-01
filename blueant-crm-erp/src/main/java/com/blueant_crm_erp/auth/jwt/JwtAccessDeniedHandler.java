package com.blueant_crm_erp.auth.jwt;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * JWT Access Denied Handler
 * =============================================================================
 *
 * Handles authorization failures after successful authentication.
 *
 * This handler is invoked when:
 * -----------------------------------------------------------------------------
 * • User is authenticated
 * • User does NOT have required Role
 * • User does NOT have required Permission
 *
 * HTTP Status
 * -----------------------------------------------------------------------------
 * 403 FORBIDDEN
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
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {

        log.warn(
                "Access Denied | URI={} | Method={} | Message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getMessage()
        );

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(false)
                .status(HttpStatus.FORBIDDEN.value())
                .message("You do not have permission to access this resource.")
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), apiResponse);

    }

}