package com.blueant_crm_erp.meeting.dto.response;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {

    private Long id;

    private String meetingCode;

    private Integer meetingNumber;
    private com.blueant_crm_erp.meeting.enums.MeetingType meetingType;

    private String meetingTitle;

    /**
     * Lead Information
     */
    private String leadCode;
    private String clientName;
    private String mobileNumber;

    /**
     * Assigned Employee
     */
    private String employeeCode;
    private String employeeName;

    private MeetingStatus meetingStatus;

    private MeetingOutcome meetingOutcome;

    private MeetingMode meetingMode;

    private LocalDate meetingDate;

    private LocalTime meetingTime;

    private String meetingLocation;

    private Status status;

    private List<Long> companyParticipantIds;
    private List<String> clientParticipants;
    private String address;
    private String landmark;
    private String googleLocation;
    private String discussion;
    private String clientInterestLevel;
    private java.math.BigDecimal estimatedInvestmentAmount;
    private LocalDate expectedClosingDate;
    private String meetingPhoto;
    private String visitingCard;
    private String meetingNotes;
    private String meetingRemarks;
    private LocalDate nextMeetingDate;
    private LocalTime nextMeetingTime;

}