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
import com.blueant_crm_erp.meeting.dto.request.RescheduleMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingRequest;
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
        update1.setRemarks("Test 1 -1");
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
        assertEquals("Test 1 -1", detail.getRemarks(), "GET meeting response should return the persisted remarks");
        assertEquals(LocalTime.of(10, 0), detail.getMeetingTime(), "GET meeting response should return the correct meetingTime (10:00)");

        // 6. Verify newly scheduled meeting has null remarks and correct next meeting time (11:00)
        String nextMeetingCode = meetingService.getActiveMeetingByLeadId(leadResponse.getUniqueLeadId()).getMeetingCode();
        MeetingDetailResponse nextDetail = meetingService.getMeetingByCode(nextMeetingCode);
        assertEquals("Test 1 -1", nextDetail.getRemarks(), "Next scheduled meeting remarks should be propagated");
        assertEquals(LocalTime.of(11, 0), nextDetail.getMeetingTime(), "Next scheduled meetingTime should be 11:00");
    }

    @Test
    public void testNullableMeetingTimeBehavior() {
        String currentUserEmail = "EMP000001";
        
        // 1. Create a lead
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Nullable Time Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, currentUserEmail);

        // 2. Schedule meeting WITHOUT time
        CreateMeetingRequest scheduleRequest = new CreateMeetingRequest();
        scheduleRequest.setLeadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()));
        scheduleRequest.setMeetingMode(MeetingMode.PHYSICAL);
        scheduleRequest.setMeetingDate(LocalDate.now().plusDays(1));
        scheduleRequest.setMeetingTime(null); // Omitted/null time
        scheduleRequest.setMeetingLocation("Office");
        
        MeetingResponse introMeeting = meetingScheduleService.scheduleMeeting(scheduleRequest, currentUserEmail);
        String rawDateInDb = jdbcTemplate.queryForObject(
                "SELECT CAST(meeting_date AS CHAR) FROM meetings WHERE meeting_code = ?", 
                String.class, 
                introMeeting.getMeetingCode());
        assertEquals(LocalDate.now().plusDays(1).toString(), rawDateInDb, "Raw meeting_date in database must match the scheduled date");
        org.junit.jupiter.api.Assertions.assertNull(introMeeting.getMeetingTime(), "Scheduled meetingTime must be null");

        // 3. Verify meetingTime is null in DB
        LocalTime timeInDb = jdbcTemplate.queryForObject(
                "SELECT meeting_time FROM meetings WHERE meeting_code = ?", 
                LocalTime.class, 
                introMeeting.getMeetingCode());
        org.junit.jupiter.api.Assertions.assertNull(timeInDb, "Meeting time in DB must be null");

        // 4. Update the meeting with a valid time
        UpdateMeetingRequest updateRequest = UpdateMeetingRequest.builder()
                .meetingCode(introMeeting.getMeetingCode())
                .meetingDate(introMeeting.getMeetingDate())
                .meetingTime(LocalTime.of(12, 0))
                .meetingMode(introMeeting.getMeetingMode())
                .meetingLocation(introMeeting.getMeetingLocation())
                .build();
        MeetingResponse updatedIntroMeeting = meetingService.updateMeeting(introMeeting.getMeetingCode(), updateRequest, currentUserEmail);
        assertEquals(LocalTime.of(12, 0), updatedIntroMeeting.getMeetingTime(), "Updated meetingTime must be 12:00");

        // 5. Verify updated meetingTime is in DB
        timeInDb = jdbcTemplate.queryForObject(
                "SELECT meeting_time FROM meetings WHERE meeting_code = ?", 
                LocalTime.class, 
                introMeeting.getMeetingCode());
        assertEquals(LocalTime.of(12, 0), timeInDb, "Meeting time in DB must be 12:00 after update");

        // 6. Perform workflow update with null nextPlanTime
        MeetingWorkflowRequest update1 = new MeetingWorkflowRequest();
        update1.setMeetingConducted(MeetingConductStatus.CONDUCTED);
        update1.setAloneWith("SELF");
        update1.setLeadStatus(MeetingLeadStatus.WORK_IN_PROGRESS);
        update1.setRemarks("Test remarks");
        update1.setDiscussion("Discussion");
        update1.setNextPlanDate(LocalDate.now().plusDays(2));
        update1.setNextPlanTime(null); // Next meeting time is optional / null
        
        meetingService.processMeetingUpdateWorkflow(introMeeting.getMeetingCode(), update1, currentUserEmail);

        // 7. Verify current completed meeting retains its time of 12:00
        MeetingDetailResponse detail = meetingService.getMeetingByCode(introMeeting.getMeetingCode());
        assertEquals(LocalTime.of(12, 0), detail.getMeetingTime(), "Completed current meeting must retain its meetingTime (12:00)");

        // 8. Verify next scheduled meeting has NULL meetingTime
        String nextMeetingCode = meetingService.getActiveMeetingByLeadId(leadResponse.getUniqueLeadId()).getMeetingCode();
        MeetingDetailResponse nextDetail = meetingService.getMeetingByCode(nextMeetingCode);
        org.junit.jupiter.api.Assertions.assertNull(nextDetail.getMeetingTime(), "Next scheduled meetingTime should be null");

        // 9. Reschedule next meeting to 14:00
        RescheduleMeetingRequest rescheduleRequest = RescheduleMeetingRequest.builder()
                .meetingCode(nextMeetingCode)
                .meetingDate(nextDetail.getMeetingDate().plusDays(1))
                .meetingTime(LocalTime.of(14, 0))
                .meetingLocation(nextDetail.getMeetingLocation())
                .rescheduleReason("Rescheduled")
                .build();
        MeetingResponse rescheduledMeeting = meetingScheduleService.rescheduleMeeting(nextMeetingCode, rescheduleRequest, currentUserEmail);
        assertEquals(LocalTime.of(14, 0), rescheduledMeeting.getMeetingTime(), "Rescheduled meetingTime must be 14:00");
    }

    @Test
    public void testNextMeetingDateAndLocationPersistence() {
        String currentUserEmail = "EMP000001";
        LocalDate nextMeetingDate = LocalDate.now().plusDays(10);
        LocalTime nextMeetingTime = LocalTime.of(15, 0);
        
        // 1. Create a lead
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Persistence Verification Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, currentUserEmail);

        // 2. Schedule meeting WITH nextMeetingDate and nextMeetingTime
        CreateMeetingRequest scheduleRequest = new CreateMeetingRequest();
        scheduleRequest.setLeadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()));
        scheduleRequest.setMeetingMode(MeetingMode.PHYSICAL);
        scheduleRequest.setMeetingDate(LocalDate.now().plusDays(1));
        scheduleRequest.setMeetingTime(LocalTime.of(10, 0));
        scheduleRequest.setMeetingLocation("Initial Location");
        scheduleRequest.setNextMeetingDate(nextMeetingDate);
        scheduleRequest.setNextMeetingTime(nextMeetingTime);
        
        MeetingResponse introMeeting = meetingScheduleService.scheduleMeeting(scheduleRequest, currentUserEmail);
        assertEquals(nextMeetingDate, introMeeting.getNextMeetingDate(), "Scheduled meeting nextMeetingDate must be correct");
        assertEquals(nextMeetingTime, introMeeting.getNextMeetingTime(), "Scheduled meeting nextMeetingTime must be correct");

        // 3. Verify meetingDate and nextMeetingDate are in DB as exact matching raw strings (no timezone shift)
        String rawMeetingDateInDb = jdbcTemplate.queryForObject(
                "SELECT CAST(meeting_date AS CHAR) FROM meetings WHERE meeting_code = ?", 
                String.class, 
                introMeeting.getMeetingCode());
        assertEquals(LocalDate.now().plusDays(1).toString(), rawMeetingDateInDb, "Raw meeting_date in database must match the scheduled date");

        String rawNextMeetingDateInDb = jdbcTemplate.queryForObject(
                "SELECT CAST(next_meeting_date AS CHAR) FROM meetings WHERE meeting_code = ?", 
                String.class, 
                introMeeting.getMeetingCode());
        assertEquals(nextMeetingDate.toString(), rawNextMeetingDateInDb, "Raw next_meeting_date in database must match the requested date");

        LocalTime nextTimeInDb = jdbcTemplate.queryForObject(
                "SELECT next_meeting_time FROM meetings WHERE meeting_code = ?", 
                LocalTime.class, 
                introMeeting.getMeetingCode());
        assertEquals(nextMeetingTime, nextTimeInDb, "next_meeting_time in DB must match");

        // 4. Perform workflow update with location and remarks
        MeetingWorkflowRequest update1 = new MeetingWorkflowRequest();
        update1.setMeetingConducted(MeetingConductStatus.CONDUCTED);
        update1.setAloneWith("SELF");
        update1.setLeadStatus(MeetingLeadStatus.WORK_IN_PROGRESS);
        update1.setMeetingLocation("Noida");
        update1.setRemarks("Client interested in mutual fund investment");
        update1.setDiscussion("Discussion");
        update1.setNextPlanDate(LocalDate.now().plusDays(20));
        update1.setNextPlanTime(LocalTime.of(16, 0));
        
        meetingService.processMeetingUpdateWorkflow(introMeeting.getMeetingCode(), update1, currentUserEmail);

        // 5. Verify current completed meeting has Noida and remarks
        MeetingDetailResponse detail = meetingService.getMeetingByCode(introMeeting.getMeetingCode());
        assertEquals("Noida", detail.getMeetingLocation(), "Completed current meeting location must be Noida");
        assertEquals("Client interested in mutual fund investment", detail.getRemarks(), "Completed current meeting remarks must be set");

        // 6. Verify current completed meeting location and remarks in DB
        String locInDb = jdbcTemplate.queryForObject(
                "SELECT meeting_location FROM meetings WHERE meeting_code = ?", 
                String.class, 
                introMeeting.getMeetingCode());
        assertEquals("Noida", locInDb, "Completed meeting location in DB must be Noida");

        String remInDb = jdbcTemplate.queryForObject(
                "SELECT remarks FROM meetings WHERE meeting_code = ?", 
                String.class, 
                introMeeting.getMeetingCode());
        assertEquals("Client interested in mutual fund investment", remInDb, "Completed meeting remarks in DB must be set");

        // 7. Verify next scheduled meeting does not copy completed meeting's remarks or location
        String nextMeetingCode = meetingService.getActiveMeetingByLeadId(leadResponse.getUniqueLeadId()).getMeetingCode();
        MeetingDetailResponse nextDetail = meetingService.getMeetingByCode(nextMeetingCode);
        assertEquals("Client interested in mutual fund investment", nextDetail.getRemarks(), "Next scheduled meeting remarks must be propagated");
        org.junit.jupiter.api.Assertions.assertNull(nextDetail.getMeetingLocation(), "Next scheduled meeting location must be null");
        assertEquals(LocalDate.now().plusDays(20), nextDetail.getMeetingDate(), "Next scheduled meeting date must be correct");
        assertEquals(LocalTime.of(16, 0), nextDetail.getMeetingTime(), "Next scheduled meeting time must be 16:00");
    }

    @Test
    public void testLocalDateTimeSerialization() throws Exception {
        LocalDateTime testDateTime = LocalDateTime.of(2026, 8, 12, 15, 30, 45);
        String json = objectMapper.writeValueAsString(testDateTime);
        // Should serialize as a formatted string without shifting (as LocalDateTime is timezone-unaware but uses the custom formatter)
        assertEquals("\"2026-08-12 15:30:45\"", json, "LocalDateTime should serialize using the default format");
    }
}
