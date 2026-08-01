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
 * Verify Login OTP Request
 * =============================================================================
 *
 * Request DTO used to verify the One-Time Password (OTP)
 * generated during the Login authentication process.
 *
 * Business Flow
 * -----------------------------------------------------------------------------
 * LoginRequest
 *      ↓
 * Validate Credentials
 *      ↓
 * Generate Login OTP
 *      ↓
 * Send OTP
 *      ↓
 * VerifyLoginOtpRequest
 *      ↓
 * Generate JWT Tokens
 *
 * Business Rules
 * -----------------------------------------------------------------------------
 * • Login verification token must be valid.
 * • OTP must be valid.
 * • OTP must not be expired.
 * • OTP must not exceed maximum retry attempts.
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
public class VerifyLoginOtpRequest {

    /**
     * Login Verification Token.
     *
     * Generated after successful username/password validation.
     */
    @NotBlank(message = "Verification token is required.")
    @Size(
            min = 20,
            max = 500,
            message = "Invalid verification token."
    )
    private String verificationToken;

    /**
     * One-Time Password.
     */
    @NotBlank(message = "OTP is required.")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "OTP must be a valid 6-digit number."
    )
    private String otp;

}