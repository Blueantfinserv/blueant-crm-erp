package com.blueant_crm_erp.negotiation.service.impl;

import com.blueant_crm_erp.negotiation.entity.Negotiation;
import com.blueant_crm_erp.negotiation.entity.NegotiationUpdate;
import com.blueant_crm_erp.negotiation.enums.NegotiationStatus;
import com.blueant_crm_erp.negotiation.event.NegotiationCompletedEvent;
import com.blueant_crm_erp.negotiation.event.NegotiationStartedEvent;
import com.blueant_crm_erp.negotiation.repository.NegotiationRepository;
import com.blueant_crm_erp.negotiation.repository.NegotiationUpdateRepository;
import com.blueant_crm_erp.negotiation.service.NegotiationWorkflowService;
import com.blueant_crm_erp.proposal.entity.Proposal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NegotiationWorkflowServiceImpl implements NegotiationWorkflowService {

    private final NegotiationRepository negotiationRepository;
    private final NegotiationUpdateRepository negotiationUpdateRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Negotiation startNegotiation(Proposal proposal, String currentUserEmail) {
        log.info("Starting negotiation for proposal: {}, by: {}", proposal.getProposalCode(), currentUserEmail);

        if (negotiationRepository.findByProposalId(proposal.getId()).isPresent()) {
            throw new IllegalArgumentException("Negotiation already started for this proposal.");
        }

        Negotiation negotiation = Negotiation.builder()
                .proposal(proposal)
                .negotiationCode("NEG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .negotiationStatus(NegotiationStatus.STARTED)
                .build();

        Negotiation savedNegotiation = negotiationRepository.save(negotiation);

        createUpdateRecord(savedNegotiation, "Negotiation started.", null, currentUserEmail);

        eventPublisher.publishEvent(new NegotiationStartedEvent(this, savedNegotiation, "Negotiation started.", currentUserEmail));

        return savedNegotiation;
    }

    @Override
    public Negotiation updateNegotiation(String negotiationCode, BigDecimal agreedAmount, String discussion, String currentUserEmail) {
        log.info("Updating negotiation: {}, by: {}", negotiationCode, currentUserEmail);

        Negotiation negotiation = negotiationRepository.findByNegotiationCode(negotiationCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid negotiation code"));

        if (negotiation.getNegotiationStatus() == NegotiationStatus.CLOSED) {
            throw new IllegalArgumentException("Negotiation is already closed.");
        }

        negotiation.setNegotiationStatus(NegotiationStatus.UPDATED);
        negotiation.setFinalAgreedAmount(agreedAmount);
        
        Negotiation savedNegotiation = negotiationRepository.save(negotiation);

        createUpdateRecord(savedNegotiation, discussion, agreedAmount, currentUserEmail);

        return savedNegotiation;
    }

    @Override
    public Negotiation closeNegotiation(String negotiationCode, String currentUserEmail) {
        log.info("Closing negotiation: {}, by: {}", negotiationCode, currentUserEmail);

        Negotiation negotiation = negotiationRepository.findByNegotiationCode(negotiationCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid negotiation code"));

        negotiation.setNegotiationStatus(NegotiationStatus.CLOSED);
        Negotiation savedNegotiation = negotiationRepository.save(negotiation);

        createUpdateRecord(savedNegotiation, "Negotiation closed successfully.", negotiation.getFinalAgreedAmount(), currentUserEmail);

        eventPublisher.publishEvent(new NegotiationCompletedEvent(this, savedNegotiation, "Negotiation closed.", currentUserEmail));

        return savedNegotiation;
    }

    private void createUpdateRecord(Negotiation negotiation, String discussion, BigDecimal agreedAmount, String currentUserEmail) {
        long existingUpdates = negotiationUpdateRepository.countByNegotiationId(negotiation.getId());
        int nextUpdateNumber = (int) existingUpdates + 1;

        NegotiationUpdate update = NegotiationUpdate.builder()
                .negotiation(negotiation)
                .updateNumber(nextUpdateNumber)
                .negotiationStatus(negotiation.getNegotiationStatus())
                .discussion(discussion)
                .agreedAmount(agreedAmount)
                .submittedBy(currentUserEmail)
                .submittedAt(LocalDateTime.now())
                .build();

        negotiationUpdateRepository.save(update);
    }
}
