package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import com.blueant_crm_erp.client.bulk.repository.ClientImportJobRepository;
import com.blueant_crm_erp.client.dto.request.ClientNomineeRequest;
import com.blueant_crm_erp.client.dto.request.CreateFamilyRequest;
import com.blueant_crm_erp.client.dto.request.UpdateClientRequest;
import com.blueant_crm_erp.client.dto.response.FamilyDetailResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientFamily;
import com.blueant_crm_erp.client.entity.ClientFamilyMember;
import com.blueant_crm_erp.client.enums.ClientPaymentStatus;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.enums.FamilyMembershipStatus;
import com.blueant_crm_erp.client.repository.ClientFamilyMemberRepository;
import com.blueant_crm_erp.client.repository.ClientFamilyRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.FamilyService;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Client Remaining 37 Questions Editability Integration Tests for Bulk-Uploaded Clients")
class ClientRemainingQuestionsEditabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientFamilyRepository clientFamilyRepository;

    @Autowired
    private ClientFamilyMemberRepository clientFamilyMemberRepository;

    @Autowired
    private FamilyService familyService;

    @Autowired
    private com.blueant_crm_erp.client.repository.ClientNomineeRepository clientNomineeRepository;

    @Autowired
    private ClientImportJobRepository jobRepository;

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
    private User adminUser;
    private User superAdminUser;
    private User rmUser;
    private User scUser;

    private UserDetails crmPrincipal;
    private UserDetails adminPrincipal;
    private UserDetails superAdminPrincipal;
    private UserDetails rmPrincipal;
    private UserDetails scPrincipal;

    private Team defaultTeam;

    @BeforeEach
    void setUp() {
        Department salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        Department crmDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(salesDept));

        defaultTeam = teamRepository.findAll().stream().findFirst()
                .orElseGet(() -> teamRepository.save(Team.builder()
                        .teamName("Test Sales Team " + UUID.randomUUID().toString().substring(0, 6))
                        .teamCode("TEAM_TEST_" + UUID.randomUUID().toString().substring(0, 6))
                        .department(salesDept)
                        .status(Status.ACTIVE)
                        .build()));

        crmUser = createTestUser("CRM_37", BootstrapConstants.ROLE_CRM_ONBOARDING,
                BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, "PHYSICAL_LEAD_ASSIGN", "client:read", "client:update");

        adminUser = createTestUser("ADMIN_37", BootstrapConstants.ROLE_ADMIN,
                "ADMIN_DESIG", crmDept, "client:read", "client:update");

        superAdminUser = createTestUser("SUPER_37", BootstrapConstants.ROLE_SUPER_ADMIN,
                "SUPER_ADMIN_DESIG", crmDept, "client:read", "client:update");

        rmUser = createTestUser("RM_37", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER,
                BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");

        scUser = createTestUser("SC_37", BootstrapConstants.ROLE_SALES_COORDINATOR,
                BootstrapConstants.DESIG_SC, salesDept, "MEETING_READ");

        crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
        adminPrincipal = customUserDetailsService.loadUserByUsername(adminUser.getEmployeeCode());
        superAdminPrincipal = customUserDetailsService.loadUserByUsername(superAdminUser.getEmployeeCode());
        rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
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

    private Client uploadBulkClient(String clientName, String mobile) throws Exception {
        String csvData = "Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code\n"
                + clientName + "," + mobile + ",Cardiology,Delhi,Fortis Clinic," + rmUser.getEmployeeCode() + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bulk_clients.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult submitResult = mockMvc.perform(multipart("/v1/clients/bulk-import")
                        .file(file)
                        .with(user(crmPrincipal)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode root = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        String jobId = root.path("data").path("jobId").asText();

        String finalStatus = pollJobStatus(jobId);
        assertThat(finalStatus).isEqualTo("COMPLETED");

        return clientRepository.findByMobileNumber(mobile).orElseThrow();
    }

    private String pollJobStatus(String jobId) throws Exception {
        for (int i = 0; i < 60; i++) {
            Thread.sleep(300);
            MvcResult result = mockMvc.perform(get("/v1/clients/bulk-import/jobs/" + jobId)
                            .with(user(crmPrincipal)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            String status = root.path("data").path("status").asText();
            if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                return status;
            }
        }
        return "TIMEOUT";
    }

    @Test
    @DisplayName("1. Bulk upload accepts only 26 questions, ignoring extra columns")
    void test1_bulkUploadAcceptsOnly26Questions_AndIgnoresExtraColumns() throws Exception {
        String mobile = randomMobile();
        String csvData = "Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code,ExtraCol1,ExtraCol2\n"
                + "Dr. Extra Column Client," + mobile + ",Pediatrics,Mumbai,Lilavati Hospital," + rmUser.getEmployeeCode() + ",IgnoredVal1,IgnoredVal2\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "extra_columns.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult submitResult = mockMvc.perform(multipart("/v1/clients/bulk-import")
                        .file(file)
                        .with(user(crmPrincipal)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode root = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        String jobId = root.path("data").path("jobId").asText();

        String finalStatus = pollJobStatus(jobId);
        assertThat(finalStatus).isEqualTo("COMPLETED");

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getClientName()).isEqualTo("Dr. Extra Column Client");
        assertThat(client.getSalesPerson().getId()).isEqualTo(rmUser.getId());
    }

    @Test
    @DisplayName("2. Bulk uploaded client initially has only 26 questions populated, remaining 37 fields are null or empty")
    void test2_bulkUploadedClient_InitiallyHasRemaining37QuestionsNullOrEmpty() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. Initially Sparse", mobile);

        // Verify bulk fields populated
        assertThat(client.getClientName()).isEqualTo("Dr. Initially Sparse");
        assertThat(client.getMobileNumber()).isEqualTo(mobile);
        assertThat(client.getSpeciality()).isEqualTo("Cardiology");
        assertThat(client.getLocation()).isEqualTo("Delhi");
        assertThat(client.getClinicAddress()).isEqualTo("Fortis Clinic");
        assertThat(client.getSalesPerson()).isNotNull();
        assertThat(client.getLead()).isNull();

        // Verify remaining fields are null or empty initially
        assertThat(client.getPanNumber()).isNull();
        assertThat(client.getDateOfBirth()).isNull();
        assertThat(client.getActualDateOfBirth()).isNull();
        assertThat(client.getResidentialAddress()).isNull();
        assertThat(client.getCorrespondenceAddress()).isNull();
        assertThat(client.getOfficeAddress()).isNull();
        assertThat(client.getCity()).isNull();
        assertThat(client.getState()).isNull();
        assertThat(client.getPinCode()).isNull();
        assertThat(client.getMotherName()).isNull();
        assertThat(client.getPlaceOfBirth()).isNull();
        assertThat(client.getOccupation()).isNull();
        assertThat(client.getFamilyHeadName()).isNull();
        assertThat(client.getFamilyDetails()).isNull();
        assertThat(client.getApplicationReceivedDate()).isNull();
        assertThat(client.getApplicationMode()).isNull();
        assertThat(client.getInvestmentType()).isNull();
        assertThat(client.getFirstInvestmentAmount()).isNull();
        assertThat(client.getExpectedMaxSIP()).isNull();
        assertThat(client.getInvestwellId()).isNull();
        assertThat(client.getClientReportDate()).isNull();
        assertThat(client.getPaymentStatus()).isNull();
        assertThat(client.getPaymentAmount()).isNull();
        assertThat(client.getPaymentDate()).isNull();
        assertThat(client.getPaymentReference()).isNull();
        assertThat(clientNomineeRepository.findByClientId(client.getId())).isEmpty();
        assertThat(client.getFamily()).isNull();
    }

    @Test
    @DisplayName("3. CRM user can edit remaining 37 questions via PUT /v1/clients/{id} (200 OK)")
    void test3_crmCanEditRemaining37Questions_Success200() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. CRM Edit Target", mobile);

        LocalDate dob = LocalDate.of(1982, 3, 15);
        LocalDate appRecDate = LocalDate.of(2025, 1, 10);
        LocalDate reportDate = LocalDate.of(2025, 2, 1);
        LocalDate payDate = LocalDate.of(2025, 1, 12);

        ClientNomineeRequest nominee = ClientNomineeRequest.builder()
                .nomineeName("Anita Sharma")
                .relationship("Spouse")
                .dateOfBirth(LocalDate.of(1985, 7, 20))
                .contactNumber("9811122233")
                .panNumber("ABCDE1234F")
                .isMinor(false)
                .build();

        UpdateClientRequest request = UpdateClientRequest.builder()
                .panNumber("ABCDE1234F")
                .dateOfBirth(dob)
                .actualDateOfBirth(dob)
                .residentialAddress("Flat 101, Palm Grove Apartments")
                .correspondenceAddress("Flat 101, Palm Grove Apartments")
                .officeAddress("Fortis Clinic, Suite 2")
                .city("New Delhi")
                .state("Delhi")
                .pinCode("110001")
                .occupation("Senior Surgeon")
                .motherName("Kalyani Sharma")
                .placeOfBirth("New Delhi")
                .familyHeadName("Rajesh Sharma")
                .familyDetails("Joint family with 4 members")
                .applicationReceivedDate(appRecDate)
                .applicationMode("PHYSICAL")
                .investmentTypes(List.of("EQUITY", "MUTUAL_FUNDS"))
                .firstInvestmentAmount(new BigDecimal("100000.00"))
                .expectedMaxSIP(new BigDecimal("25000.00"))
                .investwellId("INVW-9901")
                .clientReportDate(reportDate)
                .paymentStatus(ClientPaymentStatus.YES)
                .paymentAmount(new BigDecimal("50000.00"))
                .paymentDate(payDate)
                .paymentReference("PAY-REF-998811")
                .nominees(Collections.singletonList(nominee))
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.panNumber").value("ABCDE1234F"))
                .andExpect(jsonPath("$.familyHeadName").value("Rajesh Sharma"))
                .andExpect(jsonPath("$.paymentStatus").value("YES"))
                .andExpect(jsonPath("$.paymentAmount").value(50000.00))
                .andExpect(jsonPath("$.nominees[0].nomineeName").value("Anita Sharma"));

        // Verify DB entity
        Client updated = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(updated.getPanNumber()).isEqualTo("ABCDE1234F");
        assertThat(updated.getFamilyHeadName()).isEqualTo("Rajesh Sharma");
        assertThat(updated.getFamilyDetails()).isEqualTo("Joint family with 4 members");
        assertThat(updated.getApplicationMode()).isEqualTo("PHYSICAL");
        assertThat(updated.getPaymentStatus()).isEqualTo(ClientPaymentStatus.YES);
        assertThat(updated.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(updated.getPaymentReference()).isEqualTo("PAY-REF-998811");
        assertThat(updated.getInvestmentType()).contains("EQUITY");
        assertThat(updated.getFirstInvestmentAmount()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(updated.getExpectedMaxSIP()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(updated.getInvestwellId()).isEqualTo("INVW-9901");
        List<com.blueant_crm_erp.client.entity.ClientNominee> savedNominees = clientNomineeRepository.findByClientId(client.getId());
        assertThat(savedNominees).hasSize(1);
        assertThat(savedNominees.get(0).getNomineeName()).isEqualTo("Anita Sharma");

        // Verify original 26 bulk upload fields were preserved!
        assertThat(updated.getClientName()).isEqualTo("Dr. CRM Edit Target");
        assertThat(updated.getMobileNumber()).isEqualTo(mobile);
        assertThat(updated.getSpeciality()).isEqualTo("Cardiology");
        assertThat(updated.getLocation()).isEqualTo("Delhi");
        assertThat(updated.getClinicAddress()).isEqualTo("Fortis Clinic");
        assertThat(updated.getSalesPerson().getId()).isEqualTo(rmUser.getId());
    }

    @Test
    @DisplayName("4. Admin user can edit remaining 37 questions via PUT /v1/clients/{id} (200 OK)")
    void test4_adminCanEditRemaining37Questions_Success200() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. Admin Edit Target", mobile);

        UpdateClientRequest request = UpdateClientRequest.builder()
                .panNumber("BCDEF2345G")
                .occupation("Chief Medical Officer")
                .paymentStatus(ClientPaymentStatus.YES)
                .paymentAmount(new BigDecimal("75000.00"))
                .paymentReference("ADMIN-TX-1002")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.panNumber").value("BCDEF2345G"))
                .andExpect(jsonPath("$.occupation").value("Chief Medical Officer"))
                .andExpect(jsonPath("$.paymentAmount").value(75000.00));

        Client updated = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(updated.getPanNumber()).isEqualTo("BCDEF2345G");
        assertThat(updated.getOccupation()).isEqualTo("Chief Medical Officer");
        assertThat(updated.getClientName()).isEqualTo("Dr. Admin Edit Target");
    }

    @Test
    @DisplayName("5. Super Admin user can edit remaining 37 questions via PUT /v1/clients/{id} (200 OK)")
    void test5_superAdminCanEditRemaining37Questions_Success200() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. SuperAdmin Edit Target", mobile);

        UpdateClientRequest request = UpdateClientRequest.builder()
                .panNumber("CDEFG3456H")
                .applicationMode("ONLINE_PORTAL")
                .investmentTypes(List.of("FIXED_DEPOSITS", "BONDS"))
                .paymentStatus(ClientPaymentStatus.PENDING)
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(superAdminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.panNumber").value("CDEFG3456H"))
                .andExpect(jsonPath("$.applicationMode").value("ONLINE_PORTAL"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"));

        Client updated = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(updated.getPanNumber()).isEqualTo("CDEFG3456H");
        assertThat(updated.getApplicationMode()).isEqualTo("ONLINE_PORTAL");
        assertThat(updated.getPaymentStatus()).isEqualTo(ClientPaymentStatus.PENDING);
        assertThat(updated.getClientName()).isEqualTo("Dr. SuperAdmin Edit Target");
    }

    @Test
    @DisplayName("6. Non-permitted roles (RM, SC) CANNOT edit remaining 37 questions (403 Forbidden)")
    void test6_nonPermittedRoles_RM_And_SC_CannotEdit_Returns403Forbidden() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. Forbidden Target", mobile);

        UpdateClientRequest request = UpdateClientRequest.builder()
                .panNumber("ZZZZZ9999Z")
                .paymentAmount(new BigDecimal("100000.00"))
                .build();

        // RM attempted edit -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(rmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // SC attempted edit -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(scPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // DB record unchanged
        Client unchanged = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(unchanged.getPanNumber()).isNull();
        assertThat(unchanged.getPaymentAmount()).isNull();
    }

    @Test
    @DisplayName("7. Original 26 bulk upload values are preserved across edits")
    void test7_original26BulkUploadValuesPreservedAcrossEdits() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. Preservation Test", mobile);

        UpdateClientRequest request = UpdateClientRequest.builder()
                .motherName("Savitri Devi")
                .city("Chandigarh")
                .state("Punjab")
                .pinCode("160017")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk());

        Client reloaded = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. Preservation Test");
        assertThat(reloaded.getMobileNumber()).isEqualTo(mobile);
        assertThat(reloaded.getSpeciality()).isEqualTo("Cardiology");
        assertThat(reloaded.getLocation()).isEqualTo("Delhi");
        assertThat(reloaded.getClinicAddress()).isEqualTo("Fortis Clinic");
        assertThat(reloaded.getClientStatus()).isEqualTo(ClientStatus.ACTIVE);
        assertThat(reloaded.getSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(reloaded.getMotherName()).isEqualTo("Savitri Devi");
        assertThat(reloaded.getCity()).isEqualTo("Chandigarh");
    }

    @Test
    @DisplayName("8. Multiple updates to remaining questions work idempotently, modify same client, create no duplicates")
    void test8_multipleUpdatesIdempotent_ModifiesSameClient_NoDuplicates() throws Exception {
        String mobile = randomMobile();
        long initialCount = clientRepository.count();

        Client client = uploadBulkClient("Dr. Idempotent Test", mobile);
        assertThat(clientRepository.count()).isEqualTo(initialCount + 1);

        // Update 1: CRM updates personal details
        UpdateClientRequest update1 = UpdateClientRequest.builder()
                .panNumber("ABCDE5555K")
                .residentialAddress("Street 4, Sector 15")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update1))
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk());

        // Update 2: Admin updates payment and family details
        UpdateClientRequest update2 = UpdateClientRequest.builder()
                .familyHeadName("Head of Family Name")
                .paymentStatus(ClientPaymentStatus.YES)
                .paymentAmount(new BigDecimal("35000.00"))
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update2))
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk());

        // Assert record count remains initialCount + 1 (exactly one record, no duplicate)
        assertThat(clientRepository.count()).isEqualTo(initialCount + 1);

        // Assert both updates and original bulk data coexist on the exact same client entity
        Client finalClient = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(finalClient.getClientName()).isEqualTo("Dr. Idempotent Test");
        assertThat(finalClient.getPanNumber()).isEqualTo("ABCDE5555K"); // from update 1
        assertThat(finalClient.getResidentialAddress()).isEqualTo("Street 4, Sector 15"); // from update 1
        assertThat(finalClient.getFamilyHeadName()).isEqualTo("Head of Family Name"); // from update 2
        assertThat(finalClient.getPaymentStatus()).isEqualTo(ClientPaymentStatus.YES); // from update 2
        assertThat(finalClient.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("35000.00")); // from update 2
    }

    @Test
    @DisplayName("9. CRM cannot modify core identity fields (clientName, speciality, mobileNumber) on existing client")
    void test9_crmCannotModifyCoreIdentityFields_Returns403Forbidden() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. Tamper Test", mobile);

        UpdateClientRequest tamperRequest = UpdateClientRequest.builder()
                .clientName("Dr. Attempted Changed Name")
                .panNumber("DEFGH4567J")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tamperRequest))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        Client unchanged = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(unchanged.getClientName()).isEqualTo("Dr. Tamper Test");
        assertThat(unchanged.getPanNumber()).isNull();
    }

    @Test
    @DisplayName("10. CRM cannot modify any of the other original bulk upload fields (email, remarks, AMC, scheme, assignmentDate, clientSince, RM, CRM owner)")
    void test10_crmCannotModifyOtherOriginalBulkUploadFields_Returns403Forbidden() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. Bulk Protected", mobile);

        // 1. Email tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().email("tamper@email.com").build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // 2. Alternate mobile tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().alternateMobileNumber("9876543210").build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // 3. Remarks tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().remarks("Tampered remarks").build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // 4. Best time to meet tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().bestTimeToMeet("1st half").build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // 5. AMC tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().amc("HDFC AMC").build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // 6. Scheme tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().scheme("Equity Scheme").build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // 7. Assignment Date tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().assignmentDate(LocalDate.now().plusDays(5)).build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // 8. Client Since date tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().clientSince(LocalDate.now().minusDays(5)).build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // 9. RM ID tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().relationshipManagerId(999999L).build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // 10. CRM Owner tampering -> 403 Forbidden
        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateClientRequest.builder().crmOwnerId(999999L).build()))
                        .with(user(crmPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("11. Family association: Linking bulk client to family creates ClientFamilyMember record and links client")
    void test11_familyAssociation_CreatesClientFamilyMemberRecord() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. Family Target", mobile);
        assertThat(client.getFamily()).isNull();

        FamilyDetailResponse familyResp = familyService.createFamily(
                CreateFamilyRequest.builder().familyName("Verma Family").build(),
                crmUser.getEmployeeCode()
        );
        String famCode = familyResp.getFamilyCode();

        UpdateClientRequest request = UpdateClientRequest.builder()
                .familyCode(famCode)
                .panNumber("FGHIJ5678K")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.familyCode").value(famCode))
                .andExpect(jsonPath("$.familyName").value("Verma Family"));

        Client updated = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(updated.getFamily()).isNotNull();
        ClientFamily assignedFamily = clientFamilyRepository.findById(updated.getFamily().getId()).orElseThrow();
        assertThat(assignedFamily.getFamilyCode()).isEqualTo(famCode);

        // Verify client_family_member entry was created by FamilyService
        ClientFamilyMember member = clientFamilyMemberRepository
                .findByClientIdAndMembershipStatus(client.getId(), FamilyMembershipStatus.ACTIVE)
                .orElseThrow(() -> new AssertionError("ClientFamilyMember record was not created!"));

        ClientFamily memberFamily = clientFamilyRepository.findById(member.getFamily().getId()).orElseThrow();
        assertThat(memberFamily.getFamilyCode()).isEqualTo(famCode);
        assertThat(member.getMembershipStatus()).isEqualTo(FamilyMembershipStatus.ACTIVE);
    }

    @Test
    @DisplayName("12. Family association idempotence: Repeated updates with same familyCode creates no duplicate membership")
    void test12_familyAssociationIdempotence_NoDuplicateMembershipRecords() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. Idempotent Family", mobile);

        FamilyDetailResponse familyResp = familyService.createFamily(
                CreateFamilyRequest.builder().familyName("Mehta Family").build(),
                crmUser.getEmployeeCode()
        );
        String famCode = familyResp.getFamilyCode();

        // 1. Initial association
        UpdateClientRequest req1 = UpdateClientRequest.builder()
                .familyCode(famCode)
                .occupation("Cardiologist")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1))
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk());

        // 2. Second update with same familyCode
        UpdateClientRequest req2 = UpdateClientRequest.builder()
                .familyCode(famCode)
                .occupation("Senior Cardiologist")
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2))
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk());

        // Verify exactly 1 active membership in the family for this client
        ClientFamily family = clientFamilyRepository.findByFamilyCode(famCode).orElseThrow();
        List<ClientFamilyMember> members = clientFamilyMemberRepository.findByFamilyId(family.getId());
        long clientMemberCount = members.stream()
                .filter(m -> m.getClient().getId().equals(client.getId()))
                .count();

        assertThat(clientMemberCount).isEqualTo(1);
    }

    @Test
    @DisplayName("13. Cross-family transfer validation: Cannot switch to another active family without transfer workflow (409 Conflict)")
    void test13_crossFamilyTransfer_RejectedWith409Conflict() throws Exception {
        String mobile = randomMobile();
        Client client = uploadBulkClient("Dr. Transfer Target", mobile);

        FamilyDetailResponse fam1 = familyService.createFamily(
                CreateFamilyRequest.builder().familyName("Sharma Family A").build(),
                crmUser.getEmployeeCode()
        );
        FamilyDetailResponse fam2 = familyService.createFamily(
                CreateFamilyRequest.builder().familyName("Sharma Family B").build(),
                crmUser.getEmployeeCode()
        );

        // Associate with Family A
        UpdateClientRequest req1 = UpdateClientRequest.builder()
                .familyCode(fam1.getFamilyCode())
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1))
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk());

        // Attempt cross-family switch to Family B -> 409 Conflict
        UpdateClientRequest req2 = UpdateClientRequest.builder()
                .familyCode(fam2.getFamilyCode())
                .build();

        mockMvc.perform(put("/v1/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2))
                        .with(user(crmPrincipal)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // Verify client remains in Family A
        Client unchanged = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(unchanged.getFamily()).isNotNull();
        ClientFamily remainingFamily = clientFamilyRepository.findById(unchanged.getFamily().getId()).orElseThrow();
        assertThat(remainingFamily.getFamilyCode()).isEqualTo(fam1.getFamilyCode());
    }
}
