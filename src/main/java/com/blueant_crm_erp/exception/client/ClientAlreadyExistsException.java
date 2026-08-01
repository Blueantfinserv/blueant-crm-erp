package com.blueant_crm_erp.exception.client;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when attempting to create
 * a client that already exists in the system.
 *
 * Duplicate validation can be based on:
 * - PAN Number
 * - Mobile Number
 * - Email Address
 * - Client Code
 *
 * Used In:
 * - Client Registration
 * - Lead Conversion
 * - Client Onboarding
 * - Service Request
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class ClientAlreadyExistsException extends BaseException {

    /**
     * Default Constructor
     */
    public ClientAlreadyExistsException() {
        super(
                ErrorCode.CLIENT_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.CLIENT_ALREADY_EXISTS
        );
    }

    /**
     * Constructor with custom message
     */
    public ClientAlreadyExistsException(String message) {
        super(
                ErrorCode.CLIENT_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public ClientAlreadyExistsException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.CLIENT_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public ClientAlreadyExistsException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.CLIENT_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause
     */
    public ClientAlreadyExistsException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.CLIENT_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}