package com.blueant_crm_erp.auth.event;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * Password Reset Event
 * =============================================================================
 *
 * Published after a user successfully resets their password
 * through the Forgot Password flow.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Audit Logging
 * • Security Monitoring
 * • Logout All Active Sessions
 * • Refresh Token Revocation
 * • Email Notification
 * • SMS Notification
 * • Activity Timeline
 *
 * This event contains only password reset information.
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
public class PasswordResetEvent implements Serializable {

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
     * Password reset timestamp.
     */
    private final LocalDateTime resetAt;

    /**
     * Reset Trigger.
     *
     * Examples:
     * FORGOT_PASSWORD
     * ADMIN_RESET
     * SYSTEM_RESET
     */
    private final String resetBy;

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