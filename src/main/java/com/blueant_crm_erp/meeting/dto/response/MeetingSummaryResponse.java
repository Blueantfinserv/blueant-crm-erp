package com.blueant_crm_erp.meeting.dto.response;

import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSummaryResponse {

    /**
     * Database Id
     */
    private Long id;

    /**
     * Business Meeting Code
     */
    private String meetingCode;

    /**
     * Meeting Number
     * 0 = Intro Meeting
     * 1 = First Meeting
     * 2 = Second Meeting
     */
    private Integer meetingNumber;
    private MeetingType meetingType;
    private String meetingTitle;


    /**
     * Meeting Status
     */
    private MeetingStatus meetingStatus;

    /**
     * Meeting Date
     */
    private LocalDate meetingDate;

    /**
     * Meeting Time
     */
    private LocalTime meetingTime;

    private LocalDate nextMeetingDate;
    private LocalTime nextMeetingTime;

    private String aloneWith;
    private String personName;
    private String position;

    /**
     * Extended fields required by frontend list view
     */
    private String clientName;
    private Long leadId;
    private String leadCode;
    private String assignedEmployeeName;
    private String location;
    
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

}