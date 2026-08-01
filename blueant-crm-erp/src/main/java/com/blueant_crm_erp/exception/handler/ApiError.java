package com.blueant_crm_erp.exception.handler;

import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard API Error Response
 *
 * Every exception thrown from the BlueAnt CRM ERP Platform
 * must return this response structure.
 *
 * Used By:
 * - GlobalExceptionHandler
 * - Spring Security
 * - Validation Exceptions
 * - Business Exceptions
 * - Database Exceptions
 *
 * Example:
 *
 * {
 *   "timestamp":"2026-06-30T15:20:30",
 *   "status":404,
 *   "error":"NOT_FOUND",
 *   "errorCode":"CLIENT_NOT_FOUND",
 *   "errorType":"RESOURCE",
 *   "message":"Client not found.",
 *   "path":"/api/v1/clients/15",
 *   "method":"GET",
 *   "traceId":"4fa8b8f3d92b",
 *   "details":{}
 * }
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public record ApiError(

        /**
         * Error Timestamp
         */
        LocalDateTime timestamp,

        /**
         * HTTP Status Code
         */
        int status,

        /**
         * HTTP Status Name
         */
        String error,

        /**
         * Business Error Code
         */
        ErrorCode errorCode,

        /**
         * Error Category
         */
        ErrorType errorType,

        /**
         * Error Message
         */
        String message,

        /**
         * Requested API Path
         */
        String path,

        /**
         * HTTP Method
         */
        String method,

        /**
         * Request Trace Id
         */
        String traceId,

        /**
         * Additional Information
         */
        Map<String, Object> details

) {

    /**
     * Factory Method
     */
    public static ApiError of(
            HttpStatus status,
            ErrorCode errorCode,
            ErrorType errorType,
            String message,
            String path,
            String method,
            String traceId,
            Map<String, Object> details) {

        return new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                errorType,
                message,
                path,
                method,
                traceId,
                details == null ? Map.of() : Map.copyOf(details)
        );
    }

}