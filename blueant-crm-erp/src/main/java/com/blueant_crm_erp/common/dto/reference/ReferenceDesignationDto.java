package com.blueant_crm_erp.common.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * ============================================================================
 * Reference Designation DTO
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Common
 *
 * Description:
 * Reusable DTO for providing lightweight designation reference information.
 *
 * ============================================================================
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReferenceDesignationDto extends ReferenceResponse {

}
