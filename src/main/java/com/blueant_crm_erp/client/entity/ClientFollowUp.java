package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "client_followups",
        indexes = {
                @Index(name = "idx_client_followup_client", columnList = "client_id"),
                @Index(name = "idx_client_followup_sp", columnList = "sales_person_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFollowUp extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_client_followup_client"))
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_client_followup_sales_person"))
    private User salesPerson;

    @Column(name = "followup_date", nullable = false)
    private LocalDate followupDate;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "next_followup_date", nullable = false)
    private LocalDate nextFollowupDate;
}
