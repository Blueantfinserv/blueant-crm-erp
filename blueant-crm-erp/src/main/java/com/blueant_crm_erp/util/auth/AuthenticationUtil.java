package com.blueant_crm_erp.util.auth;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Authentication Utility
 *
 * Provides helper methods for accessing the
 * currently authenticated user from Spring Security.
 *
 * This class DOES NOT perform authentication.
 * Authentication is handled by Spring Security.
 *
 * Responsibilities:
 * - Get current Authentication
 * - Get current username
 * - Check authentication status
 * - Check anonymous user
 * - Clear security context
 *
 * Used By:
 * - Controllers
 * - Services
 * - Audit Module
 * - JWT Filter
 * - Authorization Layer
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class AuthenticationUtil {

    private AuthenticationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns current Authentication object.
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }

    /**
     * Returns true if user is authenticated.
     */
    public static boolean isAuthenticated() {

        Authentication authentication = getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Returns current username.
     *
     * Returns "ANONYMOUS" if user is not authenticated.
     */
    public static String getCurrentUsername() {

        if (!isAuthenticated()) {
            return "ANONYMOUS";
        }

        return getAuthentication().getName();
    }

    /**
     * Returns current principal.
     */
    public static Object getPrincipal() {

        if (!isAuthenticated()) {
            return null;
        }

        return getAuthentication().getPrincipal();
    }

    /**
     * Returns current authenticated user safely.
     */
    public static Optional<Object> getCurrentUser() {

        if (!isAuthenticated()) {
            return Optional.empty();
        }

        return Optional.ofNullable(getAuthentication().getPrincipal());
    }

    /**
     * Returns true if current user has specified role.
     *
     * Example:
     * hasRole("ADMIN")
     */
    public static boolean hasRole(String role) {

        if (!isAuthenticated()) {
            return false;
        }

        return getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Clears Security Context.
     *
     * Used during logout.
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

}