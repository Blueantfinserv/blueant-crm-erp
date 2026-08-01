package com.blueant_crm_erp.auth.event;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * Refresh Token Created Event
 * =============================================================================
 *
 * Published whenever a new refresh token is generated
 * and persisted successfully.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Audit Logging
 * • Security Monitoring
 * • Login History
 * • Device Tracking
 * • Active Session Tracking
 *
 * This event contains only refresh token creation information.
 * No business logic should be implemented here.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@Builder
public class RefreshTokenCreatedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User Identifier.
     */
    private final Long userId;

    /**
     * Employee Code.
     */
    private final String employeeCode;

    /**
     * User Email.
     */
    private final String email;

    /**
     * Refresh Token.
     */
    private final String refreshToken;

    /**
     * Refresh Token Expiry Date.
     */
    private final LocalDateTime expiryDate;

    /**
     * Token Created Time.
     */
    private final LocalDateTime createdAt;

    /**
     * Client IP Address.
     */
    private final String ipAddress;

    /**
     * Device Name.
     */
    private final String deviceName;

    /**
     * Browser Name.
     */
    private final String browser;

    /**
     * Operating System.
     */
    private final String operatingSystem;

}