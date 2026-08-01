package com.blueant_crm_erp.meeting.event;

import com.blueant_crm_erp.meeting.entity.Meeting;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MeetingWorkflowEvent extends ApplicationEvent {

    private final Meeting meeting;
    private final String eventType;
    private final String previousStatus;
    private final String description;
    private final String triggeredBy;

    public MeetingWorkflowEvent(Object source, Meeting meeting, String eventType, String previousStatus, String description, String triggeredBy) {
        super(source);
        this.meeting = meeting;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
        this.description = description;
        this.triggeredBy = triggeredBy;
    }
}
