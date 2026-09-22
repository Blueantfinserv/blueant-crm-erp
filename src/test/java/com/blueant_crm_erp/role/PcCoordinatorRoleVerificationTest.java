package com.blueant_crm_erp.role;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.enums.LeadSource;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingVerificationRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("default")
public class PcCoordinatorRoleVerificationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private LeadService leadService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Verify Role ID 8 is renamed in-place to PC_COORDINATOR without duplicates")
    public void testRoleId8RenamedInPlace() {
        // 1. Role ID 8 exists
        Role role8 = roleRepository.findById(8L).orElse(null);
        assertThat(role8).isNotNull();
        assertThat(role8.getId()).isEqualTo(8L);

        // 2. Code is PC_COORDINATOR
        assertThat(role8.getCode()).isEqualTo("PC_COORDINATOR");

        // 3. Name is PC Coordinator
        assertThat(role8.getName()).isEqualTo("PC Coordinator");

        // 4. Description is PC Coordinator Role
        assertThat(role8.getDescription()).isEqualTo("PC Coordinator Role");

        // 5. Status is ACTIVE
        assertThat(role8.getStatus()).isEqualTo(Status.ACTIVE);

        // 6. No duplicate role with old code exists
        var oldRoleOpt = roleRepository.findByCodeIgnoreCase("SALES_COORDINATOR");
        assertThat(oldRoleOpt).isEmpty();

        // 7. Find by new code succeeds and returns role 8
        var newRoleOpt = roleRepository.findByCodeIgnoreCase("PC_COORDINATOR");
        assertThat(newRoleOpt).isPresent();
        assertThat(newRoleOpt.get().getId()).isEqualTo(8L);
    }

    @Autowired
    private com.blueant_crm_erp.meeting.service.MeetingWorkflowService meetingWorkflowService;

    @Test
    @Transactional
    @DisplayName("Verify Role ID 8 permissions remain intact")
    public void testRole8PermissionsPreserved() {
        var permissions = rolePermissionRepository.findAllByRoleId(8L);
        assertThat(permissions).isNotEmpty();
        var permissionCodes = permissions.stream()
                .map(rp -> rp.getPermission().getCode())
                .toList();

        assertThat(permissionCodes).contains(
                "MEETING_READ",
                "MEETING_VERIFY",
                "PHYSICAL_LEAD_ASSIGN"
        );
    }

    @Test
    @WithMockUser(username = "pc_coordinator@blueant.com", authorities = {"ROLE_PC_COORDINATOR", "MEETING_READ", "MEETING_VERIFY"})
    @DisplayName("Verify user with ROLE_PC_COORDINATOR can access and verify meetings")
    public void testPcCoordinatorWorkflowAndSecurity() throws Exception {
        // 1. Create a lead
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName("PC Coordinator Test Client");
        leadRequest.setMobileNumber("9" + String.valueOf(System.currentTimeMillis()).substring(4, 13));
        leadRequest.setLeadSource(LeadSource.MANUAL);
        leadRequest.setLocation("Mumbai");
        LeadResponse lead = leadService.createLead(leadRequest, "EMP000001");

        // 2. Schedule a meeting
        CreateMeetingRequest meetingRequest = CreateMeetingRequest.builder()
                .leadId(UUID.fromString(lead.getUniqueLeadId()))
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now().plusDays(1))
                .meetingTime(LocalTime.of(10, 0))
                .meetingLocation("Mumbai Branch")
                .meetingRemarks("Testing PC Coordinator role")
                .meetingStatus(MeetingStatus.SCHEDULED)
                .build();
        MeetingResponse meeting = meetingService.createMeeting(meetingRequest, "EMP000001");

        // 3. Conduct meeting (by Sales Rep)
        com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest workflowRequest =
                com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest.builder()
                        .aloneWith("SELF")
                        .leadStatus(com.blueant_crm_erp.meeting.enums.MeetingLeadStatus.WORK_IN_PROGRESS)
                        .remarks("Meeting conducted")
                        .nextPlanDate(LocalDate.now().plusDays(2))
                        .nextPlanTime(LocalTime.of(11, 0))
                        .build();
        meetingWorkflowService.processWorkflow(meeting.getMeetingCode(), workflowRequest, "EMP000001");

        // 4. PC Coordinator verifies the meeting via /v1/meetings/{code}/verify
        MeetingVerificationRequest verifyRequest = MeetingVerificationRequest.builder()
                .remarks("PC Coordinator verified successfully")
                .aloneWith("SELF")
                .profession("SALARIED_EMPLOYEE")
                .clientAge(35)
                .previousInvestment(true)
                .meetingTiming(LocalTime.of(15, 30))
                .build();

        // 4. PC Coordinator verifies the meeting via /v1/meetings/verification/{code}/verify
        mockMvc.perform(post("/v1/meetings/verification/" + meeting.getMeetingCode() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk());

        // 5. PC Coordinator fetches verification details via /v1/meetings/verification/{code}
        mockMvc.perform(get("/v1/meetings/verification/" + meeting.getMeetingCode()))
                .andExpect(status().isOk());
    }
}
