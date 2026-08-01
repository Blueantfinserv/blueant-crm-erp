package com.blueant_crm_erp.user.listener;


import com.blueant_crm_erp.user.event.TeamAssignedEvent;
import com.blueant_crm_erp.user.event.ReportingManagerChangedEvent;
import com.blueant_crm_erp.user.event.UserCreatedEvent;
import com.blueant_crm_erp.user.event.UserDeletedEvent;
import com.blueant_crm_erp.user.event.UserStatusChangedEvent;
import com.blueant_crm_erp.user.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * =============================================================================
 * User Event Listener
 * =============================================================================
 *
 * Handles all User Domain Events.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • User Created
 * • User Updated
 * • User Deleted
 * • User Status Changed
 * • Team Assignment
 * • Reporting Manager Change
 *
 * Future Integration
 * -----------------------------------------------------------------------------
 * • Audit Logs
 * • Notifications
 * • Email
 * • WhatsApp
 * • SMS
 * • Dashboard Refresh
 * • Redis Cache Eviction
 * • Activity Timeline
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    /**
     * User Created Event
     */
    @Async
    @EventListener
    public void handleUserCreatedEvent(
            UserCreatedEvent event
    ) {

        log.info(
                "User Created | UserId={} | EmployeeCode={} | CreatedBy={}",
                event.getUserId(),
                event.getEmployeeCode(),
                event.getCreatedBy()
        );

        // TODO
        // Audit Service
        // Notification Service
        // Welcome Email
        // Cache Eviction
    }

    /**
     * User Updated Event
     */
    @Async
    @EventListener
    public void handleUserUpdatedEvent(
            UserUpdatedEvent event
    ) {

        log.info(
                "User Updated | UserId={} | UpdatedBy={}",
                event.getUserId(),
                event.getUpdatedBy()
        );

    }

    @Async
    @EventListener
    public void handleReportingManagerChangedEvent(
            ReportingManagerChangedEvent event
    ) {
        // ...
    }

    /**
     * User Deleted Event
     */
    @Async
    @EventListener
    public void handleUserDeletedEvent(
            UserDeletedEvent event
    ) {

        log.info(
                "User Deleted | UserId={} | DeletedBy={}",
                event.getUserId(),
                event.getDeletedBy()
        );

    }

    /**
     * User Status Changed Event
     */
    @Async
    @EventListener
    public void handleUserStatusChangedEvent(
            UserStatusChangedEvent event
    ) {

        log.info(
                "User Status Changed | UserId={} | Status={} | UpdatedBy={}",
                event.getUserId(),
                event.getStatus(),
                event.getUpdatedBy()
        );

    }

    /**
     * Team Assigned Event
     */
    @Async
    @EventListener
    public void handleTeamAssignedEvent(
            TeamAssignedEvent event
    ) {

        log.info(
                "Team Assigned | UserId={} | TeamId={} | UpdatedBy={}",
                event.getUserId(),
                event.getTeamId(),
                event.getUpdatedBy()
        );

    }

    /**
     * Reporting Manager Changed Event
     */
    @Async
    @EventListener
    public void handleManagerChangedEvent(
            ReportingManagerChangedEvent event
    ) {

        log.info(
                "Reporting Manager Changed | UserId={} | ManagerId={} | UpdatedBy={}",
                event.getUserId(),
                event.getReportingManagerId(),
                event.getUpdatedBy()
        );

    }
}