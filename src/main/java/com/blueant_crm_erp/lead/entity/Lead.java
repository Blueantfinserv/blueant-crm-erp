package com.blueant_crm_erp.lead.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.lead.enums.*;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ============================================================================
 * Lead Entity
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * System  : CRM and Sales Management System
 * Module  : Lead Management
 *
 * Description:
 * Represents a Lead in the Mutual Fund Distribution system.
 * This entity tracks the entire lifecycle of a lead until conversion and
 * handoff to the CRM Head via a Service Request.
 *
 * NOTE: Historical meeting logs will be maintained in a separate Meeting module.
 * Only the current state is stored here.
 *
 * ============================================================================
 */
@Entity
@Table(name = "leads",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_lead_code", columnNames = "lead_code"),
                @UniqueConstraint(name = "uk_lead_unique_id", columnNames = "unique_lead_id")
        },
        indexes = {
                @Index(name = "idx_lead_mobile", columnList = "mobile_number"),
                @Index(name = "idx_lead_status", columnList = "lead_status"),
                @Index(name = "idx_lead_sales_person", columnList = "assigned_sales_person_id"),
                @Index(name = "idx_lead_leader", columnList = "assigned_leader_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Lead extends BaseVersionEntity {

    // ========================================================================
    // BACKEND AUTO GENERATED (System Info)
    // ========================================================================
    
    @Column(name = "lead_code", nullable = false, length = 30)
    private String leadCode;

    @Column(name = "unique_lead_id", nullable = false, length = 50)
    private String uniqueLeadId;

    // ========================================================================
    // NEW LEAD FORM (Primary Information)
    // ========================================================================
    
    @Column(name = "client_name", nullable = false, length = 150)
    private String clientName;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "alternate_number", length = 20)
    private String alternateNumber;

    @Column(name = "email", length = 150)
    private String email;

    // ========================================================================
// Lead Location Information
// ========================================================================

    /**
     * Human-readable address selected or resolved from GPS.
     * Example:
     * Connaught Place, New Delhi, India
     */
    @Column(name = "location", length = 255)
    private String location;

    /**
     * GPS Latitude
     */
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;


    @Enumerated(EnumType.STRING)
    @Column(name = "profession", length = 50)
    private Profession profession;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_source", nullable = false, length = 50)
    private LeadSource leadSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_type", nullable = false, length = 50)
    private LeadType leadType;

    @Column(name = "remarks", length = 1000)
    private String remarks; // Initial Remarks

    // ========================================================================
    // AUTO GENERATED (Process Information)
    // ========================================================================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lead_status", nullable = false, length = 50)
    private LeadStatus leadStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_stage", nullable = false, length = 50)
    private LeadStage leadStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private LeadPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "duplicate_lead_status", nullable = false, length = 50)
    private DuplicateLeadStatus duplicateLeadStatus;

    // --- Assignment ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_sales_person_id", foreignKey = @ForeignKey(name = "fk_lead_sales_person"))
    private User assignedSalesPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_leader_id", foreignKey = @ForeignKey(name = "fk_lead_leader"))
    private User assignedLeader;


    // ========================================================================
    // UPDATE LEAD FORM (Meeting & Follow-up Information)
    // ========================================================================

    @Column(name = "meeting_date")
    private LocalDateTime meetingDate;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;


    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_mode", length = 30)
    @Builder.Default
    private MeetingMode meetingMode = MeetingMode.PHYSICAL;

    @Column(name = "meeting_location", length = 200)
    private String meetingLocation;

    @Column(name = "meeting_with", length = 150)
    private String meetingWith;

    @Column(name = "leader_present")
    private Boolean leaderPresent;

    @Column(name = "discussion", length = 2000)
    private String discussion;

    @Column(name = "meeting_remarks", length = 1000)
    private String meetingRemarks;

    // --- Timestamps & Triggers ---
    @Column(name = "next_plan_date")
    private LocalDateTime nextPlanDate;

    @Column(name = "last_call_date")
    private LocalDateTime lastCallDate;

    // ========================================================================
    // CONVERTED LEAD INFORMATION
    // ========================================================================



}
