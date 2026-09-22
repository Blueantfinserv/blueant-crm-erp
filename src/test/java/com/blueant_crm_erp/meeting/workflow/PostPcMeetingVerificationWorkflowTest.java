package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.client.dto.request.CrmVerificationRequest;
import com.blueant_crm_erp.client.dto.response.ClientFollowUpResponse;
import com.blueant_crm_erp.client.dto.response.CrmLeadQueueResponse;
import com.blueant_crm_erp.client.dto.response.CrmVerificationResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.CrmService;
import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.request.LeadFilterRequest;
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
import com.blueant_crm_erp.meeting.dto.response.ActiveMeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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
public class PostPcMeetingVerificationWorkflowTest {

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private ProcessCoordinatorService processCoordinatorService;

    @Autowired
    private CrmService crmService;

    @Autowired
    private ClientRepository clientRepository;

    private static final String ADMIN_USER = "EMP000001";

    @Test
    @DisplayName("TEST 1 & TEST 2: Continuous WIP follow-up cycle (WIP -> Meeting -> PC Verification -> WIP -> Next Meeting -> PC Verification -> WIP)")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "MEETING_VERIFY"})
    public void testContinuousWipFollowUpCycle() {
        // Step 1: Create Lead
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName("WIP Cycle Client");
        leadReq.setMobileNumber("98" + System.currentTimeMillis() % 100000000L);
        leadReq.setLeadSource(LeadSource.MANUAL);
        LeadResponse lead = leadService.createLead(leadReq, ADMIN_USER);

        // Step 2: Schedule Intro Meeting
        CreateMeetingRequest scheduleReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Office")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse introMeeting = meetingScheduleService.scheduleMeeting(scheduleReq, ADMIN_USER);

        // Step 3: Conduct Meeting #1 with WIP and schedule next plan date
        MeetingWorkflowRequest workflowReq1 = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .remarks("First meeting discussion: positive")
                .nextPlanDate(LocalDate.now().plusDays(20)) // ~20-day prospect cycle
                .nextPlanTime(LocalTime.of(11, 0))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();
        meetingService.processMeetingUpdateWorkflow(introMeeting.getMeetingCode(), workflowReq1, ADMIN_USER);

        // Verify next sequential meeting was created (#2)
        ActiveMeetingResponse active1 = meetingService.getActiveMeetingByLeadId(lead.getUniqueLeadId());
        assertThat(active1.getMeetingCode()).isNotNull();
        Meeting meeting2 = meetingRepository.findByMeetingCode(active1.getMeetingCode()).orElseThrow();
        assertThat(meeting2.getMeetingNumber()).isEqualTo(2);
        assertThat(meeting2.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);

        // Step 4: PC Verifies Meeting #1
        MeetingVerificationRequest pcVerifyReq1 = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.now())
                .meetingTiming(LocalTime.of(10, 0))
                .meetingWith("SELF")
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .profession("SALARIED_EMPLOYEE")
                .remarks("PC verified meeting 1: confirmed WIP")
                .build();
        processCoordinatorService.verifyMeeting(introMeeting.getMeetingCode(), pcVerifyReq1, ADMIN_USER);

        // Post PC-Verification Assertion: Lead remains WORK_IN_PROGRESS, stage is FOLLOW_UP
        Lead leadAfterPc1 = leadRepository.findByLeadCode(lead.getLeadCode()).orElseThrow();
        assertThat(leadAfterPc1.getLeadStatus()).isEqualTo(LeadStatus.WORK_IN_PROGRESS);
        assertThat(leadAfterPc1.getLeadStage()).isEqualTo(LeadStage.FOLLOW_UP);

        // Ensure WIP did NOT go to CRM
        List<CrmLeadQueueResponse> crmQueue = crmService.getCrmQueue(ADMIN_USER);
        assertThat(crmQueue.stream().noneMatch(q -> q.getLeadCode().equals(lead.getLeadCode()))).isTrue();

        // Step 5: Conduct Meeting #2 with WIP and schedule next plan date (Continuous cycle)
        MeetingWorkflowRequest workflowReq2 = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .remarks("Second meeting discussion: still analyzing options")
                .nextPlanDate(LocalDate.now().plusDays(40))
                .nextPlanTime(LocalTime.of(15, 0))
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();
        meetingService.processMeetingUpdateWorkflow(meeting2.getMeetingCode(), workflowReq2, ADMIN_USER);

        // Verify next sequential meeting #3 was created
        ActiveMeetingResponse active2 = meetingService.getActiveMeetingByLeadId(lead.getUniqueLeadId());
        assertThat(active2.getMeetingCode()).isNotNull();
        Meeting meeting3 = meetingRepository.findByMeetingCode(active2.getMeetingCode()).orElseThrow();
        assertThat(meeting3.getMeetingNumber()).isEqualTo(3);
        assertThat(meeting3.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);

        // Step 6: PC Verifies Meeting #2
        MeetingVerificationRequest pcVerifyReq2 = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.now())
                .meetingTiming(LocalTime.of(11, 0))
                .meetingWith("SELF")
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .profession("SALARIED_EMPLOYEE")
                .remarks("PC verified meeting 2: confirmed ongoing WIP")
                .build();
        processCoordinatorService.verifyMeeting(meeting2.getMeetingCode(), pcVerifyReq2, ADMIN_USER);

        // Post PC-Verification Assertion: Continuous cycle preserved
        Lead leadAfterPc2 = leadRepository.findByLeadCode(lead.getLeadCode()).orElseThrow();
        assertThat(leadAfterPc2.getLeadStatus()).isEqualTo(LeadStatus.WORK_IN_PROGRESS);
        assertThat(leadAfterPc2.getLeadStage()).isEqualTo(LeadStage.FOLLOW_UP);

        // Meeting #3 remains available and scheduled for sales person
        assertThat(meeting3.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
    }

    @Test
    @DisplayName("TEST 3 & TEST 4: CONVERTED_CLIENT reaches CRM ONLY AFTER PC verification")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "MEETING_VERIFY", "CRM"})
    public void testConvertedClientRequiresPcVerificationBeforeCrm() {
        // Step 1: Create Lead
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName("Conversion Test Client");
        leadReq.setMobileNumber("97" + System.currentTimeMillis() % 100000000L);
        leadReq.setLeadSource(LeadSource.MANUAL);
        LeadResponse lead = leadService.createLead(leadReq, ADMIN_USER);

        // Step 2: Schedule Meeting
        CreateMeetingRequest scheduleReq = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Office")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meeting = meetingScheduleService.scheduleMeeting(scheduleReq, ADMIN_USER);

        // Step 3: Conduct Meeting with CONVERTED_CLIENT outcome
        MeetingWorkflowRequest workflowReq = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.CONVERTED_CLIENT)
                .remarks("Client decided to invest 5 Lakhs!")
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();
        meetingService.processMeetingUpdateWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);

        // TEST 4 ASSERTION: BEFORE PC Verification, lead MUST NOT be in CRM queue
        List<CrmLeadQueueResponse> queueBeforePc = crmService.getCrmQueue(ADMIN_USER);
        assertThat(queueBeforePc.stream().noneMatch(q -> q.getLeadCode().equals(lead.getLeadCode()))).isTrue();

        // Direct CRM verification attempt BEFORE PC verification must fail
        CrmVerificationRequest crmReq = CrmVerificationRequest.builder()
                .kycVerified(true)
                .bankDetailsVerified(true)
                .documentsVerified(true)
                .clientContactConfirmed(true)
                .panNumber("ABCDE1234F")
                .remarks("Direct verification attempt")
                .build();

        assertThatThrownBy(() -> crmService.verifyCrm(lead.getLeadCode(), crmReq, ADMIN_USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Process Coordinator");

        // Step 4: PC Verifies Meeting
        MeetingVerificationRequest pcVerifyReq = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.now())
                .meetingTiming(LocalTime.of(10, 0))
                .meetingWith("SELF")
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .profession("BUSINESS_OWNER")
                .remarks("PC verified: Client investment confirmed")
                .build();
        processCoordinatorService.verifyMeeting(meeting.getMeetingCode(), pcVerifyReq, ADMIN_USER);

        // TEST 3 ASSERTION: AFTER PC verification, lead IS NOW eligible in CRM queue
        List<CrmLeadQueueResponse> queueAfterPc = crmService.getCrmQueue(ADMIN_USER);
        CrmLeadQueueResponse crmCandidate = queueAfterPc.stream()
                .filter(q -> q.getLeadCode().equals(lead.getLeadCode()))
                .findFirst()
                .orElse(null);

        assertThat(crmCandidate).isNotNull();
        assertThat(crmCandidate.getVerifiedMeetingCode()).isEqualTo(meeting.getMeetingCode());
        assertThat(crmCandidate.getLeadStage()).isEqualTo(LeadStage.CRM_HANDOVER.name());
    }

    @Test
    @DisplayName("TEST 5 & TEST 6: CRM Questions + CRM Verification -> CLIENT created with original Sales Person and ~3-Month Follow-up")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "MEETING_VERIFY", "CRM", "SALES"})
    public void testCrmVerificationCreatesClientWithSalesPersonAndThreeMonthFollowUp() {
        // Step 1: Create Lead assigned to sales person
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName("Verified Client Relationship");
        leadReq.setMobileNumber("96" + System.currentTimeMillis() % 100000000L);
        leadReq.setLeadSource(LeadSource.MANUAL);
        LeadResponse lead = leadService.createLead(leadReq, ADMIN_USER);

        // Step 2: Schedule & Conduct Meeting
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
                .remarks("Client onboarded")
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();
        meetingService.processMeetingUpdateWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);

        // Step 3: PC Verification
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

        // Step 4: CRM Questions and CRM Verification (TEST 5)
        CrmVerificationRequest crmReq = CrmVerificationRequest.builder()
                .kycVerified(true)
                .bankDetailsVerified(true)
                .documentsVerified(true)
                .clientContactConfirmed(true)
                .panNumber("ABCDE1234F")
                .remarks("All KYC and bank documents verified successfully.")
                .build();

        CrmVerificationResponse crmResponse = crmService.verifyCrm(lead.getLeadCode(), crmReq, ADMIN_USER);

        assertThat(crmResponse).isNotNull();
        assertThat(crmResponse.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED.name());
        assertThat(crmResponse.getClientCode()).isNotNull();

        // Verify Client entity state
        Client client = clientRepository.findByLeadId(crmResponse.getLeadId()).orElseThrow();
        assertThat(client.getClientStatus()).isEqualTo(ClientStatus.ACTIVE);
        assertThat(client.getPanNumber()).isEqualTo("ABCDE1234F");

        // TEST 6 ASSERTION: Original Sales Person relationship retained
        assertThat(client.getSalesPerson()).isNotNull();
        assertThat(client.getSalesPerson().getEmployeeCode()).isEqualTo(ADMIN_USER);

        // ~3-Month Follow-Up schedule check
        LocalDate expectedFollowUp = LocalDate.now().plusMonths(3);
        assertThat(client.getNextFollowupDate()).isEqualTo(expectedFollowUp);

        // Check Sales Person Client Follow-up API
        List<ClientFollowUpResponse> followUps = crmService.getClientFollowUps(ADMIN_USER, ADMIN_USER);
        ClientFollowUpResponse myClient = followUps.stream()
                .filter(f -> f.getClientCode().equals(client.getClientCode()))
                .findFirst()
                .orElse(null);

        assertThat(myClient).isNotNull();
        assertThat(myClient.getNextFollowupDate()).isEqualTo(expectedFollowUp);
        assertThat(myClient.getSalesPersonCode()).isEqualTo(ADMIN_USER);
    }

    @Test
    @DisplayName("TEST 7: Other status (CLIENT_NOT_INTERESTED) removed from active work while keeping DB history intact")
    @WithMockUser(username = ADMIN_USER, roles = {"SUPER_ADMIN", "MEETING_VERIFY"})
    public void testTerminalStatusRemovedFromActiveWorkPreservesHistory() {
        // Step 1: Create Lead
        CreateLeadRequest leadReq = new CreateLeadRequest();
        leadReq.setClientName("Not Interested Client");
        leadReq.setMobileNumber("95" + System.currentTimeMillis() % 100000000L);
        leadReq.setLeadSource(LeadSource.MANUAL);
        LeadResponse lead = leadService.createLead(leadReq, ADMIN_USER);

        // Step 2: Schedule & Conduct Meeting with CLIENT_NOT_INTERESTED
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
                .leadStatus(MeetingLeadStatus.CLIENT_NOT_INTERESTED)
                .remarks("Client not interested at this time")
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();
        meetingService.processMeetingUpdateWorkflow(meeting.getMeetingCode(), workflowReq, ADMIN_USER);

        // Step 3: PC Verifies Meeting
        MeetingVerificationRequest pcVerifyReq = MeetingVerificationRequest.builder()
                .meetingDate(LocalDate.now())
                .meetingTiming(LocalTime.of(10, 0))
                .meetingWith("SELF")
                .ageGroup("AGE_46_55")
                .existingSip("NO")
                .profession("RETIRED")
                .remarks("PC verified: Client confirmed not interested")
                .build();
        processCoordinatorService.verifyMeeting(meeting.getMeetingCode(), pcVerifyReq, ADMIN_USER);

        // TEST 7 ASSERTIONS:
        // 1. Database records remain 100% intact (NOT deleted)
        Lead leadInDb = leadRepository.findByLeadCode(lead.getLeadCode()).orElseThrow();
        assertThat(leadInDb.getLeadStatus()).isEqualTo(LeadStatus.NOT_INTERESTED);
        assertThat(leadInDb.isDeleted()).isFalse();

        Meeting meetingInDb = meetingRepository.findByMeetingCode(meeting.getMeetingCode()).orElseThrow();
        assertThat(meetingInDb.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);
        assertThat(meetingInDb.getVerifiedByProcessCoordinator()).isTrue();

        // 2. Active work queries exclude this lead:
        // A) No active scheduled meeting exists for this lead
        ActiveMeetingResponse activeMtg = meetingService.getActiveMeetingByLeadId(lead.getUniqueLeadId());
        assertThat(activeMtg.getMeetingCode()).isNull();

        // B) Active work lead filter excludes NOT_INTERESTED
        LeadFilterRequest activeFilter = LeadFilterRequest.builder()
                .leadCode(lead.getLeadCode())
                .activeWorkOnly(true)
                .build();
        var activeLeadsPage = leadService.filterLeads(activeFilter, PageRequest.of(0, 10));
        assertThat(activeLeadsPage.getContent()).isEmpty();

        // C) Full query still contains the lead (history intact)
        LeadFilterRequest allFilter = LeadFilterRequest.builder()
                .leadCode(lead.getLeadCode())
                .build();
        var allLeadsPage = leadService.filterLeads(allFilter, PageRequest.of(0, 10));
        assertThat(allLeadsPage.getContent()).hasSize(1);
        assertThat(allLeadsPage.getContent().get(0).getLeadCode()).isEqualTo(lead.getLeadCode());
    }
}
