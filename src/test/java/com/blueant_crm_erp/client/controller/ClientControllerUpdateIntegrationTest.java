package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.request.UpdateClientRequest;
import com.blueant_crm_erp.client.entity.Client;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Client Controller Update Integration Tests")
class ClientControllerUpdateIntegrationTest {

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
    private Team defaultTeam;

    @BeforeEach
    void setUp() {
        Department salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        Department crmDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(salesDept));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        crmUser = createTestUser("CRM_UP_USER", BootstrapConstants.ROLE_CRM_ONBOARDING, BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, "PHYSICAL_LEAD_ASSIGN", "client:read");
        rmUser = createTestUser("RM_UP_USER", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");

        crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
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

    private Client createBaseClient(String name, String mobile) {
        String clientCode = "BA-CL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return clientRepository.save(Client.builder()
                .clientCode(clientCode)
                .clientStatus(ClientStatus.ACTIVE)
                .clientName(name)
                .mobileNumber(mobile)
                .speciality("General Medicine")
                .location("Delhi")
                .clinicAddress("Base Clinic Address")
                .salesPerson(rmUser)
                .createdBySalesPerson(rmUser)
                .assignedBy(crmUser)
                .assignedAt(LocalDateTime.now())
                .assignmentDate(LocalDate.now())
                .clientSince(LocalDate.now())
                .build());
    }

    private String randomMobile() {
        return "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
    }

    @Test
    @DisplayName("1. Update client successfully with all 23 extended profile fields")
    void testUpdateClientWithAll23NewProfileFields() throws Exception {
        String mobile = randomMobile();
        String altMobile = randomMobile();
        Client client = createBaseClient("Dr. Base Profile", mobile);

        LocalDate dob = LocalDate.of(1985, 5, 20);
        LocalDate actualDob = LocalDate.of(1985, 5, 21);
        LocalDate assignDate = LocalDate.now().plusDays(3);

        UpdateClientRequest request = UpdateClientRequest.builder()
                .clientName("Dr. Updated Full Name")
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("South Delhi")
                .clinicAddress("Fortis Hospital, Suite 402")
                .alternateMobileNumber(altMobile)
                .dateOfBirth(dob)
                .actualDateOfBirth(actualDob)
                .occupation("Consultant Doctor")
                .motherName("Sunita Sharma")
                .placeOfBirth("New Delhi")
                .residentialAddress("42 Vasant Vihar, New Delhi")
                .correspondenceAddress("42 Vasant Vihar, New Delhi")
                .officeAddress("Fortis Hospital, Okhla")
                .city("New Delhi")
                .state("Delhi")
                .pinCode("110020")
                .clientSource("Direct Reference")
                .sourceDescription("Referred by Dr. Verma")
                .expectedMaxSIP(new BigDecimal("50000.00"))
                .firstInvestmentAmount(new BigDecimal("100000.00"))
                .investwellId("IW-987654")
                .bestTimeToMeet("1st half") // Valid alias to be normalized
                .assignmentDate(assignDate)
                .remarks("High net worth individual")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("Dr. Updated Full Name"))
                .andExpect(jsonPath("$.speciality").value("Cardiology"))
                .andExpect(jsonPath("$.location").value("South Delhi"))
                .andExpect(jsonPath("$.clinicAddress").value("Fortis Hospital, Suite 402"))
                .andExpect(jsonPath("$.alternateMobileNumber").value(altMobile))
                .andExpect(jsonPath("$.dateOfBirth").value("1985-05-20"))
                .andExpect(jsonPath("$.actualDateOfBirth").value("1985-05-21"))
                .andExpect(jsonPath("$.occupation").value("Consultant Doctor"))
                .andExpect(jsonPath("$.motherName").value("Sunita Sharma"))
                .andExpect(jsonPath("$.placeOfBirth").value("New Delhi"))
                .andExpect(jsonPath("$.residentialAddress").value("42 Vasant Vihar, New Delhi"))
                .andExpect(jsonPath("$.correspondenceAddress").value("42 Vasant Vihar, New Delhi"))
                .andExpect(jsonPath("$.officeAddress").value("Fortis Hospital, Okhla"))
                .andExpect(jsonPath("$.city").value("New Delhi"))
                .andExpect(jsonPath("$.state").value("Delhi"))
                .andExpect(jsonPath("$.pinCode").value("110020"))
                .andExpect(jsonPath("$.clientSource").value("Direct Reference"))
                .andExpect(jsonPath("$.sourceDescription").value("Referred by Dr. Verma"))
                .andExpect(jsonPath("$.expectedMaxSIP").value(50000.00))
                .andExpect(jsonPath("$.firstInvestmentAmount").value(100000.00))
                .andExpect(jsonPath("$.investwellId").value("IW-987654"))
                .andExpect(jsonPath("$.bestTimeToMeet").value("1st half of the day"))
                .andExpect(jsonPath("$.assignmentDate").value(assignDate.toString()))
                .andExpect(jsonPath("$.remarks").value("High net worth individual"));

        Client reloaded = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. Updated Full Name");
        assertThat(reloaded.getSpeciality()).isEqualTo("Cardiology");
        assertThat(reloaded.getLocation()).isEqualTo("South Delhi");
        assertThat(reloaded.getClinicAddress()).isEqualTo("Fortis Hospital, Suite 402");
        assertThat(reloaded.getAlternateMobileNumber()).isEqualTo(altMobile);
        assertThat(reloaded.getDateOfBirth()).isEqualTo(dob);
        assertThat(reloaded.getActualDateOfBirth()).isEqualTo(actualDob);
        assertThat(reloaded.getOccupation()).isEqualTo("Consultant Doctor");
        assertThat(reloaded.getMotherName()).isEqualTo("Sunita Sharma");
        assertThat(reloaded.getPlaceOfBirth()).isEqualTo("New Delhi");
        assertThat(reloaded.getResidentialAddress()).isEqualTo("42 Vasant Vihar, New Delhi");
        assertThat(reloaded.getCorrespondenceAddress()).isEqualTo("42 Vasant Vihar, New Delhi");
        assertThat(reloaded.getOfficeAddress()).isEqualTo("Fortis Hospital, Okhla");
        assertThat(reloaded.getCity()).isEqualTo("New Delhi");
        assertThat(reloaded.getState()).isEqualTo("Delhi");
        assertThat(reloaded.getPinCode()).isEqualTo("110020");
        assertThat(reloaded.getClientSource()).isEqualTo("Direct Reference");
        assertThat(reloaded.getSourceDescription()).isEqualTo("Referred by Dr. Verma");
        assertThat(reloaded.getExpectedMaxSIP()).isEqualByComparingTo("50000.00");
        assertThat(reloaded.getFirstInvestmentAmount()).isEqualByComparingTo("100000.00");
        assertThat(reloaded.getInvestwellId()).isEqualTo("IW-987654");
        assertThat(reloaded.getBestTimeToMeet()).isEqualTo("1st half of the day");
        assertThat(reloaded.getAssignmentDate()).isEqualTo(assignDate);
        assertThat(reloaded.getRemarks()).isEqualTo("High net worth individual");
    }

    @Test
    @DisplayName("2. Updating with omitted/null fields preserves existing database values")
    void testUpdateClientPreservesOmittedNullFields() throws Exception {
        String mobile = randomMobile();
        Client client = createBaseClient("Dr. Preserved Fields", mobile);
        client.setSpeciality("Neurology");
        client.setLocation("Connaught Place");
        client.setCity("New Delhi");
        client.setInvestwellId("IW-ORIGINAL-99");
        client = clientRepository.save(client);

        UpdateClientRequest request = UpdateClientRequest.builder()
                .remarks("Updated remarks only, other fields omitted")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remarks").value("Updated remarks only, other fields omitted"))
                .andExpect(jsonPath("$.speciality").value("Neurology"))
                .andExpect(jsonPath("$.location").value("Connaught Place"))
                .andExpect(jsonPath("$.city").value("New Delhi"))
                .andExpect(jsonPath("$.investwellId").value("IW-ORIGINAL-99"));

        Client reloaded = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloaded.getRemarks()).isEqualTo("Updated remarks only, other fields omitted");
        assertThat(reloaded.getSpeciality()).isEqualTo("Neurology");
        assertThat(reloaded.getLocation()).isEqualTo("Connaught Place");
        assertThat(reloaded.getCity()).isEqualTo("New Delhi");
        assertThat(reloaded.getInvestwellId()).isEqualTo("IW-ORIGINAL-99");
    }

    @Test
    @DisplayName("3. Updating with blank/whitespace strings does not erase existing database values")
    void testUpdateClientEmptyStringDoesNotEraseExistingData() throws Exception {
        String mobile = randomMobile();
        Client client = createBaseClient("Dr. Safe Sanitization", mobile);
        client.setOccupation("Pediatrician");
        client.setMotherName("Anita Roy");
        client.setInvestwellId("IW-PRESERVE-77");
        client.setClinicAddress("Apollo Clinic");
        client = clientRepository.save(client);

        UpdateClientRequest request = UpdateClientRequest.builder()
                .occupation("")
                .motherName("   ")
                .investwellId("   ")
                .clinicAddress("")
                .remarks("Safe sanitization test")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupation").value("Pediatrician"))
                .andExpect(jsonPath("$.motherName").value("Anita Roy"))
                .andExpect(jsonPath("$.investwellId").value("IW-PRESERVE-77"))
                .andExpect(jsonPath("$.clinicAddress").value("Apollo Clinic"))
                .andExpect(jsonPath("$.remarks").value("Safe sanitization test"));

        Client reloaded = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloaded.getOccupation()).isEqualTo("Pediatrician");
        assertThat(reloaded.getMotherName()).isEqualTo("Anita Roy");
        assertThat(reloaded.getInvestwellId()).isEqualTo("IW-PRESERVE-77");
        assertThat(reloaded.getClinicAddress()).isEqualTo("Apollo Clinic");
        assertThat(reloaded.getRemarks()).isEqualTo("Safe sanitization test");
    }

    @Test
    @DisplayName("4. Reject future dates for date of birth")
    void testUpdateClientRejectsFutureDateOfBirth() throws Exception {
        String mobile = randomMobile();
        Client client = createBaseClient("Dr. Future DOB", mobile);

        UpdateClientRequest requestFutureDob = UpdateClientRequest.builder()
                .dateOfBirth(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestFutureDob)))
                .andExpect(status().isBadRequest());

        UpdateClientRequest requestFutureActualDob = UpdateClientRequest.builder()
                .actualDateOfBirth(LocalDate.now().plusDays(2))
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestFutureActualDob)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("5. Reject negative numeric investment amounts")
    void testUpdateClientRejectsNegativeAmounts() throws Exception {
        String mobile = randomMobile();
        Client client = createBaseClient("Dr. Negative Amounts", mobile);

        UpdateClientRequest requestNegativeSip = UpdateClientRequest.builder()
                .expectedMaxSIP(new BigDecimal("-500.00"))
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestNegativeSip)))
                .andExpect(status().isBadRequest());

        UpdateClientRequest requestNegativeInvestment = UpdateClientRequest.builder()
                .firstInvestmentAmount(new BigDecimal("-1000.00"))
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestNegativeInvestment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6. Reject invalid bestTimeToMeet slot")
    void testUpdateClientRejectsInvalidBestTimeToMeet() throws Exception {
        String mobile = randomMobile();
        Client client = createBaseClient("Dr. Invalid Time Slot", mobile);

        UpdateClientRequest request = UpdateClientRequest.builder()
                .bestTimeToMeet("Morning Around 10 AM")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7. Normalize bestTimeToMeet aliases during update")
    void testUpdateClientNormalizesBestTimeToMeetAliases() throws Exception {
        String mobile = randomMobile();
        Client client = createBaseClient("Dr. Normalized Slot", mobile);

        UpdateClientRequest request2ndHalf = UpdateClientRequest.builder()
                .bestTimeToMeet("2nd half")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2ndHalf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bestTimeToMeet").value("2nd half of the day"));

        Client reloaded = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloaded.getBestTimeToMeet()).isEqualTo("2nd half of the day");

        UpdateClientRequest requestWeekend = UpdateClientRequest.builder()
                .bestTimeToMeet("Weekend")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWeekend)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bestTimeToMeet").value("Only on weekend"));

        reloaded = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloaded.getBestTimeToMeet()).isEqualTo("Only on weekend");
    }

    @Test
    @DisplayName("8. Validate PAN and PIN code formats")
    void testUpdateClientValidatesPanAndPinCode() throws Exception {
        String mobile = randomMobile();
        Client client = createBaseClient("Dr. Format Validation", mobile);

        UpdateClientRequest invalidPan = UpdateClientRequest.builder()
                .panNumber("INVALIDPAN")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPan)))
                .andExpect(status().isBadRequest());

        UpdateClientRequest invalidPin = UpdateClientRequest.builder()
                .pinCode("123")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPin)))
                .andExpect(status().isBadRequest());

        UpdateClientRequest validRequest = UpdateClientRequest.builder()
                .panNumber("abcde1234f")
                .pinCode("110001")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.panNumber").value("ABCDE1234F"))
                .andExpect(jsonPath("$.pinCode").value("110001"));

        Client reloaded = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloaded.getPanNumber()).isEqualTo("ABCDE1234F");
        assertThat(reloaded.getPinCode()).isEqualTo("110001");
    }
}

