package com.blueant_crm_erp.exception.common;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when attempting to create
 * a resource that already exists in the system.
 *
 * This is a generic duplicate resource exception
 * used across the BlueAnt CRM ERP Platform.
 *
 * Common Use Cases:
 * - Duplicate User
 * - Duplicate Role
 * - Duplicate Department
 * - Duplicate Team
 * - Duplicate Designation
 * - Duplicate Mapping
 * - Duplicate Lead
 * - Duplicate Client
 * - Duplicate Service Request
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class DuplicateResourceException extends BaseException {

    /**
     * Default Constructor
     */
    public DuplicateResourceException() {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.DUPLICATE_RESOURCE
        );
    }

    /**
     * Constructor with custom message.
     */
    public DuplicateResourceException(String message) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public DuplicateResourceException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.DUPLICATE_RESOURCE,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public DuplicateResourceException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.DUPLICATE_RESOURCE,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public DuplicateResourceException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.DUPLICATE_RESOURCE,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}