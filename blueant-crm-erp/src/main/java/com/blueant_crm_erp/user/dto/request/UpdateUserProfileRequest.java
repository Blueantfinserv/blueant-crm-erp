package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * =============================================================================
 * Update User Profile Request
 * =============================================================================
 *
 * Request DTO used for updating the logged-in user's profile.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Update Personal Information
 * • Update Contact Information
 * • Update Profile Image
 *
 * NOTE
 * -----------------------------------------------------------------------------
 * This DTO does NOT allow updating:
 * • Role
 * • Department
 * • Designation
 * • Team
 * • Reporting Manager
 * • Status
 *
 * These fields can only be updated by administrators.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
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
public class UpdateUserProfileRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * First Name.
     */
    @NotBlank(message = "First name is required.")
    @Size(max = 100, message = "First name must not exceed 100 characters.")
    @Schema(description = "First Name", example = "Example First Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    /**
     * Last Name.
     */
    @Size(max = 100, message = "Last name must not exceed 100 characters.")
    @Schema(description = "Last Name", example = "Example Last Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    /**
     * Email Address.
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email address.")
    @Size(max = 150, message = "Email must not exceed 150 characters.")
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
     * Gender.
     */
    @Schema(description = "Gender", example = "Example Gender", requiredMode = Schema.RequiredMode.REQUIRED)
    private Gender gender;

    /**
     * Date of Birth.
     */
    @Schema(description = "Date Of Birth", example = "Example Date Of Birth", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dateOfBirth;

    /**
     * Profile Image URL.
     */
    @Size(max = 1000, message = "Profile image URL must not exceed 1000 characters.")
    @Schema(description = "Profile Image", example = "Example Profile Image", requiredMode = Schema.RequiredMode.REQUIRED)
    private String profileImage;

    /**
     * Remarks.
     */
    @Size(max = 500, message = "Remarks must not exceed 500 characters.")
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

}