package com.blueant_crm_erp.exception.validation;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when one or more business
 * validation rules fail.
 *
 * This exception is used for application-level
 * validation failures that cannot be handled
 * by Bean Validation annotations.
 *
 * Examples:
 * - Invalid Lead Status
 * - Invalid PAN Number
 * - Invalid Meeting Date
 * - Invalid Service Request
 * - Invalid Transaction
 * - Invalid Client Data
 * - Invalid Role Assignment
 *
 * Used In:
 * - Service Layer
 * - Validation Layer
 * - Business Rule Engine
 *
 * HTTP Status : 400 BAD_REQUEST
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class ValidationException extends BaseException {

    /**
     * Field validation errors.
     */
    private final List<FieldValidationError> fieldErrors;

    /**
     * Default Constructor.
     */
    public ValidationException() {

        super(
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                ExceptionMessage.VALIDATION_FAILED
        );

        this.fieldErrors = List.of();
    }

    /**
     * Constructor with message.
     */
    public ValidationException(String message) {

        super(
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message
        );

        this.fieldErrors = List.of();
    }

    /**
     * Constructor with field validation errors.
     */
    public ValidationException(
            String message,
            List<FieldValidationError> fieldErrors) {

        super(
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message
        );

        this.fieldErrors = fieldErrors == null
                ? List.of()
                : List.copyOf(fieldErrors);
    }

    /**
     * Constructor with details.
     */
    public ValidationException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                details
        );

        this.fieldErrors = List.of();
    }

    /**
     * Constructor with cause.
     */
    public ValidationException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                cause
        );

        this.fieldErrors = List.of();
    }

    /**
     * Constructor with details and cause.
     */
    public ValidationException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                details,
                cause
        );

        this.fieldErrors = List.of();
    }

    /**
     * Constructor with field errors and details.
     */
    public ValidationException(
            String message,
            List<FieldValidationError> fieldErrors,
            Map<String, Object> details) {

        super(
                ErrorCode.VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                details
        );

        this.fieldErrors = fieldErrors == null
                ? List.of()
                : List.copyOf(fieldErrors);
    }

    /**
     * Returns validation errors.
     */
    public List<FieldValidationError> getFieldErrors() {
        return fieldErrors;
    }

}