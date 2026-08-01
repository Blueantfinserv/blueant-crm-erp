package com.blueant_crm_erp.lead.constants;

/**
 * ============================================================================
 * Lead Validation Constants
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Contains validation messages, field length limits and regex patterns
 * used throughout the Lead module.
 * ============================================================================
 */
public final class LeadValidationConstants {

    private LeadValidationConstants() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * ========================================================================
     * Validation Messages
     * ========================================================================
     */

    public static final String LEAD_ID_REQUIRED =
            "Lead ID is required.";

    public static final String LEAD_CODE_REQUIRED =
            "Lead code is required.";

    public static final String CLIENT_NAME_REQUIRED =
            "Client name is required.";

    public static final String MOBILE_NUMBER_REQUIRED =
            "Mobile number is required.";

    public static final String MOBILE_NUMBER_INVALID =
            "Please enter a valid mobile number.";

    public static final String EMAIL_INVALID =
            "Please enter a valid email address.";

    public static final String LOCATION_REQUIRED =
            "Location is required.";

    public static final String COMPANY_NAME_REQUIRED =
            "Company name is required.";

    public static final String LEAD_SOURCE_REQUIRED =
            "Lead source is required.";

    public static final String LEAD_PRIORITY_REQUIRED =
            "Lead priority is required.";

    public static final String LEAD_STATUS_REQUIRED =
            "Lead status is required.";

    public static final String LEAD_STAGE_REQUIRED =
            "Lead stage is required.";

    public static final String REMARK_REQUIRED =
            "Remark is required.";

    public static final String NEXT_PLAN_DATE_REQUIRED =
            "Next plan date is required.";

    public static final String ASSIGNED_USER_REQUIRED =
            "Assigned user is required.";

    public static final String INVALID_LEAD_STATUS =
            "Invalid lead status.";

    public static final String INVALID_LEAD_STAGE =
            "Invalid lead stage.";

    /*
     * ========================================================================
     * Length Validation
     * ========================================================================
     */

    public static final int CLIENT_NAME_MIN_LENGTH = 2;
    public static final int CLIENT_NAME_MAX_LENGTH = 100;

    public static final int COMPANY_NAME_MAX_LENGTH = 150;

    public static final int LOCATION_MAX_LENGTH = 100;

    public static final int REMARK_MAX_LENGTH = 1000;

    public static final int EMAIL_MAX_LENGTH = 100;

    public static final int MOBILE_NUMBER_LENGTH = 10;

    public static final int PAN_NUMBER_LENGTH = 10;

    /*
     * ========================================================================
     * Regex Patterns
     * ========================================================================
     */

    public static final String MOBILE_NUMBER_REGEX =
            "^[6-9]\\d{9}$";

    public static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public static final String PAN_NUMBER_REGEX =
            "^[A-Z]{5}[0-9]{4}[A-Z]{1}$";

}