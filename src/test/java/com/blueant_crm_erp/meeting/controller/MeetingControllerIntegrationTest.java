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
}
