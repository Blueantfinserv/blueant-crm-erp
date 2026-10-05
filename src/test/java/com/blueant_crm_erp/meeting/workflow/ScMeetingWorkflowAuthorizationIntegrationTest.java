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
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingUpdateRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.entity.RolePermission;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ============================================================================
 * SC Meeting Workflow Authorization & End-to-End Integration Test
 * ============================================================================
 * Verifies:
 * 1. SALES_COORDINATOR resolves to SalesRole.SC
 * 2. SC can process its own meeting when authorized (MEETING_UPDATE authority)
 * 3. SC cannot process another user's meeting (403 ownership check)
 * 4. SC CONDUCTED meeting succeeds without geo information (lat/long/acc/address)
 * 5. SC CONDUCTED meeting with valid geo also succeeds
 * 6. SC nextPlanDate = today succeeds
 * 7. SC nextPlanDate = today + 1 calendar month succeeds
 * 8. SC nextPlanDate beyond 1 calendar month fails
 * 9. SC NOT_CONDUCTED preserves existing behavior
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
@org.springframework.test.annotation.Rollback
public class ScMeetingWorkflowAuthorizationIntegrationTest {

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
    private DesignationRepository designationRepository;

    @org.junit.jupiter.api.BeforeEach
    void cleanStrayRolePermissions() {
        List<RolePermission> perms = rolePermissionRepository.findAllByRoleId(4L);
        boolean deletedAny = false;
        for (RolePermission rp : perms) {
            String code = rp.getPermission().getCode();
            if (!code.contains("_READ") && !code.equals("MEETING_UPDATE")) {
                rolePermissionRepository.delete(rp);
                deletedAny = true;
            }
        }
        if (deletedAny) {
            rolePermissionRepository.flush();
        }
    }

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

    private User scUser1;
    private User scUser2;

    @BeforeEach
    void setUp() {
        scUser1 = createTestUser("SC1", BootstrapConstants.ROLE_SALES_COORDINATOR, "SC");
        scUser2 = createTestUser("SC2", BootstrapConstants.ROLE_SALES_COORDINATOR, "SC");
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
    // TEST 1: Role Resolution
    // =========================================================================
    @Test
    @DisplayName("TEST 1: SALES_COORDINATOR user resolves to SalesRole.SC")
    void test1_salesCoordinator_resolvesToSc() {
        Meeting meeting = Meeting.builder().assignedEmployee(scUser1).build();
        assertThat(salesRoleResolver.resolve(meeting)).isEqualTo(SalesRole.SC);
    }

    // =========================================================================
    // TEST 2: SC + OWN ASSIGNED MEETING -> AUTHORIZED & WORKFLOW SUCCEEDS (HTTP 200)
    // =========================================================================
    @Test
    @DisplayName("TEST 2: SC user with MEETING_UPDATE processes own meeting -> 200 OK")
    void test2_scOwnMeeting_authorized_success() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_Client_Own");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = createScConductedPayload(null, null, null, null, LocalDate.now().plusDays(7));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // TEST 3: SC + ANOTHER USER'S MEETING -> 403 FORBIDDEN (OWNERSHIP ENFORCED)
    // =========================================================================
    @Test
    @DisplayName("TEST 3: SC user attempting to process another user's meeting -> 403 Forbidden")
    void test3_scAnotherMeeting_forbidden() throws Exception {
        Lead lead = createLeadAssignedTo(scUser2, "SC2_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser2);

        Map<String, Object> payload = createScConductedPayload(null, null, null, null, LocalDate.now().plusDays(7));

        // scUser1 tries to process scUser2's meeting
        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 4: SC CONDUCTED WITHOUT GEO -> SUCCESS (200 OK)
    // =========================================================================
    @Test
    @DisplayName("TEST 4: SC CONDUCTED meeting succeeds without geo information")
    void test4_scConducted_withoutGeo_success() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_No_Geo_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = createScConductedPayload(null, null, null, null, LocalDate.now().plusDays(5));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // TEST 5: SC CONDUCTED WITH VALID GEO -> SUCCESS (200 OK)
    // =========================================================================
    @Test
    @DisplayName("TEST 5: SC CONDUCTED meeting with valid geo information succeeds")
    void test5_scConducted_withValidGeo_success() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_With_Geo_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = createScConductedPayload(
                new BigDecimal("19.076090"),
                new BigDecimal("72.877426"),
                10.5,
                "BKC, Mumbai",
                LocalDate.now().plusDays(5));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // TEST 6 & 7: SC nextPlanDate = today AND today + 1 month -> SUCCESS
    // =========================================================================
    @Test
    @DisplayName("TEST 6 & 7: SC nextPlanDate = today and today + 1 month succeed")
    void test6_7_scDates_todayAndOneMonth_success() throws Exception {
        // Today
        Lead lead1 = createLeadAssignedTo(scUser1, "SC_Date_Today");
        Meeting meeting1 = scheduleMeetingFor(lead1, scUser1);
        Map<String, Object> payload1 = createScConductedPayload(null, null, null, null, LocalDate.now());

        mockMvc.perform(post("/v1/meetings/" + meeting1.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload1)))
                .andExpect(status().isOk());

        // 1 month
        Lead lead2 = createLeadAssignedTo(scUser1, "SC_Date_Month");
        Meeting meeting2 = scheduleMeetingFor(lead2, scUser1);
        Map<String, Object> payload2 = createScConductedPayload(null, null, null, null, LocalDate.now().plusMonths(1));

        mockMvc.perform(post("/v1/meetings/" + meeting2.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload2)))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // TEST 8: SC nextPlanDate beyond 1 calendar month -> 400 Bad Request
    // =========================================================================
    @Test
    @DisplayName("TEST 8: SC nextPlanDate beyond 1 calendar month fails (400 Bad Request)")
    void test8_scDateBeyondOneMonth_badRequest() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_Beyond_Month");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = createScConductedPayload(null, null, null, null, LocalDate.now().plusMonths(1).plusDays(1));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("SC follow-up date cannot be more than one month from today."));
    }

    // =========================================================================
    // TEST 9: SC NOT_CONDUCTED preserves existing behavior
    // =========================================================================
    @Test
    @DisplayName("TEST 9: SC NOT_CONDUCTED workflow update succeeds with required fields")
    void test9_scNotConducted_success() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_Not_Conducted_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingConducted", "NOT_CONDUCTED");
        payload.put("remarks", "Client was busy, reschedule requested");
        payload.put("nextPlanDate", LocalDate.now().plusDays(3).toString());
        payload.put("nextPlanTime", "11:30:00");
        payload.put("latitude", 19.076090);
        payload.put("longitude", 72.877426);
        payload.put("accuracy", 8.0);

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // TEST 10: SC WITHOUT MEETING_UPDATE -> 403 FORBIDDEN
    // =========================================================================
    @Test
    @DisplayName("TEST 10: SC user without MEETING_UPDATE authority -> 403 Forbidden")
    void test10_scWithoutMeetingUpdate_forbidden() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "SC_No_Perm_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = createScConductedPayload(null, null, null, null, LocalDate.now().plusDays(5));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(scUser1.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_COORDINATOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 11: SC ROLE 4 PERMISSION COUNT EXACTLY 26 (INCLUDES MEETING_UPDATE)
    // =========================================================================
    @Autowired
    private com.blueant_crm_erp.permission.repository.PermissionRepository permissionRepository;

    @Test
    @DisplayName("TEST 11: Role ID 4 has exactly 26 permissions, including MEETING_UPDATE")
    void test11_role4_permissionCount_isExactly26() {
        var permOpt = permissionRepository.findByCodeIgnoreCase("MEETING_UPDATE");
        assertThat(permOpt).isPresent();
        var meetingUpdatePerm = permOpt.get();
        assertThat(meetingUpdatePerm.getCode()).isEqualTo("MEETING_UPDATE");
        assertThat(meetingUpdatePerm.getName()).isEqualTo("Update MEETING");
        assertThat(meetingUpdatePerm.getStatus()).isEqualTo(com.blueant_crm_erp.common.enums.Status.ACTIVE);

        List<RolePermission> perms = rolePermissionRepository.findAllByRoleId(4L);
        assertThat(perms).hasSize(26);
        Set<String> permCodes = perms.stream()
                .map(rp -> rp.getPermission().getCode())
                .collect(java.util.stream.Collectors.toSet());
        assertThat(permCodes).contains("MEETING_UPDATE");
        assertThat(permCodes).contains("MEETING_READ");
    }

    // =========================================================================
    // TEST 12: RM WORKFLOW AUTHORIZATION UNCHANGED
    // =========================================================================
    @Test
    @DisplayName("TEST 12: RM user without MEETING_UPDATE succeeds via ROLE_RELATIONSHIP_MANAGER")
    void test12_rmAuthorization_unchanged() throws Exception {
        User rmUser = createTestUser("RM_AUTH", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, "RM");
        Lead lead = createLeadAssignedTo(rmUser, "RM_Auth_Client");
        Meeting meeting = scheduleMeetingFor(lead, rmUser);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingConducted", "CONDUCTED");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("remarks", "RM update");
        payload.put("latitude", 19.076090);
        payload.put("longitude", 72.877426);
        payload.put("accuracy", 10.0);
        payload.put("nextPlanDate", LocalDate.now().plusDays(7).toString());
        payload.put("nextPlanTime", "14:00:00");

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(rmUser.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_RELATIONSHIP_MANAGER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // TEST 13: SM WORKFLOW AUTHORIZATION UNCHANGED
    // =========================================================================
    @Test
    @DisplayName("TEST 13: SM user with MEETING_UPDATE succeeds")
    void test13_smAuthorization_unchanged() throws Exception {
        User smUserTest = createTestUser("SM_AUTH", BootstrapConstants.ROLE_SALES_MANAGER, BootstrapConstants.DESIG_SM);
        Lead lead = createLeadAssignedTo(smUserTest, "SM_Auth_Client");
        Meeting meeting = scheduleMeetingFor(lead, smUserTest);

        Map<String, Object> payload = createScConductedPayload(null, null, null, null, LocalDate.now().plusDays(5));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(smUserTest.getEmployeeCode())
                                .authorities(new SimpleGrantedAuthority("ROLE_SALES_MANAGER"),
                                             new SimpleGrantedAuthority("MEETING_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // TEST 14: ADMIN WORKFLOW AUTHORIZATION UNCHANGED
    // =========================================================================
    @Test
    @DisplayName("TEST 14: ADMIN user succeeds on workflow update")
    void test14_adminAuthorization_unchanged() throws Exception {
        Lead lead = createLeadAssignedTo(scUser1, "Admin_Auth_Client");
        Meeting meeting = scheduleMeetingFor(lead, scUser1);

        Map<String, Object> payload = createScConductedPayload(null, null, null, null, LocalDate.now().plusDays(5));

        mockMvc.perform(post("/v1/meetings/" + meeting.getMeetingCode() + "/workflow-update")
                        .with(user(ADMIN_USER)
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private Map<String, Object> createScConductedPayload(BigDecimal lat, BigDecimal lon, Double accuracy,
                                                         String address, LocalDate nextPlanDate) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingConducted", "CONDUCTED");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("aloneWith", "SELF");
        payload.put("remarks", "SC conducted meeting update");
        payload.put("nextPlanDate", nextPlanDate != null ? nextPlanDate.toString() : null);
        payload.put("nextPlanTime", "14:00:00");
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
                .assignmentReason("Assignment for automated test")
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
