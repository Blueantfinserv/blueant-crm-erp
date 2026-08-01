package com.blueant_crm_erp.user.specification;

import com.blueant_crm_erp.user.dto.request.TeamSearchRequest;
import com.blueant_crm_erp.user.entity.Team;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Team Specification
 * =============================================================================
 *
 * Dynamic Search Specification for Team Entity.
 *
 * Supports
 * -----------------------------------------------------------------------------
 * • Keyword Search
 * • Department Filter
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
public final class TeamSpecification {

    private TeamSpecification() {
    }

    public static Specification<Team> search(
            TeamSearchRequest request
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // =============================================================
            // Soft Delete
            // =============================================================

            predicates.add(
                    criteriaBuilder.isFalse(
                            root.get("deleted")
                    )
            );

            // =============================================================
            // Keyword Search
            // =============================================================

            if (StringUtils.hasText(request.getKeyword())) {

                String keyword =
                        "%" + request.getKeyword().trim().toLowerCase() + "%";

                predicates.add(
                        criteriaBuilder.or(

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("teamCode")
                                        ),
                                        keyword
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("teamName")
                                        ),
                                        keyword
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("description")
                                        ),
                                        keyword
                                )

                        )
                );
            }

            // =============================================================
            // Department
            // =============================================================

            if (request.getDepartmentId() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("department").get("id"),
                                request.getDepartmentId()
                        )
                );
            }

            // =============================================================
            // Status
            // =============================================================

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