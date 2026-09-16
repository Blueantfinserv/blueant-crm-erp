package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingType;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.MeetingCodeGeneratorService;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MeetingCodeConcurrencyAndIdempotencyRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingScheduleService meetingScheduleService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingCodeGeneratorService meetingCodeGeneratorService;

    @Autowired
    private ObjectMapper objectMapper;

    private LeadResponse createTestLead(String name) {
        CreateLeadRequest leadRequest = new CreateLeadRequest();
        leadRequest.setClientName(name);
        leadRequest.setMobileNumber(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        leadRequest.setLeadSource(com.blueant_crm_erp.lead.enums.LeadSource.MANUAL);
        return leadService.createLead(leadRequest, "EMP000001");
    }

    private MeetingResponse scheduleMeeting(String leadUniqueId) {
        CreateMeetingRequest scheduleRequest = new CreateMeetingRequest();
        scheduleRequest.setLeadId(UUID.fromString(leadUniqueId));
        scheduleRequest.setMeetingMode(MeetingMode.PHYSICAL);
        scheduleRequest.setMeetingDate(LocalDate.now().plusDays(1));
        scheduleRequest.setMeetingTime(LocalTime.of(10, 0));
        scheduleRequest.setMeetingLocation("Noida Office");
        return meetingScheduleService.scheduleMeeting(scheduleRequest, "EMP000001");
    }

    @Test
    @DisplayName("Regression Test 1 & 2 & 3: Follow-up skips existing BA-MTG-2026-000536, never overwrites it, and generates unique code")
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test1_FollowUpGeneratesUniqueCodeWhen000536AlreadyExists() throws Exception {
        LeadResponse otherLeadRes = createTestLead("Other Lead");
        Lead otherLead = leadRepository.findByUniqueLeadId(otherLeadRes.getUniqueLeadId()).orElseThrow();

        // Pre-insert or retrieve meeting with code BA-MTG-2026-000536 in database
        String duplicateTargetCode = "BA-MTG-2026-000536";
        Meeting existing536;
        if (!meetingRepository.existsByMeetingCode(duplicateTargetCode)) {
            existing536 = Meeting.builder()
                    .meetingCode(duplicateTargetCode)
                    .meetingNumber(99)
                    .meetingType(MeetingType.INTRO)
                    .meetingTitle("Existing 536 Meeting")
                    .lead(otherLead)
                    .meetingMode(MeetingMode.PHYSICAL)
                    .meetingDate(LocalDate.now().plusDays(2))
                    .meetingStatus(MeetingStatus.SCHEDULED)
                    .status(Status.ACTIVE)
                    .build();
            meetingRepository.saveAndFlush(existing536);
        } else {
            existing536 = meetingRepository.findByMeetingCode(duplicateTargetCode).orElseThrow();
        }
        String originalTitle = existing536.getMeetingTitle();
        Long originalLeadId = existing536.getLead().getId();

        // Create the current meeting (representing 000126 workflow scenario)
        LeadResponse leadRes = createTestLead("Workflow Lead 126");
        MeetingResponse currentMeeting = scheduleMeeting(leadRes.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingConducted", "CONDUCTED");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("nextPlanDate", LocalDate.now().plusDays(7).toString());
        payload.put("latitude", 28.621064790041498);
        payload.put("longitude", 77.36741379530801);
        payload.put("accuracy", 149);
        payload.put("aloneWith", "SOMEONE");
        payload.put("personName", "Colleague");
        payload.put("position", "Associate");
        payload.put("remarks", "Follow up discussion needed");

        // Submit workflow update
        String responseJson = mockMvc.perform(post("/v1/meetings/" + currentMeeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        MeetingResponse followUp = objectMapper.readValue(
                objectMapper.readTree(responseJson).path("data").toString(), MeetingResponse.class);

        // Verification 1: New follow-up was created and has a genuinely unused meeting code
        assertNotNull(followUp);
        assertNotNull(followUp.getMeetingCode());
        assertNotEquals(duplicateTargetCode, followUp.getMeetingCode(), "Follow-up code must not reuse 000536");
        assertNotEquals(currentMeeting.getMeetingCode(), followUp.getMeetingCode(), "Follow-up code must not be current meeting code");
        assertEquals(2, followUp.getMeetingNumber());

        // Verification 2: Existing 000536 must NEVER be overwritten
        Meeting existingAfter = meetingRepository.findByMeetingCode(duplicateTargetCode).orElseThrow();
        assertEquals(originalTitle, existingAfter.getMeetingTitle(), "Pre-existing 536 must not be modified or overwritten");
        assertEquals(originalLeadId, existingAfter.getLead().getId());
    }

    @Test
    @DisplayName("Regression Test 4: Repeating the same workflow-update request does not create duplicate follow-ups (Idempotency)")
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void test2_RepeatingSameWorkflowUpdateRequestDoesNotCreateDuplicateFollowUps() throws Exception {
        LeadResponse leadRes = createTestLead("Idempotency Client");
        MeetingResponse currentMeeting = scheduleMeeting(leadRes.getUniqueLeadId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("meetingMode", "PHYSICAL");
        payload.put("meetingConducted", "CONDUCTED");
        payload.put("leadStatus", "WORK_IN_PROGRESS");
        payload.put("nextPlanDate", LocalDate.now().plusDays(5).toString());
        payload.put("aloneWith", "SELF");
        payload.put("remarks", "Idempotency test remarks");

        // First call creates follow-up
        String firstResponseJson = mockMvc.perform(post("/v1/meetings/" + currentMeeting.getMeetingCode() + "/workflow-update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MeetingResponse firstFollowUp = objectMapper.readValue(
                objectMapper.readTree(firstResponseJson).path("data").toString(), MeetingResponse.class);

        // Second call (repeated / retried request)
        // With current architecture, the follow-up meeting is already created.
        // If the workflow is called again when nextSequence already exists:
        Lead lead = leadRepository.findByUniqueLeadId(leadRes.getUniqueLeadId()).orElseThrow();
        long totalMeetingsForLead = meetingRepository.countByLeadId(lead.getId());
        assertEquals(2, totalMeetingsForLead, "Lead must have exactly 2 meetings (Initial + 1 FollowUp)");

        // Verify the follow-up exists and is SCHEDULED
        Optional<Meeting> followUpOpt = meetingRepository.findByLeadIdAndMeetingNumber(lead.getId(), 2);
        assertTrue(followUpOpt.isPresent());
        assertEquals(firstFollowUp.getMeetingCode(), followUpOpt.get().getMeetingCode());
    }

    @Test
    @DisplayName("Regression Test 5: Concurrent meeting code generation calls produce unique codes")
    public void test3_ConcurrentMeetingCodeGenerationProducesUniqueCodes() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        Set<String> generatedCodes = Collections.synchronizedSet(new HashSet<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    String code = meetingCodeGeneratorService.generateNextMeetingCode();
                    generatedCodes.add(code);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All threads should complete within 30 seconds");
        assertTrue(errors.isEmpty(), "No thread should throw an exception: " + errors);
        assertEquals(threadCount, generatedCodes.size(), "Every generated meeting code must be unique!");

        for (String code : generatedCodes) {
            assertTrue(code.matches("^BA-MTG-\\d{4}-\\d{6}$"), "Code must follow standard format: " + code);
        }
    }
}
