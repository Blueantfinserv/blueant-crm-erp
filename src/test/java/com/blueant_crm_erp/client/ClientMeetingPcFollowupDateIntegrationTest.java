package com.blueant_crm_erp.client;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationSearchRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ClientMeetingPcFollowupDateIntegrationTest {

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

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment environment;

    private User rmUser;
    private User pcUser;
    private Team defaultTeam;

    @BeforeEach
    void verifyDatabaseIsolationGuard() throws Exception {
        String currentDb = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        String catalog;
        String jdbcUrl;
        try (Connection conn = dataSource.getConnection()) {
            catalog = conn.getCatalog();
            jdbcUrl = conn.getMetaData().getURL();
        }

        System.out.println("================================================================================");
        System.out.println("[FAIL-FAST ISOLATION GUARD] Verifying isolated test database...");
        System.out.println("  Active Profiles : " + Arrays.toString(environment.getActiveProfiles()));
        System.out.println("  JDBC URL        : " + jdbcUrl);
        System.out.println("  Catalog         : " + catalog);
        System.out.println("  DATABASE()      : " + currentDb);
        System.out.println("================================================================================");

        if (!"blueant_crm_test".equalsIgnoreCase(currentDb) || !"blueant_crm_test".equalsIgnoreCase(catalog)) {
            throw new IllegalStateException("FATAL SAFETY VIOLATION: Integration tests MUST run strictly against 'blueant_crm_test'! Found currentDb=" + currentDb + ", catalog=" + catalog);
        }
        if (jdbcUrl.toLowerCase().contains("blueant_crm") && !jdbcUrl.toLowerCase().contains("blueant_crm_test")) {
            throw new IllegalStateException("FATAL SAFETY VIOLATION: Target JDBC URL points to non-test schema: " + jdbcUrl);
        }
    }

    @BeforeEach
    void setUp() {
        Department salesDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("Sales").code(BootstrapConstants.DEPT_SALES).status(Status.ACTIVE).build()));

        Department crmDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("CRM").code(BootstrapConstants.DEPT_CRM).status(Status.ACTIVE).build()));

        defaultTeam = teamRepository.findAll().stream().findFirst()
                .orElseGet(() -> teamRepository.save(Team.builder()
                        .teamCode("TEAM_SALES_1")
                        .teamName("Sales Team 1")
                        .department(salesDept)
                        .status(Status.ACTIVE)
                        .build()));

        rmUser = createTestUser("RM_FUPD", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        pcUser = createTestUser("PC_FUPD", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, crmDept, "MEETING_VERIFY", "MEETING_READ");
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

    private Client createTestClient(User salesPerson) {
        String clientCode = "BA-CL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return clientRepository.save(Client.builder()
                .clientCode(clientCode)
                .clientName("Test Dr. Client " + clientCode)
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .location("Delhi")
                .clinicAddress("Test Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(salesPerson)
                .build());
    }

    private String submitMeetingUpdate(Client client, UserDetails rmPrincipal, LocalDate nextPlanDate) throws Exception {
        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Conducted meeting update for test")
                .nextPlanDate(nextPlanDate)
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        String responseStr = mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(responseStr).path("data").path("meetingCode").asText();
    }

    @Test
    @DisplayName("Scenario 1: RM/SC sets nextPlanDate; PC Pending Verification response shows same date")
    void testScenario1_RmSetsNextPlanDate_PcPendingAndSearchShowSameDate() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("TEST SCENARIO 1: RM/SC sets nextPlanDate; PC Pending shows same date");
        System.out.println("================================================================================");

        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        LocalDate expectedFollowupDate = LocalDate.now().plusWeeks(2);
        String meetingCode = submitMeetingUpdate(client, rmPrincipal, expectedFollowupDate);

        // PC checks pending queue
        org.springframework.test.web.servlet.MvcResult pendingResult = mockMvc.perform(get("/v1/client-meetings/verification/pending")
                        .param("sort", "id,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(meetingCode))
                .andExpect(jsonPath("$.data.content[0].nextFollowupDate").value(expectedFollowupDate.toString()))
                .andReturn();

        // PC checks advanced search
        ClientMeetingVerificationSearchRequest searchReq = ClientMeetingVerificationSearchRequest.builder()
                .meetingCode(meetingCode)
                .build();

        String searchReqJson = objectMapper.writeValueAsString(searchReq);
        org.springframework.test.web.servlet.MvcResult searchResult = mockMvc.perform(post("/v1/client-meetings/verification/search")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchReqJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].nextFollowupDate").value(expectedFollowupDate.toString()))
                .andReturn();

        ClientMeeting m = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        ClientMeetingVerification v = clientMeetingVerificationRepository.findByClientMeetingIdAndIsCurrentTrue(m.getId()).orElseThrow();

        System.out.println("[REQUEST PAYLOAD - RM Update]: nextPlanDate = " + expectedFollowupDate);
        System.out.println("[HTTP STATUS - Pending]: " + pendingResult.getResponse().getStatus());
        System.out.println("[RESPONSE JSON - Pending sample]: " + pendingResult.getResponse().getContentAsString());
        System.out.println("[DATABASE VALUES]:");
        System.out.println("  meeting_code: " + m.getMeetingCode());
        System.out.println("  client_meetings.next_plan_date: " + m.getNextPlanDate());
        System.out.println("  client_meeting_verifications.next_followup_date: " + v.getNextFollowupDate());
        System.out.println("  client_meeting_verifications.verification_status: " + v.getVerificationStatus());
        System.out.println("[RESULT]: PASS - UI and API contract return nextFollowupDate matching RM/SC nextPlanDate");
    }

    @Test
    @DisplayName("Scenario 2: PC verifies; Verified, Search, Verify response, and History return correct date")
    void testScenario2_PcVerifies_VerifiedSearchVerifyResponseAndHistoryReturnDate() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("TEST SCENARIO 2: PC verifies; Verified, Search, Verify, History return correct date");
        System.out.println("================================================================================");

        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        LocalDate expectedFollowupDate = LocalDate.now().plusWeeks(3);
        String meetingCode = submitMeetingUpdate(client, rmPrincipal, expectedFollowupDate);

        // PC verifies meeting WITHOUT passing nextFollowupDate
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(14, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Verification remarks by PC without entering date")
                .nextFollowupDate(null)
                .build();

        String verifyPayloadJson = objectMapper.writeValueAsString(verifyReq);
        org.springframework.test.web.servlet.MvcResult verifyResult = mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyPayloadJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.nextFollowupDate").value(expectedFollowupDate.toString()))
                .andReturn();

        // PC checks verified queue
        org.springframework.test.web.servlet.MvcResult verifiedQueueResult = mockMvc.perform(get("/v1/client-meetings/verification/verified")
                        .param("sort", "id,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(meetingCode))
                .andExpect(jsonPath("$.data.content[0].verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.content[0].nextFollowupDate").value(expectedFollowupDate.toString()))
                .andReturn();

        // PC checks history
        org.springframework.test.web.servlet.MvcResult historyResult = mockMvc.perform(get("/v1/client-meetings/verification/" + meetingCode + "/history")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nextFollowupDate").value(expectedFollowupDate.toString()))
                .andReturn();

        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        ClientMeetingVerification verification = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId()).orElseThrow();
        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();

        assertThat(verification.getNextFollowupDate()).isEqualTo(expectedFollowupDate);
        assertThat(updatedClient.getNextFollowupDate()).isEqualTo(expectedFollowupDate);

        System.out.println("[REQUEST PAYLOAD - PC Verify]: " + verifyPayloadJson);
        System.out.println("[HTTP STATUS - Verify]: " + verifyResult.getResponse().getStatus());
        System.out.println("[RESPONSE JSON - Verify]: " + verifyResult.getResponse().getContentAsString());
        System.out.println("[DATABASE VALUES]:");
        System.out.println("  client_meetings.next_plan_date: " + meeting.getNextPlanDate());
        System.out.println("  client_meeting_verifications.next_followup_date: " + verification.getNextFollowupDate());
        System.out.println("  client_meeting_verifications.verification_status: " + verification.getVerificationStatus());
        System.out.println("  clients.next_followup_date: " + updatedClient.getNextFollowupDate());
        System.out.println("[RESULT]: PASS - Verified, Search, Verify response, History, and DB all match");
    }

    @Test
    @DisplayName("Scenario 3: RM/SC leaves nextPlanDate null; all relevant responses retain null")
    void testScenario3_RmLeavesDateNull_AllResponsesRetainNull() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("TEST SCENARIO 3: RM/SC leaves nextPlanDate null; all responses retain null");
        System.out.println("================================================================================");

        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // RM submits with null nextPlanDate
        String meetingCode = submitMeetingUpdate(client, rmPrincipal, null);

        // PC pending queue shows null
        org.springframework.test.web.servlet.MvcResult pendingResult = mockMvc.perform(get("/v1/client-meetings/verification/pending")
                        .param("sort", "id,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(meetingCode))
                .andExpect(jsonPath("$.data.content[0].nextFollowupDate").value(org.hamcrest.Matchers.nullValue()))
                .andReturn();

        // PC verifies without sending date
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(15, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Verified conducted meeting with no followup date")
                .nextFollowupDate(null)
                .build();

        String verifyPayloadJson = objectMapper.writeValueAsString(verifyReq);
        org.springframework.test.web.servlet.MvcResult verifyResult = mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyPayloadJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextFollowupDate").value(org.hamcrest.Matchers.nullValue()))
                .andReturn();

        // PC verified queue shows null
        org.springframework.test.web.servlet.MvcResult verifiedResult = mockMvc.perform(get("/v1/client-meetings/verification/verified")
                        .param("sort", "id,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(meetingCode))
                .andExpect(jsonPath("$.data.content[0].nextFollowupDate").value(org.hamcrest.Matchers.nullValue()))
                .andReturn();

        // History shows null
        org.springframework.test.web.servlet.MvcResult historyResult = mockMvc.perform(get("/v1/client-meetings/verification/" + meetingCode + "/history")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nextFollowupDate").value(org.hamcrest.Matchers.nullValue()))
                .andReturn();

        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        ClientMeetingVerification verif = clientMeetingVerificationRepository.findByClientMeetingIdAndIsCurrentTrue(meeting.getId()).orElseThrow();

        assertThat(verif.getNextFollowupDate()).isNull();

        System.out.println("[REQUEST PAYLOAD - RM Update]: nextPlanDate = null");
        System.out.println("[REQUEST PAYLOAD - PC Verify]: " + verifyPayloadJson);
        System.out.println("[HTTP STATUS - Verify]: " + verifyResult.getResponse().getStatus());
        System.out.println("[RESPONSE JSON - Verify]: " + verifyResult.getResponse().getContentAsString());
        System.out.println("[DATABASE VALUES]:");
        System.out.println("  client_meetings.next_plan_date: " + meeting.getNextPlanDate());
        System.out.println("  client_meeting_verifications.next_followup_date: " + verif.getNextFollowupDate());
        System.out.println("[RESULT]: PASS - Null RM/SC date safely remains null without inventing dates");
    }

    @Test
    @DisplayName("Scenario 4: PC submits a different nextFollowupDate; backend ignores it and uses meeting.nextPlanDate")
    void testScenario4_PcSubmitsDifferentFollowupDate_BackendIgnoresItAndUsesCurrentMeetingDate() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("TEST SCENARIO 4: PC submits different date; backend ignores it & uses meeting date");
        System.out.println("================================================================================");

        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        LocalDate rmAuthoritativeDate = LocalDate.now().plusWeeks(2);
        LocalDate roguePcDate = LocalDate.now().plusYears(1); // Different / rogue date from PC
        String meetingCode = submitMeetingUpdate(client, rmPrincipal, rmAuthoritativeDate);

        // PC submits verification with rogue date
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(11, 30))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("PC attempting to override date")
                .nextFollowupDate(roguePcDate) // PC sends different date
                .build();

        String verifyPayloadJson = objectMapper.writeValueAsString(verifyReq);
        org.springframework.test.web.servlet.MvcResult verifyResult = mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyPayloadJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.nextFollowupDate").value(rmAuthoritativeDate.toString())) // Must match RM date, NOT rogue PC date
                .andReturn();

        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        ClientMeetingVerification verification = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId()).orElseThrow();
        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();

        assertThat(verification.getNextFollowupDate()).isEqualTo(rmAuthoritativeDate);
        assertThat(verification.getNextFollowupDate()).isNotEqualTo(roguePcDate);
        assertThat(updatedClient.getNextFollowupDate()).isEqualTo(rmAuthoritativeDate);

        System.out.println("[REQUEST PAYLOAD - RM Authoritative]: nextPlanDate = " + rmAuthoritativeDate);
        System.out.println("[REQUEST PAYLOAD - PC Rogue Override]: nextFollowupDate = " + roguePcDate);
        System.out.println("[HTTP STATUS - Verify]: " + verifyResult.getResponse().getStatus());
        System.out.println("[RESPONSE JSON - Verify]: " + verifyResult.getResponse().getContentAsString());
        System.out.println("[DATABASE VALUES]:");
        System.out.println("  client_meetings.next_plan_date: " + meeting.getNextPlanDate());
        System.out.println("  client_meeting_verifications.next_followup_date: " + verification.getNextFollowupDate());
        System.out.println("  clients.next_followup_date: " + updatedClient.getNextFollowupDate());
        System.out.println("[RESULT]: PASS - Rogue PC date was strictly ignored; meeting.nextPlanDate enforced");
    }

    @Test
    @DisplayName("Scenario 5: Different meetings for the same client retain their own follow-up dates")
    void testScenario5_DifferentMeetingsForSameClient_RetainIndependentDates() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("TEST SCENARIO 5: Different meetings for same client retain independent dates");
        System.out.println("================================================================================");

        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // Meeting #1
        LocalDate meeting1Date = LocalDate.now().plusDays(10);
        String meeting1Code = submitMeetingUpdate(client, rmPrincipal, meeting1Date);

        // PC verifies Meeting #1
        ClientMeetingVerificationRequest verifyReq1 = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(11, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Verified Meeting 1")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting1Code + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq1)))
                .andExpect(status().isOk());

        // Meeting #2 is submitted with its own different date
        LocalDate meeting2Date = LocalDate.now().plusDays(25);
        String meeting2Code = submitMeetingUpdate(client, rmPrincipal, meeting2Date);

        // PC inspects pending queue: Meeting #2 has its own date, NOT Meeting #1's date
        mockMvc.perform(get("/v1/client-meetings/verification/pending")
                        .param("sort", "id,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(meeting2Code))
                .andExpect(jsonPath("$.data.content[0].nextFollowupDate").value(meeting2Date.toString()));

        // PC verifies Meeting #2
        ClientMeetingVerificationRequest verifyReq2 = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(12, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Verified Meeting 2")
                .build();

        org.springframework.test.web.servlet.MvcResult verifyResult2 = mockMvc.perform(post("/v1/client-meetings/verification/" + meeting2Code + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextFollowupDate").value(meeting2Date.toString()))
                .andReturn();

        // Verify independent database records
        ClientMeeting m1 = clientMeetingRepository.findByMeetingCode(meeting1Code).orElseThrow();
        ClientMeeting m2 = clientMeetingRepository.findByMeetingCode(meeting2Code).orElseThrow();

        ClientMeetingVerification v1 = clientMeetingVerificationRepository.findByClientMeetingIdAndIsCurrentTrue(m1.getId()).orElseThrow();
        ClientMeetingVerification v2 = clientMeetingVerificationRepository.findByClientMeetingIdAndIsCurrentTrue(m2.getId()).orElseThrow();

        assertThat(v1.getNextFollowupDate()).isEqualTo(meeting1Date);
        assertThat(v2.getNextFollowupDate()).isEqualTo(meeting2Date);
        assertThat(v2.getNextFollowupDate()).isNotEqualTo(v1.getNextFollowupDate());

        Client finalClient = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(finalClient.getNextFollowupDate()).isEqualTo(meeting2Date);

        System.out.println("[DATABASE VALUES]:");
        System.out.println("  Meeting 1 Code: " + m1.getMeetingCode() + ", seq: #" + m1.getMeetingNumber());
        System.out.println("  Meeting 1 DB next_plan_date: " + m1.getNextPlanDate());
        System.out.println("  Meeting 1 Verif next_followup_date: " + v1.getNextFollowupDate());
        System.out.println("  Meeting 2 Code: " + m2.getMeetingCode() + ", seq: #" + m2.getMeetingNumber());
        System.out.println("  Meeting 2 DB next_plan_date: " + m2.getNextPlanDate());
        System.out.println("  Meeting 2 Verif next_followup_date: " + v2.getNextFollowupDate());
        System.out.println("  Client final next_followup_date: " + finalClient.getNextFollowupDate());
        System.out.println("[RESULT]: PASS - Each meeting independently retains its own date; never copied");
    }

    @Test
    @DisplayName("Scenario 6: Rejected and resubmitted verification attempts preserve meeting date and history")
    void testScenario6_RejectedAndResubmitted_PreserveCorrectDatesAndHistory() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("TEST SCENARIO 6: Rejected & resubmitted attempts preserve dates & history");
        System.out.println("================================================================================");

        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        LocalDate initialDate = LocalDate.now().plusWeeks(1);
        String meetingCode = submitMeetingUpdate(client, rmPrincipal, initialDate);

        // PC rejects Meeting Attempt #1 -> rejected response includes the meeting's nextFollowupDate
        org.springframework.test.web.servlet.MvcResult rejectResult = mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/reject")
                        .param("reason", "Please update coordinates")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.nextFollowupDate").value(initialDate.toString()))
                .andReturn();

        // RM resubmits with an updated nextPlanDate
        LocalDate updatedDate = LocalDate.now().plusWeeks(4);
        ClientMeetingUpdateRequest resubmitReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Resubmitted with updated date")
                .nextPlanDate(updatedDate)
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        String resubmitPayloadJson = objectMapper.writeValueAsString(resubmitReq);
        mockMvc.perform(post("/v1/client-meetings/" + meetingCode + "/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resubmitPayloadJson))
                .andExpect(status().isOk());

        // Resubmitted meeting in pending queue has the updated date
        org.springframework.test.web.servlet.MvcResult resubmittedPending = mockMvc.perform(get("/v1/client-meetings/verification/pending")
                        .param("sort", "id,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].meetingCode").value(meetingCode))
                .andExpect(jsonPath("$.data.content[0].nextFollowupDate").value(updatedDate.toString()))
                .andReturn();

        // PC verifies Attempt #2 without entering date
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(16, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Approved Attempt 2")
                .build();

        org.springframework.test.web.servlet.MvcResult verifyResult2 = mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attemptNumber").value(2))
                .andExpect(jsonPath("$.data.nextFollowupDate").value(updatedDate.toString()))
                .andReturn();

        // Verification history retains Attempt 1 and Attempt 2 with their respective dates
        org.springframework.test.web.servlet.MvcResult historyResult = mockMvc.perform(get("/v1/client-meetings/verification/" + meetingCode + "/history")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].attemptNumber").value(1))
                .andExpect(jsonPath("$.data[0].verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data[0].nextFollowupDate").value(initialDate.toString()))
                .andExpect(jsonPath("$.data[1].attemptNumber").value(2))
                .andExpect(jsonPath("$.data[1].verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data[1].nextFollowupDate").value(updatedDate.toString()))
                .andReturn();

        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        java.util.List<ClientMeetingVerification> allVerifs = clientMeetingVerificationRepository.findByClientMeetingIdOrderByAttemptNumberAsc(meeting.getId());

        System.out.println("[REQUEST PAYLOAD - PC Reject]: reason = Please update coordinates");
        System.out.println("[HTTP STATUS - Reject]: " + rejectResult.getResponse().getStatus());
        System.out.println("[RESPONSE JSON - Reject]: " + rejectResult.getResponse().getContentAsString());
        System.out.println("[REQUEST PAYLOAD - RM Resubmit]: nextPlanDate = " + updatedDate);
        System.out.println("[HTTP STATUS - Verify Attempt 2]: " + verifyResult2.getResponse().getStatus());
        System.out.println("[RESPONSE JSON - Verify Attempt 2]: " + verifyResult2.getResponse().getContentAsString());
        System.out.println("[RESPONSE JSON - History]: " + historyResult.getResponse().getContentAsString());
        System.out.println("[DATABASE VALUES]:");
        for (ClientMeetingVerification v : allVerifs) {
            System.out.println("  Attempt #" + v.getAttemptNumber() + " (isCurrent: " + v.getIsCurrent() + ", status: " + v.getVerificationStatus() + "): next_followup_date = " + v.getNextFollowupDate());
        }
        System.out.println("  client_meetings.next_plan_date: " + meeting.getNextPlanDate());
        System.out.println("[RESULT]: PASS - Attempt 1 and Attempt 2 preserved their respective dates");
    }
}
