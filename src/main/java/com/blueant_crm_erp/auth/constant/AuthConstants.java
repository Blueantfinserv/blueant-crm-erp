package com.blueant_crm_erp.auth.constant;

/**
 * =============================================================================
 * Auth Constants
 * =============================================================================
 *
 * Centralized constants for Authentication & Authorization.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Authentication API constants
 * • JWT constants
 * • Security headers
 * • Token types
 * • Authentication messages
 * • Password policy constants
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public final class AuthConstants {

    private AuthConstants() {
        throw new IllegalStateException("Utility class");
    }

    // =========================================================================
    // API BASE PATH
    // =========================================================================

    public static final String API_BASE = "/auth";

    // =========================================================================
    // API ENDPOINTS
    // =========================================================================

    public static final String LOGIN = "/login";

    public static final String LOGOUT = "/logout";

    public static final String REFRESH_TOKEN = "/refresh-token";

    public static final String FORGOT_PASSWORD = "/forgot-password";

    public static final String RESET_PASSWORD = "/reset-password";

    public static final String CHANGE_PASSWORD = "/change-password";

    public static final String VERIFY_OTP = "/verify-otp";

    public static final String RESEND_OTP = "/resend-otp";

    public static final String CURRENT_USER = "/me";

    // =========================================================================
    // JWT
    // =========================================================================

    public static final String TOKEN_TYPE = "Bearer";

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";

    public static final String JWT_CLAIM_USER_ID = "userId";

    public static final String JWT_CLAIM_EMPLOYEE_CODE = "employeeCode";

    public static final String JWT_CLAIM_EMAIL = "email";

    public static final String JWT_CLAIM_ROLE = "role";

    public static final String JWT_CLAIM_STATUS = "status";

    public static final String JWT_CLAIM_PERMISSIONS = "permissions";

    // =========================================================================
    // PASSWORD POLICY
    // =========================================================================

    public static final int MIN_PASSWORD_LENGTH = 8;

    public static final int MAX_PASSWORD_LENGTH = 100;

    public static final int PASSWORD_HISTORY_LIMIT = 5;

    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    // =========================================================================
    // OTP
    // =========================================================================

    public static final int OTP_LENGTH = 6;

    public static final int OTP_EXPIRY_MINUTES = 10;

    public static final int MAX_OTP_ATTEMPTS = 5;

    // =========================================================================
    // LOGIN
    // =========================================================================

    public static final String LOGIN_SUCCESS = "Login successful.";

    public static final String LOGIN_FAILED = "Invalid username or password.";

    public static final String LOGOUT_SUCCESS = "Logout successful.";

    public static final String ACCOUNT_LOCKED =
            "Your account has been locked. Please contact the administrator.";

    public static final String ACCOUNT_DISABLED =
            "Your account is disabled.";

    public static final String PASSWORD_CHANGED =
            "Password changed successfully.";

    public static final String PASSWORD_RESET =
            "Password reset successfully.";

    public static final String INVALID_REFRESH_TOKEN =
            "Invalid refresh token.";

    public static final String TOKEN_EXPIRED =
            "Authentication token has expired.";

    public static final String TOKEN_INVALID =
            "Authentication token is invalid.";

    // =========================================================================
    // CACHE KEYS
    // =========================================================================

    public static final String AUTH_CACHE = "AUTH";

    public static final String JWT_CACHE = "JWT";

    public static final String REFRESH_TOKEN_CACHE = "REFRESH_TOKEN";

    public static final String OTP_CACHE = "OTP";

}