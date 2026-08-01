package com.blueant_crm_erp.proposal.service.impl;

import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.proposal.entity.Proposal;
import com.blueant_crm_erp.proposal.enums.ProposalStatus;
import com.blueant_crm_erp.proposal.event.ProposalAcceptedEvent;
import com.blueant_crm_erp.proposal.event.ProposalCreatedEvent;
import com.blueant_crm_erp.proposal.event.ProposalRejectedEvent;
import com.blueant_crm_erp.proposal.repository.ProposalRepository;
import com.blueant_crm_erp.proposal.service.ProposalWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProposalWorkflowServiceImpl implements ProposalWorkflowService {

    private final ProposalRepository proposalRepository;
    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Proposal createProposal(Long leadId, BigDecimal investmentAmount, String productType, LocalDate expectedClosureDate, String remarks, String currentUserEmail) {
        log.info("Creating proposal for lead: {}, by: {}", leadId, currentUserEmail);

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lead ID"));

        Proposal proposal = Proposal.builder()
                .lead(lead)
                .proposalCode("PRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .proposalStatus(ProposalStatus.CREATED)
                .investmentAmount(investmentAmount)
                .productType(productType)
                .expectedClosureDate(expectedClosureDate)
                .remarks(remarks)
                .build();

        Proposal savedProposal = proposalRepository.save(proposal);

        eventPublisher.publishEvent(new ProposalCreatedEvent(this, savedProposal, "Proposal created.", currentUserEmail));

        return savedProposal;
    }

    @Override
    public Proposal acceptProposal(String proposalCode, String currentUserEmail) {
        log.info("Accepting proposal: {}, by: {}", proposalCode, currentUserEmail);

        Proposal proposal = proposalRepository.findByProposalCode(proposalCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal code"));

        if (proposal.getProposalStatus() == ProposalStatus.ACCEPTED) {
            throw new IllegalArgumentException("Proposal already accepted.");
        }

        proposal.setProposalStatus(ProposalStatus.ACCEPTED);
        Proposal savedProposal = proposalRepository.save(proposal);

        eventPublisher.publishEvent(new ProposalAcceptedEvent(this, savedProposal, "Proposal accepted by client.", currentUserEmail));

        return savedProposal;
    }

    @Override
    public Proposal rejectProposal(String proposalCode, String reason, String currentUserEmail) {
        log.info("Rejecting proposal: {}, by: {}", proposalCode, currentUserEmail);

        Proposal proposal = proposalRepository.findByProposalCode(proposalCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal code"));

        proposal.setProposalStatus(ProposalStatus.REJECTED);
        proposal.setRemarks(proposal.getRemarks() + " | Rejection Reason: " + reason);
        Proposal savedProposal = proposalRepository.save(proposal);

        eventPublisher.publishEvent(new ProposalRejectedEvent(this, savedProposal, "Proposal rejected: " + reason, currentUserEmail));

        return savedProposal;
    }
}
