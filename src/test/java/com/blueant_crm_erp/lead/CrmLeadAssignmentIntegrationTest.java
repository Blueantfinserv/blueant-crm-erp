package com.blueant_crm_erp.lead;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.dto.request.AssignLeadRequest;
import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
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

@SpringBootTest(classes = com.blueant_crm_erp.BlueantCrmErpApplication.class)
@AutoConfigureMockMvc
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

    private User crmUser;
    private User pcCoordinatorUser;
    private User rmUser;
    private User scUser;
    private User smUser;
    private User adminUser;
    private User superAdminUser;

    @BeforeEach
    void setUp() {
        adminUser = getOrCreateUser("ADM_CRM_TEST", BootstrapConstants.ROLE_ADMIN, "ADMIN", "Admin");
        superAdminUser = getOrCreateUser("SA_CRM_TEST", BootstrapConstants.ROLE_SUPER_ADMIN, "SA", "SuperAdmin");
        crmUser = getOrCreateUser("CRM_TEST_USER", BootstrapConstants.ROLE_CRM, "CRM", "CrmUser");
        pcCoordinatorUser = getOrCreateUser("PC_TEST_USER", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, "PcCoord");
        rmUser = getOrCreateUser("RM_TEST_USER", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, "RmUser");
        scUser = getOrCreateUser("SC_TEST_USER", BootstrapConstants.ROLE_SALES_COORDINATOR, BootstrapConstants.DESIG_SC, "ScUser");
        smUser = getOrCreateUser("SM_TEST_USER", BootstrapConstants.ROLE_SALES_MANAGER, BootstrapConstants.DESIG_SM, "SmUser");
    }

    private User getOrCreateUser(String employeeCode, String roleCode, String desigCode, String firstName) {
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

                    Department department = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                            .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

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
                            .password("password123")
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
    // TEST 1: CRM assigns Lead -> RM (SUCCESS)
    // =========================================================================
    @Test
    @DisplayName("TEST 1: CRM assigns Lead -> RM (Expected: SUCCESS)")
    void test1_crmAssignsLeadToRm_success() throws Exception {
        Lead lead = createTestLead("Client Test 1 RM");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedUserId").value(rmUser.getId()))
                .andExpect(jsonPath("$.data.assignedByCoordinator").value(false))
                .andExpect(jsonPath("$.data.assignmentLabel").value("Assigned by CRM"));

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(reloaded.getLeadStatus()).isEqualTo(LeadStatus.ASSIGNED);
        assertThat(reloaded.getLeadStage()).isEqualTo(LeadStage.LEAD_ASSIGNED);
        assertThat(reloaded.getAssignmentSource()).isEqualTo("CRM");
        assertThat(reloaded.getAssignedBy().getId()).isEqualTo(crmUser.getId());
        assertThat(reloaded.getAssignedAt()).isNotNull();
    }

    // =========================================================================
    // TEST 2: CRM assigns Lead -> SC (SUCCESS)
    // =========================================================================
    @Test
    @DisplayName("TEST 2: CRM assigns Lead -> SC (Expected: SUCCESS)")
    void test2_crmAssignsLeadToSc_success() throws Exception {
        Lead lead = createTestLead("Client Test 2 SC");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(scUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedUserId").value(scUser.getId()))
                .andExpect(jsonPath("$.data.assignedByCoordinator").value(false))
                .andExpect(jsonPath("$.data.assignmentLabel").value("Assigned by CRM"));

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(scUser.getId());
        assertThat(reloaded.getLeadStatus()).isEqualTo(LeadStatus.ASSIGNED);
        assertThat(reloaded.getLeadStage()).isEqualTo(LeadStage.LEAD_ASSIGNED);
        assertThat(reloaded.getAssignmentSource()).isEqualTo("CRM");
        assertThat(reloaded.getAssignedBy().getId()).isEqualTo(crmUser.getId());
    }

    // =========================================================================
    // TEST 3: CRM assigns Lead -> SM (FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("TEST 3: CRM assigns Lead -> SM (Expected: FORBIDDEN)")
    void test3_crmAssignsLeadToSm_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 3 SM");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(smUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 4: CRM assigns Lead -> PC_COORDINATOR (FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("TEST 4: CRM assigns Lead -> PC_COORDINATOR (Expected: FORBIDDEN)")
    void test4_crmAssignsLeadToPcCoordinator_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 4 PC");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(pcCoordinatorUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 5: CRM assigns Lead -> ADMIN (FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("TEST 5: CRM assigns Lead -> ADMIN (Expected: FORBIDDEN)")
    void test5_crmAssignsLeadToAdmin_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 5 ADMIN");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(adminUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 6: CRM assigns Lead -> SUPER_ADMIN (FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("TEST 6: CRM assigns Lead -> SUPER_ADMIN (Expected: FORBIDDEN)")
    void test6_crmAssignsLeadToSuperAdmin_forbidden() throws Exception {
        Lead lead = createTestLead("Client Test 6 SUPER_ADMIN");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(superAdminUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST 7: PC Coordinator assigns Lead -> RM (existing behavior unchanged)
    // =========================================================================
    @Test
    @DisplayName("TEST 7: PC Coordinator assigns Lead -> RM (Expected: existing behavior unchanged)")
    void test7_pcCoordinatorAssignsLeadToRm_success() throws Exception {
        Lead lead = createTestLead("Client Test 7 PC to RM");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(pcCoordinatorUser.getEmployeeCode()).roles("PC_COORDINATOR"))
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
    // TEST 8: PC Coordinator assigns Lead -> SC (existing behavior unchanged)
    // =========================================================================
    @Test
    @DisplayName("TEST 8: PC Coordinator assigns Lead -> SC (Expected: existing behavior unchanged)")
    void test8_pcCoordinatorAssignsLeadToSc_success() throws Exception {
        Lead lead = createTestLead("Client Test 8 PC to SC");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(scUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(pcCoordinatorUser.getEmployeeCode()).roles("PC_COORDINATOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedUserId").value(scUser.getId()));

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(scUser.getId());
        assertThat(reloaded.getLeadStatus()).isEqualTo(LeadStatus.ASSIGNED);
    }

    // =========================================================================
    // TEST 9: Assigned RM can enter existing RM Work Follow
    // =========================================================================
    @Test
    @DisplayName("TEST 9: Assigned RM can enter existing RM Work Follow")
    void test9_assignedRmCanEnterExistingWorkFollow() {
        Lead lead = createTestLead("Client Test 9 Work Follow RM");

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
    // TEST 10: Assigned SC can enter existing SC Work Follow
    // =========================================================================
    @Test
    @DisplayName("TEST 10: Assigned SC can enter existing SC Work Follow")
    void test10_assignedScCanEnterExistingWorkFollow() {
        Lead lead = createTestLead("Client Test 10 Work Follow SC");

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
    // TEST 11: Existing SM Work Follow regression (PASS)
    // =========================================================================
    @Test
    @DisplayName("TEST 11: Existing SM Work Follow regression (PASS)")
    void test11_existingSmWorkFollowRegression_pass() {
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
    // TEST 12: Repeated CRM assignment of same Lead (idempotent, no duplicate state)
    // =========================================================================
    @Test
    @DisplayName("TEST 12: Repeated CRM assignment of same Lead (Expected: no unintended duplicate state)")
    void test12_repeatedCrmAssignment_idempotent() throws Exception {
        Lead lead = createTestLead("Client Test 12 Idempotency");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        // First assignment
        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedUserId").value(rmUser.getId()));

        long leadCountBefore = leadRepository.count();

        // Repeated assignment to same RM
        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedUserId").value(rmUser.getId()));

        long leadCountAfter = leadRepository.count();
        assertThat(leadCountAfter).isEqualTo(leadCountBefore);

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(reloaded.getAssignmentSource()).isEqualTo("CRM");
    }

    // =========================================================================
    // TEST 13: Assignment history/audit preserved
    // =========================================================================
    @Test
    @DisplayName("TEST 13: Assignment history/audit preserved")
    void test13_assignmentAuditPreserved() throws Exception {
        Lead lead = createTestLead("Client Test 13 Audit");

        AssignLeadRequest request = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(rmUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Lead reloaded = leadRepository.findById(lead.getId()).orElseThrow();
        // Preserves original Lead creator
        assertThat(reloaded.getCreatedBy()).isEqualTo(lead.getCreatedBy());
        // Tracks CRM assignee and assigner
        assertThat(reloaded.getAssignedBy().getId()).isEqualTo(crmUser.getId());
        assertThat(reloaded.getAssignedSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(reloaded.getAssignmentSource()).isEqualTo("CRM");
        assertThat(reloaded.getAssignedAt()).isNotNull();

        LeadDetailResponse detail = leadService.getLeadDetails(reloaded.getUniqueLeadId());
        assertThat(detail.getAssignedByCoordinator()).isFalse();
        assertThat(detail.getAssignmentLabel()).isEqualTo("Assigned by CRM");
        assertThat(detail.getAssignedUserId()).isEqualTo(rmUser.getId());
    }

    // =========================================================================
    // TEST 14: Unauthorized CRM target-role assignment is rejected before mutation
    // =========================================================================
    @Test
    @DisplayName("TEST 14: Unauthorized CRM target-role assignment is rejected before mutation")
    void test14_unauthorizedTargetRoleRejectedBeforeMutation() throws Exception {
        Lead lead = createTestLead("Client Test 14 Unmutated");
        Lead baseline = leadRepository.findById(lead.getId()).orElseThrow();
        Long baselineAssignedPersonId = baseline.getAssignedSalesPerson() != null ? baseline.getAssignedSalesPerson().getId() : null;
        User baselineAssignedBy = baseline.getAssignedBy();
        String baselineSource = baseline.getAssignmentSource();
        LeadStatus baselineStatus = baseline.getLeadStatus();
        LeadStage baselineStage = baseline.getLeadStage();

        AssignLeadRequest forbiddenReq = AssignLeadRequest.builder()
                .leadId(lead.getId())
                .assignedUserId(smUser.getId())
                .assignmentReason("Assigned for follow-up")
                .build();

        mockMvc.perform(post("/v1/leads/assign")
                .with(user(crmUser.getEmployeeCode()).roles("CRM"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forbiddenReq)))
                .andExpect(status().isForbidden());

        // Verify that the lead entity in the database remains completely unchanged and unmutated
        Lead untouched = leadRepository.findById(lead.getId()).orElseThrow();
        assertThat(untouched.getAssignedSalesPerson() != null ? untouched.getAssignedSalesPerson().getId() : null)
                .isEqualTo(baselineAssignedPersonId);
        assertThat(untouched.getAssignedBy()).isEqualTo(baselineAssignedBy);
        assertThat(untouched.getAssignedAt()).isNull();
        assertThat(untouched.getAssignmentSource()).isEqualTo(baselineSource);
        assertThat(untouched.getLeadStatus()).isEqualTo(baselineStatus);
        assertThat(untouched.getLeadStage()).isEqualTo(baselineStage);
    }
}
