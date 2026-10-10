package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "client_meeting_verifications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cl_meeting_verif_attempt", columnNames = {"client_meeting_id", "attempt_number"})
        },
        indexes = {
                @Index(name = "idx_cl_verif_meeting", columnList = "client_meeting_id"),
                @Index(name = "idx_cl_verif_status", columnList = "verification_status"),
                @Index(name = "idx_cl_verif_current", columnList = "client_meeting_id, is_current")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientMeetingVerification extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_meeting_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_verif_meeting"))
    private ClientMeeting clientMeeting;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = Boolean.TRUE;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    private VerificationStatus verificationStatus;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "new_meeting_date")
    private LocalDate newMeetingDate;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @Column(name = "blueant_app_used", length = 10)
    private String blueantAppUsed;

    @Column(name = "alone_with", length = 20)
    private String aloneWith;

    @Column(name = "person_name", length = 100)
    private String personName;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "business_generated", length = 10)
    private String businessGenerated;

    @Column(name = "investment_type", length = 100)
    private String investmentType;

    @Column(name = "investment_amount", precision = 15, scale = 2)
    private BigDecimal investmentAmount;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "next_followup_date")
    private LocalDate nextFollowupDate;
}
