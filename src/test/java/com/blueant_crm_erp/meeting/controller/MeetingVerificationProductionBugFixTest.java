package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingType;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MeetingVerificationProductionBugFixTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Meeting createTestMeeting(String meetingCode, MeetingStatus meetingStatus, MeetingConductStatus conductStatus, VerificationStatus verificationStatus) {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("BugFix Test Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResp = leadService.createLead(leadRequest, "EMP000001");
        Lead lead = leadRepository.findByUniqueLeadId(leadResp.getUniqueLeadId()).orElseThrow();

        Meeting meeting = Meeting.builder()
                .meetingCode(meetingCode)
                .meetingNumber(1)
                .meetingType(MeetingType.INTRO)
                .meetingTitle("Intro Meeting")
                .lead(lead)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(10, 0))
                .meetingStatus(meetingStatus)
                .status(Status.ACTIVE)
                .meetingConducted(conductStatus)
                .build();

        Meeting saved = meetingRepository.save(meeting);

        if (verificationStatus != null) {
            MeetingVerification mv = MeetingVerification.builder()
                    .meeting(saved)
                    .verificationStatus(verificationStatus)
                    .build();
            meetingVerificationRepository.save(mv);
            saved.setVerification(mv);
        }

        return saved;
    }

    // A. Existing valid meeting: BA-MTG-2026-000020 COMPLETED, CONDUCTED, ACTIVE, PENDING
    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"MEETING_VERIFY", "ROLE_SALES_COORDINATOR"})
    public void testA_ValidMeetingVerification() throws Exception {
        String code = "BA-MTG-2026-TEST00020";
        createTestMeeting(code, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.PENDING);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingTiming", "15:30:00");
        payload.put("ageGroup", "AGE_35_45");
        payload.put("existingSip", "YES");
        payload.put("profession", "DOCTOR");
        payload.put("professionDetail", "Cardiologist");
        payload.put("bestTimeForMeeting", "EVENING");
        payload.put("meetingWith", "SOMEONE_ELSE");
        payload.put("personName", "Amit Sharma");
        payload.put("position", "Client");

        // Test normal lookup
        mockMvc.perform(post("/v1/meetings/verification/" + code + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingCode").value(code))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    // A2. Exact production code BA-MTG-2026-000020 from database with whitespace & case variations
    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"MEETING_VERIFY", "ROLE_SALES_COORDINATOR"})
    public void testA2_ExactProductionMeeting_BA_MTG_2026_000020() throws Exception {
        String prodCode = "BA-MTG-2026-000020";

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingTiming", "15:30:00");
        payload.put("ageGroup", "AGE_35_45");
        payload.put("existingSip", "YES");
        payload.put("profession", "DOCTOR");
        payload.put("professionDetail", "Cardiologist");
        payload.put("bestTimeForMeeting", "EVENING");
        payload.put("meetingWith", "SOMEONE_ELSE");
        payload.put("personName", "Amit Sharma");
        payload.put("position", "Client");

        // Test with exact production code
        mockMvc.perform(post("/v1/meetings/verification/" + prodCode + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingCode").value(prodCode))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    // B. Nonexistent meeting code -> 404
    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"MEETING_VERIFY", "ROLE_SALES_COORDINATOR"})
    public void testB_NonexistentMeetingCode_Returns404() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingTiming", "15:30:00");

        mockMvc.perform(post("/v1/meetings/verification/BA-MTG-9999-999999/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    // C. Scheduled meeting -> existing business validation rejects with 400
    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"MEETING_VERIFY", "ROLE_SALES_COORDINATOR"})
    public void testC_ScheduledMeeting_RejectedWith400() throws Exception {
        String code = "BA-MTG-2026-TEST00021";
        createTestMeeting(code, MeetingStatus.SCHEDULED, MeetingConductStatus.NOT_CONDUCTED, null);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingTiming", "15:30:00");

        mockMvc.perform(post("/v1/meetings/verification/" + code + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Meeting must be completed."));
    }

    // D. Already VERIFIED meeting -> existing behavior must remain unchanged (400 validation error)
    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"MEETING_VERIFY", "ROLE_SALES_COORDINATOR"})
    public void testD_AlreadyVerifiedMeeting_RejectedWith400() throws Exception {
        String code = "BA-MTG-2026-TEST00022";
        createTestMeeting(code, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.VERIFIED);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingTiming", "15:30:00");

        mockMvc.perform(post("/v1/meetings/verification/" + code + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Meeting verification status must be PENDING."));
    }

    // E. REJECTED / PENDING behavior
    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"MEETING_VERIFY", "ROLE_SALES_COORDINATOR"})
    public void testE_RejectMeetingFlow() throws Exception {
        String code = "BA-MTG-2026-TEST00023";
        createTestMeeting(code, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.PENDING);

        mockMvc.perform(post("/v1/meetings/verification/" + code + "/reject")
                .param("reason", "Client unreachable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"));

        // Verify that verifying an already REJECTED meeting now rejects
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingTiming", "15:30:00");
        mockMvc.perform(post("/v1/meetings/verification/" + code + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Meeting verification status must be PENDING."));
    }

    // F. SOMEONE_ELSE mapping: meetingWith=SOMEONE_ELSE -> aloneWith=SOMEONE
    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"MEETING_VERIFY", "ROLE_SALES_COORDINATOR"})
    public void testF_SomeoneElseMapping() throws Exception {
        String code = "BA-MTG-2026-TEST00024";
        createTestMeeting(code, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.PENDING);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingTiming", "15:30:00");
        payload.put("meetingWith", "SOMEONE_ELSE");
        payload.put("personName", "Amit Sharma");
        payload.put("position", "Client");

        mockMvc.perform(post("/v1/meetings/verification/" + code + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aloneWith").value("SOMEONE"))
                .andExpect(jsonPath("$.personName").value("Amit Sharma"))
                .andExpect(jsonPath("$.position").value("Client"));
    }

    // G. SELF mapping: meetingWith=SELF -> personName and position normalized to null
    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"MEETING_VERIFY", "ROLE_SALES_COORDINATOR"})
    public void testG_SelfMapping_NormalizesPersonAndPosition() throws Exception {
        String code = "BA-MTG-2026-TEST00025";
        createTestMeeting(code, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.PENDING);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingTiming", "15:30:00");
        payload.put("meetingWith", "SELF");
        payload.put("personName", "Should Be Cleared");
        payload.put("position", "Should Be Cleared");

        mockMvc.perform(post("/v1/meetings/verification/" + code + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aloneWith").value("SELF"))
                .andExpect(jsonPath("$.personName").isEmpty())
                .andExpect(jsonPath("$.position").isEmpty());
    }

    // H. Existing authorization: unauthorized user gets 403
    @Test
    @WithMockUser(username = "salesperson@blueant.com", authorities = {"ROLE_SALES_PERSON"})
    public void testH_UnauthorizedUser_Forbidden403() throws Exception {
        String code = "BA-MTG-2026-TEST00026";
        createTestMeeting(code, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.PENDING);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingTiming", "15:30:00");

        mockMvc.perform(post("/v1/meetings/verification/" + code + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }
}
