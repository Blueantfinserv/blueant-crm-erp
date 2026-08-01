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
 * Current User Response
 * =============================================================================
 *
 * Response DTO representing the currently authenticated user.
 *
 * This DTO is returned after successful authentication
 * and from the "/auth/me" endpoint.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • User Identity
 * • User Profile
 * • Role Information
 * • Permission Information
 * • Organization Information
 * • Account Status
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
public class CurrentUserResponse {

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
     * Profile Photo URL.
     */
    private String profileImage;

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
     * Reporting Manager Name.
     */
    private String reportingManager;

    /**
     * Primary Role.
     */
    private String role;

    /**
     * Granted Permissions.
     */
    private Set<String> permissions;

    /**
     * Account Status.
     */
    private String status;

    /**
     * Last Successful Login.
     */
    private LocalDateTime lastLoginAt;

    /**
     * Password Expiry Date.
     */
    private LocalDateTime passwordExpiryDate;

    /**
     * First Login Flag.
     */
    private Boolean firstLogin;

    /**
     * Account Locked Flag.
     */
    private Boolean accountLocked;

    /**
     * Account Enabled Flag.
     */
    private Boolean enabled;

}