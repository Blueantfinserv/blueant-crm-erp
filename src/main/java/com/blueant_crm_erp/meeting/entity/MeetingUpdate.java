package com.blueant_crm_erp.meeting.entity;

import com.blueant_crm_erp.common.base.BaseAuditEntity;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
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

    @Column(name = "meeting_conducted", nullable = false)
    @Builder.Default
    private Boolean meetingConducted = Boolean.FALSE;

    // ── Sales Workflow Fields ──────────────────────────────────────────────────

    @Column(name = "completed_stage", length = 50)
    private String completedStage;

    @Column(name = "lead_status", length = 50)
    private String leadStatus;

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

    // ── Outcome ───────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_outcome", length = 30)
    private MeetingOutcome meetingOutcome;

    @Column(name = "discussion", columnDefinition = "TEXT")
    private String discussion;

    // ── Submission Metadata ───────────────────────────────────────────────────

    @Column(name = "submitted_by", nullable = false, length = 100)
    private String submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
}
