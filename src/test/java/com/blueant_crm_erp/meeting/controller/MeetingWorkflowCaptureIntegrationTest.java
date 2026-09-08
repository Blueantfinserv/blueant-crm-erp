package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
public class MeetingWorkflowCaptureIntegrationTest {

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

    private static long phoneSuffix = 9811000001L;

    private Lead createTestLead(String clientName) {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName(clientName);
        leadRequest.setMobileNumber(String.valueOf(phoneSuffix++));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        leadRequest.setLocation("Delhi NCR");

        LeadResponse response = leadService.createLead(leadRequest, "EMP000001");
        return leadRepository.findByUniqueLeadId(response.getUniqueLeadId()).orElseThrow();
    }

    @Test
    public void testValidCoordinatesAndDocumentsPersisted() throws Exception {
        Lead lead = createTestLead("CaptureTestClient1");

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("remarks", "Meeting conducted with card and GPS captured");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload.put("latitude", 28.5355);
        payload.put("longitude", 77.3910);
        payload.put("accuracy", 12.5);
        payload.put("visitingCard", "/api/v1/documents/101/download");
        payload.put("meetingPhoto", "/api/v1/documents/102/download");

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        Meeting meeting = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId()).get(0);
        assertEquals(MeetingStatus.COMPLETED, meeting.getMeetingStatus());
        assertEquals(MeetingConductStatus.CONDUCTED, meeting.getMeetingConducted());
        assertNotNull(meeting.getLatitude());
        assertEquals(0, meeting.getLatitude().compareTo(BigDecimal.valueOf(28.5355)));
        assertNotNull(meeting.getLongitude());
        assertEquals(0, meeting.getLongitude().compareTo(BigDecimal.valueOf(77.3910)));
        assertEquals(12.5, meeting.getLocationAccuracy());
        assertNotNull(meeting.getLocationCapturedAt());
        assertNotNull(meeting.getGoogleMapsUrl());
        assertTrue(meeting.getGoogleMapsUrl().contains("28.5355"));
        assertEquals("/api/v1/documents/101/download", meeting.getVisitingCard());
        assertEquals("/api/v1/documents/102/download", meeting.getMeetingPhoto());
    }

    @Test
    public void testInvalidLatitudeRejected() throws Exception {
        Lead lead = createTestLead("CaptureTestClient2");

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload.put("latitude", 95.0); // Invalid latitude (> 90)
        payload.put("longitude", 77.0);

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidLongitudeRejected() throws Exception {
        Lead lead = createTestLead("CaptureTestClient3");

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload.put("latitude", 28.0);
        payload.put("longitude", -190.0); // Invalid longitude (< -180)

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testNegativeAccuracyRejected() throws Exception {
        Lead lead = createTestLead("CaptureTestClient4");

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload.put("latitude", 28.0);
        payload.put("longitude", 77.0);
        payload.put("accuracy", -5.0); // Negative accuracy

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPartialCoordinatesRejected() throws Exception {
        Lead lead = createTestLead("CaptureTestClient5");

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload.put("latitude", 28.0);
        // missing longitude

        mockMvc.perform(post("/v1/meetings/NEW_" + lead.getUniqueLeadId() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDangerousFileUploadRejected() throws Exception {
        MockMultipartFile executableFile = new MockMultipartFile(
                "file", "exploit.exe", "application/octet-stream", "dummy binary".getBytes()
        );

        mockMvc.perform(multipart("/v1/documents").file(executableFile))
                .andExpect(status().isBadRequest());
    }
}
