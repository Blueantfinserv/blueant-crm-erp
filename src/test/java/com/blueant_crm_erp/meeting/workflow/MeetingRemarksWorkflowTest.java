package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.InvestmentType;
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

import java.math.BigDecimal;
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
public class MeetingRemarksWorkflowTest {

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
        leadRequest.setClientName("Remarks Test Client");
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
        return meetingScheduleService.scheduleMeeting(scheduleRequest, "EMP000001");
    }

    // A. WORK_IN_PROGRESS with Custom remarks
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testRemarks_WorkInProgress_CustomRemarkPropagation() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        String customRemark = "Client requested another meeting after discussing investment options";

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("remarks", customRemark);
        payload.put("nextPlanDate", LocalDate.now().plusDays(3).toString());
        payload.put("nextPlanTime", "15:00:00");

        // 1. Verify response of the workflow update
        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Verify current completed meeting has the exact remark in details response
        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remarks").value(customRemark));

        // 3. Verify MeetingUpdate audit history contains the exact remark
        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode() + "/update-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].remarks").value(customRemark));

        // 4. Verify follow-up meeting is created and has the exact same remark
        String nextMeetingJson = mockMvc.perform(get("/v1/meetings/lead/" + lead.getUniqueLeadId() + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingCode").value(notNullValue()))
                .andReturn().getResponse().getContentAsString();

        String nextMeetingCode = objectMapper.readTree(nextMeetingJson).path("data").path("meetingCode").asText();

        mockMvc.perform(get("/v1/meetings/" + nextMeetingCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remarks").value(customRemark));
    }

    // B. CLIENT_NOT_INTERESTED
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testRemarks_ClientNotInterested() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        String remark = "Client is not interested";

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "CLIENT_NOT_INTERESTED");
        payload.put("remarks", remark);

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remarks").value(remark));

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode() + "/update-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].remarks").value(remark));

        // No active meeting scheduled anymore
        mockMvc.perform(get("/v1/meetings/lead/" + lead.getUniqueLeadId() + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingCode").value(nullValue()));
    }

    // C. ALREADY_BLUEANT_CLIENT
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testRemarks_AlreadyBlueAntClient() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        String remark = "Already BlueAnt client";

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "ALREADY_CLIENT");
        payload.put("remarks", remark);

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remarks").value(remark));

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode() + "/update-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].remarks").value(remark));
    }

    // D. CONVERTED_CLIENT
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testRemarks_ConvertedClient() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        String remark = "Client converted successfully";

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "CONVERTED_CLIENT");
        payload.put("remarks", remark);
        payload.put("panNumber", "ABCDE1234F");
        payload.put("investmentAmount", 50000.00);
        payload.put("investmentType", "SIP");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remarks").value(remark));

        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode() + "/update-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].remarks").value(remark));
    }

    // E. Backward Compatibility with legacy frontend clients (sending meetingRemarks)
    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testRemarks_LegacyFieldCompatibility() throws Exception {
        LeadResponse lead = createTestLead();
        MeetingResponse meeting = scheduleIntroMeeting(lead.getUniqueLeadId());

        String legacyRemark = "Compatibility test legacy remark text";

        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("meetingRemarks", legacyRemark); // Legacy field
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        // 1. Verify JSON alias maps meetingRemarks to remarks internally
        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // 2. Current meeting should receive legacyRemark
        mockMvc.perform(get("/v1/meetings/" + meeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remarks").value(legacyRemark));

        // 3. Follow-up meeting should also receive legacyRemark
        String nextMeetingJson = mockMvc.perform(get("/v1/meetings/lead/" + lead.getUniqueLeadId() + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingCode").value(notNullValue()))
                .andReturn().getResponse().getContentAsString();

        String nextMeetingCode = objectMapper.readTree(nextMeetingJson).path("data").path("meetingCode").asText();

        mockMvc.perform(get("/v1/meetings/" + nextMeetingCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remarks").value(legacyRemark));
    }
}
