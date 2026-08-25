package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.service.LeadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class MeetingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateMeeting_LocalTimeDeserializationSuccess() throws Exception {
        // 1. Create a lead first so the request passes service layer checks
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Meeting Integration Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        // 2. Perform POST /v1/meetings
        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", java.time.LocalDate.now().plusDays(1).toString());
        payload.put("meetingTime", "10:14:00");
        payload.put("meetingLocation", "Noida");
        payload.put("meetingRemarks", "Testing for meeting update");
        payload.put("meetingStatus", "COMPLETED");

        String jsonPayload = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated()) // HTTP 201 Created
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Meeting created successfully"))
                .andExpect(jsonPath("$.data.meetingTime").value("10:14:00"));
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateMeeting_PastMeetingDateSuccess() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Past Date Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", java.time.LocalDate.now().minusDays(3).toString()); // 3 days in past
        payload.put("meetingTime", "10:00:00");
        payload.put("meetingLocation", "Noida Office");
        payload.put("meetingRemarks", "Past meeting validation test");
        payload.put("meetingStatus", "COMPLETED"); // COMPLETED status allows past date

        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateMeeting_TodayMeetingDateSuccess() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Today Date Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", java.time.LocalDate.now().toString()); // Today
        payload.put("meetingTime", "12:00:00");
        payload.put("meetingLocation", "Noida Office");
        payload.put("meetingRemarks", "Today meeting validation test");
        payload.put("meetingStatus", "COMPLETED");

        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateMeeting_FutureMeetingDateSuccess() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Future Date Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", java.time.LocalDate.now().plusDays(3).toString()); // 3 days in future
        payload.put("meetingTime", "14:00:00");
        payload.put("meetingLocation", "Noida Office");
        payload.put("meetingRemarks", "Future meeting validation test");
        payload.put("meetingStatus", "SCHEDULED"); // SCHEDULED in future is allowed

        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateMeeting_ScheduledMeetingPastDateFailure() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Scheduled Past Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", java.time.LocalDate.now().minusDays(3).toString()); // 3 days in past
        payload.put("meetingTime", "10:00:00");
        payload.put("meetingLocation", "Noida Office");
        payload.put("meetingRemarks", "Scheduled past validation test");
        payload.put("meetingStatus", "SCHEDULED"); // SCHEDULED in past MUST FAIL

        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateMeeting_InvalidDateFormatFailure() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Invalid Date Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", "invalid-date-format"); // Invalid format
        payload.put("meetingTime", "14:00:00");
        payload.put("meetingLocation", "Noida Office");

        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateMeeting_PastNextMeetingDateFailure() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Past Next Date Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", java.time.LocalDate.now().toString());
        payload.put("meetingTime", "14:00:00");
        payload.put("meetingLocation", "Noida Office");
        payload.put("nextMeetingDate", java.time.LocalDate.now().minusDays(1).toString()); // nextMeetingDate in past is invalid

        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateMeeting_FutureNextMeetingDateSuccess() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Future Next Date Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", java.time.LocalDate.now().toString());
        payload.put("meetingTime", "14:00:00");
        payload.put("meetingLocation", "Noida Office");
        payload.put("nextMeetingDate", java.time.LocalDate.now().plusDays(2).toString()); // nextMeetingDate in future is valid

        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testRescheduleMeeting_FutureRescheduleSuccess() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Reschedule Future Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", java.time.LocalDate.now().plusDays(2).toString());
        payload.put("meetingTime", "10:00:00");
        payload.put("meetingLocation", "Noida Office");
        payload.put("meetingRemarks", "Reschedule test");
        payload.put("meetingStatus", "SCHEDULED");

        String responseStr = mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String meetingCode = objectMapper.readTree(responseStr).path("data").path("meetingCode").asText();

        Map<String, Object> reschedulePayload = new HashMap<>();
        reschedulePayload.put("meetingCode", meetingCode);
        reschedulePayload.put("meetingDate", java.time.LocalDate.now().plusDays(1).toString()); // future
        reschedulePayload.put("meetingTime", "11:00:00");
        reschedulePayload.put("meetingLocation", "New Delhi Noida Office");
        reschedulePayload.put("rescheduleReason", "Rescheduling to tomorrow");

        mockMvc.perform(post("/v1/meetings/" + meetingCode + "/reschedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reschedulePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testRescheduleMeeting_PastRescheduleFailure() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Reschedule Past Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", leadResponse.getUniqueLeadId());
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingDate", java.time.LocalDate.now().plusDays(2).toString());
        payload.put("meetingTime", "10:00:00");
        payload.put("meetingLocation", "Noida Office");
        payload.put("meetingRemarks", "Reschedule test");
        payload.put("meetingStatus", "SCHEDULED");

        String responseStr = mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String meetingCode = objectMapper.readTree(responseStr).path("data").path("meetingCode").asText();

        Map<String, Object> reschedulePayload = new HashMap<>();
        reschedulePayload.put("meetingCode", meetingCode);
        reschedulePayload.put("meetingDate", java.time.LocalDate.now().minusDays(3).toString()); // Past date -> MUST FAIL
        reschedulePayload.put("meetingTime", "11:00:00");
        reschedulePayload.put("meetingLocation", "New Delhi Noida Office");
        reschedulePayload.put("rescheduleReason", "Rescheduling to past");

        mockMvc.perform(post("/v1/meetings/" + meetingCode + "/reschedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reschedulePayload)))
                .andExpect(status().isBadRequest());
    }
}
