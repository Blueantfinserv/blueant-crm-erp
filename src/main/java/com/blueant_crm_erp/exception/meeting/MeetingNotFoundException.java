package com.blueant_crm_erp.exception.meeting;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested meeting
 * cannot be found in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - Meeting Id does not exist
 * - Meeting Code does not exist
 * - Meeting record deleted
 * - Invalid Meeting Reference
 * - Meeting lookup failed
 * - Process Coordinator verification failed
 *
 * Search Criteria:
 * - Meeting Id
 * - Meeting Code
 * - Lead Id
 * - Client Id
 * - Sales Person Id
 *
 * Modules:
 * - Meeting
 * - Lead
 * - CRM
 * - Process Coordinator
 * - KPI Dashboard
 *
 * HTTP Status : 404 NOT_FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class MeetingNotFoundException extends BaseException {

    /**
     * Default Constructor
     */
    public MeetingNotFoundException() {
        super(
                ErrorCode.MEETING_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.MEETING_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message.
     */
    public MeetingNotFoundException(String message) {
        super(
                ErrorCode.MEETING_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public MeetingNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.MEETING_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public MeetingNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.MEETING_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with additional details and root cause.
     */
    public MeetingNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.MEETING_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}