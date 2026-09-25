package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.LeadSource;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.controller.MeetingController;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingSearchRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingReportResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingSummaryResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.meeting.service.MeetingWorkflowService;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.meeting.specification.MeetingSearchSpecification;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class MeetingNotConductedTest {

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private MeetingWorkflowService meetingWorkflowService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private ProcessCoordinatorService processCoordinatorService;

    @Autowired
    private MeetingController meetingController;

    private static final String ADMIN_USER = "EMP000001";

    private Meeting createTestScheduledMeeting(String clientName) {
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName(clientName);
        leadReq.setMobileNumber("98" + (System.currentTimeMillis() % 100000000L));
        leadReq.setLeadSource(LeadSource.MANUAL);
        leadReq.setLocation("Noida");
        LeadResponse lead = leadService.createLead(leadReq, ADMIN_USER);

        CreateMeetingRequest schedReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Clinic Location")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meetingResp = meetingService.createMeeting(schedReq, ADMIN_USER);
        return meetingRepository.findByMeetingCode(meetingResp.getMeetingCode()).orElseThrow();
    }

    @Test
    @DisplayName("TEST 1: CONDUCTED + valid data -> existing behavior works")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "PC_COORDINATOR"})
    public void test1_conductedWithValidData_existingBehaviorWorks() {
        Meeting meeting = createTestScheduledMeeting("Conducted Test Client");
        String originalCode = meeting.getMeetingCode();

        MeetingWorkflowRequest request = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .remarks("Conducted meeting with client successfully.")
                .nextPlanDate(LocalDate.now().plusDays(3))
                .nextPlanTime(LocalTime.of(14, 0))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(15.0)
                .build();

        MeetingResponse response = meetingWorkflowService.processWorkflow(originalCode, request, ADMIN_USER);
        assertThat(response).isNotNull();

        // Original meeting is marked COMPLETED
        Meeting updatedOriginal = meetingRepository.findByMeetingCode(originalCode).orElseThrow();
        assertThat(updatedOriginal.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(updatedOriginal.getMeetingConducted()).isEqualTo(MeetingConductStatus.CONDUCTED);

        // Next sequential follow-up meeting was generated (#2)
        assertThat(response.getMeetingNumber()).isEqualTo(2);
        assertThat(response.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
    }

    @Test
    @DisplayName("TEST 2: NOT_CONDUCTED + valid remarks/location/follow-up -> record saved successfully")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "PC_COORDINATOR"})
    public void test2_notConductedWithValidData_recordSavedSuccessfully() {
        Meeting meeting = createTestScheduledMeeting("Not Conducted Valid Client");
        String originalCode = meeting.getMeetingCode();
        Integer originalNumber = meeting.getMeetingNumber();

        MeetingWorkflowRequest request = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Doctor was not available at the clinic.")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(12.5)
                .build();

        MeetingResponse response = meetingWorkflowService.processWorkflow(originalCode, request, ADMIN_USER);
        assertThat(response).isNotNull();
        assertThat(response.getMeetingCode()).isEqualTo(originalCode);
        assertThat(response.getMeetingNumber()).isEqualTo(originalNumber);
        assertThat(response.getMeetingStatus()).isEqualTo(MeetingStatus.NOT_CONDUCTED);
        assertThat(response.getMeetingConducted()).isEqualTo(MeetingConductStatus.NOT_CONDUCTED);

        // Verify in DB
        Meeting saved = meetingRepository.findByMeetingCode(originalCode).orElseThrow();
        assertThat(saved.getMeetingStatus()).isEqualTo(MeetingStatus.NOT_CONDUCTED);
        assertThat(saved.getMeetingConducted()).isEqualTo(MeetingConductStatus.NOT_CONDUCTED);
        assertThat(saved.getMeetingRemarks()).isEqualTo("Doctor was not available at the clinic.");
        assertThat(saved.getNextMeetingDate()).isEqualTo(LocalDate.now().plusDays(5));
        assertThat(saved.getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(28.5355));
        assertThat(saved.getLongitude()).isEqualByComparingTo(BigDecimal.valueOf(77.3910));
        assertThat(saved.getLocationAccuracy()).isEqualTo(12.5);

        // Verification record is set to PENDING
        MeetingVerification verification = meetingVerificationRepository.findByMeetingId(saved.getId()).orElseThrow();
        assertThat(verification.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(verification.getMeetingConducted()).isEqualTo(MeetingConductStatus.NOT_CONDUCTED);

        // Lead remains in WORK_IN_PROGRESS / FOLLOW_UP
        Lead lead = saved.getLead();
        assertThat(lead.getLeadStatus()).isEqualTo(LeadStatus.WORK_IN_PROGRESS);
    }

    @Test
    @DisplayName("TEST 3: NOT_CONDUCTED without remarks -> validation failure")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "PC_COORDINATOR"})
    public void test3_notConductedWithoutRemarks_validationFailure() {
        Meeting meeting = createTestScheduledMeeting("Missing Remarks Client");

        MeetingWorkflowRequest request = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("") // Empty remarks
                .nextPlanDate(LocalDate.now().plusDays(2))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();

        assertThatThrownBy(() -> meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), request, ADMIN_USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Remarks/reason is mandatory");
    }

    @Test
    @DisplayName("TEST 4: NOT_CONDUCTED without location -> validation failure")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "PC_COORDINATOR"})
    public void test4_notConductedWithoutLocation_validationFailure() {
        Meeting meeting = createTestScheduledMeeting("Missing Location Client");

        MeetingWorkflowRequest request = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Customer unavailable")
                .nextPlanDate(LocalDate.now().plusDays(2))
                .latitude(null) // Missing GPS
                .longitude(null)
                .accuracy(10.0)
                .build();

        assertThatThrownBy(() -> meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), request, ADMIN_USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location coordinates");
    }

    @Test
    @DisplayName("TEST 5: NOT_CONDUCTED without nextPlanDate -> validation failure")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "PC_COORDINATOR"})
    public void test5_notConductedWithoutNextPlanDate_validationFailure() {
        Meeting meeting = createTestScheduledMeeting("Missing Next Plan Client");

        MeetingWorkflowRequest request = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Customer unavailable")
                .nextPlanDate(null) // Missing follow-up date
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();

        assertThatThrownBy(() -> meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), request, ADMIN_USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Next plan date is mandatory");
    }

    @Test
    @DisplayName("TEST 6: NOT_CONDUCTED verification -> verification succeeds using existing location verification")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_VERIFY", "MEETING_READ"})
    public void test6_notConductedVerification_succeedsUsingExistingLocationVerification() {
        Meeting meeting = createTestScheduledMeeting("Verification Success Client");

        MeetingWorkflowRequest request = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Doctor was in surgery, could not meet.")
                .nextPlanDate(LocalDate.now().plusDays(4))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(12.0)
                .build();
        meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), request, ADMIN_USER);

        // PC Coordinator verifies the visit
        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .remarks("GPS location and visit verified. Doctor was indeed in surgery.")
                .meetingTiming(LocalTime.of(10, 0))
                .build();

        MeetingResponse verifiedResponse = processCoordinatorService.verifyMeeting(meeting.getMeetingCode(), verifyReq, ADMIN_USER);
        assertThat(verifiedResponse).isNotNull();
        assertThat(verifiedResponse.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

        Meeting savedMeeting = meetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(savedMeeting.getVerifiedByProcessCoordinator()).isTrue();

        MeetingVerification verification = meetingVerificationRepository.findByMeetingMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(verification.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(verification.getMeetingConducted()).isEqualTo(MeetingConductStatus.NOT_CONDUCTED);
        assertThat(verification.getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(28.5355));
        assertThat(verification.getLongitude()).isEqualByComparingTo(BigDecimal.valueOf(77.3910));
        assertThat(verification.getLocationAccuracy()).isEqualTo(12.0);
    }

    @Test
    @DisplayName("TEST 7: NOT_CONDUCTED verification -> conducted meeting number/count is NOT updated")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_VERIFY", "MEETING_READ"})
    public void test7_notConductedVerification_conductedMeetingNumberAndCountNotUpdated() {
        Meeting meeting = createTestScheduledMeeting("Count Check Client");
        long initialCompletedCount = meetingRepository.countByMeetingStatus(MeetingStatus.COMPLETED);
        int initialMeetingNumber = meeting.getMeetingNumber();
        String initialMeetingCode = meeting.getMeetingCode();

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Doctor was unavailable.")
                .nextPlanDate(LocalDate.now().plusDays(3))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(11.0)
                .build();
        meetingWorkflowService.processWorkflow(initialMeetingCode, workflowReq, ADMIN_USER);

        // Verify visit
        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .remarks("Verified clinic visit.")
                .meetingTiming(LocalTime.of(10, 0))
                .build();
        processCoordinatorService.verifyMeeting(initialMeetingCode, verifyReq, ADMIN_USER);

        Meeting afterVerification = meetingRepository.findByMeetingCode(initialMeetingCode).orElseThrow();
        // Meeting number and code are identical
        assertThat(afterVerification.getMeetingNumber()).isEqualTo(initialMeetingNumber);
        assertThat(afterVerification.getMeetingCode()).isEqualTo(initialMeetingCode);

        // Completed (conducted) meeting count in DB did NOT increment
        long afterCompletedCount = meetingRepository.countByMeetingStatus(MeetingStatus.COMPLETED);
        assertThat(afterCompletedCount).isEqualTo(initialCompletedCount);

        // Verified NOT_CONDUCTED created exactly one scheduled meeting for the lead
        long leadMeetingCount = meetingRepository.countByLeadId(meeting.getLead().getId());
        assertThat(leadMeetingCount).isEqualTo(2);
        long scheduledMeetingsCount = meetingRepository.countByLeadIdAndMeetingStatus(meeting.getLead().getId(), MeetingStatus.SCHEDULED);
        assertThat(scheduledMeetingsCount).isEqualTo(1);
    }

    @Test
    @DisplayName("TEST 8: CONDUCTED verification -> existing meeting number/count behavior remains unchanged")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_VERIFY", "MEETING_READ"})
    public void test8_conductedVerification_existingMeetingNumberAndCountBehaviorRemainsUnchanged() {
        Meeting meeting = createTestScheduledMeeting("Conducted Verify Client");
        long initialCompletedCount = meetingRepository.countByMeetingStatus(MeetingStatus.COMPLETED);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .remarks("Conducted discussion.")
                .nextPlanDate(LocalDate.now().plusDays(2))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();
        meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);

        // Completed count incremented by 1
        assertThat(meetingRepository.countByMeetingStatus(MeetingStatus.COMPLETED)).isEqualTo(initialCompletedCount + 1);

        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .remarks("Verified genuine meeting.")
                .aloneWith("SELF")
                .meetingTiming(LocalTime.of(11, 0))
                .build();
        MeetingResponse verified = processCoordinatorService.verifyMeeting(meeting.getMeetingCode(), verifyReq, ADMIN_USER);
        assertThat(verified.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        Meeting savedMeeting = meetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(savedMeeting.getVerifiedByProcessCoordinator()).isTrue();
    }

    @Test
    @DisplayName("TEST 9: Dashboard conducted-meeting count -> NOT_CONDUCTED is excluded")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "PC_COORDINATOR"})
    public void test9_dashboardConductedMeetingCount_notConductedIsExcluded() {
        Meeting m1 = createTestScheduledMeeting("Dashboard Client 1");
        Meeting m2 = createTestScheduledMeeting("Dashboard Client 2");

        // m1 is CONDUCTED
        MeetingWorkflowRequest req1 = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .remarks("Conducted meeting.")
                .build();
        meetingWorkflowService.processWorkflow(m1.getMeetingCode(), req1, ADMIN_USER);

        // m2 is NOT_CONDUCTED
        MeetingWorkflowRequest req2 = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Doctor not available.")
                .nextPlanDate(LocalDate.now().plusDays(3))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();
        meetingWorkflowService.processWorkflow(m2.getMeetingCode(), req2, ADMIN_USER);

        MeetingReportResponse report = meetingService.getMeetingReports();
        Long completedCount = report.getMeetingsCompleted();

        // Completed count strictly counts COMPLETED (conducted) meetings and excludes NOT_CONDUCTED
        long dbCompleted = meetingRepository.countByMeetingStatus(MeetingStatus.COMPLETED);
        assertThat(completedCount).isEqualTo(dbCompleted);

        // Verify NOT_CONDUCTED status in DB is not counted as completed
        Meeting notConductedMeeting = meetingRepository.findByMeetingCode(m2.getMeetingCode()).orElseThrow();
        assertThat(notConductedMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.NOT_CONDUCTED);
        assertThat(completedCount).isLessThan(meetingRepository.count());
    }

    @Test
    @DisplayName("TEST 10: Meeting history -> NOT_CONDUCTED record remains visible")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_READ"})
    public void test10_meetingHistory_notConductedRecordRemainsVisible() {
        Meeting meeting = createTestScheduledMeeting("History Visibility Client");
        Lead lead = meeting.getLead();

        MeetingWorkflowRequest request = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Doctor stepped out for emergency.")
                .nextPlanDate(LocalDate.now().plusDays(2))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(12.0)
                .build();
        meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), request, ADMIN_USER);

        // 1. Visible in Lead Details Meeting History
        com.blueant_crm_erp.lead.dto.response.LeadDetailResponse leadDetails = leadService.getLeadDetails(lead.getUniqueLeadId());
        assertThat(leadDetails.getMeetingHistory()).isNotEmpty();
        boolean foundInLeadHistory = leadDetails.getMeetingHistory().stream()
                .anyMatch(h -> h.getMeetingCode().equals(meeting.getMeetingCode()) &&
                               h.getMeetingStatus() == MeetingStatus.NOT_CONDUCTED);
        assertThat(foundInLeadHistory).isTrue();

        // 2. Visible in Meeting Controller /lead/{leadId}/history
        var historyResponse = meetingController.getMeetingHistory(lead.getUniqueLeadId());
        List<MeetingSummaryResponse> controllerHistory = historyResponse.getData();
        assertThat(controllerHistory).isNotEmpty();
        boolean foundInControllerHistory = controllerHistory.stream()
                .anyMatch(h -> h.getMeetingCode().equals(meeting.getMeetingCode()) &&
                               h.getMeetingStatus() == MeetingStatus.NOT_CONDUCTED);
        assertThat(foundInControllerHistory).isTrue();

        // 3. Visible in Meeting Search by statusFilter ("all" or "not_conducted")
        Specification<Meeting> spec = MeetingSearchSpecification.build(meeting.getMeetingCode(), null, "not_conducted", null);
        List<Meeting> searchResults = meetingRepository.findAll(spec);
        boolean foundInSearch = searchResults.stream()
                .anyMatch(m -> m.getMeetingCode().equals(meeting.getMeetingCode()));
        assertThat(foundInSearch).isTrue();
    }

    @Test
    @DisplayName("TEST 11: Complete Lifecycle: NOT_CONDUCTED -> VERIFIED -> Subsequent CONDUCTED meeting -> Follow-up sequence")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_VERIFY", "MEETING_READ"})
    public void test11_completeLifecycle_notConductedThenConducted_preservesSequencingAndCounts() {
        // Step 1: Create Lead & initial scheduled meeting (Meeting #1)
        Meeting m1 = createTestScheduledMeeting("Lifecycle Client A");
        Lead lead = m1.getLead();
        String m1Code = m1.getMeetingCode();
        assertThat(m1.getMeetingNumber()).isEqualTo(1);
        assertThat(m1.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);

        // Step 2: Sales Person visits but contact is unavailable -> submits NOT_CONDUCTED
        MeetingWorkflowRequest notConductedReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Doctor was in surgery, could not meet.")
                .nextPlanDate(LocalDate.now().plusDays(2))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();
        meetingWorkflowService.processWorkflow(m1Code, notConductedReq, ADMIN_USER);

        Meeting m1AfterWorkflow = meetingRepository.findByMeetingCode(m1Code).orElseThrow();
        assertThat(m1AfterWorkflow.getMeetingStatus()).isEqualTo(MeetingStatus.NOT_CONDUCTED);
        assertThat(m1AfterWorkflow.getMeetingConducted()).isEqualTo(MeetingConductStatus.NOT_CONDUCTED);
        assertThat(m1AfterWorkflow.getVerification().getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);

        // Verify: No automatic follow-up meeting after NOT_CONDUCTED
        long meetingsAfterM1 = meetingRepository.countByLeadId(lead.getId());
        assertThat(meetingsAfterM1).isEqualTo(1);
        long completedAfterM1 = meetingRepository.countByLeadIdAndMeetingStatus(lead.getId(), MeetingStatus.COMPLETED);
        assertThat(completedAfterM1).isEqualTo(0);

        // Lead remains in WORK_IN_PROGRESS / FOLLOW_UP
        Lead leadAfterM1 = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(leadAfterM1.getLeadStatus()).isEqualTo(LeadStatus.WORK_IN_PROGRESS);
        assertThat(leadAfterM1.getLeadStage()).isEqualTo(LeadStage.FOLLOW_UP);

        // Step 3: PC Coordinator verifies the visit
        MeetingVerificationRequest verifyM1Req = MeetingVerificationRequest.builder()
                .remarks("Verified clinic visit via GPS and hospital register.")
                .meetingTiming(LocalTime.of(10, 30))
                .build();
        MeetingResponse verifiedM1Resp = processCoordinatorService.verifyMeeting(m1Code, verifyM1Req, ADMIN_USER);
        assertThat(verifiedM1Resp.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

        Meeting m1AfterVerification = meetingRepository.findByMeetingCode(m1Code).orElseThrow();
        assertThat(m1AfterVerification.getMeetingStatus()).isEqualTo(MeetingStatus.NOT_CONDUCTED);
        assertThat(m1AfterVerification.getMeetingNumber()).isEqualTo(1);

        // Step 4: Meeting #2 was automatically scheduled upon PC verification of NOT_CONDUCTED
        Meeting m2 = meetingRepository.findTopByLeadIdOrderByMeetingNumberDesc(lead.getId()).orElseThrow();
        String m2Code = m2.getMeetingCode();

        assertThat(m2Code).isNotEqualTo(m1Code);
        assertThat(m2.getMeetingNumber()).isEqualTo(2); // Sequence progresses to 2
        assertThat(m2.getMeetingType()).isEqualTo(com.blueant_crm_erp.meeting.enums.MeetingType.INTRO); // INTRO remains INTRO
        assertThat(m2.getMeetingTitle()).isEqualTo("Intro Meeting");
        assertThat(m2.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);

        // Step 5: Sales Person conducts the second visit and submits CONDUCTED
        MeetingWorkflowRequest conductedM2Req = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .remarks("Doctor met, detailed product presentation given.")
                .nextPlanDate(LocalDate.now().plusDays(7))
                .nextPlanTime(LocalTime.of(15, 0))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(8.0)
                .build();
        MeetingResponse m3Resp = meetingWorkflowService.processWorkflow(m2Code, conductedM2Req, ADMIN_USER);

        // Meeting #2 is now COMPLETED
        Meeting m2AfterWorkflow = meetingRepository.findByMeetingCode(m2Code).orElseThrow();
        assertThat(m2AfterWorkflow.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(m2AfterWorkflow.getMeetingConducted()).isEqualTo(MeetingConductStatus.CONDUCTED);
        assertThat(m2AfterWorkflow.getMeetingNumber()).isEqualTo(2);

        // Automatic follow-up Meeting #3 was created for WORK_IN_PROGRESS with nextPlanDate
        assertThat(m3Resp.getMeetingNumber()).isEqualTo(3);
        assertThat(m3Resp.getMeetingTitle()).isEqualTo("2nd Meeting");
        assertThat(m3Resp.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);

        // Step 6: Verify conducted meeting counts
        long completedMeetingsCount = meetingRepository.countByLeadIdAndMeetingStatus(lead.getId(), MeetingStatus.COMPLETED);
        long notConductedMeetingsCount = meetingRepository.countByLeadIdAndMeetingStatus(lead.getId(), MeetingStatus.NOT_CONDUCTED);
        long scheduledMeetingsCount = meetingRepository.countByLeadIdAndMeetingStatus(lead.getId(), MeetingStatus.SCHEDULED);
        long totalLeadMeetings = meetingRepository.countByLeadId(lead.getId());

        assertThat(totalLeadMeetings).isEqualTo(3);
        assertThat(completedMeetingsCount).isEqualTo(1); // Only M2 is completed/conducted
        assertThat(notConductedMeetingsCount).isEqualTo(1); // M1 remains not_conducted
        assertThat(scheduledMeetingsCount).isEqualTo(1); // M3 is scheduled follow-up

        // Step 7: Verify NOT_CONDUCTED remains preserved in meeting history
        com.blueant_crm_erp.lead.dto.response.LeadDetailResponse leadDetails = leadService.getLeadDetails(lead.getUniqueLeadId());
        List<MeetingSummaryResponse> history = leadDetails.getMeetingHistory();
        assertThat(history).hasSize(2); // History shows COMPLETED and NOT_CONDUCTED (M1 and M2)
        assertThat(history.get(0).getMeetingCode()).isEqualTo(m1Code);
        assertThat(history.get(0).getMeetingStatus()).isEqualTo(MeetingStatus.NOT_CONDUCTED);
        assertThat(history.get(0).getMeetingNumber()).isEqualTo(1);

        assertThat(history.get(1).getMeetingCode()).isEqualTo(m2Code);
        assertThat(history.get(1).getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(history.get(1).getMeetingNumber()).isEqualTo(2);

        // Step 8: Verify PC Coordinator verification of the CONDUCTED meeting
        MeetingVerificationRequest verifyM2Req = MeetingVerificationRequest.builder()
                .remarks("Genuine meeting verified with doctor.")
                .aloneWith("SELF")
                .meetingTiming(LocalTime.of(14, 0))
                .build();
        MeetingResponse verifiedM2Resp = processCoordinatorService.verifyMeeting(m2Code, verifyM2Req, ADMIN_USER);
        assertThat(verifiedM2Resp.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
    }

    @Test
    @DisplayName("TEST 12: Complete Lifecycle: NOT_CONDUCTED -> Subsequent CONDUCTED with CONVERTED_CLIENT")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_VERIFY", "MEETING_READ"})
    public void test12_completeLifecycle_subsequentConductedWithConversion() {
        Meeting m1 = createTestScheduledMeeting("Conversion Lifecycle Client");
        Lead lead = m1.getLead();
        String m1Code = m1.getMeetingCode();

        // 1. First visit is NOT_CONDUCTED
        MeetingWorkflowRequest notConductedReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Doctor out of station.")
                .nextPlanDate(LocalDate.now().plusDays(4))
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();
        meetingWorkflowService.processWorkflow(m1Code, notConductedReq, ADMIN_USER);

        // 2. Schedule second visit
        CreateMeetingRequest schedM2Req = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(4))
                .meetingTime(LocalTime.of(11, 0))
                .meetingLocation("Clinic Location")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse m2Resp = meetingService.createMeeting(schedM2Req, ADMIN_USER);
        String m2Code = m2Resp.getMeetingCode();

        // 3. Second visit is CONDUCTED and converted
        MeetingWorkflowRequest convertReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.CONVERTED_CLIENT)
                .remarks("Client agreed and signed agreement.")
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();
        meetingWorkflowService.processWorkflow(m2Code, convertReq, ADMIN_USER);

        // Meeting #2 is COMPLETED
        Meeting m2 = meetingRepository.findByMeetingCode(m2Code).orElseThrow();
        assertThat(m2.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(m2.getMeetingNumber()).isEqualTo(2);

        // Lead is CONVERTED
        Lead convertedLead = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(convertedLead.getLeadStatus()).isEqualTo(LeadStatus.CONVERTED);

        // No Meeting #3 was created (conversion terminates scheduling)
        assertThat(meetingRepository.countByLeadId(lead.getId())).isEqualTo(2);
        assertThat(meetingRepository.countByLeadIdAndMeetingStatus(lead.getId(), MeetingStatus.COMPLETED)).isEqualTo(1);
    }
}

