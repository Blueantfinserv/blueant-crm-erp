package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.LeadSource;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingType;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.meeting.service.MeetingWorkflowService;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class NotConductedMeetingPcVerificationRegressionTest {

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
    private ProcessCoordinatorService processCoordinatorService;

    @Autowired
    private UserRepository userRepository;

    private static final String ADMIN_USER = "EMP000001";
    private User testSalesPerson;

    @BeforeEach
    void setUp() {
        testSalesPerson = userRepository.findByEmployeeCodeIgnoreCase(ADMIN_USER)
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));
    }

    private Meeting createIntroScheduledMeeting(String clientName) {
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName(clientName);
        leadReq.setMobileNumber("98" + (System.currentTimeMillis() % 100000000L));
        leadReq.setLeadSource(LeadSource.MANUAL);
        leadReq.setLocation("Noida Sector 62");
        LeadResponse lead = leadService.createLead(leadReq, ADMIN_USER);

        CreateMeetingRequest schedReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(11, 0))
                .meetingLocation("Client Office, Sector 62")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meetingResp = meetingService.createMeeting(schedReq, ADMIN_USER);
        Meeting m = meetingRepository.findByMeetingCode(meetingResp.getMeetingCode()).orElseThrow();
        if (testSalesPerson != null) {
            m.setAssignedEmployee(testSalesPerson);
            m = meetingRepository.save(m);
        }
        return m;
    }

    @Test
    @DisplayName("Regression Tests 1-6 & 8: NOT_CONDUCTED meeting PC verified creates exactly one new meeting with all business rules")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_VERIFY", "MEETING_READ"})
    public void testNotConductedMeetingVerificationCreatesOneNewMeetingWithCorrectRules() {
        // Setup original INTRO meeting
        Meeting oldMeeting = createIntroScheduledMeeting("Regression Client 1");
        Long oldMeetingId = oldMeeting.getId();
        String oldMeetingCode = oldMeeting.getMeetingCode();
        LocalDate oldMeetingDate = oldMeeting.getMeetingDate();
        Long leadId = oldMeeting.getLead().getId();
        User expectedOwner = oldMeeting.getAssignedEmployee();

        assertThat(oldMeeting.getMeetingType()).isEqualTo(MeetingType.INTRO);
        assertThat(oldMeeting.getMeetingNumber()).isEqualTo(1);
        assertThat(oldMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);

        // Sales Person marks NOT_CONDUCTED with nextPlanDate
        LocalDate nextPlanDate = LocalDate.now().plusDays(2);
        LocalTime nextPlanTime = LocalTime.of(14, 30);
        String workflowRemarks = "Client called away for surgery; rescheduled.";

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks(workflowRemarks)
                .nextPlanDate(nextPlanDate)
                .nextPlanTime(nextPlanTime)
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();

        meetingWorkflowService.processWorkflow(oldMeetingCode, workflowReq, ADMIN_USER);

        // Before PC verification: NO new meeting created yet
        long countBeforeVerify = meetingRepository.countByLeadId(leadId);
        assertThat(countBeforeVerify).isEqualTo(1);

        Meeting beforePcVerify = meetingRepository.findById(oldMeetingId).orElseThrow();
        assertThat(beforePcVerify.getMeetingStatus()).isEqualTo(MeetingStatus.NOT_CONDUCTED);
        assertThat(beforePcVerify.getMeetingConducted()).isEqualTo(MeetingConductStatus.NOT_CONDUCTED);

        // PC Coordinator verifies the NOT_CONDUCTED visit
        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .remarks("GPS and hospital visit verified. Doctor genuinely busy.")
                .meetingTiming(LocalTime.of(11, 0))
                .build();

        MeetingResponse verifyResponse = processCoordinatorService.verifyMeeting(oldMeetingCode, verifyReq, ADMIN_USER);
        assertThat(verifyResponse.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

        // 1. verified NOT_CONDUCTED creates one next meeting
        long totalLeadMeetings = meetingRepository.countByLeadId(leadId);
        assertThat(totalLeadMeetings).isEqualTo(2);

        long scheduledMeetings = meetingRepository.countByLeadIdAndMeetingStatus(leadId, MeetingStatus.SCHEDULED);
        assertThat(scheduledMeetings).isEqualTo(1);

        Meeting newMeeting = meetingRepository.findTopByLeadIdOrderByMeetingNumberDesc(leadId).orElseThrow();

        // 2. next meeting date = nextPlanDate
        assertThat(newMeeting.getMeetingDate()).isEqualTo(nextPlanDate);
        assertThat(newMeeting.getMeetingTime()).isEqualTo(nextPlanTime);

        // 3. same lead
        assertThat(newMeeting.getLead().getId()).isEqualTo(leadId);

        // 4. same correct owner
        if (expectedOwner != null) {
            assertThat(newMeeting.getAssignedEmployee()).isNotNull();
            assertThat(newMeeting.getAssignedEmployee().getId()).isEqualTo(expectedOwner.getId());
        }

        // 5. new meeting ID/code
        assertThat(newMeeting.getId()).isNotNull();
        assertThat(newMeeting.getId()).isNotEqualTo(oldMeetingId);
        assertThat(newMeeting.getMeetingCode()).isNotNull();
        assertThat(newMeeting.getMeetingCode()).isNotEqualTo(oldMeetingCode);
        assertThat(newMeeting.getMeetingCode()).startsWith("BA-MTG-");
        assertThat(newMeeting.getMeetingNumber()).isEqualTo(2);

        // 6. INTRO remains INTRO
        assertThat(newMeeting.getMeetingType()).isEqualTo(MeetingType.INTRO);
        assertThat(newMeeting.getMeetingTitle()).isEqualTo("Intro Meeting");
        assertThat(newMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);

        // 8. old NOT_CONDUCTED meeting remains unchanged
        Meeting preservedOldMeeting = meetingRepository.findById(oldMeetingId).orElseThrow();
        assertThat(preservedOldMeeting.getId()).isEqualTo(oldMeetingId);
        assertThat(preservedOldMeeting.getMeetingCode()).isEqualTo(oldMeetingCode);
        assertThat(preservedOldMeeting.getMeetingDate()).isEqualTo(oldMeetingDate);
        assertThat(preservedOldMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.NOT_CONDUCTED);
        assertThat(preservedOldMeeting.getMeetingConducted()).isEqualTo(MeetingConductStatus.NOT_CONDUCTED);
        assertThat(preservedOldMeeting.getVerifiedByProcessCoordinator()).isTrue();
    }

    @Test
    @DisplayName("Regression Test 7: repeated verification does not create duplicate meeting")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_VERIFY", "MEETING_READ"})
    public void testRepeatedVerificationDoesNotCreateDuplicate() {
        Meeting oldMeeting = createIntroScheduledMeeting("Duplicate Guard Client");
        Long leadId = oldMeeting.getLead().getId();

        LocalDate nextPlanDate = LocalDate.now().plusDays(3);
        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Client not available.")
                .nextPlanDate(nextPlanDate)
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();
        meetingWorkflowService.processWorkflow(oldMeeting.getMeetingCode(), workflowReq, ADMIN_USER);

        // First PC verification
        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .remarks("Visit verified.")
                .meetingTiming(LocalTime.of(10, 0))
                .build();
        processCoordinatorService.verifyMeeting(oldMeeting.getMeetingCode(), verifyReq, ADMIN_USER);

        assertThat(meetingRepository.countByLeadId(leadId)).isEqualTo(2);
        assertThat(meetingRepository.countByLeadIdAndMeetingStatus(leadId, MeetingStatus.SCHEDULED)).isEqualTo(1);

        // Reset verification status to PENDING and attempt second verification
        com.blueant_crm_erp.meeting.entity.MeetingVerification verification = oldMeeting.getVerification();
        verification.setVerificationStatus(VerificationStatus.PENDING);

        // Call verification again
        processCoordinatorService.verifyMeeting(oldMeeting.getMeetingCode(), verifyReq, ADMIN_USER);

        // Exactly one scheduled meeting remains — NO duplicate was created
        assertThat(meetingRepository.countByLeadId(leadId)).isEqualTo(2);
        assertThat(meetingRepository.countByLeadIdAndMeetingStatus(leadId, MeetingStatus.SCHEDULED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regression Test 9: existing CONDUCTED workflow still passes")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_VERIFY", "MEETING_READ"})
    public void testExistingConductedWorkflowStillPasses() {
        Meeting meeting = createIntroScheduledMeeting("Conducted Flow Client");
        String meetingCode = meeting.getMeetingCode();
        Long leadId = meeting.getLead().getId();

        // Conduct meeting with WORK_IN_PROGRESS and nextPlanDate
        LocalDate followUpDate = LocalDate.now().plusDays(5);
        LocalTime followUpTime = LocalTime.of(16, 0);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .remarks("Product discussion went well.")
                .nextPlanDate(followUpDate)
                .nextPlanTime(followUpTime)
                .latitude(BigDecimal.valueOf(28.5355))
                .longitude(BigDecimal.valueOf(77.3910))
                .accuracy(10.0)
                .build();

        MeetingResponse workflowResponse = meetingWorkflowService.processWorkflow(meetingCode, workflowReq, ADMIN_USER);
        assertThat(workflowResponse).isNotNull();

        // Meeting #1 is COMPLETED
        Meeting completedMeeting = meetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        assertThat(completedMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(completedMeeting.getMeetingConducted()).isEqualTo(MeetingConductStatus.CONDUCTED);

        // Follow-up Meeting #2 was created with sequence 2
        assertThat(workflowResponse.getMeetingNumber()).isEqualTo(2);
        assertThat(workflowResponse.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(workflowResponse.getMeetingType()).isEqualTo(MeetingType.FOLLOW_UP);
        assertThat(workflowResponse.getMeetingTitle()).isEqualTo("1st Meeting");

        // PC verifies the CONDUCTED meeting
        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .remarks("Verified genuine discussion.")
                .aloneWith("SELF")
                .meetingTiming(LocalTime.of(16, 0))
                .build();
        MeetingResponse verifiedResp = processCoordinatorService.verifyMeeting(meetingCode, verifyReq, ADMIN_USER);
        assertThat(verifiedResp.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

        // Lead remains WORK_IN_PROGRESS, stage FOLLOW_UP
        Lead lead = leadRepository.findById(leadId).orElseThrow();
        assertThat(lead.getLeadStatus()).isEqualTo(LeadStatus.WORK_IN_PROGRESS);
        assertThat(lead.getLeadStage()).isEqualTo(LeadStage.FOLLOW_UP);
    }
}
