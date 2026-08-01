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
 * Team Summary Response
 * =============================================================================
 *
 * Lightweight response DTO representing Team information.
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Team Listing API
 * • Team Search API
 * • Team Pagination
 * • Dashboard
 * • Team Management
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Business Head
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
public class TeamSummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Team Id.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Team Name.
     */
    @Schema(description = "Name", example = "Example Name")
    private String name;

    /**
     * Team Code.
     */
    @Schema(description = "Code", example = "Example Code")
    private String code;

    /**
     * Department Name.
     */
    @Schema(description = "Department Name", example = "Example Department Name")
    private String departmentName;

    /**
     * Team Leader Name.
     */
    @Schema(description = "Team Leader Name", example = "Example Team Leader Name")
    private String teamLeaderName;

    /**
     * Total Members in Team.
     */
    @Schema(description = "Total Members", example = "Example Total Members")
    private Integer totalMembers;

    /**
     * Display Order.
     */
    @Schema(description = "Display Order", example = "Example Display Order")
    private Integer displayOrder;

    /**
     * Team Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

}