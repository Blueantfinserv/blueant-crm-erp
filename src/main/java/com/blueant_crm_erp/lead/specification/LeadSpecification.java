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
            if (request == null) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getClientName())) {
                String searchKeyword = "%" + request.getClientName().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("clientName")), searchKeyword));
            }
            
            if (StringUtils.hasText(request.getMobileNumber())) {
                String searchKeyword = "%" + request.getMobileNumber().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("mobileNumber")), searchKeyword));
            }

            if (StringUtils.hasText(request.getLeadCode())) {
                String searchKeyword = "%" + request.getLeadCode().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("leadCode")), searchKeyword));
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

            if (request.getDuplicateLeadStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("duplicateLeadStatus"), request.getDuplicateLeadStatus()));
            }

            if (request.getAssignedUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedSalesPerson").get("id"), request.getAssignedUserId()));
            }

            if (request.getLeaderId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedLeader").get("id"), request.getLeaderId()));
            }

            if (request.getFromDate() != null && request.getToDate() != null) {
                predicates.add(criteriaBuilder.between(root.get("createdAt"), request.getFromDate().atStartOfDay(), request.getToDate().plusDays(1).atStartOfDay()));
            } else if (request.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getFromDate().atStartOfDay()));
            } else if (request.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), request.getToDate().plusDays(1).atStartOfDay()));
            }

            // Assignment-specific filters
            if (StringUtils.hasText(request.getAssignmentSource())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("assignmentSource")),
                        request.getAssignmentSource().trim().toUpperCase()
                ));
            }

            if (request.getIsPhysicalLead() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPhysicalLead"), request.getIsPhysicalLead()));
            }

            if (Boolean.TRUE.equals(request.getAssignedByCoordinator())) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isTrue(root.get("isPhysicalLead")),
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get("assignmentSource")), "SALES_COORDINATOR"),
                        criteriaBuilder.isNotNull(root.get("assignedBy"))
                ));
            } else if (Boolean.FALSE.equals(request.getAssignedByCoordinator())) {
                predicates.add(criteriaBuilder.and(
                        criteriaBuilder.or(
                                criteriaBuilder.isNull(root.get("isPhysicalLead")),
                                criteriaBuilder.isFalse(root.get("isPhysicalLead"))
                        ),
                        criteriaBuilder.or(
                                criteriaBuilder.isNull(root.get("assignmentSource")),
                                criteriaBuilder.notEqual(criteriaBuilder.upper(root.get("assignmentSource")), "SALES_COORDINATOR")
                        ),
                        criteriaBuilder.isNull(root.get("assignedBy"))
                ));
            }

            // assignedFromDate / assignedToDate filtering on assignedAt
            if (request.getAssignedFromDate() != null && request.getAssignedToDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("assignedAt"), request.getAssignedFromDate().atStartOfDay()));
                predicates.add(criteriaBuilder.lessThan(root.get("assignedAt"), request.getAssignedToDate().plusDays(1).atStartOfDay()));
            } else if (request.getAssignedFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("assignedAt"), request.getAssignedFromDate().atStartOfDay()));
            } else if (request.getAssignedToDate() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("assignedAt"), request.getAssignedToDate().plusDays(1).atStartOfDay()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Lead> searchAndFilter(String keyword, LeadFilterRequest request) {
        Specification<Lead> spec = Specification.where(null);
        if (StringUtils.hasText(keyword)) {
            spec = spec.and(searchByKeyword(keyword));
        }
        if (request != null) {
            spec = spec.and(filterByCriteria(request));
        }
        return spec;
    }
}
