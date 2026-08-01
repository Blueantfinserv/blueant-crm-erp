package com.blueant_crm_erp.proposal.dto.request;

import com.blueant_crm_erp.proposal.enums.ProposalStatus;
import jakarta.validation.constraints.Size;
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
public class UpdateProposalRequest {

    private ProposalStatus proposalStatus;

    private BigDecimal investmentAmount;

    @Size(max = 100, message = "Product type must not exceed 100 characters")
    private String productType;

    private LocalDate expectedClosureDate;

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;
}
