package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MeetingWorkflowDirectorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private ObjectMapper objectMapper;

    private LeadResponse createTestLead() {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Director Test Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        return leadService.createLead(leadRequest, "EMP000001");
    }

    private MeetingResponse scheduleIntroMeeting(String leadUniqueId) {
        CreateMeetingRequest scheduleRequest = new CreateMeetingRequest();
        scheduleRequest.setLeadId(java.util.UUID.fromString(leadUniqueId));
        scheduleRequest.setMeetingMode(MeetingMode.PHYSICAL);
        scheduleRequest.setMeetingDate(LocalDate.now().plusDays(1));
        scheduleRequest.setMeetingTime(LocalTime.of(10, 0));
        scheduleRequest.setMeetingLocation("Meeting Room 1");
        MeetingResponse response = meetingScheduleService.scheduleMeeting(scheduleRequest, "EMP000001");
        org.junit.jupiter.api.Assertions.assertEquals(com.blueant_crm_erp.meeting.enums.MeetingType.INTRO, response.getMeetingType());
        return response;
    }

    // 1. Successful update with SELF + WORK_IN_PROGRESS
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test1_SuccessfulUpdateSelfWip() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.aloneWith").value("SELF"))
                .andExpect(jsonPath("$.data.leadStatus").value("WORK_IN_PROGRESS"));
    }

    // 2. Successful update with SOMEONE
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test2_SuccessfulUpdateSomeone() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SOMEONE");
        payload.put("personName", "John Doe");
        payload.put("position", "Manager");
        payload.put("leadStatus", "WORK_IN_PROGRESS");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.aloneWith").value("SOMEONE"))
                .andExpect(jsonPath("$.data.personName").value("John Doe"))
                .andExpect(jsonPath("$.data.position").value("Manager"));
    }

    // 3. ALREADY_CLIENT
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test3_AlreadyClient() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "ALREADY_CLIENT");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("ALREADY_CLIENT"));

        // Verify lead status is updated to ALREADY_CLIENT
        mockMvc.perform(get("/v1/leads/" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("ALREADY_CLIENT"));
    }

    // 4. CONVERTED_CLIENT
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test4_ConvertedClient() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "CONVERTED_CLIENT");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("CONVERTED_CLIENT"));

        // Verify lead status is updated to CONVERTED
        mockMvc.perform(get("/v1/leads/" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("CONVERTED"));
    }

    // 5. CLIENT_REMOVED
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test5_ClientRemoved() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "CLIENT_REMOVED");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("CLIENT_REMOVED"));

        // Verify lead status is updated to REMOVED
        mockMvc.perform(get("/v1/leads/" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("REMOVED"));
    }

    // 6. CLIENT_NOT_INTERESTED
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test6_ClientNotInterested() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "CLIENT_NOT_INTERESTED");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("CLIENT_NOT_INTERESTED"));

        // Verify lead status is updated to NOT_INTERESTED
        mockMvc.perform(get("/v1/leads/" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("NOT_INTERESTED"));
    }

    // 7. WORK_IN_PROGRESS
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test7_WorkInProgress() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("WORK_IN_PROGRESS"));

        // Verify lead status is updated to WORK_IN_PROGRESS
        mockMvc.perform(get("/v1/leads/" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("WORK_IN_PROGRESS"));
    }

    // 8. Remarks omitted
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test8_RemarksOmitted() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        // Remarks omitted

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remarks").value(nullValue()));
    }

    // 9. Next plan date omitted
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test9_NextPlanDateOmitted() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        // nextPlanDate omitted

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextMeetingDate").value(nullValue()));
    }

    // 10. Location omitted
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test10_LocationOmitted() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latitude").value(nullValue()))
                .andExpect(jsonPath("$.data.longitude").value(nullValue()));
    }

    // 11. Location supplied
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test11_LocationSupplied() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("latitude", 12.9716);
        payload.put("longitude", 77.5946);
        payload.put("accuracy", 10.0);
        payload.put("address", "Bangalore, India");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latitude").value(12.9716))
                .andExpect(jsonPath("$.data.longitude").value(77.5946))
                .andExpect(jsonPath("$.data.locationAccuracy").value(10.0))
                .andExpect(jsonPath("$.data.address").value("Bangalore, India"));
    }

    // 12. Invalid leadStatus
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test12_InvalidLeadStatus() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "INVALID_STATUS_NAME");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    // 13. Invalid aloneWith
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test13_InvalidAloneWith() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "INVALID_VALUE");
        payload.put("leadStatus", "WORK_IN_PROGRESS");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Alone with must be either SELF or SOMEONE."));
    }

    // 14. Invalid/missing required values
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test14_MissingRequiredValues() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        // Test 1: missing aloneWith
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("leadStatus", "WORK_IN_PROGRESS");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Joined with (aloneWith) is mandatory."));

        // Test 2: missing leadStatus
        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Lead Status is mandatory when meeting is conducted."));
    }

    // 15. Meeting not found
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test15_MeetingNotFound() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");

        mockMvc.perform(post("/v1/meetings/MEETINGNONEXISTENT/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("MEETING_NOT_FOUND"));
    }

    // 16. Unauthorized access
    @Test
    public void test16_UnauthorizedAccess() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");

        mockMvc.perform(post("/v1/meetings/MEETING123/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }

    // 17. Verify meeting status becomes CONDUCTED/COMPLETED according to existing workflow
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test17_VerifyStatusCompleted() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.meetingConducted").value("CONDUCTED"));
    }

    // 18. Verify lead status is updated correctly
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test18_VerifyLeadStatusUpdated() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "CONVERTED_CLIENT");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/leads/" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leadStatus").value("CONVERTED"));
    }

    // 19. Verify audit/update history is maintained
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test19_VerifyAuditHistory() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("remarks", "First audit test remarks");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Fetch audit history via the history endpoint
        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode() + "/update-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].remarks").value("First audit test remarks"))
                .andExpect(jsonPath("$.data[0].aloneWith").value("SELF"));
    }

    // 20. Verify dynamic meetingType calculation (INTRO for sequence 1, FOLLOW_UP for sequences > 1)
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test20_VerifyDynamicMeetingTypeCalculation() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting1 = scheduleIntroMeeting(lead.getUniqueLeadId());
        org.junit.jupiter.api.Assertions.assertEquals("INTRO", meeting1.getMeetingType().name());

        // Update sequence 1 with nextPlanDate to create sequence 2
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("aloneWith", "SELF");
        payload1.put("leadStatus", "WORK_IN_PROGRESS");
        payload1.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        String resJson1 = mockMvc.perform(post("/v1/meetings/" + meeting1.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingNumber").value(2))
                .andExpect(jsonPath("$.data.meetingType").value("FOLLOW_UP"))
                .andReturn().getResponse().getContentAsString();

        MeetingResponse meeting2 = objectMapper.readValue(
                objectMapper.readTree(resJson1).path("data").toString(), MeetingResponse.class);
        org.junit.jupiter.api.Assertions.assertEquals("FOLLOW_UP", meeting2.getMeetingType().name());
    }
}
