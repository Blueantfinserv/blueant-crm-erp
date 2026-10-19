package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.common.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_family_head_histories")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFamilyHeadHistory extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_famhist_family"))
    private ClientFamily family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_head_client_id", foreignKey = @ForeignKey(name = "fk_cl_famhist_prevhead"))
    private Client previousHead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_head_client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_famhist_newhead"))
    private Client newHead;

    @Column(name = "changed_by_user", nullable = false, length = 100)
    private String changedByUser;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "reason", length = 500)
    private String reason;
}
