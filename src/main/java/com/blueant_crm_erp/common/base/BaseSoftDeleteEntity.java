package com.blueant_crm_erp.common.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * Base Soft Delete Entity
 * ============================================================================
 *
 * Provides logical deletion support.
 *
 * Records are never physically deleted from database.
 * Instead they are marked as deleted.
 *
 * All business entities requiring soft delete should
 * extend this class.
 *
 * Project : BlueAnt CRM ERP Platform
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseSoftDeleteEntity extends BaseAuditEntity {

    /**
     * Indicates whether the record is logically deleted.
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = Boolean.FALSE;

    /**
     * Date and time when the record was deleted.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * User who deleted the record.
     */
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    /**
     * Marks entity as deleted.
     *
     * @param deletedBy user performing delete
     */
    public void markAsDeleted(String deletedBy) {

        this.deleted = Boolean.TRUE;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Restores logically deleted record.
     */
    public void restore() {

        this.deleted = Boolean.FALSE;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * Ensure deleted is initialized before persisting.
     */
    @jakarta.persistence.PrePersist
    protected void onPrePersistSoftDelete() {
        if (this.deleted == null) {
            this.deleted = Boolean.FALSE;
        }
    }

    /**
     * Checks whether entity is deleted.
     *
     * @return true if deleted
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

}