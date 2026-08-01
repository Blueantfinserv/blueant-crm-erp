package com.blueant_crm_erp.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * =============================================================================
 * Login Response
 * =============================================================================
 *
 * Response DTO returned after successful authentication.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Authentication Information
 * • JWT Token Information
 * • Logged-in User Information
 * • Authorization Information
 * • Session Information
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    // =========================================================================
    // User Information
    // =========================================================================

    /**
     * User Identifier.
     */
    private Long userId;

    /**
     * Employee Code.
     */
    private String employeeCode;

    /**
     * Full Name.
     */
    private String fullName;

    /**
     * Official Email.
     */
    private String email;

    /**
     * Mobile Number.
     */
    private String mobileNumber;

    /**
     * Profile Image URL.
     */
    private String profileImage;

    // =========================================================================
    // Organization Information
    // =========================================================================

    /**
     * Role Name.
     */
    private String role;

    /**
     * Department Name.
     */
    private String department;

    /**
     * Designation Name.
     */
    private String designation;

    /**
     * Team Name.
     */
    private String team;

    /**
     * Reporting Manager.
     */
    private String reportingManager;

    // =========================================================================
    // Authorization
    // =========================================================================

    /**
     * Granted Permissions.
     */
    private Set<String> permissions;

    // =========================================================================
    // JWT Information
    // =========================================================================

    /**
     * Access Token.
     */
    private String accessToken;

    /**
     * Refresh Token.
     */
    private String refreshToken;

    /**
     * Token Type.
     *
     * Example:
     * Bearer
     */
    private String tokenType;

    /**
     * Access Token Expiry Time (Epoch Seconds).
     */
    private Long expiresIn;

    /**
     * Refresh Token Expiry Time.
     */
    private LocalDateTime refreshTokenExpiry;

    // =========================================================================
    // Account Information
    // =========================================================================

    /**
     * User Status.
     */
    private String status;

    /**
     * Indicates whether this is the user's first login.
     */
    private Boolean firstLogin;

    /**
     * Indicates whether password has expired.
     */
    private Boolean passwordExpired;

    /**
     * Indicates whether account is locked.
     */
    private Boolean accountLocked;

    /**
     * Indicates whether account is enabled.
     */
    private Boolean enabled;

    // =========================================================================
    // Session Information
    // =========================================================================

    /**
     * Login Time.
     */
    private LocalDateTime loginAt;

    /**
     * Session Identifier.
     */
    private String sessionId;

}