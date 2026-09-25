package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingType;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MeetingVerificationQueueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Lead testLead;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findAll().stream().findFirst().orElse(null);

        testLead = Lead.builder()
                .leadCode("LD-TEST-" + System.currentTimeMillis())
                .uniqueLeadId(java.util.UUID.randomUUID().toString())
                .clientName("Queue Test Client")
                .mobileNumber("98" + String.format("%08d", (int)(Math.random() * 100000000)))
                .location("Delhi")
                .leadSource(com.blueant_crm_erp.lead.enums.LeadSource.FIELD_VISIT)
                .leadType(com.blueant_crm_erp.lead.enums.LeadType.MUTUAL_FUND)
                .priority(com.blueant_crm_erp.lead.enums.LeadPriority.MEDIUM)
                .duplicateLeadStatus(com.blueant_crm_erp.lead.enums.DuplicateLeadStatus.ORIGINAL)
                .leadStatus(com.blueant_crm_erp.lead.enums.LeadStatus.ASSIGNED)
                .leadStage(com.blueant_crm_erp.lead.enums.LeadStage.LEAD_ASSIGNED)
                .assignedSalesPerson(testUser)
                .build();
        testLead = leadRepository.save(testLead);
    }

    private Meeting createMeetingWithVerification(String meetingCode,
                                                  MeetingStatus meetingStatus,
                                                  MeetingConductStatus conductStatus,
                                                  VerificationStatus verificationStatus,
                                                  int sequence) {
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode).orElse(null);
        if (meeting == null) {
            meeting = Meeting.builder()
                    .meetingCode(meetingCode)
                    .meetingNumber(sequence)
                    .meetingType(sequence == 1 ? MeetingType.INTRO : MeetingType.FOLLOW_UP)
                    .meetingTitle("Meeting #" + sequence)
                    .meetingMode(MeetingMode.PHYSICAL)
                    .meetingDate(LocalDate.now())
                    .meetingTime(LocalTime.of(11, 0))
                    .lead(testLead)
                    .assignedEmployee(testUser)
                    .meetingStatus(meetingStatus)
                    .meetingConducted(conductStatus)
                    .status(Status.ACTIVE)
                    .build();
        } else {
            meeting.setMeetingStatus(meetingStatus);
            meeting.setMeetingConducted(conductStatus);
            meeting.setMeetingDate(LocalDate.now());
            meeting.setMeetingTime(LocalTime.of(11, 0));
            meeting.setStatus(Status.ACTIVE);
            if (testUser != null) {
                meeting.setAssignedEmployee(testUser);
            }
        }
        meeting = meetingRepository.save(meeting);

        if (verificationStatus != null) {
            MeetingVerification verification = meetingVerificationRepository.findByMeetingMeetingCode(meetingCode).orElse(null);
            if (verification == null) {
                verification = MeetingVerification.builder()
                        .meeting(meeting)
                        .verificationStatus(verificationStatus)
                        .meetingConducted(conductStatus)
                        .meetingDate(meeting.getMeetingDate())
                        .meetingTime(meeting.getMeetingTime())
                        .status(Status.ACTIVE)
                        .build();
            } else {
                verification.setVerificationStatus(verificationStatus);
                verification.setMeetingConducted(conductStatus);
                verification.setStatus(Status.ACTIVE);
            }
            verification = meetingVerificationRepository.save(verification);
            meeting.setVerification(verification);
        }

        return meeting;
    }

    @Test
    @DisplayName("Specific Regression Case: NOT_CONDUCTED meeting with PENDING verification is returned in GET /v1/meetings?verificationStatus=PENDING")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_PC_COORDINATOR", "MEETING_READ"})
    public void testSpecificRegressionCase_NotConductedMeetingReturnedInPendingQueue() throws Exception {
        String targetCode = "BA-MTG-2026-001480";
        createMeetingWithVerification(targetCode, MeetingStatus.NOT_CONDUCTED, MeetingConductStatus.NOT_CONDUCTED, VerificationStatus.PENDING, 1);

        MvcResult result = mockMvc.perform(get("/v1/meetings")
                        .param("verificationStatus", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(responseBody, new TypeReference<>() {});
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");

        assertThat(data).isNotEmpty();
        Map<String, Object> found = data.stream()
                .filter(m -> targetCode.equals(m.get("meetingCode")))
                .findFirst()
                .orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.get("meetingCode")).isEqualTo(targetCode);
        assertThat(found.get("meetingStatus")).isEqualTo("NOT_CONDUCTED");
        assertThat(found.get("meetingConducted")).isEqualTo("NOT_CONDUCTED");
        assertThat(found.get("verificationStatus")).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Verification Queue Rules: COMPLETED and NOT_CONDUCTED with PENDING / VERIFIED returned, REJECTED excluded")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_PC_COORDINATOR", "MEETING_READ"})
    public void testVerificationQueueComprehensiveBehavior() throws Exception {
        // 1. COMPLETED + PENDING
        String codeCompletedPending = "BA-MTG-COMP-PEND-" + System.currentTimeMillis();
        createMeetingWithVerification(codeCompletedPending, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.PENDING, 2);

        // 2. NOT_CONDUCTED + PENDING
        String codeNotConductedPending = "BA-MTG-NOTC-PEND-" + System.currentTimeMillis();
        createMeetingWithVerification(codeNotConductedPending, MeetingStatus.NOT_CONDUCTED, MeetingConductStatus.NOT_CONDUCTED, VerificationStatus.PENDING, 3);

        // 3. COMPLETED + VERIFIED
        String codeCompletedVerified = "BA-MTG-COMP-VERI-" + System.currentTimeMillis();
        createMeetingWithVerification(codeCompletedVerified, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.VERIFIED, 4);

        // 4. NOT_CONDUCTED + VERIFIED
        String codeNotConductedVerified = "BA-MTG-NOTC-VERI-" + System.currentTimeMillis();
        createMeetingWithVerification(codeNotConductedVerified, MeetingStatus.NOT_CONDUCTED, MeetingConductStatus.NOT_CONDUCTED, VerificationStatus.VERIFIED, 5);

        // 5. COMPLETED + REJECTED
        String codeCompletedRejected = "BA-MTG-COMP-REJE-" + System.currentTimeMillis();
        createMeetingWithVerification(codeCompletedRejected, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.REJECTED, 6);

        // 6. NOT_CONDUCTED + REJECTED
        String codeNotConductedRejected = "BA-MTG-NOTC-REJE-" + System.currentTimeMillis();
        createMeetingWithVerification(codeNotConductedRejected, MeetingStatus.NOT_CONDUCTED, MeetingConductStatus.NOT_CONDUCTED, VerificationStatus.REJECTED, 7);

        // --- Test PENDING queue ---
        MvcResult pendingResult = mockMvc.perform(get("/v1/meetings")
                        .param("verificationStatus", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> pendingMap = objectMapper.readValue(pendingResult.getResponse().getContentAsString(), new TypeReference<>() {});
        List<Map<String, Object>> pendingData = (List<Map<String, Object>>) pendingMap.get("data");

        List<String> pendingCodes = pendingData.stream().map(m -> (String) m.get("meetingCode")).toList();

        // Must include COMPLETED + PENDING and NOT_CONDUCTED + PENDING
        assertThat(pendingCodes).contains(codeCompletedPending, codeNotConductedPending);

        // Must NOT include VERIFIED or REJECTED
        assertThat(pendingCodes).doesNotContain(codeCompletedVerified, codeNotConductedVerified, codeCompletedRejected, codeNotConductedRejected);

        // --- Test VERIFIED queue ---
        MvcResult verifiedResult = mockMvc.perform(get("/v1/meetings")
                        .param("verificationStatus", "VERIFIED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> verifiedMap = objectMapper.readValue(verifiedResult.getResponse().getContentAsString(), new TypeReference<>() {});
        List<Map<String, Object>> verifiedData = (List<Map<String, Object>>) verifiedMap.get("data");

        List<String> verifiedCodes = verifiedData.stream().map(m -> (String) m.get("meetingCode")).toList();

        // Must include COMPLETED + VERIFIED and NOT_CONDUCTED + VERIFIED
        assertThat(verifiedCodes).contains(codeCompletedVerified, codeNotConductedVerified);

        // Must NOT include PENDING or REJECTED
        assertThat(verifiedCodes).doesNotContain(codeCompletedPending, codeNotConductedPending, codeCompletedRejected, codeNotConductedRejected);

        // Ensure no duplicate records for any meeting code
        long uniquePendingCount = pendingData.stream().map(m -> m.get("meetingCode")).distinct().count();
        assertThat(uniquePendingCount).isEqualTo(pendingData.size());
    }

    @Test
    @DisplayName("Existing queue filters (e.g. status=completed or status=not_conducted) continue to work when verificationStatus is provided")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_PC_COORDINATOR", "MEETING_READ"})
    public void testVerificationQueueWithExplicitStatusFilter() throws Exception {
        String codeCompleted = "BA-MTG-F-COMP-" + System.currentTimeMillis();
        createMeetingWithVerification(codeCompleted, MeetingStatus.COMPLETED, MeetingConductStatus.CONDUCTED, VerificationStatus.PENDING, 8);

        String codeNotConducted = "BA-MTG-F-NOTC-" + System.currentTimeMillis();
        createMeetingWithVerification(codeNotConducted, MeetingStatus.NOT_CONDUCTED, MeetingConductStatus.NOT_CONDUCTED, VerificationStatus.PENDING, 9);

        // Filter status=not_conducted with verificationStatus=PENDING
        MvcResult notCondResult = mockMvc.perform(get("/v1/meetings")
                        .param("verificationStatus", "PENDING")
                        .param("status", "not_conducted")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> notCondMap = objectMapper.readValue(notCondResult.getResponse().getContentAsString(), new TypeReference<>() {});
        List<Map<String, Object>> notCondData = (List<Map<String, Object>>) notCondMap.get("data");
        List<String> notCondCodes = notCondData.stream().map(m -> (String) m.get("meetingCode")).toList();

        assertThat(notCondCodes).contains(codeNotConducted);
        assertThat(notCondCodes).doesNotContain(codeCompleted);

        // Filter status=completed with verificationStatus=PENDING
        MvcResult compResult = mockMvc.perform(get("/v1/meetings")
                        .param("verificationStatus", "PENDING")
                        .param("status", "completed")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> compMap = objectMapper.readValue(compResult.getResponse().getContentAsString(), new TypeReference<>() {});
        List<Map<String, Object>> compData = (List<Map<String, Object>>) compMap.get("data");
        List<String> compCodes = compData.stream().map(m -> (String) m.get("meetingCode")).toList();

        assertThat(compCodes).contains(codeCompleted);
        assertThat(compCodes).doesNotContain(codeNotConducted);
    }
}
