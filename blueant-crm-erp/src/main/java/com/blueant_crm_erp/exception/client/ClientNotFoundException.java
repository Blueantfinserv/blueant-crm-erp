package com.blueant_crm_erp.exception.client;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested client
 * cannot be found in the system.
 *
 * Used In:
 * - Client Search
 * - Client Profile
 * - Service Request
 * - Lead Conversion
 * - CRM Operations
 * - Transaction Processing
 *
 * Search Criteria:
 * - Client Id
 * - Client Code
 * - PAN Number
 * - Mobile Number
 * - Email Address
 *
 * HTTP Status : 404 NOT FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class ClientNotFoundException extends BaseException {

    /**
     * Default Constructor
     */
    public ClientNotFoundException() {
        super(
                ErrorCode.CLIENT_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.CLIENT_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message
     */
    public ClientNotFoundException(String message) {
        super(
                ErrorCode.CLIENT_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public ClientNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.CLIENT_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public ClientNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.CLIENT_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause
     */
    public ClientNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.CLIENT_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}