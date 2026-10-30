package com.blueant_crm_erp.client.bulk;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import com.blueant_crm_erp.client.bulk.repository.ClientImportJobRepository;
import com.blueant_crm_erp.client.bulk.service.impl.ImportJobRecoveryCoordinator;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientAssignmentHistory;
import com.blueant_crm_erp.client.repository.ClientAssignmentHistoryRepository;
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
import com.blueant_crm_erp.util.id.ClientCodeGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Bulk Client Import End-to-End Integration Tests")
class BulkClientImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientAssignmentHistoryRepository assignmentHistoryRepository;

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

    @Autowired
    private ImportJobRecoveryCoordinator recoveryCoordinator;

    private User crmUser;
    private User rmUser;
    private User smUser;
    private UserDetails crmPrincipal;
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

        crmUser = createTestUser("CRM_BULK", BootstrapConstants.ROLE_CRM_ONBOARDING,
                BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, "PHYSICAL_LEAD_ASSIGN", "client:read");

        rmUser = createTestUser("RM_BULK", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER,
                BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");

        smUser = createTestUser("SM_BULK", BootstrapConstants.ROLE_SALES_MANAGER,
                BootstrapConstants.DESIG_SM, salesDept, "MEETING_UPDATE");

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

    private String randomMobile() {
        return "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
    }

    @Test
    @DisplayName("Successfully imports external clients, creates assignment history, and skips lead creation")
    void testBulkImportSuccess() throws Exception {
        String mobile1 = randomMobile();
        String mobile2 = randomMobile();

        String csvData = "Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code\n"
                + "Dr. Test One," + mobile1 + ",Neurology,Delhi,Apollo Hospital," + rmUser.getEmployeeCode() + "\n"
                + "Dr. Test Two," + mobile2 + ",Cardiology,Noida,Max Hospital," + rmUser.getEmployeeCode() + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "valid_clients.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult submitResult = mockMvc.perform(multipart("/v1/clients/bulk-import")
                        .file(file)
                        .with(user(crmPrincipal)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").isNotEmpty())
                .andReturn();

        JsonNode root = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        String jobId = root.path("data").path("jobId").asText();

        // Poll job status until COMPLETED
        String finalStatus = pollJobStatus(jobId);
        assertThat(finalStatus).isEqualTo("COMPLETED");

        // Verify clients in database
        Client client1 = clientRepository.findByMobileNumber(mobile1).orElse(null);
        assertThat(client1).isNotNull();
        assertThat(client1.getClientName()).isEqualTo("Dr. Test One");
        assertThat(client1.getLead()).isNull(); // Crucial: external client must not have lead
        assertThat(client1.getClientCode()).isNotEmpty();
        assertThat(client1.getSalesPerson().getId()).isEqualTo(rmUser.getId());

        Client client2 = clientRepository.findByMobileNumber(mobile2).orElse(null);
        assertThat(client2).isNotNull();
        assertThat(client2.getClientName()).isEqualTo("Dr. Test Two");
        assertThat(client2.getLead()).isNull();
        assertThat(client2.getClientCode()).isNotEmpty();

        // Verify contiguous client codes
        long codeNum1 = ClientCodeGenerator.extractSequence(client1.getClientCode());
        long codeNum2 = ClientCodeGenerator.extractSequence(client2.getClientCode());
        assertThat(codeNum2).isEqualTo(codeNum1 + 1);

        // Verify assignment history exists
        List<ClientAssignmentHistory> history1 = assignmentHistoryRepository.findByClientIdOrderByAssignedAtDesc(client1.getId());
        assertThat(history1).isNotEmpty();
        assertThat(history1.get(0).getAssignedToUser().getId()).isEqualTo(rmUser.getId());
    }

    @Test
    @DisplayName("All-or-Nothing: Any validation error rejects the entire import batch with zero DB writes")
    void testAllOrNothingValidationFailure() throws Exception {
        String validMobile = randomMobile();
        long initialClientCount = clientRepository.count();

        // Row 1 is valid, Row 2 has invalid mobile format ("12345")
        String csvData = "Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code\n"
                + "Dr. Valid," + validMobile + ",Neurology,Delhi,Apollo Hospital," + rmUser.getEmployeeCode() + "\n"
                + "Dr. Invalid,12345,Cardiology,Noida,Max Hospital," + rmUser.getEmployeeCode() + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid_batch.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult submitResult = mockMvc.perform(multipart("/v1/clients/bulk-import")
                        .file(file)
                        .with(user(crmPrincipal)))
                .andExpect(status().isAccepted())
                .andReturn();

        JsonNode root = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        String jobId = root.path("data").path("jobId").asText();

        String finalStatus = pollJobStatus(jobId);
        assertThat(finalStatus).isEqualTo("FAILED");

        // Verify zero clients were created from this batch
        assertThat(clientRepository.count()).isEqualTo(initialClientCount);
        assertThat(clientRepository.findByMobileNumber(validMobile)).isEmpty();
    }

    @Test
    @DisplayName("Rejects assignment to ineligible sales role (e.g. Sales Manager SM)")
    void testRejectsIneligibleSalesRole() throws Exception {
        String mobile = randomMobile();

        String csvData = "Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code\n"
                + "Dr. Ineligible," + mobile + ",Neurology,Delhi,Apollo Hospital," + smUser.getEmployeeCode() + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sm_assigned.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult submitResult = mockMvc.perform(multipart("/v1/clients/bulk-import")
                        .file(file)
                        .with(user(crmPrincipal)))
                .andExpect(status().isAccepted())
                .andReturn();

        JsonNode root = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        String jobId = root.path("data").path("jobId").asText();

        String finalStatus = pollJobStatus(jobId);
        assertThat(finalStatus).isEqualTo("FAILED");

        // Verify error details mention ineligible role
        ClientImportJob job = jobRepository.findById(jobId).orElseThrow();
        assertThat(job.getErrorDetailsJson()).contains("SALES_PERSON_NOT_ELIGIBLE");
        assertThat(clientRepository.findByMobileNumber(mobile)).isEmpty();
    }

    @Test
    @DisplayName("Rejects mobile numbers that already exist in database")
    void testRejectsExistingDatabaseMobile() throws Exception {
        String existingMobile = randomMobile();

        // Create an existing client in database with unique client code
        clientRepository.save(Client.builder()
                .clientCode("TEST_DUP_" + UUID.randomUUID().toString().substring(0, 8))
                .clientName("Existing Doctor")
                .mobileNumber(existingMobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Clinic 1")
                .clientStatus(com.blueant_crm_erp.client.enums.ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        String newMobile = randomMobile();
        String csvData = "Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code\n"
                + "Dr. First," + newMobile + ",Neurology,Delhi,Clinic A," + rmUser.getEmployeeCode() + "\n"
                + "Dr. Duplicate," + existingMobile + ",Cardiology,Noida,Clinic B," + rmUser.getEmployeeCode() + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "db_duplicate.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult submitResult = mockMvc.perform(multipart("/v1/clients/bulk-import")
                        .file(file)
                        .with(user(crmPrincipal)))
                .andExpect(status().isAccepted())
                .andReturn();

        JsonNode root = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        String jobId = root.path("data").path("jobId").asText();

        String finalStatus = pollJobStatus(jobId);
        assertThat(finalStatus).isEqualTo("FAILED");

        ClientImportJob job = jobRepository.findById(jobId).orElseThrow();
        assertThat(job.getErrorDetailsJson()).contains("DUPLICATE_MOBILE_IN_DATABASE");
        assertThat(clientRepository.findByMobileNumber(newMobile)).isEmpty();
    }

    @Test
    @DisplayName("Rejects file with in-file duplicate mobile numbers")
    void testRejectsInFileDuplicateMobile() throws Exception {
        String duplicateMobile = randomMobile();

        String csvData = "Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code\n"
                + "Dr. Alice," + duplicateMobile + ",Neurology,Delhi,Clinic A," + rmUser.getEmployeeCode() + "\n"
                + "Dr. Bob," + duplicateMobile + ",Cardiology,Noida,Clinic B," + rmUser.getEmployeeCode() + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file_duplicate.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult submitResult = mockMvc.perform(multipart("/v1/clients/bulk-import")
                        .file(file)
                        .with(user(crmPrincipal)))
                .andExpect(status().isAccepted())
                .andReturn();

        JsonNode root = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        String jobId = root.path("data").path("jobId").asText();

        String finalStatus = pollJobStatus(jobId);
        assertThat(finalStatus).isEqualTo("FAILED");

        ClientImportJob job = jobRepository.findById(jobId).orElseThrow();
        assertThat(job.getErrorDetailsJson()).contains("DUPLICATE_MOBILE_IN_FILE");
    }

    @Test
    @DisplayName("Downloads Excel template with correct media type and header")
    void testDownloadTemplate() throws Exception {
        mockMvc.perform(get("/v1/clients/bulk-import/template")
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentType())
                        .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(result -> assertThat(result.getResponse().getHeader("Content-Disposition"))
                        .contains("bulk_client_import_template.xlsx"));
    }

    @Test
    @DisplayName("ImportJobRecoveryCoordinator detects and fails stale jobs")
    void testRecoveryCoordinatorRecoversStaleJob() {
        String staleJobId = UUID.randomUUID().toString();
        ClientImportJob staleJob = ClientImportJob.builder()
                .id(staleJobId)
                .fileName("crashed_import.xlsx")
                .fileHashSha256("dummyhash123")
                .status("EXECUTING")
                .totalRows(500)
                .processedRows(100)
                .createdBy(crmUser.getEmployeeCode())
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .startedAt(LocalDateTime.now().minusMinutes(18))
                .heartbeatAt(LocalDateTime.now().minusMinutes(15)) // Heartbeat 15 minutes ago (older than 10-min threshold)
                .build();
        jobRepository.save(staleJob);

        recoveryCoordinator.recoverStaleJobs();

        ClientImportJob recoveredJob = jobRepository.findById(staleJobId).orElseThrow();
        assertThat(recoveredJob.getStatus()).isEqualTo("FAILED");
        assertThat(recoveredJob.getErrorMessage()).contains("timed out or server restarted");
        assertThat(recoveredJob.getCompletedAt()).isNotNull();
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
}
