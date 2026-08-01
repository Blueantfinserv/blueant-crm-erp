package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.service.MeetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class MeetingWorkflowVerificationTest {

    @Autowired
    private LeadService leadService;

    @Autowired
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void verifyEndToEndMeetingWorkflow() {
        System.out.println("=================================================");
        System.out.println("STARTING END-TO-END MEETING WORKFLOW VERIFICATION");
        System.out.println("=================================================");

        String currentUserEmail = "EMP000001"; // Assuming Super Admin

        // 1. Create a brand-new lead
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Test Verification Client");
        leadRequest.setMobileNumber("9998887776");
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, currentUserEmail);
        
        System.out.println("\n[STEP 1] Lead Created: " + leadResponse.getLeadCode() + " (UUID: " + leadResponse.getUniqueLeadId() + ")");

        // 2. Schedule the Intro Meeting
        CreateMeetingRequest scheduleRequest = new CreateMeetingRequest();
        scheduleRequest.setLeadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()));
        scheduleRequest.setMeetingMode(MeetingMode.PHYSICAL);
        scheduleRequest.setMeetingDate(LocalDate.now().plusDays(1));
        scheduleRequest.setMeetingTime(LocalTime.of(10, 0));
        scheduleRequest.setMeetingLocation("Office");
        
        MeetingResponse introMeeting = meetingScheduleService.scheduleMeeting(scheduleRequest, currentUserEmail);
        System.out.println("\n[STEP 2] Scheduled Intro Meeting");
        printMeetingTable(leadResponse.getLeadId());

        // 4. Update the Intro Meeting
        MeetingWorkflowRequest update1 = new MeetingWorkflowRequest();
        update1.setMeetingOutcome(MeetingOutcome.INTERESTED);
        update1.setDiscussion("Intro was good");
        update1.setNextMeetingDate(LocalDate.now().plusDays(2));
        update1.setNextMeetingTime(LocalTime.of(11, 0));
        
        meetingService.processMeetingUpdateWorkflow(introMeeting.getMeetingCode(), update1, currentUserEmail);
        System.out.println("\n[STEP 3] Updated Intro Meeting -> Should create Meeting 1");
        printMeetingTable(leadResponse.getLeadId());
        
        // 5. Update Meeting 1
        String meeting1Code = getActiveMeetingCode(leadResponse.getUniqueLeadId());
        MeetingWorkflowRequest update2 = new MeetingWorkflowRequest();
        update2.setMeetingOutcome(MeetingOutcome.FOLLOW_UP_REQUIRED);
        update2.setDiscussion("Needs another follow-up");
        update2.setNextMeetingDate(LocalDate.now().plusDays(3));
        update2.setNextMeetingTime(LocalTime.of(14, 0));
        
        meetingService.processMeetingUpdateWorkflow(meeting1Code, update2, currentUserEmail);
        System.out.println("\n[STEP 4] Updated Meeting 1 -> Should create Meeting 2");
        printMeetingTable(leadResponse.getLeadId());

        // Update Meeting 2
        String meeting2Code = getActiveMeetingCode(leadResponse.getUniqueLeadId());
        MeetingWorkflowRequest update3 = new MeetingWorkflowRequest();
        update3.setMeetingOutcome(MeetingOutcome.INTERESTED);
        update3.setNextMeetingDate(LocalDate.now().plusDays(4));
        update3.setNextMeetingTime(LocalTime.of(15, 0));
        
        meetingService.processMeetingUpdateWorkflow(meeting2Code, update3, currentUserEmail);
        System.out.println("\n[STEP 5] Updated Meeting 2 -> Should create Meeting 3");
        printMeetingTable(leadResponse.getLeadId());
        
        // Update Meeting 3
        String meeting3Code = getActiveMeetingCode(leadResponse.getUniqueLeadId());
        MeetingWorkflowRequest update4 = new MeetingWorkflowRequest();
        update4.setMeetingOutcome(MeetingOutcome.INTERESTED);
        update4.setNextMeetingDate(LocalDate.now().plusDays(5));
        update4.setNextMeetingTime(LocalTime.of(16, 0));
        
        meetingService.processMeetingUpdateWorkflow(meeting3Code, update4, currentUserEmail);
        System.out.println("\n[STEP 6] Updated Meeting 3 -> Should create Meeting 4");
        printMeetingTable(leadResponse.getLeadId());
        
        // Update Meeting 4 -> Terminal Outcome
        String meeting4Code = getActiveMeetingCode(leadResponse.getUniqueLeadId());
        MeetingWorkflowRequest terminalUpdate = new MeetingWorkflowRequest();
        terminalUpdate.setMeetingOutcome(MeetingOutcome.CONVERTED);
        terminalUpdate.setDiscussion("Client Converted!");
        // Notice: No next meeting date/time provided
        
        meetingService.processMeetingUpdateWorkflow(meeting4Code, terminalUpdate, currentUserEmail);
        System.out.println("\n[STEP 7] Updated Meeting 4 with Terminal Outcome (CONVERTED) -> Should NOT create next meeting");
        printMeetingTable(leadResponse.getLeadId());
        
        System.out.println("\n=================================================");
        System.out.println("END OF VERIFICATION");
        System.out.println("=================================================");
    }
    
    private String getActiveMeetingCode(String leadId) {
        return meetingService.getActiveMeetingByLeadId(leadId).getMeetingCode();
    }

    private void printMeetingTable(Long leadId) {
        String sql = "SELECT id, meeting_code, meeting_number, meeting_title, meeting_status, meeting_outcome, lead_id FROM meetings WHERE lead_id = ? ORDER BY id ASC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, leadId);
        
        System.out.println("--- SQL SNAPSHOT ---");
        System.out.printf("%-5s | %-15s | %-10s | %-20s | %-15s | %-20s | %-10s%n", 
            "ID", "CODE", "NUMBER", "TITLE", "STATUS", "OUTCOME", "LEAD_ID");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        for (Map<String, Object> row : rows) {
            System.out.printf("%-5s | %-15s | %-10s | %-20s | %-15s | %-20s | %-10s%n",
                row.get("id"),
                row.get("meeting_code"),
                row.get("meeting_number"),
                row.get("meeting_title"),
                row.get("meeting_status"),
                row.get("meeting_outcome"),
                row.get("lead_id"));
        }
        System.out.println("---------------------------------------------------------------------------------------------------------");
    }
}
