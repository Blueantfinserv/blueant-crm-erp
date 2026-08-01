package com.blueant_crm_erp.user.event;

import com.blueant_crm_erp.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * User Deleted Event
 * =============================================================================
 *
 * Published whenever a user is soft deleted from the system.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Audit Logging
 * • Activity Timeline
 * • Notification
 * • Dashboard Analytics
 * • Future Kafka Publishing
 * • Future Redis Cache Eviction
 *
 * NOTE
 * -----------------------------------------------------------------------------
 * This event is fired only for Soft Delete.
 * User data always remains in database.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author  : BlueAnt CRM ERP Team
 * Since   : 1.0.0
 * =============================================================================
 */
@Getter
public class UserDeletedEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Deleted User.
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
     * Deleted By User Id.
     */
    private final Long deletedBy;

    /**
     * Deletion Time.
     */
    private final LocalDateTime deletedAt;

    public UserDeletedEvent(
            Object source,
            User user,
            Long deletedBy
    ) {
        super(source);
        this.user = user;
        this.userId = user.getId();
        this.employeeCode = user.getEmployeeCode();
        this.fullName = user.getFullName();
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
    }

}