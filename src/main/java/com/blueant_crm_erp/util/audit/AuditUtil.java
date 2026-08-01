package com.blueant_crm_erp.util.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for audit related helper methods.
 *
 * This class provides reusable helper methods for:
 *
 * - Audit Timestamp
 * - Trace Id
 * - Current Username
 * - Client IP Address
 * - User Agent
 * - Request URI
 * - HTTP Method
 *
 * Business Modules:
 * - Authentication
 * - User
 * - Role
 * - Lead
 * - CRM
 * - Client
 * - Service Request
 * - Transaction
 * - Incentive
 *
 * NOTE:
 * This class DOES NOT save audit logs.
 * Audit persistence should be handled by AuditService.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class AuditUtil {

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns current audit timestamp.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(INDIA_ZONE);
    }

    /**
     * Returns formatted audit timestamp.
     */
    public static String currentTimestamp() {
        return now().format(DATE_TIME_FORMATTER);
    }

    /**
     * Generates unique trace id.
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns currently authenticated username.
     */
    public static String currentUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "ANONYMOUS";
        }

        return authentication.getName();
    }

    /**
     * Returns current request URI.
     */
    public static String requestUri(HttpServletRequest request) {

        return request == null
                ? ""
                : request.getRequestURI();
    }

    /**
     * Returns HTTP Method.
     */
    public static String httpMethod(HttpServletRequest request) {

        return request == null
                ? ""
                : request.getMethod();
    }

    /**
     * Returns User-Agent.
     */
    public static String userAgent(HttpServletRequest request) {

        return request == null
                ? ""
                : Optional.ofNullable(request.getHeader("User-Agent"))
                .orElse("UNKNOWN");
    }

    /**
     * Returns Client IP Address.
     */
    public static String clientIp(HttpServletRequest request) {

        if (request == null) {
            return "UNKNOWN";
        }

        String forwarded =
                request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    /**
     * Returns request id if available.
     */
    public static String requestId(HttpServletRequest request) {

        if (request == null) {
            return generateTraceId();
        }

        return Optional.ofNullable(
                        request.getHeader("X-Request-Id"))
                .orElse(generateTraceId());
    }

}