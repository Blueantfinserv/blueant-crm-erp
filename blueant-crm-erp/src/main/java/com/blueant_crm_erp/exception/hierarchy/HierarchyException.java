package com.blueant_crm_erp.exception.hierarchy;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when an invalid hierarchy operation
 * is performed in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - Invalid Reporting Hierarchy
 * - Circular Reporting Structure
 * - Self Reporting
 * - Manager Assignment Failure
 * - Invalid Team Hierarchy
 * - Cross Department Reporting
 * - Leader Assignment Failure
 * - Hierarchy Validation Failure
 *
 * Modules:
 * - User
 * - Hierarchy
 * - Team
 * - Department
 * - Mapping
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class HierarchyException extends BaseException {

    /**
     * Default Constructor
     */
    public HierarchyException() {
        super(
                ErrorCode.HIERARCHY_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.HIERARCHY_ERROR
        );
    }

    /**
     * Constructor with custom message.
     */
    public HierarchyException(String message) {
        super(
                ErrorCode.HIERARCHY_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public HierarchyException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.HIERARCHY_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public HierarchyException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.HIERARCHY_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public HierarchyException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.HIERARCHY_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}