package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

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

/**
 * =============================================================================
 * Reset Password Request
 * =============================================================================
 *
 * Request DTO used to reset an existing user's password.
 *
 * NOTE:
 * This API is intended for:
 * • Super Admin
 * • Admin
 * • HR
 *
 * End users should use ChangePasswordRequest
 * to change their own password.
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
public class ResetPasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * New Password.
     */
    @NotBlank(message = "New password is required.")
    @Size(
            min = 8,
            max = 100,
            message = "Password must be between 8 and 100 characters."
    )
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,100}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character."
    )
    @Schema(description = "New Password", example = "Example New Password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    /**
     * Confirm Password.
     */
    @NotBlank(message = "Confirm password is required.")
    @Schema(description = "Confirm Password", example = "Example Confirm Password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    /**
     * Remarks for password reset.
     */
    @Size(
            max = 500,
            message = "Remarks must not exceed 500 characters."
    )
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

}