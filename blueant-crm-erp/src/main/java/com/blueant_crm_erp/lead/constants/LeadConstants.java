package com.blueant_crm_erp.lead.constants;

/**
 * ============================================================================
 * Lead Constants
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 * Purpose : Common business constants used across Lead module.
 *
 * NOTE:
 * - Validation messages should be placed in LeadValidationConstants.
 * - API endpoints should be placed in LeadApiConstants.
 * - This class contains only business constants and default values.
 * ============================================================================
 */
public final class LeadConstants {

    private LeadConstants() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * ========================================================================
     * Module Information
     * ========================================================================
     */

    public static final String MODULE_NAME = "Lead";
    public static final String MODULE_CODE = "LEAD";

    /*
     * ========================================================================
     * Lead Code
     * ========================================================================
     */

    public static final String LEAD_CODE_PREFIX = "LD";
    public static final int LEAD_CODE_PADDING = 6;

    /*
     * ========================================================================
     * Duplicate Lead Rules
     * ========================================================================
     */

    public static final String LEAD_DUPLICATE_MOBILE = "A lead with this mobile number already exists.";

    /**
     * Lead can be reassigned only if previous salesperson has not worked
     * on the lead for 40 days.
     */
    public static final int DUPLICATE_LEAD_TRANSFER_DAYS = 40;

    /*
     * ========================================================================
     * Default Values
     * ========================================================================
     */

    public static final String DEFAULT_LOCATION = "NA";
    public static final String DEFAULT_COMPANY_NAME = "Not Available";
    public static final String DEFAULT_REMARK = "-";

    /*
     * ========================================================================
     * Search & Pagination
     * ========================================================================
     */

    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    /*
     * ========================================================================
     * Date Formats
     * ========================================================================
     */

    public static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy";
    public static final String DEFAULT_DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

    /*
     * ========================================================================
     * File Upload
     * ========================================================================
     */

    public static final long MAX_DOCUMENT_SIZE = 5 * 1024 * 1024L; // 5 MB

    public static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024L; // 2 MB

    /*
     * ========================================================================
     * Supported File Types
     * ========================================================================
     */

    public static final String PDF = "application/pdf";

    public static final String JPEG = "image/jpeg";

    public static final String PNG = "image/png";

    /*
     * ========================================================================
     * Lead Assignment
     * ========================================================================
     */

    public static final String AUTO_ASSIGN = "AUTO";
    public static final String MANUAL_ASSIGN = "MANUAL";

    /*
     * ========================================================================
     * Service Request
     * ========================================================================
     */

    public static final String SERVICE_REQUEST_REQUIRED = "YES";
    public static final String SERVICE_REQUEST_NOT_REQUIRED = "NO";

    /*
     * ========================================================================
     * Dashboard Labels
     * ========================================================================
     */

    public static final String TOTAL_LEADS = "Total Leads";
    public static final String TODAY_LEADS = "Today's Leads";
    public static final String CONVERTED_LEADS = "Converted Leads";
    public static final String FOLLOW_UP_PENDING = "Follow-up Pending";

    /*
     * ========================================================================
     * Lead Timeline
     * ========================================================================
     */

    public static final String CREATED = "Lead Created";
    public static final String UPDATED = "Lead Updated";
    public static final String ASSIGNED = "Lead Assigned";
    public static final String TRANSFERRED = "Lead Transferred";
    public static final String CONVERTED = "Lead Converted";
    public static final String CLOSED = "Lead Closed";

}