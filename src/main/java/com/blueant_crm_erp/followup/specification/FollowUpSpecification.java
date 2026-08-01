package com.blueant_crm_erp.followup.specification;

import com.blueant_crm_erp.followup.entity.FollowUp;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class FollowUpSpecification {

    public static Specification<FollowUp> hasLeadId(Long leadId) {
        return (root, query, criteriaBuilder) ->
                leadId == null ? null : criteriaBuilder.equal(root.get("lead").get("id"), leadId);
    }

    public static Specification<FollowUp> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }
    
    public static Specification<FollowUp> beforeDate(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                date == null ? null : criteriaBuilder.lessThan(root.get("followUpDate"), date);
    }
}
