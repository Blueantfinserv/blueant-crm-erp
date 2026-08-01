package com.blueant_crm_erp.util.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ==============================================================
 * Security Utility
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility methods for accessing Spring Security context.
 *
 * Responsibilities:
 * - Current Authentication
 * - Current Username
 * - Current Roles
 * - Role Checking
 * - Authentication Status
 * - Client IP
 * - User Agent
 *
 * Thread Safe : Yes
 * ==============================================================
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtil {

    /**
     * Returns current authentication.
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }

    /**
     * Returns current username.
     */
    public static String getCurrentUsername() {

        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getName();
    }

    /**
     * Returns current principal.
     */
    public static Object getPrincipal() {

        Authentication authentication = getAuthentication();

        return authentication == null
                ? null
                : authentication.getPrincipal();
    }

    /**
     * Checks whether user is authenticated.
     */
    public static boolean isAuthenticated() {

        Authentication authentication = getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Checks whether current user is anonymous.
     */
    public static boolean isAnonymous() {
        return !isAuthenticated();
    }

    /**
     * Returns all roles.
     */
    public static List<String> getRoles() {

        Authentication authentication = getAuthentication();

        if (authentication == null) {
            return List.of();
        }

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    /**
     * Checks single role.
     */
    public static boolean hasRole(String role) {

        if (role == null || role.isBlank()) {
            return false;
        }

        return getRoles().contains(role);
    }

    /**
     * Checks multiple roles.
     */
    public static boolean hasAnyRole(String... roles) {

        if (roles == null || roles.length == 0) {
            return false;
        }

        List<String> authorities = getRoles();

        for (String role : roles) {

            if (authorities.contains(role)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns authenticated status.
     */
    public static boolean isCurrentUser(String username) {

        return Objects.equals(
                getCurrentUsername(),
                username
        );
    }

    /**
     * Returns client IP.
     */
    public static String getClientIp(HttpServletRequest request) {

        String forwarded =
                request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0];
        }

        return request.getRemoteAddr();
    }

    /**
     * Returns user agent.
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

}