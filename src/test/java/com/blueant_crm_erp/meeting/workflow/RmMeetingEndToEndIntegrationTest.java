package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.dto.request.AssignLeadRequest;
import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.LeadSource;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingUpdate;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.*;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingUpdateRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.meeting.service.MeetingWorkflowService;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.DesignationRepository;
import com.blueant_crm_erp.user.repository.TeamRepository;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ============================================================================
 * RM End-to-End Integration Test Suite
 * ============================================================================
 * Verifies the complete production RM lifecycle with test data only:
 * Temporary RM User -> Temporary Lead -> Lead Assignment -> Scheduled Meeting
 * -> RM Conducts Meeting -> Workflow Update Validation (Date & Geo)
 * -> Meeting Completion & Sequential Follow-up Creation -> PC Verification
 * -> Verified Snapshot Audit.
 *
 * Transaction Isolation:
 * Class is annotated with @SpringBootTest, @Transactional, and @Rollback.
 * All temporary test entity IDs are tracked and explicitly cleaned in @AfterEach
 * (child-before-parent order) ensuring zero residual test data.
 */
@SpringBootTest
@Transactional
@Rollback
public class RmMeetingEndToEndIntegrationTest {

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingWorkflowService meetingWorkflowService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingUpdateRepository meetingUpdateRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private ProcessCoordinatorService processCoordinatorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SalesRoleResolver salesRoleResolver;

    private static final String ADMIN_USER = "EMP000001";

    // Tracked IDs for strict deterministic cleanup in @AfterEach
    private final Set<Long> createdUserIds = new HashSet<>();
    private final Set<Long> createdLeadIds = new HashSet<>();
    private final Set<Long> createdMeetingIds = new HashSet<>();

    private User testRmUser;

    @BeforeEach
    void setUp() {
        testRmUser = createTemporaryRmUser();
    }

    @AfterEach
    void tearDown() {
        // Explicit child-to-parent cleanup safety net for created test records
        for (Long meetingId : createdMeetingIds) {
            meetingVerificationRepository.findByMeetingId(meetingId)
                    .ifPresent(v -> meetingVerificationRepository.delete(v));
            List<MeetingUpdate> updates = meetingUpdateRepository.findByMeetingIdOrderByUpdateNumberAsc(meetingId);
            if (!updates.isEmpty()) {
                meetingUpdateRepository.deleteAll(updates);
            }
        }
        for (Long meetingId : createdMeetingIds) {
            if (meetingRepository.existsById(meetingId)) {
                meetingRepository.deleteById(meetingId);
            }
        }
        for (Long leadId : createdLeadIds) {
            if (leadRepository.existsById(leadId)) {
                leadRepository.deleteById(leadId);
            }
        }
        for (Long userId : createdUserIds) {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
            }
        }
    }

    // =========================================================================
    // HELPER: CREATE TEMPORARY RM USER
    // =========================================================================

    private User createTemporaryRmUser() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        Role rmRole = roleRepository.findByCodeIgnoreCase(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER)
                .or(() -> roleRepository.findByCodeIgnoreCase("RELATIONSHIP_MANAGER"))
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER)
                        .name("Relationship Manager")
                        .status(Status.ACTIVE)
                        .build()));

        Designation rmDesig = designationRepository.findByCodeIgnoreCaseAndDeletedFalse("RM")
                .orElseGet(() -> designationRepository.save(Designation.builder()
                        .code("RM")
                        .name("Relationship Manager")
                        .hierarchyLevel(4)
                        .status(Status.ACTIVE)
                        .build()));

        Department salesDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        Team team = teamRepository.findAll().stream().findFirst().orElse(null);

        User rmUser = User.builder()
                .employeeCode("RM_TEST_" + uniqueSuffix.toUpperCase())
                .firstName("RM_Test")
                .lastName("Automated")
                .email("rm.test." + uniqueSuffix + "@blueantcrm.com")
                .mobileNumber("9" + String.valueOf(System.currentTimeMillis()).substring(4, 13))
                .password("$2a$10$e8w.Kz2k6D7P1R3j3lY5aOb3Xz8pQ8mQ0fW9yY3t1r4y2x1w0z")
                .gender(Gender.MALE)
                .status(Status.ACTIVE)
                .role(rmRole)
                .designation(rmDesig)
                .department(salesDept)
                .team(team)
                .build();

        User savedUser = userRepository.save(rmUser);
        createdUserIds.add(savedUser.getId());

        // Validate role resolution on the created user
        assertThat(salesRoleResolver.resolve(savedUser)).isEqualTo(SalesRole.RM);

        return savedUser;
    }

    // =========================================================================
    // HELPER: CREATE TEMPORARY LEAD ASSIGNED TO RM
    // =========================================================================

    private Lead createTemporaryLeadAssignedToRm(String clientName) {
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName(clientName);
        leadReq.setMobileNumber("9" + (System.currentTimeMillis() % 1000000000L));
        leadReq.setLeadSource(LeadSource.MANUAL);
        leadReq.setLocation("Bandra Kurla Complex, Mumbai");

        LeadResponse leadResp = leadService.createLead(leadReq, ADMIN_USER);
        Lead lead = leadRepository.findByLeadCode(leadResp.getLeadCode()).orElseThrow();
        createdLeadIds.add(lead.getId());

        // Assign to the temporary RM User
        AssignLeadRequest assignReq = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(testRmUser.getId())
                .assignmentReason("Assigned to RM for high net-worth relationship management")
                .build();
        leadService.assignLead(assignReq, ADMIN_USER);

        Lead assignedLead = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(assignedLead.getAssignedSalesPerson()).isNotNull();
        assertThat(assignedLead.getAssignedSalesPerson().getId()).isEqualTo(testRmUser.getId());

        return assignedLead;
    }

    // =========================================================================
    // HELPER: CREATE FIRST SCHEDULED INTRO MEETING FOR RM LEAD
    // =========================================================================

    private Meeting createFirstScheduledMeeting(Lead lead) {
        CreateMeetingRequest schedReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(11, 0))
                .meetingLocation("Client Corporate Office, BKC")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();

        MeetingResponse meetingResp = meetingService.createMeeting(schedReq, ADMIN_USER);
        Meeting meeting = meetingRepository.findByMeetingCode(meetingResp.getMeetingCode()).orElseThrow();
        createdMeetingIds.add(meeting.getId());

        // Verify meeting properties
        assertThat(meeting.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(meeting.getMeetingNumber()).isEqualTo(1);
        assertThat(meeting.getMeetingType()).isEqualTo(MeetingType.INTRO);

        // Verify ownership and RM role resolution
        assertThat(salesRoleResolver.resolve(meeting)).isEqualTo(SalesRole.RM);

        return meeting;
    }

    // =========================================================================
    // TEST 1, 10, 11, 12, 13, 14, 15: COMPLETE END-TO-END RM WORKFLOW
    // =========================================================================

    @Test
    @DisplayName("1. Complete RM Workflow: Scheduled -> Conducted with valid Geo -> PC Verified -> Follow-up scheduled")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN", "ROLE_PC_COORDINATOR", "MEETING_VERIFY", "MEETING_READ"})
    void test1_completeRmWorkflow_success() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_AUTOMATED_TEST_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);
        String meetingCode = meeting.getMeetingCode();
        Long meetingId = meeting.getId();

        // 10. Actual meeting date can be earlier than previously planned follow-up date
        meeting.setMeetingDate(LocalDate.now());
        meeting = meetingRepository.save(meeting);

        LocalDate nextPlanDate = LocalDate.now().plusDays(14);
        LocalTime nextPlanTime = LocalTime.of(15, 30);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Conducted comprehensive wealth management discussion with client.")
                .latitude(new BigDecimal("19.065700"))
                .longitude(new BigDecimal("72.868700"))
                .accuracy(8.5)
                .address("BKC, Mumbai")
                .nextPlanDate(nextPlanDate)
                .nextPlanTime(nextPlanTime)
                .build();

        // 17. Authenticated user can be elevated Admin/PC, business rules still governed by RM owner
        MeetingResponse response = meetingWorkflowService.processWorkflow(meetingCode, workflowReq, ADMIN_USER);

        // 12. Verify original meeting state from DB
        Meeting originalMeetingAfterWorkflow = meetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        assertThat(originalMeetingAfterWorkflow.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(originalMeetingAfterWorkflow.getMeetingConducted()).isEqualTo(MeetingConductStatus.CONDUCTED);
        assertThat(originalMeetingAfterWorkflow.getLatitude()).isEqualByComparingTo(new BigDecimal("19.065700"));
        assertThat(originalMeetingAfterWorkflow.getLongitude()).isEqualByComparingTo(new BigDecimal("72.868700"));
        assertThat(originalMeetingAfterWorkflow.getLocationAccuracy()).isEqualTo(8.5);

        // Verify meeting update history was written
        List<MeetingUpdate> updates = meetingUpdateRepository.findByMeetingIdOrderByUpdateNumberAsc(meetingId);
        assertThat(updates).hasSize(1);
        assertThat(updates.get(0).getUpdateNumber()).isEqualTo(1);
        assertThat(updates.get(0).getMeetingConducted()).isEqualTo(MeetingConductStatus.CONDUCTED);

        // Verify verification record was initialized to PENDING
        MeetingVerification verificationBeforePc = meetingVerificationRepository.findByMeetingId(meetingId).orElseThrow();
        assertThat(verificationBeforePc.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);

        // 13. Verify next scheduled follow-up meeting was created for WORK_IN_PROGRESS
        Meeting nextMeeting = meetingRepository.findTopByLeadIdOrderByMeetingNumberDesc(lead.getId()).orElseThrow();
        createdMeetingIds.add(nextMeeting.getId());

        assertThat(nextMeeting.getId()).isNotEqualTo(meetingId);
        assertThat(nextMeeting.getMeetingNumber()).isEqualTo(2);
        assertThat(nextMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(nextMeeting.getMeetingDate()).isEqualTo(nextPlanDate);
        assertThat(nextMeeting.getMeetingTime()).isEqualTo(nextPlanTime);
        assertThat(nextMeeting.getAssignedEmployee().getId()).isEqualTo(testRmUser.getId());
        assertThat(salesRoleResolver.resolve(nextMeeting)).isEqualTo(SalesRole.RM);

        // 14. PC Coordinator verifies the completed RM meeting using the original meetingCode
        MeetingVerificationRequest pcVerifyReq = MeetingVerificationRequest.builder()
                .remarks("RM meeting and client discussion verified successfully.")
                .meetingTiming(LocalTime.of(11, 0))
                .aloneWith("SELF")
                .build();

        MeetingResponse pcResponse = processCoordinatorService.verifyMeeting(meetingCode, pcVerifyReq, ADMIN_USER);
        assertThat(pcResponse.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

        // 15. Verify database snapshot after PC verification
        MeetingVerification finalVerification = meetingVerificationRepository.findByMeetingId(meetingId).orElseThrow();
        assertThat(finalVerification.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(finalVerification.getVerifiedBy()).isEqualTo(ADMIN_USER);
        assertThat(finalVerification.getVerifiedAt()).isNotNull();
        assertThat(finalVerification.getLatitude()).isEqualByComparingTo(new BigDecimal("19.065700"));
        assertThat(finalVerification.getLongitude()).isEqualByComparingTo(new BigDecimal("72.868700"));
        assertThat(finalVerification.getAssignedEmployeeId()).isEqualTo(testRmUser.getId());

        Meeting verifiedOriginalMeeting = meetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        assertThat(verifiedOriginalMeeting.getVerifiedByProcessCoordinator()).isTrue();
    }

    // =========================================================================
    // TEST 2 & 3: RM FOLLOW-UP DATE ACCEPTED (TODAY, EXACTLY +1 CALENDAR MONTH)
    // =========================================================================

    @Test
    @DisplayName("2. RM follow-up date: Today succeeds")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN"})
    void test2_rmFollowUpDateToday_succeeds() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_DATE_TODAY_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Follow up today")
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.0)
                .nextPlanDate(LocalDate.now())
                .build();

        MeetingResponse response = meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);
        assertThat(response).isNotNull();

        Meeting m = meetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(m.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
    }

    @Test
    @DisplayName("3. RM follow-up date: Exactly +1 calendar month succeeds")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN"})
    void test3_rmFollowUpDateExactlyOneMonth_succeeds() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_DATE_1M_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Follow up in 1 month")
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.0)
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .build();

        MeetingResponse response = meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);
        assertThat(response).isNotNull();

        Meeting m = meetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(m.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
    }

    // =========================================================================
    // TEST 4 & 5: RM FOLLOW-UP DATE REJECTED (BEYOND +1 MONTH, PAST DATE)
    // =========================================================================

    @Test
    @DisplayName("4. RM follow-up date: Beyond +1 calendar month fails before mutation")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN"})
    void test4_rmFollowUpDateBeyondOneMonth_fails() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_DATE_OVERFLOW_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);
        String meetingCode = meeting.getMeetingCode();
        Long meetingId = meeting.getId();

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Follow up too far in future")
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.0)
                .nextPlanDate(LocalDate.now().plusMonths(1).plusDays(1))
                .build();

        assertThatThrownBy(() -> meetingWorkflowService.processWorkflow(meetingCode, workflowReq, ADMIN_USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("RM follow-up date cannot be more than one month from today.");

        // Verify no mutation occurred
        Meeting meetingAfterFail = meetingRepository.findById(meetingId).orElseThrow();
        assertThat(meetingAfterFail.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(meetingUpdateRepository.findByMeetingIdOrderByUpdateNumberAsc(meetingId)).isEmpty();
        assertThat(meetingRepository.countByLeadId(lead.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("5. RM follow-up date: Past date fails before mutation")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN"})
    void test5_rmFollowUpDatePast_fails() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_DATE_PAST_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);
        String meetingCode = meeting.getMeetingCode();
        Long meetingId = meeting.getId();

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Follow up in past")
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.0)
                .nextPlanDate(LocalDate.now().minusDays(1))
                .build();

        assertThatThrownBy(() -> meetingWorkflowService.processWorkflow(meetingCode, workflowReq, ADMIN_USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("RM follow-up date cannot be in the past.");

        // Verify no mutation occurred
        Meeting meetingAfterFail = meetingRepository.findById(meetingId).orElseThrow();
        assertThat(meetingAfterFail.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(meetingUpdateRepository.findByMeetingIdOrderByUpdateNumberAsc(meetingId)).isEmpty();
        assertThat(meetingRepository.countByLeadId(lead.getId())).isEqualTo(1);
    }

    // =========================================================================
    // TEST 6, 7, 8: RM CONDUCTED WITHOUT LATITUDE, LONGITUDE, ACCURACY FAILS
    // =========================================================================

    @Test
    @DisplayName("6. RM conducted without latitude fails before mutation")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN"})
    void test6_rmConductedWithoutLatitude_fails() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_NO_LAT_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Missing latitude")
                .latitude(null)
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.0)
                .nextPlanDate(LocalDate.now().plusDays(5))
                .build();

        assertThatThrownBy(() -> meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Latitude is required for RM conducted meeting.");

        Meeting m = meetingRepository.findById(meeting.getId()).orElseThrow();
        assertThat(m.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
    }

    @Test
    @DisplayName("7. RM conducted without longitude fails before mutation")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN"})
    void test7_rmConductedWithoutLongitude_fails() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_NO_LNG_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Missing longitude")
                .latitude(new BigDecimal("19.076090"))
                .longitude(null)
                .accuracy(10.0)
                .nextPlanDate(LocalDate.now().plusDays(5))
                .build();

        assertThatThrownBy(() -> meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Longitude is required for RM conducted meeting.");

        Meeting m = meetingRepository.findById(meeting.getId()).orElseThrow();
        assertThat(m.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
    }

    @Test
    @DisplayName("8. RM conducted without accuracy fails before mutation")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN"})
    void test8_rmConductedWithoutAccuracy_fails() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_NO_ACC_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Missing accuracy")
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(null)
                .nextPlanDate(LocalDate.now().plusDays(5))
                .build();

        assertThatThrownBy(() -> meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Location accuracy is required for RM conducted meeting.");

        Meeting m = meetingRepository.findById(meeting.getId()).orElseThrow();
        assertThat(m.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
    }

    // =========================================================================
    // TEST 9: RM CONDUCTED WITH VALID GEO SUCCEEDS (ADDRESS OPTIONAL)
    // =========================================================================

    @Test
    @DisplayName("9. RM conducted with valid Geo and omitted address succeeds")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN"})
    void test9_rmConductedValidGeoNoAddress_succeeds() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_VALID_GEO_NO_ADDR_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Address omitted")
                .address(null) // Address is optional
                .latitude(new BigDecimal("18.520430"))
                .longitude(new BigDecimal("73.856744"))
                .accuracy(5.0)
                .nextPlanDate(LocalDate.now().plusDays(3))
                .build();

        MeetingResponse resp = meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);
        assertThat(resp).isNotNull();

        Meeting m = meetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(m.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(m.getLatitude()).isEqualByComparingTo(new BigDecimal("18.520430"));
        assertThat(m.getLongitude()).isEqualByComparingTo(new BigDecimal("73.856744"));
    }

    // =========================================================================
    // TEST 14: NO DUPLICATE SCHEDULED MEETING CREATED
    // =========================================================================

    @Test
    @DisplayName("14. Idempotency: Follow-up creates exactly one scheduled meeting without duplicates")
    @WithMockUser(username = ADMIN_USER, authorities = {"ROLE_SUPER_ADMIN"})
    void test14_noDuplicateScheduledMeetingCreated() {
        Lead lead = createTemporaryLeadAssignedToRm("RM_IDEMPOTENCY_LEAD");
        Meeting meeting = createFirstScheduledMeeting(lead);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("First workflow update")
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(8.0)
                .nextPlanDate(LocalDate.now().plusDays(7))
                .build();

        meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);

        // Verify exactly 1 SCHEDULED meeting exists for this lead
        long scheduledCount = meetingRepository.countByLeadIdAndMeetingStatus(lead.getId(), MeetingStatus.SCHEDULED);
        assertThat(scheduledCount).isEqualTo(1);

        long totalCount = meetingRepository.countByLeadId(lead.getId());
        assertThat(totalCount).isEqualTo(2); // 1 Completed + 1 Scheduled
    }
}
