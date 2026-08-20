package com.blueant_crm_erp.meeting.controller;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SalesCoordinatorVerificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testVerificationLifecycleFlow() throws Exception {
        // 1. Create a lead first
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Verification Lifecycle Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        // 2. Schedule a meeting
        CreateMeetingRequest meetingRequest = CreateMeetingRequest.builder()
                .leadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()))
                .meetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Delhi Office")
                .meetingRemarks("Need lifecycle test")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meetingResponse = meetingService.createMeeting(meetingRequest, "EMP000001");

        // Assert that the scheduled meeting has NO verification record (status is null)
        MeetingVerification initialVer = meetingVerificationRepository.findByMeetingMeetingCode(meetingResponse.getMeetingCode())
                .orElse(null);
        assertThat(initialVer).isNull();

        // 3. Conduct/Complete the meeting using workflow update
        Map<String, Object> payload = new HashMap<>();
        payload.put("aloneWith", "SELF");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("remarks", "Meeting completed successfully");
        payload.put("nextPlanDate", LocalDate.now().plusDays(2).toString());
        payload.put("nextPlanTime", "12:00:00");

        mockMvc.perform(post("/v1/meetings/" + meetingResponse.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // Assert current meeting verificationStatus = PENDING
        MeetingVerification pendingVer = meetingVerificationRepository.findByMeetingMeetingCode(meetingResponse.getMeetingCode())
                .orElse(null);
        assertThat(pendingVer).isNotNull();
        assertThat(pendingVer.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);

        // Fetch current meeting to verify status and next meeting details
        Meeting currentMeeting = meetingRepository.findByMeetingCode(meetingResponse.getMeetingCode()).orElse(null);
        assertThat(currentMeeting).isNotNull();
        assertThat(currentMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.COMPLETED);

        // Fetch next follow-up meeting and assert it has NO pending verification
        int nextSequence = currentMeeting.getMeetingNumber() + 1;
        Meeting nextMeeting = meetingRepository.findAll().stream()
                .filter(m -> m.getLead().getId().equals(currentMeeting.getLead().getId()) && m.getMeetingNumber() == nextSequence)
                .findFirst()
                .orElse(null);

        assertThat(nextMeeting).isNotNull();
        assertThat(nextMeeting.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);

        MeetingVerification nextVer = meetingVerificationRepository.findByMeetingMeetingCode(nextMeeting.getMeetingCode())
                .orElse(null);
        assertThat(nextVer).isNull();
    }

    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void testSalesCoordinatorAccessAndVerificationFlow() throws Exception {
        // 1. Create a lead first
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Verification Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        // 2. Schedule a meeting
        CreateMeetingRequest meetingRequest = CreateMeetingRequest.builder()
                .leadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()))
                .meetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Delhi Office")
                .meetingRemarks("Need verification test")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meetingResponse = meetingService.createMeeting(meetingRequest, "EMP000001");

        // Conduct/Complete it first to make verificationStatus = PENDING
        MeetingWorkflowRequest workflowRequest = MeetingWorkflowRequest.builder()
                .aloneWith("SELF")
                .leadStatus(com.blueant_crm_erp.meeting.enums.MeetingLeadStatus.CLIENT_NOT_INTERESTED)
                .remarks("Client is not interested at this stage.")
                .build();
        meetingService.processMeetingUpdateWorkflow(meetingResponse.getMeetingCode(), workflowRequest, "salesperson@blueant.com");

        // Verify state is now PENDING
        MeetingVerification initialVer = meetingVerificationRepository.findByMeetingMeetingCode(meetingResponse.getMeetingCode())
                .orElse(null);
        assertThat(initialVer).isNotNull();
        assertThat(initialVer.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);

        // 3. Test verification validation failure (aloneWith = SOMEONE but missing personName and position)
        MeetingVerificationRequest invalidRequest = MeetingVerificationRequest.builder()
                .remarks("Invalid request test")
                .aloneWith("SOMEONE")
                .clientAge(30)
                .maritalStatus("MARRIED")
                .profession("ENGINEER")
                .email("someone@test.com")
                .companyName("Test Inc")
                .anyChildren(false)
                .previousInvestment(true)
                .build();

        mockMvc.perform(post("/v1/meetings/verification/" + meetingResponse.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // 4. Test successful verification by Sales Coordinator
        MeetingVerificationRequest validRequest = MeetingVerificationRequest.builder()
                .remarks("Meeting looks very genuine")
                .aloneWith("SELF")
                .clientAge(45)
                .maritalStatus("MARRIED")
                .profession("DOCTOR")
                .email("doctor@test.com")
                .companyName("Hospital Corp")
                .anyChildren(true)
                .numberOfChildren(2)
                .previousInvestment(true)
                .build();

        mockMvc.perform(post("/v1/meetings/verification/" + meetingResponse.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingCode").value(meetingResponse.getMeetingCode()))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        // Verify database states
        MeetingVerification verifiedVal = meetingVerificationRepository.findByMeetingMeetingCode(meetingResponse.getMeetingCode())
                .orElse(null);
        assertThat(verifiedVal).isNotNull();
        assertThat(verifiedVal.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(verifiedVal.getVerifiedBy()).isEqualTo("coordinator@blueant.com");
        assertThat(verifiedVal.getClientAge()).isEqualTo(45);
        assertThat(verifiedVal.getProfession()).isEqualTo("DOCTOR");
        assertThat(verifiedVal.getNumberOfChildren()).isEqualTo(2);

        // Verify meeting entity denormalized columns updated in sync
        Meeting updatedMeeting = meetingRepository.findByMeetingCode(meetingResponse.getMeetingCode()).orElse(null);
        assertThat(updatedMeeting).isNotNull();
        assertThat(updatedMeeting.getVerifiedByProcessCoordinator()).isTrue();
        assertThat(updatedMeeting.getVerificationRemarks()).isEqualTo("Meeting looks very genuine");
        assertThat(updatedMeeting.getVerifiedBy()).isEqualTo("coordinator@blueant.com");
    }

    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void testSalesCoordinatorRejectionFlow() throws Exception {
        // 1. Create a lead first
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Rejection Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        // 2. Schedule a meeting
        CreateMeetingRequest meetingRequest = CreateMeetingRequest.builder()
                .leadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()))
                .meetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(2))
                .meetingTime(LocalTime.of(11, 0))
                .meetingLocation("Gurgaon Office")
                .meetingRemarks("Reject test")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meetingResponse = meetingService.createMeeting(meetingRequest, "EMP000001");

        // Conduct/Complete the meeting
        MeetingWorkflowRequest workflowRequest = MeetingWorkflowRequest.builder()
                .aloneWith("SELF")
                .leadStatus(com.blueant_crm_erp.meeting.enums.MeetingLeadStatus.CLIENT_NOT_INTERESTED)
                .remarks("Rejected meeting candidate")
                .build();
        meetingService.processMeetingUpdateWorkflow(meetingResponse.getMeetingCode(), workflowRequest, "salesperson@blueant.com");

        // 3. Test successful rejection by Sales Coordinator
        mockMvc.perform(post("/v1/meetings/verification/" + meetingResponse.getMeetingCode() + "/reject")
                .param("reason", "Client age is wrong"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingCode").value(meetingResponse.getMeetingCode()))
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"));

        // Verify database states
        MeetingVerification verifiedVal = meetingVerificationRepository.findByMeetingMeetingCode(meetingResponse.getMeetingCode())
                .orElse(null);
        assertThat(verifiedVal).isNotNull();
        assertThat(verifiedVal.getVerificationStatus()).isEqualTo(VerificationStatus.REJECTED);
        assertThat(verifiedVal.getRejectionReason()).isEqualTo("Client age is wrong");

        Meeting updatedMeeting = meetingRepository.findByMeetingCode(meetingResponse.getMeetingCode()).orElse(null);
        assertThat(updatedMeeting).isNotNull();
        assertThat(updatedMeeting.getVerifiedByProcessCoordinator()).isFalse();
        assertThat(updatedMeeting.getVerificationRemarks()).isEqualTo("REJECTED: Client age is wrong");
    }

    @Test
    @WithMockUser(username = "salesperson@blueant.com", authorities = {"ROLE_RELATIONSHIP_MANAGER", "MEETING_READ"})
    public void testSalesPersonUnauthorizedAccess() throws Exception {
        MeetingVerificationRequest validRequest = MeetingVerificationRequest.builder()
                .remarks("Unauth test")
                .aloneWith("SELF")
                .clientAge(30)
                .maritalStatus("SINGLE")
                .profession("EMPLOYEE")
                .email("unauth@test.com")
                .companyName("None")
                .anyChildren(false)
                .previousInvestment(false)
                .build();

        // Attempting to verify meeting using SALES_PERSON role should return 403 Forbidden
        mockMvc.perform(post("/v1/meetings/verification/MEET000001/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        // Attempting to reject meeting using SALES_PERSON role should return 403 Forbidden
        mockMvc.perform(post("/v1/meetings/verification/MEET000001/reject")
                .param("reason", "unauthorized"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void testGetAllMeetingsFiltering() throws Exception {
        // Perform search request with verificationStatus parameter
        mockMvc.perform(get("/v1/meetings")
                .param("verificationStatus", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void testSalesCoordinatorForbiddenEndpoints() throws Exception {
        // Sales Coordinator attempts to create a meeting
        CreateMeetingRequest createRequest = CreateMeetingRequest.builder()
                .leadId(java.util.UUID.randomUUID())
                .meetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Delhi Office")
                .meetingRemarks("Need verification test")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        // Sales Coordinator attempts to update a meeting
        UpdateMeetingRequest updateRequest = UpdateMeetingRequest.builder()
                .meetingCode("MEET001")
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL)
                .meetingLocation("Delhi")
                .build();
        mockMvc.perform(put("/v1/meetings/MEET001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        // Sales Coordinator attempts workflow update
        Map<String, Object> workflowPayload = new HashMap<>();
        workflowPayload.put("aloneWith", "SELF");
        workflowPayload.put("leadStatus", "WORK_IN_PROGRESS");
        workflowPayload.put("remarks", "Test");
        mockMvc.perform(post("/v1/meetings/MEET001/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workflowPayload)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void testSalesCoordinatorDefenseInDepthValidations() throws Exception {
        // 1. Validate invalid meeting code returns 404
        MeetingVerificationRequest validRequest = MeetingVerificationRequest.builder()
                .remarks("Verification test")
                .aloneWith("SELF")
                .clientAge(30)
                .maritalStatus("SINGLE")
                .profession("EMPLOYEE")
                .email("test@test.com")
                .companyName("None")
                .anyChildren(false)
                .previousInvestment(false)
                .build();

        mockMvc.perform(post("/v1/meetings/verification/INVALID-CODE/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());

        // 2. Schedule a meeting but do NOT conduct it
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Validation Defense Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        CreateMeetingRequest meetingRequest = CreateMeetingRequest.builder()
                .leadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()))
                .meetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Delhi Office")
                .meetingRemarks("Defense validation test")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meetingResponse = meetingService.createMeeting(meetingRequest, "EMP000001");

        // Attempting to verify scheduled meeting should return 400 Bad Request
        mockMvc.perform(post("/v1/meetings/verification/" + meetingResponse.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "EMP000001", authorities = {"ROLE_EMPLOYEE", "LEAD_CREATE", "LEAD_READ", "LEAD_UPDATE", "MEETING_CREATE", "MEETING_READ", "MEETING_UPDATE"})
    public void testSalesExecutiveWorkflowAndSecurity() throws Exception {
        // 1. Sales Executive creates a lead
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Sales Exec Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        leadRequest.setLocation("Delhi");

        String leadResponseJson = mockMvc.perform(post("/v1/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        LeadResponse leadResponse = objectMapper.readValue(
                objectMapper.readTree(leadResponseJson).get("data").toString(),
                LeadResponse.class
        );

        // 2. Sales Executive creates a meeting
        CreateMeetingRequest meetingRequest = CreateMeetingRequest.builder()
                .leadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()))
                .meetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Delhi Office")
                .meetingRemarks("RM test")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();

        String meetingResponseJson = mockMvc.perform(post("/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(meetingRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Deserialize response
        MeetingResponse meetingResponse = objectMapper.readValue(
                objectMapper.readTree(meetingResponseJson).get("data").toString(),
                MeetingResponse.class
        );

        // 3. Sales Executive performs workflow update
        Map<String, Object> workflowPayload = new HashMap<>();
        workflowPayload.put("aloneWith", "SELF");
        workflowPayload.put("leadStatus", "WORK_IN_PROGRESS");
        workflowPayload.put("remarks", "Meeting conducted");

        mockMvc.perform(post("/v1/meetings/" + meetingResponse.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workflowPayload)))
                .andExpect(status().isOk());

        // 4. Sales Executive attempts to verify/reject meeting (should be forbidden)
        MeetingVerificationRequest validRequest = MeetingVerificationRequest.builder()
                .remarks("Verification test")
                .aloneWith("SELF")
                .clientAge(30)
                .maritalStatus("SINGLE")
                .profession("EMPLOYEE")
                .email("test@test.com")
                .companyName("None")
                .anyChildren(false)
                .previousInvestment(false)
                .build();

        mockMvc.perform(post("/v1/meetings/verification/" + meetingResponse.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/v1/meetings/verification/" + meetingResponse.getMeetingCode() + "/reject")
                .param("reason", "rejection test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void testSalesCoordinatorLeadCreationForbidden() throws Exception {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Unauth Coordinator Client");
        leadRequest.setMobileNumber("9998887776");
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        leadRequest.setLocation("Delhi");

        mockMvc.perform(post("/v1/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coordinator@blueant.com", authorities = {"ROLE_SALES_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    public void testVerificationPayloadStrictness() throws Exception {
        // 1. Create a lead first
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("Strictness Client");
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        leadRequest.setLocation("Delhi");
        LeadResponse leadResponse = leadService.createLead(leadRequest, "EMP000001");

        // 2. Schedule a meeting
        CreateMeetingRequest meetingRequest = CreateMeetingRequest.builder()
                .leadId(java.util.UUID.fromString(leadResponse.getUniqueLeadId()))
                .meetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Delhi Office")
                .meetingRemarks("Need verification test")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meetingResponse = meetingService.createMeeting(meetingRequest, "EMP000001");

        // Conduct/Complete it
        MeetingWorkflowRequest workflowRequest = MeetingWorkflowRequest.builder()
                .aloneWith("SELF")
                .leadStatus(com.blueant_crm_erp.meeting.enums.MeetingLeadStatus.CLIENT_NOT_INTERESTED)
                .remarks("Client is not interested")
                .build();
        meetingService.processMeetingUpdateWorkflow(meetingResponse.getMeetingCode(), workflowRequest, "salesperson@blueant.com");

        // Verification payload with extra malicious fields
        String maliciousPayload = "{" +
                "\"remarks\": \"Verified\"," +
                "\"aloneWith\": \"SELF\"," +
                "\"clientAge\": 35," +
                "\"maritalStatus\": \"SINGLE\"," +
                "\"profession\": \"EMPLOYEE\"," +
                "\"email\": \"test@test.com\"," +
                "\"companyName\": \"None\"," +
                "\"anyChildren\": false," +
                "\"previousInvestment\": false," +
                "\"meetingTitle\": \"Hacked Title\"," +
                "\"meetingRemarks\": \"Hacked Remarks\"" +
                "}";

        mockMvc.perform(post("/v1/meetings/verification/" + meetingResponse.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(maliciousPayload))
                .andExpect(status().isOk());

        // Assert that meeting title and other meeting-level RM data were NOT altered
        Meeting meeting = meetingRepository.findByMeetingCode(meetingResponse.getMeetingCode()).orElse(null);
        assertThat(meeting).isNotNull();
        assertThat(meeting.getMeetingTitle()).isEqualTo("Intro Meeting");
    }
}
