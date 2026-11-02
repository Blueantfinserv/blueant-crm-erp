package com.blueant_crm_erp.client;

import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.entity.ClientMeetingUpdate;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingUpdateRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientMeetingOptionalQuestionsIntegrationTest {

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

        rmUser = createTestUser("RM_OPT", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        pcUser = createTestUser("PC_OPT", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, crmDept, "MEETING_VERIFY", "MEETING_READ");
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
                .clinicAddress("Original Clinic Address")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(salesPerson)
                .build());
    }

    @Test
    @DisplayName("Meeting update with all 10 optional questions answered persists to meeting and update audit")
    void testMeetingUpdateWithAllOptionalQuestions_SuccessAndHistoricalPersistence() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Conducted comprehensive client discussion")
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                // 10 Additional Optional Fields
                .residentialAddress("123 Civil Lines, New Delhi")
                .officeAddress("Suite 401, Connaught Place, New Delhi")
                .currentCity("New Delhi")
                .preferredContactTime("10:00 AM - 12:00 PM")
                .occupationBusinessType("Cardiologist / Private Clinic")
                .companyOrganizationName("Heart Care Clinic Ltd")
                .communicationPreference("WHATSAPP")
                .preferredInvestmentService("Mutual Funds & PMS")
                .clientRequirements("Looking for high-yield low-risk options")
                .otherInfoNotes("Met along with practice manager")
                .build();

        String responseStr = mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.residentialAddress").value("123 Civil Lines, New Delhi"))
                .andExpect(jsonPath("$.data.officeAddress").value("Suite 401, Connaught Place, New Delhi"))
                .andExpect(jsonPath("$.data.currentCity").value("New Delhi"))
                .andExpect(jsonPath("$.data.preferredContactTime").value("10:00 AM - 12:00 PM"))
                .andExpect(jsonPath("$.data.occupationBusinessType").value("Cardiologist / Private Clinic"))
                .andExpect(jsonPath("$.data.companyOrganizationName").value("Heart Care Clinic Ltd"))
                .andExpect(jsonPath("$.data.communicationPreference").value("WHATSAPP"))
                .andExpect(jsonPath("$.data.preferredInvestmentService").value("Mutual Funds & PMS"))
                .andExpect(jsonPath("$.data.clientRequirements").value("Looking for high-yield low-risk options"))
                .andExpect(jsonPath("$.data.otherInfoNotes").value("Met along with practice manager"))
                .andReturn().getResponse().getContentAsString();

        String meetingCode = objectMapper.readTree(responseStr).path("data").path("meetingCode").asText();

        // 1. Verify ClientMeeting entity
        ClientMeeting savedMeeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        assertThat(savedMeeting.getResidentialAddress()).isEqualTo("123 Civil Lines, New Delhi");
        assertThat(savedMeeting.getOfficeAddress()).isEqualTo("Suite 401, Connaught Place, New Delhi");
        assertThat(savedMeeting.getCurrentCity()).isEqualTo("New Delhi");
        assertThat(savedMeeting.getPreferredContactTime()).isEqualTo("10:00 AM - 12:00 PM");
        assertThat(savedMeeting.getOccupationBusinessType()).isEqualTo("Cardiologist / Private Clinic");
        assertThat(savedMeeting.getCompanyOrganizationName()).isEqualTo("Heart Care Clinic Ltd");
        assertThat(savedMeeting.getCommunicationPreference()).isEqualTo("WHATSAPP");
        assertThat(savedMeeting.getPreferredInvestmentService()).isEqualTo("Mutual Funds & PMS");
        assertThat(savedMeeting.getClientRequirements()).isEqualTo("Looking for high-yield low-risk options");
        assertThat(savedMeeting.getOtherInfoNotes()).isEqualTo("Met along with practice manager");

        // 2. Verify ClientMeetingUpdate audit record
        List<ClientMeetingUpdate> updates = clientMeetingUpdateRepository.findByClientMeetingIdOrderByUpdateNumberAsc(savedMeeting.getId());
        assertThat(updates).hasSize(1);
        ClientMeetingUpdate update1 = updates.get(0);
        assertThat(update1.getUpdateNumber()).isEqualTo(1);
        assertThat(update1.getResidentialAddress()).isEqualTo("123 Civil Lines, New Delhi");
        assertThat(update1.getOfficeAddress()).isEqualTo("Suite 401, Connaught Place, New Delhi");
        assertThat(update1.getCurrentCity()).isEqualTo("New Delhi");
        assertThat(update1.getPreferredContactTime()).isEqualTo("10:00 AM - 12:00 PM");
        assertThat(update1.getOccupationBusinessType()).isEqualTo("Cardiologist / Private Clinic");
        assertThat(update1.getCompanyOrganizationName()).isEqualTo("Heart Care Clinic Ltd");
        assertThat(update1.getCommunicationPreference()).isEqualTo("WHATSAPP");
        assertThat(update1.getPreferredInvestmentService()).isEqualTo("Mutual Funds & PMS");
        assertThat(update1.getClientRequirements()).isEqualTo("Looking for high-yield low-risk options");
        assertThat(update1.getOtherInfoNotes()).isEqualTo("Met along with practice manager");

        // 3. Verify getMeetingUpdateHistory API exposes historical values
        mockMvc.perform(get("/v1/client-meetings/" + meetingCode + "/update-history")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].updateNumber").value(1))
                .andExpect(jsonPath("$.data[0].residentialAddress").value("123 Civil Lines, New Delhi"))
                .andExpect(jsonPath("$.data[0].officeAddress").value("Suite 401, Connaught Place, New Delhi"))
                .andExpect(jsonPath("$.data[0].currentCity").value("New Delhi"))
                .andExpect(jsonPath("$.data[0].companyOrganizationName").value("Heart Care Clinic Ltd"));

        // 4. Verify Client master profile was NOT modified
        Client reloadedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloadedClient.getClinicAddress()).isEqualTo("Original Clinic Address");
    }

    @Test
    @DisplayName("Meeting update with blank/omitted optional questions succeeds without blocking")
    void testMeetingUpdateWithBlankOptionalQuestions_Success() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        // Request with zero optional fields populated
        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Conducted meeting without any optional question answers")
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.residentialAddress").doesNotExist())
                .andExpect(jsonPath("$.data.officeAddress").doesNotExist())
                .andExpect(jsonPath("$.data.currentCity").doesNotExist());
    }

    @Test
    @DisplayName("Resubmission preserves historical answers: Update #1 retains original answers, Update #2 contains new answers")
    void testResubmissionPreservesHistoricalAnswers() throws Exception {
        Client client = createTestClient(rmUser);
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // 1. Initial Meeting Update (#1) with initial answers
        ClientMeetingUpdateRequest updateReq1 = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("First submission")
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .currentCity("Mumbai")
                .residentialAddress("Marine Drive, Mumbai")
                .companyOrganizationName("Original Hospital")
                .build();

        String respStr = mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String meetingCode = objectMapper.readTree(respStr).path("data").path("meetingCode").asText();

        // 2. PC Rejection of Update #1
        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "Need updated hospital affiliation details"))
                .andExpect(status().isOk());

        // 3. RM Resubmits Meeting Update (#2) with revised answers
        ClientMeetingUpdateRequest updateReq2 = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Second submission with updated details")
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .currentCity("Navi Mumbai")
                .residentialAddress("Vashi Sector 17, Navi Mumbai")
                .companyOrganizationName("Apollo Hospitals")
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentCity").value("Navi Mumbai"))
                .andExpect(jsonPath("$.data.companyOrganizationName").value("Apollo Hospitals"));

        // 4. Verify update history preserves both snapshots independently
        mockMvc.perform(get("/v1/client-meetings/" + meetingCode + "/update-history")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                // Update #1 preserved
                .andExpect(jsonPath("$.data[0].updateNumber").value(1))
                .andExpect(jsonPath("$.data[0].currentCity").value("Mumbai"))
                .andExpect(jsonPath("$.data[0].residentialAddress").value("Marine Drive, Mumbai"))
                .andExpect(jsonPath("$.data[0].companyOrganizationName").value("Original Hospital"))
                // Update #2 has new answers
                .andExpect(jsonPath("$.data[1].updateNumber").value(2))
                .andExpect(jsonPath("$.data[1].currentCity").value("Navi Mumbai"))
                .andExpect(jsonPath("$.data[1].residentialAddress").value("Vashi Sector 17, Navi Mumbai"))
                .andExpect(jsonPath("$.data[1].companyOrganizationName").value("Apollo Hospitals"));
    }
}
