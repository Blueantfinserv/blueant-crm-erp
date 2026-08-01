package com.blueant_crm_erp.followup.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.lead.entity.Lead;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "followups")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUp extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @Column(name = "followup_date", nullable = false)
    private LocalDate followupDate;

    @Column(name = "followup_time", nullable = false)
    private LocalTime followupTime;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "reminder")
    private Boolean reminder;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "completed_by", length = 100)
    private String completedBy;

    @Column(name = "next_followup_date")
    private LocalDate nextFollowupDate;
}
