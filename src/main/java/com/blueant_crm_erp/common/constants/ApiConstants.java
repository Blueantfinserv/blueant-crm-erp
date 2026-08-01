package com.blueant_crm_erp.common.constants;

/**
 * API Constants
 *
 * Common API related constants used throughout the application.
 *
 * @author BlueAnt
 * @version 1.0
 */
public final class ApiConstants {

    private ApiConstants() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * ==========================================
     * API BASE PATH
     * ==========================================
     */

    public static final String API_BASE = "/api";

    public static final String API_VERSION_V1 = "/v1";

    public static final String API_V1 = API_BASE + API_VERSION_V1;

    /*
     * ==========================================
     * MODULE ENDPOINTS
     * ==========================================
     */

    public static final String AUTH = "/auth";

    public static final String USERS = "/users";

    public static final String ROLES = "/roles";

    public static final String HIERARCHY = "/hierarchy";

    public static final String MAPPING = "/mapping";

    public static final String LEADS = "/leads";

    public static final String FOLLOWUPS = "/followups";

    public static final String MEETINGS = "/meetings";

    public static final String CLIENTS = "/clients";

    public static final String ONBOARDING = "/onboarding";

    public static final String SERVICE_REQUESTS = "/service-requests";

    public static final String CRM = "/crm";

    public static final String TRANSACTIONS = "/transactions";

    public static final String HELPDESK = "/helpdesk";

    public static final String DASHBOARD = "/dashboard";

    public static final String REPORTS = "/reports";

    public static final String NOTIFICATIONS = "/notifications";

    public static final String HR = "/hr";

    public static final String AUDIT = "/audit";

    /*
     * ==========================================
     * COMMON ENDPOINTS
     * ==========================================
     */

    public static final String LOGIN = "/login";

    public static final String LOGOUT = "/logout";

    public static final String REGISTER = "/register";

    public static final String REFRESH_TOKEN = "/refresh-token";

    public static final String FORGOT_PASSWORD = "/forgot-password";

    public static final String RESET_PASSWORD = "/reset-password";

    public static final String CHANGE_PASSWORD = "/change-password";

    public static final String VERIFY_OTP = "/verify-otp";

    public static final String RESEND_OTP = "/resend-otp";

    /*
     * ==========================================
     * DEFAULT PAGINATION
     * ==========================================
     */

    public static final int DEFAULT_PAGE = 0;

    public static final int DEFAULT_SIZE = 10;

    public static final int MAX_PAGE_SIZE = 100;

    public static final String DEFAULT_SORT_BY = "createdAt";

    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    /*
     * ==========================================
     * CONTENT TYPES
     * ==========================================
     */

    public static final String APPLICATION_JSON = "application/json";

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public static final String APPLICATION_PDF = "application/pdf";

    public static final String APPLICATION_EXCEL =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /*
     * ==========================================
     * HEADER NAMES
     * ==========================================
     */

    public static final String AUTHORIZATION = "Authorization";

    public static final String BEARER = "Bearer ";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String ACCEPT = "Accept";

    public static final String REQUEST_ID = "X-Request-Id";

    public static final String CORRELATION_ID = "X-Correlation-Id";

    /*
     * ==========================================
     * RESPONSE HEADERS
     * ==========================================
     */

    public static final String TOTAL_COUNT = "X-Total-Count";

    public static final String PAGE_NUMBER = "X-Page-Number";

    public static final String PAGE_SIZE = "X-Page-Size";

    /*
     * ==========================================
     * SWAGGER
     * ==========================================
     */

    public static final String SWAGGER_UI = "/swagger-ui/**";

    public static final String API_DOCS = "/v3/api-docs/**";

}