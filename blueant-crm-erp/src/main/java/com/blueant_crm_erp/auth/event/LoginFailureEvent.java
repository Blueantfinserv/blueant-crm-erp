package com.blueant_crm_erp.auth.event;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * Login Failure Event
 * =============================================================================
 *
 * Published whenever a user authentication attempt fails.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Audit Logging
 * • Security Monitoring
 * • Failed Login Tracking
 * • Account Lock Verification
 * • Alert Generation
 *
 * This event contains only login failure information.
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
public class LoginFailureEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Username or Email used for login.
     */
    private final String username;

    /**
     * Failure reason.
     *
     * Examples:
     * INVALID_PASSWORD
     * USER_NOT_FOUND
     * ACCOUNT_LOCKED
     * ACCOUNT_DISABLED
     * PASSWORD_EXPIRED
     */
    private final String reason;

    /**
     * Login attempt time.
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

}