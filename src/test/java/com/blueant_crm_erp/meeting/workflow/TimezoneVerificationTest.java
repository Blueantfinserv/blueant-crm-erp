package com.blueant_crm_erp.meeting.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TimeZone;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TimezoneVerificationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testJvmTimezoneIsKolkata() {
        assertEquals("Asia/Kolkata", TimeZone.getDefault().getID(), "JVM default timezone must be Asia/Kolkata");
        assertEquals(ZoneId.of("Asia/Kolkata"), ZoneId.systemDefault(), "System default ZoneId must be Asia/Kolkata");
    }

    @Test
    public void testJacksonTimeZoneIsKolkata() {
        assertEquals(TimeZone.getTimeZone("Asia/Kolkata"), objectMapper.getSerializationConfig().getTimeZone(), "Jackson ObjectMapper serialization timezone must be Asia/Kolkata");
        assertEquals(TimeZone.getTimeZone("Asia/Kolkata"), objectMapper.getDeserializationConfig().getTimeZone(), "Jackson ObjectMapper deserialization timezone must be Asia/Kolkata");
    }

    @Test
    public void testDatabaseTimezone() {
        String sessionTimeZone = jdbcTemplate.queryForObject("SELECT @@session.time_zone", String.class);
        String globalTimeZone = jdbcTemplate.queryForObject("SELECT @@global.time_zone", String.class);
        String dbNow = jdbcTemplate.queryForObject("SELECT NOW()", String.class);
        System.out.println("====== DATABASE TIMEZONE INFO ======");
        System.out.println("Session Time Zone: " + sessionTimeZone);
        System.out.println("Global Time Zone: " + globalTimeZone);
        System.out.println("Database NOW(): " + dbNow);
        System.out.println("====================================");
    }

    @Autowired
    private com.blueant_crm_erp.lead.service.LeadService leadService;

    @Autowired
    private com.blueant_crm_erp.meeting.service.MeetingScheduleService meetingScheduleService;

    @Autowired
    private com.blueant_crm_erp.meeting.service.MeetingService meetingService;

    @Test
    public void testMeetingRemarksPersistence() {
        String currentUserEmail = "EMP000001";
        
        // 1. Create a lead
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Remarks Verification Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, currentUserEmail);

        // 2. Schedule meeting
        CreateMeetingRequest scheduleRequest = new CreateMeetingRequest();
        scheduleRequest.setLeadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()));
        scheduleRequest.setMeetingMode(MeetingMode.PHYSICAL);
        scheduleRequest.setMeetingDate(LocalDate.now().plusDays(1));
        scheduleRequest.setMeetingTime(LocalTime.of(10, 0));
        scheduleRequest.setMeetingLocation("Office");
        
        MeetingResponse introMeeting = meetingScheduleService.scheduleMeeting(scheduleRequest, currentUserEmail);
        assertEquals(LocalTime.of(10, 0), introMeeting.getMeetingTime(), "Scheduled meetingTime must be 10:00");

        // 3. Perform workflow update with remarks
        MeetingWorkflowRequest update1 = new MeetingWorkflowRequest();
        update1.setMeetingConducted(MeetingConductStatus.CONDUCTED);
        update1.setAloneWith("SELF");
        update1.setLeadStatus(MeetingLeadStatus.WORK_IN_PROGRESS);
        update1.setMeetingRemarks("Test 1 -1");
        update1.setDiscussion("Intro was good");
        update1.setNextPlanDate(LocalDate.now().plusDays(2));
        update1.setNextPlanTime(LocalTime.of(11, 0));
        
        meetingService.processMeetingUpdateWorkflow(introMeeting.getMeetingCode(), update1, currentUserEmail);

        // 4. Verify remarks and meetingTime on the original completed meeting in DB
        String remarksInDb = jdbcTemplate.queryForObject(
                "SELECT remarks FROM meetings WHERE meeting_code = ?", 
                String.class, 
                introMeeting.getMeetingCode());
        assertEquals("Test 1 -1", remarksInDb, "Meeting remarks should be persisted in meetings table");

        LocalTime timeInDb = jdbcTemplate.queryForObject(
                "SELECT meeting_time FROM meetings WHERE meeting_code = ?", 
                LocalTime.class, 
                introMeeting.getMeetingCode());
        assertEquals(LocalTime.of(10, 0), timeInDb, "Meeting time in DB must remain 10:00");

        // 5. Verify remarks and meetingTime through API / service response
        MeetingDetailResponse detail = meetingService.getMeetingByCode(introMeeting.getMeetingCode());
        assertEquals("Test 1 -1", detail.getMeetingRemarks(), "GET meeting response should return the persisted remarks");
        assertEquals(LocalTime.of(10, 0), detail.getMeetingTime(), "GET meeting response should return the correct meetingTime (10:00)");

        // 6. Verify newly scheduled meeting has null remarks and correct next meeting time (11:00)
        String nextMeetingCode = meetingService.getActiveMeetingByLeadId(leadResponse.getUniqueLeadId()).getMeetingCode();
        MeetingDetailResponse nextDetail = meetingService.getMeetingByCode(nextMeetingCode);
        org.junit.jupiter.api.Assertions.assertNull(nextDetail.getMeetingRemarks(), "Next scheduled meeting remarks should be null");
        assertEquals(LocalTime.of(11, 0), nextDetail.getMeetingTime(), "Next scheduled meetingTime should be 11:00");
    }

    @Test
    public void testLocalDateTimeSerialization() throws Exception {
        LocalDateTime testDateTime = LocalDateTime.of(2026, 8, 12, 15, 30, 45);
        String json = objectMapper.writeValueAsString(testDateTime);
        // Should serialize as a formatted string without shifting (as LocalDateTime is timezone-unaware but uses the custom formatter)
        assertEquals("\"2026-08-12 15:30:45\"", json, "LocalDateTime should serialize using the default format");
    }
}
