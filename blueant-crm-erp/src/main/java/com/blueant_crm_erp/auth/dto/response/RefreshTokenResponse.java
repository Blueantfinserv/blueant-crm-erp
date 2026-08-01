package com.blueant_crm_erp.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * =============================================================================
 * Refresh Token Response
 * =============================================================================
 *
 * Response DTO returned after successful Refresh Token validation.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • New Access Token
 * • Rotated Refresh Token
 * • Token Expiration Information
 * • Session Information
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
public class RefreshTokenResponse {

    /**
     * JWT Access Token.
     */
    private String accessToken;

    /**
     * Newly Generated Refresh Token.
     *
     * Returned only when Refresh Token Rotation
     * is enabled.
     */
    private String refreshToken;

    /**
     * Token Type.
     *
     * Example:
     * Bearer
     */
    private String tokenType;

    /**
     * Access Token Expiry Time (Seconds).
     */
    private Long expiresIn;

    /**
     * Refresh Token Expiry Date.
     */
    private LocalDateTime refreshTokenExpiry;

    /**
     * Session Identifier.
     */
    private String sessionId;

}