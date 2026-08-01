package com.blueant_crm_erp.proposal.repository;

import com.blueant_crm_erp.proposal.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long>, JpaSpecificationExecutor<Proposal> {

    Optional<Proposal> findByProposalCode(String proposalCode);

}
