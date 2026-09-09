package com.blueant_crm_erp.meeting.dto.response;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.meeting.enums.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingVerificationResponse {

    private Long id;
    private VerificationStatus verificationStatus;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private String rejectionReason;

    // --- Coordinator collected information ---
    private String aloneWith;
    private String personName;
    private String position;
    private Integer clientAge;
    private String maritalStatus;
    private String profession;
    private String email;
    private String companyName;
    private Boolean anyChildren;
    private Integer numberOfChildren;
    private Boolean previousInvestment;

    // New questionnaire fields
    private LocalTime meetingTiming;
    private AgeGroup ageGroup;
    private ExistingSip existingSip;
    private String professionDetail;
    private BestTimeForMeeting bestTimeForMeeting;
    private String meetingWith;

    // --- Verified Meeting Snapshot ---
    // Meeting Identity
    private String meetingCode;
    private Integer meetingNumber;
    private MeetingType meetingType;
    private String meetingTitle;

    // Lead / Client
    private Long leadId;
    private String leadCode;
    private String clientName;
    private String mobileNumber;

    // Sales Person
    private Long assignedEmployeeId;
    private String employeeCode;
    private String employeeName;

    // Meeting Execution
    private LocalDate meetingDate;
    private LocalTime meetingTime;
    private MeetingMode meetingMode;
    private String meetingLocation;
    private MeetingStatus meetingStatus;
    private Status status;
    private String meetingRemarks;
    private LocalDate nextMeetingDate;
    private LocalTime nextMeetingTime;
    private MeetingConductStatus meetingConducted;
    private MeetingLeadStatus leadStatus;

    // Captured Meeting GPS & Visiting Card
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double locationAccuracy;
    private LocalDateTime locationCapturedAt;
    private String googleMapsUrl;
    private String visitingCard;
}
