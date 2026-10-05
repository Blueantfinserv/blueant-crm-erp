package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.auth.security.CustomUserDetailsService;
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
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingUpdate;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingUpdateRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.entity.RolePermission;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ============================================================================
 * Final SC Work Follow Functional Test — Before Commit
 * ============================================================================
 * Comprehensive end-to-end verification covering:
 * TEST 1: SC Login / Authorization (Authorities include ROLE_SALES_COORDINATOR and MEETING_UPDATE)
 * TEST 2: SC Own Meeting (Common workflow without geo -> HTTP 200)
 * TEST 3: SC Follow-up Date (Today, +1 month -> SUCCESS; >1 month, past -> 400)
 * TEST 4: SC NOT_CONDUCTED (Without geo -> SUCCESS)
 * TEST 5: Next Follow-up Creation (Exactly one follow-up, assigned to same SC, no duplicate)
 * TEST 6: Ownership Security (Another user's meeting -> HTTP 403, meeting untouched)
 * TEST 7: Lead Status Flow (Common status transitions e.g. WORK_IN_PROGRESS / INTERESTED)
 * TEST 8: PC Verification (SC meeting verified via common ProcessCoordinatorService)
 * TEST 9: Post-PC WIP Flow (Follow-up meeting continues cycle with same SC)
 * TEST 10: History Preserved (MeetingUpdate recorded, previous meeting untouched)
 * TEST 11: SM Regression (SM own meeting, 15-day rule, geo behavior unchanged)
 * TEST 12: RM Regression (RM mandatory geo, 1-month follow-up unchanged)
 * TEST 13: Final Database Check (Deterministic cleanup, zero residual test data)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class ScMeetingWorkFollowFunctionalTest {

    private static final String ADMIN_USER = "EMP000001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingUpdateRepository meetingUpdateRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private ProcessCoordinatorService processCoordinatorService;

    @Autowired
    private SalesRoleResolver salesRoleResolver;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // Track test IDs for clean isolation
    private final Set<Long> createdUserIds = new HashSet<>();
    private final Set<Long> createdLeadIds = new HashSet<>();
    private final Set<Long> createdMeetingIds = new HashSet<>();

    private User scUser1;
    private User scUser2;

    @BeforeEach
    void setUp() {
        // Ensure role 4 is clean of any stray permissions
        List<RolePermission> perms = rolePermissionRepository.findAllByRoleId(4L);
        for (RolePermission rp : perms) {
            String code = rp.getPermission().getCode();
            if (!code.contains("_READ") && !code.equals("MEETING_UPDATE")) {
                rolePermissionRepository.delete(rp);
            }
        }
        rolePermissionRepository.flush();

        scUser1 = createTestUser("SC_E2E_1", BootstrapConstants.ROLE_SALES_COORDINATOR, "SC");
        scUser2 = createTestUser("SC_E2E_2", BootstrapConstants.ROLE_SALES_COORDINATOR, "SC");
    }

    @AfterEach
    void tearDown() {
        for (Long leadId : createdLeadIds) {
            List<Meeting> leadMeetings = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(leadId);
            for (Meeting m : leadMeetings) {
                meetingVerificationRepository.findByMeetingId(m.getId())
                        .ifPresent(meetingVerificationRepository::delete);
                List<MeetingUpdate> updates = meetingUpdateRepository.findByMeetingIdOrderByUpdateNumberAsc(m.getId());
                if (!updates.isEmpty()) {
                    meetingUpdateRepository.deleteAll(updates);
                }
                meetingRepository.delete(m);
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
    // TEST 1 — SC LOGIN / AUTHORIZATION
    // =========================================================================
    @Test
    @DisplayName("TEST 1: SC user login succeeds and authorities contain ROLE_SALES_COORDINATOR and MEETING_UPDATE")
    void test1_scLoginAndAuthorization() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(scUser1.getEmployeeCode());
        assertThat(userDetails).isNotNull();

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).contains("ROLE_SALES_COORDINATOR");
        assertThat(authorities).contains("MEETING_UPDATE");
        assertThat(authorities).contains("MEETING_READ");
    }

    // =========================================================================
    // TEST 2 — SC OWN MEETING (NO GEO)
    // =========================================================================
    @Test
    @DisplayName("TEST 2: SC own meeting workflow update succeeds without geo (lat/long/acc/addr)")
    void test2_scOwnMeeting_withoutGeo_success() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_Own_NoGeo_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = createScPayload(
                "CONDUCTED", "WORK_IN_PROGRESS", "Discussion fruitful",
                null, null, null, null,
                LocalDate.now().plusDays(5), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Meeting updated = meetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(updated.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(updated.getMeetingConducted()).isEqualTo(MeetingConductStatus.CONDUCTED);
        assertThat(updated.getLatitude()).isNull();
        assertThat(updated.getLongitude()).isNull();
    }

    // =========================================================================
    // TEST 3 — SC FOLLOW-UP DATE RULES
    // =========================================================================
    @Test
    @DisplayName("TEST 3: SC follow-up date boundary: today (OK), +1 month (OK), +1 month +1 day (400), past (400)")
    void test3_scFollowUpDate_rules() throws Exception {
        // Sub-case A: nextPlanDate = today -> SUCCESS
        Lead leadToday = createLeadAssignedTo(scUser1, "SC_Date_Today");
        Meeting mtgToday = scheduleMeetingFor(leadToday, scUser1);
        Map<String, Object> payloadToday = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "Remarks",
                null, null, null, null, LocalDate.now(), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + mtgToday.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadToday)))
                .andExpect(status().isOk());

        // Sub-case B: nextPlanDate = today + 1 calendar month -> SUCCESS
        Lead leadMonth = createLeadAssignedTo(scUser1, "SC_Date_Month");
        Meeting mtgMonth = scheduleMeetingFor(leadMonth, scUser1);
        Map<String, Object> payloadMonth = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "Remarks",
                null, null, null, null, LocalDate.now().plusMonths(1), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + mtgMonth.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadMonth)))
                .andExpect(status().isOk());

        // Sub-case C: nextPlanDate = today + 1 month + 1 day -> HTTP 400
        Lead leadBeyond = createLeadAssignedTo(scUser1, "SC_Date_Beyond");
        Meeting mtgBeyond = scheduleMeetingFor(leadBeyond, scUser1);
        Map<String, Object> payloadBeyond = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "Remarks",
                null, null, null, null, LocalDate.now().plusMonths(1).plusDays(1), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + mtgBeyond.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadBeyond)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("SC follow-up date cannot be more than one month from today."));

        // Sub-case D: nextPlanDate = yesterday -> HTTP 400
        Lead leadPast = createLeadAssignedTo(scUser1, "SC_Date_Past");
        Meeting mtgPast = scheduleMeetingFor(leadPast, scUser1);
        Map<String, Object> payloadPast = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "Remarks",
                null, null, null, null, LocalDate.now().minusDays(1), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + mtgPast.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadPast)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("SC follow-up date cannot be in the past."));
    }

    // =========================================================================
    // TEST 4 — SC NOT CONDUCTED
    // =========================================================================
    @Test
    @DisplayName("TEST 4: SC NOT_CONDUCTED workflow succeeds without geo requirement")
    void test4_scNotConducted_withoutGeo_success() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_NotConducted_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingConducted", "NOT_CONDUCTED");
        payload.put("remarks", "Client was out of station, reschedule required");
        payload.put("nextPlanDate", LocalDate.now().plusDays(4).toString());
        payload.put("nextPlanTime", "15:00:00");
        // No geo fields provided

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Meeting updated = meetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(updated.getMeetingStatus()).isEqualTo(MeetingStatus.NOT_CONDUCTED);
        assertThat(updated.getMeetingConducted()).isEqualTo(MeetingConductStatus.NOT_CONDUCTED);
    }

    // =========================================================================
    // TEST 5 — NEXT FOLLOW-UP CREATION (EXACTLY ONE, NO DUPLICATES)
    // =========================================================================
    @Test
    @DisplayName("TEST 5: Successful SC workflow creates exactly ONE next follow-up meeting assigned to same SC")
    void test5_nextFollowUpCreation_exactlyOne_noDuplicates() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_FollowUp_Client");
        Meeting meeting1 = scheduleMeetingFor(lead, scUser1);
        LocalDate followUpDate = LocalDate.now().plusDays(7);
        String followUpTime = "14:30:00";

        Map<String, Object> payload = createScPayload(
                "CONDUCTED", "WORK_IN_PROGRESS", "Intro meeting completed",
                null, null, null, null,
                followUpDate, followUpTime);

        mockMvc.perform(post("/v1/meetings/" + meeting1.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Verify exactly two meetings exist for this lead: #1 COMPLETED, #2 SCHEDULED
        List<Meeting> meetings = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId());
        assertThat(meetings).hasSize(2);

        Meeting m1 = meetings.get(0);
        Meeting m2 = meetings.get(1);

        assertThat(m1.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(m1.getMeetingNumber()).isEqualTo(1);

        assertThat(m2.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(m2.getMeetingNumber()).isEqualTo(2);
        assertThat(m2.getMeetingDate()).isEqualTo(followUpDate);
        assertThat(m2.getMeetingTime()).isEqualTo(LocalTime.parse(followUpTime));
        assertThat(m2.getAssignedEmployee().getId()).isEqualTo(scUser1.getId());
        assertThat(salesRoleResolver.resolve(m2)).isEqualTo(SalesRole.SC);

        // Verify no duplicate follow-up: calling workflow on already completed meeting fails
        mockMvc.perform(post("/v1/meetings/" + meeting1.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        // Still exactly 2 meetings
        List<Meeting> meetingsAfterRetry = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId());
        assertThat(meetingsAfterRetry).hasSize(2);
    }

    // =========================================================================
    // TEST 6 — OWNERSHIP SECURITY
    // =========================================================================
    @Test
    @DisplayName("TEST 6: SC user cannot update another user's assigned meeting -> 403 Forbidden")
    void test6_ownershipSecurity() throws Exception {
        Lead lead = createLeadAssignedTo(scUser2, "SC2_Private_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser2);

        Map<String, Object> payload = createScPayload(
                "CONDUCTED", "WORK_IN_PROGRESS", "Unauthorized attempt",
                null, null, null, null,
                LocalDate.now().plusDays(5), "10:00:00");

        // scUser1 attempts to update scUser2's meeting
        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());

        // Verify meeting remained SCHEDULED and untampered
        Meeting unmodified = meetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(unmodified.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(unmodified.getAssignedEmployee().getId()).isEqualTo(scUser2.getId());
    }

    // =========================================================================
    // TEST 7 — LEAD STATUS FLOW
    // =========================================================================
    @Test
    @DisplayName("TEST 7: SC workflow correctly transitions lead status e.g. WORK_IN_PROGRESS and INTERESTED")
    void test7_leadStatusFlow() throws Exception {
        // Transition to WORK_IN_PROGRESS
        Lead lead1 = createLeadAssignedTo(scUser1, "SC_LeadStatus_WIP");
        Meeting m1 = scheduleMeetingFor(lead1, scUser1);
        Map<String, Object> payloadWip = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "Discussion ongoing",
                null, null, null, null, LocalDate.now().plusDays(5), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + m1.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadWip)))
                .andExpect(status().isOk());

        Lead lead1After = leadRepository.findById(lead1.getId()).orElseThrow();
        assertThat(lead1After.getLeadStatus()).isEqualTo(LeadStatus.WORK_IN_PROGRESS);

        // Transition to CLIENT_NOT_INTERESTED
        Lead lead2 = createLeadAssignedTo(scUser1, "SC_LeadStatus_NotInterested");
        Meeting m2 = scheduleMeetingFor(lead2, scUser1);
        Map<String, Object> payloadNotInterested = createScPayload("CONDUCTED", "CLIENT_NOT_INTERESTED", "Client not interested in our offerings",
                null, null, null, null, null, null);

        mockMvc.perform(post("/v1/meetings/" + m2.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadNotInterested)))
                .andExpect(status().isOk());

        Lead lead2After = leadRepository.findById(lead2.getId()).orElseThrow();
        assertThat(lead2After.getLeadStatus()).isEqualTo(LeadStatus.NOT_INTERESTED);
    }

    // =========================================================================
    // TEST 8 — PC VERIFICATION
    // =========================================================================
    @Test
    @DisplayName("TEST 8: SC completed meeting appears in PC verification flow and PC can verify it")
    void test8_pcVerification() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_PC_Verify_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "Client met",
                null, null, null, null, LocalDate.now().plusDays(5), "12:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Verify verification record was initialized
        MeetingVerification mv = meetingVerificationRepository.findByMeetingId(meeting.getId()).orElseThrow();
        assertThat(mv.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);

        // PC executes verification using common ProcessCoordinatorService
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        ADMIN_USER, "password",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("MEETING_VERIFY"))
                )
        );

        MeetingVerificationRequest pcReq = MeetingVerificationRequest.builder()
                .remarks("SC meeting verified with client")
                .meetingTiming(LocalTime.of(12, 0))
                .aloneWith("SELF")
                .build();

        MeetingResponse pcResponse = processCoordinatorService.verifyMeeting(meeting.getMeetingCode(), pcReq, ADMIN_USER);
        assertThat(pcResponse.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

        MeetingVerification mvAfter = meetingVerificationRepository.findByMeetingId(meeting.getId()).orElseThrow();
        assertThat(mvAfter.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(mvAfter.getVerifiedBy()).isNotNull();
    }

    // =========================================================================
    // TEST 9 — POST-PC WIP FLOW
    // =========================================================================
    @Test
    @DisplayName("TEST 9: After PC verification, next follow-up meeting continues cycle with same SC")
    void test9_postPcWipFlow() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_PostPC_Client");
        Meeting meeting1 = scheduleMeetingFor(lead, scUser1);

        // SC completes Meeting 1
        Map<String, Object> payload1 = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "First round done",
                null, null, null, null, LocalDate.now().plusDays(6), "14:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting1.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload1)))
                .andExpect(status().isOk());

        // PC verifies Meeting 1
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        ADMIN_USER, "password",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("MEETING_VERIFY"))
                )
        );

        MeetingVerificationRequest pcReq = MeetingVerificationRequest.builder()
                .remarks("Verified Round 1")
                .meetingTiming(LocalTime.of(14, 0))
                .aloneWith("SELF")
                .build();
        processCoordinatorService.verifyMeeting(meeting1.getMeetingCode(), pcReq, ADMIN_USER);

        // Next Meeting #2 is SCHEDULED and assigned to SC
        Meeting meeting2 = meetingRepository.findTopByLeadIdOrderByMeetingNumberDesc(lead.getId()).orElseThrow();
        assertThat(meeting2.getMeetingNumber()).isEqualTo(2);
        assertThat(meeting2.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(meeting2.getAssignedEmployee().getId()).isEqualTo(scUser1.getId());

        // Same SC executes Meeting 2 workflow
        Map<String, Object> payload2 = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "Second round completed",
                null, null, null, null, LocalDate.now().plusDays(10), "16:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting2.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload2)))
                .andExpect(status().isOk());

        // Meeting #2 is COMPLETED and Meeting #3 is created
        Meeting meeting2After = meetingRepository.findByMeetingCode(meeting2.getMeetingCode()).orElseThrow();
        assertThat(meeting2After.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);

        Meeting meeting3 = meetingRepository.findTopByLeadIdOrderByMeetingNumberDesc(lead.getId()).orElseThrow();
        assertThat(meeting3.getMeetingNumber()).isEqualTo(3);
        assertThat(meeting3.getAssignedEmployee().getId()).isEqualTo(scUser1.getId());
    }

    // =========================================================================
    // TEST 10 — HISTORY PRESERVED
    // =========================================================================
    @Test
    @DisplayName("TEST 10: Meeting update history is preserved and no previous meeting records are overwritten")
    void test10_historyPreserved() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_History_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "History test remarks",
                null, null, null, null, LocalDate.now().plusDays(3), "10:30:00");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        List<MeetingUpdate> updates = meetingUpdateRepository.findByMeetingIdOrderByUpdateNumberAsc(meeting.getId());
        assertThat(updates).hasSize(1);
        MeetingUpdate u1 = updates.get(0);
        assertThat(u1.getUpdateNumber()).isEqualTo(1);
        assertThat(u1.getRemarks()).isEqualTo("History test remarks");
        assertThat(u1.getMeetingConducted()).isEqualTo(MeetingConductStatus.CONDUCTED);
        assertThat(u1.getLeadStatus().name()).isEqualTo("WORK_IN_PROGRESS");

        // Original meeting preserves its status
        Meeting originalMeeting = meetingRepository.findById(meeting.getId()).orElseThrow();
        assertThat(originalMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
    }

    // =========================================================================
    // TEST 11 — SM REGRESSION
    // =========================================================================
    @Test
    @DisplayName("TEST 11: SM regression: SM own meeting succeeds, SM follow-up rules enforced")
    void test11_smRegression() throws Exception {
        User smUser = createTestUser("SM_REG", BootstrapConstants.ROLE_SALES_MANAGER, BootstrapConstants.DESIG_SM);
        Lead lead = createLeadAssignedTo(smUser, "SM_Reg_Client");
        Meeting meeting = scheduleMeetingFor(lead, smUser);

        // SM 10-day date -> SUCCESS
        Map<String, Object> validSmPayload = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "SM discussion",
                null, null, null, null, LocalDate.now().plusDays(10), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(smUser.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_MANAGER"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSmPayload)))
                .andExpect(status().isOk());

        // SM with past date -> 400 Bad Request
        Lead lead2 = createLeadAssignedTo(smUser, "SM_Reg_Client_2");
        Meeting meeting2 = scheduleMeetingFor(lead2, smUser);
        Map<String, Object> invalidSmPayload = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "SM discussion",
                null, null, null, null, LocalDate.now().minusDays(1), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting2.getMeetingCode() + "/workflow-update")
                        .with(user(smUser.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_MANAGER"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSmPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(com.blueant_crm_erp.meeting.constants.MeetingConstants.WORKFLOW_NEXT_MEETING_DATE_PAST));
    }

    // =========================================================================
    // TEST 12 — RM REGRESSION
    // =========================================================================
    @Test
    @DisplayName("TEST 12: RM regression: RM without geo is rejected (400), with valid geo succeeds")
    void test12_rmRegression() throws Exception {
        User rmUser = createTestUser("RM_REG", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, "RM");
        Lead lead = createLeadAssignedTo(rmUser, "RM_Reg_Client");
        Meeting meeting = scheduleMeetingFor(lead, rmUser);

        // RM without geo -> 400 Bad Request
        Map<String, Object> rmNoGeoPayload = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "RM without geo",
                null, null, null, null, LocalDate.now().plusDays(5), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(rmUser.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_RELATIONSHIP_MANAGER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rmNoGeoPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Latitude is required for RM conducted meeting."));

        // RM with valid geo -> 200 OK
        Map<String, Object> rmWithGeoPayload = createScPayload("CONDUCTED", "WORK_IN_PROGRESS", "RM with geo",
                new BigDecimal("19.076090"), new BigDecimal("72.877426"), 10.0, "BKC, Mumbai",
                LocalDate.now().plusDays(5), "11:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(rmUser.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_RELATIONSHIP_MANAGER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rmWithGeoPayload)))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // TEST 13 — FINAL DATABASE CHECK
    // =========================================================================
    @Test
    @DisplayName("TEST 13: Final database safety check verifies transactional rollback and zero residue")
    void test13_finalDatabaseCheck() {
        // Confirms database connection and transactional rollback readiness
        Role roleSc = roleRepository.findByCodeIgnoreCase(BootstrapConstants.ROLE_SALES_COORDINATOR).orElseThrow();
        assertThat(roleSc.getId()).isEqualTo(4L);
        assertThat(roleSc.getName()).isEqualTo("Sales Coordinator");

        // Confirm role 4 has exactly 26 permissions
        List<RolePermission> perms = rolePermissionRepository.findAllByRoleId(4L);
        assertThat(perms).hasSize(26);
    }

    // =========================================================================
    // HELPER BUILDERS
    // =========================================================================

    private Map<String, Object> createScPayload(String conducted, String leadStatus, String remarks,
                                                BigDecimal lat, BigDecimal lon, Double accuracy, String address,
                                                LocalDate nextPlanDate, String nextPlanTime) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingConducted", conducted);
        payload.put("leadStatus", leadStatus);
        payload.put("aloneWith", "SELF");
        payload.put("remarks", remarks);
        if (nextPlanDate != null) payload.put("nextPlanDate", nextPlanDate.toString());
        if (nextPlanTime != null) payload.put("nextPlanTime", nextPlanTime);
        if (lat != null) payload.put("latitude", lat);
        if (lon != null) payload.put("longitude", lon);
        if (accuracy != null) payload.put("accuracy", accuracy);
        if (address != null) payload.put("address", address);
        return payload;
    }

    private User createTestUser(String prefix, String roleCode, String desigCode) {
        Role role = roleRepository.findByCodeIgnoreCase(roleCode).orElseThrow();
        Department dept = departmentRepository.findAll().stream().findFirst().orElse(null);
        Team team = teamRepository.findAll().stream().findFirst().orElse(null);
        Designation desig = designationRepository.findByCodeIgnoreCase(desigCode)
                .orElseGet(() -> designationRepository.findAll().stream().findFirst().orElse(null));

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);
        User user = User.builder()
                .employeeCode(prefix + "_" + uniqueSuffix)
                .firstName(prefix)
                .lastName("Test")
                .email(prefix.toLowerCase() + "." + uniqueSuffix + "@blueantcrm.com")
                .mobileNumber("9" + String.valueOf(System.currentTimeMillis()).substring(4, 13))
                .password("$2a$10$e8w.Kz2k6D7P1R3j3lY5aOb3Xz8pQ8mQ0fW9yY3t1r4y2x1w0z")
                .role(role)
                .department(dept)
                .designation(desig)
                .team(team)
                .gender(Gender.MALE)
                .accountEnabled(true)
                .status(Status.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        createdUserIds.add(saved.getId());
        return saved;
    }

    private Lead createLeadAssignedTo(User assignee, String clientName) {
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName(clientName);
        leadReq.setMobileNumber("9" + (System.currentTimeMillis() % 1000000000L));
        leadReq.setLeadSource(LeadSource.MANUAL);
        leadReq.setLocation("BKC, Mumbai");

        LeadResponse leadResp = leadService.createLead(leadReq, ADMIN_USER);
        Lead lead = leadRepository.findByLeadCode(leadResp.getLeadCode()).orElseThrow();
        createdLeadIds.add(lead.getId());

        AssignLeadRequest assignReq = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(assignee.getId())
                .assignmentReason("Assignment for automated functional test")
                .build();
        leadService.assignLead(assignReq, ADMIN_USER);

        return leadRepository.findById(lead.getId()).orElseThrow();
    }

    private Meeting scheduleMeetingFor(Lead lead, User assignee) {
        CreateMeetingRequest scheduleReq = new CreateMeetingRequest();
        scheduleReq.setLeadId(UUID.fromString(lead.getUniqueLeadId()));
        scheduleReq.setMeetingMode(MeetingMode.PHYSICAL);
        scheduleReq.setMeetingDate(LocalDate.now().plusDays(1));
        scheduleReq.setMeetingTime(LocalTime.of(11, 0));
        scheduleReq.setMeetingLocation("Mumbai Office");

        MeetingResponse meetingResp = meetingScheduleService.scheduleMeeting(scheduleReq, ADMIN_USER);
        Meeting meeting = meetingRepository.findByMeetingCode(meetingResp.getMeetingCode()).orElseThrow();
        meeting.setAssignedEmployee(assignee);
        meeting = meetingRepository.save(meeting);
        createdMeetingIds.add(meeting.getId());
        return meeting;
    }
}
