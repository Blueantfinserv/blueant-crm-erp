package com.blueant_crm_erp.exception.common;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested resource
 * does not exist in the system.
 *
 * This is a generic exception that can be reused
 * across all modules of the BlueAnt CRM ERP Platform.
 *
 * Used In:
 * - Lead Module
 * - User Module
 * - Client Module
 * - Role Module
 * - Department Module
 * - Meeting Module
 * - Service Request Module
 * - Transaction Module
 * - HR Module
 *
 * HTTP Status : 404 NOT_FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class ResourceNotFoundException extends BaseException {

    /**
     * Default Constructor
     */
    public ResourceNotFoundException() {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.RESOURCE_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message.
     */
    public ResourceNotFoundException(String message) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public ResourceNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public ResourceNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with additional details and root cause.
     */
    public ResourceNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}