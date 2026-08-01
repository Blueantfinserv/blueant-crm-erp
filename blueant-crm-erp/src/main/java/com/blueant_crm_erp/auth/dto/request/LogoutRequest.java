package com.blueant_crm_erp.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * =============================================================================
 * Logout Request
 * =============================================================================
 *
 * Request DTO used to logout an authenticated user.
 *
 * Business Rules
 * -----------------------------------------------------------------------------
 * • User must be authenticated.
 * • Refresh Token is mandatory.
 * • Supports single device logout.
 * • Supports logout from all devices.
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
public class LogoutRequest {

    /**
     * Refresh Token.
     */
    @NotBlank(message = "Refresh token is required.")
    @Size(
            min = 20,
            max = 1000,
            message = "Invalid refresh token."
    )
    private String refreshToken;

    /**
     * Logout from all active devices.
     */
    @Builder.Default
    private Boolean logoutFromAllDevices = Boolean.FALSE;

    /**
     * Device Identifier.
     */
    @Size(
            max = 255,
            message = "Device ID must not exceed 255 characters."
    )
    private String deviceId;

}