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
 * Designation Response
 * =============================================================================
 *
 * Complete response DTO representing a Designation.
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Create Designation API
 * • Update Designation API
 * • Get Designation By Id API
 * • Designation Details API
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
public class DesignationResponse implements Serializable {

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
     * Hierarchy Level.
     *
     * Example:
     * 1 = Business Head
     * 2 = Sales Manager
     * 3 = Team Leader
     * 4 = Sales Person
     */
    @Schema(description = "Hierarchy Level", example = "Example Hierarchy Level")
    private Integer hierarchyLevel;

    /**
     * Display Order.
     */
    @Schema(description = "Display Order", example = "Example Display Order")
    private Integer displayOrder;

    /**
     * Number of Users assigned to this Designation.
     */
    @Schema(description = "Total Users", example = "Example Total Users")
    private Integer totalUsers;

    /**
     * Current Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

    /**
     * Description.
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

}