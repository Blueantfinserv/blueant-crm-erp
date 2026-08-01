package com.blueant_crm_erp.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Department Dropdown Response
 * =============================================================================
 *
 * Lightweight response DTO used for:
 * -----------------------------------------------------------------------------
 * • Department Dropdown
 * • Department Lookup APIs
 * • User Creation Form
 * • User Update Form
 * • Search Filters
 * • Autocomplete APIs
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Author : BlueAnt CRM ERP Team
 * Since  : 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDropdownResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Department Id.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Department Name.
     */
    @Schema(description = "Name", example = "Example Name")
    private String name;

    /**
     * Department Code.
     */
    @Schema(description = "Code", example = "Example Code")
    private String code;

}