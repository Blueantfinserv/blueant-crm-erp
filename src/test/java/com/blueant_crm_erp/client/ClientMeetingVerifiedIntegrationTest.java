package com.blueant_crm_erp.client;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientMeeting;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientMeetingVerifiedIntegrationTest {

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

    private User rmUser;
    private User pcUser;
    private User unauthorizedUser;
    private Team defaultTeam;

    @BeforeEach
    void setUp() {
        Department salesDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("Sales").code(BootstrapConstants.DEPT_SALES).status(Status.ACTIVE).build()));

        Department crmDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("CRM").code(BootstrapConstants.DEPT_CRM).status(Status.ACTIVE).build()));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        rmUser = createTestUser("RM_VERIF", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        pcUser = createTestUser("PC_VERIF", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, crmDept, "MEETING_VERIFY", "MEETING_READ");
        unauthorizedUser = createTestUser("UNAUTH_VERIF", "SALES_ASSOCIATE", "DESIG_SA", salesDept, "CLIENT_READ");
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

    private String submitMeetingUpdate(Client client, UserDetails rmPrincipal, String aloneWith) throws Exception {
        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith(aloneWith)
                .businessGenerated("NO")
                .remarks("Meeting conducted update for verification test")
                .nextPlanDate(LocalDate.now().plusMonths(1))
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
    @DisplayName("RM submits meeting update -> appears in pending, does NOT appear in verified list")
    void testRmSubmitsMeeting_AppearsInPending_NotPresentInVerified() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, rmPrincipal, "SELF");

        // PC checks pending: meeting must be present
        mockMvc.perform(get("/v1/client-meetings/verification/pending")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')].verificationStatus").value("PENDING"));

        // PC checks verified: meeting must NOT be present
        mockMvc.perform(get("/v1/client-meetings/verification/verified")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')]").doesNotExist());
    }

    @Test
    @DisplayName("PC verifies meeting -> appears in verified list with status VERIFIED, disappears from pending")
    void testPcVerifiesMeeting_AppearsInVerifiedList_DisappearsFromPending() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, rmPrincipal, "SELF");

        // PC verifies meeting
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(11, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Verified successfully by PC")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

        // PC checks verified: meeting must now be present with VERIFIED status and isCurrent = true
        mockMvc.perform(get("/v1/client-meetings/verification/verified")
                        .param("sort", "id,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')].verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')].isCurrent").value(true))
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')].aloneWith").value("SELF"));

        // PC checks pending: meeting must NOT be present anymore
        mockMvc.perform(get("/v1/client-meetings/verification/pending")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')]").doesNotExist());
    }

    @Test
    @DisplayName("PC rejects meeting -> does NOT appear in verified list and disappears from pending")
    void testPcRejectsMeeting_DoesNotAppearInVerified() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, rmPrincipal, "SELF");

        // PC rejects meeting
        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "Doctor was unavailable, meeting details inconsistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("REJECTED"));

        // Meeting must NOT appear in verified list
        mockMvc.perform(get("/v1/client-meetings/verification/verified")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')]").doesNotExist());

        // Meeting must NOT appear in pending list
        mockMvc.perform(get("/v1/client-meetings/verification/pending")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')]").doesNotExist());
    }

    @Test
    @DisplayName("Re-update of rejected meeting creates attempt 2 -> when verified, only attempt 2 is in verified list and history is preserved")
    void testReUpdateAfterRejection_VerifiedShowsOnlyCurrentAttempt_HistoryPreserved() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // 1. Initial RM meeting update
        String meetingCode = submitMeetingUpdate(client, rmPrincipal, "SOMEONE");

        // 2. PC rejects Attempt 1
        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "Missing attendee details"))
                .andExpect(status().isOk());

        // 3. RM re-updates the meeting -> creates Attempt 2 (PENDING)
        ClientMeetingUpdateRequest reUpdateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SOMEONE")
                .businessGenerated("NO")
                .remarks("Re-submitted with corrected info")
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reUpdateReq)))
                .andExpect(status().isOk());

        // At this point, Attempt 2 is PENDING -> must NOT be in verified list
        mockMvc.perform(get("/v1/client-meetings/verification/verified")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')]").doesNotExist());

        // 4. PC verifies Attempt 2
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(15, 30))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName("Dr. Roy")
                .position("TL")
                .remarks("Verified attempt 2 with colleague details")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attemptNumber").value(2))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

        // 5. In verified list: exactly one record for this meeting, with attemptNumber = 2 and isCurrent = true
        mockMvc.perform(get("/v1/client-meetings/verification/verified")
                        .param("sort", "id,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')].attemptNumber").value(2))
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')].verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')].isCurrent").value(true));

        // 6. Verification history must have both attempts intact (attempt 1 REJECTED isCurrent=false, attempt 2 VERIFIED isCurrent=true)
        mockMvc.perform(get("/v1/client-meetings/verification/" + meetingCode + "/history")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].attemptNumber").value(1))
                .andExpect(jsonPath("$.data[0].verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data[0].isCurrent").value(false))
                .andExpect(jsonPath("$.data[1].attemptNumber").value(2))
                .andExpect(jsonPath("$.data[1].verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data[1].isCurrent").value(true));
    }

    @Test
    @DisplayName("Verified list pagination returns valid PageResponse schema")
    void testVerifiedListPagination() throws Exception {
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        mockMvc.perform(get("/v1/client-meetings/verification/verified")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "verifiedAt,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.totalPages").isNumber());
    }

    @Test
    @DisplayName("Unauthorized users cannot access verified client meetings list")
    void testVerifiedList_UnauthorizedAccessForbidden() throws Exception {
        UserDetails unauthPrincipal = customUserDetailsService.loadUserByUsername(unauthorizedUser.getEmployeeCode());

        // User without MEETING_VERIFY permission / PC_COORDINATOR role -> 403 Forbidden
        mockMvc.perform(get("/v1/client-meetings/verification/verified")
                        .with(user(unauthPrincipal)))
                .andExpect(status().isForbidden());

        // Anonymous request -> 401 Unauthorized
        mockMvc.perform(get("/v1/client-meetings/verification/verified"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Genuinely unmapped routes return HTTP 404 with safe ApiError response, not HTTP 500")
    void testUnmappedRoute_Returns404NotFoundInsteadOf500() throws Exception {
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        mockMvc.perform(get("/v1/client-meetings/verification/nonexistent-route-for-testing")
                        .with(user(pcPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }
}
