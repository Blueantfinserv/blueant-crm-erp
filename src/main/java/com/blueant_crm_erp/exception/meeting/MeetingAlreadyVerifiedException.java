package com.blueant_crm_erp.exception.meeting;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when an attempt is made
 * to verify a meeting that has already been verified.
 *
 * Business Scenarios:
 * - Duplicate Meeting Verification
 * - Re-verification Attempt
 * - KPI Recalculation Prevention
 * - Incentive Reprocessing Prevention
 * - Verification Audit Protection
 *
 * BlueAnt CRM ERP Workflow:
 *
 * Sales Person
 *      ↓
 * Meeting Completed
 *      ↓
 * Process Coordinator Verification
 *      ↓
 * VERIFIED
 *
 * Once VERIFIED:
 * - Verification cannot be repeated.
 * - Verification details cannot be modified.
 * - KPI calculation remains locked.
 * - Incentive calculation remains locked.
 *
 * Modules:
 * - Meeting
 * - Process Coordinator
 * - KPI Dashboard
 * - Incentive
 * - Audit
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class MeetingAlreadyVerifiedException extends BaseException {

    /**
     * Default Constructor
     */
    public MeetingAlreadyVerifiedException() {

        super(
                ErrorCode.MEETING_ALREADY_VERIFIED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.MEETING_ALREADY_VERIFIED
        );
    }

    /**
     * Constructor with custom message.
     */
    public MeetingAlreadyVerifiedException(String message) {

        super(
                ErrorCode.MEETING_ALREADY_VERIFIED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public MeetingAlreadyVerifiedException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.MEETING_ALREADY_VERIFIED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public MeetingAlreadyVerifiedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.MEETING_ALREADY_VERIFIED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public MeetingAlreadyVerifiedException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.MEETING_ALREADY_VERIFIED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}