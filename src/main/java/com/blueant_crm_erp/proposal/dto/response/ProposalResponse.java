package com.blueant_crm_erp.proposal.dto.response;

import com.blueant_crm_erp.proposal.enums.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponse {

    private Long id;
    private Long leadId;
    private String proposalCode;
    private ProposalStatus proposalStatus;
    private BigDecimal investmentAmount;
    private String productType;
    private LocalDate expectedClosureDate;
    private String remarks;
}
