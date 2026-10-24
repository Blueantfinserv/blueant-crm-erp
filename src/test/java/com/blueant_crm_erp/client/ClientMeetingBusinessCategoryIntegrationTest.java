package com.blueant_crm_erp.client;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.ClientBusinessItemDto;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.client.entity.ClientMeetingVerificationBusinessItem;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationBusinessItemRepository;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientMeetingBusinessCategoryIntegrationTest {

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
    private ClientMeetingVerificationBusinessItemRepository clientMeetingVerificationBusinessItemRepository;

    @Autowired
    private UserDetailsService customUserDetailsService;

    private User scUser;
    private User pcUser;
    private Team defaultTeam;

    @BeforeEach
    void setUp() {
        Department salesDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("Sales").code(BootstrapConstants.DEPT_SALES).status(Status.ACTIVE).build()));

        Department crmDept = departmentRepository.findByCodeIgnoreCaseAndDeletedFalse(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.save(Department.builder().name("CRM").code(BootstrapConstants.DEPT_CRM).status(Status.ACTIVE).build()));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        scUser = createTestUser("SC_BIZ", BootstrapConstants.ROLE_SALES_COORDINATOR, BootstrapConstants.DESIG_SC, salesDept, "MEETING_UPDATE", "MEETING_READ");
        pcUser = createTestUser("PC_BIZ", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, crmDept, "MEETING_VERIFY", "MEETING_READ");
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
                .clientName("Test Client " + clientCode)
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .location("Mumbai")
                .clinicAddress("Bandra West")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(salesPerson)
                .build());
    }

    private String submitMeetingUpdate(Client client, UserDetails scPrincipal, String businessGenerated) throws Exception {
        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated(businessGenerated)
                .remarks("Meeting conducted with client")
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .build();

        String responseStr = mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(scPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(responseStr).path("data").path("meetingCode").asText();
    }

    @Test
    @DisplayName("Scenario 1: Pending verification queue correctly exposes businessGenerated = 'YES' from SC submission")
    void testPendingQueueExposesBusinessGeneratedFromScSubmission() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, scPrincipal, "YES");

        mockMvc.perform(get("/v1/client-meetings/verification/pending")
                        .param("sort", "id,desc")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.meetingCode == '" + meetingCode + "')].businessGenerated").value("YES"));
    }

    @Test
    @DisplayName("Scenario 2: PC verifies multiple categories with independent separate amounts")
    void testPcVerifyMultipleCategoriesWithIndependentAmounts() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, scPrincipal, "YES");

        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(11, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessItems(List.of(
                        ClientBusinessItemDto.builder().businessType("SIP").amount(new BigDecimal("15000.00")).build(),
                        ClientBusinessItemDto.builder().businessType("LUMP_SUM").amount(new BigDecimal("50000.00")).build(),
                        ClientBusinessItemDto.builder().businessType("LOAN").amount(new BigDecimal("200000.00")).build()
                ))
                .remarks("Multi-category business verified")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.businessItems", hasSize(3)))
                .andExpect(jsonPath("$.data.businessItems[?(@.businessType == 'SIP')].amount").value(15000.00))
                .andExpect(jsonPath("$.data.businessItems[?(@.businessType == 'LUMP_SUM')].amount").value(50000.00))
                .andExpect(jsonPath("$.data.businessItems[?(@.businessType == 'LOAN')].amount").value(200000.00))
                .andExpect(jsonPath("$.data.investmentAmount").value(265000.00));
    }

    @Test
    @DisplayName("Scenario 3: Non-monetary categories (Service Request and Broker Change) do not require a positive amount")
    void testPcVerifyNonMonetaryCategoriesWithoutAmount() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, scPrincipal, "YES");

        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(12, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessItems(List.of(
                        ClientBusinessItemDto.builder().businessType("SERVICE_REQUEST").amount(null).build(),
                        ClientBusinessItemDto.builder().businessType("BROKER_CHANGE").amount(BigDecimal.ZERO).build()
                ))
                .remarks("Non-monetary requests verified")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.businessItems", hasSize(2)))
                .andExpect(jsonPath("$.data.investmentAmount").doesNotExist());
    }

    @Test
    @DisplayName("Scenario 4: Combined monetary and non-monetary categories sum only the monetary category amount")
    void testPcVerifyCombinedMonetaryAndNonMonetaryCategories() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, scPrincipal, "YES");

        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(14, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessItems(List.of(
                        ClientBusinessItemDto.builder().businessType("SIP").amount(new BigDecimal("10000.00")).build(),
                        ClientBusinessItemDto.builder().businessType("SERVICE_REQUEST").amount(null).build()
                ))
                .remarks("SIP + Service Request verified")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.businessItems", hasSize(2)))
                .andExpect(jsonPath("$.data.investmentAmount").value(10000.00));
    }

    @Test
    @DisplayName("Scenario 5: Monetary category (e.g. SIP) with zero or null amount is rejected with HTTP 400 Bad Request")
    void testMonetaryCategoryRequiresPositiveAmount() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, scPrincipal, "YES");

        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(14, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessItems(List.of(
                        ClientBusinessItemDto.builder().businessType("SIP").amount(BigDecimal.ZERO).build()
                ))
                .remarks("Invalid SIP amount")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Amount must be greater than zero for category: SIP")));
    }

    @Test
    @DisplayName("Scenario 6: SHARES category is correctly normalized to SHARE and verified")
    void testShareAndSharesNormalization() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, scPrincipal, "YES");

        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(15, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessItems(List.of(
                        ClientBusinessItemDto.builder().businessType("SHARES").amount(new BigDecimal("30000.00")).build()
                ))
                .remarks("Shares category normalization test")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.businessItems[0].businessType").value("SHARE"))
                .andExpect(jsonPath("$.data.businessItems[0].amount").value(30000.00))
                .andExpect(jsonPath("$.data.businessTypes[0]").value("SHARE"));
    }

    @Test
    @DisplayName("Scenario 7: Rejection and resubmission preserves attempt #1 and creates attempt #2 with independent category amounts")
    void testPcRejectionAndResubmissionPreservesCategoryAmountsHistory() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, scPrincipal, "YES");

        // PC rejects attempt #1 with reason
        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "Incorrect insurance amount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.attemptNumber").value(1))
                .andExpect(jsonPath("$.data.rejectionReason").value("Incorrect insurance amount"));

        // SC resubmits meeting update -> creates attempt #2
        String resubmitCode = submitMeetingUpdate(client, scPrincipal, "YES");
        assertThat(resubmitCode).isEqualTo(meetingCode);

        // PC verifies attempt #2 with SIP (25k) and INSURANCE (50k)
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(16, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessItems(List.of(
                        ClientBusinessItemDto.builder().businessType("SIP").amount(new BigDecimal("25000.00")).build(),
                        ClientBusinessItemDto.builder().businessType("INSURANCE").amount(new BigDecimal("50000.00")).build()
                ))
                .remarks("Corrected amounts verified")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.attemptNumber").value(2))
                .andExpect(jsonPath("$.data.investmentAmount").value(75000.00));

        // PC fetches verification history -> must contain 2 attempts
        mockMvc.perform(get("/v1/client-meetings/verification/" + meetingCode + "/history")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].attemptNumber").value(1))
                .andExpect(jsonPath("$.data[0].verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data[0].rejectionReason").value("Incorrect insurance amount"))
                .andExpect(jsonPath("$.data[1].attemptNumber").value(2))
                .andExpect(jsonPath("$.data[1].verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data[1].investmentAmount").value(75000.00));
    }

    @Test
    @DisplayName("Scenario 8: Legacy request with multiple monetary categories does not duplicate shared amount to child items")
    void testLegacyRequestFormatRemainsCompatible() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, scPrincipal, "YES");

        ClientMeetingVerificationRequest legacyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(16, 30))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessTypes(List.of("LUMP_SUM", "INSURANCE"))
                .investmentAmount(new BigDecimal("75000.00"))
                .remarks("Legacy format payload")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(legacyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.businessTypes", hasSize(2)))
                .andExpect(jsonPath("$.data.investmentAmount").value(75000.00))
                .andExpect(jsonPath("$.data.businessItems", hasSize(2)))
                .andExpect(jsonPath("$.data.businessItems[0].amount").value(nullValue()))
                .andExpect(jsonPath("$.data.businessItems[1].amount").value(nullValue()));

        // In database, ensure child rows do not duplicate the shared 75000 across categories
        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        ClientMeetingVerification verif = clientMeetingVerificationRepository.findByClientMeetingIdAndIsCurrentTrue(meeting.getId()).orElseThrow();
        List<ClientMeetingVerificationBusinessItem> items = clientMeetingVerificationBusinessItemRepository.findByVerificationId(verif.getId());
        assertThat(items).hasSize(2);
        assertThat(items).allMatch(item -> item.getAmount() == null);
    }

    @Test
    @DisplayName("Scenario 9: Legacy request with exactly one monetary category preserves amount mapping")
    void testLegacyRequestSingleMonetaryCategory_PreservesAmountMapping() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails scPrincipal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String meetingCode = submitMeetingUpdate(client, scPrincipal, "YES");

        ClientMeetingVerificationRequest legacyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(17, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessTypes(List.of("SIP"))
                .investmentAmount(new BigDecimal("50000.00"))
                .remarks("Single category legacy payload")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(legacyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.businessTypes", hasSize(1)))
                .andExpect(jsonPath("$.data.investmentAmount").value(50000.00))
                .andExpect(jsonPath("$.data.businessItems", hasSize(1)))
                .andExpect(jsonPath("$.data.businessItems[0].businessType").value("SIP"))
                .andExpect(jsonPath("$.data.businessItems[0].amount").value(50000.00));

        // In database, child row preserves the 50000.00 amount
        ClientMeeting meeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        ClientMeetingVerification verif = clientMeetingVerificationRepository.findByClientMeetingIdAndIsCurrentTrue(meeting.getId()).orElseThrow();
        List<ClientMeetingVerificationBusinessItem> items = clientMeetingVerificationBusinessItemRepository.findByVerificationId(verif.getId());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("Scenario 10: Historical record with SIP and LUMP_SUM without child rows does not duplicate shared amount to synthesized items")
    void testHistoricalRecordWithoutChildRows_DoesNotDuplicateSharedAmountAcrossCategories() throws Exception {
        Client client = createTestClient(scUser);
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        String histMeetingCode = "BA-CLM-HIST-" + UUID.randomUUID().toString().substring(0, 8);
        ClientMeeting histMeeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode(histMeetingCode)
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(scUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .verifiedByPc(true)
                .businessGenerated("YES")
                .businessTypes("SIP, LUMP_SUM")
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(histMeeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.VERIFIED)
                .businessGenerated("YES")
                .businessTypes("SIP, LUMP_SUM")
                .investmentType("SIP, LUMP_SUM")
                .investmentAmount(new BigDecimal("50000.00"))
                .verifiedBy(pcUser.getEmployeeCode())
                .verifiedAt(java.time.LocalDateTime.now())
                .build());

        // Fetch verification history for historical meeting
        mockMvc.perform(get("/v1/client-meetings/verification/" + histMeetingCode + "/history")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].investmentAmount").value(50000.00))
                .andExpect(jsonPath("$.data[0].businessTypes", hasSize(2)))
                .andExpect(jsonPath("$.data[0].businessItems", hasSize(2)))
                .andExpect(jsonPath("$.data[0].businessItems[0].amount").value(nullValue()))
                .andExpect(jsonPath("$.data[0].businessItems[1].amount").value(nullValue()));

        // Ensure database has zero child rows for this historical record (not fabricated)
        ClientMeetingVerification verif = clientMeetingVerificationRepository.findByClientMeetingIdAndIsCurrentTrue(histMeeting.getId()).orElseThrow();
        List<ClientMeetingVerificationBusinessItem> items = clientMeetingVerificationBusinessItemRepository.findByVerificationId(verif.getId());
        assertThat(items).isEmpty();
    }
}
