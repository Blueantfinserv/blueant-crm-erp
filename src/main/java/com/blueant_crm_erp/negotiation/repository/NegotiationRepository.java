package com.blueant_crm_erp.negotiation.repository;

import com.blueant_crm_erp.negotiation.entity.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long>, JpaSpecificationExecutor<Negotiation> {
    Optional<Negotiation> findByNegotiationCode(String negotiationCode);
    Optional<Negotiation> findByProposalId(Long proposalId);
}
