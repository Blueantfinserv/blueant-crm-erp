package com.blueant_crm_erp.exception.auth;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the JWT Access Token
 * has expired.
 *
 * Examples:
 * - Access Token Expired
 * - JWT Expired
 * - Session Timeout
 *
 * Note:
 * Refresh Token expiration should throw
 * RefreshTokenExpiredException.
 *
 * HTTP Status : 401 UNAUTHORIZED
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class TokenExpiredException extends BaseException {

    /**
     * Default Constructor
     */
    public TokenExpiredException() {
        super(
                ErrorCode.TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                ExceptionMessage.TOKEN_EXPIRED
        );
    }

    /**
     * Constructor with custom message
     */
    public TokenExpiredException(String message) {
        super(
                ErrorCode.TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public TokenExpiredException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public TokenExpiredException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                cause
        );
    }

    /**
     * Constructor with additional details and root cause
     */
    public TokenExpiredException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details,
                cause
        );
    }

}