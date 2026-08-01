package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.exception.meeting.MeetingNotFoundException;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingOutcomeRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.mapper.MeetingMapper;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.MeetingOutcomeService;
import com.blueant_crm_erp.meeting.validator.MeetingOutcomeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MeetingOutcomeServiceImpl implements MeetingOutcomeService {

    private final MeetingRepository meetingRepository;
    private final MeetingMapper meetingMapper;
    private final MeetingOutcomeValidator meetingOutcomeValidator;

    @Override
    public MeetingResponse updateMeetingOutcome(String meetingCode, UpdateMeetingOutcomeRequest request, String currentUserEmail) {
        log.info("Updating meeting outcome for meeting: {}, requested by: {}", meetingCode, currentUserEmail);
        Meeting meeting = getMeetingByCodeInternal(meetingCode);

        meetingOutcomeValidator.validateOutcome(meeting, request.getMeetingOutcome());
        
        meeting.setMeetingOutcome(request.getMeetingOutcome());
        meeting.setMeetingRemarks(request.getMeetingRemarks());
        meeting.setMeetingStatus(MeetingStatus.COMPLETED);

        Meeting savedMeeting = meetingRepository.save(meeting);
        return meetingMapper.toResponse(savedMeeting);
    }

    @Override
    public MeetingResponse markAsConverted(String meetingCode, String currentUserEmail) {
        log.info("Marking meeting as converted: {}, requested by: {}", meetingCode, currentUserEmail);
        Meeting meeting = getMeetingByCodeInternal(meetingCode);

        if (MeetingOutcome.CONVERTED.equals(meeting.getMeetingOutcome())) {
            throw new IllegalArgumentException(MeetingConstants.MEETING_ALREADY_CONVERTED);
        }

        meetingOutcomeValidator.validateOutcome(meeting, MeetingOutcome.CONVERTED);
        
        meeting.setMeetingOutcome(MeetingOutcome.CONVERTED);
        meeting.setMeetingStatus(MeetingStatus.COMPLETED);

        Meeting savedMeeting = meetingRepository.save(meeting);
        return meetingMapper.toResponse(savedMeeting);
    }

    @Override
    public MeetingResponse markAsRejected(String meetingCode, String currentUserEmail) {
        log.info("Marking meeting as rejected: {}, requested by: {}", meetingCode, currentUserEmail);
        Meeting meeting = getMeetingByCodeInternal(meetingCode);

        if (MeetingOutcome.REJECTED.equals(meeting.getMeetingOutcome())) {
            throw new IllegalArgumentException(MeetingConstants.MEETING_ALREADY_REJECTED);
        }

        meetingOutcomeValidator.validateOutcome(meeting, MeetingOutcome.REJECTED);
        
        meeting.setMeetingOutcome(MeetingOutcome.REJECTED);
        meeting.setMeetingStatus(MeetingStatus.COMPLETED);

        Meeting savedMeeting = meetingRepository.save(meeting);
        return meetingMapper.toResponse(savedMeeting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingDetailResponse> getMeetingsByOutcome(MeetingOutcome outcome) {
        log.info("Fetching meetings by outcome: {}", outcome);
        List<Meeting> meetings = meetingRepository.findByMeetingOutcome(outcome);
        return meetings.stream().map(meetingMapper::toDetailResponse).collect(Collectors.toList());
    }

    private Meeting getMeetingByCodeInternal(String meetingCode) {
        return meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new MeetingNotFoundException(MeetingConstants.MEETING_NOT_FOUND));
    }
}
