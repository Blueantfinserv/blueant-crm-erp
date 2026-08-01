package com.blueant_crm_erp.common.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * ============================================================================
 * Reference User DTO
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Common
 *
 * Description:
 * Reusable DTO for providing lightweight user reference information with metadata.
 *
 * ============================================================================
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReferenceUserDto extends ReferenceResponse {

    private String email;
    private String mobileNumber;
    private String profileImage;

}
