package com.blueant_crm_erp.exception.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a single field validation error.
 *
 * Used By:
 * - GlobalExceptionHandler
 * - ValidationException
 * - MethodArgumentNotValidException
 * - ConstraintViolationException
 *
 * Every invalid field submitted by the client
 * will be converted into this response object.
 *
 * Example:
 *
 * {
 *     "field":"mobileNumber",
 *     "rejectedValue":"12345",
 *     "message":"Mobile number must contain exactly 10 digits.",
 *     "code":"Pattern"
 * }
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldValidationError(

        /**
         * Invalid field name.
         */
        String field,

        /**
         * Value submitted by client.
         */
        Object rejectedValue,

        /**
         * Validation error message.
         */
        String message,

        /**
         * Validation code.
         *
         * Examples:
         * NotNull
         * NotBlank
         * Size
         * Pattern
         * Min
         * Max
         */
        String code

) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}