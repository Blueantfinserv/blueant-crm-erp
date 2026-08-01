package com.blueant_crm_erp.exception.auth;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when a user is not authenticated
 * or authentication is required before accessing
 * a protected resource.
 *
 * Examples:
 * - Missing JWT Token
 * - Missing Authorization Header
 * - Anonymous User Access
 * - Login Required
 *
 * NOTE:
 * If the user is authenticated but lacks permission,
 * throw AccessDeniedException instead.
 *
 * HTTP Status : 401 UNAUTHORIZED
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class UnauthorizedException extends BaseException {

    /**
     * Default Constructor
     */
    public UnauthorizedException() {
        super(
                ErrorCode.UNAUTHORIZED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                ExceptionMessage.UNAUTHORIZED
        );
    }

    /**
     * Constructor with custom message
     */
    public UnauthorizedException(String message) {
        super(
                ErrorCode.UNAUTHORIZED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public UnauthorizedException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.UNAUTHORIZED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public UnauthorizedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.UNAUTHORIZED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                cause
        );
    }

    /**
     * Constructor with additional details and root cause
     */
    public UnauthorizedException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.UNAUTHORIZED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details,
                cause
        );
    }

}