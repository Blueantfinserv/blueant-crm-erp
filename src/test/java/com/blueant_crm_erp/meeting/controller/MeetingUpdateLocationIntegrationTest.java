package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
public class MeetingUpdateLocationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private ObjectMapper objectMapper;

    private Lead createTestLead(String clientName, String location) {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName(clientName);
        leadRequest.setMobileNumber("9354344802");
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        leadRequest.setLocation(location);
        
        LeadResponse response = leadService.createLead(leadRequest, "EMP000001");
        
        Lead lead = leadRepository.findByUniqueLeadId(response.getUniqueLeadId()).orElseThrow();
        if (location == null) {
            // Bypass potential default restrictions in mock setup
            lead.setLocation(null);
            lead = leadRepository.save(lead);
        }
        return lead;
    }

    @Test
    public void testLeadHasLocation_NewMeeting() throws Exception {
        Lead lead = createTestLead("Saurav", "Noida");

        mockMvc.perform(get("/v1/meetings/NEW_" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientName").value("Saurav"))
                .andExpect(jsonPath("$.data.mobileNumber").value("9354344802"))
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));
    }

    @Test
    public void testLeadLocationIsNull_NewMeeting() throws Exception {
        Lead lead = createTestLead("SauravNull", null);

        mockMvc.perform(get("/v1/meetings/NEW_" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientName").value("SauravNull"))
                .andExpect(jsonPath("$.data.mobileNumber").value("9354344802"))
                .andExpect(jsonPath("$.data.meetingLocation").value((Object) null));
    }

    @Test
    public void testIntroMeeting_LocationFallback() throws Exception {
        Lead lead = createTestLead("SauravIntro", "Noida");
        
        // When we open/fetch the pseudo-meeting
        mockMvc.perform(get("/v1/meetings/NEW_" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));

        // Conduct the intro meeting but pass a null meetingLocation to workflow-update
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("meetingRemarks", "Intro meeting completed");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload.put("meetingLocation", null);

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Get the active meeting (which will be follow-up, but let's check the intro meeting which is now completed)
        Meeting completedMeeting = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId()).get(0);
        
        // Assert database meeting location is actually null
        assertNull(completedMeeting.getMeetingLocation());

        // Retrieve it via controller to check the fallback in detail response
        mockMvc.perform(get("/v1/meetings/" + completedMeeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));
    }

    @Test
    public void testFollowUpMeeting_LocationFallback() throws Exception {
        Lead lead = createTestLead("SauravFollowUp", "Noida");

        // 1. Conduct Intro Meeting
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("meetingRemarks", "Intro completed");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // 2. Fetch code of follow-up meeting
        Meeting followUpMeeting = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();
        
        // 3. Retrieve follow-up details and verify fallback
        mockMvc.perform(get("/v1/meetings/" + followUpMeeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));
    }

    @Test
    public void testMultipleMeetings_LocationFallback() throws Exception {
        Lead lead = createTestLead("SauravMulti", "Noida");

        // Conduct 1st meeting (Intro)
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("meetingMode", "PHYSICAL");
        payload1.put("leadStatus", "WORK_IN_PROGRESS");
        payload1.put("aloneWith", "SELF");
        payload1.put("meetingRemarks", "Intro done");
        payload1.put("nextPlanDate", LocalDate.now().plusDays(1).toString());

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload1)))
                .andExpect(status().isOk());

        // Get 2nd meeting
        Meeting meeting2 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();
        
        // Verify 2nd meeting location before conducting is fallback
        mockMvc.perform(get("/v1/meetings/" + meeting2.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));

        // Conduct 2nd meeting
        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("meetingMode", "PHYSICAL");
        payload2.put("leadStatus", "WORK_IN_PROGRESS");
        payload2.put("aloneWith", "SELF");
        payload2.put("meetingRemarks", "First follow up done");
        payload2.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        mockMvc.perform(post("/v1/meetings/" + meeting2.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload2)))
                .andExpect(status().isOk());

        // Get 3rd meeting
        Meeting meeting3 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();

        // Verify 3rd meeting location is fallback
        mockMvc.perform(get("/v1/meetings/" + meeting3.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));
    }

    @Test
    public void testExistingMeetingLocation_NoOverwrite() throws Exception {
        Lead lead = createTestLead("SauravNoOverwrite", "Noida");

        // Conduct meeting with an explicit location "Greater Noida"
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("meetingRemarks", "Intro completed");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload.put("meetingLocation", "Greater Noida");

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Retrieve the completed meeting
        Meeting completedMeeting = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId()).get(0);

        // Verify that the explicitly saved location is returned and NOT overwritten by "Noida"
        mockMvc.perform(get("/v1/meetings/" + completedMeeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Greater Noida"));
    }

    @Test
    public void testUserChangesMeetingLocation_Preserved() throws Exception {
        Lead lead = createTestLead("SauravChangePreserved", "Noida");

        // Conduct meeting with Noida (or let it fallback)
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("meetingMode", "PHYSICAL");
        payload1.put("leadStatus", "WORK_IN_PROGRESS");
        payload1.put("aloneWith", "SELF");
        payload1.put("meetingRemarks", "First attempt");
        payload1.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload1.put("meetingLocation", "Noida");

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload1)))
                .andExpect(status().isOk());

        // Retrieve the scheduled follow-up meeting
        Meeting scheduledFollowUp = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();

        // Verify that initial fallback location is "Noida"
        mockMvc.perform(get("/v1/meetings/" + scheduledFollowUp.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));

        // Now update the scheduled meeting details (change location to "Greater Noida") via PUT /v1/meetings/{meetingCode}
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("meetingCode", scheduledFollowUp.getMeetingCode());
        updatePayload.put("meetingDate", LocalDate.now().plusDays(5).toString());
        updatePayload.put("meetingMode", "PHYSICAL");
        updatePayload.put("meetingLocation", "Greater Noida");

        mockMvc.perform(put("/v1/meetings/" + scheduledFollowUp.getMeetingCode())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk());

        // Retrieve and verify that the updated location "Greater Noida" is preserved
        mockMvc.perform(get("/v1/meetings/" + scheduledFollowUp.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Greater Noida"));
    }
}
