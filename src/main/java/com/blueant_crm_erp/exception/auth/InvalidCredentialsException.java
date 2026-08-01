package com.blueant_crm_erp.exception.auth;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when authentication fails
 * due to invalid login credentials.
 *
 * Examples:
 * - Invalid Email
 * - Invalid Password
 * - Invalid Username
 * - Invalid Mobile Number
 *
 * HTTP Status : 401 UNAUTHORIZED
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class InvalidCredentialsException extends BaseException {

    /**
     * Default Constructor
     */
    public InvalidCredentialsException() {
        super(
                ErrorCode.INVALID_CREDENTIALS,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                ExceptionMessage.INVALID_CREDENTIALS
        );
    }

    /**
     * Constructor with custom message
     */
    public InvalidCredentialsException(String message) {
        super(
                ErrorCode.INVALID_CREDENTIALS,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public InvalidCredentialsException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.INVALID_CREDENTIALS,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public InvalidCredentialsException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.INVALID_CREDENTIALS,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause
     */
    public InvalidCredentialsException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.INVALID_CREDENTIALS,
                ErrorType.AUTHENTICATION,
                HttpStatus.UNAUTHORIZED,
                message,
                details,
                cause
        );
    }

}