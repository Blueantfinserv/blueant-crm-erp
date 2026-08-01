package com.blueant_crm_erp.exception.lead;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested lead
 * cannot be found in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - Lead Id does not exist
 * - Lead Code does not exist
 * - Lead deleted or archived
 * - Invalid Lead Reference
 * - Lead not assigned
 * - Lead lookup failed
 *
 * Search Criteria:
 * - Lead Id
 * - Lead Code
 * - Mobile Number
 * - PAN Number
 * - Email Address
 *
 * Modules:
 * - Lead
 * - Sales
 * - CRM
 * - Mapping
 * - Dashboard
 *
 * HTTP Status : 404 NOT_FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class LeadNotFoundException extends BaseException {

    /**
     * Default Constructor
     */
    public LeadNotFoundException() {
        super(
                ErrorCode.LEAD_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.LEAD_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message.
     */
    public LeadNotFoundException(String message) {
        super(
                ErrorCode.LEAD_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public LeadNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.LEAD_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public LeadNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.LEAD_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with additional details and root cause.
     */
    public LeadNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.LEAD_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}