package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.common.base.BaseAuditEntity;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_assignment_histories",
        indexes = {
                @Index(name = "idx_cl_hist_client", columnList = "client_id"),
                @Index(name = "idx_cl_hist_to_user", columnList = "assigned_to_user_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAssignmentHistory extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_hist_client"))
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_from_user_id", foreignKey = @ForeignKey(name = "fk_cl_hist_from_user"))
    private User assignedFromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_hist_to_user"))
    private User assignedToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_hist_by_user"))
    private User assignedByUser;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "reason", length = 500)
    private String reason;
}
