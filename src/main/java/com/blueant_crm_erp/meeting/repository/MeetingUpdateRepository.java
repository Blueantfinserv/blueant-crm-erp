package com.blueant_crm_erp.meeting.repository;

import com.blueant_crm_erp.meeting.entity.MeetingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingUpdateRepository extends JpaRepository<MeetingUpdate, Long> {

    /**
     * Find all updates for a meeting, ordered by update number ascending.
     */
    List<MeetingUpdate> findByMeetingIdOrderByUpdateNumberAsc(Long meetingId);

    /**
     * Find the latest update for a meeting.
     */
    Optional<MeetingUpdate> findTopByMeetingIdOrderByUpdateNumberDesc(Long meetingId);

    /**
     * Count updates for a meeting (used to calculate next update_number).
     */
    long countByMeetingId(Long meetingId);
}
