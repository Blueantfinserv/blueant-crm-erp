package com.blueant_crm_erp.user.specification;

import com.blueant_crm_erp.user.dto.request.UserSearchRequest;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * User Specification
 * =============================================================================
 *
 * Dynamic Search Specification for User Entity.
 *
 * Supports
 * -----------------------------------------------------------------------------
 * • Keyword Search
 * • Department Filter
 * • Designation Filter
 * • Team Filter
 * • Role Filter
 * • Reporting Manager Filter
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
public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> search(UserSearchRequest request) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ===============================================================
            // Soft Delete
            // ===============================================================

            predicates.add(cb.isFalse(root.get("deleted")));

            // ===============================================================
            // Keyword Search
            // ===============================================================

            if (StringUtils.hasText(request.getKeyword())) {

                String keyword =
                        "%" + request.getKeyword().trim().toLowerCase() + "%";

                predicates.add(
                        cb.or(

                                cb.like(
                                        cb.lower(root.get("employeeCode")),
                                        keyword
                                ),

                                cb.like(
                                        cb.lower(root.get("firstName")),
                                        keyword
                                ),

                                cb.like(
                                        cb.lower(root.get("lastName")),
                                        keyword
                                ),

                                cb.like(
                                        cb.lower(root.get("email")),
                                        keyword
                                ),

                                cb.like(
                                        cb.lower(root.get("mobileNumber")),
                                        keyword
                                )

                        )
                );
            }

            // ===============================================================
            // Department
            // ===============================================================

            if (request.getDepartmentId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("department").get("id"),
                                request.getDepartmentId()
                        )
                );
            }

            // ===============================================================
            // Designation
            // ===============================================================

            if (request.getDesignationId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("designation").get("id"),
                                request.getDesignationId()
                        )
                );
            }

            // ===============================================================
            // Team
            // ===============================================================

            if (request.getTeamId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("team").get("id"),
                                request.getTeamId()
                        )
                );
            }

            // ===============================================================
            // Role
            // ===============================================================

            if (request.getRoleId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("role").get("id"),
                                request.getRoleId()
                        )
                );
            }

            // ===============================================================
            // Reporting Manager
            // ===============================================================

            if (request.getReportingManagerId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("reportingManager").get("id"),
                                request.getReportingManagerId()
                        )
                );
            }

            // ===============================================================
            // Status
            // ===============================================================

            if (request.getStatus() != null) {

                predicates.add(
                        cb.equal(
                                root.get("status"),
                                request.getStatus()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}