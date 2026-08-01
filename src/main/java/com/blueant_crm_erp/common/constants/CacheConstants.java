package com.blueant_crm_erp.common.constants;

/**
 * Cache Constants
 *
 * Centralized cache names used throughout the application.
 *
 * Used By:
 * - Redis
 * - Spring Cache
 * - Service Layer
 * - Dashboard
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class CacheConstants {

    private CacheConstants() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * ===========================================================
     * CACHE PREFIX
     * ===========================================================
     */

    public static final String CACHE_PREFIX = "BLUEANT::";

    /*
     * ===========================================================
     * AUTH MODULE
     * ===========================================================
     */

    /*
     * ===========================================================
     * AUTH MODULE
     * ===========================================================
     */

    public static final String AUTH_CACHE =
            CACHE_PREFIX + "AUTH";

    public static final String JWT_CACHE =
            CACHE_PREFIX + "JWT";

    /**
     * Cache used for storing refresh tokens.
     */
    public static final String REFRESH_TOKEN_CACHE =
            CACHE_PREFIX + "REFRESH_TOKEN";

    public static final String OTP_CACHE =
            CACHE_PREFIX + "OTP";
    /*
     * ===========================================================
     * USER MODULE
     * ===========================================================
     */

    public static final String USER_CACHE =
            CACHE_PREFIX + "USER";

    public static final String USER_PROFILE_CACHE =
            CACHE_PREFIX + "USER_PROFILE";

    /*
     * ===========================================================
     * ROLE MODULE
     * ===========================================================
     */

    public static final String ROLE_CACHE =
            CACHE_PREFIX + "ROLE";

    /*
     * ===========================================================
     * HIERARCHY MODULE
     * ===========================================================
     */

    public static final String HIERARCHY_CACHE =
            CACHE_PREFIX + "HIERARCHY";

    public static final String TEAM_CACHE =
            CACHE_PREFIX + "TEAM";

    /*
     * ===========================================================
     * SALES MODULE
     * ===========================================================
     */

    public static final String LEAD_CACHE =
            CACHE_PREFIX + "LEAD";

    public static final String FOLLOWUP_CACHE =
            CACHE_PREFIX + "FOLLOWUP";

    public static final String MEETING_CACHE =
            CACHE_PREFIX + "MEETING";

    /*
     * ===========================================================
     * CLIENT MODULE
     * ===========================================================
     */

    public static final String CLIENT_CACHE =
            CACHE_PREFIX + "CLIENT";

    public static final String ONBOARDING_CACHE =
            CACHE_PREFIX + "ONBOARDING";

    public static final String CRM_CACHE =
            CACHE_PREFIX + "CRM";

    /*
     * ===========================================================
     * SERVICE MODULE
     * ===========================================================
     */

    public static final String SERVICE_REQUEST_CACHE =
            CACHE_PREFIX + "SERVICE_REQUEST";

    /*
     * ===========================================================
     * FINANCE MODULE
     * ===========================================================
     */

    public static final String TRANSACTION_CACHE =
            CACHE_PREFIX + "TRANSACTION";

    public static final String FMS_CACHE =
            CACHE_PREFIX + "FMS";

    /*
     * ===========================================================
     * HELPDESK MODULE
     * ===========================================================
     */

    public static final String HELPDESK_CACHE =
            CACHE_PREFIX + "HELPDESK";

    /*
     * ===========================================================
     * NOTIFICATION MODULE
     * ===========================================================
     */

    public static final String NOTIFICATION_CACHE =
            CACHE_PREFIX + "NOTIFICATION";

    /*
     * ===========================================================
     * REPORT MODULE
     * ===========================================================
     */

    public static final String REPORT_CACHE =
            CACHE_PREFIX + "REPORT";

    /*
     * ===========================================================
     * DASHBOARD MODULE
     * ===========================================================
     */

    public static final String DASHBOARD_CACHE =
            CACHE_PREFIX + "DASHBOARD";

    /*
     * ===========================================================
     * ANALYTICS MODULE
     * ===========================================================
     */

    public static final String ANALYTICS_CACHE =
            CACHE_PREFIX + "ANALYTICS";

    /*
     * ===========================================================
     * MASTER DATA
     * ===========================================================
     */

    public static final String MASTER_DATA_CACHE =
            CACHE_PREFIX + "MASTER_DATA";


}