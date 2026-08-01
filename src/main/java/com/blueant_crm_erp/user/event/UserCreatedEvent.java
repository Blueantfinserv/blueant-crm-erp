package com.blueant_crm_erp.user.event;

import com.blueant_crm_erp.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * User Created Event
 * =============================================================================
 *
 * Published whenever a new user is successfully created.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Audit Logging
 * • Activity Timeline
 * • Notification
 * • Welcome Email
 * • Welcome SMS / WhatsApp
 * • Dashboard Analytics
 * • Future Kafka Publishing
 * • Future Redis Cache Synchronization
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author  : BlueAnt CRM ERP Team
 * Since   : 1.0.0
 * =============================================================================
 */
@Getter
public class UserCreatedEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Created User Entity.
     */
    private final User user;

    /**
     * User Id.
     */
    private final Long userId;

    /**
     * Employee Code.
     */
    private final String employeeCode;

    /**
     * User Full Name.
     */
    private final String fullName;

    /**
     * Created By User Id.
     */
    private final Long createdBy;

    /**
     * Event Time.
     */
    private final LocalDateTime createdAt;

    public UserCreatedEvent(
            Object source,
            User user,
            Long createdBy
    ) {
        super(source);
        this.user = user;
        this.userId = user.getId();
        this.employeeCode = user.getEmployeeCode();
        this.fullName = user.getFullName();
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

}