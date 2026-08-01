package com.blueant_crm_erp.exception.handler;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Builds standardized API error responses
 * for the BlueAnt CRM ERP Platform.
 *
 * Used By:
 * - GlobalExceptionHandler
 * - Spring Security Exception Handlers
 * - AuthenticationEntryPoint
 * - AccessDeniedHandler
 *
 * Every exception response must be created
 * through this builder to ensure consistency.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
@Component
public class ErrorResponseBuilder {

    /**
     * Builds ApiError from BaseException.
     */
    public ApiError build(
            BaseException exception,
            HttpServletRequest request) {

        return new ApiError(

                LocalDateTime.now(),

                exception.getHttpStatus().value(),

                exception.getHttpStatus().getReasonPhrase(),

                exception.getErrorCode(),

                exception.getErrorType(),

                exception.getMessage(),

                request.getRequestURI(),

                request.getMethod(),

                generateTraceId(),

                exception.getDetails()

        );
    }

    /**
     * Builds ApiError for generic exceptions.
     */
    public ApiError build(
            HttpStatus status,
            ErrorCode errorCode,
            ErrorType errorType,
            String message,
            HttpServletRequest request) {

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

                Map.of()

        );
    }

    /**
     * Builds ApiError with additional details.
     */
    public ApiError build(
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

                details == null ? Map.of() : Map.copyOf(details)

        );
    }

    /**
     * Generates request trace id.
     *
     * Can later be replaced by:
     * - Spring Sleuth
     * - Zipkin
     * - OpenTelemetry
     * - ELK Correlation Id
     */
    private String generateTraceId() {

        return UUID.randomUUID().toString();

    }

}