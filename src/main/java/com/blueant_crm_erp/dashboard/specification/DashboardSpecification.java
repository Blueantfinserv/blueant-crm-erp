package com.blueant_crm_erp.dashboard.specification;

import com.blueant_crm_erp.lead.entity.Lead;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class DashboardSpecification {

    public static Specification<Lead> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("assignedSalesPerson").get("id"), userId);
    }

    public static Specification<Lead> createdBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> cb.between(root.get("createdAt").as(java.time.LocalDateTime.class), start.atStartOfDay(), end.atTime(23, 59, 59));
    }
}
