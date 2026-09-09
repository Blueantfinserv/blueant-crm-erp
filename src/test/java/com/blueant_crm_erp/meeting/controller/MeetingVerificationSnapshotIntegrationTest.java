package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MeetingVerificationSnapshotIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static long phoneSuffix = 9812000001L;

    private Lead createLeadWithSalesPerson(String clientName) {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName(clientName);
        leadRequest.setMobileNumber(String.valueOf(phoneSuffix++));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        leadRequest.setLocation("Connaught Place, New Delhi");

        LeadResponse response = leadService.createLead(leadRequest, "EMP000001");
        Lead lead = leadRepository.findByUniqueLeadId(response.getUniqueLeadId()).orElseThrow();

        User salesPerson = userRepository.findByEmployeeCodeIgnoreCase("EMP000001").orElse(null);
        if (salesPerson != null) {
            lead.setAssignedSalesPerson(salesPerson);
            leadRepository.save(lead);
        }
        return lead;
    }

    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void testCompleteVerificationSnapshotLifecycle() throws Exception {
        Lead lead = createLeadWithSalesPerson("Snapshot Verification Client");

        // 1. Conduct workflow update on initial meeting with GPS + Visiting Card
        MeetingWorkflowRequest workflowRequest = MeetingWorkflowRequest.builder()
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingLocation("Connaught Place, New Delhi")
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Discussion conducted in-person. Client interested.")
                .nextPlanDate(LocalDate.now().plusDays(3))
                .nextPlanTime(LocalTime.of(14, 30))
                .latitude(BigDecimal.valueOf(28.5355000))
                .longitude(BigDecimal.valueOf(77.3910000))
                .accuracy(15.2)
                .visitingCard("/api/v1/documents/201/download")
                .build();

        meetingService.processMeetingUpdateWorkflow("NEW_" + lead.getUniqueLeadId(), workflowRequest, "EMP000001");

        // Fetch completed initial meeting
        Meeting completedMeeting = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId()).get(0);
        assertEquals(MeetingStatus.COMPLETED, completedMeeting.getMeetingStatus());
        assertNotNull(completedMeeting.getLatitude());

        // Verify that before coordinator verification, the verification status is PENDING
        MeetingVerification pendingVer = meetingVerificationRepository.findByMeetingId(completedMeeting.getId()).orElseThrow();
        assertEquals(VerificationStatus.PENDING, pendingVer.getVerificationStatus());
        assertNull(pendingVer.getMeetingCode()); // snapshot columns are null in PENDING state

        // 2. Coordinator verifies the meeting
        MeetingVerificationRequest verifyReq = MeetingVerificationRequest.builder()
                .meetingTiming(LocalTime.of(11, 0))
                .ageGroup("AGE_25_35")
                .existingSip("YES")
                .profession("SALARIED_EMPLOYEE")
                .professionDetail("Senior Software Engineer at MNC")
                .bestTimeForMeeting("MORNING")
                .meetingWith("SELF")
                .clientAge(35)
                .maritalStatus("MARRIED")
                .email("client@snapshot-test.com")
                .companyName("Tech Innovators Pvt Ltd")
                .anyChildren(true)
                .numberOfChildren(2)
                .previousInvestment(true)
                .remarks("Coordinator verified client details and investment readiness.")
                .build();

        mockMvc.perform(post("/v1/meetings/verification/" + completedMeeting.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk());

        // 3. Inspect meeting_verifications database entity
        MeetingVerification verifiedVer = meetingVerificationRepository.findByMeetingId(completedMeeting.getId()).orElseThrow();
        assertEquals(VerificationStatus.VERIFIED, verifiedVer.getVerificationStatus());
        assertEquals("coordinator@blueant.com", verifiedVer.getVerifiedBy());
        assertNotNull(verifiedVer.getVerifiedAt());

        // Verify Meeting Identity
        assertEquals(completedMeeting.getMeetingCode(), verifiedVer.getMeetingCode());
        assertEquals(completedMeeting.getMeetingNumber(), verifiedVer.getMeetingNumber());
        assertEquals(completedMeeting.getMeetingType(), verifiedVer.getMeetingType());
        assertEquals(completedMeeting.getMeetingTitle(), verifiedVer.getMeetingTitle());

        // Verify Lead / Client Snapshot
        assertEquals(lead.getId(), verifiedVer.getLeadId());
        assertEquals(lead.getLeadCode(), verifiedVer.getLeadCode());
        assertEquals(lead.getClientName(), verifiedVer.getClientName());
        assertEquals(lead.getMobileNumber(), verifiedVer.getMobileNumber());

        // Verify Sales Person Snapshot
        assertNotNull(verifiedVer.getAssignedEmployeeId());
        assertEquals("EMP000001", verifiedVer.getEmployeeCode());
        assertNotNull(verifiedVer.getEmployeeName());

        // Verify Meeting Execution Snapshot
        assertEquals(completedMeeting.getMeetingDate(), verifiedVer.getMeetingDate());
        assertEquals(completedMeeting.getMeetingMode(), verifiedVer.getMeetingMode());
        assertNotNull(verifiedVer.getMeetingLocation());
        assertEquals(MeetingStatus.COMPLETED, verifiedVer.getMeetingStatus());
        assertEquals(completedMeeting.getStatus(), verifiedVer.getStatus());
        assertEquals("Discussion conducted in-person. Client interested.", verifiedVer.getMeetingRemarks());
        assertEquals(completedMeeting.getNextMeetingDate(), verifiedVer.getNextMeetingDate());
        assertEquals(completedMeeting.getMeetingConducted(), verifiedVer.getMeetingConducted());
        assertEquals(MeetingLeadStatus.WORK_IN_PROGRESS, verifiedVer.getLeadStatus());

        // Verify GPS & Visiting Card Captured Data
        assertNotNull(verifiedVer.getLatitude());
        assertEquals(0, verifiedVer.getLatitude().compareTo(BigDecimal.valueOf(28.5355000)));
        assertNotNull(verifiedVer.getLongitude());
        assertEquals(0, verifiedVer.getLongitude().compareTo(BigDecimal.valueOf(77.3910000)));
        assertEquals(15.2, verifiedVer.getLocationAccuracy());
        assertNotNull(verifiedVer.getLocationCapturedAt());
        assertNotNull(verifiedVer.getGoogleMapsUrl());
        assertTrue(verifiedVer.getGoogleMapsUrl().contains("28.5355"));
        assertEquals("/api/v1/documents/201/download", verifiedVer.getVisitingCard());

        // Verify Questionnaire Fields
        assertEquals(LocalTime.of(11, 0), verifiedVer.getMeetingTiming());
        assertEquals("AGE_25_35", verifiedVer.getAgeGroup().name());
        assertEquals("YES", verifiedVer.getExistingSip().name());
        assertEquals("Senior Software Engineer at MNC", verifiedVer.getProfessionDetail());
        assertEquals("MORNING", verifiedVer.getBestTimeForMeeting().name());
        assertEquals("SALARIED_EMPLOYEE", verifiedVer.getProfession());
        assertEquals("SELF", verifiedVer.getAloneWith());
        assertEquals(35, verifiedVer.getClientAge());
        assertEquals("MARRIED", verifiedVer.getMaritalStatus());
        assertEquals("client@snapshot-test.com", verifiedVer.getEmail());
        assertEquals("Tech Innovators Pvt Ltd", verifiedVer.getCompanyName());
        assertTrue(verifiedVer.getAnyChildren());
        assertEquals(2, verifiedVer.getNumberOfChildren());
        assertTrue(verifiedVer.getPreviousInvestment());

        // 4. Verify GET /v1/meetings/verification/{meetingCode} API returns complete snapshot
        mockMvc.perform(get("/v1/meetings/verification/" + completedMeeting.getMeetingCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingCode").value(completedMeeting.getMeetingCode()))
                .andExpect(jsonPath("$.leadCode").value(lead.getLeadCode()))
                .andExpect(jsonPath("$.clientName").value(lead.getClientName()))
                .andExpect(jsonPath("$.employeeCode").value("EMP000001"))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.latitude").value(28.5355))
                .andExpect(jsonPath("$.longitude").value(77.391))
                .andExpect(jsonPath("$.visitingCard").value("/api/v1/documents/201/download"))
                .andExpect(jsonPath("$.email").value("client@snapshot-test.com"));
    }

    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void testRejectionPreservesRejectionStateWithoutSnapshotFalsification() throws Exception {
        Lead lead = createLeadWithSalesPerson("Rejected Snapshot Client");

        MeetingWorkflowRequest workflowRequest = MeetingWorkflowRequest.builder()
                .aloneWith("SELF")
                .leadStatus(MeetingLeadStatus.CLIENT_NOT_INTERESTED)
                .remarks("Client not interested")
                .build();

        meetingService.processMeetingUpdateWorkflow("NEW_" + lead.getUniqueLeadId(), workflowRequest, "EMP000001");

        Meeting completedMeeting = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId()).get(0);

        mockMvc.perform(post("/v1/meetings/verification/" + completedMeeting.getMeetingCode() + "/reject")
                .param("reason", "Client denied having any meeting"))
                .andExpect(status().isOk());

        MeetingVerification rejectedVer = meetingVerificationRepository.findByMeetingId(completedMeeting.getId()).orElseThrow();
        assertEquals(VerificationStatus.REJECTED, rejectedVer.getVerificationStatus());
        assertEquals("Client denied having any meeting", rejectedVer.getRejectionReason());
        assertNull(verifiedSnapshotClientName(rejectedVer));
    }

    private String verifiedSnapshotClientName(MeetingVerification ver) {
        return ver.getClientName();
    }
}
