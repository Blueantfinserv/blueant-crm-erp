package com.blueant_crm_erp.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

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
 * User Response
 * =============================================================================
 *
 * Complete response DTO representing a User.
 *
 * Used By:
 * -----------------------------------------------------------------------------
 * • Create User API
 * • Update User API
 * • User Details API
 * • Profile API
 * • Admin User Management
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
public class UserResponse implements Serializable {

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
     * Profile Image URL.
     */
    @Schema(description = "Profile Image", example = "Example Profile Image")
    private String profileImage;

    /**
     * Department Id.
     */
    @Schema(description = "Department Id", example = "Example Department Id")
    private Long departmentId;

    /**
     * Department Name.
     */
    @Schema(description = "Department Name", example = "Example Department Name")
    private String departmentName;

    /**
     * Designation Id.
     */
    @Schema(description = "Designation Id", example = "Example Designation Id")
    private Long designationId;

    /**
     * Designation Name.
     */
    @Schema(description = "Designation Name", example = "Example Designation Name")
    private String designationName;

    /**
     * Team Id.
     */
    @Schema(description = "Team Id", example = "Example Team Id")
    private Long teamId;

    /**
     * Team Name.
     */
    @Schema(description = "Team Name", example = "Example Team Name")
    private String teamName;

    /**
     * Role Id.
     */
    @Schema(description = "Role Id", example = "Example Role Id")
    private Long roleId;

    /**
     * Role Name.
     */
    @Schema(description = "Role Name", example = "Example Role Name")
    private String roleName;

    /**
     * Reporting Manager Id.
     */
    @Schema(description = "Reporting Manager Id", example = "Example Reporting Manager Id")
    private Long reportingManagerId;

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
     * Remarks.
     */
    @Schema(description = "Remarks", example = "Example Remarks")
    private String remarks;

    /**
     * Created By.
     */
    @Schema(description = "Created By", example = "Example Created By")
    private String createdBy;

    /**
     * Created Date & Time.
     */
    @Schema(description = "Created At", example = "Example Created At")
    private LocalDateTime createdAt;

    /**
     * Updated By.
     */
    @Schema(description = "Updated By", example = "Example Updated By")
    private String updatedBy;

    /**
     * Updated Date & Time.
     */
    @Schema(description = "Updated At", example = "Example Updated At")
    private LocalDateTime updatedAt;

}