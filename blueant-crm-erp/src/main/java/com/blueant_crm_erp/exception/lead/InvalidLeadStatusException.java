package com.blueant_crm_erp.exception.lead;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when an invalid lead status
 * or invalid lead status transition is detected.
 *
 * Business Scenarios:
 * - Invalid Lead Status
 * - Invalid Status Transition
 * - Reopening Converted Lead
 * - Reopening Removed Lead
 * - Invalid Follow-up Status
 * - Invalid Meeting Status
 * - Invalid Lead Workflow
 *
 * BlueAnt CRM ERP Workflow:
 *
 * NEW
 *      ↓
 * WORK_IN_PROGRESS
 *      ↓
 * FOLLOW_UP_PENDING
 *      ↓
 * MEETING_SCHEDULED
 *      ↓
 * CONVERTED
 *
 * Terminal States:
 * - CONVERTED
 * - ALREADY_CLIENT
 * - NOT_INTERESTED
 * - REMOVED
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class InvalidLeadStatusException extends BaseException {

    /**
     * Default Constructor
     */
    public InvalidLeadStatusException() {

        super(
                ErrorCode.INVALID_LEAD_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.INVALID_LEAD_STATUS
        );
    }

    /**
     * Constructor with custom message.
     */
    public InvalidLeadStatusException(String message) {

        super(
                ErrorCode.INVALID_LEAD_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public InvalidLeadStatusException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.INVALID_LEAD_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public InvalidLeadStatusException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.INVALID_LEAD_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public InvalidLeadStatusException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.INVALID_LEAD_STATUS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}