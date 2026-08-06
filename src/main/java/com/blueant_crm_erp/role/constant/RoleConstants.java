package com.blueant_crm_erp.role.constant;

/**
 * ============================================================================
 * Role Constants
 * ============================================================================
 *
 * Centralized constants used throughout the Role Management module.
 *
 * This class contains:
 * • REST API Endpoints
 * • URI Path Variables
 * • Validation Limits
 * • Pagination Defaults
 * • Sorting Defaults
 * • Cache Names
 * • Success Messages
 * • Error Messages
 *
 * NOTE:
 * Role names such as ADMIN, SUPER_ADMIN, USER, etc.
 * must be maintained inside RoleType enum.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * ============================================================================
 */
public final class RoleConstants {

    /**
     * Prevent instantiation.
     */
    private RoleConstants() {
        throw new UnsupportedOperationException(
                "RoleConstants is a utility class and cannot be instantiated."
        );
    }

    // =========================================================================
    // API Base Path
    // =========================================================================

    public static final String API_BASE = "/v1/roles";

    // =========================================================================
    // Path Variables
    // =========================================================================

    public static final String ROLE_ID = "/{roleId}";

    // =========================================================================
    // API Endpoints
    // =========================================================================

    public static final String SEARCH = "/search";

    public static final String PAGE = "/page";

    public static final String STATUS = "/status";

    public static final String RESTORE = "/restore";

    public static final String PERMISSIONS = "/permissions";

    public static final String ROLE_PERMISSIONS = ROLE_ID + PERMISSIONS;

    public static final String ROLE_STATUS = ROLE_ID + STATUS;

    public static final String ROLE_RESTORE = ROLE_ID + RESTORE;

    public static final String DROPDOWN = "/dropdown";

    public static final String ACTIVE = "/active";

    public static final String ALL = "/all";

    // =========================================================================
    // Validation
    // =========================================================================

    public static final int ROLE_NAME_MIN_LENGTH = 3;

    public static final int ROLE_NAME_MAX_LENGTH = 100;

    public static final int ROLE_CODE_MIN_LENGTH = 2;

    public static final int ROLE_CODE_MAX_LENGTH = 30;

    public static final int DESCRIPTION_MAX_LENGTH = 500;

    // =========================================================================
    // Pagination
    // =========================================================================

    public static final int DEFAULT_PAGE = 0;

    public static final int DEFAULT_SIZE = 10;

    public static final int MAX_PAGE_SIZE = 100;

    // =========================================================================
    // Sorting
    // =========================================================================

    public static final String DEFAULT_SORT_BY = "createdAt";

    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // =========================================================================
    // Cache
    // =========================================================================

    public static final String ROLE_CACHE = "roles";

    public static final String ROLE_PERMISSION_CACHE = "role_permissions";

    public static final String ROLE_DROPDOWN_CACHE = "role_dropdown";

    // =========================================================================
    // Success Messages
    // =========================================================================

    public static final String ROLE_CREATED_SUCCESS =
            "Role created successfully.";

    public static final String ROLE_UPDATED_SUCCESS =
            "Role updated successfully.";

    public static final String ROLE_DELETED_SUCCESS =
            "Role deleted successfully.";

    public static final String ROLE_RESTORED_SUCCESS =
            "Role restored successfully.";

    public static final String ROLE_STATUS_UPDATED_SUCCESS =
            "Role status updated successfully.";

    public static final String ROLE_PERMISSION_ASSIGNED_SUCCESS =
            "Permissions assigned successfully.";

    public static final String ROLE_PERMISSION_REMOVED_SUCCESS =
            "Permissions removed successfully.";

    // =========================================================================
    // Error Messages
    // =========================================================================

    public static final String ROLE_NOT_FOUND =
            "Role not found.";

    public static final String ROLE_ALREADY_EXISTS =
            "Role already exists.";

    public static final String ROLE_CODE_ALREADY_EXISTS =
            "Role code already exists.";

    public static final String ROLE_IN_USE =
            "Role is currently assigned and cannot be deleted.";

    public static final String INVALID_ROLE_STATUS =
            "Invalid role status.";
}