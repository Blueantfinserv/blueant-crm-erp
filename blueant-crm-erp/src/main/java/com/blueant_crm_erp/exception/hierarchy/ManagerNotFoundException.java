package com.blueant_crm_erp.exception.hierarchy;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested reporting manager
 * does not exist in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - Reporting Manager not found
 * - Team Leader not found
 * - Department Manager not found
 * - CRM Head not found
 * - Leader mapping missing
 * - Invalid Manager Id
 * - Inactive Manager
 *
 * Modules:
 * - Hierarchy
 * - User
 * - Team
 * - Department
 * - Mapping
 *
 * HTTP Status : 404 NOT_FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class ManagerNotFoundException extends BaseException {

    /**
     * Default Constructor
     */
    public ManagerNotFoundException() {
        super(
                ErrorCode.MANAGER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.MANAGER_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message
     */
    public ManagerNotFoundException(String message) {
        super(
                ErrorCode.MANAGER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public ManagerNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.MANAGER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public ManagerNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.MANAGER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause
     */
    public ManagerNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.MANAGER_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}