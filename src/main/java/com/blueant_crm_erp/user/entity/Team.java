package com.blueant_crm_erp.user.entity;

import com.blueant_crm_erp.common.base.BaseSoftDeleteEntity;
import com.blueant_crm_erp.common.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Team Entity
 * =============================================================================
 *
 * Represents a Sales Team within the BlueAnt CRM ERP Platform.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Team Management
 * • User Assignment
 * • Department Association
 * • Team Status Management
 * • Display Ordering
 * • Soft Delete Support
 *
 * Relationships
 * -----------------------------------------------------------------------------
 * Department
 *      │
 *      └────────► Team
 *                     │
 *                     └────────► Users
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author  : BlueAnt CRM ERP Team
 * Since   : 1.0.0
 * =============================================================================
 */
@Entity
@Table(
        name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_team_code",
                        columnNames = "team_code"
                ),
                @UniqueConstraint(
                        name = "uk_team_name",
                        columnNames = "team_name"
                )
        },
        indexes = {
                @Index(
                        name = "idx_team_code",
                        columnList = "team_code"
                ),
                @Index(
                        name = "idx_team_name",
                        columnList = "team_name"
                ),
                @Index(
                        name = "idx_team_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_team_department",
                        columnList = "department_id"
                ),
                @Index(
                        name = "idx_team_display_order",
                        columnList = "display_order"
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
public class Team extends BaseSoftDeleteEntity {

    /**
     * Team Code.
     */
    @Column(
            name = "team_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String teamCode;

    /**
     * Team Name.
     */
    @Column(
            name = "team_name",
            nullable = false,
            unique = true,
            length = 100
    )
    private String teamName;

    /**
     * Department.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "department_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_team_department")
    )
    private Department department;

    /**
     * Team Description.
     */
    @Column(length = 500)
    private String description;

    /**
     * Display Order.
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 1;

    /**
     * Team Status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    /**
     * Team Members.
     */
    @Builder.Default
    @OneToMany(
            mappedBy = "team",
            fetch = FetchType.LAZY
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> users = new ArrayList<>();

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Checks whether team is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return Status.ACTIVE.equals(status)
                && Boolean.FALSE.equals(getDeleted());
    }

    /**
     * Returns total active users.
     *
     * @return total users
     */
    public int getTotalUsers() {
        return users == null ? 0 : users.size();
    }

}