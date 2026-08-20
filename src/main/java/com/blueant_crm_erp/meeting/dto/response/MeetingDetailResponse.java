package com.blueant_crm_erp.meeting.dto.response;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.meeting.enums.MeetingType;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDetailResponse {

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
     * Lead Information
     */
    private Long leadId;

    private String leadCode;

    private String clientName;

    private String mobileNumber;

    /**
     * Assigned Employee
     */
    private Long assignedEmployeeId;

    private String employeeCode;

    private String employeeName;

    /**
     * Meeting Status
     */
    private MeetingStatus meetingStatus;

    /**
     * Meeting Mode
     */
    private MeetingMode meetingMode;

    /**
     * Meeting Date
     */
    private LocalDate meetingDate;

    /**
     * Meeting Time
     */
    private LocalTime meetingTime;

    /**
     * Meeting Location
     */
    private String meetingLocation;

    /**
     * Agenda
     */
    private String agenda;

    /**
     * Remarks
     */
    private String remarks;

    /**
     * Active / Inactive
     */
    private Status status;

    /**
     * Audit Information
     */
    private String createdBy;

    private LocalDate createdDate;

    private String lastModifiedBy;

    private LocalDate lastModifiedDate;

    private List<Long> companyParticipantIds;
    private List<String> clientParticipants;
    private String address;
    private String landmark;
    private String googleLocation;
    private String discussion;

    private String aloneWith;
    private String personName;
    private String position;

    private String clientInterestLevel;
    private java.math.BigDecimal estimatedInvestmentAmount;
    private LocalDate expectedClosingDate;
    private String meetingPhoto;
    private String visitingCard;
    private String meetingNotes;

    private LocalDate nextMeetingDate;
    private LocalTime nextMeetingTime;

    private MeetingConductStatus meetingConducted;
    private MeetingLeadStatus leadStatus;
    private String reason;
    private String currentInvestmentCompany;
    private String currentAdvisor;
    private com.blueant_crm_erp.meeting.enums.InvestmentType investmentType;
    private String investmentCompany;
    private String currentStage;
    private String panNumber;
    private java.math.BigDecimal investmentAmount;

    // GPS fields
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    private java.time.LocalDateTime locationCapturedAt;
    private Double locationAccuracy;
    private String googleMapsUrl;

    private MeetingVerificationResponse verification;
}