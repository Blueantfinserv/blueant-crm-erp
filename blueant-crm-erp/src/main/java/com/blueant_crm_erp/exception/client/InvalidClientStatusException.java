package com.blueant_crm_erp.exception.client;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when a client is in an invalid
 * status for the requested business operation.
 *
 * Examples:
 * - Client is INACTIVE
 * - Client is BLOCKED
 * - Client is BLACKLISTED
 * - Client is REMOVED
 * - Client is CLOSED
 * - Client is PENDING_VERIFICATION
 *
 * Used In:
 * - Client Module
 * - Lead Conversion
 * - Service Request
 * - CRM Operations
 * - Transaction Processing
 * - Investment Processing
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class InvalidClientStatusException extends BaseException {

    /**
     * Default Constructor
     */
    public InvalidClientStatusException() {
        super(
                ErrorCode.INVALID_CLIENT_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.INVALID_CLIENT_STATUS
        );
    }

    /**
     * Constructor with custom message
     */
    public InvalidClientStatusException(String message) {
        super(
                ErrorCode.INVALID_CLIENT_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public InvalidClientStatusException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.INVALID_CLIENT_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public InvalidClientStatusException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.INVALID_CLIENT_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause
     */
    public InvalidClientStatusException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.INVALID_CLIENT_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}