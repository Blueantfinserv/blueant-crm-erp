package com.blueant_crm_erp.followup.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FollowUpCompletedEvent extends ApplicationEvent {

    private final Long followUpId;
    private final String completedBy;

    public FollowUpCompletedEvent(Object source, Long followUpId, String completedBy) {
        super(source);
        this.followUpId = followUpId;
        this.completedBy = completedBy;
    }
}
