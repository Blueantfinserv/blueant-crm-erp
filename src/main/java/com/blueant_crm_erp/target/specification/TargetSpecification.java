package com.blueant_crm_erp.target.specification;

import com.blueant_crm_erp.target.entity.Target;
import org.springframework.data.jpa.domain.Specification;

public class TargetSpecification {

    public static Specification<Target> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
                userId == null ? null : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Target> hasTargetMonth(String targetMonth) {
        return (root, query, criteriaBuilder) ->
                targetMonth == null ? null : criteriaBuilder.equal(root.get("targetMonth"), targetMonth);
    }
}
