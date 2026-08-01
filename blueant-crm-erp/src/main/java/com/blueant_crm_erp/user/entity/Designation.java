package com.blueant_crm_erp.user.entity;

import com.blueant_crm_erp.common.base.BaseSoftDeleteEntity;
import com.blueant_crm_erp.common.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Designation Entity
 * =============================================================================
 *
 * Represents a Designation in BlueAnt CRM ERP.
 *
 * Examples
 * -----------------------------------------------------------------------------
 * Business Head
 * Sales Manager
 * Team Leader
 * Sales Person
 * CRM Executive
 * Operations Executive
 * HR Executive
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Level 1 -> Business Head
 * Level 2 -> Sales Manager
 * Level 3 -> Team Leader
 * Level 4 -> Sales Person
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Entity
@Table(
        name = "designations",
        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_designation_name",
                        columnNames = "name"
                ),

                @UniqueConstraint(
                        name = "uk_designation_code",
                        columnNames = "code"
                )

        },
        indexes = {

                @Index(
                        name = "idx_designation_name",
                        columnList = "name"
                ),

                @Index(
                        name = "idx_designation_code",
                        columnList = "code"
                ),

                @Index(
                        name = "idx_designation_status",
                        columnList = "status"
                ),

                @Index(
                        name = "idx_designation_level",
                        columnList = "hierarchy_level"
                )

        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Designation extends BaseSoftDeleteEntity {

    /**
     * Designation Name.
     */
    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String name;

    /**
     * Designation Code.
     */
    @Column(
            nullable = false,
            unique = true,
            length = 50
    )
    private String code;

    /**
     * Department.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "department_id",
            nullable = false
    )
    private Department department;

    /**
     * Hierarchy Level.
     *
     * Example:
     * 1 = Business Head
     * 2 = Sales Manager
     * 3 = Team Leader
     * 4 = Sales Person
     */
    @Column(
            name = "hierarchy_level",
            nullable = false
    )
    private Integer hierarchyLevel;

    /**
     * Display Order.
     */
    @Builder.Default
    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder = 1;

    /**
     * Designation Status.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(
            nullable = false,
            length = 20
    )
    private Status status = Status.ACTIVE;

    /**
     * Description.
     */
    @Column(length = 500)
    private String description;

    /**
     * Remarks.
     */
    @Column(length = 500)
    private String remarks;

    /**
     * Users assigned to this Designation.
     */
    @Builder.Default
    @OneToMany(mappedBy = "designation")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> users = new ArrayList<>();

}