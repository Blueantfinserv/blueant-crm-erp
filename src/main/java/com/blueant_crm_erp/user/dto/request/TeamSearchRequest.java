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
 * Team Search Request
 * =============================================================================
 *
 * Request DTO used for searching Teams with
 * filtering, sorting and pagination support.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Team Search
 * • Team Filtering
 * • Pagination
 * • Sorting
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Team Listing API
 * • Team Search API
 * • Dashboard
 * • Team Management
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
public class TeamSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Search by Team Name.
     */
    @Schema(description = "Name", example = "Example Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * Search by Team Code.
     */
    @Schema(description = "Code", example = "Example Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /**
     * Filter by Department.
     */
    @Schema(description = "Department Id", example = "Example Department Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long departmentId;

    /**
     * Filter by Team Leader.
     */
    @Schema(description = "Team Leader Id", example = "Example Team Leader Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long teamLeaderId;

    /**
     * Filter by Status.
     */
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Search by Remarks.
     */
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

    /**
     * Global Search Keyword.
     *
     * Searches:
     * • Team Code
     * • Team Name
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
     * Records per page.
     */
    @Builder.Default
    @Min(value = 1, message = "Page size must be greater than zero.")
    private Integer size = 10;

    /**
     * Sort Field.
     */
    @Builder.Default
    private String sortBy = "displayOrder";

    /**
     * Sort Direction.
     * ASC / DESC
     */
    @Builder.Default
    private String sortDirection = "ASC";

}