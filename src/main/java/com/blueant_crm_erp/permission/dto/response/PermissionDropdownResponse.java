package com.blueant_crm_erp.permission.dto.response;

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
 * Permission Dropdown Response
 * =============================================================================
 *
 * Lightweight response DTO used for dropdowns, lookup APIs,
 * autocomplete and selection components.
 *
 * This DTO contains only the minimum information required
 * to populate UI dropdowns and selection lists.
 *
 * Used By:
 * • Permission Dropdown API
 * • Role Permission Assignment
 * • Lookup APIs
 * • Autocomplete Components
 * • Select2 / React Select
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Permission Management
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
public class PermissionDropdownResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Permission Identifier.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Permission Name.
     *
     * Example:
     * Create Lead
     * Update Lead
     * Delete Lead
     */
    @Schema(description = "Name", example = "Example Name")
    private String name;

    /**
     * Unique Permission Code.
     *
     * Example:
     * CREATE_LEAD
     * UPDATE_LEAD
     * DELETE_LEAD
     */
    @Schema(description = "Code", example = "Example Code")
    private String code;

}