package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Update User Request
 * =============================================================================
 *
 * Request DTO used to update an existing user.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Rohit
 *      ↓
 * Sales Manager
 *      ↓
 * Team Leader
 *      ↓
 * Sales Person
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
public class UpdateUserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * First Name.
     */
    @NotBlank(message = "First name is required.")
    @Size(min = 2, max = 100,
            message = "First name must be between 2 and 100 characters.")
    @Schema(description = "First Name", example = "Example First Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    /**
     * Last Name.
     */
    @Size(max = 100,
            message = "Last name cannot exceed 100 characters.")
    @Schema(description = "Last Name", example = "Example Last Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    /**
     * Official Email.
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    @Size(max = 150)
    @Schema(description = "Email", example = "Example Email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * Mobile Number.
     */
    @NotBlank(message = "Mobile number is required.")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid mobile number."
    )
    @Schema(description = "Mobile Number", example = "Example Mobile Number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mobileNumber;

    /**
     * Department Id.
     */
    @NotNull(message = "Department is required.")
    @Schema(description = "Department Id", example = "Example Department Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long departmentId;

    /**
     * Designation Id.
     */
    @NotNull(message = "Designation is required.")
    @Schema(description = "Designation Id", example = "Example Designation Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long designationId;

    /**
     * Team Id.
     */
    @NotNull(message = "Team is required.")
    @Schema(description = "Team Id", example = "Example Team Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long teamId;

    /**
     * Role Id.
     */
    @NotNull(message = "Role is required.")
    @Schema(description = "Role Id", example = "Example Role Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;

    /**
     * Reporting Manager Id.
     */
    @Schema(description = "Reporting Manager Id", example = "Example Reporting Manager Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reportingManagerId;

    /**
     * Employee Status.
     */
    @NotNull(message = "Status is required.")
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Employee Remarks.
     */
    @Size(max = 500,
            message = "Remarks cannot exceed 500 characters.")
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

}