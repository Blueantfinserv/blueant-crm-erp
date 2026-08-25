package com.blueant_crm_erp.meeting.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.meeting.enums.AgeGroup;
import com.blueant_crm_erp.meeting.enums.ExistingSip;
import com.blueant_crm_erp.meeting.enums.BestTimeForMeeting;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
}
