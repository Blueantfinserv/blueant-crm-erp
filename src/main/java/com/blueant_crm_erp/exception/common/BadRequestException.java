package com.blueant_crm_erp.exception.common;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the client sends
 * an invalid request to the server.
 *
 * Used In:
 * - Invalid Request Body
 * - Invalid Query Parameters
 * - Invalid Path Variables
 * - Invalid Pagination
 * - Invalid Sorting
 * - Invalid Business Request
 * - Invalid Status Transition
 *
 * HTTP Status : 400 BAD_REQUEST
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class BadRequestException extends BaseException {

    /**
     * Default Constructor
     */
    public BadRequestException() {
        super(
                ErrorCode.BAD_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                ExceptionMessage.BAD_REQUEST
        );
    }

    /**
     * Constructor with custom message
     */
    public BadRequestException(String message) {
        super(
                ErrorCode.BAD_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public BadRequestException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.BAD_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public BadRequestException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.BAD_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause
     */
    public BadRequestException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.BAD_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                details,
                cause
        );
    }

}