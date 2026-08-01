package com.blueant_crm_erp.proposal.validator;

import com.blueant_crm_erp.proposal.dto.request.UpdateProposalRequest;
import com.blueant_crm_erp.proposal.exception.InvalidProposalException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProposalValidator {

    public void validateUpdateRequest(UpdateProposalRequest request) {
        if (request.getInvestmentAmount() != null && request.getInvestmentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProposalException("Investment amount must be positive");
        }
    }
}
