package com.blueant_crm_erp.exception.service;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when an operation is attempted on
 * a service request that has already been completed.
 *
 * BlueAnt CRM ERP Business Rules:
 *
 * A completed service request is immutable.
 *
 * Once a service reaches COMPLETED status:
 *
 * • It cannot be completed again.
 * • It cannot be processed again.
 * • It cannot return to any previous stage.
 * • Documents cannot be re-submitted.
 * • CRM processing cannot restart.
 * • Incentive workflow cannot restart.
 * • First payment cannot be processed twice.
 *
 * Used In:
 * - Service Request Module
 * - CRM Module
 * - Client Module
 * - Investment Processing
 * - Incentive Module
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class ServiceAlreadyCompletedException extends BaseException {

    /**
     * Default Constructor.
     */
    public ServiceAlreadyCompletedException() {

        super(
                ErrorCode.SERVICE_ALREADY_COMPLETED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.SERVICE_ALREADY_COMPLETED
        );
    }

    /**
     * Constructor with custom message.
     */
    public ServiceAlreadyCompletedException(String message) {

        super(
                ErrorCode.SERVICE_ALREADY_COMPLETED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public ServiceAlreadyCompletedException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.SERVICE_ALREADY_COMPLETED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public ServiceAlreadyCompletedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.SERVICE_ALREADY_COMPLETED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public ServiceAlreadyCompletedException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.SERVICE_ALREADY_COMPLETED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}