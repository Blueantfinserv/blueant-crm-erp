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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MeetingWorkflowRedesignTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private ObjectMapper objectMapper;

    private LeadResponse createTestLead() {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Workflow Redesign Test Client");
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
        scheduleRequest.setMeetingLocation("Office Room A");
        return meetingScheduleService.scheduleMeeting(scheduleRequest, "EMP000001");
    }

    // TEST A: No meetingConducted field + no GPS + no remarks + WORK_IN_PROGRESS + no nextPlanDate
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testA_NoConductedNoGpsNoRemarksWipNoNextPlanDate() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TEST B: No meetingConducted field + no GPS + ALREADY_CLIENT
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testB_NoConductedNoGpsAlreadyClient() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "ALREADY_CLIENT");
        payload.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TEST C: No meetingConducted field + no GPS + CONVERTED_CLIENT
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testC_NoConductedNoGpsConvertedClient() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "CONVERTED_CLIENT");
        payload.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TEST D: CLIENT_REMOVED without reason
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testD_ClientRemovedWithoutReason() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "CLIENT_REMOVED");
        payload.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TEST E: CLIENT_NOT_INTERESTED without reason
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testE_ClientNotInterestedWithoutReason() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "CLIENT_NOT_INTERESTED");
        payload.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TEST F: WORK_IN_PROGRESS without nextPlanDate
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testF_WipWithoutNextPlanDate() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"));

        // Verify that no second scheduled meeting exists
        mockMvc.perform(get("/v1/meetings/lead/" + lead.getUniqueLeadId() + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingCode").isEmpty());
    }

    // TEST G: WORK_IN_PROGRESS with nextPlanDate
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testG_WipWithNextPlanDate() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("nextPlanDate", LocalDate.now().plusDays(5).toString());
        payload.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meetingStatus").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.meetingNumber").value(2));
    }

    // TEST H: Location completely absent
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testH_LocationCompletelyAbsent() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latitude").value(nullValue()))
                .andExpect(jsonPath("$.data.longitude").value(nullValue()))
                .andExpect(jsonPath("$.data.locationAccuracy").value(nullValue()))
                .andExpect(jsonPath("$.data.address").value(nullValue()))
                .andExpect(jsonPath("$.data.locationCapturedAt").value(nullValue()))
                .andExpect(jsonPath("$.data.googleMapsUrl").value(nullValue()));
    }

    // TEST I: Location provided
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testI_LocationProvided() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("latitude", 12.345678);
        payload.put("longitude", 98.765432);
        payload.put("accuracy", 5.5);
        payload.put("address", "Tech Park");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latitude").value(12.345678))
                .andExpect(jsonPath("$.data.longitude").value(98.765432))
                .andExpect(jsonPath("$.data.locationAccuracy").value(5.5))
                .andExpect(jsonPath("$.data.address").value("Tech Park"));
    }

    // TEST J: meetingConducted explicitly = NOT_CONDUCTED
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testJ_MeetingConductedExplicitlyNotConducted() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingConducted", "NOT_CONDUCTED");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    // TEST: aloneWith is SELF but personName/position provided (should reject)
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testK_AloneWithSelfWithDetailsRejects() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("personName", "Mr. X");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    // TEST: Duplicate active meeting scheduling is blocked
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testL_DuplicateActiveMeetingBlocks() throws Exception {
        LeadResponse lead = createTestLead();
        scheduleIntroMeeting(lead.getUniqueLeadId());

        CreateMeetingRequest duplicateRequest = new CreateMeetingRequest();
        duplicateRequest.setLeadId(java.util.UUID.fromString(lead.getUniqueLeadId()));
        duplicateRequest.setMeetingMode(MeetingMode.ONLINE);
        duplicateRequest.setMeetingDate(LocalDate.now().plusDays(2));
        duplicateRequest.setMeetingTime(LocalTime.of(11, 0));
        duplicateRequest.setMeetingLocation("Zoom Link");

        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }
}
