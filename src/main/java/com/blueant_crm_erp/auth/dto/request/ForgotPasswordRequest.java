package com.blueant_crm_erp.auth.dto.request;

import jakarta.validation.constraints.Email;
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
 * Forgot Password Request
 * =============================================================================
 *
 * Request DTO used to initiate the Forgot Password process.
 *
 * Business Flow
 * -----------------------------------------------------------------------------
 * • Verify registered user
 * • Generate OTP
 * • Send OTP via Email/SMS
 * • Continue with Reset Password flow
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
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
public class ForgotPasswordRequest {

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
     * Registered Email Address.
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    @Size(
            max = 150,
            message = "Email must not exceed 150 characters."
    )
    private String email;

    /**
     * Registered Mobile Number.
     */
    @NotBlank(message = "Mobile number is required.")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Please enter a valid 10-digit mobile number."
    )
    private String mobileNumber;

}