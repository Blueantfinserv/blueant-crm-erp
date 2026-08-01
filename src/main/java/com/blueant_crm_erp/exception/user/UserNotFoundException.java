package com.blueant_crm_erp.exception.user;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested user
 * cannot be found in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - User Id does not exist
 * - Employee Code does not exist
 * - Official Email does not exist
 * - Username does not exist
 * - User deleted or archived
 * - Invalid reporting manager
 * - Invalid sales person
 * - Invalid CRM executive
 * - Invalid leader
 * - Invalid process coordinator
 *
 * Search Criteria:
 * - User Id
 * - Employee Code
 * - Username
 * - Official Email
 * - Mobile Number
 *
 * Used In:
 * - Authentication
 * - User Management
 * - Lead Assignment
 * - CRM Module
 * - HR Module
 * - Hierarchy Module
 * - Dashboard
 *
 * HTTP Status : 404 NOT_FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class UserNotFoundException extends BaseException {

    /**
     * Default Constructor.
     */
    public UserNotFoundException() {

        super(
                ErrorCode.USER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.USER_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message.
     */
    public UserNotFoundException(String message) {

        super(
                ErrorCode.USER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public UserNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.USER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public UserNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.USER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public UserNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.USER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}