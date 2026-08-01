package com.blueant_crm_erp.proposal.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.proposal.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ============================================================================
 * Proposal Entity
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Proposal Management
 */
@Entity
@Table(name = "proposals")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Proposal extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @Column(name = "proposal_code", nullable = false, unique = true, length = 50)
    private String proposalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposal_status", nullable = false, length = 50)
    private ProposalStatus proposalStatus;

    @Column(name = "investment_amount")
    private BigDecimal investmentAmount;

    @Column(name = "product_type", length = 100)
    private String productType;

    @Column(name = "expected_closure_date")
    private LocalDate expectedClosureDate;

    @Column(name = "remarks", length = 1000)
    private String remarks;

}
