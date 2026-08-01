package com.blueant_crm_erp.common.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Generic Error Response
 *
 * Used by:
 * - GlobalExceptionHandler
 * - BusinessException
 * - ValidationException
 * - ResourceNotFoundException
 * - UnauthorizedException
 * - ForbiddenException
 * - DuplicateResourceException
 *
 * All error APIs should return this response.
 */
@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse extends ApiResponse<Object> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Error Code
     *
     * Example:
     * ROLE_NOT_FOUND
     * USER_ALREADY_EXISTS
     * INVALID_TOKEN
     */
    private String errorCode;

    /**
     * Detailed Validation Errors
     */
    private List<String> errors;

    /**
     * Constructor
     */
    public ErrorResponse(
            int status,
            String message,
            String path,
            String errorCode,
            List<String> errors
    ) {

        setSuccess(false);
        setStatus(status);
        setMessage(message);
        setPath(path);
        setTimestamp(LocalDateTime.now());
        this.errorCode = errorCode;
        this.errors = errors;
    }

    /**
     * Constructor without validation errors.
     */
    public ErrorResponse(
            int status,
            String message,
            String path,
            String errorCode
    ) {

        setSuccess(false);
        setStatus(status);
        setMessage(message);
        setPath(path);
        setTimestamp(LocalDateTime.now());
        this.errorCode = errorCode;
    }
}