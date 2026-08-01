package com.blueant_crm_erp.exception.auth;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the refresh token
 * has expired and a new access token
 * cannot be generated.
 *
 * Examples:
 * - Refresh Token Expired
 * - Refresh Token Revoked
 * - Refresh Token Invalid
 *
 * HTTP Status : 401 UNAUTHORIZED
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class RefreshTokenExpiredException extends BaseException {

    /**
     * Default Constructor
     */
    public RefreshTokenExpiredException() {
        super(
                ErrorCode.REFRESH_TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                ExceptionMessage.REFRESH_TOKEN_EXPIRED
        );
    }

    /**
     * Constructor with custom message
     */
    public RefreshTokenExpiredException(String message) {
        super(
                ErrorCode.REFRESH_TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public RefreshTokenExpiredException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.REFRESH_TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public RefreshTokenExpiredException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.REFRESH_TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                cause
        );
    }

    /**
     * Constructor with additional details and root cause
     */
    public RefreshTokenExpiredException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.REFRESH_TOKEN_EXPIRED,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details,
                cause
        );
    }

}