package com.blueant_crm_erp.meeting.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.meeting.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "meeting_verifications")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingVerification extends BaseVersionEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_meeting_verification_meeting"))
    private Meeting meeting;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    private VerificationStatus verificationStatus;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    // --- Coordinator collected information ---
    @Column(name = "alone_with", length = 20)
    private String aloneWith;

    @Column(name = "person_name", length = 100)
    private String personName;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "client_age")
    private Integer clientAge;

    @Column(name = "marital_status", length = 50)
    private String maritalStatus;

    @Column(name = "profession", length = 100)
    private String profession;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "any_children")
    private Boolean anyChildren;

    @Column(name = "number_of_children")
    private Integer numberOfChildren;

    @Column(name = "previous_investment")
    private Boolean previousInvestment;

    @Column(name = "meeting_timing")
    private LocalTime meetingTiming;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", length = 50)
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "existing_sip", length = 50)
    private ExistingSip existingSip;

    @Column(name = "profession_detail", length = 255)
    private String professionDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "best_time_for_meeting", length = 50)
    private BestTimeForMeeting bestTimeForMeeting;

    // =========================================================================
    // VERIFIED MEETING SNAPSHOT (Enriched at verification time for reporting)
    // =========================================================================

    // Meeting Identity
    @Column(name = "meeting_code", length = 50)
    private String meetingCode;

    @Column(name = "meeting_number")
    private Integer meetingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", length = 30)
    private MeetingType meetingType;

    @Column(name = "meeting_title", length = 100)
    private String meetingTitle;

    // Lead / Client Snapshot
    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "lead_code", length = 30)
    private String leadCode;

    @Column(name = "client_name", length = 150)
    private String clientName;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    // Sales Person Snapshot
    @Column(name = "assigned_employee_id")
    private Long assignedEmployeeId;

    @Column(name = "employee_code", length = 30)
    private String employeeCode;

    @Column(name = "employee_name", length = 200)
    private String employeeName;

    // Meeting Execution Snapshot
    @Column(name = "meeting_date")
    private LocalDate meetingDate;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_mode", length = 30)
    private MeetingMode meetingMode;

    @Column(name = "meeting_location", length = 255)
    private String meetingLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_status", length = 30)
    private MeetingStatus meetingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "meeting_remarks", length = 1000)
    private String meetingRemarks;

    @Column(name = "next_meeting_date")
    private LocalDate nextMeetingDate;

    @Column(name = "next_meeting_time")
    private LocalTime nextMeetingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_conducted", length = 30)
    private MeetingConductStatus meetingConducted;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_status", length = 50)
    private MeetingLeadStatus leadStatus;

    // Captured Meeting GPS & Visiting Card Data
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "location_accuracy")
    private Double locationAccuracy;

    @Column(name = "location_captured_at")
    private LocalDateTime locationCapturedAt;

    @Column(name = "google_maps_url", length = 512)
    private String googleMapsUrl;

    @Column(name = "visiting_card", length = 500)
    private String visitingCard;
}
