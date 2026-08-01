package com.blueant_crm_erp.exception.validation;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when a client sends
 * an invalid or malformed request that
 * violates business or API validation rules.
 *
 * Business Scenarios:
 * - Invalid request payload
 * - Missing mandatory request data
 * - Unsupported request parameters
 * - Invalid search criteria
 * - Invalid pagination request
 * - Invalid sorting request
 * - Invalid filter combination
 * - Invalid API request
 *
 * Used In:
 * - All REST Controllers
 * - Validation Layer
 * - Service Layer
 * - Common Utilities
 *
 * HTTP Status : 400 BAD_REQUEST
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class InvalidRequestException extends BaseException {

    /**
     * Default Constructor.
     */
    public InvalidRequestException() {

        super(
                ErrorCode.INVALID_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                ExceptionMessage.INVALID_REQUEST
        );
    }

    /**
     * Constructor with custom message.
     */
    public InvalidRequestException(String message) {

        super(
                ErrorCode.INVALID_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public InvalidRequestException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.INVALID_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public InvalidRequestException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.INVALID_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public InvalidRequestException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.INVALID_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                details,
                cause
        );
    }

}