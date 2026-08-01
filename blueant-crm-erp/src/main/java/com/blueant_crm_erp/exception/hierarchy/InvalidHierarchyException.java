package com.blueant_crm_erp.exception.hierarchy;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when an invalid reporting hierarchy
 * is detected in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - Self Reporting
 * - Circular Hierarchy
 * - Invalid Reporting Manager
 * - Cross Department Reporting
 * - Invalid Reporting Level
 * - Invalid Parent Hierarchy
 * - Multiple Reporting Managers
 *
 * Modules:
 * - Hierarchy
 * - User
 * - Team
 * - Department
 * - Mapping
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class InvalidHierarchyException extends BaseException {

    /**
     * Default Constructor
     */
    public InvalidHierarchyException() {
        super(
                ErrorCode.INVALID_HIERARCHY,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.INVALID_HIERARCHY
        );
    }

    /**
     * Constructor with custom message.
     */
    public InvalidHierarchyException(String message) {
        super(
                ErrorCode.INVALID_HIERARCHY,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public InvalidHierarchyException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.INVALID_HIERARCHY,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public InvalidHierarchyException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.INVALID_HIERARCHY,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public InvalidHierarchyException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.INVALID_HIERARCHY,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}