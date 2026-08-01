package com.blueant_crm_erp.exception.role;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when attempting to create
 * a role that already exists in the system.
 *
 * Business Scenarios:
 * - Duplicate Role Name
 * - Duplicate Role Code
 * - Duplicate System Role
 * - Duplicate Custom Role
 * - Existing Default Role
 *
 * Modules:
 * - Role Management
 * - User Management
 * - Authorization
 * - Security
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class RoleAlreadyExistsException extends BaseException {

    /**
     * Default Constructor
     */
    public RoleAlreadyExistsException() {

        super(
                ErrorCode.ROLE_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.ROLE_ALREADY_EXISTS
        );
    }

    /**
     * Constructor with custom message.
     */
    public RoleAlreadyExistsException(String message) {

        super(
                ErrorCode.ROLE_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public RoleAlreadyExistsException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.ROLE_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public RoleAlreadyExistsException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.ROLE_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public RoleAlreadyExistsException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.ROLE_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}