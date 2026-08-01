package com.blueant_crm_erp.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * =============================================================================
 * User Session Response
 * =============================================================================
 *
 * Represents an authenticated user session.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Active Session Information
 * • Device Information
 * • Login Information
 * • Session Expiration
 * • Security Monitoring
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • GET /auth/session
 * • Active Sessions
 * • Login History
 * • Security Dashboard
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
public class UserSessionResponse {

    /**
     * Session Identifier.
     */
    private String sessionId;

    /**
     * User Identifier.
     */
    private Long userId;

    /**
     * Employee Code.
     */
    private String employeeCode;

    /**
     * Login Time.
     */
    private LocalDateTime loginAt;

    /**
     * Last Activity Time.
     */
    private LocalDateTime lastActivityAt;

    /**
     * Session Expiry Time.
     */
    private LocalDateTime expiresAt;

    /**
     * Device Identifier.
     */
    private String deviceId;

    /**
     * Device Name.
     */
    private String deviceName;

    /**
     * Device Type.
     *
     * Example:
     * MOBILE
     * DESKTOP
     * TABLET
     * LAPTOP
     */
    private String deviceType;

    /**
     * Browser Name.
     */
    private String browser;

    /**
     * Operating System.
     */
    private String operatingSystem;

    /**
     * Client IP Address.
     */
    private String ipAddress;

    /**
     * Client Location.
     */
    private String location;

    /**
     * Indicates whether the current session
     * belongs to the logged-in device.
     */
    private Boolean currentSession;

    /**
     * Session Status.
     *
     * Example:
     * ACTIVE
     * EXPIRED
     * REVOKED
     */
    private String sessionStatus;

}