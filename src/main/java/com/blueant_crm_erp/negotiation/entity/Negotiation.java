package com.blueant_crm_erp.negotiation.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.negotiation.enums.NegotiationStatus;
import com.blueant_crm_erp.proposal.entity.Proposal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "negotiations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Negotiation extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @Column(name = "negotiation_code", nullable = false, unique = true, length = 50)
    private String negotiationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "negotiation_status", nullable = false, length = 50)
    private NegotiationStatus negotiationStatus;

    @Column(name = "final_agreed_amount")
    private BigDecimal finalAgreedAmount;

    @Column(name = "final_product_type", length = 100)
    private String finalProductType;

}
