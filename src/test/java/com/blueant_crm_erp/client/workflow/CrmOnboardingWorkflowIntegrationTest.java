package com.blueant_crm_erp.client.workflow;

import com.blueant_crm_erp.client.dto.request.CrmClientAssignRequest;
import com.blueant_crm_erp.client.dto.request.CrmOnboardingRequest;
import com.blueant_crm_erp.client.dto.request.CrmVerificationRequest;
import com.blueant_crm_erp.client.dto.request.RecordClientFollowUpRequest;
import com.blueant_crm_erp.client.dto.response.*;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.CrmOnboarding;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.enums.CrmOnboardingStatus;
import com.blueant_crm_erp.client.repository.ClientFollowUpRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.repository.CrmOnboardingRepository;
import com.blueant_crm_erp.client.service.CrmService;
import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.LeadSource;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class CrmOnboardingWorkflowIntegrationTest {

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private ProcessCoordinatorService processCoordinatorService;

    @Autowired
    private CrmService crmService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CrmOnboardingRepository crmOnboardingRepository;

    @Autowired
    private ClientFollowUpRepository clientFollowUpRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String ADMIN_USER = "EMP000001";
    private static final String SECOND_USER = "EMP000002";

    @Test
    @DisplayName("A & B: CONVERTED_CLIENT reaches CRM Onboarding only after PC verification; WIP is strictly excluded")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "MEETING_VERIFY", "CRM", "SALES"})
    public void testConvertedClientEligibleOnlyAfterPcVerification() {
        // Step 1: Create Lead
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName("PC Flow Client");
        leadReq.setMobileNumber("91" + System.currentTimeMillis() % 100000000L);
        leadReq.setLeadSource(LeadSource.MANUAL);
        LeadResponse lead = leadService.createLead(leadReq, ADMIN_USER);

        // Step 2: Schedule & Conduct Meeting with CONVERTED_CLIENT
        CreateMeetingRequest scheduleReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Office")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meeting = meetingScheduleService.scheduleMeeting(scheduleReq, ADMIN_USER);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.CONVERTED_CLIENT)
                .remarks("Agreed to invest")
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();
        meetingService.processMeetingUpdateWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);

        // BEFORE PC Verification: Must NOT be in CRM queue
        List<CrmLeadQueueResponse> queueBefore = crmService.getCrmQueue(ADMIN_USER);
        assertThat(queueBefore.stream().noneMatch(q -> q.getLeadCode().equals(lead.getLeadCode()))).isTrue();

        // Direct onboarding attempt before PC verification must fail
        CrmOnboardingRequest onboardingReq = CrmOnboardingRequest.builder()
                .investorName("PC Flow Client")
                .paymentDone(false)
                .build();
        assertThatThrownBy(() -> crmService.saveOrSubmitOnboarding(lead.getLeadCode(), onboardingReq, ADMIN_USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Process Coordinator");

        // Step 3: PC Verification
        MeetingVerificationRequest pcVerifyReq = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.now())
                .meetingTiming(LocalTime.of(10, 0))
                .meetingWith("SELF")
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .profession("BUSINESS_OWNER")
                .remarks("PC verified for conversion")
                .build();
        processCoordinatorService.verifyMeeting(meeting.getMeetingCode(), pcVerifyReq, ADMIN_USER);

        // AFTER PC Verification: NOW eligible in CRM queue
        List<CrmLeadQueueResponse> queueAfter = crmService.getCrmQueue(ADMIN_USER);
        CrmLeadQueueResponse crmCandidate = queueAfter.stream()
                .filter(q -> q.getLeadCode().equals(lead.getLeadCode()))
                .findFirst()
                .orElse(null);

        assertThat(crmCandidate).isNotNull();
        assertThat(crmCandidate.getCrmOnboardingStatus()).isEqualTo("NOT_STARTED");
        assertThat(crmCandidate.getVerifiedMeetingCode()).isEqualTo(meeting.getMeetingCode());

        // Can get default pre-filled onboarding info
        CrmOnboardingResponse defaultOnboarding = crmService.getOnboarding(lead.getLeadCode(), ADMIN_USER);
        assertThat(defaultOnboarding.getInvestorName()).isEqualTo("PC Flow Client");
        assertThat(defaultOnboarding.getOnboardingStatus()).isEqualTo("NOT_STARTED");
        assertThat(defaultOnboarding.getCreatedBySalesPersonCode()).isEqualTo(ADMIN_USER);
    }

    @Test
    @DisplayName("D & E & F: CRM Onboarding Questions 1-28 saved with Payment=NO results in DRAFT state; Client is NOT created")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "MEETING_VERIFY", "CRM", "SALES"})
    public void testOnboardingPaymentNoResultsInDraft() {
        Lead lead = createAndPcVerifyConvertedLead("Draft Workflow Client");

        // Submit Questions 1 to 28 with Payment Done = NO
        CrmOnboardingRequest request = CrmOnboardingRequest.builder()
                .investorName("Draft Workflow Client")
                .isBlueantInvestor(true)
                .familyHead("Self")
                .occupation("Software Engineer")
                .panNumber("ABCDE1234F")
                .contactDetail("9876500001")
                .mailId("client@example.com")
                .correspondenceAddress("123 Street, City")
                .officeAddress("Tech Park, City")
                .placeOfBirth("New Delhi")
                .familyDetails("Spouse, 1 Child")
                .location("Delhi")
                .source("MANUAL")
                .sourceDescription("Referral")
                .applicationReceivedDate(LocalDate.now())
                .nomineeDetails("Spouse Name")
                .nomineePanOrAadhaar("AAAAA0000A")
                .motherName("Mother Name")
                .applicationMode("ONLINE")
                .firstInvestmentAmount(new BigDecimal("100000.00"))
                .expectedMaxSIP(new BigDecimal("25000.00"))
                .investmentType("SIP with Online")
                .allDocumentsCompleted(true)
                .investwellUserId("IW-10023")
                .helpdeskQueryNo("HD-5501")
                .clientReportedDate(LocalDate.now())
                .paymentDone(false) // Question 28: Payment Done = NO
                .remarks("Draft onboarding - waiting for cheque clearance")
                .build();

        CrmOnboardingResponse onboardingResponse = crmService.saveOrSubmitOnboarding(lead.getLeadCode(), request, ADMIN_USER);

        // Assertions for DRAFT state
        assertThat(onboardingResponse.getOnboardingStatus()).isEqualTo(CrmOnboardingStatus.DRAFT.name());
        assertThat(onboardingResponse.getPaymentDone()).isFalse();
        assertThat(onboardingResponse.getCreatedBySalesPersonCode()).isEqualTo(ADMIN_USER);
        assertThat(onboardingResponse.getInvestwellUserId()).isEqualTo("IW-10023");

        // Verify entity in repository
        CrmOnboarding savedEntity = crmOnboardingRepository.findByLeadId(lead.getId()).orElseThrow();
        assertThat(savedEntity.getOnboardingStatus()).isEqualTo(CrmOnboardingStatus.DRAFT);
        assertThat(savedEntity.getCreatedBySalesPerson().getEmployeeCode()).isEqualTo(ADMIN_USER);

        // Client must NOT be created yet
        assertThat(clientRepository.findByLeadId(lead.getId())).isEmpty();

        // Attempting CRM Verification on DRAFT record must fail
        CrmVerificationRequest verifyReq = CrmVerificationRequest.builder()
                .kycVerified(true)
                .bankDetailsVerified(true)
                .documentsVerified(true)
                .clientContactConfirmed(true)
                .build();

        assertThatThrownBy(() -> crmService.verifyCrm(lead.getLeadCode(), verifyReq, ADMIN_USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Payment is not done")
                .hasMessageContaining("DRAFT");
    }

    @Test
    @DisplayName("G & H: Payment=YES allows CRM Verification -> Active CLIENT created with ~3-Month Follow-Up")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "MEETING_VERIFY", "CRM", "SALES"})
    public void testOnboardingPaymentYesAllowsVerificationAndClientCreation() {
        Lead lead = createAndPcVerifyConvertedLead("Full Onboard Client");

        // Step 1: Submit Onboarding with Payment Done = YES
        CrmOnboardingRequest request = CrmOnboardingRequest.builder()
                .investorName("Full Onboard Client")
                .isBlueantInvestor(false)
                .occupation("Doctor")
                .panNumber("BCDEF2345G")
                .contactDetail("9876500002")
                .mailId("doctor@example.com")
                .correspondenceAddress("Clinic Road")
                .applicationReceivedDate(LocalDate.now())
                .firstInvestmentAmount(new BigDecimal("500000.00"))
                .expectedMaxSIP(new BigDecimal("50000.00"))
                .investmentType("SIP with Online")
                .allDocumentsCompleted(true)
                .paymentDone(true) // Question 28: Payment Done = YES
                .remarks("Payment received via RTGS")
                .build();

        CrmOnboardingResponse onboarding = crmService.saveOrSubmitOnboarding(lead.getLeadCode(), request, ADMIN_USER);
        assertThat(onboarding.getOnboardingStatus()).isEqualTo(CrmOnboardingStatus.PAYMENT_DONE.name());
        assertThat(onboarding.getPaymentDone()).isTrue();

        // Step 2: Perform CRM Verification
        CrmVerificationRequest verifyReq = CrmVerificationRequest.builder()
                .kycVerified(true)
                .bankDetailsVerified(true)
                .documentsVerified(true)
                .clientContactConfirmed(true)
                .remarks("All documents & payment verified")
                .build();

        CrmVerificationResponse verifyResponse = crmService.verifyCrm(lead.getLeadCode(), verifyReq, ADMIN_USER);
        assertThat(verifyResponse).isNotNull();
        assertThat(verifyResponse.getClientCode()).isNotNull();

        // Assert Client entity state
        Client client = clientRepository.findByLeadId(lead.getId()).orElseThrow();
        assertThat(client.getClientStatus()).isEqualTo(ClientStatus.ACTIVE);
        assertThat(client.getClientName()).isEqualTo("Full Onboard Client");
        assertThat(client.getPanNumber()).isEqualTo("BCDEF2345G");

        // Created By = Original Sales Person (ADMIN_USER)
        assertThat(client.getCreatedBySalesPerson()).isNotNull();
        assertThat(client.getCreatedBySalesPerson().getEmployeeCode()).isEqualTo(ADMIN_USER);

        // Assigned To = Original Sales Person (ADMIN_USER initially)
        assertThat(client.getSalesPerson()).isNotNull();
        assertThat(client.getSalesPerson().getEmployeeCode()).isEqualTo(ADMIN_USER);

        // Initial ~3-Month Follow-Up schedule check
        LocalDate expectedFollowUp = LocalDate.now().plusMonths(3);
        assertThat(client.getNextFollowupDate()).isEqualTo(expectedFollowUp);

        // Onboarding entity status is now VERIFIED
        CrmOnboarding updatedOnboarding = crmOnboardingRepository.findByLeadId(lead.getId()).orElseThrow();
        assertThat(updatedOnboarding.getOnboardingStatus()).isEqualTo(CrmOnboardingStatus.VERIFIED);
    }

    @Test
    @DisplayName("I & J & K & L: CRM Assignment changes Assigned To but NEVER overwrites Created By (Original Sales Person)")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "MEETING_VERIFY", "CRM", "SALES"})
    public void testCrmAssignmentPreservesCreatedBy() {
        Lead lead = createAndPcVerifyConvertedLead("Assignment Client");

        // Submit onboarding with Payment = YES
        CrmOnboardingRequest request = CrmOnboardingRequest.builder()
                .investorName("Assignment Client")
                .paymentDone(true)
                .build();
        crmService.saveOrSubmitOnboarding(lead.getLeadCode(), request, ADMIN_USER);

        // Verify lead to create Client
        CrmVerificationRequest verifyReq = CrmVerificationRequest.builder()
                .kycVerified(true)
                .bankDetailsVerified(true)
                .documentsVerified(true)
                .clientContactConfirmed(true)
                .build();
        CrmVerificationResponse verifyResp = crmService.verifyCrm(lead.getLeadCode(), verifyReq, ADMIN_USER);

        Client client = clientRepository.findByClientCode(verifyResp.getClientCode()).orElseThrow();
        assertThat(client.getCreatedBySalesPerson().getEmployeeCode()).isEqualTo(ADMIN_USER);
        assertThat(client.getSalesPerson().getEmployeeCode()).isEqualTo(ADMIN_USER);

        // Ensure second user exists
        User secondUser = getOrCreateSecondUser();

        // Reassign Client to second sales person (EMP000002)
        CrmClientAssignRequest assignReq = CrmClientAssignRequest.builder()
                .salesPersonCode(secondUser.getEmployeeCode())
                .remarks("Reassigned by CRM head to second RM")
                .build();

        ClientResponse assignResp = crmService.assignClient(client.getClientCode(), assignReq, ADMIN_USER);

        // CRITICAL ASSERTIONS:
        // Assigned To has changed to secondUser
        assertThat(assignResp.getSalesPersonCode()).isEqualTo(secondUser.getEmployeeCode());

        // Created By STILL remains original sales person (ADMIN_USER)!
        assertThat(assignResp.getCreatedBySalesPersonCode()).isEqualTo(ADMIN_USER);

        // Verify in database entity directly
        Client reloadedClient = clientRepository.findByClientCode(client.getClientCode()).orElseThrow();
        assertThat(reloadedClient.getSalesPerson().getEmployeeCode()).isEqualTo(secondUser.getEmployeeCode());
        assertThat(reloadedClient.getCreatedBySalesPerson().getEmployeeCode()).isEqualTo(ADMIN_USER);

        // Assign back to ADMIN_USER (same sales person)
        CrmClientAssignRequest reassignSameReq = CrmClientAssignRequest.builder()
                .salesPersonCode(ADMIN_USER)
                .build();
        ClientResponse reassignResp = crmService.assignClient(client.getClientCode(), reassignSameReq, ADMIN_USER);

        assertThat(reassignResp.getSalesPersonCode()).isEqualTo(ADMIN_USER);
        assertThat(reassignResp.getCreatedBySalesPersonCode()).isEqualTo(ADMIN_USER);
    }

    @Test
    @DisplayName("M: Client gets recurring ~3-month follow-up, and recording follow-up schedules the next ~3-month check-in")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "MEETING_VERIFY", "CRM", "SALES"})
    public void testRecurringThreeMonthClientFollowUp() {
        Lead lead = createAndPcVerifyConvertedLead("Recurring FollowUp Client");

        CrmOnboardingRequest request = CrmOnboardingRequest.builder()
                .investorName("Recurring FollowUp Client")
                .paymentDone(true)
                .build();
        crmService.saveOrSubmitOnboarding(lead.getLeadCode(), request, ADMIN_USER);

        CrmVerificationRequest verifyReq = CrmVerificationRequest.builder()
                .kycVerified(true)
                .bankDetailsVerified(true)
                .documentsVerified(true)
                .clientContactConfirmed(true)
                .build();
        CrmVerificationResponse verifyResp = crmService.verifyCrm(lead.getLeadCode(), verifyReq, ADMIN_USER);

        // Query follow-ups for Sales Person
        List<ClientFollowUpResponse> followUps = crmService.getClientFollowUps(ADMIN_USER, ADMIN_USER);
        ClientFollowUpResponse myFollowUp = followUps.stream()
                .filter(f -> f.getClientCode().equals(verifyResp.getClientCode()))
                .findFirst()
                .orElse(null);

        assertThat(myFollowUp).isNotNull();
        LocalDate initialFollowUp = LocalDate.now().plusMonths(3);
        assertThat(myFollowUp.getNextFollowupDate()).isEqualTo(initialFollowUp);

        // Record a completed follow-up
        LocalDate completedDate = LocalDate.now();
        RecordClientFollowUpRequest recordReq = RecordClientFollowUpRequest.builder()
                .followupDate(completedDate)
                .remarks("Conducted 3-month portfolio review with client. Satisfied with performance.")
                .build();

        ClientFollowUpRecordResponse recordResp = crmService.recordClientFollowUp(verifyResp.getClientCode(), recordReq, ADMIN_USER);

        // Next follow-up is automatically scheduled for ~3 months after the follow-up date
        LocalDate expectedNextDate = completedDate.plusMonths(3);
        assertThat(recordResp.getNextFollowupDate()).isEqualTo(expectedNextDate);
        assertThat(recordResp.getRemarks()).contains("portfolio review");

        // Verify Client entity is updated
        Client updatedClient = clientRepository.findByClientCode(verifyResp.getClientCode()).orElseThrow();
        assertThat(updatedClient.getNextFollowupDate()).isEqualTo(expectedNextDate);

        // Verify log in ClientFollowUp repository
        assertThat(clientFollowUpRepository.findByClientIdOrderByFollowupDateDesc(updatedClient.getId())).isNotEmpty();
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private Lead createAndPcVerifyConvertedLead(String clientName) {
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName(clientName);
        leadReq.setMobileNumber("92" + System.currentTimeMillis() % 100000000L);
        leadReq.setLeadSource(LeadSource.MANUAL);
        LeadResponse leadResp = leadService.createLead(leadReq, ADMIN_USER);

        CreateMeetingRequest scheduleReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(leadResp.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Office")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meeting = meetingScheduleService.scheduleMeeting(scheduleReq, ADMIN_USER);

        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.CONVERTED_CLIENT)
                .remarks("Client onboarded")
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();
        meetingService.processMeetingUpdateWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);

        MeetingVerificationRequest pcVerifyReq = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.now())
                .meetingTiming(LocalTime.of(10, 0))
                .meetingWith("SELF")
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .profession("SALARIED_EMPLOYEE")
                .remarks("PC verified for CRM handover")
                .build();
        processCoordinatorService.verifyMeeting(meeting.getMeetingCode(), pcVerifyReq, ADMIN_USER);

        return leadRepository.findByLeadCode(leadResp.getLeadCode()).orElseThrow();
    }

    private User getOrCreateSecondUser() {
        return userRepository.findByEmployeeCodeIgnoreCase(SECOND_USER)
                .orElseGet(() -> {
                    User second = User.builder()
                            .employeeCode(SECOND_USER)
                            .firstName("Second")
                            .lastName("SalesPerson")
                            .email("secondsp@blueant.com")
                            .mobileNumber("9988776655")
                            .password("Password@123")
                            .gender(com.blueant_crm_erp.common.enums.Gender.MALE)
                            .joiningDate(LocalDate.now())
                            .status(com.blueant_crm_erp.common.enums.Status.ACTIVE)
                            .accountEnabled(true)
                            .accountLocked(false)
                            .accountNonExpired(true)
                            .credentialsNonExpired(true)
                            .emailVerified(true)
                            .mobileVerified(true)
                            .build();
                    return userRepository.save(second);
                });
    }
}
