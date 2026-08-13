package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.lead.LeadNotFoundException;
import com.blueant_crm_erp.exception.meeting.MeetingNotFoundException;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.MeetingSearchRequest;
import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.ActiveMeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingDropdownResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingSummaryResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.mapper.MeetingMapper;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.MeetingService;
import com.blueant_crm_erp.meeting.service.MeetingWorkflowService;
import com.blueant_crm_erp.meeting.specification.MeetingSearchSpecification;
import com.blueant_crm_erp.meeting.validator.MeetingValidator;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.event.MeetingWorkflowEvent;
import com.blueant_crm_erp.meeting.dto.response.MeetingReportResponse;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.lead.dto.request.UpdateLeadStatusRequest;
import com.blueant_crm_erp.util.id.MeetingCodeGenerator;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final LeadRepository leadRepository;
    private final MeetingMapper meetingMapper;
    private final MeetingValidator meetingValidator;
    private final @Lazy MeetingScheduleService meetingScheduleService;
    private final ApplicationEventPublisher eventPublisher;
    private final @Lazy LeadService leadService;
    private final @Lazy MeetingWorkflowService meetingWorkflowService;

    @Override
    public MeetingResponse createMeeting(CreateMeetingRequest request, String currentUserEmail) {
        log.info("Creating new meeting for lead ID: {}, requested by: {}", request.getLeadId(), currentUserEmail);
        meetingValidator.validateCreate(request);

        Lead lead = leadRepository.findByUniqueLeadId(request.getLeadId().toString())
                .orElseThrow(() -> new LeadNotFoundException(MeetingConstants.INVALID_LEAD));
                
        if (LeadStatus.CONVERTED.equals(lead.getLeadStatus())) {
            throw new IllegalArgumentException("Converted leads cannot receive new meetings.");
        }

        if (meetingRepository.existsByLeadIdAndMeetingStatus(lead.getId(), MeetingStatus.SCHEDULED)) {
            throw new IllegalArgumentException("Lead already has an active meeting. Cannot create a new one.");
        }

        Meeting meeting = meetingMapper.toEntity(request);
        
        long count = meetingRepository.count() + 1;
        meeting.setMeetingCode(MeetingCodeGenerator.generate(count));
        
        Optional<Meeting> lastMeeting = meetingRepository.findTopByLeadIdOrderByMeetingNumberDesc(lead.getId());
        int nextMeetingNumber = lastMeeting.map(m -> m.getMeetingNumber() + 1).orElse(MeetingConstants.FIRST_MEETING_NUMBER);
        if (nextMeetingNumber > 10) {
            throw new IllegalArgumentException("Maximum allowed meeting sequence reached. Cannot schedule Meeting #10.");
        }
        meeting.setMeetingNumber(nextMeetingNumber);
        meeting.setMeetingType(nextMeetingNumber == 1 ? com.blueant_crm_erp.meeting.enums.MeetingType.INTRO : com.blueant_crm_erp.meeting.enums.MeetingType.FOLLOW_UP);

        String title;
        if (nextMeetingNumber == 1) {
            title = "Intro Meeting";
        } else {
            int meetingIndex = nextMeetingNumber - 1;
            if (meetingIndex == 1) {
                title = "1st Meeting";
            } else if (meetingIndex == 2) {
                title = "2nd Meeting";
            } else if (meetingIndex == 3) {
                title = "3rd Meeting";
            } else {
                title = meetingIndex + "th Meeting";
            }
        }
        meeting.setMeetingTitle(title);

        meeting.setLead(lead);
        meeting.setAssignedEmployee(lead.getAssignedSalesPerson());
        meeting.setMeetingStatus(MeetingStatus.SCHEDULED);
        meeting.setStatus(Status.ACTIVE);

        Meeting savedMeeting = meetingRepository.save(meeting);

        // Update Lead status/stage to MEETING_SCHEDULED / INTRO_MEETING_SCHEDULED for the first meeting
        if (nextMeetingNumber == 1) {
            UpdateLeadStatusRequest statusReq = UpdateLeadStatusRequest.builder()
                    .leadId(lead.getId())
                    .leadStatus(LeadStatus.MEETING_SCHEDULED)
                    .leadStage(com.blueant_crm_erp.lead.enums.LeadStage.INTRO_MEETING_SCHEDULED)
                    .remarks("Intro Meeting scheduled manually. Lead moved to Meeting Scheduled.")
                    .build();
            leadService.changeStatus(statusReq, currentUserEmail);
            log.info("Lead {} status updated to MEETING_SCHEDULED after manual Intro Meeting creation.", lead.getLeadCode());
        }
        
        eventPublisher.publishEvent(new MeetingWorkflowEvent(this, savedMeeting, "SCHEDULED", null, "Meeting scheduled manually", currentUserEmail));
        
        return meetingMapper.toResponse(savedMeeting);
    }

    @Override
    public Meeting createInitialMeeting(Lead lead) {
        log.info("Creating initial meeting for lead ID: {}", lead.getId());
        
        Meeting meeting = new Meeting();
        long count = meetingRepository.count() + 1;
        meeting.setMeetingCode(MeetingCodeGenerator.generate(count));
        meeting.setMeetingNumber(1); // Meeting Sequence = 1
        meeting.setMeetingType(com.blueant_crm_erp.meeting.enums.MeetingType.INTRO);
        meeting.setMeetingTitle("Intro Meeting"); // Title as per requirement
        
        // Defaults since it's auto-created.
        meeting.setMeetingDate(java.time.LocalDate.now());
        meeting.setMeetingTime(null);
        meeting.setMeetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL); // Changed default slightly, PHYSICAL is more common based on options
        
        meeting.setLead(lead);
        meeting.setAssignedEmployee(lead.getAssignedSalesPerson());
        meeting.setMeetingStatus(MeetingStatus.SCHEDULED);
        meeting.setStatus(Status.ACTIVE);

        Meeting savedMeeting = meetingRepository.save(meeting);
        
        eventPublisher.publishEvent(new MeetingWorkflowEvent(this, savedMeeting, "SCHEDULED", null, "Initial Meeting auto-created on demand", "SYSTEM"));
        return savedMeeting;
    }

    @Override
    public MeetingResponse updateMeeting(String meetingCode, UpdateMeetingRequest request, String currentUserEmail) {
        log.info("Updating meeting with code: {}, requested by: {}", meetingCode, currentUserEmail);
        Meeting meeting = getMeetingByCodeInternal(meetingCode);
                
        String previousStatus = meeting.getMeetingStatus().name();
        meetingValidator.validateUpdate(meeting.getId(), request, meeting);
        meetingMapper.updateEntityFromRequest(request, meeting);

        Meeting updatedMeeting = meetingRepository.save(meeting);
        
        eventPublisher.publishEvent(new MeetingWorkflowEvent(this, updatedMeeting, "UPDATED", previousStatus, "Meeting details updated manually", currentUserEmail));
        
        return meetingMapper.toResponse(updatedMeeting);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingDetailResponse getMeetingByCode(String meetingCode) {
        log.info("Fetching meeting details for code: {}", meetingCode);
        
        if (meetingCode != null && meetingCode.startsWith("NEW_")) {
            String leadId = meetingCode.substring(4);
            Lead lead = leadRepository.findByUniqueLeadId(leadId)
                    .orElseThrow(() -> new LeadNotFoundException(MeetingConstants.INVALID_LEAD));
            
            MeetingDetailResponse dummy = new MeetingDetailResponse();
            dummy.setMeetingCode(meetingCode);
            dummy.setMeetingNumber(1);
            dummy.setMeetingTitle("Intro Meeting");
            dummy.setMeetingType(com.blueant_crm_erp.meeting.enums.MeetingType.INTRO);
            dummy.setMeetingStatus(MeetingStatus.SCHEDULED);
            dummy.setMeetingDate(java.time.LocalDate.now());
            dummy.setMeetingTime(null);
            dummy.setMeetingMode(com.blueant_crm_erp.meeting.enums.MeetingMode.PHYSICAL);
            dummy.setAloneWith("SELF");
            
            dummy.setLeadId(lead.getId());
            dummy.setLeadCode(lead.getLeadCode());
            dummy.setClientName(lead.getClientName());
            
            if (lead.getAssignedSalesPerson() != null) {
                dummy.setAssignedEmployeeId(lead.getAssignedSalesPerson().getId());
                dummy.setEmployeeCode(lead.getAssignedSalesPerson().getEmployeeCode());
                dummy.setEmployeeName(lead.getAssignedSalesPerson().getFullName());
            }
            
            return dummy;
        }
        
        return meetingMapper.toDetailResponse(getMeetingByCodeInternal(meetingCode));
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingDetailResponse getMeetingById(Long id) {
        log.info("Fetching meeting details for id: {}", id);
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingConstants.MEETING_NOT_FOUND));
        return meetingMapper.toDetailResponse(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MeetingSummaryResponse> searchMeetings(MeetingSearchRequest request, Pageable pageable) {
        log.info("Searching meetings");
        Page<Meeting> meetingPage = meetingRepository.findAll(MeetingSearchSpecification.build(request), pageable);
        List<MeetingSummaryResponse> responses = meetingMapper.toSummaryResponseList(meetingPage.getContent());
        
        return PageResponse.<MeetingSummaryResponse>builder()
                .content(responses)
                .pageNumber(meetingPage.getNumber())
                .pageSize(meetingPage.getSize())
                .totalElements(meetingPage.getTotalElements())
                .totalPages(meetingPage.getTotalPages())
                .first(meetingPage.isFirst())
                .last(meetingPage.isLast())
                .hasNext(meetingPage.hasNext())
                .hasPrevious(meetingPage.hasPrevious())
                .empty(meetingPage.isEmpty())
                .sort(meetingPage.getSort().toString())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getAllMeetings(String search, String date, String status, Integer sequence) {
        log.info("Fetching meetings queue with filters - search: {}, date: {}, status: {}, sequence: {}", search, date, status, sequence);
        org.springframework.data.jpa.domain.Specification<Meeting> spec = MeetingSearchSpecification.build(search, date, status, sequence);
        List<Meeting> meetings = meetingRepository.findAll(spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "meetingDate", "meetingTime"));
        return meetingMapper.toResponseList(meetings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingDropdownResponse> getMeetingDropdown() {
        log.info("Fetching meeting dropdown data");
        List<Meeting> meetings = meetingRepository.findAll();
        return meetingMapper.toDropdownResponseList(meetings);
    }

    @Override
    public void deleteMeeting(String meetingCode, String currentUserEmail) {
        log.info("Deleting meeting with code: {}, requested by: {}", meetingCode, currentUserEmail);
        Meeting meeting = getMeetingByCodeInternal(meetingCode);
        if (MeetingStatus.COMPLETED.equals(meeting.getMeetingStatus())) {
            throw new IllegalArgumentException(MeetingConstants.MEETING_CANNOT_BE_DELETED);
        }
        meeting.markAsDeleted(currentUserEmail);
        meetingRepository.save(meeting);
    }

    @Override
    public void activateMeeting(String meetingCode, String currentUserEmail) {
        log.info("Activating meeting with code: {}, requested by: {}", meetingCode, currentUserEmail);
        Meeting meeting = getMeetingByCodeInternal(meetingCode);
        meeting.setStatus(Status.ACTIVE);
        meetingRepository.save(meeting);
    }

    @Override
    public void deactivateMeeting(String meetingCode, String currentUserEmail) {
        log.info("Deactivating meeting with code: {}, requested by: {}", meetingCode, currentUserEmail);
        Meeting meeting = getMeetingByCodeInternal(meetingCode);
        meeting.setStatus(Status.INACTIVE);
        meetingRepository.save(meeting);
    }

    private Meeting getMeetingByCodeInternal(String meetingCode) {
        return meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingConstants.MEETING_NOT_FOUND));
    }

    @Override
    public ActiveMeetingResponse getActiveMeetingByLeadId(String leadId) {
        log.info("Fetching active meeting for lead ID: {}", leadId);
        
        Optional<Lead> leadOpt = leadRepository.findByUniqueLeadId(leadId);
        if (leadOpt.isEmpty()) {
            log.info("No active meeting found for lead ID: {} (Lead not found)", leadId);
            return ActiveMeetingResponse.builder().build();
        }
        
        Optional<Meeting> meetingOpt = meetingRepository
                .findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(
                        leadOpt.get().getId(), MeetingStatus.SCHEDULED);
        
        if (meetingOpt.isPresent()) {
            return ActiveMeetingResponse.builder()
                    .meetingCode(meetingOpt.get().getMeetingCode())
                    .build();
        }
        
        // Lazily return a pseudo code if this Lead has NO meetings at all to avoid DB insert on GET
        long meetingCount = meetingRepository.countByLeadId(leadOpt.get().getId());
        if (meetingCount == 0) {
            log.info("No meetings found for Lead ID: {}. Returning pseudo code for frontend.", leadId);
            return ActiveMeetingResponse.builder()
                    .meetingCode("NEW_" + leadId)
                    .build();
        }
        
        log.info("No active meeting found for lead ID: {} (Lead already has completed meetings)", leadId);
        
        return ActiveMeetingResponse.builder().build();
    }

    /**
     * Sales-friendly workflow: update current meeting outcome + optionally schedule next follow-up.
     *
     * Delegates entirely to MeetingWorkflowService (orchestrator).
     * MeetingServiceImpl is responsible for meeting CRUD only.
     */
    @Override
    public MeetingResponse processMeetingUpdateWorkflow(String meetingCode, MeetingWorkflowRequest request, String currentUserEmail) {
        log.info("Delegating meeting workflow to MeetingWorkflowService. Meeting: {}, by: {}", meetingCode, currentUserEmail);
        
        if (meetingCode != null && meetingCode.startsWith("NEW_")) {
            String leadId = meetingCode.substring(4);
            Lead lead = leadRepository.findByUniqueLeadId(leadId)
                    .orElseThrow(() -> new LeadNotFoundException(MeetingConstants.INVALID_LEAD));
            
            if (meetingRepository.countByLeadId(lead.getId()) > 0) {
                throw new IllegalArgumentException("Intro meeting already created for this lead.");
            }
            
            log.info("Intercepted pseudo meeting code {}. Creating physical initial meeting now.", meetingCode);
            Meeting initialMeeting = createInitialMeeting(lead);
            meetingCode = initialMeeting.getMeetingCode();
            
            // Rule 8: Move lead out of "Lead Created" queue
            UpdateLeadStatusRequest statusReq = UpdateLeadStatusRequest.builder()
                    .leadId(lead.getId())
                    .leadStatus(LeadStatus.MEETING_SCHEDULED)
                    .leadStage(com.blueant_crm_erp.lead.enums.LeadStage.INTRO_MEETING_SCHEDULED)
                    .remarks("Intro Meeting created. Lead moved to Meeting Scheduled.")
                    .build();
            leadService.changeStatus(statusReq, currentUserEmail);
            log.info("Lead {} status updated to MEETING_SCHEDULED after Intro Meeting creation.", lead.getLeadCode());
        }
        
        return meetingWorkflowService.processWorkflow(meetingCode, request, currentUserEmail);
    }

    // getMeetingTimeline has been removed as it is now handled by ActivityTimeline

    @Override
    @Transactional(readOnly = true)
    public java.util.List<MeetingSummaryResponse> getMeetingsBySequence(String leadId) {
        Lead lead = leadRepository.findByUniqueLeadId(leadId)
                .orElseThrow(() -> new LeadNotFoundException(MeetingConstants.INVALID_LEAD));
        List<Meeting> meetings = meetingRepository.findByLeadIdOrderByMeetingNumberAsc(lead.getId());
        return meetingMapper.toSummaryResponseList(meetings);
    }

    @Override
    public void convertLead(String leadId, String currentUserEmail) {
        Lead lead = leadRepository.findByUniqueLeadId(leadId)
                .orElseThrow(() -> new LeadNotFoundException(MeetingConstants.INVALID_LEAD));
                
        if (LeadStatus.CONVERTED.equals(lead.getLeadStatus())) {
            throw new IllegalArgumentException("Lead is already converted.");
        }
        
        UpdateLeadStatusRequest updateLeadReq = UpdateLeadStatusRequest.builder()
                .leadId(lead.getId())
                .leadStatus(LeadStatus.CONVERTED)
                .leadStage(lead.getLeadStage())
                .remarks("Lead explicitly converted.")
                .build();
        leadService.changeStatus(updateLeadReq, currentUserEmail);
        
        // Find active meeting if any
        Optional<Meeting> activeMeetingOpt = meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), MeetingStatus.SCHEDULED);
        if (activeMeetingOpt.isPresent()) {
            Meeting activeMeeting = activeMeetingOpt.get();
            String previousStatus = activeMeeting.getMeetingStatus().name();
            activeMeeting.setMeetingStatus(MeetingStatus.COMPLETED);
            activeMeeting.setMeetingConducted(MeetingConductStatus.CONDUCTED);
            activeMeeting.setLeadStatus(MeetingLeadStatus.CONVERTED_CLIENT);
            Meeting savedMeeting = meetingRepository.save(activeMeeting);
            
            eventPublisher.publishEvent(new MeetingWorkflowEvent(this, savedMeeting, "CONVERTED", previousStatus, "Lead explicitly converted.", currentUserEmail));
        } else {
            // No active meeting to update, just log the conversion event without meeting
            eventPublisher.publishEvent(new MeetingWorkflowEvent(this, Meeting.builder().lead(lead).meetingNumber(0).build(), "CONVERTED", null, "Lead explicitly converted (no active meeting).", currentUserEmail));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingReportResponse getMeetingReports() {
        long totalMeetings = meetingRepository.count();
        long pending = meetingRepository.countByMeetingStatus(MeetingStatus.SCHEDULED);
        long completed = meetingRepository.countByMeetingStatus(MeetingStatus.COMPLETED);
        
        long convertedLeads = leadRepository.countByLeadStatus(LeadStatus.CONVERTED);
        
        double avgBeforeConversion = convertedLeads > 0 ? (double) completed / convertedLeads : 0.0;
        double successRate = totalMeetings > 0 ? ((double) convertedLeads / totalMeetings) * 100 : 0.0;
        double journeyCompletionRate = convertedLeads > 0 ? 100.0 : 0.0;
        double avgSalesCycle = 15.5; // Stub for average sales cycle in days
        
        java.util.Map<String, Long> funnels = new java.util.HashMap<>();
        funnels.put("Total", totalMeetings);
        funnels.put("Pending", pending);
        funnels.put("Completed", completed);
        
        return MeetingReportResponse.builder()
                .totalMeetings(totalMeetings)
                .meetingsPending(pending)
                .meetingsCompleted(completed)
                .averageMeetingsBeforeConversion(avgBeforeConversion)
                .meetingSuccessRate(successRate)
                .averageSalesCycle(avgSalesCycle)
                .journeyCompletionRate(journeyCompletionRate)
                .conversionFunnels(funnels)
                .build();
    }
}

