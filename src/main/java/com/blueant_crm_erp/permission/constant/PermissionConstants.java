package com.blueant_crm_erp.permission.constant;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ============================================================================
 * Permission Constants
 * ============================================================================
 *
 * Centralized constants for the Permission Management module.
 *
 * This class contains:
 * • API Endpoints
 * • URI Path Variables
 * • Database Constants
 * • Validation Limits
 * • Pagination Defaults
 * • Sorting Defaults
 * • Cache Names
 * • Permission Modules
 * • Success Messages
 * • Error Messages
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * ============================================================================
 */
@RequestMapping(PermissionConstants.API_BASE)
public final class PermissionConstants {

    /**
     * Prevent instantiation.
     */
    private PermissionConstants() {
        throw new UnsupportedOperationException(
                "PermissionConstants is a utility class and cannot be instantiated."
        );
    }

    // =========================================================================
    // API Base Path
    // =========================================================================

    public static final String API_BASE = "/v1/permissions";

    // =========================================================================
    // Path Variables
    // =========================================================================

    public static final String PERMISSION_ID = "/{permissionId}";

    // =========================================================================
    // API Endpoints
    // =========================================================================

    public static final String SEARCH = "/search";

    public static final String STATUS = "/status";

    public static final String RESTORE = "/restore";

    public static final String DROPDOWN = "/dropdown";

    public static final String EXISTS = "/exists";

    public static final String COUNT = "/count";

    public static final String ACTIVE_COUNT = "/count/active";

    public static final String BY_CODE = "/code/{code}";

    public static final String ALL = "/all";

    // =========================================================================
    // Database
    // =========================================================================

    public static final String TABLE_NAME = "permissions";

    public static final String UNIQUE_CODE = "uk_permission_code";

    // =========================================================================
    // Permission Modules
    // =========================================================================

    public static final String SALES = "SALES";

    public static final String CRM = "CRM";

    public static final String USER = "USER";

    public static final String ROLE = "ROLE";

    public static final String AUTH = "AUTH";

    public static final String HR = "HR";

    public static final String ATTENDANCE = "ATTENDANCE";

    public static final String LEAVE = "LEAVE";

    public static final String HELPDESK = "HELPDESK";

    public static final String DASHBOARD = "DASHBOARD";

    public static final String REPORT = "REPORT";

    public static final String NOTIFICATION = "NOTIFICATION";

    public static final String INSURANCE = "INSURANCE";

    public static final String SHARE = "SHARE";

    public static final String LOAN = "LOAN";

    public static final String SETTINGS = "SETTINGS";

    // =========================================================================
    // Validation
    // =========================================================================

    public static final int NAME_MIN_LENGTH = 3;

    public static final int NAME_MAX_LENGTH = 100;

    public static final int CODE_MIN_LENGTH = 3;

    public static final int CODE_MAX_LENGTH = 100;

    public static final int MODULE_MAX_LENGTH = 100;

    public static final int DESCRIPTION_MAX_LENGTH = 500;

    public static final int REMARKS_MAX_LENGTH = 500;

    public static final int DISPLAY_ORDER_MIN = 1;

    public static final int DISPLAY_ORDER_MAX = 9999;

    // =========================================================================
    // Pagination
    // =========================================================================

    public static final int DEFAULT_PAGE = 0;

    public static final int DEFAULT_SIZE = 10;

    public static final int MAX_PAGE_SIZE = 100;

    // =========================================================================
    // Sorting
    // =========================================================================

    public static final String DEFAULT_SORT_BY = "displayOrder";

    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    // =========================================================================
    // Cache
    // =========================================================================

    public static final String PERMISSION_CACHE = "permissions";

    public static final String PERMISSION_DROPDOWN_CACHE = "permission_dropdown";

    // =========================================================================
    // Success Messages
    // =========================================================================

    public static final String PERMISSION_CREATED_SUCCESS =
            "Permission created successfully.";

    public static final String PERMISSION_UPDATED_SUCCESS =
            "Permission updated successfully.";

    public static final String PERMISSION_DELETED_SUCCESS =
            "Permission deleted successfully.";

    public static final String PERMISSION_RESTORED_SUCCESS =
            "Permission restored successfully.";

    public static final String PERMISSION_STATUS_UPDATED_SUCCESS =
            "Permission status updated successfully.";

    // =========================================================================
    // Error Messages
    // =========================================================================

    public static final String PERMISSION_NOT_FOUND =
            "Permission not found.";

    public static final String PERMISSION_ALREADY_EXISTS =
            "Permission already exists.";

    public static final String PERMISSION_CODE_ALREADY_EXISTS =
            "Permission code already exists.";

    public static final String PERMISSION_NAME_ALREADY_EXISTS =
            "Permission name already exists.";

    public static final String PERMISSION_ASSIGNED_TO_ROLE =
            "Permission is assigned to one or more roles.";

    // =========================================================================
    // Audit
    // =========================================================================

    public static final String CREATED_BY_SYSTEM = "SYSTEM";

}