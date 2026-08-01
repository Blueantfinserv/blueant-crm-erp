package com.blueant_crm_erp.user.specification;

import com.blueant_crm_erp.user.dto.request.DepartmentSearchRequest;
import com.blueant_crm_erp.user.entity.Department;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Department Specification
 * =============================================================================
 *
 * Dynamic JPA Specification for Department Search.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Global Keyword Search
 * • Status Filter
 * • Soft Delete Filter
 * • Dynamic Filtering
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
public final class DepartmentSpecification {

    private DepartmentSpecification() {
    }

    /**
     * Builds Dynamic Search Specification.
     */
    public static Specification<Department> search(
            DepartmentSearchRequest request
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ===============================================================
            // Soft Delete
            // ===============================================================

            predicates.add(
                    criteriaBuilder.isFalse(root.get("deleted"))
            );

            // ===============================================================
            // Global Keyword Search
            // ===============================================================

            if (StringUtils.hasText(request.getKeyword())) {

                String keyword =
                        "%" + request.getKeyword().trim().toLowerCase() + "%";

                predicates.add(

                        criteriaBuilder.or(

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("code")),
                                        keyword
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("name")),
                                        keyword
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("description")),
                                        keyword
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("remarks")),
                                        keyword
                                )

                        )

                );

            }

            // ===============================================================
            // Status Filter
            // ===============================================================

            if (request.getStatus() != null) {

                predicates.add(

                        criteriaBuilder.equal(
                                root.get("status"),
                                request.getStatus()
                        )

                );

            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );

        };

    }

}