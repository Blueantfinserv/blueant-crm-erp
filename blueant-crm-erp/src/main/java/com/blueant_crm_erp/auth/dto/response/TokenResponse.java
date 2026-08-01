package com.blueant_crm_erp.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * =============================================================================
 * Token Response
 * =============================================================================
 *
 * Represents authentication token information returned
 * after successful authentication or token refresh.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • JWT Access Token
 * • Refresh Token
 * • Token Metadata
 * • Session Information
 *
 * This DTO is reusable across authentication APIs.
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
public class TokenResponse {

    /**
     * JWT Access Token.
     */
    private String accessToken;

    /**
     * Refresh Token.
     */
    private String refreshToken;

    /**
     * Token Type.
     *
     * Example:
     * Bearer
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access Token Expiry Time (in seconds).
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

    /**
     * Token Issued Time.
     */
    private LocalDateTime issuedAt;

    /**
     * Access Token Expiry Date.
     */
    private LocalDateTime accessTokenExpiry;

}