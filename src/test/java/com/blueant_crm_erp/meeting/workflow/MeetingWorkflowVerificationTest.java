package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.InvestmentType;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.service.MeetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
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
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
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

        // Helper to set GPS parameters on requests
        java.math.BigDecimal lat = new java.math.BigDecimal("28.6139");
        java.math.BigDecimal lng = new java.math.BigDecimal("77.2090");
        Double acc = 15.5;


        // 4. Update the Intro Meeting
        MeetingWorkflowRequest update1 = new MeetingWorkflowRequest();
        update1.setMeetingConducted(MeetingConductStatus.CONDUCTED);
        update1.setAloneWith("SELF");
        update1.setLeadStatus(MeetingLeadStatus.WORK_IN_PROGRESS);
        update1.setMeetingRemarks("Intro remarks: Work in progress");
        update1.setDiscussion("Intro was good");
        update1.setNextPlanDate(LocalDate.now().plusDays(2));
        update1.setNextPlanTime(LocalTime.of(11, 0));
        update1.setLatitude(lat);
        update1.setLongitude(lng);
        update1.setAccuracy(acc);
        
        meetingService.processMeetingUpdateWorkflow(introMeeting.getMeetingCode(), update1, currentUserEmail);
        System.out.println("\n[STEP 3] Updated Intro Meeting -> Should create Meeting 1");
        printMeetingTable(leadResponse.getLeadId());
        
        // 5. Update Meeting 1
        String meeting1Code = getActiveMeetingCode(leadResponse.getUniqueLeadId());
        MeetingWorkflowRequest update2 = new MeetingWorkflowRequest();
        update2.setMeetingConducted(MeetingConductStatus.CONDUCTED);
        update2.setAloneWith("SELF");
        update2.setLeadStatus(MeetingLeadStatus.WORK_IN_PROGRESS);
        update2.setMeetingRemarks("Meeting 1 remarks: WIP");
        update2.setDiscussion("Needs another follow-up");
        update2.setNextPlanDate(LocalDate.now().plusDays(3));
        update2.setNextPlanTime(LocalTime.of(14, 0));
        update2.setLatitude(lat);
        update2.setLongitude(lng);
        update2.setAccuracy(acc);
        
        meetingService.processMeetingUpdateWorkflow(meeting1Code, update2, currentUserEmail);
        System.out.println("\n[STEP 4] Updated Meeting 1 -> Should create Meeting 2");
        printMeetingTable(leadResponse.getLeadId());

        // Update Meeting 2
        String meeting2Code = getActiveMeetingCode(leadResponse.getUniqueLeadId());
        MeetingWorkflowRequest update3 = new MeetingWorkflowRequest();
        update3.setMeetingConducted(MeetingConductStatus.CONDUCTED);
        update3.setAloneWith("SELF");
        update3.setLeadStatus(MeetingLeadStatus.WORK_IN_PROGRESS);
        update3.setMeetingRemarks("Meeting 2 remarks: WIP");
        update3.setDiscussion("Still discussing");
        update3.setNextPlanDate(LocalDate.now().plusDays(4));
        update3.setNextPlanTime(LocalTime.of(15, 0));
        update3.setLatitude(lat);
        update3.setLongitude(lng);
        update3.setAccuracy(acc);
        
        meetingService.processMeetingUpdateWorkflow(meeting2Code, update3, currentUserEmail);
        System.out.println("\n[STEP 5] Updated Meeting 2 -> Should create Meeting 3");
        printMeetingTable(leadResponse.getLeadId());
        
        // Update Meeting 3
        String meeting3Code = getActiveMeetingCode(leadResponse.getUniqueLeadId());
        MeetingWorkflowRequest update4 = new MeetingWorkflowRequest();
        update4.setMeetingConducted(MeetingConductStatus.CONDUCTED);
        update4.setAloneWith("SELF");
        update4.setLeadStatus(MeetingLeadStatus.WORK_IN_PROGRESS);
        update4.setMeetingRemarks("Meeting 3 remarks: WIP");
        update4.setDiscussion("Deciding amount");
        update4.setNextPlanDate(LocalDate.now().plusDays(5));
        update4.setNextPlanTime(LocalTime.of(16, 0));
        update4.setLatitude(lat);
        update4.setLongitude(lng);
        update4.setAccuracy(acc);
        
        meetingService.processMeetingUpdateWorkflow(meeting3Code, update4, currentUserEmail);
        System.out.println("\n[STEP 6] Updated Meeting 3 -> Should create Meeting 4");
        printMeetingTable(leadResponse.getLeadId());
        
        // Update Meeting 4 -> Terminal Outcome
        String meeting4Code = getActiveMeetingCode(leadResponse.getUniqueLeadId());
        MeetingWorkflowRequest terminalUpdate = new MeetingWorkflowRequest();
        terminalUpdate.setMeetingConducted(MeetingConductStatus.CONDUCTED);
        terminalUpdate.setAloneWith("SELF");
        terminalUpdate.setLeadStatus(MeetingLeadStatus.CONVERTED_CLIENT);
        terminalUpdate.setMeetingRemarks("Client Converted!");
        terminalUpdate.setDiscussion("Client Converted with SIP!");
        terminalUpdate.setPanNumber("ABCDE1234F");
        terminalUpdate.setInvestmentAmount(new BigDecimal("5000.00"));
        terminalUpdate.setInvestmentType(InvestmentType.SIP);
        terminalUpdate.setLatitude(lat);
        terminalUpdate.setLongitude(lng);
        terminalUpdate.setAccuracy(acc);
        
        meetingService.processMeetingUpdateWorkflow(meeting4Code, terminalUpdate, currentUserEmail);
        System.out.println("\n[STEP 7] Updated Meeting 4 with Terminal Outcome (CONVERTED_CLIENT) -> Should NOT create next meeting");
        printMeetingTable(leadResponse.getLeadId());
        
        System.out.println("\n=================================================");
        System.out.println("END OF VERIFICATION");
        System.out.println("=================================================");
    }
    
    private String getActiveMeetingCode(String leadId) {
        return meetingService.getActiveMeetingByLeadId(leadId).getMeetingCode();
    }

    private void printMeetingTable(Long leadId) {
        String sql = "SELECT id, meeting_code, meeting_sequence, meeting_title, meeting_status, meeting_conducted, lead_status, lead_id FROM meetings WHERE lead_id = ? ORDER BY id ASC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, leadId);
        
        System.out.println("--- SQL SNAPSHOT ---");
        System.out.printf("%-5s | %-15s | %-10s | %-20s | %-15s | %-20s | %-20s | %-10s%n", 
            "ID", "CODE", "SEQUENCE", "TITLE", "STATUS", "CONDUCTED", "LEAD_STATUS", "LEAD_ID");
        System.out.println("----------------------------------------------------------------------------------------------------------------------");
        for (Map<String, Object> row : rows) {
            System.out.printf("%-5s | %-15s | %-10s | %-20s | %-15s | %-20s | %-20s | %-10s%n",
                row.get("id"),
                row.get("meeting_code"),
                row.get("meeting_sequence"),
                row.get("meeting_title"),
                row.get("meeting_status"),
                row.get("meeting_conducted"),
                row.get("lead_status"),
                row.get("lead_id"));
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------------");
    }
}
