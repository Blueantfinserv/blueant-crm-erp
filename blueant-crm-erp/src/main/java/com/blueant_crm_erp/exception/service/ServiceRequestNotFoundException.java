package com.blueant_crm_erp.exception.service;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested service request
 * cannot be found in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - Service Request Id does not exist
 * - Invalid Service Request Number
 * - Client has no Service Request
 * - Converted Lead has no Service Request
 * - Service Request deleted or archived
 * - Invalid Service Request Reference
 *
 * Search Criteria:
 * - Service Request Id
 * - Service Request Number
 * - Client Id
 * - Lead Id
 * - PAN Number
 *
 * Used In:
 * - Service Request Module
 * - CRM Module
 * - Client Module
 * - Investment Processing
 * - Operations Module
 *
 * HTTP Status : 404 NOT_FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class ServiceRequestNotFoundException extends BaseException {

    /**
     * Default Constructor.
     */
    public ServiceRequestNotFoundException() {

        super(
                ErrorCode.SERVICE_REQUEST_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.SERVICE_REQUEST_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message.
     */
    public ServiceRequestNotFoundException(String message) {

        super(
                ErrorCode.SERVICE_REQUEST_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public ServiceRequestNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.SERVICE_REQUEST_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public ServiceRequestNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.SERVICE_REQUEST_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public ServiceRequestNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.SERVICE_REQUEST_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}