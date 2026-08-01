package com.blueant_crm_erp.negotiation.specification;

import com.blueant_crm_erp.negotiation.entity.Negotiation;
import com.blueant_crm_erp.negotiation.enums.NegotiationStatus;
import org.springframework.data.jpa.domain.Specification;

public class NegotiationSpecification {

    public static Specification<Negotiation> hasNegotiationCode(String code) {
        return (root, query, criteriaBuilder) ->
                code == null ? null : criteriaBuilder.equal(root.get("negotiationCode"), code);
    }

    public static Specification<Negotiation> hasStatus(NegotiationStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("negotiationStatus"), status);
    }
    
    public static Specification<Negotiation> hasProposalId(Long proposalId) {
        return (root, query, criteriaBuilder) ->
                proposalId == null ? null : criteriaBuilder.equal(root.get("proposal").get("id"), proposalId);
    }
}
