package com.blueant_crm_erp.meeting.repository;

import com.blueant_crm_erp.meeting.entity.MeetingVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingVerificationRepository extends JpaRepository<MeetingVerification, Long> {
    Optional<MeetingVerification> findByMeetingMeetingCode(String meetingCode);
    Optional<MeetingVerification> findByMeetingId(Long meetingId);
}
