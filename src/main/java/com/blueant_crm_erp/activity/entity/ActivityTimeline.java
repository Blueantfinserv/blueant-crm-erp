package com.blueant_crm_erp.activity.entity;

import com.blueant_crm_erp.activity.enums.ActivityType;
import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.lead.entity.Lead;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activity_timeline",
        indexes = {
                @Index(name = "idx_activity_timeline_lead", columnList = "lead_id"),
                @Index(name = "idx_activity_timeline_type", columnList = "activity_type"),
                @Index(name = "idx_activity_timeline_ref", columnList = "reference_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTimeline extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false, foreignKey = @ForeignKey(name = "fk_activity_timeline_lead"))
    private Lead lead;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Column(name = "outcome", length = 50)
    private String outcome;

    @Column(name = "previous_status", length = 50)
    private String previousStatus;

    @Column(name = "current_status", length = 50)
    private String currentStatus;

}
