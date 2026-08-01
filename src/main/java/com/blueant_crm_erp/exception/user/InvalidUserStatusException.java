package com.blueant_crm_erp.exception.user;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when a user account is in an invalid
 * status for the requested operation.
 *
 * Business Scenarios:
 * - Inactive User
 * - Suspended User
 * - Locked User
 * - Disabled User
 * - Blocked User
 * - Deleted User
 * - Pending Approval
 * - Terminated Employee
 *
 * Modules:
 * - Authentication
 * - Authorization
 * - User Management
 * - Lead Module
 * - CRM Module
 * - HR Module
 *
 * HTTP Status : 403 FORBIDDEN
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class InvalidUserStatusException extends BaseException {

    /**
     * Default Constructor.
     */
    public InvalidUserStatusException() {

        super(
                ErrorCode.INVALID_USER_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                ExceptionMessage.INVALID_USER_STATUS
        );
    }

    /**
     * Constructor with custom message.
     */
    public InvalidUserStatusException(String message) {

        super(
                ErrorCode.INVALID_USER_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public InvalidUserStatusException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.INVALID_USER_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public InvalidUserStatusException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.INVALID_USER_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public InvalidUserStatusException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.INVALID_USER_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                message,
                details,
                cause
        );
    }

}