package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.dto.request.UpdateLeadStatusRequest;
import com.blueant_crm_erp.lead.service.LeadService;
import org.springframework.context.annotation.Lazy;
import com.blueant_crm_erp.exception.lead.LeadNotFoundException;
import com.blueant_crm_erp.exception.meeting.MeetingNotFoundException;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.RescheduleMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.mapper.MeetingMapper;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.MeetingScheduleService;
import com.blueant_crm_erp.meeting.validator.MeetingScheduleValidator;
import com.blueant_crm_erp.util.id.MeetingCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MeetingScheduleServiceImpl implements MeetingScheduleService {

    private final MeetingRepository meetingRepository;
    private final LeadRepository leadRepository;
    private final MeetingMapper meetingMapper;
    private final MeetingScheduleValidator meetingScheduleValidator;
    private final @Lazy LeadService leadService;

    @Override
    public MeetingResponse scheduleMeeting(CreateMeetingRequest request, String currentUserEmail) {
        log.info("Scheduling new meeting for lead ID: {}, requested by: {}", request.getLeadId(), currentUserEmail);
        meetingScheduleValidator.validateSchedule(request);

        Lead lead = leadRepository.findByUniqueLeadId(request.getLeadId().toString())
                .orElseThrow(() -> new LeadNotFoundException(MeetingConstants.INVALID_LEAD));

        Meeting meeting = meetingMapper.toEntity(request);
        
        long count = meetingRepository.count() + 1;
        meeting.setMeetingCode(MeetingCodeGenerator.generate(count));
        
        Optional<Meeting> lastMeeting = meetingRepository.findTopByLeadIdOrderByMeetingNumberDesc(lead.getId());
        int nextMeetingNumber = lastMeeting.map(m -> m.getMeetingNumber() + 1).orElse(MeetingConstants.FIRST_MEETING_NUMBER);
        if (nextMeetingNumber > 10) {
            throw new IllegalArgumentException("Maximum allowed meeting sequence reached. Cannot schedule Meeting #10.");
        }
        meeting.setMeetingNumber(nextMeetingNumber);
        meeting.setMeetingType(com.blueant_crm_erp.meeting.enums.MeetingType.INTRO);

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
                    .leadStage(LeadStage.INTRO_MEETING_SCHEDULED)
                    .remarks("Intro Meeting scheduled manually. Lead moved to Meeting Scheduled.")
                    .build();
            leadService.changeStatus(statusReq, currentUserEmail);
            log.info("Lead {} status updated to MEETING_SCHEDULED after manual Intro Meeting creation.", lead.getLeadCode());
        }

        return meetingMapper.toResponse(savedMeeting);
    }

    @Override
    public MeetingResponse rescheduleMeeting(String meetingCode, RescheduleMeetingRequest request, String currentUserEmail) {
        log.info("Rescheduling meeting: {}, requested by: {}", meetingCode, currentUserEmail);
        Meeting meeting = getMeetingByCodeInternal(meetingCode);
        
        if (MeetingStatus.COMPLETED.equals(meeting.getMeetingStatus()) || MeetingStatus.CANCELLED.equals(meeting.getMeetingStatus())) {
            throw new IllegalArgumentException(MeetingConstants.INVALID_MEETING_STATUS);
        }

        meeting.setMeetingDate(request.getMeetingDate());
        meeting.setMeetingTime(request.getMeetingTime());
        meeting.setMeetingLocation(request.getMeetingLocation());
        meeting.setMeetingRemarks(request.getRescheduleReason());
        meeting.setMeetingStatus(MeetingStatus.RESCHEDULED);

        Meeting savedMeeting = meetingRepository.save(meeting);
        return meetingMapper.toResponse(savedMeeting);
    }

    @Override
    public void cancelMeeting(String meetingCode, String currentUserEmail) {
        log.info("Cancelling meeting: {}, requested by: {}", meetingCode, currentUserEmail);
        Meeting meeting = getMeetingByCodeInternal(meetingCode);
        
        if (MeetingStatus.COMPLETED.equals(meeting.getMeetingStatus())) {
            throw new IllegalArgumentException(MeetingConstants.MEETING_ALREADY_COMPLETED);
        }
        
        meeting.setMeetingStatus(MeetingStatus.CANCELLED);
        meetingRepository.save(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingDetailResponse> getTodayMeetings() {
        log.info("Fetching today's meetings");
        List<Meeting> meetings = meetingRepository.findByMeetingDate(LocalDate.now());
        return meetings.stream().map(meetingMapper::toDetailResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingDetailResponse> getUpcomingMeetings() {
        log.info("Fetching upcoming meetings");
        List<Meeting> meetings = meetingRepository.findByMeetingDateGreaterThanEqual(LocalDate.now());
        return meetings.stream().map(meetingMapper::toDetailResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingDetailResponse> getMeetingsByDate(LocalDate meetingDate) {
        log.info("Fetching meetings by date: {}", meetingDate);
        List<Meeting> meetings = meetingRepository.findByMeetingDate(meetingDate);
        return meetings.stream().map(meetingMapper::toDetailResponse).collect(Collectors.toList());
    }

    private Meeting getMeetingByCodeInternal(String meetingCode) {
        return meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingConstants.MEETING_NOT_FOUND));
    }
}
