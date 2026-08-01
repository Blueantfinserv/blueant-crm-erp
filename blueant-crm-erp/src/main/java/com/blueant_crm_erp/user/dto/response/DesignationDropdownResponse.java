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
 * Designation Dropdown Response
 * =============================================================================
 *
 * Lightweight response DTO used for:
 * -----------------------------------------------------------------------------
 * • Designation Dropdown
 * • Designation Lookup API
 * • User Creation Form
 * • User Update Form
 * • Autocomplete APIs
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Business Head (Rohit)
 *        ↓
 * Sales Manager
 *        ↓
 * Team Leader
 *        ↓
 * Sales Person
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
public class DesignationDropdownResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Designation Id.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Designation Name.
     */
    @Schema(description = "Name", example = "Example Name")
    private String name;

    /**
     * Designation Code.
     */
    @Schema(description = "Code", example = "Example Code")
    private String code;

    /**
     * Department Name.
     */
    @Schema(description = "Department Name", example = "Example Department Name")
    private String departmentName;

    /**
     * Hierarchy Level.
     */
    @Schema(description = "Hierarchy Level", example = "Example Hierarchy Level")
    private Integer hierarchyLevel;

}