package com.blueant_crm_erp.permission.specification;

import com.blueant_crm_erp.permission.dto.request.PermissionSearchRequest;
import com.blueant_crm_erp.permission.entity.Permission;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Permission Specification
 * =============================================================================
 *
 * Dynamic JPA Specification used for searching and filtering permissions.
 *
 * Supports:
 * ---------------------------------------------------------------------------
 * • Soft Delete Filter
 * • Keyword Search
 * • Module Filter
 * • Status Filter
 *
 * Keyword Search Fields:
 * ---------------------------------------------------------------------------
 * • Name
 * • Code
 * • Description
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
public final class PermissionSpecification {

    /**
     * Private Constructor.
     */
    private PermissionSpecification() {
    }

    /**
     * Builds dynamic search specification.
     *
     * @param request Permission Search Request
     * @return Specification
     */
    public static Specification<Permission> search(
            PermissionSearchRequest request
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            /*
             * ==============================================================
             * Soft Delete Filter
             * ==============================================================
             */
            predicates.add(
                    criteriaBuilder.isFalse(root.get("deleted"))
            );

            /*
             * ==============================================================
             * Keyword Search
             * ==============================================================
             */
            if (request.getKeyword() != null &&
                    !request.getKeyword().trim().isEmpty()) {

                String keyword =
                        "%" + request.getKeyword().trim().toLowerCase() + "%";

                predicates.add(

                        criteriaBuilder.or(

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("name")),
                                        keyword
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("code")),
                                        keyword
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("description")),
                                        keyword
                                )
                        )
                );
            }

            /*
             * ==============================================================
             * Module Filter
             * ==============================================================
             */
            if (request.getModule() != null &&
                    !request.getModule().trim().isEmpty()) {

                predicates.add(

                        criteriaBuilder.equal(

                                criteriaBuilder.lower(root.get("module")),

                                request.getModule()
                                        .trim()
                                        .toLowerCase()

                        )
                );
            }

            /*
             * ==============================================================
             * Status Filter
             * ==============================================================
             */
            if (request.getStatus() != null) {

                predicates.add(

                        criteriaBuilder.equal(

                                root.get("status"),

                                request.getStatus()

                        )
                );
            }

            /*
             * ==============================================================
             * Final Predicate
             * ==============================================================
             */
            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }

}