package com.blueant_crm_erp.exception.role;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when a role assignment
 * cannot be completed due to business rule violations.
 *
 * Business Scenarios:
 * - Role already assigned
 * - Duplicate role assignment
 * - Conflicting role assignment
 * - Invalid role assignment
 * - Inactive role assignment
 * - Reserved role assignment
 * - Unauthorized role assignment
 *
 * Modules:
 * - User Management
 * - Role Management
 * - Security
 * - Authorization
 * - Hierarchy
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class RoleAssignedException extends BaseException {

    /**
     * Default Constructor
     */
    public RoleAssignedException() {
        super(
                ErrorCode.ROLE_ALREADY_ASSIGNED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.ROLE_ALREADY_ASSIGNED
        );
    }

    /**
     * Constructor with custom message.
     */
    public RoleAssignedException(String message) {
        super(
                ErrorCode.ROLE_ALREADY_ASSIGNED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public RoleAssignedException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.ROLE_ALREADY_ASSIGNED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public RoleAssignedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.ROLE_ALREADY_ASSIGNED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public RoleAssignedException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.ROLE_ALREADY_ASSIGNED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}