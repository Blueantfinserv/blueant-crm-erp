package com.blueant_crm_erp.bootstrap.constant;

/**
 * =============================================================================
 * Bootstrap Constants
 * =============================================================================
 *
 * Centralized constants for Database Bootstrap Module.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Bootstrap Configuration
 * • Default Super Admin
 * • Default Password
 * • Seeder Names
 * • Execution Order
 * * Seed Data Count
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public final class BootstrapConstants {

    private BootstrapConstants() {
        throw new IllegalStateException("Utility class");
    }

    // =========================================================================
    // Bootstrap
    // =========================================================================

    public static final String MODULE_NAME = "BOOTSTRAP";

    public static final String DEFAULT_COUNTRY = "India";

    public static final String DEFAULT_TIME_ZONE = "Asia/Kolkata";

    // =========================================================================
    // Default Super Admin
    // =========================================================================

    public static final String SUPER_ADMIN_EMPLOYEE_CODE = "EMP000001";

    public static final String SUPER_ADMIN_FIRST_NAME = "Super";

    public static final String SUPER_ADMIN_LAST_NAME = "Admin";

    public static final String SUPER_ADMIN_EMAIL =
            "admin@blueantcrm.com";

    public static final String SUPER_ADMIN_MOBILE =
            "9999999999";

    /**
     * Raw password.
     *
     * BootstrapService will encode before saving.
     */
    public static final String SUPER_ADMIN_PASSWORD =
            "Admin@123";

    // =========================================================================
    // Default Role Codes
    // =========================================================================

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    public static final String ROLE_ADMIN = "ADMIN";

    public static final String ROLE_BUSINESS_HEAD = "BUSINESS_HEAD";

    public static final String ROLE_SALES_MANAGER = "SALES_MANAGER";

    public static final String ROLE_TEAM_LEADER = "TEAM_LEADER";

    public static final String ROLE_RELATIONSHIP_MANAGER =
            "RELATIONSHIP_MANAGER";

    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    // =========================================================================
    // Default Department Codes
    // =========================================================================

    public static final String DEPT_SALES = "SALES";
    public static final String DEPT_HR = "HR";
    public static final String DEPT_OPS = "OPS";
    public static final String DEPT_CRM = "CRM";
    public static final String DEPT_ACC = "ACC";
    public static final String DEPT_HELP = "HELP";

    // =========================================================================
    // Default Designation Codes
    // =========================================================================

    public static final String DESIG_BH = "BH";
    public static final String DESIG_SM = "SM";
    public static final String DESIG_HRM = "HRM";

    // =========================================================================
    // Default Team Codes
    // =========================================================================

    public static final String TEAM_ST1 = "ST1";
    public static final String TEAM_ST2 = "ST2";

    // =========================================================================
    // Default Permission Modules
    // =========================================================================

    public static final String PERM_MODULE_USER = "USER";
    public static final String PERM_MODULE_ROLE = "ROLE";

    // Permission Keys
    public static final String PERM_USER_CREATE = "USER_CREATE";
    public static final String PERM_ROLE_CREATE = "ROLE_CREATE";

    // =========================================================================
    // Seeder Names
    // =========================================================================

    public static final String DEPARTMENT_SEEDER =
            "Department Seeder";

    public static final String DESIGNATION_SEEDER =
            "Designation Seeder";

    public static final String TEAM_SEEDER =
            "Team Seeder";

    public static final String ROLE_SEEDER =
            "Role Seeder";

    public static final String PERMISSION_SEEDER =
            "Permission Seeder";

    public static final String ROLE_PERMISSION_SEEDER =
            "Role Permission Seeder";

    public static final String USER_SEEDER =
            "User Seeder";

    // =========================================================================
    // Seeder Execution Order
    // =========================================================================

    public static final int ORDER_LEVEL_1 = 1;
    public static final int ORDER_LEVEL_2 = 2;
    public static final int ORDER_LEVEL_3 = 3;

    public static final int DEPARTMENT_ORDER = 1;

    public static final int DESIGNATION_ORDER = 2;

    public static final int TEAM_ORDER = 3;

    public static final int ROLE_ORDER = 4;

    public static final int PERMISSION_ORDER = 5;

    public static final int ROLE_PERMISSION_ORDER = 6;

    public static final int USER_ORDER = 7;

    // =========================================================================
    // Default Seed Count
    // =========================================================================

    public static final int DEFAULT_DEPARTMENT_COUNT = 6;

    public static final int DEFAULT_DESIGNATION_COUNT = 20;

    public static final int DEFAULT_TEAM_COUNT = 10;

    public static final int DEFAULT_ROLE_COUNT = 7;

    public static final int DEFAULT_PERMISSION_COUNT = 100;

}