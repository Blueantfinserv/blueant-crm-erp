package com.blueant_crm_erp.negotiation.service;

import com.blueant_crm_erp.negotiation.entity.Negotiation;
import com.blueant_crm_erp.proposal.entity.Proposal;

import java.math.BigDecimal;

public interface NegotiationWorkflowService {

    Negotiation startNegotiation(Proposal proposal, String currentUserEmail);

    Negotiation updateNegotiation(String negotiationCode, BigDecimal agreedAmount, String discussion, String currentUserEmail);

    Negotiation closeNegotiation(String negotiationCode, String currentUserEmail);
}
