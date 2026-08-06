package com.blueant_crm_erp.user.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * =============================================================================
 * User Constants
 * =============================================================================
 *
 * Centralized constants used across the User Management module.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • API Endpoints
 * • Validation Limits
 * • Default Values
 * • Search Configuration
 * • Cache Names
 * • Success Messages
 * • Employee Code Prefix
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequestMapping(UserConstants.API_BASE)
public final class UserConstants {

    // =========================================================================
    // Database
    // =========================================================================

    public static final String TABLE_NAME = "users";

    public static final String UNIQUE_EMAIL =
            "uk_user_email";

    public static final String UNIQUE_MOBILE =
            "uk_user_mobile";

    public static final String UNIQUE_EMPLOYEE_CODE =
            "uk_user_employee_code";

    // =========================================================================
    // API
    // =========================================================================

    public static final String ALL = "/all";

    public static final String RESTORE = "/{userId}/restore";

    public static final String API_BASE = "/v1/users";

    public static final String USER_ID = "/{userId}";

    public static final String STATUS = "/status";

    public static final String PROFILE = "/profile";

    public static final String SEARCH = "/search";

    public static final String DROPDOWN = "/dropdown";

    public static final String RESET_PASSWORD = "/reset-password";

    public static final String CHANGE_PASSWORD = "/change-password";

    public static final String ASSIGN_ROLE = "/assign-role";

    public static final String ASSIGN_MANAGER = "/assign-manager";

    // =========================================================================
    // Employee
    // =========================================================================

    public static final String EMPLOYEE_CODE_PREFIX = "BA";

    public static final Integer EMPLOYEE_CODE_LENGTH = 6;

    // =========================================================================
    // Validation
    // =========================================================================

    public static final int FIRST_NAME_MIN_LENGTH = 2;
    public static final int FIRST_NAME_MAX_LENGTH = 100;

    public static final int LAST_NAME_MIN_LENGTH = 2;
    public static final int LAST_NAME_MAX_LENGTH = 100;

    public static final int EMAIL_MAX_LENGTH = 150;

    public static final int MOBILE_LENGTH = 10;

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;

    public static final int ADDRESS_MAX_LENGTH = 500;

    public static final int REMARKS_MAX_LENGTH = 500;

    // =========================================================================
    // Default Values
    // =========================================================================

    public static final boolean DEFAULT_ACTIVE = true;

    public static final boolean DEFAULT_ACCOUNT_NON_LOCKED = true;

    public static final boolean DEFAULT_ACCOUNT_NON_EXPIRED = true;

    public static final boolean DEFAULT_CREDENTIAL_NON_EXPIRED = true;

    public static final boolean DEFAULT_ENABLED = true;

    public static final int DEFAULT_FAILED_LOGIN_ATTEMPTS = 0;

    // =========================================================================
    // Search
    // =========================================================================

    public static final String DEFAULT_SORT_BY = "createdAt";

    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    public static final int DEFAULT_PAGE = 0;

    public static final int DEFAULT_SIZE = 10;

    public static final int MAX_PAGE_SIZE = 100;

    // =========================================================================
    // Cache
    // =========================================================================

    public static final String USER_CACHE = "users";

    public static final String USER_DROPDOWN_CACHE = "user_dropdown";

    // =========================================================================
    // Messages
    // =========================================================================

    public static final String USER_CREATED =
            "User created successfully.";

    public static final String USER_UPDATED =
            "User updated successfully.";

    public static final String USER_DELETED =
            "User deleted successfully.";

    public static final String USER_RESTORED =
            "User restored successfully.";

    public static final String USER_STATUS_UPDATED =
            "User status updated successfully.";

    public static final String PASSWORD_CHANGED =
            "Password changed successfully.";

    public static final String PASSWORD_RESET =
            "Password reset successfully.";

    public static final String ROLE_ASSIGNED =
            "Role assigned successfully.";

    public static final String MANAGER_ASSIGNED =
            "Reporting manager assigned successfully.";

}