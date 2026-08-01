package com.blueant_crm_erp.permission.dto.response;

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
 * Permission Summary Response
 * =============================================================================
 *
 * Lightweight response DTO representing Permission information.
 *
 * Used By:
 * • Permission Listing API
 * • Search Permission API
 * • Pagination API
 * • Grid/Table View
 * • Dashboard
 *
 * This DTO is optimized for list views and avoids sending
 * unnecessary audit information.
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
public class PermissionSummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Permission Identifier.
     */
    @Schema(description = "Id", example = "Example Id")
    private Long id;

    /**
     * Permission Name.
     */
    @Schema(description = "Name", example = "Example Name")
    private String name;

    /**
     * Unique Permission Code.
     */
    @Schema(description = "Code", example = "Example Code")
    private String code;

    /**
     * Module Name.
     *
     * Examples:
     * SALES
     * CRM
     * USER
     * ROLE
     * HR
     * REPORT
     * DASHBOARD
     */
    @Schema(description = "Module", example = "Example Module")
    private String module;

    /**
     * Permission Description.
     */
    @Schema(description = "Description", example = "Example Description")
    private String description;

    /**
     * Display Order.
     */
    @Schema(description = "Display Order", example = "Example Display Order")
    private Integer displayOrder;

    /**
     * Indicates whether this is a System Permission.
     */
    @Schema(description = "System Permission", example = "Example System Permission")
    private Boolean systemPermission;

    /**
     * Current Permission Status.
     */
    @Schema(description = "Status", example = "Example Status")
    private Status status;

    /**
     * Permission Creation Date & Time.
     */
    @Schema(description = "Created At", example = "Example Created At")
    private LocalDateTime createdAt;

}