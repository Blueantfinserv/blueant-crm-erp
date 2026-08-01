package com.blueant_crm_erp.exception.user;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when an operation is attempted
 * using an inactive user account.
 *
 * BlueAnt CRM ERP Business Scenarios:
 *
 * - User account is inactive
 * - Inactive Sales Person login
 * - Inactive CRM access
 * - Inactive Leader access
 * - Inactive Process Coordinator access
 * - Inactive employee assigned to lead
 * - Inactive employee assigned to meeting
 * - Inactive employee assigned to service request
 *
 * Business Rules:
 * - Inactive users cannot login.
 * - Inactive users cannot access secured APIs.
 * - Inactive users cannot receive lead assignments.
 * - Inactive users cannot perform CRM operations.
 * - Inactive users cannot approve transactions.
 *
 * Modules:
 * - Authentication
 * - User Management
 * - Lead Management
 * - Meeting Management
 * - Service Request
 * - Transaction
 *
 * HTTP Status : 403 FORBIDDEN
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class UserInactiveException extends BaseException {

    /**
     * Default Constructor.
     */
    public UserInactiveException() {

        super(
                ErrorCode.USER_INACTIVE,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                ExceptionMessage.USER_INACTIVE
        );
    }

    /**
     * Constructor with custom message.
     */
    public UserInactiveException(String message) {

        super(
                ErrorCode.USER_INACTIVE,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public UserInactiveException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.USER_INACTIVE,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public UserInactiveException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.USER_INACTIVE,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public UserInactiveException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.USER_INACTIVE,
                ErrorType.BUSINESS,
                HttpStatus.FORBIDDEN,
                message,
                details,
                cause
        );
    }

}