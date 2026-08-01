package com.blueant_crm_erp.util.network;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * IP Utility.
 *
 * Utility class for IP address related operations.
 *
 * Responsibilities:
 * - Extract client IP
 * - Validate IPv4
 * - Validate IPv6
 * - Detect localhost
 * - Detect private IP
 *
 * This utility DOES NOT:
 * - Perform Geo Location lookup
 * - Block IP addresses
 * - Rate limiting
 * - Security validation
 *
 * Used By:
 * - Authentication Module
 * - Audit Module
 * - Security Module
 * - Login History
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class IpUtil {

    /**
     * X-Forwarded-For header.
     */
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * Proxy Client IP header.
     */
    private static final String PROXY_CLIENT_IP = "Proxy-Client-IP";

    /**
     * WL Proxy header.
     */
    private static final String WL_PROXY_CLIENT_IP =
            "WL-Proxy-Client-IP";

    /**
     * IPv4 Regex.
     */
    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}" +
                            "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
            );

    private IpUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns client IP.
     */
    public static String getClientIp(HttpServletRequest request) {

        Objects.requireNonNull(request);

        String ip = request.getHeader(X_FORWARDED_FOR);

        if (isUnknown(ip)) {
            ip = request.getHeader(PROXY_CLIENT_IP);
        }

        if (isUnknown(ip)) {
            ip = request.getHeader(WL_PROXY_CLIENT_IP);
        }

        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * Returns true if IPv4.
     */
    public static boolean isIpv4(String ip) {

        if (ip == null || ip.isBlank()) {
            return false;
        }

        return IPV4_PATTERN.matcher(ip).matches();
    }

    /**
     * Returns true if IPv6.
     */
    public static boolean isIpv6(String ip) {

        if (ip == null || ip.isBlank()) {
            return false;
        }

        try {
            return InetAddress.getByName(ip)
                    .getHostAddress()
                    .contains(":");
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    /**
     * Returns true if localhost.
     */
    public static boolean isLocalhost(String ip) {

        return "127.0.0.1".equals(ip)
                || "::1".equals(ip)
                || "localhost".equalsIgnoreCase(ip);
    }

    /**
     * Returns true if private IP.
     */
    public static boolean isPrivateIp(String ip) {

        if (!isIpv4(ip)) {
            return false;
        }

        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*");
    }

    /**
     * Returns true if valid IP.
     */
    public static boolean isValidIp(String ip) {

        return isIpv4(ip) || isIpv6(ip);
    }

    /**
     * Checks unknown header value.
     */
    private static boolean isUnknown(String value) {

        return value == null
                || value.isBlank()
                || "unknown".equalsIgnoreCase(value);
    }

}