package com.blueant_crm_erp.client;

import com.blueant_crm_erp.auth.security.CustomUserDetailsService;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.request.ClientDocumentRequest;
import com.blueant_crm_erp.client.dto.request.ClientNomineeRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientMeetingRequest;
import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientDocument;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.client.enums.ClientDocumentType;
import com.blueant_crm_erp.client.enums.ClientPaymentStatus;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientAssignmentHistoryRepository;
import com.blueant_crm_erp.client.repository.ClientDocumentRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingUpdateRepository;
import com.blueant_crm_erp.client.repository.ClientMeetingVerificationRepository;
import com.blueant_crm_erp.client.repository.ClientNomineeRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.dto.response.ClientDocumentResponse;
import com.blueant_crm_erp.client.service.ClientDocumentService;
import com.blueant_crm_erp.client.service.ClientMeetingService;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.lead.repository.LeadRepository;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class ClientWorkflowIntegrationTest {

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
    private ClientNomineeRepository clientNomineeRepository;

    @Autowired
    private ClientDocumentRepository clientDocumentRepository;

    @Autowired
    private ClientAssignmentHistoryRepository clientAssignmentHistoryRepository;

    @Autowired
    private ClientMeetingRepository clientMeetingRepository;

    @Autowired
    private ClientMeetingUpdateRepository clientMeetingUpdateRepository;

    @Autowired
    private ClientMeetingVerificationRepository clientMeetingVerificationRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ClientMeetingService clientMeetingService;

    @Autowired
    private ClientDocumentService clientDocumentService;

    private User crmUser;
    private User rmUser;
    private User scUser;
    private User smUser;
    private User pcUser;
    private User otherRmUser;

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

        crmUser = createTestUser("CRM_USER", BootstrapConstants.ROLE_CRM_ONBOARDING, BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, "PHYSICAL_LEAD_ASSIGN");
        rmUser = createTestUser("RM_USER", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        otherRmUser = createTestUser("RM_OTHER", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, BootstrapConstants.DESIG_RM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        scUser = createTestUser("SC_USER", BootstrapConstants.ROLE_SALES_COORDINATOR, BootstrapConstants.DESIG_SC, salesDept, "MEETING_UPDATE", "MEETING_READ");
        smUser = createTestUser("SM_USER", BootstrapConstants.ROLE_SALES_MANAGER, BootstrapConstants.DESIG_SM, salesDept, "MEETING_UPDATE", "MEETING_READ");
        pcUser = createTestUser("PC_USER", BootstrapConstants.ROLE_PC_COORDINATOR, BootstrapConstants.DESIG_PC, crmDept, "MEETING_VERIFY", "MEETING_READ");
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

    // =========================================================================
    // 1. Client Registration & Assignment Tests
    // =========================================================================

    @Test
    @DisplayName("1.1 CRM Onboarding successfully registers client and assigns to RM; Lead table remains untouched")
    void test1_1_successfulClientRegistrationToRm() throws Exception {
        long leadCountBefore = leadRepository.count();
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Vikram Rathore")
                .mobileNumber(mobile)
                .alternateMobileNumber("9876543211")
                .email("dr.vikram" + UUID.randomUUID().toString().substring(0, 5) + "@example.com")
                .speciality("Neurology")
                .location("Connaught Place, New Delhi")
                .clinicAddress("Clinic 12, Medical Plaza")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .assignmentDate(LocalDate.now())
                .bestTimeToMeet("12:00 PM - 3:00 PM")
                .remarks("Referred from Apollo Hospital Excel sheet")
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientName").value("Dr. Vikram Rathore"))
                .andExpect(jsonPath("$.data.clientCode").isNotEmpty())
                .andExpect(jsonPath("$.data.assignedSalesPersonCode").value(rmUser.getEmployeeCode()));

        // CRITICAL INVARIANT: Zero lead rows created
        assertThat(leadRepository.count()).isEqualTo(leadCountBefore);

        // Verify database state
        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getLead()).isNull(); // Strictly null lead relationship
        assertThat(client.getClientStatus()).isEqualTo(ClientStatus.ACTIVE);
        assertThat(client.getSalesPerson().getId()).isEqualTo(rmUser.getId());
        assertThat(client.getSpeciality()).isEqualTo("Neurology");
        assertThat(client.getBestTimeToMeet()).isEqualTo("12:00 PM - 3:00 PM");
    }

    @Test
    @DisplayName("1.2 CRM Onboarding successfully registers client and assigns to SC")
    void test1_2_successfulClientRegistrationToSc() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Ananya Roy")
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Noida Sector 62")
                .clinicAddress("Fortis Clinic")
                .salesPersonEmployeeCode(scUser.getEmployeeCode())
                .bestTimeToMeet("3:00 PM - 6:00 PM")
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.assignedSalesPersonCode").value(scUser.getEmployeeCode()));

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getSalesPerson().getId()).isEqualTo(scUser.getId());
    }

    @Test
    @DisplayName("1.3 Ineligible assignee (SM, PC, Admin) is rejected on backend")
    void test1_3_ineligibleAssigneeRejected() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Ineligible Test")
                .mobileNumber(mobile)
                .speciality("General Medicine")
                .location("Delhi")
                .clinicAddress("Address")
                .salesPersonEmployeeCode(smUser.getEmployeeCode()) // SM is ineligible!
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("1.4 Duplicate mobile number registration is rejected with HTTP 409 Conflict")
    void test1_4_duplicateMobileRejected() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req1 = CreateClientRegistrationRequest.builder()
                .clientName("Dr. First Client")
                .mobileNumber(mobile)
                .speciality("ENT")
                .location("Delhi")
                .clinicAddress("Clinic 1")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Attempt duplicate with same mobile
        CreateClientRegistrationRequest req2 = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Second Client")
                .mobileNumber(mobile) // Duplicate!
                .speciality("ENT")
                .location("Delhi")
                .clinicAddress("Clinic 2")
                .salesPersonEmployeeCode(scUser.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("1.5 Invalid bestTimeToMeet slot is rejected")
    void test1_5_invalidBestTimeToMeetRejected() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Slot Test")
                .mobileNumber(mobile)
                .speciality("Pediatrics")
                .location("Delhi")
                .clinicAddress("Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .bestTimeToMeet("10:00 AM - 11:00 AM") // Invalid slot!
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("1.6 Full profile registration with Section A-E, nominees, documents and initial assignment history")
    void test1_6_fullProfileRegistrationWithNomineesDocumentsAndHistory() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        LocalDate dob = LocalDate.of(1985, 5, 20);
        LocalDate actualDob = LocalDate.of(1985, 3, 15);
        LocalDate appDate = LocalDate.now().minusDays(1);
        LocalDate reportDate = LocalDate.now();
        LocalDate paymentDate = LocalDate.now();

        ClientNomineeRequest nominee1 = ClientNomineeRequest.builder()
                .nomineeName("Anita Rathore")
                .relationship("Spouse")
                .dateOfBirth(LocalDate.of(1987, 8, 14))
                .contactNumber("9876500001")
                .panNumber("ABCDE1234F")
                .isMinor(false)
                .build();

        ClientNomineeRequest nominee2 = ClientNomineeRequest.builder()
                .nomineeName("Aarav Rathore")
                .relationship("Son")
                .dateOfBirth(LocalDate.of(2015, 3, 10))
                .isMinor(true)
                .guardianName("Anita Rathore")
                .guardianRelationship("Mother")
                .guardianContact("9876500001")
                .guardianPan("ABCDE1234F")
                .build();

        ClientDocumentRequest doc1 = ClientDocumentRequest.builder()
                .documentType(ClientDocumentType.PAN_KYC)
                .documentName("Client PAN Card")
                .received(true)
                .receivedDate(LocalDate.now())
                .verificationStatus(VerificationStatus.VERIFIED)
                .documentReference("DOC-REF-001")
                .remarks("Verified via NSDL")
                .build();

        ClientDocumentRequest doc2 = ClientDocumentRequest.builder()
                .documentType(ClientDocumentType.APPLICATION_FORM)
                .documentName("Signed Application Form")
                .received(true)
                .receivedDate(LocalDate.now())
                .verificationStatus(VerificationStatus.PENDING)
                .documentReference("DOC-REF-002")
                .remarks("Awaiting PC check")
                .build();

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Vikram Profile")
                .mobileNumber(mobile)
                .alternateMobileNumber("9876543211")
                .email("dr.profile" + UUID.randomUUID().toString().substring(0, 5) + "@example.com")
                .speciality("Neurology")
                .location("Connaught Place, New Delhi")
                .clinicAddress("Clinic 12, Medical Plaza")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .assignmentDate(LocalDate.now())
                .bestTimeToMeet("12:00 PM - 3:00 PM")
                .remarks("Complete onboarding profile")
                // Section A
                .panNumber("ABCDE1234F")
                .dateOfBirth(dob)
                .actualDateOfBirth(actualDob)
                .motherName("Sunita Rathore")
                .placeOfBirth("New Delhi")
                .occupation("Doctor")
                .familyHeadName("Vikram Rathore")
                .familyDetails("Spouse, 1 child")
                .residentialAddress("Flat 402, Green Valley")
                .correspondenceAddress("Clinic 12, Medical Plaza")
                .officeAddress("Apollo Hospital, Sarita Vihar")
                .city("New Delhi")
                .state("Delhi")
                .pinCode("110001")
                // Section B
                .clientSource("Doctor Referral")
                .sourceDescription("Referred by Dr. Sharma")
                // Section C
                .applicationReceivedDate(appDate)
                .applicationMode("PHYSICAL")
                .investmentTypes(java.util.List.of("New SIP", "Lumpsum"))
                .firstInvestmentAmount(new BigDecimal("50000.00"))
                .expectedMaxSIP(new BigDecimal("100000.00"))
                .investwellId("IW-99881")
                .clientReportDate(reportDate)
                .paymentStatus(ClientPaymentStatus.YES)
                .paymentAmount(new BigDecimal("50000.00"))
                .paymentDate(paymentDate)
                .paymentReference("PAY-REF-XYZ")
                // Section D
                .nominees(java.util.List.of(nominee1, nominee2))
                // Section E
                .documents(java.util.List.of(doc1, doc2))
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientName").value("Dr. Vikram Profile"))
                .andExpect(jsonPath("$.data.panNumber").value("ABCDE1234F"))
                .andExpect(jsonPath("$.data.dateOfBirth").value(dob.toString()))
                .andExpect(jsonPath("$.data.actualDateOfBirth").value(actualDob.toString()))
                .andExpect(jsonPath("$.data.motherName").value("Sunita Rathore"))
                .andExpect(jsonPath("$.data.city").value("New Delhi"))
                .andExpect(jsonPath("$.data.clientSource").value("Doctor Referral"))
                .andExpect(jsonPath("$.data.investmentType").value("New SIP, Lumpsum"))
                .andExpect(jsonPath("$.data.investmentTypes[0]").value("New SIP"))
                .andExpect(jsonPath("$.data.investmentTypes[1]").value("Lumpsum"))
                .andExpect(jsonPath("$.data.paymentStatus").value("YES"))
                .andExpect(jsonPath("$.data.nominees").isArray())
                .andExpect(jsonPath("$.data.nominees.length()").value(2))
                .andExpect(jsonPath("$.data.documents").isArray())
                .andExpect(jsonPath("$.data.documents.length()").value(2))
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("ALL_RECEIVED"));

        // Verify Database Persistence
        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getPanNumber()).isEqualTo("ABCDE1234F");
        assertThat(client.getDateOfBirth()).isEqualTo(dob);
        assertThat(client.getActualDateOfBirth()).isEqualTo(actualDob);
        assertThat(client.getCity()).isEqualTo("New Delhi");
        assertThat(client.getInvestmentType()).isEqualTo("New SIP, Lumpsum");
        assertThat(client.getPaymentStatus()).isEqualTo(ClientPaymentStatus.YES);

        // Verify Nominees
        var savedNominees = clientNomineeRepository.findByClientIdOrderByCreatedAtAsc(client.getId());
        assertThat(savedNominees).hasSize(2);
        assertThat(savedNominees.get(0).getNomineeName()).isEqualTo("Anita Rathore");
        assertThat(savedNominees.get(0).getIsMinor()).isFalse();
        assertThat(savedNominees.get(1).getNomineeName()).isEqualTo("Aarav Rathore");
        assertThat(savedNominees.get(1).getIsMinor()).isTrue();
        assertThat(savedNominees.get(1).getGuardianName()).isEqualTo("Anita Rathore");

        // Verify Documents
        var savedDocs = clientDocumentRepository.findByClientIdOrderByCreatedAtAsc(client.getId());
        assertThat(savedDocs).hasSize(2);
        assertThat(savedDocs.get(0).getDocumentType()).isEqualTo(ClientDocumentType.PAN_KYC);
        assertThat(savedDocs.get(0).getReceived()).isTrue();
        assertThat(savedDocs.get(0).getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

        // Verify Assignment History
        var histories = clientAssignmentHistoryRepository.findByClientIdOrderByAssignedAtDesc(client.getId());
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getAssignedFromUser()).isNull();
        assertThat(histories.get(0).getAssignedToUser().getId()).isEqualTo(rmUser.getId());
        assertThat(histories.get(0).getAssignedByUser().getId()).isEqualTo(crmUser.getId());
    }

    @Test
    @DisplayName("1.7 Minor nominee without guardian details is rejected with HTTP 400 Bad Request")
    void test1_7_minorNomineeWithoutGuardianRejected() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        ClientNomineeRequest minorNomineeWithoutGuardian = ClientNomineeRequest.builder()
                .nomineeName("Baby Rathore")
                .relationship("Child")
                .isMinor(true)
                // guardianName is missing!
                .build();

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Vikram Guardian Test")
                .mobileNumber(mobile)
                .speciality("Neurology")
                .location("Delhi")
                .clinicAddress("Clinic 12")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .nominees(java.util.List.of(minorNomineeWithoutGuardian))
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Guardian name is required for minor nominee")));
    }

    @Test
    @DisplayName("1.8 Authorized user can retrieve complete client profile endpoint")
    void test1_8_clientFullProfileRetrievalEndpoint() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Profile Fetch Test")
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Connaught Place")
                .clinicAddress("Fortis Plaza")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .panNumber("ABCDE9999Z")
                .city("Gurugram")
                .clientSource("Direct")
                .investmentType("New SIP")
                .build();

        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        String responseStr = mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String clientCode = objectMapper.readTree(responseStr).path("data").path("clientCode").asText();

        // Retrieve full profile via GET /v1/clients/{clientCode}/profile as RM
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        mockMvc.perform(get("/v1/clients/" + clientCode + "/profile")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientCode").value(clientCode))
                .andExpect(jsonPath("$.data.clientName").value("Dr. Profile Fetch Test"))
                .andExpect(jsonPath("$.data.panNumber").value("ABCDE9999Z"))
                .andExpect(jsonPath("$.data.city").value("Gurugram"))
                .andExpect(jsonPath("$.data.clientSource").value("Direct"))
                .andExpect(jsonPath("$.data.investmentType").value("New SIP"))
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("NO_DOCUMENTS"))
                .andExpect(jsonPath("$.data.assignmentHistories").isArray())
                .andExpect(jsonPath("$.data.assignmentHistories.length()").value(1));
    }

    @Test
    @DisplayName("1.9 Unauthenticated access to client profile retrieval is rejected with HTTP 401/403")
    void test1_9_clientProfileRetrievalUnauthorizedForbidden() throws Exception {
        mockMvc.perform(get("/v1/clients/BA-CL-NONEXISTENT/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("1.10 Backward compatibility: Registration succeeds with only mandatory fields and zero optional profile fields")
    void test1_10_backwardCompatibilityOmitAllOptionalFields() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Minimal Client")
                .mobileNumber(mobile)
                .speciality("Dentistry")
                .location("South Ex")
                .clinicAddress("Dental Care 1")
                .salesPersonEmployeeCode(scUser.getEmployeeCode())
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientName").value("Dr. Minimal Client"))
                .andExpect(jsonPath("$.data.panNumber").doesNotExist())
                .andExpect(jsonPath("$.data.dateOfBirth").doesNotExist())
                .andExpect(jsonPath("$.data.actualDateOfBirth").doesNotExist())
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("NO_DOCUMENTS"));

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getPanNumber()).isNull();
        assertThat(client.getDateOfBirth()).isNull();
        assertThat(client.getActualDateOfBirth()).isNull();
        assertThat(client.getClientSource()).isNull();
        assertThat(client.getNominees()).isEmpty();
        assertThat(client.getDocuments()).isEmpty();
    }

    @Test
    @DisplayName("1.11 actualDateOfBirth and dateOfBirth are stored independently, coexist, and return via registration and profile API")
    void test1_11_actualDateOfBirthIndependentStorageAndRetrieval() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        LocalDate recordedDob = LocalDate.of(1980, 5, 10);
        LocalDate actualDob = LocalDate.of(1980, 3, 25);

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Independent DOB Client")
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Delhi Central")
                .clinicAddress("Heart Institute")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .dateOfBirth(recordedDob)
                .actualDateOfBirth(actualDob)
                .build();

        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        String responseStr = mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dateOfBirth").value(recordedDob.toString()))
                .andExpect(jsonPath("$.data.actualDateOfBirth").value(actualDob.toString()))
                .andReturn().getResponse().getContentAsString();

        String clientCode = objectMapper.readTree(responseStr).path("data").path("clientCode").asText();

        // Verify independent database persistence
        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getDateOfBirth()).isEqualTo(recordedDob);
        assertThat(client.getActualDateOfBirth()).isEqualTo(actualDob);
        assertThat(client.getDateOfBirth()).isNotEqualTo(client.getActualDateOfBirth());

        // Verify retrieval via GET /v1/clients/{clientCode}/profile
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        mockMvc.perform(get("/v1/clients/" + clientCode + "/profile")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dateOfBirth").value(recordedDob.toString()))
                .andExpect(jsonPath("$.data.actualDateOfBirth").value(actualDob.toString()));
    }

    @Test
    @DisplayName("1.12 actualDateOfBirth in the future is rejected with HTTP 400 Bad Request")
    void test1_12_actualDateOfBirthFutureDateRejected() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        LocalDate futureDate = LocalDate.now().plusDays(2);

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Future Actual DOB")
                .mobileNumber(mobile)
                .speciality("Orthopedics")
                .location("Delhi South")
                .clinicAddress("Bone Care")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .actualDateOfBirth(futureDate)
                .build();

        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.actualDateOfBirth").value("Actual date of birth must be in the past."));
    }

    @Test
    @DisplayName("1.13 Backward compatibility: Registration succeeds when actualDateOfBirth is omitted")
    void test1_13_backwardCompatibilityOmitActualDateOfBirth() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        LocalDate recordedDob = LocalDate.of(1990, 7, 15);

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Legacy Client No Actual DOB")
                .mobileNumber(mobile)
                .speciality("Pediatrics")
                .location("Delhi East")
                .clinicAddress("Child Care Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .dateOfBirth(recordedDob)
                // actualDateOfBirth omitted / null
                .build();

        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        String responseStr = mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dateOfBirth").value(recordedDob.toString()))
                .andExpect(jsonPath("$.data.actualDateOfBirth").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String clientCode = objectMapper.readTree(responseStr).path("data").path("clientCode").asText();

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getDateOfBirth()).isEqualTo(recordedDob);
        assertThat(client.getActualDateOfBirth()).isNull();

        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        mockMvc.perform(get("/v1/clients/" + clientCode + "/profile")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dateOfBirth").value(recordedDob.toString()))
                .andExpect(jsonPath("$.data.actualDateOfBirth").doesNotExist());
    }

    @Test
    @DisplayName("1.14 Any TWO distinct supported document types satisfy the document requirement (PAN + Passport, Cheque + Signature, Aadhaar + Other)")
    void test1_14_documentsAnyTwoDistinctSupportedTypesAccepted() throws Exception {
        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Combination 1: PAN_CARD + PASSPORT
        String mobile1 = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req1 = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Pair One Client")
                .mobileNumber(mobile1)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Heart Institute")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .documents(List.of(
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.PAN_CARD).received(true).receivedDate(LocalDate.now()).build(),
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.PASSPORT).received(true).receivedDate(LocalDate.now()).build()
                ))
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("ALL_RECEIVED"));

        // Combination 2: CHEQUE + SIGNATURE
        String mobile2 = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req2 = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Pair Two Client")
                .mobileNumber(mobile2)
                .speciality("Neurology")
                .location("Delhi")
                .clinicAddress("Brain Hospital")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .documents(List.of(
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.CHEQUE).received(true).receivedDate(LocalDate.now()).build(),
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.SIGNATURE).received(true).receivedDate(LocalDate.now()).build()
                ))
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("ALL_RECEIVED"));

        // Combination 3: AADHAAR_CARD + OTHER
        String mobile3 = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req3 = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Pair Three Client")
                .mobileNumber(mobile3)
                .speciality("Pediatrics")
                .location("Delhi")
                .clinicAddress("Child Care")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .documents(List.of(
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.AADHAAR_CARD).received(true).receivedDate(LocalDate.now()).build(),
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.OTHER).received(true).receivedDate(LocalDate.now()).build()
                ))
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("ALL_RECEIVED"));
    }

    @Test
    @DisplayName("1.15 Submitting only ONE document type in registration is rejected with HTTP 400 Bad Request")
    void test1_15_documentsSingleTypeOnlyRejected() throws Exception {
        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Single Doc Client")
                .mobileNumber(mobile)
                .speciality("Dentistry")
                .location("Delhi")
                .clinicAddress("Dental Care")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .documents(List.of(
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.PAN_CARD).received(true).receivedDate(LocalDate.now()).build()
                ))
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Any TWO distinct supported document types are mandatory")));
    }

    @Test
    @DisplayName("1.16 Submitting TWO files of the SAME document type in registration is rejected with HTTP 400 Bad Request")
    void test1_16_documentsTwoFilesOfSameTypeRejected() throws Exception {
        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Duplicate Doc Type Client")
                .mobileNumber(mobile)
                .speciality("Dentistry")
                .location("Delhi")
                .clinicAddress("Dental Care")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .documents(List.of(
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.PAN_CARD).received(true).receivedDate(LocalDate.now()).build(),
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.PAN_CARD).received(true).receivedDate(LocalDate.now()).build()
                ))
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Any TWO distinct supported document types are mandatory")));
    }

    @Test
    @DisplayName("1.17 Incremental upload: PARTIAL status with 1 distinct type or duplicate type, ALL_RECEIVED when 2 distinct types uploaded")
    void test1_17_documentsIncrementalUploadToDocumentComplete() throws Exception {
        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        // Step 1: Register client without documents -> NO_DOCUMENTS
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Incremental Upload Client")
                .mobileNumber(mobile)
                .speciality("Dermatology")
                .location("Delhi")
                .clinicAddress("Skin Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .build();

        String responseStr = mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("NO_DOCUMENTS"))
                .andReturn().getResponse().getContentAsString();

        String clientCode = objectMapper.readTree(responseStr).path("data").path("clientCode").asText();

        // Step 2: Upload 1st document (PAN_CARD)
        byte[] pdfBytes1 = "%PDF-1.4 pan content 1".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MockMultipartFile file1 = new MockMultipartFile("file", "pan.pdf", "application/pdf", pdfBytes1);
        mockMvc.perform(multipart("/v1/clients/" + clientCode + "/documents")
                        .file(file1)
                        .param("documentType", "PAN_CARD")
                        .with(user(rmPrincipal)))
                .andExpect(status().isCreated());

        // Status must be PARTIAL because only 1 distinct type exists
        mockMvc.perform(get("/v1/clients/" + clientCode + "/profile")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("PARTIAL"));

        // Step 3: Upload 2nd document of SAME type (PAN_CARD)
        byte[] pdfBytes2 = "%PDF-1.4 pan content 2".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MockMultipartFile file2 = new MockMultipartFile("file", "pan_back.pdf", "application/pdf", pdfBytes2);
        mockMvc.perform(multipart("/v1/clients/" + clientCode + "/documents")
                        .file(file2)
                        .param("documentType", "PAN_CARD")
                        .with(user(rmPrincipal)))
                .andExpect(status().isCreated());

        // Status must STILL be PARTIAL because distinct types count is still 1
        mockMvc.perform(get("/v1/clients/" + clientCode + "/profile")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("PARTIAL"));

        // Step 4: Upload 3rd document of a DISTINCT supported type (PASSPORT)
        byte[] pdfBytes3 = "%PDF-1.4 passport content".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MockMultipartFile file3 = new MockMultipartFile("file", "passport.pdf", "application/pdf", pdfBytes3);
        mockMvc.perform(multipart("/v1/clients/" + clientCode + "/documents")
                        .file(file3)
                        .param("documentType", "PASSPORT")
                        .with(user(rmPrincipal)))
                .andExpect(status().isCreated());

        // Now distinct types = 2 (PAN_CARD, PASSPORT) -> Status must become ALL_RECEIVED
        mockMvc.perform(get("/v1/clients/" + clientCode + "/profile")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("ALL_RECEIVED"));
    }

    @Test
    @DisplayName("1.18 Registration with all SIX supported document types succeeds and is marked ALL_RECEIVED")
    void test1_18_documentsAllSixSupportedTypesAccepted() throws Exception {
        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. All Six Docs Client")
                .mobileNumber(mobile)
                .speciality("Surgery")
                .location("Delhi")
                .clinicAddress("Surgical Center")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .documents(List.of(
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.PAN_CARD).received(true).receivedDate(LocalDate.now()).build(),
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.AADHAAR_CARD).received(true).receivedDate(LocalDate.now()).build(),
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.PASSPORT).received(true).receivedDate(LocalDate.now()).build(),
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.CHEQUE).received(true).receivedDate(LocalDate.now()).build(),
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.SIGNATURE).received(true).receivedDate(LocalDate.now()).build(),
                        ClientDocumentRequest.builder().documentType(ClientDocumentType.OTHER).received(true).receivedDate(LocalDate.now()).build()
                ))
                .build();

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(crmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentCompletionStatus").value("ALL_RECEIVED"));
    }

    @Test
    @DisplayName("1.19 Uploading unsupported document type is rejected with HTTP 400 Bad Request")
    void test1_19_documentsUnsupportedTypeRejected() throws Exception {
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-BADTYPE-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Bad Doc Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        byte[] pdfBytes = "%PDF-1.4 test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", pdfBytes);

        mockMvc.perform(multipart("/v1/clients/" + client.getClientCode() + "/documents")
                        .file(file)
                        .param("documentType", "INVALID_DOC_TYPE")
                        .with(user(rmPrincipal)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 2. Client Work Follow Queue Tests
    // =========================================================================

    @Test
    @DisplayName("2.1 RM sees only their own assigned clients in Client Work Follow; other RM cannot see them")
    void test2_1_rmWorkFollowIsolation() throws Exception {
        // Register client assigned to rmUser
        Client clientRm1 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900001")
                .clientName("Client For RM 1")
                .mobileNumber("9111111111")
                .speciality("Dental")
                .location("Delhi")
                .clinicAddress("Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        // Register client assigned to otherRmUser
        Client clientRm2 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900002")
                .clientName("Client For RM 2")
                .mobileNumber("9222222222")
                .speciality("Dental")
                .location("Delhi")
                .clinicAddress("Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(otherRmUser)
                .build());

        // RM 1 queries work follow
        UserDetails principalRm1 = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        mockMvc.perform(get("/v1/clients/work-follow")
                        .with(user(principalRm1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == 'BA-CL-2026-900001')]").exists())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == 'BA-CL-2026-900002')]").doesNotExist());

        // RM 2 queries work follow
        UserDetails principalRm2 = customUserDetailsService.loadUserByUsername(otherRmUser.getEmployeeCode());
        mockMvc.perform(get("/v1/clients/work-follow")
                        .with(user(principalRm2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == 'BA-CL-2026-900002')]").exists())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == 'BA-CL-2026-900001')]").doesNotExist());
    }

    // =========================================================================
    // 3. Client Meeting Scheduling & Update Tests
    // =========================================================================

    @Test
    @DisplayName("3.1 RM can schedule meeting, and unauthorized RM is denied")
    void test3_1_scheduleMeetingOwnership() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900003")
                .clientName("Meeting Test Client")
                .mobileNumber("9333333333")
                .speciality("Oncology")
                .location("Delhi")
                .clinicAddress("Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        CreateClientMeetingRequest req = CreateClientMeetingRequest.builder()
                .clientCode(client.getClientCode())
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(2))
                .meetingTime(LocalTime.of(14, 0))
                .meetingLocation("Hospital OPD")
                .build();

        // otherRmUser attempts to schedule meeting for rmUser's client -> 403 Forbidden
        UserDetails otherPrincipal = customUserDetailsService.loadUserByUsername(otherRmUser.getEmployeeCode());
        mockMvc.perform(post("/v1/client-meetings")
                        .with(user(otherPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        // rmUser schedules successfully -> 201 Created
        UserDetails principal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        mockMvc.perform(post("/v1/client-meetings")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.meetingCode").isNotEmpty())
                .andExpect(jsonPath("$.data.meetingStatus").value("SCHEDULED"));

        // Duplicate active scheduled meeting rejected
        mockMvc.perform(post("/v1/client-meetings")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("3.2 RM Conducted meeting update enforces mandatory geo-location and max 1 month follow-up")
    void test3_2_rmConductedMeetingUpdateValidation() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900004")
                .clientName("Geo Test Client")
                .mobileNumber("9444444444")
                .speciality("Orthopedics")
                .location("Delhi")
                .clinicAddress("Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-2026-900001")
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(10, 0))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .build());

        UserDetails principal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        // Conducted WITHOUT geo coordinates -> Rejected for RM
        ClientMeetingUpdateRequest noGeoReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("YES")
                .remarks("Positive discussion")
                .nextPlanDate(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noGeoReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Location coordinates")));

        // Conducted with follow-up exceeding 1 calendar month -> Rejected
        ClientMeetingUpdateRequest exceedMonthReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("YES")
                .remarks("Positive discussion")
                .nextPlanDate(LocalDate.now().plusMonths(2)) // Exceeds 1 month!
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exceedMonthReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cannot exceed one calendar month")));

        // Valid Conducted update with geo -> 200 OK
        ClientMeetingUpdateRequest validReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SOMEONE")
                .personName("Dr. Colleague")
                .position("RM")
                .businessGenerated("YES")
                .remarks("Meeting conducted successfully with co-worker")
                .nextPlanDate(LocalDate.now().plusDays(15))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(12.5)
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));

        // Verify verification record created in PENDING state
        ClientMeetingVerification verif = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId()).orElseThrow();
        assertThat(verif.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(verif.getAttemptNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("3.3 SC Conducted meeting update allows optional geo-location")
    void test3_3_scConductedMeetingOptionalGeo() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900005")
                .clientName("SC Client")
                .mobileNumber("9555555555")
                .speciality("General")
                .location("Noida")
                .clinicAddress("Sector 18")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(scUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-2026-900002")
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(scUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(11, 0))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .build());

        UserDetails principal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());

        // SC update WITHOUT geo -> Success (Geo is optional for SC)
        ClientMeetingUpdateRequest req = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("SC conducted meeting without geo")
                .nextPlanDate(LocalDate.now().plusDays(20))
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"));
    }

    // =========================================================================
    // 4. PC Client Meeting Verification, Rejection & Resubmission Tests
    // =========================================================================

    @Test
    @DisplayName("4.1 PC verifies conducted meeting with conditional investment questions when Business Generated = YES")
    void test4_1_pcVerificationConductedBusinessGeneratedYes() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900006")
                .clientName("PC Test Client")
                .mobileNumber("9666666666")
                .speciality("Dermatology")
                .location("Delhi")
                .clinicAddress("Address")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-2026-900003")
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("YES")
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(meeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // Missing investment questions when Business Generated = YES -> Rejected
        ClientMeetingVerificationRequest missingInvReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(15, 30))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("Verified")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingInvReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Investment Type is required")));

        // Valid verification with investment details
        ClientMeetingVerificationRequest validReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(15, 30))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName("Rahul Sharma")
                .position("RM") // Valid position: SM, RM, TL, ADMIN, SUPER ADMIN
                .investmentType("New SIP")
                .investmentAmount(new BigDecimal("25000.00"))
                .remarks("Doctor verified, 25k SIP booked")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.investmentAmount").value(25000.00));

        // Verify meeting verified_by_pc is true
        ClientMeeting updatedMeeting = clientMeetingRepository.findById(meeting.getId()).orElseThrow();
        assertThat(updatedMeeting.getVerifiedByPc()).isTrue();

        // Verify client nextFollowupDate advanced and investmentType saved
        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(updatedClient.getInvestmentType()).isEqualTo("New SIP");
        assertThat(updatedClient.getNextFollowupDate()).isEqualTo(LocalDate.now().plusMonths(1));
    }

    @Test
    @DisplayName("4.2 PC rejects meeting with mandatory reason, and RM resubmits with attempt history preserved")
    void test4_2_pcRejectionAndResubmissionLifecycle() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-2026-900007")
                .clientName("Rejection Lifecycle Client")
                .mobileNumber("9777777777")
                .speciality("Psychiatry")
                .location("Delhi")
                .clinicAddress("Address")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-2026-900004")
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("NO")
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(meeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // Step 1: PC Rejects meeting with reason
        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "Doctor stated meeting was not conducted in clinic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Doctor stated meeting was not conducted in clinic"));

        // Verify attempt 1 is retained as REJECTED in database
        assertThat(clientMeetingVerificationRepository.countByClientMeetingId(meeting.getId())).isEqualTo(1);

        // Step 2: RM resubmits meeting update with corrections
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        ClientMeetingUpdateRequest correctedReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Re-visited doctor and verified discussion")
                .nextPlanDate(LocalDate.now().plusDays(10))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(15.0)
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctedReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));

        // Step 3: Verify attempt 2 was created and attempt 1 is preserved
        assertThat(clientMeetingVerificationRepository.countByClientMeetingId(meeting.getId())).isEqualTo(2);

        ClientMeetingVerification attempt1 = clientMeetingVerificationRepository
                .findByClientMeetingIdOrderByAttemptNumberAsc(meeting.getId()).get(0);
        assertThat(attempt1.getAttemptNumber()).isEqualTo(1);
        assertThat(attempt1.getVerificationStatus()).isEqualTo(VerificationStatus.REJECTED);
        assertThat(attempt1.getIsCurrent()).isFalse();

        ClientMeetingVerification attempt2 = clientMeetingVerificationRepository
                .findByClientMeetingIdOrderByAttemptNumberAsc(meeting.getId()).get(1);
        assertThat(attempt2.getAttemptNumber()).isEqualTo(2);
        assertThat(attempt2.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(attempt2.getIsCurrent()).isTrue();

        // Step 4: PC queries history endpoint
        mockMvc.perform(get("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/history")
                        .with(user(pcPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data[1].verificationStatus").value("PENDING"));
    }

    @Test
    @DisplayName("4.5 PC verification supports multi-select business types, persists on meeting and verification, and does not duplicate meetings")
    void test4_5_pcVerificationMultiSelectBusinessTypesSupported() throws Exception {
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // Step 1: Schedule physical client meeting
        String clientCode = "BA-CL-PCMULTI-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        Client client = clientRepository.save(Client.builder()
                .clientCode(clientCode)
                .clientName("Dr. Multi Business Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .client(client)
                .meetingCode("BA-CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .meetingNumber(1)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(11, 0))
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("YES")
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(meeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        long meetingCountBefore = clientMeetingRepository.count();

        // Step 2: PC submits verification with multiple business types (SIP, INSURANCE)
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(11, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessTypes(List.of("SIP", "INSURANCE"))
                .investmentAmount(new BigDecimal("250000"))
                .remarks("Conducted successfully with multiple businesses")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.businessGenerated").value("YES"))
                .andExpect(jsonPath("$.data.businessTypes[0]").value("SIP"))
                .andExpect(jsonPath("$.data.businessTypes[1]").value("INSURANCE"));

        // Step 3: Verify meeting record is NOT duplicated
        long meetingCountAfter = clientMeetingRepository.count();
        assertThat(meetingCountAfter).isEqualTo(meetingCountBefore);

        // Step 4: Verify entity persistence
        ClientMeeting updatedMeeting = clientMeetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(updatedMeeting.getBusinessTypes()).isEqualTo("SIP, INSURANCE");

        ClientMeetingVerification verification = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId()).orElseThrow();
        assertThat(verification.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(verification.getBusinessTypes()).isEqualTo("SIP, INSURANCE");

        // Step 5: Verify meeting detail endpoint returns the multi-select business types array
        mockMvc.perform(get("/v1/client-meetings/" + meeting.getMeetingCode())
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessTypes[0]").value("SIP"))
                .andExpect(jsonPath("$.data.businessTypes[1]").value("INSURANCE"));
    }

    @Test
    @DisplayName("4.6 PC verification supports combinations across all seven types (LUMP_SUM, SHARE, LOAN, BROKER_CHANGE, SERVICE_REQUEST)")
    void test4_6_pcVerificationSevenSupportedTypesAndCombinations() throws Exception {
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-7TYPES-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Seven Types Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .client(client)
                .meetingCode("BA-CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .meetingNumber(1)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(14, 0))
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("YES")
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(meeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        // Combination: LUMP_SUM, SHARE, LOAN
        ClientMeetingVerificationRequest verifyReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(14, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessTypes(List.of("LUMP_SUM", "SHARE", "LOAN"))
                .investmentAmount(new BigDecimal("1000000"))
                .remarks("Three distinct business types generated")
                .nextFollowupDate(LocalDate.now().plusMonths(2))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessTypes.length()").value(3))
                .andExpect(jsonPath("$.data.businessTypes[0]").value("LUMP_SUM"))
                .andExpect(jsonPath("$.data.businessTypes[1]").value("SHARE"))
                .andExpect(jsonPath("$.data.businessTypes[2]").value("LOAN"));
    }

    @Test
    @DisplayName("4.7 PC verification rejects unsupported business types with HTTP 400 Bad Request")
    void test4_7_pcVerificationUnsupportedBusinessTypeRejected() throws Exception {
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-BADTYPE-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Bad Type Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .client(client)
                .meetingCode("BA-CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .meetingNumber(1)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(14, 0))
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("YES")
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(meeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        ClientMeetingVerificationRequest badReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(14, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .businessTypes(List.of("SIP", "CRYPTO_INVESTMENT"))
                .investmentAmount(new BigDecimal("50000"))
                .remarks("Invalid type")
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unsupported business/service type")));
    }

    @Test
    @DisplayName("4.8 PC verification with Business Generated = NO requires no business types, clears them, and preserves single meeting")
    void test4_8_pcVerificationBusinessGeneratedNoRequiresNoBusinessTypes() throws Exception {
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-BGNO-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. No Business Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .client(client)
                .meetingCode("BA-CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .meetingNumber(1)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(16, 0))
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("NO")
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(meeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        long countBefore = clientMeetingRepository.count();

        ClientMeetingVerificationRequest noBgReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(16, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("No business generated at this time")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noBgReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.businessGenerated").value("NO"))
                .andExpect(jsonPath("$.data.businessTypes").isEmpty());

        long countAfter = clientMeetingRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);

        ClientMeeting updatedMeeting = clientMeetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(updatedMeeting.getBusinessGenerated()).isEqualTo("NO");
        assertThat(updatedMeeting.getBusinessTypes()).isNull();
    }

    // =========================================================================
    // 5. Concurrency Protection & Batch Performance Tests
    // =========================================================================

    @Test
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    @DisplayName("5.1 Concurrent meeting scheduling requests for the same client are serialized; exactly 1 active meeting is created")
    void test5_1_concurrentSchedulingProtection() throws Exception {
        String clientCode = "BA-CL-CONC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Client client = clientRepository.save(Client.builder()
                .clientCode(clientCode)
                .clientName("Concurrent Test Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .speciality("Oncology")
                .location("Delhi")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        int threadCount = 4;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch readyLatch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger conflictCount = new java.util.concurrent.atomic.AtomicInteger(0);

        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        CreateClientMeetingRequest req = CreateClientMeetingRequest.builder()
                                .clientCode(clientCode)
                                .meetingMode(MeetingMode.PHYSICAL)
                                .meetingDate(LocalDate.now().plusDays(2))
                                .meetingTime(LocalTime.of(10, 30))
                                .meetingLocation("Clinic")
                                .remarks("Concurrent scheduling attempt")
                                .build();

                        clientMeetingService.scheduleMeeting(req, rmUser.getEmployeeCode());
                        successCount.incrementAndGet();
                    } catch (BadRequestException ex) {
                        if (ex.getMessage().contains("already exists")) {
                            conflictCount.incrementAndGet();
                        }
                    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                        conflictCount.incrementAndGet();
                    } catch (Exception ex) {
                        System.err.println("Unexpected concurrency error: " + ex.getClass().getName() + " - " + ex.getMessage());
                    }
                });
            }

            readyLatch.await();
            startLatch.countDown();
            executor.shutdown();
            executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

            // Strict assertion: exactly 1 succeeds, all other 3 competing requests are blocked and rejected
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(conflictCount.get()).isEqualTo(threadCount - 1);

            // Database state verification: client has exactly 1 scheduled meeting
            long scheduledCount = clientMeetingRepository.countByClientIdAndMeetingStatus(client.getId(), MeetingStatus.SCHEDULED);
            assertThat(scheduledCount).isEqualTo(1);
        } finally {
            clientMeetingRepository.findByClientId(client.getId(), org.springframework.data.domain.Pageable.unpaged())
                    .forEach(m -> clientMeetingRepository.deleteById(m.getId()));
            clientRepository.deleteById(client.getId());
        }
    }

    @Test
    @DisplayName("5.2 Work Follow batch lookup correctly associates active scheduled meetings across multiple page sizes")
    void test5_2_workFollowBatchLookupPerformance() throws Exception {
        // Create 3 clients assigned to scUser, 2 with active meetings, 1 without
        Client c1 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-BATCH-1-" + UUID.randomUUID().toString().substring(0, 4))
                .clientName("Batch Client 1")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(scUser)
                .build());

        Client c2 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-BATCH-2-" + UUID.randomUUID().toString().substring(0, 4))
                .clientName("Batch Client 2")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(scUser)
                .build());

        Client c3 = clientRepository.save(Client.builder()
                .clientCode("BA-CL-BATCH-3-" + UUID.randomUUID().toString().substring(0, 4))
                .clientName("Batch Client 3")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(scUser)
                .build());

        clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-B1-" + UUID.randomUUID().toString().substring(0, 4))
                .meetingNumber(1)
                .client(c1)
                .assignedEmployee(scUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .verifiedByPc(false)
                .build());

        clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-B2-" + UUID.randomUUID().toString().substring(0, 4))
                .meetingNumber(1)
                .client(c2)
                .assignedEmployee(scUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(3))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .verifiedByPc(false)
                .build());

        // Call getClientWorkFollow with page size 10
        org.springframework.data.domain.PageRequest pageReq = org.springframework.data.domain.PageRequest.of(0, 10);
        var response = clientMeetingService.getClientWorkFollow(scUser.getEmployeeCode(), pageReq, null, "ACTIVE", null);

        assertThat(response.getContent()).isNotEmpty();
        var client1Resp = response.getContent().stream().filter(r -> r.getClientId().equals(c1.getId())).findFirst().orElseThrow();
        assertThat(client1Resp.getActiveMeetingCode()).isNotNull();
        assertThat(client1Resp.getActiveMeetingStatus()).isEqualTo("SCHEDULED");

        var client3Resp = response.getContent().stream().filter(r -> r.getClientId().equals(c3.getId())).findFirst().orElseThrow();
        assertThat(client3Resp.getActiveMeetingCode()).isNull();
        assertThat(client3Resp.getActiveMeetingStatus()).isNull();
    }

    // =========================================================================
    // 6. Assignment Dates Tests (Today & Future Allowed, Past Rejected, Separate assignedAt)
    // =========================================================================

    @Test
    @DisplayName("6.1 Today's assignment date is accepted and assignedAt remains separate audit timestamp")
    void test6_1_todayAssignmentDateAccepted() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        LocalDate today = LocalDate.now();

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Today Assign")
                .mobileNumber(mobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Apollo")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .assignmentDate(today)
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignmentDate").value(today.toString()))
                .andExpect(jsonPath("$.data.assignedAt").isNotEmpty());

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getAssignmentDate()).isEqualTo(today);
        assertThat(client.getAssignedAt()).isNotNull();

        var histories = clientAssignmentHistoryRepository.findByClientIdOrderByAssignedAtDesc(client.getId());
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getAssignmentDate()).isEqualTo(today);
        assertThat(histories.get(0).getAssignedAt()).isNotNull();
    }

    @Test
    @DisplayName("6.2 Future assignment date (e.g. 20 Oct 2026) is accepted and persisted correctly")
    void test6_2_futureAssignmentDateAccepted() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        LocalDate futureDate = LocalDate.now().plusDays(10);

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Future Assign")
                .mobileNumber(mobile)
                .speciality("Neurology")
                .location("Noida")
                .clinicAddress("Fortis")
                .salesPersonEmployeeCode(scUser.getEmployeeCode())
                .assignmentDate(futureDate)
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.assignmentDate").value(futureDate.toString()))
                .andExpect(jsonPath("$.data.assignedSalesPersonCode").value(scUser.getEmployeeCode()));

        Client client = clientRepository.findByMobileNumber(mobile).orElseThrow();
        assertThat(client.getAssignmentDate()).isEqualTo(futureDate);
        assertThat(client.getAssignedAt()).isNotNull();

        // Assignment history has the future effective date
        var histories = clientAssignmentHistoryRepository.findByClientIdOrderByAssignedAtDesc(client.getId());
        assertThat(histories.get(0).getAssignmentDate()).isEqualTo(futureDate);
    }

    @Test
    @DisplayName("6.3 Past assignment date is rejected by Client-specific validator with HTTP 400 Bad Request")
    void test6_3_pastAssignmentDateRejected() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        LocalDate pastDate = LocalDate.now().minusDays(1);

        CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Past Assign")
                .mobileNumber(mobile)
                .speciality("Dentistry")
                .location("Delhi")
                .clinicAddress("Clinic")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .assignmentDate(pastDate) // In the past!
                .build();

        UserDetails principal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(post("/v1/clients/register")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Assignment date cannot be in the past")));
    }

    @Test
    @DisplayName("6.4 Future-dated Client appears in the assigned RM/SC Work Follow without auto-created meeting")
    void test6_4_futureDatedClientAppearsInWorkFollowWithoutAutoMeeting() throws Exception {
        String mobile = "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
        LocalDate futureAssignDate = LocalDate.now().plusDays(5);

        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-FUT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Future Work Follow")
                .mobileNumber(mobile)
                .speciality("Orthopedics")
                .location("Delhi")
                .clinicAddress("Care Clinic")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .assignmentDate(futureAssignDate)
                .assignedAt(java.time.LocalDateTime.now())
                .build());

        // Zero meetings auto-created
        assertThat(clientMeetingRepository.countByClientIdAndMeetingStatus(client.getId(), MeetingStatus.SCHEDULED)).isEqualTo(0);

        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        mockMvc.perform(get("/v1/clients/work-follow")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == '" + client.getClientCode() + "')]").exists())
                .andExpect(jsonPath("$.data.content[?(@.clientCode == '" + client.getClientCode() + "')].assignmentDate").value(futureAssignDate.toString()))
                .andExpect(jsonPath("$.data.content[?(@.clientCode == '" + client.getClientCode() + "')].activeMeetingCode", org.hamcrest.Matchers.contains((Object) null)));
    }

    // =========================================================================
    // 7. Actual Meetings Before Assignment Date & PC Verification Tests
    // =========================================================================

    @Test
    @DisplayName("7.1 Client assigned for future date (20 Oct) allows conducted meeting update on earlier actual date (16 Oct)")
    void test7_1_earlyActualMeetingAcceptedBeforeAssignmentDate() throws Exception {
        LocalDate assignmentDate = LocalDate.of(2026, 10, 20);
        LocalDate actualMeetingDate = LocalDate.of(2026, 10, 16);

        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-EARLY-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Rahul Sharma Early Meeting")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Fortis")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .assignmentDate(assignmentDate)
                .assignedAt(java.time.LocalDateTime.now())
                .build());

        // Schedule meeting for client (initial scheduled date e.g. assignment date 20 Oct)
        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-EARLY-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(assignmentDate)
                .meetingTime(LocalTime.of(11, 0))
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .verifiedByPc(false)
                .build());

        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        // Submit conducted meeting update with ACTUAL meeting date: 16 Oct (earlier than 20 Oct assignmentDate)
        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(actualMeetingDate) // 16 Oct: earlier than 20 Oct assignmentDate!
                .meetingTime(LocalTime.of(14, 30))
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Met Dr. Rahul Sharma on 16 Oct ahead of planned schedule")
                .nextPlanDate(LocalDate.now().plusDays(20))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meetingDate").value("2026-10-16"))
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));

        // Verify Database: Meeting has 16 Oct, Client preserved 20 Oct assignmentDate
        ClientMeeting updatedMeeting = clientMeetingRepository.findById(meeting.getId()).orElseThrow();
        assertThat(updatedMeeting.getMeetingDate()).isEqualTo(actualMeetingDate);

        Client reloadedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloadedClient.getAssignmentDate()).isEqualTo(assignmentDate); // Preserved as 20 Oct!

        var auditUpdates = clientMeetingUpdateRepository.findByClientMeetingIdOrderByUpdateNumberAsc(meeting.getId());
        assertThat(auditUpdates).hasSize(1);
        assertThat(auditUpdates.get(0).getMeetingDate()).isEqualTo(actualMeetingDate); // 16 Oct!
    }

    @Test
    @DisplayName("7.2 Early actual meeting (16 Oct) proceeds through PC verification; assignment date remains 20 Oct and meeting date remains 16 Oct")
    void test7_2_earlyActualMeetingPcVerification() throws Exception {
        LocalDate assignmentDate = LocalDate.of(2026, 10, 20);
        LocalDate actualMeetingDate = LocalDate.of(2026, 10, 16);

        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-PCVER-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Early Verif Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .speciality("Neurology")
                .location("Delhi")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .assignmentDate(assignmentDate)
                .assignedAt(java.time.LocalDateTime.now())
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-PCVER-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(actualMeetingDate)
                .meetingTime(LocalTime.of(15, 0))
                .meetingStatus(MeetingStatus.COMPLETED)
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("NO")
                .verifiedByPc(false)
                .build());

        clientMeetingVerificationRepository.save(ClientMeetingVerification.builder()
                .clientMeeting(meeting)
                .attemptNumber(1)
                .isCurrent(true)
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        ClientMeetingVerificationRequest verifReq = ClientMeetingVerificationRequest.builder()
                .newMeetingDate(actualMeetingDate)
                .meetingTime(LocalTime.of(15, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .remarks("PC verified actual meeting held on 16 Oct")
                .nextFollowupDate(LocalDate.now().plusMonths(1))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meeting.getMeetingCode() + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

        // Assert strictly: Assignment date remains 20 Oct, Meeting date remains 16 Oct
        Client reloadedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(reloadedClient.getAssignmentDate()).isEqualTo(assignmentDate);

        ClientMeeting verifiedMeeting = clientMeetingRepository.findById(meeting.getId()).orElseThrow();
        assertThat(verifiedMeeting.getMeetingDate()).isEqualTo(actualMeetingDate);
        assertThat(verifiedMeeting.getVerifiedByPc()).isTrue();
    }

    @Test
    @DisplayName("7.3 Unauthorized and wrong-assignee meeting update remains strictly rejected")
    void test7_3_unauthorizedMeetingUpdateRejected() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-AUTH-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Auth Guard Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .assignmentDate(LocalDate.now().plusDays(3))
                .build());

        ClientMeeting meeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-AUTH-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(rmUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .build());

        UserDetails otherRmPrincipal = customUserDetailsService.loadUserByUsername(otherRmUser.getEmployeeCode());

        ClientMeetingUpdateRequest updateReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Attempted update by other RM")
                .nextPlanDate(LocalDate.now().plusDays(10))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/client-meetings/" + meeting.getMeetingCode() + "/update")
                        .with(user(otherRmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 8. Secure KYC Document Upload, Attachment, Download & Authorization Tests
    // =========================================================================

    @Test
    @DisplayName("8.1 Document upload endpoint accepts valid PDF and JPEG with genuine magic bytes")
    void test8_1_validDocumentUploadSuccess() throws Exception {
        byte[] pdfContent = "%PDF-1.4 mock valid pdf document content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "pan_kyc.pdf", "application/pdf", pdfContent);

        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(pdfFile)
                        .param("documentType", "PAN_KYC")
                        .with(user(crmPrincipal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentReference").isNotEmpty())
                .andExpect(jsonPath("$.data.fileName").value("pan_kyc.pdf"))
                .andExpect(jsonPath("$.data.documentType").value("PAN_KYC"))
                .andExpect(jsonPath("$.data.uploadStatus").value("SUCCESS"));

        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        MockMultipartFile jpgFile = new MockMultipartFile(
                "file", "application_form.jpg", "image/jpeg", jpegContent);

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(jpgFile)
                        .param("documentType", "APPLICATION_FORM")
                        .with(user(crmPrincipal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentType").value("APPLICATION_FORM"));
    }

    @Test
    @DisplayName("8.2 Invalid file extension (.exe) and spoofed file signature are rejected with HTTP 400 Bad Request")
    void test8_2_invalidAndSpoofedFilesRejected() throws Exception {
        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Forbidden extension (.exe)
        MockMultipartFile exeFile = new MockMultipartFile(
                "file", "malicious.exe", "application/octet-stream", "MZ dummy binary content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(exeFile)
                        .param("documentType", "PAN_KYC")
                        .with(user(crmPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unsupported file type")));

        // Spoofed PDF (has .pdf name but content is plain text, not starting with %PDF-)
        MockMultipartFile fakePdfFile = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf", "This is plain text pretending to be PDF".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(fakePdfFile)
                        .param("documentType", "PAN_KYC")
                        .with(user(crmPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid file signature")));
    }

    @Test
    @DisplayName("8.3 Oversized file exceeding 10MB is rejected with HTTP 400 Bad Request")
    void test8_3_oversizedFileRejected() throws Exception {
        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());

        // Create dummy file > 10MB
        byte[] oversizedContent = new byte[10 * 1024 * 1024 + 1024];
        // Set valid PDF magic bytes at header to test size limit specifically
        System.arraycopy("%PDF-".getBytes(StandardCharsets.UTF_8), 0, oversizedContent, 0, 5);

        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "huge.pdf", "application/pdf", oversizedContent);

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(largeFile)
                        .param("documentType", "PAN_KYC")
                        .with(user(crmPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("exceeds maximum allowed limit of 10MB")));
    }

    @Test
    @DisplayName("8.4 Multiple documents can be attached and retrieved for a registered Client")
    void test8_4_multipleDocumentsAttachedAndRetrieved() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-DOCS-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Multi Docs Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .speciality("Cardiology")
                .location("Delhi")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        // Attach Document 1: PAN_KYC
        byte[] pdfBytes = "%PDF-1.4 pan content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file1 = new MockMultipartFile("file", "pan.pdf", "application/pdf", pdfBytes);

        mockMvc.perform(multipart("/v1/clients/" + client.getClientCode() + "/documents")
                        .file(file1)
                        .param("documentType", "PAN_KYC")
                        .param("documentName", "Doctor PAN Card")
                        .param("remarks", "Clear copy")
                        .with(user(rmPrincipal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentType").value("PAN_KYC"))
                .andExpect(jsonPath("$.data.documentReference").isNotEmpty());

        // Attach Document 2: NOMINEE_DOCUMENTS
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        MockMultipartFile file2 = new MockMultipartFile("file", "nominee_id.jpg", "image/jpeg", jpegBytes);

        mockMvc.perform(multipart("/v1/clients/" + client.getClientCode() + "/documents")
                        .file(file2)
                        .param("documentType", "NOMINEE_DOCUMENTS")
                        .param("documentName", "Nominee Aadhar")
                        .with(user(rmPrincipal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.documentType").value("NOMINEE_DOCUMENTS"));

        // Retrieve client documents
        mockMvc.perform(get("/v1/clients/" + client.getClientCode() + "/documents")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("8.5 Cross-client file access is denied: Unauthorized RM cannot download another RM's client document")
    void test8_5_crossClientFileAccessDenied() throws Exception {
        // Client A assigned to rmUser
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-CROSS-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Protected Files Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        // Upload and attach genuine test file using real storage service
        byte[] originalBytes = "%PDF-1.4 confidential KYC doctor tax returns".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile testPdf = new MockMultipartFile("file", "Confidential_PAN.pdf", "application/pdf", originalBytes);

        ClientDocumentResponse doc = clientDocumentService.attachDocumentToClient(
                client.getClientCode(), testPdf, ClientDocumentType.PAN_KYC, "Confidential_PAN.pdf", "Strictly Private", true, rmUser.getEmployeeCode());

        // otherRmUser attempts to download rmUser's client's document -> 403 Forbidden!
        UserDetails otherRmPrincipal = customUserDetailsService.loadUserByUsername(otherRmUser.getEmployeeCode());

        mockMvc.perform(get("/v1/clients/" + client.getClientCode() + "/documents/" + doc.getId() + "/download")
                        .with(user(otherRmPrincipal)))
                .andExpect(status().isForbidden());

        // Authorized assigned RM can download successfully -> 200 OK with EXACT BYTES
        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        byte[] downloadedBytes = mockMvc.perform(get("/v1/clients/" + client.getClientCode() + "/documents/" + doc.getId() + "/download")
                        .with(user(rmPrincipal)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string(
                        "Content-Disposition", org.hamcrest.Matchers.containsString("Confidential_PAN.pdf")))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(downloadedBytes).isEqualTo(originalBytes);

        // Path tampering defense: Client B cannot be used to download Client A's document ID -> 404 Not Found
        Client clientB = clientRepository.save(Client.builder()
                .clientCode("BA-CL-CROSS-B-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Dr. Other Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        mockMvc.perform(get("/v1/clients/" + clientB.getClientCode() + "/documents/" + doc.getId() + "/download")
                        .with(user(rmPrincipal)))
                .andExpect(status().isNotFound());

        // CRM Onboarding can download successfully -> 200 OK
        UserDetails crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
        mockMvc.perform(get("/v1/clients/" + client.getClientCode() + "/documents/" + doc.getId() + "/download")
                        .with(user(crmPrincipal)))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // 9. Direct Client Meeting Update Tests (Work Follow Direct Submission)
    // =========================================================================

    @Test
    @DisplayName("9.1 First direct Client meeting update: creates meeting #1 and queues PC verification without prior scheduling")
    void test9_1_firstDirectClientMeetingUpdate() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-DIRECT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Direct Update Client 1")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .location("Delhi")
                .clinicAddress("Apollo Hospital OPD")
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        // Ensure NO meetings exist for this client
        assertThat(clientMeetingRepository.findByClientId(client.getId(), Pageable.unpaged()).getContent()).isEmpty();

        UserDetails principal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        ClientMeetingUpdateRequest request = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("YES")
                .remarks("First direct meeting conducted from Work Follow successfully")
                .nextPlanDate(LocalDate.now().plusDays(10))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(15.0)
                .build();

        // Direct update via POST /v1/clients/{clientCode}/meetings/update -> 200 OK
        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingCode").isNotEmpty())
                .andExpect(jsonPath("$.data.meetingNumber").value(1))
                .andExpect(jsonPath("$.data.clientCode").value(client.getClientCode()))
                .andExpect(jsonPath("$.data.meetingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.meetingConducted").value("CONDUCTED"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));

        // Verify database: 1 meeting created, 1 update audit record, 1 verification attempt in PENDING
        List<ClientMeeting> meetings = clientMeetingRepository.findByClientId(client.getId(), Pageable.unpaged()).getContent();
        assertThat(meetings).hasSize(1);
        ClientMeeting meeting = meetings.get(0);
        assertThat(meeting.getMeetingNumber()).isEqualTo(1);
        assertThat(meeting.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(meeting.getMeetingLocation()).isEqualTo("Apollo Hospital OPD");

        int updatesCount = clientMeetingUpdateRepository.countByClientMeetingId(meeting.getId());
        assertThat(updatesCount).isEqualTo(1);

        ClientMeetingVerification verif = clientMeetingVerificationRepository
                .findByClientMeetingIdAndIsCurrentTrue(meeting.getId()).orElseThrow();
        assertThat(verif.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
        assertThat(verif.getAttemptNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("9.2 Direct Client meeting update with existing SCHEDULED meeting updates the same meeting without creating duplicate")
    void test9_2_directClientMeetingUpdateWithExistingScheduled() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-DIRECT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Direct Update Client 2")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(scUser)
                .build());

        // Pre-existing scheduled meeting
        ClientMeeting scheduledMeeting = clientMeetingRepository.save(ClientMeeting.builder()
                .meetingCode("BA-CLM-SCHEDULED-123")
                .meetingNumber(1)
                .client(client)
                .assignedEmployee(scUser)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .meetingStatus(MeetingStatus.SCHEDULED)
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .build());

        UserDetails principal = customUserDetailsService.loadUserByUsername(scUser.getEmployeeCode());

        ClientMeetingUpdateRequest request = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .meetingDate(LocalDate.now())
                .meetingTime(LocalTime.of(15, 30))
                .remarks("Client was busy with surgery; reschedule requested")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingCode").value(scheduledMeeting.getMeetingCode()))
                .andExpect(jsonPath("$.data.meetingStatus").value("NOT_CONDUCTED"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));

        // Total meetings for client remains exactly 1
        assertThat(clientMeetingRepository.findByClientId(client.getId(), Pageable.unpaged()).getContent()).hasSize(1);
    }

    @Test
    @DisplayName("9.3 Unauthorized RM cannot submit direct meeting update for another RM's client -> 403 Forbidden")
    void test9_3_directClientMeetingUpdate_unauthorizedForbidden() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-DIRECT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Protected RM Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        UserDetails otherPrincipal = customUserDetailsService.loadUserByUsername(otherRmUser.getEmployeeCode());

        ClientMeetingUpdateRequest request = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Unauthorized attempt")
                .nextPlanDate(LocalDate.now().plusDays(7))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(otherPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("9.4 Direct Client meeting update for non-existent client code returns 404 Not Found")
    void test9_4_directClientMeetingUpdate_nonExistentClient_returns404() throws Exception {
        UserDetails principal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        ClientMeetingUpdateRequest request = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Trying non-existent client")
                .nextPlanDate(LocalDate.now().plusDays(7))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/clients/BA-CL-NON-EXISTENT-99999/meetings/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("9.5 Direct Client meeting update validation: RM missing geo-location or invalid aloneWith is rejected with 400")
    void test9_5_directClientMeetingUpdate_validationFailures() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-DIRECT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Validation Test Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        UserDetails principal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        // 1. RM Conducted WITHOUT Geo -> 400 Bad Request
        ClientMeetingUpdateRequest noGeoReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("YES")
                .remarks("Missing coordinates")
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noGeoReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Location coordinates")));

        // 2. aloneWith SOMEONE without personName / position -> 400 Bad Request
        ClientMeetingUpdateRequest missingPersonReq = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SOMEONE")
                .businessGenerated("YES")
                .remarks("Met someone without details")
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingPersonReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Person name is required")));
    }

    @Test
    @DisplayName("9.6 Duplicate direct meeting update retry while already PENDING PC verification is rejected with 400")
    void test9_6_duplicateDirectUpdateWhilePendingVerification_rejected() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-DIRECT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Pending Duplicate Test Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        UserDetails principal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());

        ClientMeetingUpdateRequest request = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("YES")
                .remarks("Initial meeting update")
                .nextPlanDate(LocalDate.now().plusDays(10))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        // First update -> 200 OK
        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Duplicate retry before PC verifies -> 400 Bad Request
        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("pending PC verification")));
    }

    @Test
    @DisplayName("9.7 End-to-end Lifecycle: Direct Update -> PC Rejection -> Direct Update Resubmission -> PC Verification")
    void test9_7_directMeetingUpdate_fullRejectionAndVerificationLifecycle() throws Exception {
        Client client = clientRepository.save(Client.builder()
                .clientCode("BA-CL-DIRECT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .clientName("Lifecycle Test Client")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .build());

        UserDetails rmPrincipal = customUserDetailsService.loadUserByUsername(rmUser.getEmployeeCode());
        UserDetails pcPrincipal = customUserDetailsService.loadUserByUsername(pcUser.getEmployeeCode());

        // Step 1: RM submits first direct meeting update -> PENDING (Attempt #1)
        ClientMeetingUpdateRequest update1 = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("YES")
                .remarks("First submission")
                .nextPlanDate(LocalDate.now().plusDays(10))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        String response1Str = mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        String meetingCode = objectMapper.readTree(response1Str).path("data").path("meetingCode").asText();

        // Step 2: PC rejects meeting -> REJECTED (Attempt #1)
        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/reject")
                        .with(user(pcPrincipal))
                        .param("reason", "GPS coordinates mismatch with clinic location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("GPS coordinates mismatch with clinic location"));

        // Step 3: RM resubmits via direct update endpoint -> PENDING (Attempt #2)
        ClientMeetingUpdateRequest update2 = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("YES")
                .remarks("Corrected GPS submission with client at OPD")
                .nextPlanDate(LocalDate.now().plusDays(10))
                .latitude(new BigDecimal("28.6140"))
                .longitude(new BigDecimal("77.2091"))
                .accuracy(8.0)
                .build();

        mockMvc.perform(post("/v1/clients/" + client.getClientCode() + "/meetings/update")
                        .with(user(rmPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingCode").value(meetingCode))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"));

        // Verify verification attempts: Attempt 1 is non-current REJECTED, Attempt 2 is current PENDING
        List<ClientMeetingVerification> verifs = clientMeetingVerificationRepository
                .findByClientMeetingIdOrderByAttemptNumberAsc(
                        clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow().getId());
        assertThat(verifs).hasSize(2);
        assertThat(verifs.get(0).getAttemptNumber()).isEqualTo(1);
        assertThat(verifs.get(0).getIsCurrent()).isFalse();
        assertThat(verifs.get(0).getVerificationStatus()).isEqualTo(VerificationStatus.REJECTED);
        assertThat(verifs.get(1).getAttemptNumber()).isEqualTo(2);
        assertThat(verifs.get(1).getIsCurrent()).isTrue();
        assertThat(verifs.get(1).getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);

        // Step 4: PC verifies meeting with conditional questionnaire
        ClientMeetingVerificationRequest pcVerifyReq = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(11, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .businessGenerated("YES")
                .investmentType("New SIP")
                .investmentAmount(new BigDecimal("25000.00"))
                .remarks("Confirmed with client personally; SIP application in progress")
                .nextFollowupDate(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/v1/client-meetings/verification/" + meetingCode + "/verify")
                        .with(user(pcPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pcVerifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

        // Step 5: Verify meeting and client final states
        ClientMeeting finalMeeting = clientMeetingRepository.findByMeetingCode(meetingCode).orElseThrow();
        assertThat(finalMeeting.getVerifiedByPc()).isTrue();
        assertThat(finalMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);

        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(updatedClient.getInvestmentType()).isEqualTo("New SIP");
        assertThat(updatedClient.getNextFollowupDate()).isEqualTo(LocalDate.now().plusDays(10));
    }
}

