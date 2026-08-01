package com.blueant_crm_erp.util.cache;

import com.blueant_crm_erp.common.constants.CacheConstants;

import java.util.Objects;

/**
 * Utility class for generating standardized cache keys
 * used throughout the BlueAnt CRM ERP Platform.
 *
 * Responsibilities:
 * - Generate cache keys
 * - Maintain consistent cache naming
 * - Prevent duplicate key generation logic
 *
 * This class does NOT:
 * - Read cache
 * - Write cache
 * - Evict cache
 *
 * Cache operations should be handled by Spring CacheManager
 * or a dedicated CacheService.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class CacheKeyUtil {

    /**
     * Cache key separator.
     */
    private static final String SEPARATOR = "::";

    private CacheKeyUtil() {
        throw new IllegalStateException("Utility class");
    }

    /* ==========================================================
     * USER
     * ==========================================================
     */

    public static String user(Long userId) {
        return build(CacheConstants.USER_CACHE, userId);
    }

    public static String employee(String employeeCode) {
        return build(CacheConstants.USER_CACHE, employeeCode);
    }

    /* ==========================================================
     * ROLE
     * ==========================================================
     */

    public static String role(Long roleId) {
        return build(CacheConstants.ROLE_CACHE, roleId);
    }

    /* ==========================================================
     * AUTHENTICATION
     * ==========================================================
     */

    public static String jwt(String username) {
        return build(CacheConstants.JWT_CACHE, username);
    }

    public static String refreshToken(String username) {
        return build(CacheConstants.REFRESH_TOKEN_CACHE, username);
    }

    public static String otp(String mobileNumber) {
        return build(CacheConstants.OTP_CACHE, mobileNumber);
    }

    /* ==========================================================
     * LEAD
     * ==========================================================
     */

    public static String lead(Long leadId) {
        return build(CacheConstants.LEAD_CACHE, leadId);
    }

    public static String leadCode(String leadCode) {
        return build(CacheConstants.LEAD_CACHE, leadCode);
    }

    /* ==========================================================
     * CLIENT
     * ==========================================================
     */

    public static String client(Long clientId) {
        return build(CacheConstants.CLIENT_CACHE, clientId);
    }

    /* ==========================================================
     * MEETING
     * ==========================================================
     */

    public static String meeting(Long meetingId) {
        return build(CacheConstants.MEETING_CACHE, meetingId);
    }

    /* ==========================================================
     * SERVICE REQUEST
     * ==========================================================
     */

    public static String serviceRequest(Long requestId) {
        return build(CacheConstants.SERVICE_REQUEST_CACHE, requestId);
    }

    /* ==========================================================
     * TRANSACTION
     * ==========================================================
     */

    public static String transaction(Long transactionId) {
        return build(CacheConstants.TRANSACTION_CACHE, transactionId);
    }

    /* ==========================================================
     * DASHBOARD
     * ==========================================================
     */

    public static String dashboard(String username) {
        return build(CacheConstants.DASHBOARD_CACHE, username);
    }

    /* ==========================================================
     * GENERIC
     * ==========================================================
     */

    public static String custom(String cacheName, Object key) {
        return build(cacheName, key);
    }

    /**
     * Builds a standardized cache key.
     *
     * Example:
     * USER::15
     * LEAD::1001
     * ROLE::3
     */
    private static String build(String cacheName, Object key) {

        Objects.requireNonNull(cacheName, "Cache name must not be null.");
        Objects.requireNonNull(key, "Cache key must not be null.");

        return cacheName + SEPARATOR + key;
    }

}