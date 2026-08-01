package com.blueant_crm_erp.exception.lead;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when attempting to create
 * a duplicate lead in the BlueAnt CRM ERP Platform.
 *
 * Business Rules:
 * - Same Mobile Number
 * - Same PAN Number
 * - Same Email Address
 * - Existing Active Lead
 * - Existing Converted Client
 * - Duplicate Lead within Cooling Period
 * - Duplicate Lead already assigned
 *
 * BlueAnt Business Logic:
 * -------------------------------------
 * A duplicate lead can be reassigned only if:
 *
 * 1. Previous Sales Person has not worked on
 *    the lead for the configured inactive period
 *    (currently 40 days), OR
 *
 * 2. Previous Sales Person explicitly transfers
 *    ownership of the lead.
 *
 * Otherwise the lead remains locked with the
 * existing owner.
 *
 * Modules:
 * - Lead
 * - Sales
 * - CRM
 * - Mapping
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class DuplicateLeadException extends BaseException {

    /**
     * Default Constructor
     */
    public DuplicateLeadException() {

        super(
                ErrorCode.DUPLICATE_LEAD,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.DUPLICATE_LEAD
        );
    }

    /**
     * Constructor with custom message.
     */
    public DuplicateLeadException(String message) {

        super(
                ErrorCode.DUPLICATE_LEAD,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public DuplicateLeadException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.DUPLICATE_LEAD,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public DuplicateLeadException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.DUPLICATE_LEAD,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public DuplicateLeadException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.DUPLICATE_LEAD,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}