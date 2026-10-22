package com.blueant_crm_erp.client;

import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.response.ClientMeetingResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingUpdateRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientMeetingWorkflowIntegrationE2ETest {

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
    private ClientMeetingUpdateRepository clientMeetingUpdateRepository;

    @Autowired
    private ClientMeetingVerificationRepository clientMeetingVerificationRepository;

    @Autowired
    private UserDetailsService customUserDetailsService;

    private User rmUser;
    private User pcUser;
    private Team defaultTeam;

    @BeforeEach
    void setUp() {
        Department salesDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("Sales").code(BootstrapConstants.DEPT_SALES).status(Status.ACTIVE).build()));

        Department crmDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("CRM").code(BootstrapConstants.DEPT_CRM).status(Status.ACTIVE).build()));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        rmUser = createTestUser("RM_E2E", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        pcUser = createTestUser("PC_E2E", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, crmDept, "MEETING_VERIFY", "MEETING_READ");
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

    // =========================================================================
    // REQUIREMENT 1 & 2: RM/SC Meeting Update without person details & PC verification
    // =========================================================================

    @Test
    @DisplayName("RM submits SOMEONE without person details -> PC enters person details -> Verified successfully")
    void testRmSubmitsSomeoneWithoutPersonDetails_AndPcVerifiesWithDetails() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // 1. RM updates meeting with aloneWith = SOMEONE (no personName or position in request)
        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SOMEONE")
                .businessGenerated("NO")
                .remarks("Conducted meeting with colleague present")
                .nextPlanDate(LocalDate.now().plusMonths(2)) // 2 calendar months limit
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        String responseStr = mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aloneWith").value("SOMEONE"))
                .andExpect(jsonPath("$.data.personName").doesNotExist())
                .andExpect(jsonPath("$.data.position").doesNotExist())
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        String meetingCode = objectMapper.readTree(responseStr).path("data").path("meetingCode").asText();

        // 2. PC attempts to verify with aloneWith = SOMEONE but missing personName -> 400 Bad Request
        ClientMeetingVerificationRequest missingNameReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 30))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .position("RM")
                .remarks("Verification attempt")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingNameReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Person name is required")));

        // 3. PC verifies with aloneWith = SOMEONE and valid personName and position -> 200 OK
        ClientMeetingVerificationRequest validPcReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 30))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName("Sunita Rao")
                .position("RM")
                .remarks("Confirmed colleague Sunita Rao was present")
                .nextFollowupDate(LocalDate.now().plusDays(20))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPcReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.aloneWith").value("SOMEONE"))
                .andExpect(jsonPath("$.data.personName").value("Sunita Rao"))
                .andExpect(jsonPath("$.data.position").value("RM"));

        // 4. Verify persisted meeting entity has the verified values
        ClientMeeting savedMeeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        assertThat(savedMeeting.getVerifiedByPc()).isTrue();
        assertThat(savedMeeting.getAloneWith()).isEqualTo("SOMEONE");
        assertThat(savedMeeting.getPersonName()).isEqualTo("Sunita Rao");
        assertThat(savedMeeting.getPosition()).isEqualTo("RM");
    }

    @Test
    @DisplayName("RM submits SELF -> PC verifies with SELF without person details -> Verified successfully")
    void testRmSubmitsSelf_AndPcVerifiesWithSelf() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // RM submits aloneWith = SELF
        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Solo meeting")
                .nextPlanDate(LocalDate.now().plusMonths(2))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        String responseStr = mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String meetingCode = objectMapper.readTree(responseStr).path("data").path("meetingCode").asText();

        // PC provides personName when aloneWith = SELF -> Rejected with 400
        ClientMeetingVerificationRequest invalidSelfReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(11, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .personName("Inappropriate Name")
                .remarks("Remarks")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSelfReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Person name and position must not be provided when aloneWith is SELF")));

        // PC verifies aloneWith = SELF without personName/position -> 200 OK
        ClientMeetingVerificationRequest validSelfReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(11, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Verified solo meeting with doctor")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSelfReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.aloneWith").value("SELF"))
                .andExpect(jsonPath("$.data.personName").doesNotExist())
                .andExpect(jsonPath("$.data.position").doesNotExist());
    }

    @Test
    @DisplayName("Immutable Verification History: PC Rejection -> RM Resubmission -> PC Verification preserves attempt records")
    void testVerificationHistoryPreservedAcrossAttempts() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // Attempt 1: RM submits meeting
        ClientMeetingUpdateRequest update1 = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SOMEONE")
                .businessGenerated("NO")
                .remarks("First submission")
                .nextPlanDate(LocalDate.now().plusMonths(2))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        String responseStr = mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String meetingCode = objectMapper.readTree(responseStr).path("data").path("meetingCode").asText();

        // PC Rejects Attempt 1
        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "Needs re-verification of attendee details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.attemptNumber").value(1));

        // Attempt 2: RM resubmits
        ClientMeetingUpdateRequest update2 = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SOMEONE")
                .businessGenerated("NO")
                .remarks("Resubmission with confirmed attendee")
                .nextPlanDate(LocalDate.now().plusMonths(2))
                .latitude(new BigDecimal("28.6140"))
                .longitude(new BigDecimal("77.2091"))
                .accuracy(8.0)
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));

        // PC verifies Attempt 2 with person details
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(14, 0))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName("Dr. Anita Roy")
                .position("TL")
                .remarks("Verified with team leader presence")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.attemptNumber").value(2))
                .andExpect(jsonPath("$.data.personName").value("Dr. Anita Roy"))
                .andExpect(jsonPath("$.data.position").value("TL"));

        // Query verification history: must have 2 distinct immutable attempts
        mockMvc.perform(get("/v1/client-meetings/verification/" + meetingCode + "/history")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].attemptNumber").value(1))
                .andExpect(jsonPath("$.data[0].verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data[0].isCurrent").value(false))
                .andExpect(jsonPath("$.data[0].rejectionReason").value("Needs re-verification of attendee details"))
                .andExpect(jsonPath("$.data[1].attemptNumber").value(2))
                .andExpect(jsonPath("$.data[1].verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data[1].isCurrent").value(true))
                .andExpect(jsonPath("$.data[1].personName").value("Dr. Anita Roy"))
                .andExpect(jsonPath("$.data[1].position").value("TL"));
    }

    // =========================================================================
    // REQUIREMENT 3: Client follow-up date accepts up to 2 calendar months and rejects beyond
    // =========================================================================

    @Test
    @DisplayName("Endpoint rejects nextPlanDate exceeding 2 calendar months and accepts up to 2 months")
    void testFollowUpDateLimitEndpoints() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        // 1 day beyond 2 calendar months -> Rejected
        ClientMeetingUpdateRequest exceedReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Exceeding date test")
                .nextPlanDate(LocalDate.now().plusMonths(2).plusDays(1))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exceedReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cannot exceed two calendar months")));

        // Exactly 2 calendar months -> 200 OK
        ClientMeetingUpdateRequest validReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Exactly 2 months test")
                .nextPlanDate(LocalDate.now().plusMonths(2))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextPlanDate").value(LocalDate.now().plusMonths(2).toString()));
    }
}
