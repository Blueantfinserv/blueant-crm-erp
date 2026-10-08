package com.blueant_crm_erp.lead;

import com.blueant_crm_erp.auth.dto.request.LoginRequest;
import com.blueant_crm_erp.auth.dto.response.LoginResponse;
import com.blueant_crm_erp.auth.service.AuthService;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.dto.request.AssignLeadRequest;
import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.request.LeadFilterRequest;
import com.blueant_crm_erp.lead.dto.request.LeadSearchRequest;
import com.blueant_crm_erp.lead.dto.response.LeadDetailResponse;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.LeadSource;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.service.MeetingService;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CrmLeadAssignmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

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
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User crmUser;
    private User crmOtherUser;
    private User pcCoordinatorUser;
    private User rmUser;
    private User scUser;
    private User smUser;
    private User tlUser;
    private User adminUser;
    private User superAdminUser;
    private User crmTargetUser;

    @BeforeEach
    void setUp() {
        adminUser = getOrCreateUser("ADM_CRM_TEST", BootstrapConstants.ROLE_ADMIN, "ADMIN", "Admin", null);
        superAdminUser = getOrCreateUser("SA_CRM_TEST", BootstrapConstants.ROLE_SUPER_ADMIN, "SA", "SuperAdmin", null);
        pcCoordinatorUser = getOrCreateUser("PC_TEST_USER", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, "PcCoord", null);
        rmUser = getOrCreateUser("RM_TEST_USER", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, "RmUser", null);
        scUser = getOrCreateUser("SC_TEST_USER", BootstrapConstants.ROLE_SALES_COORDINATOR, BootstrapConstants.DESIG_SC, "ScUser", null);
        smUser = getOrCreateUser("SM_TEST_USER", BootstrapConstants.ROLE_SALES_MANAGER, BootstrapConstants.DESIG_SM, "SmUser", null);
        tlUser = getOrCreateUser("TL_TEST_USER", BootstrapConstants.ROLE_TEAM_LEADER, BootstrapConstants.DESIG_TL, "TlUser", null);

        // Dedicated CRM Onboarding User: Dept = CRM (2), Role = CRM_ONBOARDING (9), Desig = CRM_ONBOARDING (7)
        crmUser = getOrCreateUser("CRM_TEST_USER", BootstrapConstants.ROLE_CRM_ONBOARDING, BootstrapConstants.DESIG_CRM_ONBOARDING, "CrmUser", BootstrapConstants.DEPT_CRM);

        // Another CRM Onboarding User (to test CRM -> CRM_ONBOARDING assignment forbidden)
        crmTargetUser = getOrCreateUser("CRM_TARGET_USER", BootstrapConstants.ROLE_CRM_ONBOARDING, BootstrapConstants.DESIG_CRM_ONBOARDING, "CrmTarget", BootstrapConstants.DEPT_CRM);

        // CRM Department employee WITHOUT CRM_ONBOARDING role
        crmOtherUser = getOrCreateUser("CRM_OTHER_USER", BootstrapConstants.ROLE_EMPLOYEE, "CRM_STAFF", "CrmOther", BootstrapConstants.DEPT_CRM);
    }

    private User getOrCreateUser(String employeeCode, String roleCode, String desigCode, String firstName, String deptCode) {
        return userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(employeeCode)
                .orElseGet(() -> {
                    Role role = roleRepository.findByCodeIgnoreCase(roleCode)
                            .orElseGet(() -> roleRepository.save(Role.builder()
                                    .name(roleCode)
                                    .code(roleCode)
                                    .displayOrder(10)
                                    .systemRole(true)
                                    .status(Status.ACTIVE)
                                    .build()));

                    Department department;
                    if (deptCode != null) {
                        department = departmentRepository.findByCodeIgnoreCase(deptCode)
                                .orElseGet(() -> departmentRepository.save(Department.builder()
                                        .name(deptCode)
                                        .code(deptCode)
                                        .displayOrder(2)
                                        .status(Status.ACTIVE)
                                        .build()));
                    } else {
                        department = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));
                    }

                    Designation designation = null;
                    if (desigCode != null) {
                        designation = designationRepository.findByCodeIgnoreCase(desigCode)
                                .orElseGet(() -> designationRepository.save(Designation.builder()
                                        .name(desigCode)
                                        .code(desigCode)
                                        .hierarchyLevel(4)
                                        .displayOrder(10)
                                        .department(department)
                                        .status(Status.ACTIVE)
                                        .build()));
                    }

                    Team team = teamRepository.findAll().stream().findFirst().orElse(null);

                    return userRepository.save(User.builder()
                            .employeeCode(employeeCode)
                            .firstName(firstName)
                            .lastName("Test")
                            .email(employeeCode.toLowerCase() + "@test.com")
                            .mobileNumber("9" + String.format("%09d", Math.abs(employeeCode.hashCode() % 1000000000L)))
                            .password(passwordEncoder.encode("password123"))
                            .gender(Gender.MALE)
                            .status(Status.ACTIVE)
                            .role(role)
                            .designation(designation)
                            .department(department)
                            .team(team)
                            .build());
                });
    }

    private Lead createTestLead(String clientName) {
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName(clientName);
        leadReq.setMobileNumber("9" + String.format("%09d", (long) (Math.random() * 1000000000L)));
        leadReq.setLocation("Delhi");
        leadReq.setLeadSource(LeadSource.MANUAL);
        LeadResponse leadResp = leadService.createLead(leadReq, adminUser.getEmployeeCode());
        return leadRepository.findById(leadResp.getLeadId()).orElseThrow();
    }

    // =========================================================================
    // 1-3. Master Data Verification: Role ID 9, Designation ID 7, Department ID 2
    // =========================================================================
    @Test
    @DisplayName("Master Data: Verify Role 9, Designation 7, Department 2 exist")
    void testMasterDataVerification() {
        Role role9 = roleRepository.findByCodeIgnoreCase(BootstrapConstants.ROLE_CRM_ONBOARDING).orElse(null);
        assertThat(role9).isNotNull();
        assertThat(role9.getCode()).isEqualTo("CRM_ONBOARDING");
        assertThat(role9.getStatus()).isEqualTo(Status.ACTIVE);

        Designation desig7 = designationRepository.findByCodeIgnoreCase(BootstrapConstants.DESIG_CRM_ONBOARDING).orElse(null);
        assertThat(desig7).isNotNull();
        assertThat(desig7.getCode()).isEqualTo("CRM_ONBOARDING");
        assertThat(desig7.getStatus()).isEqualTo(Status.ACTIVE);

        Department dept2 = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_CRM).orElse(null);
        assertThat(dept2).isNotNull();
        assertThat(dept2.getCode()).isEqualTo("CRM");
        assertThat(dept2.getStatus()).isEqualTo(Status.ACTIVE);

        // Absolute SM Check: Role ID 7 must remain SALES_MANAGER
        Role role7 = roleRepository.findByCodeIgnoreCase(BootstrapConstants.ROLE_SALES_MANAGER).orElse(null);
        assertThat(role7).isNotNull();
        assertThat(role7.getCode()).isEqualTo("SALES_MANAGER");
        assertThat(role7.getId()).isEqualTo(7L);
    }

    @Autowired
    private com.blueant_crm_erp.auth.security.CustomUserDetailsService userDetailsService;

    // =========================================================================
    // 4-5. Authentication & Authority: ROLE_CRM_ONBOARDING
    // =========================================================================
    @Test
    @DisplayName("Auth: CRM user can authenticate and receives ROLE_CRM_ONBOARDING")
    void testCrmUserCanAuthenticate() {
        LoginRequest loginRequest = LoginRequest.builder()
                .employeeCode(crmUser.getEmployeeCode())
                .password("password123")
                .build();
        LoginResponse response = authService.login(loginRequest);
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getDepartment()).isEqualTo("CRM");

        org.springframework.security.core.userdetails.UserDetails userDetails =
                userDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
        assertThat(userDetails.getAuthorities().stream().map(org.springframework.security.core.GrantedAuthority::getAuthority))
                .contains("ROLE_CRM_ONBOARDING");
    }

    // =========================================================================
    // 6. Search and Filter Leads with CRM_ONBOARDING
    // =========================================================================
    @Test
    @DisplayName("Search & Filter: CRM_ONBOARDING can search and filter Leads")
    void testCrmOnboardingCanSearchAndFilterLeads() throws Exception {
        createTestLead("Searchable Lead CRM");

        mockMvc.perform(post("/v1/leads/search")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("LEAD_READ")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LeadSearchRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/v1/leads/filter")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("LEAD_READ")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LeadFilterRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // 7. CRM_ONBOARDING -> RM (SUCCESS)
    // =========================================================================
    @Test
    @DisplayName("7. CRM_ONBOARDING assigns Lead -> RM (Allowed)")
    void test7_crmAssignsLeadToRm_allowed() throws Exception {
        Lead lead = createTestLead("Client Test 7 RM");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedUserId").value(rmUser.getId()))
                .andExpect(jsonPath("$.data.assignedByCoordinator").value(false))
                .andExpect(jsonPath("$.data.assignmentLabel").value("Assigned by CRM Onboarding"));

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(reloaded.getLeadStatus()).isEqualTo(LeadStatus.ASSIGNED);
        assertThat(reloaded.getLeadStage()).isEqualTo(LeadStage.LEAD_ASSIGNED);
        assertThat(reloaded.getAssignmentSource()).isEqualTo("CRM_ONBOARDING");
        assertThat(reloaded.getAssignedBy().getId()).isEqualTo(crmUser.getId());
        assertThat(reloaded.getAssignedAt()).isNotNull();
    }

    // =========================================================================
    // 8. CRM_ONBOARDING -> SC (SUCCESS)
    // =========================================================================
    @Test
    @DisplayName("8. CRM_ONBOARDING assigns Lead -> SC (Allowed)")
    void test8_crmAssignsLeadToSc_allowed() throws Exception {
        Lead lead = createTestLead("Client Test 8 SC");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(scUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedUserId").value(scUser.getId()))
                .andExpect(jsonPath("$.data.assignedByCoordinator").value(false))
                .andExpect(jsonPath("$.data.assignmentLabel").value("Assigned by CRM Onboarding"));

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(scUser.getId());
        assertThat(reloaded.getLeadStatus()).isEqualTo(LeadStatus.ASSIGNED);
        assertThat(reloaded.getLeadStage()).isEqualTo(LeadStage.LEAD_ASSIGNED);
        assertThat(reloaded.getAssignmentSource()).isEqualTo("CRM_ONBOARDING");
        assertThat(reloaded.getAssignedBy().getId()).isEqualTo(crmUser.getId());
    }

    // =========================================================================
    // 9. CRM_ONBOARDING -> SM (403 FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("9. CRM_ONBOARDING assigns Lead -> SM (403 Forbidden)")
    void test9_crmAssignsLeadToSm_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 9 SM");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(smUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 10. CRM_ONBOARDING -> PC (403 FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("10. CRM_ONBOARDING assigns Lead -> PC (403 Forbidden)")
    void test10_crmAssignsLeadToPc_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 10 PC");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(pcCoordinatorUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 11. CRM_ONBOARDING -> TL (403 FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("11. CRM_ONBOARDING assigns Lead -> TL (403 Forbidden)")
    void test11_crmAssignsLeadToTl_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 11 TL");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(tlUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 12. CRM_ONBOARDING -> Admin (403 FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("12. CRM_ONBOARDING assigns Lead -> Admin (403 Forbidden)")
    void test12_crmAssignsLeadToAdmin_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 12 ADMIN");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(adminUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 13. CRM_ONBOARDING -> Super Admin (403 FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("13. CRM_ONBOARDING assigns Lead -> Super Admin (403 Forbidden)")
    void test13_crmAssignsLeadToSuperAdmin_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 13 SUPER_ADMIN");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(superAdminUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 14. CRM_ONBOARDING -> CRM_ONBOARDING (403 FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("14. CRM_ONBOARDING assigns Lead -> CRM_ONBOARDING (403 Forbidden)")
    void test14_crmAssignsLeadToCrmOnboarding_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 14 CRM_TARGET");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(crmTargetUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 15. CRM Department employee WITHOUT CRM_ONBOARDING role rejected (403)
    // =========================================================================
    @Test
    @DisplayName("15. CRM Department employee WITHOUT CRM_ONBOARDING role rejected")
    void test15_crmDeptWithoutCrmOnboardingRole_rejected() throws Exception {
        Lead lead = createTestLead("Client Test 15 Unauth Dept User");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmOtherUser.getEmployeeCode()).roles("EMPLOYEE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 16. Existing RM Work Follow regression passes
    // =========================================================================
    @Test
    @DisplayName("16. Existing RM Work Follow regression passes")
    void test16_assignedRmCanEnterExistingWorkFollow() {
        Lead lead = createTestLead("Client Test 16 Work Follow RM");

        // CRM assigns lead to RM
        AssignLeadRequest assignReq = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();
        leadService.assignLead(assignReq, crmUser.getEmployeeCode());

        // RM schedules meeting via existing work follow
        CreateMeetingRequest meetingReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(11, 0))
                .meetingLocation("Connaught Place")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();

        MeetingResponse meetingResp = meetingScheduleService.scheduleMeeting(meetingReq, rmUser.getEmployeeCode());
        assertThat(meetingResp).isNotNull();
        assertThat(meetingResp.getId()).isNotNull();

        // RM completes meeting update via existing work follow
        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(11, 0))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .discussion("Detailed discussion with RM regarding portfolio")
                .remarks("Positive interaction")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .nextPlanTime(LocalTime.of(11, 0))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(15.5)
                .build();

        MeetingResponse updatedMeeting = meetingService.processMeetingUpdateWorkflow(meetingResp.getMeetingCode(), workflowReq, rmUser.getEmployeeCode());
        assertThat(updatedMeeting).isNotNull();
        assertThat(updatedMeeting.getMeetingStatus()).isNotNull();
    }

    // =========================================================================
    // 17. Existing SC Work Follow regression passes
    // =========================================================================
    @Test
    @DisplayName("17. Existing SC Work Follow regression passes")
    void test17_assignedScCanEnterExistingWorkFollow() {
        Lead lead = createTestLead("Client Test 17 Work Follow SC");

        // CRM assigns lead to SC
        AssignLeadRequest assignReq = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(scUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();
        leadService.assignLead(assignReq, crmUser.getEmployeeCode());

        // SC schedules meeting via existing work follow
        CreateMeetingRequest meetingReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(2))
                .meetingTime(LocalTime.of(14, 0))
                .meetingLocation("Nehru Place")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();

        MeetingResponse meetingResp = meetingScheduleService.scheduleMeeting(meetingReq, scUser.getEmployeeCode());
        assertThat(meetingResp).isNotNull();
        assertThat(meetingResp.getId()).isNotNull();

        // SC updates meeting
        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.now().plusDays(2))
                .meetingTime(LocalTime.of(14, 0))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .discussion("Detailed discussion with SC")
                .remarks("Coordinator meeting successful")
                .nextPlanDate(LocalDate.now().plusDays(7))
                .nextPlanTime(LocalTime.of(14, 0))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(15.5)
                .build();

        MeetingResponse updatedMeeting = meetingService.processMeetingUpdateWorkflow(meetingResp.getMeetingCode(), workflowReq, scUser.getEmployeeCode());
        assertThat(updatedMeeting).isNotNull();
        assertThat(updatedMeeting.getMeetingStatus()).isNotNull();
    }

    // =========================================================================
    // 18. Existing SM Work Follow regression passes (SM Untouched)
    // =========================================================================
    @Test
    @DisplayName("18. Existing SM Work Follow regression passes")
    void test18_existingSmWorkFollowRegression_pass() {
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName("SM Regression Lead");
        leadReq.setMobileNumber("9" + String.format("%09d", (long) (Math.random() * 1000000000L)));
        leadReq.setLocation("Gurgaon");
        leadReq.setLeadSource(LeadSource.MANUAL);
        LeadResponse smLead = leadService.createLead(leadReq, smUser.getEmployeeCode());

        CreateMeetingRequest meetingReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(smLead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(15, 0))
                .meetingLocation("Cyber City")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();

        MeetingResponse meetingResp = meetingScheduleService.scheduleMeeting(meetingReq, smUser.getEmployeeCode());
        assertThat(meetingResp).isNotNull();

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(15, 0))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .discussion("SM Work Follow discussion")
                .remarks("SM remarks unchanged")
                .nextPlanDate(LocalDate.now().plusDays(3))
                .nextPlanTime(LocalTime.of(15, 0))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(15.5)
                .build();

        MeetingResponse updatedMeeting = meetingService.processMeetingUpdateWorkflow(meetingResp.getMeetingCode(), workflowReq, smUser.getEmployeeCode());
        assertThat(updatedMeeting).isNotNull();
        assertThat(updatedMeeting.getMeetingStatus()).isNotNull();
    }

    // =========================================================================
    // 19. Existing PC Coordinator Lead Assignment regression passes
    // =========================================================================
    @Test
    @DisplayName("19. Existing PC Coordinator Lead Assignment regression passes")
    void test19_pcCoordinatorAssignsLeadToRm_success() throws Exception {
        Lead lead = createTestLead("Client Test 19 PC to RM");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(pcCoordinatorUser.getEmployeeCode()).roles("PC_COORDINATOR").authorities(
                        new SimpleGrantedAuthority("ROLE_PC_COORDINATOR"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedUserId").value(rmUser.getId()));

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(reloaded.getLeadStatus()).isEqualTo(LeadStatus.ASSIGNED);
    }

    // =========================================================================
    // 20. Assignment audit/history preserved
    // =========================================================================
    @Test
    @DisplayName("20. Assignment audit/history preserved")
    void test20_assignmentAuditPreserved() throws Exception {
        Lead lead = createTestLead("Client Test 20 Audit");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getCreatedBy()).isEqualTo(lead.getCreatedBy());
        assertThat(reloaded.getAssignedBy().getId()).isEqualTo(crmUser.getId());
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(reloaded.getAssignmentSource()).isEqualTo("CRM_ONBOARDING");
        assertThat(reloaded.getAssignedAt()).isNotNull();

        LeadDetailResponse detail = leadService.getLeadDetails(reloaded.getUniqueLeadId());
        assertThat(detail.getAssignedByCoordinator()).isFalse();
        assertThat(detail.getAssignmentLabel()).isEqualTo("Assigned by CRM Onboarding");
        assertThat(detail.getAssignedUserId()).isEqualTo(rmUser.getId());
    }

    // =========================================================================
    // 21. Duplicate assignment remains idempotent
    // =========================================================================
    @Test
    @DisplayName("21. Duplicate assignment remains idempotent")
    void test21_repeatedCrmAssignment_idempotent() throws Exception {
        Lead lead = createTestLead("Client Test 21 Idempotency");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        // First assignment
        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedUserId").value(rmUser.getId()));

        long leadCountBefore = leadRepository.count();

        // Repeated assignment to same RM
        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM_ONBOARDING").authorities(
                        new SimpleGrantedAuthority("ROLE_CRM_ONBOARDING"),
                        new SimpleGrantedAuthority("PHYSICAL_LEAD_ASSIGN")
                ))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedUserId").value(rmUser.getId()));

        long leadCountAfter = leadRepository.count();
        assertThat(leadCountAfter).isEqualTo(leadCountBefore);

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(reloaded.getAssignmentSource()).isEqualTo("CRM_ONBOARDING");
    }
}
