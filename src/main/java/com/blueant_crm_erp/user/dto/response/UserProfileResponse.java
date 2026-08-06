package com.blueant_crm_erp.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * User Profile Response
 * =============================================================================
 *
 * Response DTO representing the authenticated user's profile.
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • My Profile API
 * • Dashboard API
 * • Logged-in User Details
 * • Account Settings
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Business Head (Rohit)
 *        ↓
 * Sales Manager
 *        ↓
 * Team Leader
 *        ↓
 * Sales Person
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User Id.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Employee Code.
     */
    @Schema(description = "Employee Code", example = "Example Employee Code")
    private String employeeCode;

    /**
     * First Name.
     */
    @Schema(description = "First Name", example = "Example First Name")
    private String firstName;

    /**
     * Last Name.
     */
    @Schema(description = "Last Name", example = "Example Last Name")
    private String lastName;

    /**
     * Full Name.
     */
    @Schema(description = "Full Name", example = "Example Full Name")
    private String fullName;

    /**
     * Official Email.
     */
    @Schema(description = "Email", example = "Example Email")
    private String email;

    /**
     * Mobile Number.
     */
    @Schema(description = "Mobile Number", example = "Example Mobile Number")
    private String mobileNumber;

    /**
     * Gender.
     */
    @Schema(description = "Gender", example = "MALE")
    private Gender gender;

    /**
     * Profile Image URL.
     */
    @Schema(description = "Profile Image", example = "Example Profile Image")
    private String profileImage;

    /**
     * Department Name.
     */
    @Schema(description = "Department Name", example = "Example Department Name")
    private String departmentName;

    /**
     * Designation Name.
     */
    @Schema(description = "Designation Name", example = "Example Designation Name")
    private String designationName;

    /**
     * Team Name.
     */
    @Schema(description = "Team Name", example = "Example Team Name")
    private String teamName;

    /**
     * Role Name.
     */
    @Schema(description = "Role Name", example = "Example Role Name")
    private String roleName;

    /**
     * Reporting Manager Name.
     */
    @Schema(description = "Reporting Manager Name", example = "Example Reporting Manager Name")
    private String reportingManagerName;

    /**
     * User Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

    /**
     * Account Enabled.
     */
    @Schema(description = "Enabled", example = "Example Enabled")
    private Boolean enabled;

    /**
     * Account Locked.
     */
    @Schema(description = "Account Locked", example = "Example Account Locked")
    private Boolean accountLocked;

    /**
     * Last Login Date & Time.
     */
    @Schema(description = "Last Login At", example = "Example Last Login At")
    private LocalDateTime lastLoginAt;

    /**
     * Account Creation Date & Time.
     */
    @Schema(description = "Created At", example = "Example Created At")
    private LocalDateTime createdAt;

    /**
     * Last Profile Update Date & Time.
     */
    @Schema(description = "Updated At", example = "Example Updated At")
    private LocalDateTime updatedAt;

}