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
 * Department Response
 * =============================================================================
 *
 * Complete response DTO representing a Department.
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Create Department API
 * • Update Department API
 * • Get Department By Id API
 * • Department Details API
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Departments
 * -----------------------------------------------------------------------------
 * • Sales
 * • Operations
 * • CRM
 * • HR
 * • Accounts
 * • Helpdesk
 * • Mutual Fund
 * • Insurance
 * • Share
 * • Loan
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
public class DepartmentResponse implements Serializable {

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
     * Display Order.
     */
    @Schema(description = "Display Order", example = "Example Display Order")
    private Integer displayOrder;

    /**
     * Total Teams under this Department.
     */
    @Schema(description = "Total Teams", example = "Example Total Teams")
    private Integer totalTeams;

    /**
     * Total Users in this Department.
     */
    @Schema(description = "Total Users", example = "Example Total Users")
    private Integer totalUsers;

    /**
     * Department Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

    /**
     * Department Description.
     */
    @Schema(description = "Description", example = "Example Description")
    private String description;

    /**
     * Additional Remarks.
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