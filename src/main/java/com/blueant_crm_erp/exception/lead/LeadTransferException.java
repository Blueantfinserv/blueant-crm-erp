package com.blueant_crm_erp.exception.lead;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when a lead transfer operation
 * cannot be completed.
 *
 * Business Scenarios:
 * - Invalid Lead Transfer
 * - Transfer Validation Failed
 * - Lead Ownership Conflict
 * - Transfer Approval Missing
 * - Transfer Restricted by Business Rules
 * - Cross Hierarchy Transfer
 *
 * BlueAnt CRM ERP Business Rules:
 *
 * A lead can be transferred only if:
 *
 * 1. Previous Sales Person has not worked on the lead
 *    within the configured inactive period (40 days).
 *
 * OR
 *
 * 2. Previous Sales Person explicitly approves
 *    the ownership transfer.
 *
 * OR
 *
 * 3. Transfer is approved by an authorized Leader/Admin.
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class LeadTransferException extends BaseException {

    /**
     * Default Constructor
     */
    public LeadTransferException() {

        super(
                ErrorCode.LEAD_TRANSFER_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.LEAD_TRANSFER_ERROR
        );
    }

    /**
     * Constructor with custom message.
     */
    public LeadTransferException(String message) {

        super(
                ErrorCode.LEAD_TRANSFER_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public LeadTransferException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.LEAD_TRANSFER_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public LeadTransferException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.LEAD_TRANSFER_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public LeadTransferException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.LEAD_TRANSFER_ERROR,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}