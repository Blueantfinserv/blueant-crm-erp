package com.blueant_crm_erp.client;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.dto.request.UpdateClientRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.enums.ClientBestTimeToMeet;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Client Best Time to Meet Integration Tests")
class ClientBestTimeToMeetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private UserDetailsService customUserDetailsService;

    private User crmUser;
    private User rmUser;
    private UserDetails crmPrincipal;
    private UserDetails rmPrincipal;
    private Team defaultTeam;

    @BeforeEach
    void setUp() {
        Department salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        Department crmDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(salesDept));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        crmUser = createTestUser("CRM_USER", BootstrapConstants.ROLE_CRM_ONBOARDING, BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, "PHYSICAL_LEAD_ASSIGN", "client:read");
        rmUser = createTestUser("RM_USER", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");

        crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
        rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
    }

    private User createTestUser(String prefix, String roleCode, String desigCode, Department dept, String... permissions) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Role role = roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(roleCode).name(roleCode).status(Status.ACTIVE).systemRole(true).defaultRole(false).build()));

        for (String permCode : permissions) {
            Permission perm = permissionRepository.findByCodeIgnoreCase(permCode)
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .code(permCode).name(permCode).module("TEST").displayOrder(1).systemPermission(false).status(Status.ACTIVE).build()));
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

    private String randomMobile() {
        return "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "By Appointment",
            "1st half of the day",
            "2nd half of the day",
            "Only on weekend"
    })
    @DisplayName("1. Register client with exact display labels succeeds and persists correctly")
    void testRegisterClient_withExactDisplayLabels(String label) throws Exception {
        String mobile = randomMobile();
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Client " + label)
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Fortis Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .bestTimeToMeet(label)
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bestTimeToMeet").value(label));

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getBestTimeToMeet()).isEqualTo(label);
    }

    @ParameterizedTest
    @CsvSource({
            "BY_APPOINTMENT, By Appointment",
            "FIRST_HALF, 1st half of the day",
            "SECOND_HALF, 2nd half of the day",
            "WEEKENDS_ONLY, Only on weekend",
            "by_appointment, By Appointment",
            "first_half, 1st half of the day",
            "second_half, 2nd half of the day",
            "weekends_only, Only on weekend"
    })
    @DisplayName("2. Register client with canonical enum codes normalizes to display label")
    void testRegisterClient_withCanonicalEnumCodes(String code, String expectedLabel) throws Exception {
        String mobile = randomMobile();
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Client " + code)
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Fortis Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .bestTimeToMeet(code)
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bestTimeToMeet").value(expectedLabel));

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getBestTimeToMeet()).isEqualTo(expectedLabel);
    }

    @ParameterizedTest
    @CsvSource({
            "1st half, 1st half of the day",
            "FIRST HALF, 1st half of the day",
            "2nd half, 2nd half of the day",
            "SECOND HALF, 2nd half of the day",
            "Weekend, Only on weekend",
            "Weekends, Only on weekend",
            "Only on weekends, Only on weekend"
    })
    @DisplayName("3. Register client with aliases normalizes to canonical display label")
    void testRegisterClient_withAliases(String alias, String expectedLabel) throws Exception {
        String mobile = randomMobile();
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Client " + alias)
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Fortis Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .bestTimeToMeet(alias)
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bestTimeToMeet").value(expectedLabel));

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getBestTimeToMeet()).isEqualTo(expectedLabel);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "9:00 AM - 12:00 PM",
            "10:00 AM - 11:00 AM",
            "12:00 PM - 3:00 PM",
            "INVALID_TIME_SLOT",
            "Morning",
            "Afternoon"
    })
    @DisplayName("4. Register client with invalid time slot is rejected with 400 Bad Request")
    void testRegisterClient_withInvalidBestTimeToMeet(String invalidInput) throws Exception {
        String mobile = randomMobile();
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Client " + invalidInput)
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Fortis Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .bestTimeToMeet(invalidInput)
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid bestTimeToMeet")));
    }

    @Test
    @DisplayName("5. Register client with null or empty bestTimeToMeet is accepted and saved as null")
    void testRegisterClient_withNullOrBlank() throws Exception {
        String mobile = randomMobile();
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Client Null Slot")
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Fortis Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .bestTimeToMeet(null)
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bestTimeToMeet").doesNotExist());

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getBestTimeToMeet()).isNull();
    }

    @Test
    @DisplayName("6. Existing client records with legacy time-range values are preserved and readable")
    void testLegacyClientRecordCompatibility() throws Exception {
        String mobile = randomMobile();
        String legacySlot = "12:00 PM - 3:00 PM";
        String clientCode = "BA-CL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Client legacyClient = Client.builder()
                .clientCode(clientCode)
                .clientStatus(ClientStatus.ACTIVE)
                .clientName("Dr. Legacy Record")
                .mobileNumber(mobile)
                .speciality("Orthopedics")
                .location("South Delhi")
                .clinicAddress("Bone Clinic")
                .salesPerson(rmUser)
                .createdBySalesPerson(rmUser)
                .assignedBy(crmUser)
                .assignedAt(LocalDateTime.now())
                .assignmentDate(LocalDate.now())
                .bestTimeToMeet(legacySlot)
                .clientSince(LocalDate.now())
                .build();

        Client saved = clientRepository.save(legacyClient);

        // 1. Verify profile retrieval preserves legacy slot intact
        mockMvc.perform(get("/v1/clients/" + clientCode + "/profile")
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bestTimeToMeet").value(legacySlot));

        // 2. Verify work-follow retrieval preserves legacy slot intact
        mockMvc.perform(get("/v1/clients/work-follow")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].bestTimeToMeet").value(legacySlot));

        // 3. Updating client record preserves legacy bestTimeToMeet intact
        saved.setClientName("Dr. Legacy Record Updated");
        clientRepository.save(saved);

        Client reloaded = clientRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. Legacy Record Updated");
        assertThat(reloaded.getBestTimeToMeet()).isEqualTo(legacySlot);
    }
}
