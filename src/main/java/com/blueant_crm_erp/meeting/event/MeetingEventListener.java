package com.blueant_crm_erp.meeting.event;

import com.blueant_crm_erp.lead.event.LeadCreatedEvent;
import com.blueant_crm_erp.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventListener {

    private final MeetingService meetingService;

    @EventListener
    public void handleLeadCreatedEvent(LeadCreatedEvent event) {
        log.info("Received LeadCreatedEvent for lead: {}. Meeting creation is deferred.", event.getLead().getLeadCode());
        // meetingService.createInitialMeeting(event.getLead()); // Removed based on new workflow
    }
}
