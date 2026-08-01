package com.blueant_crm_erp.common.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * ============================================================================
 * Base Reference Response
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Common
 *
 * Description:
 * A generic abstract base response for all reference objects, establishing 
 * uniform properties `id`, `code`, and `name` while allowing metadata 
 * extension via subclassing.
 *
 * ============================================================================
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ReferenceResponse {

    private Long id;
    private String code;
    private String name;

}
