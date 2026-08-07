package com.blueant_crm_erp.meeting.repository;

import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long>,
        JpaSpecificationExecutor<Meeting> {

    /**
     * Find by Meeting Code
     */
    Optional<Meeting> findByMeetingCode(String meetingCode);

    /**
     * Check Meeting Code Exists
     */
    boolean existsByMeetingCode(String meetingCode);

    /**
     * Find all meetings by Lead ID
     */
    java.util.List<Meeting> findByLeadIdOrderByMeetingNumberAsc(Long leadId);

    long countByLeadId(Long leadId);

    long countByMeetingStatus(MeetingStatus status);

    /**
     * Find latest meeting of a Lead by ID
     */
    Optional<Meeting> findTopByLeadIdOrderByMeetingNumberDesc(Long leadId);

    /**
     * Find meetings by status
     */
    List<Meeting> findByMeetingStatus(MeetingStatus meetingStatus);

    /**
     * Find meetings by date
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"lead", "assignedEmployee"})
    List<Meeting> findByMeetingDate(LocalDate meetingDate);

    /**
     * Find meetings between dates
     */
    List<Meeting> findByMeetingDateBetween(LocalDate fromDate,
                                           LocalDate toDate);

    /**
     * Find meetings assigned to Employee
     */
    List<Meeting> findByAssignedEmployeeEmployeeCode(String employeeCode);

    /**
     * Check Intro Meeting Exists by Lead ID
     */
    boolean existsByLeadIdAndMeetingNumber(Long leadId,
                                           Integer meetingNumber);

    /**
     * Check if Lead has an active meeting by status
     */
    boolean existsByLeadIdAndMeetingStatus(Long leadId, MeetingStatus meetingStatus);

    /**
     * Find meetings after or equal to date
     */
    List<Meeting> findByMeetingDateGreaterThanEqual(java.time.LocalDate date);

    /**
     * Find the latest SCHEDULED (ACTIVE) meeting for a lead by Lead ID.
     * Used by the workflow active-meeting lookup — returns meetingCode for the frontend.
     */
    Optional<Meeting> findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(
            Long leadId, MeetingStatus meetingStatus);
            
    /**
     * Find meeting history for a lead (e.g. COMPLETED meetings).
     */
    List<Meeting> findByLeadIdAndMeetingStatusOrderByMeetingNumberAsc(Long leadId, MeetingStatus meetingStatus);
}