package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MeetingVerificationDateMappingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private ProcessCoordinatorService processCoordinatorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static long phoneSuffix = 9811000001L;

    private Lead createTestLeadWithSalesPerson(String clientName) {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName(clientName);
        leadRequest.setMobileNumber(String.valueOf(phoneSuffix++));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        leadRequest.setLocation("Barakhamba Road, New Delhi");

        LeadResponse response = leadService.createLead(leadRequest, "EMP000001");
        Lead lead = leadRepository.findByUniqueLeadId(response.getUniqueLeadId()).orElseThrow();

        User salesPerson = userRepository.findByEmployeeCodeIgnoreCase("EMP000001").orElse(null);
        if (salesPerson != null) {
            lead.setAssignedSalesPerson(salesPerson);
            leadRepository.save(lead);
        }
        return lead;
    }

    /**
     * Helper to set up the scenario:
     * Intro meeting on 2026-09-23 with nextPlanDate = 2026-09-28.
     * Intro meeting is conducted and verified by Coordinator.
     * Follow-up meeting is created with scheduled date 2026-09-28.
     */
    private Meeting[] setupIntroAndFollowUpScenario() throws Exception {
        Lead lead = createTestLeadWithSalesPerson("Date Mapping Test Lead");

        // 1. Conduct Intro Meeting on 2026-09-23 with planned follow-up on 2026-09-28
        MeetingWorkflowRequest introWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 23))
                .meetingTime(LocalTime.of(10, 0))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingLocation("Connaught Place, New Delhi")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Intro meeting conducted. Follow-up planned.")
                .nextPlanDate(LocalDate.of(2026, 9, 28))
                .nextPlanTime(LocalTime.of(11, 0))
                .build();

        meetingService.processMeetingUpdateWorkflow("NEW_" + lead.getUniqueLeadId(), introWorkflowReq, "EMP000001");

        // Intro meeting is meeting #1
        Meeting introMeeting = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId()).get(0);
        assertThat(introMeeting.getMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 23));
        assertThat(introMeeting.getNextMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 28));

        // 2. Coordinator verifies Intro Meeting
        MeetingVerificationRequest introVerifyReq = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 23))
                .meetingTiming(LocalTime.of(10, 0))
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .profession("SALARIED_EMPLOYEE")
                .professionDetail("Software Engineer")
                .bestTimeForMeeting("MORNING")
                .meetingWith("SELF")
                .remarks("Verified Intro meeting")
                .build();
        processCoordinatorService.verifyMeeting(introMeeting.getMeetingCode(), introVerifyReq, "coordinator@blueant.com");

        // 3. Follow-up meeting (meeting #2) was automatically created
        Meeting followUpMeeting = meetingRepository.findByLeadIdAndMeetingNumber(lead.getId(), 2)
                .orElseThrow(() -> new IllegalStateException("Follow-up meeting #2 should have been created"));
        assertThat(followUpMeeting.getMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 28));
        assertThat(followUpMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);

        return new Meeting[]{introMeeting, followUpMeeting};
    }

    @Test
    @DisplayName("TEST 1: Current follow-up meeting receives its own actual meetingDate (2026-09-25) and NOT 2026-09-28")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void test1_FollowUpMeetingReceivesOwnMeetingDateNotPreviousNextDate() throws Exception {
        Meeting[] meetings = setupIntroAndFollowUpScenario();
        Meeting followUpMeeting = meetings[1];

        // Sales Person conducts the follow-up meeting early on 2026-09-25 with next follow-up on 2026-09-30
        MeetingWorkflowRequest followUpWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .meetingTime(LocalTime.of(14, 0))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingLocation("Gurgaon Hub")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Follow-up conducted early. Client wants proposal.")
                .nextPlanDate(LocalDate.of(2026, 9, 30))
                .nextPlanTime(LocalTime.of(15, 0))
                .build();

        meetingService.processMeetingUpdateWorkflow(followUpMeeting.getMeetingCode(), followUpWorkflowReq, "EMP000001");

        // Verify that Coordinator query via GET /v1/meetings/verification/{meetingCode} receives meetingDate = 2026-09-25 and NOT 2026-09-28
        mockMvc.perform(get("/v1/meetings/verification/" + followUpMeeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingCode").value(followUpMeeting.getMeetingCode()))
                .andExpect(jsonPath("$.meetingDate").value("2026-09-25"))
                .andExpect(jsonPath("$.nextMeetingDate").value("2026-09-30"));

        // Also verify GET /v1/meetings/{meetingCode} returns meetingDate = 2026-09-25
        mockMvc.perform(get("/v1/meetings/" + followUpMeeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingDate").value("2026-09-25"))
                .andExpect(jsonPath("$.data.nextMeetingDate").value("2026-09-30"));
    }

    @Test
    @DisplayName("TEST 2: Coordinator verification with meetingDate (2026-09-25) succeeds on later date")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void test2_CoordinatorVerificationWithMeetingDateOnLaterDateSucceeds() throws Exception {
        Meeting[] meetings = setupIntroAndFollowUpScenario();
        Meeting followUpMeeting = meetings[1];

        MeetingWorkflowRequest followUpWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Conducted early")
                .nextPlanDate(LocalDate.of(2026, 9, 30))
                .build();
        meetingService.processMeetingUpdateWorkflow(followUpMeeting.getMeetingCode(), followUpWorkflowReq, "EMP000001");

        Map<String, Object> verifyPayload = new HashMap<>();
        verifyPayload.put("meetingDate", "2026-09-25");
        verifyPayload.put("meetingTiming", "15:30:00");
        verifyPayload.put("ageGroup", "AGE_25_35");
        verifyPayload.put("existingSip", "YES");
        verifyPayload.put("profession", "DOCTOR");
        verifyPayload.put("professionDetail", "Cardiologist");
        verifyPayload.put("bestTimeForMeeting", "EVENING");
        verifyPayload.put("meetingWith", "SOMEONE_ELSE");
        verifyPayload.put("personName", "Amit Sharma");
        verifyPayload.put("position", "Client");

        mockMvc.perform(post("/v1/meetings/verification/" + followUpMeeting.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingCode").value(followUpMeeting.getMeetingCode()))
                .andExpect(jsonPath("$.meetingDate").value("2026-09-25"))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        Meeting updatedMeeting = meetingRepository.findByMeetingCode(followUpMeeting.getMeetingCode()).orElseThrow();
        assertThat(updatedMeeting.getMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 25));

        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(followUpMeeting.getId()).orElseThrow();
        assertThat(verification.getMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 25));
        assertThat(verification.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
    }

    @Test
    @DisplayName("TEST 3: Past meetingDate must NOT fail because it is before verification date/current date")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void test3_PastMeetingDateDoesNotFail() throws Exception {
        Meeting[] meetings = setupIntroAndFollowUpScenario();
        Meeting followUpMeeting = meetings[1];

        // Meeting happened well in the past (e.g., 10 days ago)
        LocalDate pastDate = LocalDate.now().minusDays(10);

        MeetingWorkflowRequest followUpWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(pastDate)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Conducted in past")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .build();
        meetingService.processMeetingUpdateWorkflow(followUpMeeting.getMeetingCode(), followUpWorkflowReq, "EMP000001");

        // Coordinator submits past meeting date - must succeed and not be rejected by any future-date validation
        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .meetingDate(pastDate)
                .meetingTiming(LocalTime.of(16, 0))
                .ageGroup("AGE_25_35")
                .existingSip("NO")
                .bestTimeForMeeting("AFTERNOON")
                .meetingWith("SELF")
                .remarks("Past date verification allowed")
                .build();

        mockMvc.perform(post("/v1/meetings/verification/" + followUpMeeting.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingDate").value(pastDate.toString()))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(followUpMeeting.getId()).orElseThrow();
        assertThat(verification.getMeetingDate()).isEqualTo(pastDate);
    }

    @Test
    @DisplayName("TEST 4: Existing verification questions continue to work exactly as before")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void test4_ExistingVerificationQuestionsWorkAsBefore() throws Exception {
        Meeting[] meetings = setupIntroAndFollowUpScenario();
        Meeting followUpMeeting = meetings[1];

        MeetingWorkflowRequest followUpWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Conducted early")
                .nextPlanDate(LocalDate.of(2026, 9, 30))
                .build();
        meetingService.processMeetingUpdateWorkflow(followUpMeeting.getMeetingCode(), followUpWorkflowReq, "EMP000001");

        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .meetingTiming(LocalTime.of(15, 30))
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .profession("DOCTOR")
                .professionDetail("Cardiologist")
                .bestTimeForMeeting("EVENING")
                .meetingWith("SOMEONE_ELSE")
                .personName("Amit Sharma")
                .position("Client")
                .clientAge(32)
                .maritalStatus("MARRIED")
                .email("amit.sharma@example.com")
                .companyName("Apollo Hospitals")
                .anyChildren(true)
                .numberOfChildren(1)
                .previousInvestment(true)
                .remarks("Complete questionnaire answers verified.")
                .build();

        mockMvc.perform(post("/v1/meetings/verification/" + followUpMeeting.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk());

        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(followUpMeeting.getId()).orElseThrow();
        assertThat(verification.getMeetingTiming()).isEqualTo(LocalTime.of(15, 30));
        assertThat(verification.getAgeGroup().name()).isEqualTo("AGE_25_35");
        assertThat(verification.getExistingSip().name()).isEqualTo("YES");
        assertThat(verification.getProfession()).isEqualTo("DOCTOR");
        assertThat(verification.getProfessionDetail()).isEqualTo("Cardiologist");
        assertThat(verification.getBestTimeForMeeting().name()).isEqualTo("EVENING");
        assertThat(verification.getAloneWith()).isEqualTo("SOMEONE");
        assertThat(verification.getPersonName()).isEqualTo("Amit Sharma");
        assertThat(verification.getPosition()).isEqualTo("Client");
        assertThat(verification.getClientAge()).isEqualTo(32);
        assertThat(verification.getMaritalStatus()).isEqualTo("MARRIED");
        assertThat(verification.getEmail()).isEqualTo("amit.sharma@example.com");
        assertThat(verification.getCompanyName()).isEqualTo("Apollo Hospitals");
        assertThat(verification.getAnyChildren()).isTrue();
        assertThat(verification.getNumberOfChildren()).isEqualTo(1);
        assertThat(verification.getPreviousInvestment()).isTrue();
    }

    @Test
    @DisplayName("TEST 5: verifiedAt remains verification timestamp and is not overwritten by meetingDate")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void test5_VerifiedAtRemainsTimestampAndNotOverwrittenByMeetingDate() throws Exception {
        Meeting[] meetings = setupIntroAndFollowUpScenario();
        Meeting followUpMeeting = meetings[1];

        MeetingWorkflowRequest followUpWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Conducted early")
                .nextPlanDate(LocalDate.of(2026, 9, 30))
                .build();
        meetingService.processMeetingUpdateWorkflow(followUpMeeting.getMeetingCode(), followUpWorkflowReq, "EMP000001");

        LocalDateTime beforeVerification = LocalDateTime.now().minusSeconds(1);

        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .meetingTiming(LocalTime.of(15, 30))
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .bestTimeForMeeting("EVENING")
                .meetingWith("SELF")
                .build();

        mockMvc.perform(post("/v1/meetings/verification/" + followUpMeeting.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk());

        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(followUpMeeting.getId()).orElseThrow();
        assertThat(verification.getMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 25));
        assertThat(verification.getVerifiedAt()).isAfterOrEqualTo(beforeVerification);
        assertThat(verification.getVerifiedAt().toLocalDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("TEST 6: Previous Intro meeting dates remain unchanged")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void test6_PreviousIntroMeetingDatesRemainUnchanged() throws Exception {
        Meeting[] meetings = setupIntroAndFollowUpScenario();
        Meeting introMeeting = meetings[0];
        Meeting followUpMeeting = meetings[1];

        // Sales Person conducts follow-up on 2026-09-25 with next follow-up on 2026-09-30
        MeetingWorkflowRequest followUpWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Follow-up meeting done")
                .nextPlanDate(LocalDate.of(2026, 9, 30))
                .build();
        meetingService.processMeetingUpdateWorkflow(followUpMeeting.getMeetingCode(), followUpWorkflowReq, "EMP000001");

        // Coordinator verifies follow-up meeting with meetingDate = 2026-09-25
        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .meetingTiming(LocalTime.of(15, 30))
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .bestTimeForMeeting("EVENING")
                .meetingWith("SELF")
                .build();
        mockMvc.perform(post("/v1/meetings/verification/" + followUpMeeting.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk());

        // Previous Intro meeting's meetingDate and nextMeetingDate must remain unchanged
        Meeting introReloaded = meetingRepository.findByMeetingCode(introMeeting.getMeetingCode()).orElseThrow();
        assertThat(introReloaded.getMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 23));
        assertThat(introReloaded.getNextMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 28));
    }

    @Test
    @DisplayName("TEST 7: Current follow-up meeting dates remain correct")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void test7_CurrentFollowUpMeetingDatesRemainCorrect() throws Exception {
        Meeting[] meetings = setupIntroAndFollowUpScenario();
        Meeting followUpMeeting = meetings[1];

        // Sales Person conducts follow-up on 2026-09-25 with next follow-up on 2026-09-30
        MeetingWorkflowRequest followUpWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Follow-up meeting done")
                .nextPlanDate(LocalDate.of(2026, 9, 30))
                .build();
        meetingService.processMeetingUpdateWorkflow(followUpMeeting.getMeetingCode(), followUpWorkflowReq, "EMP000001");

        // Coordinator verifies follow-up meeting with meetingDate = 2026-09-25
        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .meetingTiming(LocalTime.of(15, 30))
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .bestTimeForMeeting("EVENING")
                .meetingWith("SELF")
                .build();
        mockMvc.perform(post("/v1/meetings/verification/" + followUpMeeting.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk());

        // Current follow-up meeting dates are correct
        Meeting followUpReloaded = meetingRepository.findByMeetingCode(followUpMeeting.getMeetingCode()).orElseThrow();
        assertThat(followUpReloaded.getMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 25));
        assertThat(followUpReloaded.getNextMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(followUpReloaded.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
    }

    @Test
    @DisplayName("TEST 8: Follow-up sequential creation workflow continues to work intact")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void test8_FollowUpSequentialCreationRemainsIntact() throws Exception {
        Meeting[] meetings = setupIntroAndFollowUpScenario();
        Meeting followUpMeeting = meetings[1];

        // Updating follow-up meeting (sequence 2) with WORK_IN_PROGRESS and nextPlanDate = 2026-09-30 creates sequence 3
        MeetingWorkflowRequest followUpWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Follow-up meeting conducted")
                .nextPlanDate(LocalDate.of(2026, 9, 30))
                .nextPlanTime(LocalTime.of(16, 0))
                .build();

        MeetingResponse response = meetingService.processMeetingUpdateWorkflow(followUpMeeting.getMeetingCode(), followUpWorkflowReq, "EMP000001");

        // The newly created sequence 3 follow-up meeting exists and is SCHEDULED for 2026-09-30
        Meeting seq3Meeting = meetingRepository.findByLeadIdAndMeetingNumber(followUpMeeting.getLead().getId(), 3)
                .orElseThrow(() -> new IllegalStateException("Sequential meeting #3 should exist"));

        assertThat(seq3Meeting.getMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(seq3Meeting.getMeetingTime()).isEqualTo(LocalTime.of(16, 0));
        assertThat(seq3Meeting.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(response.getMeetingCode()).isEqualTo(seq3Meeting.getMeetingCode());
    }

    @Test
    @DisplayName("TEST 9: Existing API behavior remains backward compatible when meetingDate is omitted from verification request")
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void test9_VerificationWithoutMeetingDateRemainsBackwardCompatible() throws Exception {
        Meeting[] meetings = setupIntroAndFollowUpScenario();
        Meeting followUpMeeting = meetings[1];

        MeetingWorkflowRequest followUpWorkflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.of(2026, 9, 25))
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Follow-up conducted")
                .nextPlanDate(LocalDate.of(2026, 9, 30))
                .build();
        meetingService.processMeetingUpdateWorkflow(followUpMeeting.getMeetingCode(), followUpWorkflowReq, "EMP000001");

        // Request WITHOUT meetingDate (backward compatible legacy payload)
        Map<String, Object> legacyPayload = new HashMap<>();
        legacyPayload.put("meetingTiming", "15:30:00");
        legacyPayload.put("ageGroup", "AGE_25_35");
        legacyPayload.put("existingSip", "YES");
        legacyPayload.put("profession", "DOCTOR");
        legacyPayload.put("professionDetail", "Cardiologist");
        legacyPayload.put("bestTimeForMeeting", "EVENING");
        legacyPayload.put("meetingWith", "SOMEONE_ELSE");
        legacyPayload.put("personName", "Amit Sharma");
        legacyPayload.put("position", "Client");

        mockMvc.perform(post("/v1/meetings/verification/" + followUpMeeting.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(legacyPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingCode").value(followUpMeeting.getMeetingCode()))
                .andExpect(jsonPath("$.meetingDate").value("2026-09-25"))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(followUpMeeting.getId()).orElseThrow();
        assertThat(verification.getMeetingDate()).isEqualTo(LocalDate.of(2026, 9, 25));
    }
}
