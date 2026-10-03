package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.dto.request.AssignLeadRequest;
import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.LeadSource;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingUpdate;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingUpdateRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.repository.RoleRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * =============================================================================
 * RM Meeting Workflow-Update Authorization Integration Test
 * =============================================================================
 *
 * Verifies that the common POST /v1/meetings/{meetingCode}/workflow-update endpoint
 * permits RELATIONSHIP_MANAGER access while strictly enforcing:
 * 1. Role-based / permission-based authorization at @PreAuthorize
 * 2. Ownership-based authorization (own assigned meeting only)
 * 3. Rejection of unassigned meetings (RM accessing another RM's or SM's meeting)
 * 4. Unbroken existing SM workflow authorization
 * 5. Unbroken existing ADMIN / SUPER_ADMIN authorization
 * 6. Business date validation execution after authorization succeeds
 * 7. Business Geo validation execution after authorization succeeds
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class RmMeetingWorkflowAuthorizationIntegrationTest {

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
    private DesignationRepository designationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private LeadService leadService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private MeetingUpdateRepository meetingUpdateRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private SalesRoleResolver salesRoleResolver;

    private final Set<Long> createdUserIds = new HashSet<>();
    private final Set<Long> createdLeadIds = new HashSet<>();
    private final Set<Long> createdMeetingIds = new HashSet<>();

    private User rmUser1;
    private User rmUser2;
    private User smUser;

    @BeforeEach
    void setUp() {
        rmUser1 = createTestUser("RM", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, "RM");
        rmUser2 = createTestUser("RM2", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, "RM");
        smUser = createTestUser("SM", BootstrapConstants.ROLE_SALES_MANAGER, BootstrapConstants.DESIG_SM);
    }

    @AfterEach
    void tearDown() {
        // Deterministic cleanup in reverse dependency order:
        // Delete all meetings (initial + follow-up) for created leads
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
    // TEST 1: RM + OWN ASSIGNED MEETING -> AUTHORIZED & WORKFLOW SUCCEEDS (HTTP 200)
    // =========================================================================

    @Test
    @DisplayName("TEST 1: RELATIONSHIP_MANAGER + own assigned meeting -> workflow-update succeeds")
    void test1_rmOwnMeeting_workflowUpdate_success() throws Exception {
        Lead lead = createLeadAssignedTo(rmUser1, "RM_Client_Own");
        Meeting meeting = scheduleMeetingFor(lead, rmUser1);

        Map<String, Object> payload = createValidRmWorkflowPayload(LocalDate.now().plusDays(7));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(rmUser1.getEmployeeCode()).roles("RELATIONSHIP_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // TEST 2: RM + ANOTHER RM's MEETING -> 403 FORBIDDEN (OWNERSHIP ENFORCED)
    // =========================================================================

    @Test
    @DisplayName("TEST 2: RELATIONSHIP_MANAGER + another RM's meeting -> 403 Access Denied")
    void test2_rmAnotherRmMeeting_forbidden() throws Exception {
        Lead lead = createLeadAssignedTo(rmUser2, "RM2_Client");
        Meeting meeting = scheduleMeetingFor(lead, rmUser2);

        Map<String, Object> payload = createValidRmWorkflowPayload(LocalDate.now().plusDays(7));

        // rmUser1 attempts to update rmUser2's meeting
        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(rmUser1.getEmployeeCode()).roles("RELATIONSHIP_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 3: RM + SM-OWNED MEETING -> 403 FORBIDDEN
    // =========================================================================

    @Test
    @DisplayName("TEST 3: RELATIONSHIP_MANAGER + SM-owned meeting -> 403 Access Denied")
    void test3_rmSmOwnedMeeting_forbidden() throws Exception {
        Lead lead = createLeadAssignedTo(smUser, "SM_Client");
        Meeting meeting = scheduleMeetingFor(lead, smUser);

        Map<String, Object> payload = createValidRmWorkflowPayload(LocalDate.now().plusDays(7));

        // rmUser1 attempts to update smUser's meeting
        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(rmUser1.getEmployeeCode()).roles("RELATIONSHIP_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 4: SM WORKFLOW-UPDATE -> STILL SUCCEEDS (HTTP 200)
    // =========================================================================

    @Test
    @DisplayName("TEST 4: Existing SM workflow-update -> still succeeds")
    void test4_smWorkflowUpdate_success() throws Exception {
        Lead lead = createLeadAssignedTo(smUser, "SM_Client_Direct");
        Meeting meeting = scheduleMeetingFor(lead, smUser);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingConducted", "CONDUCTED");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("remarks", "SM meeting conducted successfully");
        payload.put("nextPlanDate", LocalDate.now().plusDays(3).toString());
        payload.put("nextPlanTime", "11:00:00");

        // SM user with authority MEETING_UPDATE
        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(smUser.getEmployeeCode()).authorities(() -> "MEETING_UPDATE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // TEST 5: ADMIN / SUPER_ADMIN -> UNCHANGED ELEVATED ACCESS (HTTP 200)
    // =========================================================================

    @Test
    @DisplayName("TEST 5: Admin / Super Admin elevated workflow-update -> succeeds")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN"})
    void test5_adminWorkflowUpdate_success() throws Exception {
        Lead lead = createLeadAssignedTo(rmUser1, "Admin_Managed_RM_Lead");
        Meeting meeting = scheduleMeetingFor(lead, rmUser1);

        Map<String, Object> payload = createValidRmWorkflowPayload(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // TEST 6: RM MEETING + PAST NEXT PLAN DATE -> REJECTED BY BUSINESS VALIDATION (HTTP 400)
    // =========================================================================

    @Test
    @DisplayName("TEST 6: RM meeting + invalid past nextPlanDate -> rejects with 400 Bad Request (NOT 403)")
    void test6_rmPastDate_rejectedWithBadRequest() throws Exception {
        Lead lead = createLeadAssignedTo(rmUser1, "RM_Date_Validation_Client");
        Meeting meeting = scheduleMeetingFor(lead, rmUser1);

        // Date in past
        Map<String, Object> payload = createValidRmWorkflowPayload(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(rmUser1.getEmployeeCode()).roles("RELATIONSHIP_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TEST 7: RM MEETING + MISSING GEO FOR CONDUCTED -> REJECTED BY BUSINESS VALIDATION (HTTP 400)
    // =========================================================================

    @Test
    @DisplayName("TEST 7: RM meeting + missing latitude/longitude/accuracy for CONDUCTED -> rejects with 400 Bad Request (NOT 403)")
    void test7_rmMissingGeo_rejectedWithBadRequest() throws Exception {
        Lead lead = createLeadAssignedTo(rmUser1, "RM_Geo_Validation_Client");
        Meeting meeting = scheduleMeetingFor(lead, rmUser1);

        // Missing latitude, longitude, and accuracy
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingConducted", "CONDUCTED");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("remarks", "RM meeting without Geo");
        payload.put("nextPlanDate", LocalDate.now().plusDays(5).toString());
        payload.put("nextPlanTime", "14:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(rmUser1.getEmployeeCode()).roles("RELATIONSHIP_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // TEST HELPERS
    // =========================================================================

    private Map<String, Object> createValidRmWorkflowPayload(LocalDate nextPlanDate) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingConducted", "CONDUCTED");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("remarks", "Comprehensive RM client wealth portfolio review");
        payload.put("latitude", 19.0760);
        payload.put("longitude", 72.8777);
        payload.put("accuracy", 5.0);
        payload.put("address", "Bandra Kurla Complex, Mumbai");
        if (nextPlanDate != null) {
            payload.put("nextPlanDate", nextPlanDate.toString());
            payload.put("nextPlanTime", "15:00:00");
        }
        return payload;
    }

    private User createTestUser(String prefix, String roleCode, String desigCode) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        Role role = roleRepository.findByCodeIgnoreCase(roleCode)
                .or(() -> roleRepository.findByCodeIgnoreCase("ROLE_" + roleCode))
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(roleCode)
                        .name(roleCode + " Role")
                        .status(Status.ACTIVE)
                        .build()));

        Designation desig = designationRepository.findByCodeIgnoreCaseAndDeletedFalse(desigCode)
                .orElseGet(() -> designationRepository.save(Designation.builder()
                        .code(desigCode)
                        .name(desigCode + " Designation")
                        .hierarchyLevel(4)
                        .status(Status.ACTIVE)
                        .build()));

        Department salesDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        Team team = teamRepository.findAll().stream().findFirst().orElse(null);

        User user = User.builder()
                .employeeCode(prefix + "_" + uniqueSuffix.toUpperCase())
                .firstName("Test_" + prefix)
                .lastName("Automated")
                .email(prefix.toLowerCase() + "." + uniqueSuffix + "@blueantcrm.com")
                .mobileNumber("9" + String.valueOf(System.currentTimeMillis()).substring(4, 13))
                .password("$2a$10$e8w.Kz2k6D7P1R3j3lY5aOb3Xz8pQ8mQ0fW9yY3t1r4y2x1w0z")
                .gender(Gender.MALE)
                .status(Status.ACTIVE)
                .role(role)
                .designation(desig)
                .department(salesDept)
                .team(team)
                .build();

        User savedUser = userRepository.save(user);
        createdUserIds.add(savedUser.getId());
        return savedUser;
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
                .assignmentReason("Assignment for automated test")
                .build();
        leadService.assignLead(assignReq, ADMIN_USER);

        Lead assignedLead = leadRepository.findById(lead.getId()).orElseThrow();
        return assignedLead;
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
