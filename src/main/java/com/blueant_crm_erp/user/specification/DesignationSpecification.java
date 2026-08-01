package com.blueant_crm_erp.user.specification;

import com.blueant_crm_erp.user.dto.request.DesignationSearchRequest;
import com.blueant_crm_erp.user.entity.Designation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Designation Specification
 * =============================================================================
 *
 * Dynamic JPA Specification for Designation Search.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Keyword Search
 * • Department Filter
 * • Hierarchy Level Filter
 * • Status Filter
 * • Soft Delete Filter
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
public final class DesignationSpecification {

    private DesignationSpecification() {
    }

    /**
     * Builds Dynamic Specification.
     */
    public static Specification<Designation> search(
            DesignationSearchRequest request
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ================================================================
            // Soft Delete
            // ================================================================

            predicates.add(
                    criteriaBuilder.isFalse(root.get("deleted"))
            );

            // ================================================================
            // Keyword Search
            // ================================================================

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

            // ================================================================
            // Department Filter
            // ================================================================

            if (request.getDepartmentId() != null) {

                predicates.add(

                        criteriaBuilder.equal(
                                root.get("department").get("id"),
                                request.getDepartmentId()
                        )

                );

            }

            // ================================================================
            // Hierarchy Level Filter
            // ================================================================

            if (request.getHierarchyLevel() != null) {

                predicates.add(

                        criteriaBuilder.equal(
                                root.get("hierarchyLevel"),
                                request.getHierarchyLevel()
                        )

                );

            }

            // ================================================================
            // Status Filter
            // ================================================================

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