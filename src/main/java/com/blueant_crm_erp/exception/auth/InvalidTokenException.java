package com.blueant_crm_erp.exception.auth;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the provided JWT token
 * is invalid, malformed, tampered, or unsupported.
 *
 * Examples:
 * - Invalid JWT Signature
 * - Malformed JWT Token
 * - Unsupported JWT Token
 * - Corrupted JWT Token
 * - Missing Required Claims
 *
 * Note:
 * Expired JWT tokens should throw TokenExpiredException.
 *
 * HTTP Status : 401 UNAUTHORIZED
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class InvalidTokenException extends BaseException {

    /**
     * Default constructor
     */
    public InvalidTokenException() {
        super(
                ErrorCode.INVALID_TOKEN,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                ExceptionMessage.INVALID_TOKEN
        );
    }

    /**
     * Constructor with custom message
     */
    public InvalidTokenException(String message) {
        super(
                ErrorCode.INVALID_TOKEN,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public InvalidTokenException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.INVALID_TOKEN,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public InvalidTokenException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.INVALID_TOKEN,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                cause
        );
    }

    /**
     * Constructor with additional details and root cause
     */
    public InvalidTokenException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.INVALID_TOKEN,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details,
                cause
        );
    }

}