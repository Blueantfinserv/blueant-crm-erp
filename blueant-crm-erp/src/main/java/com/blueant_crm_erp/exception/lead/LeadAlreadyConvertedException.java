package com.blueant_crm_erp.exception.lead;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when an operation is attempted
 * on a lead that has already been converted into a client.
 *
 * Business Rules:
 * - Converted lead cannot be converted again.
 * - Converted lead cannot be reassigned.
 * - Converted lead cannot be deleted.
 * - Converted lead cannot be merged.
 * - Converted lead cannot return to previous statuses.
 * - Further operations should be performed on the Client module.
 *
 * Modules:
 * - Lead
 * - Client
 * - CRM
 * - Sales
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class LeadAlreadyConvertedException extends BaseException {

    /**
     * Default Constructor
     */
    public LeadAlreadyConvertedException() {

        super(
                ErrorCode.LEAD_ALREADY_CONVERTED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.LEAD_ALREADY_CONVERTED
        );
    }

    /**
     * Constructor with custom message.
     */
    public LeadAlreadyConvertedException(String message) {

        super(
                ErrorCode.LEAD_ALREADY_CONVERTED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public LeadAlreadyConvertedException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.LEAD_ALREADY_CONVERTED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public LeadAlreadyConvertedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.LEAD_ALREADY_CONVERTED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with additional details and root cause.
     */
    public LeadAlreadyConvertedException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.LEAD_ALREADY_CONVERTED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}