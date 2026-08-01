package com.blueant_crm_erp.role.entity;

import com.blueant_crm_erp.common.base.BaseSoftDeleteEntity;
import com.blueant_crm_erp.common.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * ============================================================================
 * Role Entity
 * ============================================================================
 *
 * Represents a system role used in Role-Based Access Control (RBAC).
 *
 * Examples:
 * - SUPER_ADMIN
 * - ADMIN
 * - SALES_MANAGER
 * - RELATIONSHIP_MANAGER
 * - EMPLOYEE
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Role Management
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * ============================================================================
 */
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_code",
                        columnNames = "code"
                )
        }
)
public class Role extends BaseSoftDeleteEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Role Name.
     */
    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    /**
     * Unique Role Code.
     */
    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String code;

    /**
     * Role Description.
     */
    @Column(
            name = "description",
            length = 500
    )
    private String description;

    /**
     * Display Order.
     */
    @Column(
            name = "display_order",
            nullable = false
    )
    @Builder.Default
    private Integer displayOrder = 1;

    /**
     * Indicates whether this is a system role.
     * System roles cannot be deleted.
     */
    @Builder.Default
    @Column(
            name = "system_role",
            nullable = false
    )
    private Boolean systemRole = Boolean.FALSE;

    /**
     * Indicates whether this role is assigned by default
     * when a new user is created.
     */
    @Builder.Default
    @Column(
            name = "default_role",
            nullable = false
    )
    private Boolean defaultRole = Boolean.FALSE;

    /**
     * Current Role Status.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private Status status = Status.ACTIVE;

    /**
     * Additional Remarks.
     */
    @Column(
            name = "remarks",
            length = 500
    )
    private String remarks;

}