package com.blueant_crm_erp.auth.event;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * Login Success Event
 * =============================================================================
 *
 * Published after successful user authentication.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Audit Logging
 * • Login History
 * • Security Monitoring
 * • Dashboard Activity
 * • Notification Trigger
 * • Cache Refresh
 *
 * This event contains only authentication information.
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
public class LoginSuccessEvent implements Serializable {

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
     * Login Time.
     */
    private final LocalDateTime loginTime;

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

    /**
     * Access Token.
     */
    private final String accessToken;

    /**
     * Refresh Token.
     */
    private final String refreshToken;

}