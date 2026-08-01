package com.blueant_crm_erp.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * =============================================================================
 * Reset Password Request
 * =============================================================================
 *
 * Request DTO used to reset a user's password after
 * successful OTP verification.
 *
 * Business Rules
 * -----------------------------------------------------------------------------
 * • Employee must exist.
 * • OTP must be verified.
 * • New password must satisfy password policy.
 * • New password and confirm password must match.
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
public class ResetPasswordRequest {

    /**
     * Employee Code.
     */
    @NotBlank(message = "Employee code is required.")
    @Size(
            min = 3,
            max = 30,
            message = "Employee code must be between 3 and 30 characters."
    )
    private String employeeCode;

    /**
     * One Time Password.
     */
    @NotBlank(message = "OTP is required.")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "OTP must be a valid 6-digit number."
    )
    private String otp;

    /**
     * New Password.
     */
    @NotBlank(message = "New password is required.")
    @Size(
            min = 8,
            max = 100,
            message = "New password must be between 8 and 100 characters."
    )
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&^#()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,100}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character."
    )
    private String newPassword;

    /**
     * Confirm Password.
     */
    @NotBlank(message = "Confirm password is required.")
    @Size(
            min = 8,
            max = 100,
            message = "Confirm password must be between 8 and 100 characters."
    )
    private String confirmPassword;

}