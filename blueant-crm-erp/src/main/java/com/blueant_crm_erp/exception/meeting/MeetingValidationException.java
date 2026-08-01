package com.blueant_crm_erp.exception.meeting;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when meeting business validation fails.
 *
 * Business Scenarios:
 * - Invalid Meeting Date
 * - Invalid Meeting Time
 * - Missing Meeting Mode
 * - Missing Meeting Purpose
 * - Missing Client
 * - Missing Lead
 * - Missing Sales Person
 * - Invalid Meeting Duration
 * - Invalid Meeting Location
 * - Invalid Online Meeting Link
 * - Invalid Attendees
 * - Invalid Meeting Request
 *
 * Modules:
 * - Meeting
 * - Lead
 * - Client
 * - CRM
 * - Process Coordinator
 *
 * HTTP Status : 400 BAD_REQUEST
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class MeetingValidationException extends BaseException {

    /**
     * Default Constructor
     */
    public MeetingValidationException() {

        super(
                ErrorCode.MEETING_VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                ExceptionMessage.MEETING_VALIDATION_FAILED
        );
    }

    /**
     * Constructor with custom message.
     */
    public MeetingValidationException(String message) {

        super(
                ErrorCode.MEETING_VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public MeetingValidationException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.MEETING_VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public MeetingValidationException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.MEETING_VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public MeetingValidationException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.MEETING_VALIDATION_FAILED,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message,
                details,
                cause
        );
    }

}