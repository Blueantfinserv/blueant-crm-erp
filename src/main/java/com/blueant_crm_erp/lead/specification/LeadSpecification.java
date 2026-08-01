package com.blueant_crm_erp.lead.specification;

import com.blueant_crm_erp.lead.dto.request.LeadFilterRequest;
import com.blueant_crm_erp.lead.entity.Lead;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class LeadSpecification {

    public static Specification<Lead> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return criteriaBuilder.conjunction();
            }
            String searchKeyword = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("clientName")), searchKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("mobileNumber")), searchKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("leadCode")), searchKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), searchKeyword)
            );
        };
    }

    public static Specification<Lead> filterByCriteria(LeadFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getClientName())) {
                String searchKeyword = "%" + request.getClientName().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("clientName")), searchKeyword));
            }
            
            if (StringUtils.hasText(request.getMobileNumber())) {
                String searchKeyword = "%" + request.getMobileNumber().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("mobileNumber")), searchKeyword));
            }

            if (request.getLeadStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("leadStatus"), request.getLeadStatus()));
            }

            if (request.getLeadStage() != null) {
                predicates.add(criteriaBuilder.equal(root.get("leadStage"), request.getLeadStage()));
            }

            if (request.getLeadPriority() != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), request.getLeadPriority()));
            }
            
            if (request.getLeadSource() != null) {
                predicates.add(criteriaBuilder.equal(root.get("leadSource"), request.getLeadSource()));
            }

            if (request.getFromDate() != null && request.getToDate() != null) {
                predicates.add(criteriaBuilder.between(root.get("createdAt"), request.getFromDate().atStartOfDay(), request.getToDate().plusDays(1).atStartOfDay()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
