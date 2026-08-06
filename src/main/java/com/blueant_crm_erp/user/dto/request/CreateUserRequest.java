package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Gender;
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
 * Create User Request
 * =============================================================================
 *
 * Request DTO used to create a new User.
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
public class CreateUserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Employee Code.
     * Example:
     * BA000001
     */
    @Size(
            max = 20,
            message = "Employee code must not exceed 20 characters."
    )
    @Schema(description = "Employee Code", example = "Example Employee Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String employeeCode;

    /**
     * First Name.
     */
    @NotBlank(message = "First name is required.")
    @Size(
            min = 2,
            max = 100,
            message = "First name must be between 2 and 100 characters."
    )
    @Schema(description = "First Name", example = "Example First Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    /**
     * Last Name.
     */
    @Size(
            max = 100,
            message = "Last name must not exceed 100 characters."
    )
    @Schema(description = "Last Name", example = "Example Last Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    /**
     * Official Email.
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email address.")
    @Size(
            max = 150,
            message = "Email must not exceed 150 characters."
    )
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
     * Login Password.
     */
    @NotBlank(message = "Password is required.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,100}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character."
    )
    @Schema(description = "Password", example = "Example Password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

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
     * Gender.
     */
    @Schema(description = "Gender", example = "MALE", requiredMode = Schema.RequiredMode.REQUIRED)
    private Gender gender;

    /**
     * User Status.
     */
    @Builder.Default
    private Status status = Status.ACTIVE;

    /**
     * Remarks.
     */
    @Size(
            max = 500,
            message = "Remarks must not exceed 500 characters."
    )
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

}