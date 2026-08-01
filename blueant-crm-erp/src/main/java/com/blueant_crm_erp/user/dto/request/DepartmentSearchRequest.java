package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Department Search Request
 * =============================================================================
 *
 * Request DTO for Department Search.
 *
 * Supports
 * -----------------------------------------------------------------------------
 * • Global Keyword Search
 * • Status Filter
 * • Pagination
 * • Sorting
 *
 * Keyword Searches
 * -----------------------------------------------------------------------------
 * • Department Code
 * • Department Name
 * • Description
 * • Remarks
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
public class DepartmentSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Global Search Keyword.
     */
    @Size(max = 100)
    @Schema(description = "Keyword", example = "Example Keyword", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;

    /**
     * Department Status.
     */
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Page Number.
     */
    @Builder.Default
    @Min(0)
    private Integer page = 0;

    /**
     * Page Size.
     */
    @Builder.Default
    @Min(1)
    @Max(100)
    private Integer size = 10;

    /**
     * Sort Field.
     */
    @Builder.Default
    private String sortBy = "displayOrder";

    /**
     * Sort Direction.
     */
    @Builder.Default
    private String sortDirection = "ASC";

}