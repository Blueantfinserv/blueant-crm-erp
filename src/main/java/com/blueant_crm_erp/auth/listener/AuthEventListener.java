package com.blueant_crm_erp.auth.listener;

import com.blueant_crm_erp.auth.event.LoginFailureEvent;
import com.blueant_crm_erp.auth.event.LoginSuccessEvent;
import com.blueant_crm_erp.auth.event.LogoutEvent;
import com.blueant_crm_erp.auth.event.PasswordChangedEvent;
import com.blueant_crm_erp.auth.event.PasswordResetEvent;
import com.blueant_crm_erp.auth.event.RefreshTokenCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * =============================================================================
 * Authentication Event Listener
 * =============================================================================
 *
 * Handles all Authentication Domain Events.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Login Success
 * • Login Failure
 * • Logout
 * • Password Change
 * • Password Reset
 * • Refresh Token Creation
 *
 * Future Integration
 * -----------------------------------------------------------------------------
 * • Audit Logging
 * • Login History
 * • Security Monitoring
 * • Email Notification
 * • SMS Notification
 * • WhatsApp Notification
 * • Cache Eviction
 * • Dashboard Activity
 * • Active Session Tracking
 *
 * This listener should contain only event handling logic.
 * Business logic must remain inside the Authentication Service.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Slf4j
@Component
public class AuthEventListener {

    /**
     * Handles successful login events.
     */
    @Async
    @EventListener
    public void handleLoginSuccessEvent(LoginSuccessEvent event) {

        log.info(
                "Authentication Success | UserId={} | EmployeeCode={} | Email={} | IP={} | Device={} | Browser={} | LoginTime={}",
                event.getUserId(),
                event.getEmployeeCode(),
                event.getEmail(),
                event.getIpAddress(),
                event.getDeviceName(),
                event.getBrowser(),
                event.getLoginTime()
        );

        // Future integration:
        // Save Login History
        // Publish Audit Log
        // Send Login Notification
        // Update Dashboard
        // Security Monitoring
    }

    /**
     * Handles failed login attempts.
     */
    @Async
    @EventListener
    public void handleLoginFailureEvent(LoginFailureEvent event) {

        log.warn(
                "Authentication Failed | Username={} | Reason={} | IP={} | Device={} | Browser={} | AttemptTime={}",
                event.getUsername(),
                event.getReason(),
                event.getIpAddress(),
                event.getDeviceName(),
                event.getBrowser(),
                event.getLoginTime()
        );

        // Future integration:
        // Increment Failed Login Attempts
        // Trigger Security Alert
        // Publish Audit Log
    }

    /**
     * Handles logout events.
     */
    @Async
    @EventListener
    public void handleLogoutEvent(LogoutEvent event) {

        log.info(
                "Logout Success | UserId={} | EmployeeCode={} | Email={} | LogoutBy={} | LogoutTime={}",
                event.getUserId(),
                event.getEmployeeCode(),
                event.getEmail(),
                event.getLogoutBy(),
                event.getLogoutTime()
        );

        // Future integration:
        // Revoke Refresh Token
        // Remove Active Session
        // Clear Authentication Cache
        // Publish Audit Log
    }

    /**
     * Handles password change events.
     */
    @Async
    @EventListener
    public void handlePasswordChangedEvent(
            PasswordChangedEvent event) {

        log.info(
                "Password Changed | UserId={} | EmployeeCode={} | ChangedBy={} | ChangedAt={}",
                event.getUserId(),
                event.getEmployeeCode(),
                event.getChangedBy(),
                event.getChangedAt()
        );

        // Future integration:
        // Logout All Devices
        // Revoke Refresh Tokens
        // Send Email Notification
        // Send SMS Notification
        // Publish Audit Log
    }

    /**
     * Handles password reset events.
     */
    @Async
    @EventListener
    public void handlePasswordResetEvent(
            PasswordResetEvent event) {

        log.info(
                "Password Reset | UserId={} | EmployeeCode={} | ResetBy={} | ResetAt={}",
                event.getUserId(),
                event.getEmployeeCode(),
                event.getResetBy(),
                event.getResetAt()
        );

        // Future integration:
        // Logout All Devices
        // Revoke Refresh Tokens
        // Send Email Notification
        // Publish Audit Log
    }

    /**
     * Handles refresh token creation events.
     */
    @Async
    @EventListener
    public void handleRefreshTokenCreatedEvent(
            RefreshTokenCreatedEvent event) {

        log.info(
                "Refresh Token Created | UserId={} | EmployeeCode={} | Expiry={} | Device={} | Browser={}",
                event.getUserId(),
                event.getEmployeeCode(),
                event.getExpiryDate(),
                event.getDeviceName(),
                event.getBrowser()
        );

        // Future integration:
        // Save Login Session
        // Audit Logging
        // Security Monitoring
    }

}