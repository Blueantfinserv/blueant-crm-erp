package com.blueant_crm_erp.auth.event;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * Password Changed Event
 * =============================================================================
 *
 * Published after a user successfully changes their password.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Audit Logging
 * • Security Monitoring
 * • Logout Active Sessions
 * • Refresh Token Revocation
 * • Notification Trigger
 * • Password History Tracking
 *
 * This event contains only password change information.
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
public class PasswordChangedEvent implements Serializable {

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
     * Password changed timestamp.
     */
    private final LocalDateTime changedAt;

    /**
     * Changed By.
     *
     * Examples:
     * SELF
     * ADMIN
     * SYSTEM
     */
    private final String changedBy;

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