package com.blueant_crm_erp.auth.event;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * Logout Event
 * =============================================================================
 *
 * Published after a successful user logout.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Audit Logging
 * • Login History
 * • Security Monitoring
 * • Session Tracking
 * • Cache Eviction
 * • Notification Trigger
 *
 * This event contains only logout information.
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
public class LogoutEvent implements Serializable {

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
     * Logout Time.
     */
    private final LocalDateTime logoutTime;

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
     * Refresh Token.
     */
    private final String refreshToken;

    /**
     * Logout Triggered By.
     *
     * Examples:
     * SELF
     * ADMIN
     * SYSTEM
     * SESSION_EXPIRED
     */
    private final String logoutBy;

}