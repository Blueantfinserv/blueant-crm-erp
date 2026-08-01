package com.blueant_crm_erp.common.constants;

/**
 * Application Constants
 *
 * Common application-wide constants used throughout the system.
 *
 * @author BlueAnt
 * @version 1.0
 */
public final class AppConstants {

    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * ==========================================
     * APPLICATION INFORMATION
     * ==========================================
     */

    public static final String APPLICATION_NAME = "BlueAnt CRM ERP";

    public static final String APPLICATION_VERSION = "1.0.0";

    public static final String COMPANY_NAME = "BlueAnt";

    public static final String DEFAULT_TIME_ZONE = "Asia/Kolkata";

    public static final String DEFAULT_LANGUAGE = "en";

    /*
     * ==========================================
     * DATE & TIME FORMAT
     * ==========================================
     */

    public static final String DATE_FORMAT = "dd-MM-yyyy";

    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

    public static final String TIME_FORMAT = "HH:mm:ss";

    /*
     * ==========================================
     * DEFAULT VALUES
     * ==========================================
     */

    public static final String DEFAULT_PASSWORD = "Change@123";

    public static final String DEFAULT_PROFILE_IMAGE = "default-profile.png";

    public static final String SYSTEM_USER = "SYSTEM";

    public static final String UNKNOWN = "UNKNOWN";

    public static final String NOT_AVAILABLE = "N/A";

    /*
     * ==========================================
     * BOOLEAN FLAGS
     * ==========================================
     */

    public static final String YES = "YES";

    public static final String NO = "NO";

    public static final String ACTIVE = "ACTIVE";

    public static final String INACTIVE = "INACTIVE";

    public static final String ENABLED = "ENABLED";

    public static final String DISABLED = "DISABLED";

    /*
     * ==========================================
     * FILE SETTINGS
     * ==========================================
     */

    public static final long KB = 1024L;

    public static final long MB = KB * 1024;

    public static final long MAX_FILE_SIZE = 10 * MB;

    /*
     * ==========================================
     * DEFAULT PAGE SETTINGS
     * ==========================================
     */

    public static final int DEFAULT_PAGE_NUMBER = 0;

    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final int MAX_PAGE_SIZE = 100;

    /*
     * ==========================================
     * CACHE
     * ==========================================
     */

    public static final String CACHE_PREFIX = "BLUEANT::";

    /*
     * ==========================================
     * AUDIT
     * ==========================================
     */

    public static final String CREATED = "CREATED";

    public static final String UPDATED = "UPDATED";

    public static final String DELETED = "DELETED";

    /*
     * ==========================================
     * COMMON MESSAGES
     * ==========================================
     */

    public static final String SUCCESS = "Success";

    public static final String FAILED = "Failed";

    public static final String ERROR = "Error";

    /*
     * ==========================================
     * MODULE NAMES
     * ==========================================
     */

    public static final String ROLE_MODULE = "ROLE";

    public static final String USER_MODULE = "USER";

    public static final String AUTH_MODULE = "AUTH";

    public static final String HIERARCHY_MODULE = "HIERARCHY";

    public static final String LEAD_MODULE = "LEAD";

    public static final String CLIENT_MODULE = "CLIENT";

    public static final String CRM_MODULE = "CRM";

    public static final String TRANSACTION_MODULE = "TRANSACTION";

    public static final String REPORT_MODULE = "REPORT";

    public static final String HR_MODULE = "HR";

}