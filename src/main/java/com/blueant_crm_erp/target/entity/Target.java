package com.blueant_crm_erp.target.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "targets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_target_user_month", columnNames = {"user_id", "target_month"})
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Target extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_month", nullable = false, length = 7)
    private String targetMonth; // Format: YYYY-MM

    @Column(name = "revenue_target", precision = 15, scale = 2)
    private BigDecimal revenueTarget;

    @Column(name = "meeting_target")
    private Integer meetingTarget;

    @Column(name = "lead_target")
    private Integer leadTarget;

    @Column(name = "followup_target")
    private Integer followupTarget;
}
