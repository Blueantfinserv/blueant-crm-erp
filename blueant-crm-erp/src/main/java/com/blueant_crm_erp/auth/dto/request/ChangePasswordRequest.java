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
 * Change Password Request
 * =============================================================================
 *
 * Request DTO used by an authenticated user to change
 * their account password.
 *
 * Business Rules
 * -----------------------------------------------------------------------------
 * • User must be authenticated.
 * • Old password must match the current password.
 * • New password must satisfy password policy.
 * • New password must not be the same as the old password.
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
public class ChangePasswordRequest {

    /**
     * Current account password.
     */
    @NotBlank(message = "Current password is required.")
    @Size(min = 8, max = 100,
            message = "Current password must be between 8 and 100 characters.")
    private String currentPassword;

    /**
     * New account password.
     *
     * Password Policy:
     * • Minimum 8 characters
     * • At least one uppercase letter
     * • At least one lowercase letter
     * • At least one digit
     * • At least one special character
     */
    @NotBlank(message = "New password is required.")
    @Size(min = 8, max = 100,
            message = "New password must be between 8 and 100 characters.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&^#()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,100}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character."
    )
    private String newPassword;

    /**
     * Password confirmation.
     */
    @NotBlank(message = "Confirm password is required.")
    @Size(min = 8, max = 100,
            message = "Confirm password must be between 8 and 100 characters.")
    private String confirmPassword;

}