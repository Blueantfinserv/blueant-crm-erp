package com.blueant_crm_erp.user.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.time.LocalDateTime;

@Getter
public class UserUpdatedEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final Long updatedBy;
    private final LocalDateTime eventTime;

    public UserUpdatedEvent(
            Object source,
            Long userId,
            Long updatedBy
    ) {
        super(source);
        this.userId = userId;
        this.updatedBy = updatedBy;
        this.eventTime = LocalDateTime.now();
    }
}