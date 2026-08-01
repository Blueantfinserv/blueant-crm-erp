package com.blueant_crm_erp.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT Configuration Properties.
 *
 * Loads JWT related configuration from application.yml.
 *
 * Example:
 *
 * jwt:
 *   secret: ${JWT_SECRET}
 *   expiration-ms: 86400000
 *   refresh-expiration-ms: 604800000
 *   issuer: BlueAnt CRM ERP
 *   audience:
 *     - WEB
 *     - ADMIN
 *   header: Authorization
 *   prefix: Bearer
 *
 * Used By:
 * - JwtTokenFactory
 * - JwtUtil
 * - JwtAuthenticationFilter
 * - SecurityConfig
 * - AuthService
 *
 * Production Notes:
 * - Never hardcode JWT secrets.
 * - Store secrets in Environment Variables, Vault or Docker Secrets.
 * - Use minimum 256-bit secret for HS256.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key used for signing JWT.
     */
    @NotBlank(message = "JWT secret must not be blank.")
    @Size(min = 32,
            message = "JWT secret must contain at least 32 characters.")
    private String secret;

    /**
     * Access token expiration (milliseconds).
     */
    @Min(value = 60000,
            message = "Access token expiration must be at least 1 minute.")
    private long expirationMs;

    /**
     * Refresh token expiration (milliseconds).
     */
    @Min(value = 60000,
            message = "Refresh token expiration must be at least 1 minute.")
    private long refreshExpirationMs;

    /**
     * Token issuer.
     */
    @NotBlank(message = "JWT issuer must not be blank.")
    private String issuer = "BlueAnt CRM ERP";

    /**
     * Authorization header.
     */
    @NotBlank(message = "JWT header must not be blank.")
    private String header = "Authorization";

    /**
     * Header prefix.
     */
    @NotBlank(message = "JWT prefix must not be blank.")
    private String prefix = "Bearer";

}