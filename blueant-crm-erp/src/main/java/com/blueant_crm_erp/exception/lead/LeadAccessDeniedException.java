package com.blueant_crm_erp.exception.lead;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when a user attempts to access
 * or modify a lead without sufficient authorization.
 *
 * Business Rules:
 * - Sales Person can access only assigned leads.
 * - Team Leader can access leads of team members.
 * - CRM can access only allocated leads.
 * - Admin can access all leads.
 * - Cross-team lead access is not allowed.
 * - Unauthorized lead modification is prohibited.
 *
 * Modules:
 * - Lead
 * - User
 * - Role
 * - Hierarchy
 * - Security
 *
 * HTTP Status : 403 FORBIDDEN
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class LeadAccessDeniedException extends BaseException {

    /**
     * Default Constructor
     */
    public LeadAccessDeniedException() {

        super(
                ErrorCode.LEAD_ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                ExceptionMessage.LEAD_ACCESS_DENIED
        );
    }

    /**
     * Constructor with custom message.
     */
    public LeadAccessDeniedException(String message) {

        super(
                ErrorCode.LEAD_ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public LeadAccessDeniedException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.LEAD_ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public LeadAccessDeniedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.LEAD_ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public LeadAccessDeniedException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.LEAD_ACCESS_DENIED,
                ErrorType.AUTHORIZATION,
                HttpStatus.FORBIDDEN,
                message,
                details,
                cause
        );
    }

}