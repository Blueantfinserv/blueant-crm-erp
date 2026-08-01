package com.blueant_crm_erp.common.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * ============================================================================
 * Base Version Entity
 * ============================================================================
 *
 * Provides optimistic locking support.
 *
 * Prevents concurrent update conflicts.
 *
 * All business entities should extend this class.
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
public abstract class BaseVersionEntity extends BaseSoftDeleteEntity {

    /**
     * Optimistic locking version.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

}