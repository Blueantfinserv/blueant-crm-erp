package com.blueant_crm_erp.common.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * ============================================================================
 * Reference Department DTO
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Common
 *
 * Description:
 * Reusable DTO for providing lightweight department reference information.
 *
 * ============================================================================
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReferenceDepartmentDto extends ReferenceResponse {

    private String location;

}
