package com.blueant_crm_erp.meeting.entity;

import com.blueant_crm_erp.common.base.BaseAuditEntity;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ============================================================================
 * Meeting Update Entity
 * ============================================================================
 *
 * Immutable audit record capturing every sales meeting update submission.
 *
 * Business Rule: Meeting updates are NEVER overwritten.
 * Each submission creates a new MeetingUpdate record with a sequential
 * update_number, preserving complete audit history.
 *
 * The parent Meeting entity retains denormalized latest values for fast reads.
 *
 * Project : BlueAnt CRM ERP
 * Module  : Meeting Management
 */
@Entity
@Table(name = "meeting_updates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_meeting_update_number", columnNames = {"meeting_id", "update_number"})
        },
        indexes = {
                @Index(name = "idx_meeting_update_meeting", columnList = "meeting_id"),
                @Index(name = "idx_meeting_update_submitted", columnList = "submitted_at")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUpdate extends BaseAuditEntity {

    // ── Parent Reference ──────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false, foreignKey = @ForeignKey(name = "fk_meeting_update_meeting"))
    private Meeting meeting;

    /**
     * Sequential update number per meeting (1, 2, 3, ...).
     */
    @Column(name = "update_number", nullable = false)
    private Integer updateNumber;

    // ── Meeting Context at Time of Update ──────────────────────────────────────

    @Column(name = "meeting_date")
    private LocalDate meetingDate;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_mode", length = 30)
    private MeetingMode meetingMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_conducted", nullable = false, length = 30)
    @Builder.Default
    private MeetingConductStatus meetingConducted = MeetingConductStatus.NOT_CONDUCTED;

    // ── Sales Workflow Fields ──────────────────────────────────────────────────

    @Column(name = "completed_stage", length = 50)
    private String completedStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_status", length = 50)
    private MeetingLeadStatus leadStatus;

    @Column(name = "client_status", length = 50)
    private String clientStatus;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "joined_meeting_with", length = 255)
    private String joinedMeetingWith;

    @Column(name = "leader_name", length = 100)
    private String leaderName;

    @Column(name = "next_plan_date")
    private LocalDate nextPlanDate;

    // ── Investment Fields ──────────────────────────────────────────────────────

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "investment_amount", precision = 15, scale = 2)
    private BigDecimal investmentAmount;

    @Column(name = "product_type", length = 100)
    private String productType;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "next_plan_time")
    private LocalTime nextPlanTime;

    @Column(name = "current_investment_company", length = 150)
    private String currentInvestmentCompany;

    @Column(name = "current_advisor", length = 150)
    private String currentAdvisor;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", length = 50)
    private com.blueant_crm_erp.meeting.enums.InvestmentType investmentType;

    @Column(name = "investment_company", length = 150)
    private String investmentCompany;

    @Column(name = "current_stage", length = 100)
    private String currentStage;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "location_captured_at")
    private LocalDateTime locationCapturedAt;

    @Column(name = "location_accuracy")
    private Double locationAccuracy;

    @Column(name = "google_maps_url", length = 512)
    private String googleMapsUrl;

    @Column(name = "discussion", columnDefinition = "TEXT")
    private String discussion;

    // ── Submission Metadata ───────────────────────────────────────────────────

    @Column(name = "submitted_by", nullable = false, length = 100)
    private String submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
}
