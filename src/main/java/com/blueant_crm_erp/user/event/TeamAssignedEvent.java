package com.blueant_crm_erp.user.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * Team Assigned Event
 * =============================================================================
 *
 * Published whenever a user is assigned to a team.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 * =============================================================================
 */
@Getter
public class TeamAssignedEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User Id
     */
    private final Long userId;

    /**
     * Team Id
     */
    private final Long teamId;

    /**
     * Team Name
     */
    private final String teamName;

    /**
     * Updated By
     */
    private final Long updatedBy;

    /**
     * Event Time
     */
    private final LocalDateTime eventTime;

    public TeamAssignedEvent(
            Object source,
            Long userId,
            Long teamId,
            String teamName,
            Long updatedBy
    ) {
        super(source);
        this.userId = userId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.updatedBy = updatedBy;
        this.eventTime = LocalDateTime.now();
    }
}