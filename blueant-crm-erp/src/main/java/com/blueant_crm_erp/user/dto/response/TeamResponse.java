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
import java.time.LocalDateTime;

/**
 * =============================================================================
 * Team Response
 * =============================================================================
 *
 * Complete response DTO representing a Team.
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Create Team API
 * • Update Team API
 * • Get Team By Id API
 * • Team Details API
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
public class TeamResponse implements Serializable {

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
     * Department Id.
     */
    @Schema(description = "Department Id", example = "Example Department Id")
    private Long departmentId;

    /**
     * Department Name.
     */
    @Schema(description = "Department Name", example = "Example Department Name")
    private String departmentName;

    /**
     * Team Leader User Id.
     */
    @Schema(description = "Team Leader Id", example = "Example Team Leader Id")
    private Long teamLeaderId;

    /**
     * Team Leader Employee Code.
     */
    @Schema(description = "Team Leader Code", example = "Example Team Leader Code")
    private String teamLeaderCode;

    /**
     * Team Leader Name.
     */
    @Schema(description = "Team Leader Name", example = "Example Team Leader Name")
    private String teamLeaderName;

    /**
     * Total Members.
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

    /**
     * Team Description.
     */
    @Schema(description = "Description", example = "Example Description")
    private String description;

    /**
     * Remarks.
     */
    @Schema(description = "Remarks", example = "Example Remarks")
    private String remarks;

    /**
     * Created By.
     */
    @Schema(description = "Created By", example = "Example Created By")
    private String createdBy;

    /**
     * Created Date & Time.
     */
    @Schema(description = "Created At", example = "Example Created At")
    private LocalDateTime createdAt;

    /**
     * Updated By.
     */
    @Schema(description = "Updated By", example = "Example Updated By")
    private String updatedBy;

    /**
     * Updated Date & Time.
     */
    @Schema(description = "Updated At", example = "Example Updated At")
    private LocalDateTime updatedAt;

    @Schema(description = "Total Users", example = "Example Total Users")
    private Integer totalUsers;
    @Schema(description = "Total Designations", example = "Example Total Designations")
    private Integer totalDesignations;

}