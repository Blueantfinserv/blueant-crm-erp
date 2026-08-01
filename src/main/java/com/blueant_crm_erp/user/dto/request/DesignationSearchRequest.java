package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Designation Search Request
 * =============================================================================
 *
 * Request DTO used for searching Designations with
 * filtering, sorting and pagination support.
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Designation Search API
 * • Designation Listing API
 * • Designation Pagination
 * • Dashboard
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
public class DesignationSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Search by designation name.
     */
    @Schema(description = "Name", example = "Example Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * Search by designation code.
     */
    @Schema(description = "Code", example = "Example Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /**
     * Search by department.
     */
    @Schema(description = "Department Id", example = "Example Department Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long departmentId;

    /**
     * Search by hierarchy level.
     */
    @Schema(description = "Hierarchy Level", example = "Example Hierarchy Level", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer hierarchyLevel;

    /**
     * Search by status.
     */
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Search by remarks.
     */
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

    /**
     * Global Search Keyword.
     *
     * Searches:
     * • Code
     * • Name
     * • Description
     * • Remarks
     */
    @Schema(description = "Keyword", example = "Example Keyword", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;

    /**
     * Page Number.
     */
    @Builder.Default
    @Min(value = 0, message = "Page number cannot be negative.")
    private Integer page = 0;

    /**
     * Page Size.
     */
    @Builder.Default
    @Min(value = 1, message = "Page size must be greater than zero.")
    private Integer size = 10;

    /**
     * Sort Field.
     */
    @Builder.Default
    private String sortBy = "hierarchyLevel";

    /**
     * Sort Direction.
     * ASC / DESC
     */
    @Builder.Default
    private String sortDirection = "ASC";

}