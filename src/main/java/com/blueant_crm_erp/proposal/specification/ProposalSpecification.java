package com.blueant_crm_erp.proposal.specification;

import com.blueant_crm_erp.proposal.entity.Proposal;
import com.blueant_crm_erp.proposal.enums.ProposalStatus;
import org.springframework.data.jpa.domain.Specification;

public class ProposalSpecification {

    public static Specification<Proposal> hasProposalCode(String code) {
        return (root, query, criteriaBuilder) ->
                code == null ? null : criteriaBuilder.equal(root.get("proposalCode"), code);
    }

    public static Specification<Proposal> hasStatus(ProposalStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("proposalStatus"), status);
    }
    
    public static Specification<Proposal> hasLeadId(Long leadId) {
        return (root, query, criteriaBuilder) ->
                leadId == null ? null : criteriaBuilder.equal(root.get("lead").get("id"), leadId);
    }
}
