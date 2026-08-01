package com.blueant_crm_erp.exception.auth;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when an authenticated user
 * tries to access a resource without sufficient permissions.
 *
 * Examples:
 * - Sales Person accessing Admin APIs
 * - Employee updating another employee's lead
 * - CRM user accessing HR module
 * - User accessing resources outside reporting hierarchy
 * - User trying to delete a lead without permission
 *
 * HTTP Status : 403 FORBIDDEN
 *
 * @author BlueAnt
 * @version 1.0
 */
public class AccessDeniedException extends BaseException {

    /**
     * Default Constructor
     */
    public AccessDeniedException() {
        super(
                ErrorCode.ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                ExceptionMessage.ACCESS_DENIED
        );
    }

    /**
     * Constructor with custom message
     */
    public AccessDeniedException(String message) {
        super(
                ErrorCode.ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                message
        );
    }

    /**
     * Constructor with custom details
     */
    public AccessDeniedException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public AccessDeniedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause
     */
    public AccessDeniedException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                message,
                details,
                cause
        );
    }

}