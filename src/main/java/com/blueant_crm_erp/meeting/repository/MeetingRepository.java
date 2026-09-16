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
     * Find by Meeting Code ignoring case
     */
    Optional<Meeting> findByMeetingCodeIgnoreCase(String meetingCode);

    /**
     * Find by Meeting Code with TRIM and Case-Insensitive matching
     */
    @org.springframework.data.jpa.repository.Query("SELECT m FROM Meeting m WHERE TRIM(m.meetingCode) = TRIM(:meetingCode) OR UPPER(TRIM(m.meetingCode)) = UPPER(TRIM(:meetingCode))")
    Optional<Meeting> findByMeetingCodeNormalized(@org.springframework.data.repository.query.Param("meetingCode") String meetingCode);

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
     * Find meeting of a Lead by meeting sequence number
     */
    Optional<Meeting> findByLeadIdAndMeetingNumber(Long leadId, Integer meetingNumber);

    /**
     * Find maximum numeric sequence from existing meeting codes
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(m.meeting_code, 13) AS UNSIGNED)), 0) FROM meetings m WHERE m.meeting_code LIKE 'BA-MTG-%' AND LENGTH(m.meeting_code) >= 18", nativeQuery = true)
    Long findMaxMeetingCodeSequence();

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