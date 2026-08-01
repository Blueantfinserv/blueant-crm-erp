package com.blueant_crm_erp.exception.base;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Base Exception for BlueAnt CRM ERP
 *
 * Every custom exception in the project must extend this class.
 *
 * Example:
 * - UserNotFoundException
 * - LeadNotFoundException
 * - DuplicateLeadException
 * - InvalidCredentialsException
 * - AccessDeniedException
 * - ClientNotFoundException
 *
 * @author BlueAnt
 * @version 1.0
 */
@Getter
public abstract class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Business Error Code
     */
    private final ErrorCode errorCode;

    /**
     * Error Category
     */
    private final ErrorType errorType;

    /**
     * HTTP Status
     */
    private final HttpStatus httpStatus;

    /**
     * Exception Time
     */
    private final LocalDateTime timestamp;

    /**
     * Additional Error Details
     */
    private final Map<String, Object> details;

    /**
     * Constructor
     */
    protected BaseException(
            ErrorCode errorCode,
            ErrorType errorType,
            HttpStatus httpStatus,
            String message) {

        super(message);

        this.errorCode = errorCode;
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.details = Collections.emptyMap();
    }

    /**
     * Constructor with Details
     */
    protected BaseException(
            ErrorCode errorCode,
            ErrorType errorType,
            HttpStatus httpStatus,
            String message,
            Map<String, Object> details) {

        super(message);

        this.errorCode = errorCode;
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.details = details == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(details);
    }

    /**
     * Constructor with Root Cause
     */
    protected BaseException(
            ErrorCode errorCode,
            ErrorType errorType,
            HttpStatus httpStatus,
            String message,
            Throwable cause) {

        super(message, cause);

        this.errorCode = errorCode;
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.details = Collections.emptyMap();
    }

    /**
     * Constructor with Details & Root Cause
     */
    protected BaseException(
            ErrorCode errorCode,
            ErrorType errorType,
            HttpStatus httpStatus,
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(message, cause);

        this.errorCode = errorCode;
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.timestamp = LocalDateTime.now();
        this.details = details == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(details);
    }

}