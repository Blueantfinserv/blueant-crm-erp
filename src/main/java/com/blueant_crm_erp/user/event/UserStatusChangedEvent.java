package com.blueant_crm_erp.user.event;

import com.blueant_crm_erp.common.enums.Status;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * User Status Changed Event
 * =============================================================================
 *
 * Published whenever a user's status changes.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 * =============================================================================
 */
@Getter
public class UserStatusChangedEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User Id
     */
    private final Long userId;

    /**
     * Previous Status
     */
    private final Status oldStatus;

    /**
     * New Status
     */
    private final Status newStatus;

    /**
     * Updated By
     */
    private final Long updatedBy;

    /**
     * Event Time
     */
    private final LocalDateTime eventTime;

    public UserStatusChangedEvent(
            Object source,
            Long userId,
            Status oldStatus,
            Status newStatus,
            Long updatedBy
    ) {
        super(source);
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.updatedBy = updatedBy;
        this.eventTime = LocalDateTime.now();
    }

    /**
     * Current Status
     * (used by listener)
     */
    public Status getStatus() {
        return newStatus;
    }
}