package com.blueant_crm_erp.exception.handler;

import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for creating standardized API error responses.
 *
 * This class provides reusable helper methods used by:
 * - GlobalExceptionHandler
 * - Spring Security Handlers
 * - AuthenticationEntryPoint
 * - AccessDeniedHandler
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ExceptionHandlerUtil {

    private ExceptionHandlerUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates ApiError without additional details.
     */
    public static ApiError buildError(
            HttpStatus status,
            ErrorCode errorCode,
            ErrorType errorType,
            String message,
            HttpServletRequest request) {

        return buildError(
                status,
                errorCode,
                errorType,
                message,
                request,
                Collections.emptyMap()
        );
    }

    /**
     * Creates ApiError with additional details.
     */
    public static ApiError buildError(
            HttpStatus status,
            ErrorCode errorCode,
            ErrorType errorType,
            String message,
            HttpServletRequest request,
            Map<String, Object> details) {

        return new ApiError(

                LocalDateTime.now(),

                status.value(),

                status.getReasonPhrase(),

                errorCode,

                errorType,

                message,

                request.getRequestURI(),

                request.getMethod(),

                generateTraceId(),

                details == null
                        ? Collections.emptyMap()
                        : Collections.unmodifiableMap(details)

        );
    }

    /**
     * Generates unique request trace id.
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns request URI.
     */
    public static String getRequestPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * Returns request method.
     */
    public static String getRequestMethod(HttpServletRequest request) {
        return request.getMethod();
    }

}