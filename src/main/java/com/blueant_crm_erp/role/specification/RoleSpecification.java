package com.blueant_crm_erp.role.specification;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.role.entity.Role;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * =============================================================================
 * Role Specification
 * =============================================================================
 *
 * Dynamic JPA Specifications for Role module.
 *
 * Used for:
 * - Search
 * - Filtering
 * - Pagination
 * - Sorting
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * Author : BlueAnt CRM ERP Team
 * =============================================================================
 */
public final class RoleSpecification {

    private RoleSpecification() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Search by Role Name.
     *
     * @param roleName role name
     * @return specification
     */
    public static Specification<Role> hasRoleName(String roleName) {

        return (root, query, criteriaBuilder) -> {

            if (!StringUtils.hasText(roleName)) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("roleName")),
                    "%" + roleName.toLowerCase().trim() + "%"
            );
        };
    }

    /**
     * Search by Role Code.
     *
     * @param roleCode role code
     * @return specification
     */
    public static Specification<Role> hasRoleCode(String roleCode) {

        return (root, query, criteriaBuilder) -> {

            if (!StringUtils.hasText(roleCode)) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("roleCode")),
                    "%" + roleCode.toLowerCase().trim() + "%"
            );
        };
    }

    /**
     * Filter by Status.
     *
     * @param status role status
     * @return specification
     */
    public static Specification<Role> hasStatus(Status status) {

        return (root, query, criteriaBuilder) -> {

            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter System Roles.
     *
     * @param systemRole true/false
     * @return specification
     */
    public static Specification<Role> isSystemRole(Boolean systemRole) {

        return (root, query, criteriaBuilder) -> {

            if (systemRole == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("systemRole"), systemRole);
        };
    }

    /**
     * Filter Default Roles.
     *
     * @param defaultRole true/false
     * @return specification
     */
    public static Specification<Role> isDefaultRole(Boolean defaultRole) {

        return (root, query, criteriaBuilder) -> {

            if (defaultRole == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("defaultRole"), defaultRole);
        };
    }

    /**
     * Filter by Hierarchy Level.
     *
     * @param hierarchyLevel hierarchy level
     * @return specification
     */
    public static Specification<Role> hasHierarchyLevel(Integer hierarchyLevel) {

        return (root, query, criteriaBuilder) -> {

            if (hierarchyLevel == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("hierarchyLevel"), hierarchyLevel);
        };
    }

}