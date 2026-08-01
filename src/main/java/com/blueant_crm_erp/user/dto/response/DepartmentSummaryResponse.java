package com.blueant_crm_erp.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Department Summary Response
 * =============================================================================
 *
 * Lightweight Department response used for:
 * -----------------------------------------------------------------------------
 * • Department Listing
 * • Department Search
 * • Pagination APIs
 * • Dashboard Tables
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
public class DepartmentSummaryResponse implements Serializable {

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

    /**
     * Total Teams.
     */
    @Schema(description = "Total Teams", example = "Example Total Teams")
    private Integer totalTeams;

    /**
     * Total Users.
     */
    @Schema(description = "Total Users", example = "Example Total Users")
    private Integer totalUsers;

    /**
     * Display Order.
     */
    @Schema(description = "Display Order", example = "Example Display Order")
    private Integer displayOrder;

    /**
     * Department Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

}