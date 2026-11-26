package com.blueant_crm_erp.client;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.ClientBusinessItemDto;
import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationSearchRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.enums.PcBusinessType;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.SalesRole;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientMeetingVerificationSearchIntegrationTest {

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
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientMeetingRepository clientMeetingRepository;

    @Autowired
    private ClientMeetingVerificationRepository clientMeetingVerificationRepository;

    @Autowired
    private UserDetailsService customUserDetailsService;

    private User scUser;
    private User rmUser;
    private User smUser;
    private User pcUser;
    private User adminUser;
    private Team defaultTeam;
    private Department salesDept;
    private Department crmDept;

    @BeforeEach
    void setUp() {
        salesDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("Sales").code(BootstrapConstants.DEPT_SALES).status(Status.ACTIVE).build()));

        crmDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("CRM").code(BootstrapConstants.DEPT_CRM).status(Status.ACTIVE).build()));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        scUser = createTestUser("SC_SRCH", BootstrapConstants.ROLE_SALES_COORDINATOR, BootstrapConstants.DESIG_SC, salesDept, "MEETING_UPDATE", "MEETING_READ");
        rmUser = createTestUser("RM_SRCH", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, "RM", salesDept, "MEETING_UPDATE", "MEETING_READ");
        smUser = createTestUser("SM_SRCH", BootstrapConstants.ROLE_SALES_MANAGER, BootstrapConstants.DESIG_SM, salesDept, "MEETING_READ");
        pcUser = createTestUser("PC_SRCH", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, crmDept, "MEETING_VERIFY", "MEETING_READ");
        adminUser = createTestUser("ADM_SRCH", BootstrapConstants.ROLE_ADMIN, "ADMIN", crmDept, "MEETING_VERIFY", "MEETING_READ");
    }

    private User createTestUser(String prefix, String roleCode, String desigCode, Department dept, String... permissions) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Role role = roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(roleCode).name(roleCode).status(Status.ACTIVE).systemRole(true).defaultRole(false).build()));

        for (String permCode : permissions) {
            Permission perm = permissionRepository.findByCodeIgnoreCase(permCode)
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .code(permCode).name(permCode).module("TEST").displayOrder(1).status(Status.ACTIVE).build()));
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
                .deleted(false)
                .build());
    }

    private Client createTestClient(User salesPerson, String name, String mobile) {
        String clientCode = "BA-CL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return clientRepository.save(Client.builder()
                .clientCode(clientCode)
                .clientName(name != null ? name : ("Client " + clientCode))
                .mobileNumber(mobile != null ? mobile : ("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L))))
                .location("Mumbai")
                .clinicAddress("BKC")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(salesPerson)
                .build());
    }

    private String submitMeetingUpdate(Client client, UserDetails submitterPrincipal, LocalDate meetingDate, String businessGenerated) throws Exception {
        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(meetingDate != null ? meetingDate : LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated(businessGenerated != null ? businessGenerated : "NO")
                .remarks("Conducted meeting update for test")
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .latitude(new BigDecimal("19.0760"))
                .longitude(new BigDecimal("72.8777"))
                .accuracy(15.0)
                .build();

        String responseStr = mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(submitterPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(responseStr).path("data").path("meetingCode").asText();
    }

    @Test
    @DisplayName("1. Search by exact Meeting Code returns specific meeting")
    void testSearchByMeetingCode() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());

        Client client = createTestClient(scUser, "Dr. Meeting Code Test", null);
        String meetingCode = submitMeetingUpdate(client, scPrincipal, LocalDate.now(), "NO");

        ClientMeetingVerificationSearchRequest request = ClientMeetingVerificationSearchRequest.builder()
                .meetingCode(meetingCode)
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(meetingCode))
                .andExpect(jsonPath("$.data.content[0].clientName").value("Dr. Meeting Code Test"));
    }

    @Test
    @DisplayName("2. Search by Client Name and Mobile Number")
    void testSearchByClientNameAndMobile() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());

        String uniqueMobile = "9876543210";
        Client client = createTestClient(scUser, "Unique Patient Client", uniqueMobile);
        String meetingCode = submitMeetingUpdate(client, scPrincipal, LocalDate.now(), "NO");

        // Filter by Client Name
        ClientMeetingVerificationSearchRequest reqName = ClientMeetingVerificationSearchRequest.builder()
                .clientName("Unique Patient")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(meetingCode)));

        // Filter by Mobile Number
        ClientMeetingVerificationSearchRequest reqMobile = ClientMeetingVerificationSearchRequest.builder()
                .mobileNumber(uniqueMobile)
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqMobile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(meetingCode)));
    }

    @Test
    @DisplayName("3. Search by Employee Code and Name")
    void testSearchByEmployeeFilters() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());

        Client client = createTestClient(scUser, "Dr. Employee Filter Test", null);
        String meetingCode = submitMeetingUpdate(client, scPrincipal, LocalDate.now(), "NO");

        // By Employee Code
        ClientMeetingVerificationSearchRequest reqCode = ClientMeetingVerificationSearchRequest.builder()
                .employeeCode(scUser.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(meetingCode)))
                .andExpect(jsonPath("$.data.content[0].assignedEmployeeCode").value(scUser.getEmployeeCode()));

        // By Employee Name
        ClientMeetingVerificationSearchRequest reqName = ClientMeetingVerificationSearchRequest.builder()
                .employeeName(scUser.getFirstName())
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(meetingCode)));
    }

    @Test
    @DisplayName("4. Filter by Employee Role: RM vs SC vs SM")
    void testSearchByRoleFilters() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());

        Client scClient = createTestClient(scUser, "SC Client", null);
        String scMeetingCode = submitMeetingUpdate(scClient, scPrincipal, LocalDate.now(), "NO");

        Client rmClient = createTestClient(rmUser, "RM Client", null);
        String rmMeetingCode = submitMeetingUpdate(rmClient, rmPrincipal, LocalDate.now(), "NO");

        // Filter RM
        ClientMeetingVerificationSearchRequest reqRm = ClientMeetingVerificationSearchRequest.builder()
                .employeeRole(SalesRole.RM)
                .meetingCode(rmMeetingCode)
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqRm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(rmMeetingCode))
                .andExpect(jsonPath("$.data.content[0].employeeRole").value("RM"));

        // Filter SC
        ClientMeetingVerificationSearchRequest reqSc = ClientMeetingVerificationSearchRequest.builder()
                .employeeRole(SalesRole.SC)
                .meetingCode(scMeetingCode)
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqSc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(scMeetingCode))
                .andExpect(jsonPath("$.data.content[0].employeeRole").value("SC"));

        // Filter SM -> business rule says SM cannot be assigned client meetings -> returns empty
        ClientMeetingVerificationSearchRequest reqSm = ClientMeetingVerificationSearchRequest.builder()
                .employeeRole(SalesRole.SM)
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqSm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("5. Filter by Date range inclusive and reject fromDate > toDate with 400")
    void testDateRangeFilters() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());

        LocalDate pastDate = LocalDate.now().minusDays(5);
        Client client = createTestClient(scUser, "Date Range Client", null);
        String pastMeetingCode = submitMeetingUpdate(client, scPrincipal, pastDate, "NO");

        // Valid date range including pastDate
        ClientMeetingVerificationSearchRequest validReq = ClientMeetingVerificationSearchRequest.builder()
                .meetingCode(pastMeetingCode)
                .fromDate(pastDate.minusDays(1))
                .toDate(pastDate.plusDays(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(pastMeetingCode));

        // Invalid date range (fromDate > toDate) -> 400 Bad Request
        ClientMeetingVerificationSearchRequest invalidReq = ClientMeetingVerificationSearchRequest.builder()
                .fromDate(LocalDate.now().plusDays(2))
                .toDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6. Filter by Verification Status: PENDING vs VERIFIED vs REJECTED")
    void testVerificationStatusFilters() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());

        // Create Pending Meeting
        Client client1 = createTestClient(scUser, "Pending Verif Client", null);
        String pendingCode = submitMeetingUpdate(client1, scPrincipal, LocalDate.now(), "NO");

        // Create Verified Meeting
        Client client2 = createTestClient(scUser, "Verified Verif Client", null);
        String verifiedCode = submitMeetingUpdate(client2, scPrincipal, LocalDate.now(), "NO");
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now().plusWeeks(2))
                .meetingTime(java.time.LocalTime.of(14, 30))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Verified successfully")
                .build();
        mockMvc.perform(post("/v1/client-meetings/verification/" + verifiedCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk());

        // Create Rejected Meeting
        Client client3 = createTestClient(scUser, "Rejected Verif Client", null);
        String rejectedCode = submitMeetingUpdate(client3, scPrincipal, LocalDate.now(), "NO");
        mockMvc.perform(post("/v1/client-meetings/verification/" + rejectedCode + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "Incomplete verification details"))
                .andExpect(status().isOk());

        // Filter PENDING
        ClientMeetingVerificationSearchRequest reqPending = ClientMeetingVerificationSearchRequest.builder()
                .meetingCode(pendingCode)
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqPending)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].verificationStatus").value("PENDING"));

        // Filter VERIFIED
        ClientMeetingVerificationSearchRequest reqVerified = ClientMeetingVerificationSearchRequest.builder()
                .meetingCode(verifiedCode)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVerified)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].verificationStatus").value("VERIFIED"));

        // Filter REJECTED
        ClientMeetingVerificationSearchRequest reqRejected = ClientMeetingVerificationSearchRequest.builder()
                .meetingCode(rejectedCode)
                .verificationStatus(VerificationStatus.REJECTED)
                .build();
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqRejected)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].verificationStatus").value("REJECTED"));
    }

    @Test
    @DisplayName("7. Filter by Business Generated and Business Type with multiple items without row duplication")
    void testBusinessFiltersAndNoDuplication() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());

        Client client = createTestClient(scUser, "Multi Business Client", null);
        String meetingCode = submitMeetingUpdate(client, scPrincipal, LocalDate.now(), "YES");

        // Verify with 3 business items (including Redeem and Switch)
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now().plusWeeks(2))
                .meetingTime(java.time.LocalTime.of(15, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessItems(List.of(
                        ClientBusinessItemDto.builder().businessType("REDEEM").amount(new BigDecimal("50000.00")).build(),
                        ClientBusinessItemDto.builder().businessType("SWITCH").amount(new BigDecimal("100000.00")).build(),
                        ClientBusinessItemDto.builder().businessType("CAN_CREATE").amount(null).build()
                ))
                .remarks("Multiple business lines verified")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk());

        // Search by businessType REDEEM
        ClientMeetingVerificationSearchRequest reqBizType = ClientMeetingVerificationSearchRequest.builder()
                .meetingCode(meetingCode)
                .businessType("Redeem")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBizType)))
                .andExpect(status().isOk())
                // Invariant: EXISTS subquery must prevent row duplication (content size must be exactly 1, not 3)
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(meetingCode))
                .andExpect(jsonPath("$.data.content[0].businessItems", hasSize(3)));

        // Search by businessGenerated YES
        ClientMeetingVerificationSearchRequest reqBg = ClientMeetingVerificationSearchRequest.builder()
                .meetingCode(meetingCode)
                .businessGenerated("YES")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].businessGenerated").value("YES"));
    }

    @Test
    @DisplayName("8. General Keyword Search across code, client, employee")
    void testKeywordSearch() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());

        Client client = createTestClient(scUser, "Dr. Alexander Fleming", "9123456789");
        String meetingCode = submitMeetingUpdate(client, scPrincipal, LocalDate.now(), "NO");

        // Keyword matching meeting code
        ClientMeetingVerificationSearchRequest kwMeeting = ClientMeetingVerificationSearchRequest.builder()
                .keyword(meetingCode)
                .build();
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kwMeeting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(meetingCode)));

        // Keyword matching client name
        ClientMeetingVerificationSearchRequest kwClient = ClientMeetingVerificationSearchRequest.builder()
                .keyword("Fleming")
                .build();
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kwClient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(meetingCode)));

        // Keyword matching employee code
        ClientMeetingVerificationSearchRequest kwEmp = ClientMeetingVerificationSearchRequest.builder()
                .keyword(scUser.getEmployeeCode())
                .build();
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kwEmp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(meetingCode)));
    }

    @Test
    @DisplayName("9. Invariant: isCurrent = true ensures rejected historical attempts do not duplicate")
    void testIsCurrentInvariantNoDuplicatesOnRejection() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());

        Client client = createTestClient(scUser, "Resubmit Verification Client", null);
        String meetingCode = submitMeetingUpdate(client, scPrincipal, LocalDate.now(), "NO");

        // Reject attempt #1
        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "Needs correction"))
                .andExpect(status().isOk());

        // Resubmit attempt #2
        submitMeetingUpdate(client, scPrincipal, LocalDate.now(), "NO");

        // Verify there are 2 verification rows in DB for this meeting
        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        List<ClientMeetingVerification> allVerifs = clientMeetingVerificationRepository.findByClientMeetingIdOrderByAttemptNumberAsc(meeting.getId());
        assertThat(allVerifs).hasSize(2);

        // Search must return ONLY the current attempt (exactly 1 row, isCurrent = true)
        ClientMeetingVerificationSearchRequest request = ClientMeetingVerificationSearchRequest.builder()
                .meetingCode(meetingCode)
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].attemptNumber").value(2))
                .andExpect(jsonPath("$.data.content[0].isCurrent").value(true));
    }

    @Test
    @DisplayName("10. RBAC Visibility: SM caller gets empty result, RM and SC caller see only assigned meetings")
    void testRbacCallerVisibilityScoping() throws Exception {
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmail());
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmail());
        UserDetails smPrincipal = customUserDetailsService.loadUserByUsername(smUser.getEmail());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmail());
        UserDetails adminPrincipal = customUserDetailsService.loadUserByUsername(adminUser.getEmail());

        // Create second SC user to prove SC-to-SC isolation
        User scUser2 = createTestUser("SC2_SRCH", BootstrapConstants.ROLE_SALES_COORDINATOR, BootstrapConstants.DESIG_SC, salesDept, "MEETING_UPDATE", "MEETING_READ");
        UserDetails scPrincipal2 = customUserDetailsService.loadUserByUsername(scUser2.getEmail());

        Client scClient = createTestClient(scUser, "SC Assigned Client", null);
        String scMeetingCode = submitMeetingUpdate(scClient, scPrincipal, LocalDate.now(), "NO");

        Client scClient2 = createTestClient(scUser2, "SC2 Assigned Client", null);
        String scMeetingCode2 = submitMeetingUpdate(scClient2, scPrincipal2, LocalDate.now(), "NO");

        Client rmClient = createTestClient(rmUser, "RM Assigned Client", null);
        String rmMeetingCode = submitMeetingUpdate(rmClient, rmPrincipal, LocalDate.now(), "NO");

        ClientMeetingVerificationSearchRequest emptyReq = ClientMeetingVerificationSearchRequest.builder().build();

        // 1. SM caller -> always gets empty list (SM cannot access client meetings)
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(smPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));

        // 2. RM caller -> sees ONLY rmClient's meeting, NOT scClient or scClient2 meetings
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(rmMeetingCode)))
                .andExpect(jsonPath("$.data.content[*].meetingCode", not(hasItem(scMeetingCode))))
                .andExpect(jsonPath("$.data.content[*].meetingCode", not(hasItem(scMeetingCode2))));

        // 3. SC 1 caller -> sees ONLY scClient's meeting, NOT scClient2 or rmClient meetings
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(scPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(scMeetingCode)))
                .andExpect(jsonPath("$.data.content[*].meetingCode", not(hasItem(scMeetingCode2))))
                .andExpect(jsonPath("$.data.content[*].meetingCode", not(hasItem(rmMeetingCode))));

        // 4. SC 2 caller -> sees ONLY scClient2's meeting, NOT scClient or rmClient meetings
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(scPrincipal2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(scMeetingCode2)))
                .andExpect(jsonPath("$.data.content[*].meetingCode", not(hasItem(scMeetingCode))))
                .andExpect(jsonPath("$.data.content[*].meetingCode", not(hasItem(rmMeetingCode))));

        // 5. PC Coordinator caller -> sees organization-wide records (all 3 meetings)
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(scMeetingCode)))
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(scMeetingCode2)))
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(rmMeetingCode)));

        // 6. Admin caller -> sees organization-wide records (all 3 meetings)
        mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(scMeetingCode)))
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(scMeetingCode2)))
                .andExpect(jsonPath("$.data.content[*].meetingCode", hasItem(rmMeetingCode)));
    }
}
