package com.blueant_crm_erp.exception.user;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when attempting to create
 * a user that already exists in the system.
 *
 * Business Scenarios:
 * - Duplicate Employee Code
 * - Duplicate Email Address
 * - Duplicate Username
 * - Duplicate Mobile Number
 * - Duplicate PAN Number
 * - Duplicate Aadhaar Number
 * - Existing User Registration
 *
 * Modules:
 * - User Management
 * - Authentication
 * - HR Module
 * - Security
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class UserAlreadyExistsException extends BaseException {

    /**
     * Default Constructor.
     */
    public UserAlreadyExistsException() {

        super(
                ErrorCode.USER_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.USER_ALREADY_EXISTS
        );
    }

    /**
     * Constructor with custom message.
     */
    public UserAlreadyExistsException(String message) {

        super(
                ErrorCode.USER_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public UserAlreadyExistsException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.USER_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public UserAlreadyExistsException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.USER_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public UserAlreadyExistsException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.USER_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}