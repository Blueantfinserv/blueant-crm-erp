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
 * Refresh Token Request
 * =============================================================================
 *
 * Request DTO used to generate a new Access Token
 * using a valid Refresh Token.
 *
 * Business Rules
 * -----------------------------------------------------------------------------
 * • Refresh Token must be valid.
 * • Refresh Token must not be revoked.
 * • Refresh Token must not be expired.
 * • Device information is validated for security.
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
public class RefreshTokenRequest {

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
     * Device Identifier.
     *
     * Used to validate that the refresh token
     * belongs to the requesting device.
     */
    @Size(
            max = 255,
            message = "Device ID must not exceed 255 characters."
    )
    private String deviceId;

}