package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientMeetingRepository extends JpaRepository<ClientMeeting, Long>, JpaSpecificationExecutor<ClientMeeting> {

    Optional<ClientMeeting> findByMeetingCode(String meetingCode);

    Optional<ClientMeeting> findByMeetingCodeAndAssignedEmployeeId(String meetingCode, Long assignedEmployeeId);

    boolean existsByMeetingCode(String meetingCode);

    boolean existsByClientIdAndMeetingStatus(Long clientId, MeetingStatus meetingStatus);

    long countByClientIdAndMeetingStatus(Long clientId, MeetingStatus meetingStatus);

    java.util.List<ClientMeeting> findByClientIdInAndMeetingStatus(java.util.List<Long> clientIds, MeetingStatus meetingStatus);

    Optional<ClientMeeting> findTopByClientIdOrderByMeetingNumberDesc(Long clientId);

    Optional<ClientMeeting> findTopByClientIdAndMeetingStatusOrderByMeetingNumberDesc(Long clientId, MeetingStatus meetingStatus);

    Page<ClientMeeting> findByAssignedEmployeeId(Long employeeId, Pageable pageable);

    Page<ClientMeeting> findByAssignedEmployeeIdAndMeetingStatus(Long employeeId, MeetingStatus status, Pageable pageable);

    Page<ClientMeeting> findByClientId(Long clientId, Pageable pageable);

    @Query("SELECT MAX(CAST(SUBSTRING(m.meetingCode, 13) AS long)) FROM ClientMeeting m WHERE m.meetingCode LIKE 'BA-CLM-%'")
    Long findMaxClientMeetingCodeSequence();
}
