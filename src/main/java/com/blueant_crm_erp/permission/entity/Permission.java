package com.blueant_crm_erp.permission.entity;

import com.blueant_crm_erp.common.base.BaseSoftDeleteEntity;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.permission.constant.PermissionConstants;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(
        name = PermissionConstants.TABLE_NAME,
        uniqueConstraints = {
                @UniqueConstraint(
                        name = PermissionConstants.UNIQUE_CODE,
                        columnNames = "code"
                )
        },
        indexes = {

                @Index(
                        name = "idx_permission_name",
                        columnList = "name"
                ),

                @Index(
                        name = "idx_permission_code",
                        columnList = "code"
                ),

                @Index(
                        name = "idx_permission_module",
                        columnList = "module"
                ),

                @Index(
                        name = "idx_permission_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Permission extends BaseSoftDeleteEntity {

    /**
     * Permission Name.
     *
     * Example:
     * Create User
     * Delete Lead
     */
    @Column(
            name = "name",
            nullable = false,
            length = PermissionConstants.NAME_MAX_LENGTH
    )
    private String name;

    /**
     * Unique Permission Code.
     *
     * Example:
     * CREATE_USER
     * DELETE_LEAD
     */
    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = PermissionConstants.CODE_MAX_LENGTH
    )
    private String code;

    /**
     * Business Module.
     *
     * Example:
     * SALES
     * CRM
     * USER
     * ROLE
     */
    @Column(
            name = "module",
            nullable = false,
            length = PermissionConstants.MODULE_MAX_LENGTH
    )
    private String module;

    /**
     * Permission Description.
     */
    @Column(
            name = "description",
            length = PermissionConstants.DESCRIPTION_MAX_LENGTH
    )
    private String description;

    /**
     * UI Display Order.
     */
    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;

    /**
     * Indicates whether this permission
     * is system generated.
     *
     * System permissions cannot be deleted
     * by normal administrators.
     */
    @Builder.Default
    @Column(
            name = "system_permission",
            nullable = false
    )
    private Boolean systemPermission = Boolean.FALSE;

    /**
     * Current Permission Status.
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
     * Internal Remarks.
     */
    @Column(
            name = "remarks",
            length = PermissionConstants.REMARKS_MAX_LENGTH
    )
    private String remarks;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Permission that = (Permission) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}