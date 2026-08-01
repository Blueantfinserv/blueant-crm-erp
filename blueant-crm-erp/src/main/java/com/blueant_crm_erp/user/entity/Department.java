package com.blueant_crm_erp.user.entity;

import com.blueant_crm_erp.common.base.BaseSoftDeleteEntity;
import com.blueant_crm_erp.common.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
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
 * Department Entity
 * =============================================================================
 *
 * Represents a business department in BlueAnt CRM ERP.
 *
 * Examples
 * -----------------------------------------------------------------------------
 * Sales
 * CRM
 * Operations
 * HR
 * Accounts
 * Helpdesk
 * Insurance
 * Share
 * Loan
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Entity
@Table(
        name = "departments",
        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_department_name",
                        columnNames = "name"
                ),

                @UniqueConstraint(
                        name = "uk_department_code",
                        columnNames = "code"
                )

        },
        indexes = {

                @Index(
                        name = "idx_department_name",
                        columnList = "name"
                ),

                @Index(
                        name = "idx_department_code",
                        columnList = "code"
                ),

                @Index(
                        name = "idx_department_status",
                        columnList = "status"
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
public class Department extends BaseSoftDeleteEntity {

    /**
     * Department Name.
     */
    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String name;

    /**
     * Department Code.
     */
    @Column(
            nullable = false,
            unique = true,
            length = 50
    )
    private String code;

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
     * Department Status.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(
            nullable = false,
            length = 20
    )
    private Status status = Status.ACTIVE;

    /**
     * Department Description.
     */
    @Column(length = 500)
    private String description;

    /**
     * Remarks.
     */
    @Column(length = 500)
    private String remarks;

    /**
     * Users belonging to this Department.
     */
    @Builder.Default
    @OneToMany(mappedBy = "department")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> users = new ArrayList<>();

    /**
     * Designations under this Department.
     */
    @Builder.Default
    @OneToMany(mappedBy = "department")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Designation> designations = new ArrayList<>();

    /**
     * Teams under this Department.
     */
    @Builder.Default
    @OneToMany(mappedBy = "department")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Team> teams = new ArrayList<>();

}