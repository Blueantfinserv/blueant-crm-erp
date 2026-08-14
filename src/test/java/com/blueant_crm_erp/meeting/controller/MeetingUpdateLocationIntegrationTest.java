package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
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
            lead.setLocation(null);
            lead = leadRepository.save(lead);
        }
        return lead;
    }

    // TEST 1: Lead location = "Noida", INTRO meeting meetingLocation = null -> Expected: meetingLocation = "Noida"
    @Test
    public void test1_LeadHasLocation_IntroMeetingNullInDb_FallsBackToLeadLocation() throws Exception {
        Lead lead = createTestLead("SauravTest1", "Noida");

        // The first NEW_ meeting (representing intro meeting) is opened
        mockMvc.perform(get("/v1/meetings/NEW_" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));
    }

    // TEST 2: Lead location = "Noida", FOLLOW_UP #1 meetingLocation = null -> Expected: meetingLocation = "Noida"
    @Test
    public void test2_LeadHasLocation_FollowUp1NullInDb_FallsBackToLeadLocation() throws Exception {
        Lead lead = createTestLead("SauravTest2", "Noida");

        // 1. Conduct Intro Meeting to generate Follow Up #1 (Meeting #2)
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("meetingRemarks", "Intro conducted");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // 2. Fetch follow up meeting (1st Meeting)
        Meeting followUp1 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();
        assertNull(followUp1.getMeetingLocation()); // In DB, location is null

        // 3. Verify fallback in GET /v1/meetings/{meetingCode}
        mockMvc.perform(get("/v1/meetings/" + followUp1.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));

        // 4. Verify fallback in GET /v1/meetings/lead/{leadId}/journey (timeline)
        mockMvc.perform(get("/v1/meetings/lead/" + lead.getUniqueLeadId() + "/journey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[1].location").value("Noida"));
    }

    // TEST 3: Lead location = "Noida", FOLLOW_UP #2 meetingLocation = null -> Expected: meetingLocation = "Noida"
    @Test
    public void test3_LeadHasLocation_FollowUp2NullInDb_FallsBackToLeadLocation() throws Exception {
        Lead lead = createTestLead("SauravTest3", "Noida");

        // Conduct Intro Meeting -> schedules Follow Up #1
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("meetingMode", "PHYSICAL");
        payload1.put("leadStatus", "WORK_IN_PROGRESS");
        payload1.put("aloneWith", "SELF");
        payload1.put("meetingRemarks", "Intro conducted");
        payload1.put("nextPlanDate", LocalDate.now().plusDays(1).toString());

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload1)))
                .andExpect(status().isOk());

        Meeting followUp1 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();

        // Conduct Follow Up #1 -> schedules Follow Up #2
        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("meetingMode", "PHYSICAL");
        payload2.put("leadStatus", "WORK_IN_PROGRESS");
        payload2.put("aloneWith", "SELF");
        payload2.put("meetingRemarks", "Follow Up #1 conducted");
        payload2.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        mockMvc.perform(post("/v1/meetings/" + followUp1.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload2)))
                .andExpect(status().isOk());

        Meeting followUp2 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();
        assertNull(followUp2.getMeetingLocation());

        // Verify fallback
        mockMvc.perform(get("/v1/meetings/" + followUp2.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));
    }

    // TEST 4: Lead location = "Noida", FOLLOW_UP #3 meetingLocation = null -> Expected: meetingLocation = "Noida"
    @Test
    public void test4_LeadHasLocation_FollowUp3NullInDb_FallsBackToLeadLocation() throws Exception {
        Lead lead = createTestLead("SauravTest4", "Noida");

        // Conduct Intro Meeting
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("meetingMode", "PHYSICAL");
        payload1.put("leadStatus", "WORK_IN_PROGRESS");
        payload1.put("aloneWith", "SELF");
        payload1.put("nextPlanDate", LocalDate.now().plusDays(1).toString());

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload1)));

        Meeting followUp1 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();

        // Conduct Follow Up #1
        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("meetingMode", "PHYSICAL");
        payload2.put("leadStatus", "WORK_IN_PROGRESS");
        payload2.put("aloneWith", "SELF");
        payload2.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        mockMvc.perform(post("/v1/meetings/" + followUp1.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload2)));

        Meeting followUp2 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();

        // Conduct Follow Up #2
        Map<String, Object> payload3 = new HashMap<>();
        payload3.put("meetingMode", "PHYSICAL");
        payload3.put("leadStatus", "WORK_IN_PROGRESS");
        payload3.put("aloneWith", "SELF");
        payload3.put("nextPlanDate", LocalDate.now().plusDays(3).toString());

        mockMvc.perform(post("/v1/meetings/" + followUp2.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload3)));

        Meeting followUp3 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();
        assertNull(followUp3.getMeetingLocation());

        // Verify fallback
        mockMvc.perform(get("/v1/meetings/" + followUp3.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));
    }

    // TEST 5: Existing Meeting has meetingLocation = "Greater Noida", Lead.location = "Noida" -> Expected: meetingLocation = "Greater Noida"
    @Test
    public void test5_SavedMeetingLocationWins_NoOverwrite() throws Exception {
        Lead lead = createTestLead("SauravTest5", "Noida");

        // Conduct meeting with an explicit location "Greater Noida"
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("meetingRemarks", "Completed with new location");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload.put("meetingLocation", "Greater Noida");

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        Meeting completedMeeting = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId()).get(0);

        // Verify that the explicitly saved location is returned and NOT overwritten by "Noida" in DetailResponse
        mockMvc.perform(get("/v1/meetings/" + completedMeeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Greater Noida"));

        // Verify that the explicitly saved location is returned in SummaryResponse (journey timeline)
        mockMvc.perform(get("/v1/meetings/lead/" + lead.getUniqueLeadId() + "/journey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].location").value("Greater Noida"));
    }

    // TEST 6: Lead.location = null, Meeting.meetingLocation = null -> Expected: meetingLocation = null (No exception)
    @Test
    public void test6_LeadLocationAndMeetingLocationNull_ReturnsNull_NoException() throws Exception {
        Lead lead = createTestLead("SauravTest6", null);

        mockMvc.perform(get("/v1/meetings/NEW_" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value((Object) null));
    }

    // TEST 7: Meeting Update changes meetingLocation = "Greater Noida" -> Expected: meetingLocation = "Greater Noida"
    @Test
    public void test7_MeetingUpdateChangesLocation_Preserved() throws Exception {
        Lead lead = createTestLead("SauravTest7", "Noida");

        // Conduct Intro meeting -> schedules Follow Up #1
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("meetingMode", "PHYSICAL");
        payload1.put("leadStatus", "WORK_IN_PROGRESS");
        payload1.put("aloneWith", "SELF");
        payload1.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload1)));

        Meeting followUp1 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();

        // Update follow Up #1 location using PUT to "Greater Noida"
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("meetingCode", followUp1.getMeetingCode());
        updatePayload.put("meetingDate", LocalDate.now().plusDays(3).toString());
        updatePayload.put("meetingMode", "PHYSICAL");
        updatePayload.put("meetingLocation", "Greater Noida");

        mockMvc.perform(put("/v1/meetings/" + followUp1.getMeetingCode())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk());

        // Assert location update is saved and preserved
        mockMvc.perform(get("/v1/meetings/" + followUp1.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Greater Noida"));
    }

    // TEST 8: Meeting Update Form data for every meeting in the sequence must contain: clientName, mobileNumber, meetingLocation
    @Test
    public void test8_MeetingUpdateFormDataContainsExpectedFields() throws Exception {
        Lead lead = createTestLead("SauravTest8", "Noida");

        mockMvc.perform(get("/v1/meetings/NEW_" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clientName").value("SauravTest8"))
                .andExpect(jsonPath("$.data.mobileNumber").value("9354344802"))
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));
    }

    // TEST 9: Verify INTRO and FOLLOW_UP meetings use the same correct location resolution behavior.
    @Test
    public void test9_VerifyIntroAndFollowUpUseSameCorrectLocationResolution() throws Exception {
        Lead lead = createTestLead("SauravTest9", "Noida");

        // Intro dummy GET check
        mockMvc.perform(get("/v1/meetings/NEW_" + lead.getUniqueLeadId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));

        // Conduct Intro
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)));

        // Follow Up scheduled GET check
        Meeting followUp1 = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED).orElseThrow();
        mockMvc.perform(get("/v1/meetings/" + followUp1.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingLocation").value("Noida"));
    }

    // TEST 10: Verify Meeting status/conducted fields returned by backend match database values.
    @Test
    public void test10_VerifyMeetingStatusAndConductedFieldsReturnedByBackendMatchDatabase() throws Exception {
        Lead lead = createTestLead("SauravTest10", "Noida");

        // Conduct intro meeting
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        Meeting completedIntro = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId()).get(0);
        
        // Assert database values
        assertEquals(MeetingStatus.COMPLETED, completedIntro.getMeetingStatus());
        assertEquals(MeetingConductStatus.CONDUCTED, completedIntro.getMeetingConducted());

        // Verify backend returned values via GET endpoint match database
        mockMvc.perform(get("/v1/meetings/" + completedIntro.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.meetingConducted").value("CONDUCTED"));
    }
}
