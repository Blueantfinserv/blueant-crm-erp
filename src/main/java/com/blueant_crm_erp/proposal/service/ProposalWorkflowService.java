package com.blueant_crm_erp.proposal.service;

import com.blueant_crm_erp.proposal.entity.Proposal;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ProposalWorkflowService {

    Proposal createProposal(Long leadId, BigDecimal investmentAmount, String productType, LocalDate expectedClosureDate, String remarks, String currentUserEmail);

    Proposal acceptProposal(String proposalCode, String currentUserEmail);

    Proposal rejectProposal(String proposalCode, String reason, String currentUserEmail);

}
