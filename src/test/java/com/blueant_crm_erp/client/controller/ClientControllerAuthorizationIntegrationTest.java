package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.auth.jwt.JwtTokenProvider;
import com.blueant_crm_erp.auth.security.CustomUserDetails;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Client Controller Strict RBAC Read/Write Authorization Integration Tests")
class ClientControllerAuthorizationIntegrationTest {

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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Department salesDept;
    private Department crmDept;
    private Team defaultTeam;
    private Client testClient;

    private User crmOnboardingUser;
    private User rmUser;
    private User scUser;
    private User pcUser;
    private User adminUser;
    private User superAdminUser;
    private User unauthorizedEmployee;

    @BeforeEach
    void setUp() {
        salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        crmDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(salesDept));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        // 1. Authorized CRM_ONBOARDING user (READ only)
        crmOnboardingUser = createTestUser("CRM_AUTH", BootstrapConstants.ROLE_CRM_ONBOARDING,
                BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept);

        // 2. Authorized RELATIONSHIP_MANAGER user (READ only)
        rmUser = createTestUser("RM_AUTH", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER,
                BootstrapConstants.DESIG_RM, salesDept);

        // 3. Authorized SALES_COORDINATOR user (READ only)
        scUser = createTestUser("SC_AUTH", BootstrapConstants.ROLE_SALES_COORDINATOR,
                BootstrapConstants.DESIG_SC, salesDept);

        // 4. Authorized PC_COORDINATOR user (READ only)
        pcUser = createTestUser("PC_AUTH", BootstrapConstants.ROLE_PC_COORDINATOR,
                BootstrapConstants.DESIG_PC, salesDept);

        // 5. Authorized ADMIN user (READ and WRITE)
        adminUser = createTestUser("ADMIN_AUTH", BootstrapConstants.ROLE_ADMIN,
                "ADMIN_DESIG", crmDept);

        // 6. Authorized SUPER_ADMIN user (READ and WRITE)
        superAdminUser = createTestUser("SA_AUTH", BootstrapConstants.ROLE_SUPER_ADMIN,
                "SUPER_ADMIN_DESIG", crmDept);

        // 7. Unauthorized EMPLOYEE (neither READ nor WRITE)
        unauthorizedEmployee = createTestUser("UNAUTH_EMP", BootstrapConstants.ROLE_EMPLOYEE,
                "EMP_DESIG", salesDept);

        // Ensure at least one test client exists
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        testClient = clientRepository.save(Client.builder()
                .clientCode("BA-CL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .clientStatus(ClientStatus.ACTIVE)
                .clientName("Dr. RBAC Authorization Client")
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Fortis Hospital")
                .salesPerson(rmUser)
                .createdBySalesPerson(rmUser)
                .assignedBy(crmOnboardingUser)
                .assignedAt(LocalDateTime.now())
                .assignmentDate(LocalDate.now())
                .clientSince(LocalDate.now())
                .build());
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

    private String generateBearerToken(User user) {
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(user.getEmployeeCode());
        return "Bearer " + jwtTokenProvider.generateAccessToken(userDetails, UUID.randomUUID().toString());
    }

    // =========================================================================
    // SECTION 1: GET /v1/clients (Search/List Clients - Read Access)
    // =========================================================================

    @Test
    @DisplayName("GET /v1/clients: 1. CRM_ONBOARDING user receives 200 OK")
    void testSearchClients_CrmOnboarding_Returns200() throws Exception {
        String token = generateBearerToken(crmOnboardingUser);
        mockMvc.perform(get("/v1/clients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /v1/clients: 2. RELATIONSHIP_MANAGER user receives 200 OK")
    void testSearchClients_RelationshipManager_Returns200() throws Exception {
        String token = generateBearerToken(rmUser);
        mockMvc.perform(get("/v1/clients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /v1/clients: 3. SALES_COORDINATOR user receives 200 OK")
    void testSearchClients_SalesCoordinator_Returns200() throws Exception {
        String token = generateBearerToken(scUser);
        mockMvc.perform(get("/v1/clients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /v1/clients: 4. PC_COORDINATOR user receives 200 OK")
    void testSearchClients_PcCoordinator_Returns200() throws Exception {
        String token = generateBearerToken(pcUser);
        mockMvc.perform(get("/v1/clients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /v1/clients: 5. ADMIN user receives 200 OK")
    void testSearchClients_Admin_Returns200() throws Exception {
        String token = generateBearerToken(adminUser);
        mockMvc.perform(get("/v1/clients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /v1/clients: 6. SUPER_ADMIN user receives 200 OK")
    void testSearchClients_SuperAdmin_Returns200() throws Exception {
        String token = generateBearerToken(superAdminUser);
        mockMvc.perform(get("/v1/clients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /v1/clients: 7. Unauthorized EMPLOYEE receives 403 Forbidden")
    void testSearchClients_UnauthorizedEmployee_Returns403() throws Exception {
        String token = generateBearerToken(unauthorizedEmployee);
        mockMvc.perform(get("/v1/clients")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /v1/clients: 8. Missing token receives 401 Unauthorized")
    void testSearchClients_MissingToken_Returns401() throws Exception {
        mockMvc.perform(get("/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // =========================================================================
    // SECTION 2: GET /v1/clients/{id} (Get Client By ID - Read Access)
    // =========================================================================

    @Test
    @DisplayName("GET /v1/clients/{id}: 9. CRM_ONBOARDING receives 200 OK")
    void testGetClientById_CrmOnboarding_Returns200() throws Exception {
        String token = generateBearerToken(crmOnboardingUser);
        mockMvc.perform(get("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()));
    }

    @Test
    @DisplayName("GET /v1/clients/{id}: 10. RELATIONSHIP_MANAGER receives 200 OK")
    void testGetClientById_RelationshipManager_Returns200() throws Exception {
        String token = generateBearerToken(rmUser);
        mockMvc.perform(get("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()));
    }

    @Test
    @DisplayName("GET /v1/clients/{id}: 11. SALES_COORDINATOR receives 200 OK")
    void testGetClientById_SalesCoordinator_Returns200() throws Exception {
        String token = generateBearerToken(scUser);
        mockMvc.perform(get("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()));
    }

    @Test
    @DisplayName("GET /v1/clients/{id}: 12. PC_COORDINATOR receives 200 OK")
    void testGetClientById_PcCoordinator_Returns200() throws Exception {
        String token = generateBearerToken(pcUser);
        mockMvc.perform(get("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()));
    }

    @Test
    @DisplayName("GET /v1/clients/{id}: 13. ADMIN receives 200 OK")
    void testGetClientById_Admin_Returns200() throws Exception {
        String token = generateBearerToken(adminUser);
        mockMvc.perform(get("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()));
    }

    @Test
    @DisplayName("GET /v1/clients/{id}: 14. SUPER_ADMIN receives 200 OK")
    void testGetClientById_SuperAdmin_Returns200() throws Exception {
        String token = generateBearerToken(superAdminUser);
        mockMvc.perform(get("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()));
    }

    @Test
    @DisplayName("GET /v1/clients/{id}: 15. Unauthorized EMPLOYEE receives 403 Forbidden")
    void testGetClientById_UnauthorizedEmployee_Returns403() throws Exception {
        String token = generateBearerToken(unauthorizedEmployee);
        mockMvc.perform(get("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /v1/clients/{id}: 16. Missing token receives 401 Unauthorized")
    void testGetClientById_MissingToken_Returns401() throws Exception {
        mockMvc.perform(get("/v1/clients/" + testClient.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // =========================================================================
    // SECTION 3: PUT /v1/clients/{id} (Update Client - Write Access ONLY for ADMIN/SUPER_ADMIN)
    // =========================================================================

    @Test
    @DisplayName("PUT /v1/clients/{id}: 17. CRM_ONBOARDING receives 403 Forbidden and DB record unchanged")
    void testPutClient_CrmOnboarding_Returns403() throws Exception {
        String token = generateBearerToken(crmOnboardingUser);
        UpdateClientRequest request = UpdateClientRequest.builder().clientName("Unauthorized Name by CRM").build();

        mockMvc.perform(put("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        Client reloaded = clientRepository.findById(testClient.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. RBAC Authorization Client");
    }

    @Test
    @DisplayName("PUT /v1/clients/{id}: 18. RELATIONSHIP_MANAGER receives 403 Forbidden and DB record unchanged")
    void testPutClient_RelationshipManager_Returns403() throws Exception {
        String token = generateBearerToken(rmUser);
        UpdateClientRequest request = UpdateClientRequest.builder().clientName("Unauthorized Name by RM").build();

        mockMvc.perform(put("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        Client reloaded = clientRepository.findById(testClient.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. RBAC Authorization Client");
    }

    @Test
    @DisplayName("PUT /v1/clients/{id}: 19. SALES_COORDINATOR receives 403 Forbidden and DB record unchanged")
    void testPutClient_SalesCoordinator_Returns403() throws Exception {
        String token = generateBearerToken(scUser);
        UpdateClientRequest request = UpdateClientRequest.builder().clientName("Unauthorized Name by SC").build();

        mockMvc.perform(put("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        Client reloaded = clientRepository.findById(testClient.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. RBAC Authorization Client");
    }

    @Test
    @DisplayName("PUT /v1/clients/{id}: 20. PC_COORDINATOR receives 403 Forbidden and DB record unchanged")
    void testPutClient_PcCoordinator_Returns403() throws Exception {
        String token = generateBearerToken(pcUser);
        UpdateClientRequest request = UpdateClientRequest.builder().clientName("Unauthorized Name by PC").build();

        mockMvc.perform(put("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        Client reloaded = clientRepository.findById(testClient.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. RBAC Authorization Client");
    }

    @Test
    @DisplayName("PUT /v1/clients/{id}: 21. Unauthorized EMPLOYEE receives 403 Forbidden and DB record unchanged")
    void testPutClient_UnauthorizedEmployee_Returns403() throws Exception {
        String token = generateBearerToken(unauthorizedEmployee);
        UpdateClientRequest request = UpdateClientRequest.builder().clientName("Unauthorized Name by Emp").build();

        mockMvc.perform(put("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        Client reloaded = clientRepository.findById(testClient.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. RBAC Authorization Client");
    }

    @Test
    @DisplayName("PUT /v1/clients/{id}: 22. Missing token receives 401 Unauthorized")
    void testPutClient_MissingToken_Returns401() throws Exception {
        UpdateClientRequest request = UpdateClientRequest.builder().clientName("Anonymous Name").build();

        mockMvc.perform(put("/v1/clients/" + testClient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("PUT /v1/clients/{id}: 23. ADMIN user receives 200 OK and successfully updates client")
    void testPutClient_Admin_Returns200() throws Exception {
        String token = generateBearerToken(adminUser);
        UpdateClientRequest request = UpdateClientRequest.builder()
                .clientName("Dr. Legitimate Admin Update")
                .speciality("Neurology")
                .build();

        mockMvc.perform(put("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("Dr. Legitimate Admin Update"))
                .andExpect(jsonPath("$.speciality").value("Neurology"));

        Client reloaded = clientRepository.findById(testClient.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. Legitimate Admin Update");
        assertThat(reloaded.getSpeciality()).isEqualTo("Neurology");
    }

    @Test
    @DisplayName("PUT /v1/clients/{id}: 24. SUPER_ADMIN user receives 200 OK and successfully updates client")
    void testPutClient_SuperAdmin_Returns200() throws Exception {
        String token = generateBearerToken(superAdminUser);
        UpdateClientRequest request = UpdateClientRequest.builder()
                .clientName("Dr. Legitimate SuperAdmin Update")
                .speciality("Endocrinology")
                .build();

        mockMvc.perform(put("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("Dr. Legitimate SuperAdmin Update"))
                .andExpect(jsonPath("$.speciality").value("Endocrinology"));

        Client reloaded = clientRepository.findById(testClient.getId()).orElseThrow();
        assertThat(reloaded.getClientName()).isEqualTo("Dr. Legitimate SuperAdmin Update");
        assertThat(reloaded.getSpeciality()).isEqualTo("Endocrinology");
    }

    // =========================================================================
    // SECTION 4: PATCH /v1/clients/{id} (Verification that PATCH endpoint is NOT implemented)
    // =========================================================================

    @Test
    @DisplayName("PATCH /v1/clients/{id}: 25. Verifies PATCH is not implemented (Returns 405 Method Not Allowed)")
    void testPatchClient_EndpointNotImplemented_Returns405() throws Exception {
        String token = generateBearerToken(adminUser);
        UpdateClientRequest request = UpdateClientRequest.builder().clientName("Attempted Patch").build();

        mockMvc.perform(patch("/v1/clients/" + testClient.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isMethodNotAllowed());
    }
}
