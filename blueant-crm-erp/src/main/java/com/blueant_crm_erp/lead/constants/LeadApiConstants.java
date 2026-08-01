package com.blueant_crm_erp.lead.constants;

/**
 * ============================================================================
 * Lead API Constants
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Contains all REST API endpoint constants for Lead module.
 *
 * Author  : BlueAnt Development Team
 * ============================================================================
 */
public final class LeadApiConstants {

    private LeadApiConstants() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * =========================================================================
     * Base URL
     * =========================================================================
     */

    public static final String BASE_URL = "/api/v1/leads";

    /*
     * =========================================================================
     * CRUD APIs
     * =========================================================================
     */

    public static final String CREATE = "";
    public static final String GET_BY_ID = "/{leadId}";
    public static final String UPDATE = "/{leadId}";
    public static final String DELETE = "/{leadId}";
    public static final String RESTORE = "/{leadId}/restore";

    /*
     * =========================================================================
     * Search APIs
     * =========================================================================
     */

    public static final String SEARCH = "/search";
    public static final String DROPDOWN = "/dropdown";
    public static final String ALL = "/all";

    /*
     * =========================================================================
     * Lead Assignment APIs
     * =========================================================================
     */

    public static final String ASSIGN = "/{leadId}/assign";
    public static final String TRANSFER = "/{leadId}/transfer";

    /*
     * =========================================================================
     * Lead Status APIs
     * =========================================================================
     */

    public static final String CHANGE_STATUS = "/{leadId}/status";
    public static final String CHANGE_PRIORITY = "/{leadId}/priority";
    public static final String CONVERT = "/{leadId}/convert";

    /*
     * =========================================================================
     * Lead Lookup APIs
     * =========================================================================
     */

    public static final String GET_BY_CODE = "/code/{leadCode}";
    public static final String GET_BY_MOBILE = "/mobile/{mobileNumber}";
    public static final String GET_BY_UNIQUE_ID = "/unique/{uniqueId}";

    /*
     * =========================================================================
     * Sales Person APIs
     * =========================================================================
     */

    public static final String MY_LEADS = "/my";
    public static final String MY_PENDING_LEADS = "/my/pending";
    public static final String MY_CONVERTED_LEADS = "/my/converted";

    /*
     * =========================================================================
     * Dashboard APIs
     * =========================================================================
     */

    public static final String COUNT = "/count";
    public static final String STATISTICS = "/statistics";

}