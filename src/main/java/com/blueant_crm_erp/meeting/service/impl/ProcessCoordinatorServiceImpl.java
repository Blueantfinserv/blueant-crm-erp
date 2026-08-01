package com.blueant_crm_erp.meeting.service.impl;

import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.service.ProcessCoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProcessCoordinatorServiceImpl implements ProcessCoordinatorService {

    private final MeetingRepository meetingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Meeting verifyMeeting(String meetingCode, String remarks, String currentUserEmail) {
        log.info("Verifying meeting: {} by PC: {}", meetingCode, currentUserEmail);
        
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid meeting code"));
                
        meeting.setVerifiedByProcessCoordinator(true);
        meeting.setMeetingVerificationDate(LocalDateTime.now());
        meeting.setVerificationRemarks(remarks);
        meeting.setVerifiedBy(currentUserEmail);
        
        meeting = meetingRepository.save(meeting);
        // Note: Real business logic for syncing Lead Status should be dispatched here.
        return meeting;
    }

    @Override
    public Meeting rejectMeeting(String meetingCode, String reason, String currentUserEmail) {
        log.info("Rejecting meeting verification: {} by PC: {}", meetingCode, currentUserEmail);
        
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid meeting code"));
                
        meeting.setVerifiedByProcessCoordinator(false);
        meeting.setMeetingVerificationDate(LocalDateTime.now());
        meeting.setVerificationRemarks("REJECTED: " + reason);
        meeting.setVerifiedBy(currentUserEmail);
        
        return meetingRepository.save(meeting);
    }
}
