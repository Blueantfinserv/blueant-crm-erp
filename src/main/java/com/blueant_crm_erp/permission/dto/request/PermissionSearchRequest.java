package com.blueant_crm_erp.permission.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Permission Search Request
 * =============================================================================
 *
 * Request DTO used for searching, filtering, sorting and pagination
 * of Permission records.
 *
 * Supported Features:
 * • Keyword Search
 * • Module Filter
 * • Status Filter
 * • Pagination
 * • Sorting
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
public class PermissionSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Search keyword.
     *
     * Searches in:
     * - Permission Name
     * - Permission Code
     * - Description
     */
    @Size(
            max = 100,
            message = "Keyword must not exceed 100 characters."
    )
    @Schema(description = "Keyword", example = "Example Keyword", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;

    /**
     * Module Filter.
     *
     * Example:
     * SALES
     * CRM
     * USER
     * ROLE
     * HR
     * REPORT
     * DASHBOARD
     */
    @Size(
            max = 100,
            message = "Module name must not exceed 100 characters."
    )
    @Schema(description = "Module", example = "Example Module", requiredMode = Schema.RequiredMode.REQUIRED)
    private String module;

    /**
     * Permission Status Filter.
     */
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Include deleted permissions.
     *
     * Default:
     * false
     */
    @Builder.Default
    private Boolean includeDeleted = Boolean.FALSE;

    /**
     * Page Number.
     *
     * Default:
     * 0
     */
    @Builder.Default
    @Min(
            value = 0,
            message = "Page number cannot be negative."
    )
    private Integer page = 0;

    /**
     * Page Size.
     *
     * Default:
     * 10
     */
    @Builder.Default
    @Min(
            value = 1,
            message = "Page size must be at least 1."
    )
    @Max(
            value = 100,
            message = "Page size cannot exceed 100."
    )
    private Integer size = 10;

    /**
     * Sort By Field.
     *
     * Supported:
     * - name
     * - code
     * - module
     * - displayOrder
     * - status
     * - createdAt
     * - updatedAt
     */
    @Builder.Default
    private String sortBy = "displayOrder";

    /**
     * Sort Direction.
     *
     * Allowed Values:
     * ASC
     * DESC
     */
    @Builder.Default
    private String sortDirection = "ASC";

}