package com.blueant_crm_erp.client;

import com.blueant_crm_erp.auth.security.CustomUserDetailsService;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.request.AddFamilyMemberRequest;
import com.blueant_crm_erp.client.dto.request.ChangeFamilyHeadRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.dto.request.CreateFamilyRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientFamily;
import com.blueant_crm_erp.client.entity.ClientFamilyHeadHistory;
import com.blueant_crm_erp.client.enums.ClientPaymentStatus;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.enums.FamilyRelationship;
import com.blueant_crm_erp.client.enums.FamilyStatus;
import com.blueant_crm_erp.client.repository.ClientFamilyHeadHistoryRepository;
import com.blueant_crm_erp.client.repository.ClientFamilyMemberRepository;
import com.blueant_crm_erp.client.repository.ClientFamilyRepository;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class ClientFamilyIntegrationTest {

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
    private PermissionRepository permissionRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientFamilyRepository clientFamilyRepository;

    @Autowired
    private ClientFamilyMemberRepository clientFamilyMemberRepository;

    @Autowired
    private ClientFamilyHeadHistoryRepository clientFamilyHeadHistoryRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private User crmUser;
    private User rmUser;
    private Department salesDept;
    private Department crmDept;
    private Team defaultTeam;

    @BeforeEach
    void setUp() {
        salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        crmDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(salesDept));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        crmUser = createTestUser("CRM_FAM", BootstrapConstants.ROLE_CRM_ONBOARDING, BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, "PHYSICAL_LEAD_ASSIGN");
        rmUser = createTestUser("RM_FAM", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
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
                .build());
    }

    private Client createClient(String name, ClientPaymentStatus paymentStatus, BigDecimal paymentAmount, String investmentType, BigDecimal expectedMaxSip) {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        String code = "BA-CL-2026-" + String.format("%06d", Math.abs(UUID.randomUUID().hashCode() % 900000 + 100000));
        return clientRepository.save(Client.builder()
                .clientCode(code)
                .clientName(name)
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Delhi Medical Centre")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .createdBySalesPerson(rmUser)
                .paymentStatus(paymentStatus)
                .paymentAmount(paymentAmount)
                .investmentType(investmentType)
                .expectedMaxSIP(expectedMaxSip)
                .assignmentDate(LocalDate.now())
                .clientSince(LocalDate.now())
                .build());
    }

    @Test
    @DisplayName("1. Create Family without head successfully")
    void testCreateFamilyWithoutHead_success() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        CreateFamilyRequest req = CreateFamilyRequest.builder()
                .familyName("Kapoor Family")
                .build();

        mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.familyName").value("Kapoor Family"))
                .andExpect(jsonPath("$.data.familyCode").isNotEmpty())
                .andExpect(jsonPath("$.data.familyHeadClientId").doesNotExist())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("2. Create Family with eligible head records initial head and history")
    void testCreateFamilyWithEligibleHead_success() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        Client krishan = createClient("Krishan", ClientPaymentStatus.YES, new BigDecimal("15000.00"), "MUTUAL_FUNDS", null);

        CreateFamilyRequest req = CreateFamilyRequest.builder()
                .familyName("Sharma Family")
                .initialHeadClientId(krishan.getId())
                .initialHeadRelationship(FamilyRelationship.SELF)
                .build();

        MvcResult result = mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.familyHeadClientId").value(krishan.getId()))
                .andExpect(jsonPath("$.data.members[0].clientCode").value(krishan.getClientCode()))
                .andExpect(jsonPath("$.data.members[0].isFamilyHead").value(true))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String familyCode = json.path("data").path("familyCode").asText();

        // Verify history table contains initial head designation
        ClientFamily family = clientFamilyRepository.findByFamilyCode(familyCode).orElseThrow();
        List<ClientFamilyHeadHistory> history = clientFamilyHeadHistoryRepository.findByFamilyIdOrderByChangedAtDesc(family.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getPreviousHead()).isNull();
        assertThat(history.get(0).getNewHead().getId()).isEqualTo(krishan.getId());
    }

    @Test
    @DisplayName("3. Create Family rejects ineligible head (only expectedMaxSIP, no payment)")
    void testCreateFamilyWithIneligibleHead_rejected() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        Client aarti = createClient("Aarti", ClientPaymentStatus.NO, null, null, new BigDecimal("5000.00"));

        CreateFamilyRequest req = CreateFamilyRequest.builder()
                .familyName("Aarti Family")
                .initialHeadClientId(aarti.getId())
                .build();

        mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("is ineligible")));
    }

    @Test
    @DisplayName("4. Add member to family successfully and prevent cross-family duplicate active membership")
    void testAddMemberAndCrossFamilyConflict() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        Client krishan = createClient("Krishan", ClientPaymentStatus.YES, new BigDecimal("10000.00"), "SIP", null);
        Client govind = createClient("Govind", ClientPaymentStatus.NO, null, null, null);

        // 1. Create Family 1 with Krishan as head
        CreateFamilyRequest req1 = CreateFamilyRequest.builder()
                .familyName("Family One")
                .initialHeadClientId(krishan.getId())
                .build();

        MvcResult res1 = mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andReturn();

        String famCode1 = objectMapper.readTree(res1.getResponse().getContentAsString()).path("data").path("familyCode").asText();

        // 2. Add Govind to Family 1
        AddFamilyMemberRequest addGovindReq = AddFamilyMemberRequest.builder()
                .clientId(govind.getId())
                .relationshipToHead(FamilyRelationship.SIBLING)
                .build();

        mockMvc.perform(post("/v1/client-families/" + famCode1 + "/members")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addGovindReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientCode").value(govind.getClientCode()))
                .andExpect(jsonPath("$.data.isFamilyHead").value(false));

        // 3. Create Family 2
        CreateFamilyRequest req2 = CreateFamilyRequest.builder()
                .familyName("Family Two")
                .build();

        MvcResult res2 = mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andReturn();

        String famCode2 = objectMapper.readTree(res2.getResponse().getContentAsString()).path("data").path("familyCode").asText();

        // 4. Try to add Govind to Family 2 -> Must be rejected with 409 Conflict
        mockMvc.perform(post("/v1/client-families/" + famCode2 + "/members")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addGovindReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already belongs to active family")));
    }

    @Test
    @DisplayName("5. Explicit Family Head change: Mother replaces Krishan, then Father with no investment is rejected")
    void testChangeFamilyHeadLifecycle() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Setup members
        Client krishan = createClient("Krishan", ClientPaymentStatus.YES, new BigDecimal("10000.00"), "SIP", null);
        Client mother = createClient("Mother", ClientPaymentStatus.YES, new BigDecimal("25000.00"), "MUTUAL_FUNDS", null);
        Client father = createClient("Father", ClientPaymentStatus.NO, null, null, null);

        // 1. Create family with Krishan as head
        CreateFamilyRequest createReq = CreateFamilyRequest.builder()
                .familyName("Joint Family")
                .initialHeadClientId(krishan.getId())
                .build();

        MvcResult createRes = mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String familyCode = objectMapper.readTree(createRes.getResponse().getContentAsString()).path("data").path("familyCode").asText();

        // 2. Add Mother and Father to family
        mockMvc.perform(post("/v1/client-families/" + familyCode + "/members")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddFamilyMemberRequest.builder()
                                .clientId(mother.getId())
                                .relationshipToHead(FamilyRelationship.MOTHER)
                                .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/client-families/" + familyCode + "/members")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddFamilyMemberRequest.builder()
                                .clientId(father.getId())
                                .relationshipToHead(FamilyRelationship.FATHER)
                                .build())))
                .andExpect(status().isCreated());

        // 3. Change head to Mother (eligible with confirmed payment)
        ChangeFamilyHeadRequest changeToMotherReq = ChangeFamilyHeadRequest.builder()
                .newHeadClientId(mother.getId())
                .reason("Mother designated as primary portfolio manager")
                .build();

        mockMvc.perform(put("/v1/client-families/" + familyCode + "/head")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeToMotherReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familyHeadClientId").value(mother.getId()));

        // 4. Attempt to change head to Father (ineligible, no active investment) -> MUST FAIL
        ChangeFamilyHeadRequest changeToFatherReq = ChangeFamilyHeadRequest.builder()
                .newHeadClientId(father.getId())
                .reason("Father attempt")
                .build();

        mockMvc.perform(put("/v1/client-families/" + familyCode + "/head")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeToFatherReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("does not meet active investment eligibility")));

        // 5. Verify Mother is STILL the head, and history preserved
        ClientFamily family = clientFamilyRepository.findByFamilyCode(familyCode).orElseThrow();
        assertThat(family.getFamilyHead().getId()).isEqualTo(mother.getId());

        List<ClientFamilyHeadHistory> history = clientFamilyHeadHistoryRepository.findByFamilyIdOrderByChangedAtDescIdDesc(family.getId());
        assertThat(history).hasSize(2);
        // Latest history entry: new head is Mother, previous head was Krishan
        assertThat(history.get(0).getNewHead().getId()).isEqualTo(mother.getId());
        assertThat(history.get(0).getPreviousHead().getId()).isEqualTo(krishan.getId());
        // Previous history entry: initial Krishan
        assertThat(history.get(1).getNewHead().getId()).isEqualTo(krishan.getId());
        assertThat(history.get(1).getPreviousHead()).isNull();
    }

    @Test
    @DisplayName("6. Evaluate eligible heads endpoint correctly identifies eligible vs ineligible members")
    void testEligibleHeadsEndpoint() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        Client krishan = createClient("Krishan", ClientPaymentStatus.YES, new BigDecimal("10000.00"), "SIP", null);
        Client mother = createClient("Mother", ClientPaymentStatus.YES, new BigDecimal("25000.00"), "MUTUAL_FUNDS", null);
        Client father = createClient("Father", ClientPaymentStatus.NO, null, null, null);

        CreateFamilyRequest createReq = CreateFamilyRequest.builder()
                .familyName("Eligibility Test Family")
                .initialHeadClientId(krishan.getId())
                .build();

        MvcResult res = mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String familyCode = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("familyCode").asText();

        mockMvc.perform(post("/v1/client-families/" + familyCode + "/members")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddFamilyMemberRequest.builder().clientId(mother.getId()).build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/client-families/" + familyCode + "/members")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddFamilyMemberRequest.builder().clientId(father.getId()).build())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/client-families/" + familyCode + "/eligible-heads")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(3)))
                .andExpect(jsonPath("$.data[?(@.clientCode == '" + krishan.getClientCode() + "')].eligible").value(true))
                .andExpect(jsonPath("$.data[?(@.clientCode == '" + mother.getClientCode() + "')].eligible").value(true))
                .andExpect(jsonPath("$.data[?(@.clientCode == '" + father.getClientCode() + "')].eligible").value(false));
    }

    @Test
    @DisplayName("7. Direct CRM Client Registration integrates with Family linking and new family creation")
    void testClientRegistrationWithFamilyIntegration() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // 1. Register client and create a new family with head designation
        String mobile1 = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest regReq1 = CreateClientRegistrationRequest.builder()
                .clientName("Ananya Verma")
                .mobileNumber(mobile1)
                .speciality("Dentistry")
                .location("Delhi")
                .clinicAddress("Dent Care, Delhi")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .paymentStatus(ClientPaymentStatus.YES)
                .paymentAmount(new BigDecimal("50000.00"))
                .investmentType("MUTUAL_FUNDS")
                .newFamilyName("Verma Family")
                .designateAsHead(true)
                .build();

        MvcResult regRes1 = mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.familyName").value("Verma Family"))
                .andExpect(jsonPath("$.data.familyCode").isNotEmpty())
                .andExpect(jsonPath("$.data.isFamilyHead").value(true))
                .andReturn();

        String generatedFamilyCode = objectMapper.readTree(regRes1.getResponse().getContentAsString()).path("data").path("familyCode").asText();

        // 2. Register second client and link to the existing family
        String mobile2 = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest regReq2 = CreateClientRegistrationRequest.builder()
                .clientName("Rohan Verma")
                .mobileNumber(mobile2)
                .speciality("Pediatrics")
                .location("Delhi")
                .clinicAddress("Child Clinic, Delhi")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .familyCode(generatedFamilyCode)
                .relationshipToHead(FamilyRelationship.SPOUSE)
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.familyCode").value(generatedFamilyCode))
                .andExpect(jsonPath("$.data.familyName").value("Verma Family"))
                .andExpect(jsonPath("$.data.isFamilyHead").value(false));

        // 3. Verify family now has 2 members
        ClientFamily family = clientFamilyRepository.findByFamilyCode(generatedFamilyCode).orElseThrow();
        assertThat(clientFamilyMemberRepository.findByFamilyIdAndMembershipStatus(family.getId(), com.blueant_crm_erp.client.enums.FamilyMembershipStatus.ACTIVE))
                .hasSize(2);
    }

    @Test
    @DisplayName("8. Unauthorized caller without CRM onboarding permissions is forbidden")
    void testUnauthorizedAccess_forbidden() throws Exception {
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        CreateFamilyRequest req = CreateFamilyRequest.builder()
                .familyName("Unauthorized Family")
                .build();

        // RM does not have PHYSICAL_LEAD_ASSIGN or CRM_ONBOARDING role
        mockMvc.perform(post("/v1/client-families")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
