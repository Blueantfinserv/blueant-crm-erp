package com.blueant_crm_erp.exception.role;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested role
 * cannot be found in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - Role Id does not exist
 * - Role Code does not exist
 * - Role Name does not exist
 * - Role has been deleted
 * - Invalid Role Reference
 * - User assigned with invalid role
 * - Security configuration references missing role
 *
 * Modules:
 * - Role Management
 * - User Management
 * - Security
 * - Authorization
 * - Authentication
 *
 * Search Criteria:
 * - Role Id
 * - Role Name
 * - Role Code
 * - System Role
 *
 * HTTP Status : 404 NOT_FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class RoleNotFoundException extends BaseException {

    /**
     * Default Constructor
     */
    public RoleNotFoundException() {
        super(
                ErrorCode.ROLE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.ROLE_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message.
     */
    public RoleNotFoundException(String message) {
        super(
                ErrorCode.ROLE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public RoleNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.ROLE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public RoleNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.ROLE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public RoleNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.ROLE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}