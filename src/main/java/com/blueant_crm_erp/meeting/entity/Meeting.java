package com.blueant_crm_erp.meeting.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "meetings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_meeting_code", columnNames = "meeting_code"),
                @UniqueConstraint(name = "uk_lead_meeting_sequence", columnNames = {"lead_id", "meeting_sequence"})
        },
        indexes = {
                @Index(name = "idx_meeting_status", columnList = "meeting_status"),
                @Index(name = "idx_meeting_date", columnList = "meeting_date"),
                @Index(name = "idx_meeting_lead", columnList = "lead_id"),
                @Index(name = "idx_meeting_employee", columnList = "assigned_employee_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Meeting extends BaseVersionEntity {

    @Column(name = "meeting_code", nullable = false, length = 30)
    private String meetingCode;

    @Column(name = "meeting_sequence", nullable = false)
    private Integer meetingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false, length = 30)
    private com.blueant_crm_erp.meeting.enums.MeetingType meetingType;

    @Column(name = "meeting_title", length = 100)
    private String meetingTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", foreignKey = @ForeignKey(name = "fk_meeting_lead"))
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id", foreignKey = @ForeignKey(name = "fk_meeting_employee"))
    private User assignedEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_mode", nullable = false, length = 30)
    private MeetingMode meetingMode;

    // TODO: Future normalization should replace this comma-separated storage
    @Column(name = "company_participants", length = 500)
    private String companyParticipants;

    // TODO: Future normalization should replace this comma-separated storage
    @Column(name = "client_participants", length = 500)
    private String clientParticipants;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "meeting_time", nullable = false)
    private LocalTime meetingTime;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "landmark", length = 255)
    private String landmark;

    @Column(name = "meeting_location", length = 255)
    private String meetingLocation;

    @Column(name = "google_location", length = 255)
    private String googleLocation;

    @Column(name = "agenda", length = 500)
    private String agenda;

    @Column(name = "remarks", length = 1000)
    private String meetingRemarks;

    @Column(name = "discussion", length = 2000)
    private String discussion;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_status", nullable = false, length = 30)
    private MeetingStatus meetingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_outcome", length = 30)
    private MeetingOutcome meetingOutcome;

    @Column(name = "client_interest_level", length = 50)
    private String clientInterestLevel;

    @Column(name = "estimated_investment_amount", precision = 15, scale = 2)
    private java.math.BigDecimal estimatedInvestmentAmount;

    @Column(name = "expected_closing_date")
    private LocalDate expectedClosingDate;

    @Column(name = "meeting_photo", length = 500)
    private String meetingPhoto;

    @Column(name = "visiting_card", length = 500)
    private String visitingCard;

    @Column(name = "meeting_notes", length = 2000)
    private String meetingNotes;

    @Column(name = "next_meeting_date")
    private LocalDate nextMeetingDate;

    @Column(name = "next_meeting_time")
    private LocalTime nextMeetingTime;

    // --- Process Coordinator Verification ---
    @Column(name = "verified_by_process_coordinator")
    @Builder.Default
    private Boolean verifiedByProcessCoordinator = Boolean.FALSE;

    @Column(name = "alone_with", length = 20)
    private String aloneWith;

    @Column(name = "person_name", length = 100)
    private String personName;

    @Column(name = "position", length = 100)
    private String position;

    @PrePersist
    @PreUpdate
    protected void onPrePersistUpdate() {
        if ("SELF".equalsIgnoreCase(aloneWith)) {
            this.personName = null;
            this.position = null;
        }
    }

    @Column(name = "meeting_verification_date")
    private java.time.LocalDateTime meetingVerificationDate;

    @Column(name = "verification_remarks", length = 1000)
    private String verificationRemarks;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;
}
