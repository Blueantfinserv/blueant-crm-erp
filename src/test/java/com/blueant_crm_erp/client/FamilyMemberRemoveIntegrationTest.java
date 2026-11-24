package com.blueant_crm_erp.client;

import com.blueant_crm_erp.auth.security.CustomUserDetailsService;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.request.AddFamilyMemberRequest;
import com.blueant_crm_erp.client.dto.request.ChangeFamilyHeadRequest;
import com.blueant_crm_erp.client.dto.request.CreateFamilyRequest;
import com.blueant_crm_erp.client.dto.request.RemoveFamilyMemberRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientFamily;
import com.blueant_crm_erp.client.entity.ClientFamilyMember;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.enums.ClientPaymentStatus;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.enums.FamilyMembershipStatus;
import com.blueant_crm_erp.client.enums.FamilyRelationship;
import com.blueant_crm_erp.client.enums.FamilyStatus;
import com.blueant_crm_erp.client.repository.ClientFamilyHeadHistoryRepository;
import com.blueant_crm_erp.client.repository.ClientFamilyMemberRepository;
import com.blueant_crm_erp.client.repository.ClientFamilyRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.FamilyService;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class FamilyMemberRemoveIntegrationTest {

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
    private ClientMeetingRepository clientMeetingRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private FamilyService familyService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private User crmUser;
    private User rmUser;
    private User pcUser;
    private User adminUser;
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

        crmUser = createTestUser("CRM_REM", BootstrapConstants.ROLE_CRM_ONBOARDING, BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, "PHYSICAL_LEAD_ASSIGN");
        rmUser = createTestUser("RM_REM", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        pcUser = createTestUser("PC_REM", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, crmDept, "PHYSICAL_LEAD_ASSIGN");
        adminUser = createTestUser("ADMIN_REM", BootstrapConstants.ROLE_ADMIN, BootstrapConstants.DESIG_BH, crmDept);
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
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().getMostSignificantBits() % 1000000000L));
        String code = "BA-CL-REM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
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

    private ClientMeeting createClientMeeting(Client client, User employee) {
        String code = "BA-CLM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode(code)
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(employee)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(10, 0))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .build());
    }

    /**
     * Helper to set up a standard 3-member family:
     * - Head (Father)
     * - Member 1 (Mother)
     * - Member 2 (Son)
     */
    private StandardFamilyFixture createStandardFamily() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        Client head = createClient("Head Father", ClientPaymentStatus.YES, new BigDecimal("25000.00"), "MUTUAL_FUNDS", new BigDecimal("5000.00"));
        Client member1 = createClient("Member Mother", ClientPaymentStatus.YES, new BigDecimal("10000.00"), "SIP", new BigDecimal("3000.00"));
        Client member2 = createClient("Member Son", ClientPaymentStatus.NO, null, null, null);

        // Create Family with head
        CreateFamilyRequest createReq = CreateFamilyRequest.builder()
                .familyName("Sharma Enterprise Family")
                .initialHeadClientId(head.getId())
                .initialHeadRelationship(FamilyRelationship.SELF)
                .build();

        MvcResult createResult = mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String familyCode = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("familyCode").asText();

        // Add Member 1
        AddFamilyMemberRequest addReq1 = AddFamilyMemberRequest.builder()
                .clientId(member1.getId())
                .relationshipToHead(FamilyRelationship.SPOUSE)
                .build();

        mockMvc.perform(post("/v1/client-families/" + familyCode + "/members")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq1)))
                .andExpect(status().isCreated());

        // Add Member 2
        AddFamilyMemberRequest addReq2 = AddFamilyMemberRequest.builder()
                .clientId(member2.getId())
                .relationshipToHead(FamilyRelationship.CHILD)
                .build();

        mockMvc.perform(post("/v1/client-families/" + familyCode + "/members")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq2)))
                .andExpect(status().isCreated());

        ClientFamily family = clientFamilyRepository.findByFamilyCode(familyCode).orElseThrow();
        return new StandardFamilyFixture(family, head, member1, member2);
    }

    private record StandardFamilyFixture(ClientFamily family, Client head, Client member1, Client member2) {}

    // =========================================================================
    // MANDATORY SCENARIOS 1-8: SUCCESSFUL REMOVAL & INTEGRITY INVARIANTS
    // =========================================================================

    @Test
    @DisplayName("1. Successfully remove a normal active family member")
    void test1_removeNormalActiveMember_success() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        RemoveFamilyMemberRequest req = RemoveFamilyMemberRequest.builder()
                .reason("Member requested independent portfolio management")
                .build();

        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Member removed from family successfully"))
                .andExpect(jsonPath("$.data.familyCode").value(fixture.family.getFamilyCode()))
                .andExpect(jsonPath("$.data.members").isArray());
    }

    @Test
    @DisplayName("2. Removed member disappears from active family-member listing")
    void test2_removedMemberDisappearsFromActiveListing() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Remove member2 (Son)
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(principal)))
                .andExpect(status().isOk());

        // Query active family members
        MvcResult getResult = mockMvc.perform(get("/v1/client-families/" + fixture.family.getFamilyCode())
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.members", org.hamcrest.Matchers.hasSize(2)))
                .andReturn();

        JsonNode membersNode = objectMapper.readTree(getResult.getResponse().getContentAsString()).path("data").path("members");
        for (JsonNode m : membersNode) {
            assertThat(m.path("clientId").asLong()).isNotEqualTo(fixture.member2.getId());
        }

        // DB level check for active members
        List<ClientFamilyMember> activeMembers = clientFamilyMemberRepository
                .findByFamilyIdAndMembershipStatus(fixture.family.getId(), FamilyMembershipStatus.ACTIVE);
        assertThat(activeMembers).hasSize(2);
        assertThat(activeMembers.stream().noneMatch(m -> m.getClient().getId().equals(fixture.member2.getId()))).isTrue();
    }

    @Test
    @DisplayName("3. Client record remains unchanged and accessible")
    void test3_clientRecordRemainsUnchangedAndAccessible() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(principal)))
                .andExpect(status().isOk());

        Client freshClient = clientRepository.findById(fixture.member2.getId()).orElseThrow();
        assertThat(freshClient).isNotNull();
        assertThat(freshClient.getClientName()).isEqualTo(fixture.member2.getClientName());
        assertThat(freshClient.getClientCode()).isEqualTo(fixture.member2.getClientCode());
        assertThat(freshClient.getMobileNumber()).isEqualTo(fixture.member2.getMobileNumber());
        assertThat(freshClient.getClientStatus()).isEqualTo(ClientStatus.ACTIVE);
        // Relationship dissociation
        assertThat(freshClient.getFamily()).isNull();
    }

    @Test
    @DisplayName("4. Client ID remains unchanged")
    void test4_clientIdRemainsUnchanged() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
        Long originalId = fixture.member2.getId();
        String originalCode = fixture.member2.getClientCode();

        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + originalId)
                        .with(user(principal)))
                .andExpect(status().isOk());

        Client freshClient = clientRepository.findById(originalId).orElseThrow();
        assertThat(freshClient.getId()).isEqualTo(originalId);
        assertThat(freshClient.getClientCode()).isEqualTo(originalCode);
    }

    @Test
    @DisplayName("5. Investment and SIP records remain unchanged")
    void test5_investmentAndSipRecordsRemainUnchanged() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Remove member1 (who has investment and SIP data)
        BigDecimal expectedPayment = fixture.member1.getPaymentAmount();
        BigDecimal expectedSip = fixture.member1.getExpectedMaxSIP();
        String expectedInvestmentType = fixture.member1.getInvestmentType();

        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member1.getId())
                        .with(user(principal)))
                .andExpect(status().isOk());

        Client freshClient = clientRepository.findById(fixture.member1.getId()).orElseThrow();
        assertThat(freshClient.getPaymentAmount()).isEqualByComparingTo(expectedPayment);
        assertThat(freshClient.getExpectedMaxSIP()).isEqualByComparingTo(expectedSip);
        assertThat(freshClient.getInvestmentType()).isEqualTo(expectedInvestmentType);
    }

    @Test
    @DisplayName("6. Payment history remains unchanged")
    void test6_paymentHistoryRemainsUnchanged() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member1.getId())
                        .with(user(principal)))
                .andExpect(status().isOk());

        Client freshClient = clientRepository.findById(fixture.member1.getId()).orElseThrow();
        assertThat(freshClient.getPaymentStatus()).isEqualTo(ClientPaymentStatus.YES);
        assertThat(freshClient.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("7. Meeting history remains unchanged")
    void test7_meetingHistoryRemainsUnchanged() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Add a meeting to member2
        ClientMeeting meeting = createClientMeeting(fixture.member2, rmUser);
        assertThat(meeting).isNotNull();

        // Remove member2
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(principal)))
                .andExpect(status().isOk());

        // Verify meeting record remains intact
        ClientMeeting freshMeeting = clientMeetingRepository.findById(meeting.getId()).orElseThrow();
        assertThat(freshMeeting.getClient().getId()).isEqualTo(fixture.member2.getId());
        assertThat(freshMeeting.getMeetingCode()).isEqualTo(meeting.getMeetingCode());
        assertThat(freshMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
    }

    @Test
    @DisplayName("8. Family membership removal history is persisted")
    void test8_membershipRemovalHistoryPersisted() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        RemoveFamilyMemberRequest req = RemoveFamilyMemberRequest.builder()
                .reason("Audit tracking reason verification")
                .build();

        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Check client_family_members table: row is NOT deleted, status is INACTIVE, left_at and left_by populated
        ClientFamilyMember memberRow = clientFamilyMemberRepository
                .findByFamilyIdAndClientId(fixture.family.getId(), fixture.member2.getId()).orElseThrow();

        assertThat(memberRow.getMembershipStatus()).isEqualTo(FamilyMembershipStatus.INACTIVE);
        assertThat(memberRow.getLeftAt()).isNotNull();
        assertThat(memberRow.getLeftBy()).isEqualTo(crmUser.getEmployeeCode());
        assertThat(memberRow.getJoinedAt()).isNotNull();
        assertThat(memberRow.getJoinedBy()).isNotNull();
    }

    // =========================================================================
    // MANDATORY SCENARIOS 9-14: BUSINESS VALIDATION PROTECTIONS
    // =========================================================================

    @Test
    @DisplayName("9. Cannot remove the current Family Head")
    void test9_cannotRemoveCurrentFamilyHead() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.head.getId())
                        .with(user(principal)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("currently the Family Head")))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("designate another existing active family member as Family Head first")));

        // Verify head and membership remain completely untouched
        ClientFamily freshFamily = clientFamilyRepository.findByFamilyCode(fixture.family.getFamilyCode()).orElseThrow();
        assertThat(freshFamily.getFamilyHead().getId()).isEqualTo(fixture.head.getId());
    }

    @Test
    @DisplayName("10. Cannot remove the last active member")
    void test10_cannotRemoveLastActiveMember() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Create a 1-member family (head only)
        Client soleClient = createClient("Sole Member", ClientPaymentStatus.YES, new BigDecimal("10000.00"), "MUTUAL_FUNDS", null);
        CreateFamilyRequest createReq = CreateFamilyRequest.builder()
                .familyName("Sole Member Family")
                .initialHeadClientId(soleClient.getId())
                .initialHeadRelationship(FamilyRelationship.SELF)
                .build();

        MvcResult createResult = mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String familyCode = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("familyCode").asText();

        // Attempting to remove the sole member (even if designated head, or if 1 active member remains)
        mockMvc.perform(delete("/v1/client-families/" + familyCode + "/members/" + soleClient.getId())
                        .with(user(principal)))
                .andExpect(status().isConflict());

        // Now test 2-member family where one is removed, leaving 1 member who is NOT head
        StandardFamilyFixture fixture = createStandardFamily();
        // Remove member2 -> leaves head and member1 (2 members)
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(principal)))
                .andExpect(status().isOk());

        // Remove member1 -> leaves only head (1 member)
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member1.getId())
                        .with(user(principal)))
                .andExpect(status().isOk());

        // Now family has only 1 active member left (head). Attempt removal of that last member
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.head.getId())
                        .with(user(principal)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("11. Cannot remove a member belonging to another family")
    void test11_cannotRemoveMemberOfAnotherFamily() throws Exception {
        StandardFamilyFixture fixture1 = createStandardFamily();
        StandardFamilyFixture fixture2 = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Attempt to remove fixture2's member from fixture1's family
        mockMvc.perform(delete("/v1/client-families/" + fixture1.family.getFamilyCode() + "/members/" + fixture2.member2.getId())
                        .with(user(principal)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("not an active member of family")));
    }

    @Test
    @DisplayName("12. Cannot remove an already removed member")
    void test12_cannotRemoveAlreadyRemovedMember() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // First removal succeeds
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(principal)))
                .andExpect(status().isOk());

        // Second removal attempt of the same member fails with 409 CONFLICT
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(principal)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("already an inactive or removed member of family")));
    }

    @Test
    @DisplayName("13. Cannot remove a member from an inactive family")
    void test13_cannotRemoveMemberFromInactiveFamily() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Deactivate family
        ClientFamily family = clientFamilyRepository.findByFamilyCode(fixture.family.getFamilyCode()).orElseThrow();
        family.setStatus(FamilyStatus.INACTIVE);
        clientFamilyRepository.save(family);

        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(principal)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Cannot remove member from inactive family")));
    }

    @Test
    @DisplayName("14. Cannot remove a member from a nonexistent family")
    void test14_cannotRemoveMemberFromNonexistentFamily() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(delete("/v1/client-families/BA-FAM-NONEXISTENT-9999/members/99999")
                        .with(user(principal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Family not found with code")));
    }

    // =========================================================================
    // MANDATORY SCENARIOS 15-18: SECURITY, CONCURRENCY & TRANSACTION ROLLBACK
    // =========================================================================

    @Test
    @DisplayName("15. Unauthorized user receives 403")
    void test15_unauthorizedUserReceivesForbidden() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        // RM lacks PHYSICAL_LEAD_ASSIGN / CRM_ONBOARDING / ADMIN
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member2.getId())
                        .with(user(rmPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("16. Authorized user can remove a normal member (PC Coordinator and Admin)")
    void test16_authorizedUserCanRemoveMember() throws Exception {
        // Test with PC Coordinator (has PHYSICAL_LEAD_ASSIGN)
        StandardFamilyFixture fixture1 = createStandardFamily();
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        mockMvc.perform(delete("/v1/client-families/" + fixture1.family.getFamilyCode() + "/members/" + fixture1.member2.getId())
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Test with Admin
        StandardFamilyFixture fixture2 = createStandardFamily();
        UserDetails adminPrincipal = customUserDetailsService.loadUserByUsername(adminUser.getEmployeeCode());

        mockMvc.perform(delete("/v1/client-families/" + fixture2.family.getFamilyCode() + "/members/" + fixture2.member2.getId())
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    @DisplayName("17. Failure during persistence rolls back the entire transaction")
    void test17_failureDuringPersistenceRollsBackTransaction() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();

        // Verify initial state
        ClientFamilyMember initialMember = clientFamilyMemberRepository
                .findByFamilyIdAndClientId(fixture.family.getId(), fixture.member2.getId()).orElseThrow();
        assertThat(initialMember.getMembershipStatus()).isEqualTo(FamilyMembershipStatus.ACTIVE);
        assertThat(initialMember.getLeftAt()).isNull();

        Client initialClient = clientRepository.findById(fixture.member2.getId()).orElseThrow();
        assertThat(initialClient.getFamily()).isNotNull();

        // Execute service call inside a TransactionTemplate that throws an exception after removeMember
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            template.execute(status -> {
                familyService.removeMember(
                        fixture.family.getFamilyCode(),
                        fixture.member2.getId(),
                        RemoveFamilyMemberRequest.builder().reason("Testing rollback").build(),
                        "ROLLBACK_TEST_USER"
                );
                // Simulate failure before commit
                throw new RuntimeException("Simulated persistence/audit failure triggering transaction rollback");
            });
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("Simulated persistence/audit failure");
        }

        // Verify rollback: member is still ACTIVE, leftAt is still null, client.family is still set
        ClientFamilyMember postRollbackMember = clientFamilyMemberRepository
                .findByFamilyIdAndClientId(fixture.family.getId(), fixture.member2.getId()).orElseThrow();
        assertThat(postRollbackMember.getMembershipStatus()).isEqualTo(FamilyMembershipStatus.ACTIVE);
        assertThat(postRollbackMember.getLeftAt()).isNull();

        Client postRollbackClient = clientRepository.findById(fixture.member2.getId()).orElseThrow();
        assertThat(postRollbackClient.getFamily()).isNotNull();
        assertThat(postRollbackClient.getFamily().getId()).isEqualTo(fixture.family.getId());
    }

    @Test
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    @DisplayName("18. Concurrent removal attempts do not corrupt family state")
    void test18_concurrentRemovalAttemptsSafety() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        String familyCode = fixture.family.getFamilyCode();
        Long member2Id = fixture.member2.getId();

        int numThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    TransactionTemplate tt = new TransactionTemplate(transactionManager);
                    tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    tt.execute(status -> {
                        familyService.removeMember(familyCode, member2Id, null, "CONCURRENT_USER");
                        return null;
                    });
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // One of the concurrent requests will succeed, the rest will fail with conflict or lock wait
                    conflictCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactly 1 thread must succeed, remaining threads must fail cleanly
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(numThreads - 1);

        // Final state must be cleanly 2 active members remaining
        List<ClientFamilyMember> remainingActive = clientFamilyMemberRepository
                .findByFamilyIdAndMembershipStatus(fixture.family.getId(), FamilyMembershipStatus.ACTIVE);
        assertThat(remainingActive).hasSize(2);
    }

    // =========================================================================
    // MANDATORY SCENARIOS 19-20: EXISTING WORKFLOW REGRESSION VERIFICATION
    // =========================================================================

    @Test
    @DisplayName("19. Existing Family Head Change continues to pass and works seamlessly")
    void test19_existingFamilyHeadChangeWorksSeamlessly() throws Exception {
        StandardFamilyFixture fixture = createStandardFamily();
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Change head from Father to Mother (member1)
        ChangeFamilyHeadRequest changeReq = ChangeFamilyHeadRequest.builder()
                .newHeadClientId(fixture.member1.getId())
                .reason("Transferred headship to spouse")
                .build();

        mockMvc.perform(put("/v1/client-families/" + fixture.family.getFamilyCode() + "/head")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familyHeadClientId").value(fixture.member1.getId()));

        // Now previous head (Father) is a normal member and CAN be removed safely
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.head.getId())
                        .with(user(principal)))
                .andExpect(status().isOk());

        // New head (Mother) CANNOT be removed because she is now the head
        mockMvc.perform(delete("/v1/client-families/" + fixture.family.getFamilyCode() + "/members/" + fixture.member1.getId())
                        .with(user(principal)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("currently the Family Head")));
    }

    @Test
    @DisplayName("20. Existing createFamily() and addMember() continue to pass without deviation")
    void test20_createFamilyAndAddMemberContinueToPass() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // 1. Create family
        CreateFamilyRequest createReq = CreateFamilyRequest.builder()
                .familyName("Regression Test Family")
                .build();

        MvcResult createResult = mockMvc.perform(post("/v1/client-families")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String familyCode = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("familyCode").asText();

        // 2. Add member
        Client newClient = createClient("New Member", ClientPaymentStatus.NO, null, null, null);
        AddFamilyMemberRequest addReq = AddFamilyMemberRequest.builder()
                .clientId(newClient.getId())
                .relationshipToHead(FamilyRelationship.SELF)
                .build();

        mockMvc.perform(post("/v1/client-families/" + familyCode + "/members")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.clientCode").value(newClient.getClientCode()));
    }
}
