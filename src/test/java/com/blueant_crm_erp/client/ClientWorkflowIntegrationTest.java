package com.blueant_crm_erp.client;

import com.blueant_crm_erp.auth.security.CustomUserDetailsService;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientMeetingRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientMeetingService;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.permission.entity.Permission;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class ClientWorkflowIntegrationTest {

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
    private ClientRepository clientRepository;

    @Autowired
    private ClientMeetingRepository clientMeetingRepository;

    @Autowired
    private ClientMeetingVerificationRepository clientMeetingVerificationRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ClientMeetingService clientMeetingService;

    private User crmUser;
    private User rmUser;
    private User scUser;
    private User smUser;
    private User pcUser;
    private User otherRmUser;

    private Department salesDept;
    private Department crmDept;
    private Team defaultTeam;

    @BeforeEach
    void setUp() {
        salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        crmDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(salesDept));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        crmUser = createTestUser("CRM_USER", BootstrapConstants.ROLE_CRM_ONBOARDING, BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, "PHYSICAL_LEAD_ASSIGN");
        rmUser = createTestUser("RM_USER", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        otherRmUser = createTestUser("RM_OTHER", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        scUser = createTestUser("SC_USER", BootstrapConstants.ROLE_SALES_COORDINATOR, BootstrapConstants.DESIG_SC, salesDept, "MEETING_UPDATE", "MEETING_READ");
        smUser = createTestUser("SM_USER", BootstrapConstants.ROLE_SALES_MANAGER, BootstrapConstants.DESIG_SM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        pcUser = createTestUser("PC_USER", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, crmDept, "MEETING_VERIFY", "MEETING_READ");
    }

    private User createTestUser(String prefix, String roleCode, String desigCode, Department dept, String... permissions) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Role role = roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(roleCode).name(roleCode).status(Status.ACTIVE).systemRole(true).defaultRole(false).build()));

        for (String permCode : permissions) {
            Permission perm = permissionRepository.findByCodeIgnoreCase(permCode)
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .code(permCode).name(permCode).module("TEST").status(Status.ACTIVE).build()));
            if (!rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), perm.getId())) {
                rolePermissionRepository.save(RolePermission.builder().role(role).permission(perm).build());
            }
        }

        Designation desig = designationRepository.findByCodeIgnoreCase(desigCode)
                .orElseGet(() -> designationRepository.save(Designation.builder()
                        .code(desigCode).name(desigCode).department(dept).hierarchyLevel(4).status(Status.ACTIVE).build()));

        return userRepository.save(User.builder()
                .employeeCode(prefix + "_" + suffix)
                .email(prefix.toLowerCase() + "_" + suffix + "@blueant.com")
                .password("$2a$10$dummyHashedPasswordForTestEnvironment")
                .firstName(prefix)
                .lastName("Test")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .status(Status.ACTIVE)
                .accountEnabled(true)
                .role(role)
                .designation(desig)
                .department(dept)
                .team(defaultTeam)
                .gender(Gender.MALE)
                .build());
    }

    // =========================================================================
    // 1. Client Registration & Assignment Tests
    // =========================================================================

    @Test
    @DisplayName("1.1 CRM Onboarding successfully registers client and assigns to RM; Lead table remains untouched")
    void test1_1_successfulClientRegistrationToRm() throws Exception {
        long leadCountBefore = leadRepository.count();
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Vikram Rathore")
                .mobileNumber(mobile)
                .alternateMobileNumber("9876543211")
                .email("dr.vikram" + UUID.randomUUID().toString().substring(0, 5) + "@example.com")
                .speciality("Neurology")
                .location("Connaught Place, New Delhi")
                .clinicAddress("Clinic 12, Medical Plaza")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .assignmentDate(LocalDate.now())
                .bestTimeToMeet("12:00 PM - 3:00 PM")
                .remarks("Referred from Apollo Hospital Excel sheet")
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientName").value("Dr. Vikram Rathore"))
                .andExpect(jsonPath("$.data.clientCode").isNotEmpty())
                .andExpect(jsonPath("$.data.assignedSalesPersonCode").value(rmUser.getEmployeeCode()));

        // CRITICAL INVARIANT: Zero lead rows created
        assertThat(leadRepository.count()).isEqualTo(leadCountBefore);

        // Verify database state
        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getLead()).isNull(); // Strictly null lead relationship
        assertThat(client.getClientStatus()).isEqualTo(ClientStatus.ACTIVE);
        assertThat(client.getSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(client.getSpeciality()).isEqualTo("Neurology");
        assertThat(client.getBestTimeToMeet()).isEqualTo("12:00 PM - 3:00 PM");
    }

    @Test
    @DisplayName("1.2 CRM Onboarding successfully registers client and assigns to SC")
    void test1_2_successfulClientRegistrationToSc() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Ananya Roy")
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Noida Sector 62")
                .clinicAddress("Fortis Clinic")
                .salesPersonEmployeeCode(scUser.getEmployeeCode())
                .bestTimeToMeet("3:00 PM - 6:00 PM")
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.assignedSalesPersonCode").value(scUser.getEmployeeCode()));

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getSalesPerson().getId()).isEqualTo(scUser.getId());
    }

    @Test
    @DisplayName("1.3 Ineligible assignee (SM, PC, Admin) is rejected on backend")
    void test1_3_ineligibleAssigneeRejected() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Ineligible Test")
                .mobileNumber(mobile)
                .speciality("General Medicine")
                .location("Delhi")
                .clinicAddress("Address")
                .salesPersonEmployeeCode(smUser.getEmployeeCode()) // SM is ineligible!
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("1.4 Duplicate mobile number registration is rejected with HTTP 409 Conflict")
    void test1_4_duplicateMobileRejected() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req1 = CreateClientRegistrationRequest.builder()
                .clientName("Dr. First Client")
                .mobileNumber(mobile)
                .speciality("ENT")
                .location("Delhi")
                .clinicAddress("Clinic 1")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Attempt duplicate with same mobile
        CreateClientRegistrationRequest req2 = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Second Client")
                .mobileNumber(mobile) // Duplicate!
                .speciality("ENT")
                .location("Delhi")
                .clinicAddress("Clinic 2")
                .salesPersonEmployeeCode(scUser.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("1.5 Invalid bestTimeToMeet slot is rejected")
    void test1_5_invalidBestTimeToMeetRejected() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Slot Test")
                .mobileNumber(mobile)
                .speciality("Pediatrics")
                .location("Delhi")
                .clinicAddress("Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .bestTimeToMeet("10:00 AM - 11:00 AM") // Invalid slot!
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // =========================================================================
    // 2. Client Work Follow Queue Tests
    // =========================================================================

    @Test
    @DisplayName("2.1 RM sees only their own assigned clients in Client Work Follow; other RM cannot see them")
    void test2_1_rmWorkFollowIsolation() throws Exception {
        // Register client assigned to rmUser
        Client clientRm1 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900001")
                .clientName("Client For RM 1")
                .mobileNumber("9111111111")
                .speciality("Dental")
                .location("Delhi")
                .clinicAddress("Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        // Register client assigned to otherRmUser
        Client clientRm2 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900002")
                .clientName("Client For RM 2")
                .mobileNumber("9222222222")
                .speciality("Dental")
                .location("Delhi")
                .clinicAddress("Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(otherRmUser)
                .build());

        // RM 1 queries work follow
        UserDetails principalRm1 = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        mockMvc.perform(get("/v1/clients/work-follow")
                        .with(user(principalRm1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == 'BA-CL-2026-900001')]").exists())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == 'BA-CL-2026-900002')]").doesNotExist());

        // RM 2 queries work follow
        UserDetails principalRm2 = customUserDetailsService.loadUserByUsername(otherRmUser.getEmployeeCode());
        mockMvc.perform(get("/v1/clients/work-follow")
                        .with(user(principalRm2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == 'BA-CL-2026-900002')]").exists())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == 'BA-CL-2026-900001')]").doesNotExist());
    }

    // =========================================================================
    // 3. Client Meeting Scheduling & Update Tests
    // =========================================================================

    @Test
    @DisplayName("3.1 RM can schedule meeting, and unauthorized RM is denied")
    void test3_1_scheduleMeetingOwnership() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900003")
                .clientName("Meeting Test Client")
                .mobileNumber("9333333333")
                .speciality("Oncology")
                .location("Delhi")
                .clinicAddress("Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        CreateClientMeetingRequest req = CreateClientMeetingRequest.builder()
                .clientCode(client.getClientCode())
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(2))
                .meetingTime(LocalTime.of(14, 0))
                .meetingLocation("Hospital OPD")
                .build();

        // otherRmUser attempts to schedule meeting for rmUser's client -> 403 Forbidden
        UserDetails otherPrincipal = customUserDetailsService.loadUserByUsername(otherRmUser.getEmployeeCode());
        mockMvc.perform(post("/v1/client-meetings")
                        .with(user(otherPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        // rmUser schedules successfully -> 201 Created
        UserDetails principal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        mockMvc.perform(post("/v1/client-meetings")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.meetingCode").isNotEmpty())
                .andExpect(jsonPath("$.data.meetingStatus").value("SCHEDULED"));

        // Duplicate active scheduled meeting rejected
        mockMvc.perform(post("/v1/client-meetings")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("3.2 RM Conducted meeting update enforces mandatory geo-location and max 1 month follow-up")
    void test3_2_rmConductedMeetingUpdateValidation() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900004")
                .clientName("Geo Test Client")
                .mobileNumber("9444444444")
                .speciality("Orthopedics")
                .location("Delhi")
                .clinicAddress("Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-2026-900001")
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(10, 0))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .build());

        UserDetails principal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        // Conducted WITHOUT geo coordinates -> Rejected for RM
        ClientMeetingUpdateRequest noGeoReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("YES")
                .remarks("Positive discussion")
                .nextPlanDate(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noGeoReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Location coordinates")));

        // Conducted with follow-up exceeding 1 calendar month -> Rejected
        ClientMeetingUpdateRequest exceedMonthReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("YES")
                .remarks("Positive discussion")
                .nextPlanDate(LocalDate.now().plusMonths(2)) // Exceeds 1 month!
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exceedMonthReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cannot exceed one calendar month")));

        // Valid Conducted update with geo -> 200 OK
        ClientMeetingUpdateRequest validReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SOMEONE")
                .personName("Dr. Colleague")
                .position("RM")
                .businessGenerated("YES")
                .remarks("Meeting conducted successfully with co-worker")
                .nextPlanDate(LocalDate.now().plusDays(15))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(12.5)
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));

        // Verify verification record created in PENDING state
        ClientMeetingVerification verif = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId()).orElseThrow();
        assertThat(verif.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(verif.getAttemptNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("3.3 SC Conducted meeting update allows optional geo-location")
    void test3_3_scConductedMeetingOptionalGeo() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900005")
                .clientName("SC Client")
                .mobileNumber("9555555555")
                .speciality("General")
                .location("Noida")
                .clinicAddress("Sector 18")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(scUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-2026-900002")
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(scUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(11, 0))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .build());

        UserDetails principal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());

        // SC update WITHOUT geo -> Success (Geo is optional for SC)
        ClientMeetingUpdateRequest req = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("SC conducted meeting without geo")
                .nextPlanDate(LocalDate.now().plusDays(20))
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"));
    }

    // =========================================================================
    // 4. PC Client Meeting Verification, Rejection & Resubmission Tests
    // =========================================================================

    @Test
    @DisplayName("4.1 PC verifies conducted meeting with conditional investment questions when Business Generated = YES")
    void test4_1_pcVerificationConductedBusinessGeneratedYes() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900006")
                .clientName("PC Test Client")
                .mobileNumber("9666666666")
                .speciality("Dermatology")
                .location("Delhi")
                .clinicAddress("Address")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-2026-900003")
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("YES")
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(meeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // Missing investment questions when Business Generated = YES -> Rejected
        ClientMeetingVerificationRequest missingInvReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(15, 30))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Verified")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingInvReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Investment Type is required")));

        // Valid verification with investment details
        ClientMeetingVerificationRequest validReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(15, 30))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName("Rahul Sharma")
                .position("RM") // Valid position: SM, RM, TL, ADMIN, SUPER ADMIN
                .investmentType("New SIP")
                .investmentAmount(new BigDecimal("25000.00"))
                .remarks("Doctor verified, 25k SIP booked")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.investmentAmount").value(25000.00));

        // Verify meeting verified_by_pc is true
        ClientMeeting updatedMeeting = clientMeetingRepository.findById(meeting.getId()).orElseThrow();
        assertThat(updatedMeeting.getVerifiedByPc()).isTrue();

        // Verify client nextFollowupDate advanced and investmentType saved
        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(updatedClient.getInvestmentType()).isEqualTo("New SIP");
        assertThat(updatedClient.getNextFollowupDate()).isEqualTo(LocalDate.now().plusMonths(1));
    }

    @Test
    @DisplayName("4.2 PC rejects meeting with mandatory reason, and RM resubmits with attempt history preserved")
    void test4_2_pcRejectionAndResubmissionLifecycle() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900007")
                .clientName("Rejection Lifecycle Client")
                .mobileNumber("9777777777")
                .speciality("Psychiatry")
                .location("Delhi")
                .clinicAddress("Address")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-2026-900004")
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("NO")
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(meeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // Step 1: PC Rejects meeting with reason
        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "Doctor stated meeting was not conducted in clinic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Doctor stated meeting was not conducted in clinic"));

        // Verify attempt 1 is retained as REJECTED in database
        assertThat(clientMeetingVerificationRepository.countByClientMeetingId(meeting.getId())).isEqualTo(1);

        // Step 2: RM resubmits meeting update with corrections
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        ClientMeetingUpdateRequest correctedReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Re-visited doctor and verified discussion")
                .nextPlanDate(LocalDate.now().plusDays(10))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(15.0)
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctedReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));

        // Step 3: Verify attempt 2 was created and attempt 1 is preserved
        assertThat(clientMeetingVerificationRepository.countByClientMeetingId(meeting.getId())).isEqualTo(2);

        ClientMeetingVerification attempt1 = clientMeetingVerificationRepository
                .findByClientMeetingIdOrderByAttemptNumberAsc(meeting.getId()).get(0);
        assertThat(attempt1.getAttemptNumber()).isEqualTo(1);
        assertThat(attempt1.getVerificationStatus()).isEqualTo(VerificationStatus.REJECTED);
        assertThat(attempt1.getIsCurrent()).isFalse();

        ClientMeetingVerification attempt2 = clientMeetingVerificationRepository
                .findByClientMeetingIdOrderByAttemptNumberAsc(meeting.getId()).get(1);
        assertThat(attempt2.getAttemptNumber()).isEqualTo(2);
        assertThat(attempt2.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(attempt2.getIsCurrent()).isTrue();

        // Step 4: PC queries history endpoint
        mockMvc.perform(get("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/history")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data[1].verificationStatus").value("PENDING"));
    }

    // =========================================================================
    // 5. Concurrency Protection & Batch Performance Tests
    // =========================================================================

    @Test
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    @DisplayName("5.1 Concurrent meeting scheduling requests for the same client are serialized; exactly 1 active meeting is created")
    void test5_1_concurrentSchedulingProtection() throws Exception {
        String clientCode = "BA-CL-CONC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Client client = clientRepository.save(Client.builder()
                .clientCode(clientCode)
                .clientName("Concurrent Test Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .speciality("Oncology")
                .location("Delhi")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        int threadCount = 4;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch readyLatch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger conflictCount = new java.util.concurrent.atomic.AtomicInteger(0);

        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        CreateClientMeetingRequest req = CreateClientMeetingRequest.builder()
                                .clientCode(clientCode)
                                .meetingMode(MeetingMode.PHYSICAL)
                                .meetingDate(LocalDate.now().plusDays(2))
                                .meetingTime(LocalTime.of(10, 30))
                                .meetingLocation("Clinic")
                                .remarks("Concurrent scheduling attempt")
                                .build();

                        clientMeetingService.scheduleMeeting(req, rmUser.getEmployeeCode());
                        successCount.incrementAndGet();
                    } catch (BadRequestException ex) {
                        if (ex.getMessage().contains("already exists")) {
                            conflictCount.incrementAndGet();
                        }
                    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                        conflictCount.incrementAndGet();
                    } catch (Exception ex) {
                        System.err.println("Unexpected concurrency error: " + ex.getClass().getName() + " - " + ex.getMessage());
                    }
                });
            }

            readyLatch.await();
            startLatch.countDown();
            executor.shutdown();
            executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

            // Strict assertion: exactly 1 succeeds, all other 3 competing requests are blocked and rejected
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(conflictCount.get()).isEqualTo(threadCount - 1);

            // Database state verification: client has exactly 1 scheduled meeting
            long scheduledCount = clientMeetingRepository.countByClientIdAndMeetingStatus(client.getId(), MeetingStatus.SCHEDULED);
            assertThat(scheduledCount).isEqualTo(1);
        } finally {
            clientMeetingRepository.findByClientId(client.getId(), org.springframework.data.domain.Pageable.unpaged())
                    .forEach(m -> clientMeetingRepository.deleteById(m.getId()));
            clientRepository.deleteById(client.getId());
        }
    }

    @Test
    @DisplayName("5.2 Work Follow batch lookup correctly associates active scheduled meetings across multiple page sizes")
    void test5_2_workFollowBatchLookupPerformance() throws Exception {
        // Create 3 clients assigned to scUser, 2 with active meetings, 1 without
        Client c1 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-BATCH-1-" + UUID.randomUUID().toString().substring(0, 4))
                .clientName("Batch Client 1")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(scUser)
                .build());

        Client c2 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-BATCH-2-" + UUID.randomUUID().toString().substring(0, 4))
                .clientName("Batch Client 2")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(scUser)
                .build());

        Client c3 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-BATCH-3-" + UUID.randomUUID().toString().substring(0, 4))
                .clientName("Batch Client 3")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(scUser)
                .build());

        clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-B1-" + UUID.randomUUID().toString().substring(0, 4))
                .meetingNumber(1)
                .client(c1)
                .assignedEmployee(scUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .verifiedByPc(false)
                .build());

        clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-B2-" + UUID.randomUUID().toString().substring(0, 4))
                .meetingNumber(1)
                .client(c2)
                .assignedEmployee(scUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(3))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .verifiedByPc(false)
                .build());

        // Call getClientWorkFollow with page size 10
        org.springframework.data.domain.PageRequest pageReq = org.springframework.data.domain.PageRequest.of(0, 10);
        var response = clientMeetingService.getClientWorkFollow(scUser.getEmployeeCode(), pageReq, null, "ACTIVE", null);

        assertThat(response.getContent()).isNotEmpty();
        var client1Resp = response.getContent().stream().filter(r -> r.getClientId().equals(c1.getId())).findFirst().orElseThrow();
        assertThat(client1Resp.getActiveMeetingCode()).isNotNull();
        assertThat(client1Resp.getActiveMeetingStatus()).isEqualTo("SCHEDULED");

        var client3Resp = response.getContent().stream().filter(r -> r.getClientId().equals(c3.getId())).findFirst().orElseThrow();
        assertThat(client3Resp.getActiveMeetingCode()).isNull();
        assertThat(client3Resp.getActiveMeetingStatus()).isNull();
    }
}
