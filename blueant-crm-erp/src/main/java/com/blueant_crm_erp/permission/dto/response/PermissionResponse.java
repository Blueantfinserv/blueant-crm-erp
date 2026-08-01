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
 * Permission Response
 * =============================================================================
 *
 * Complete response DTO representing a Permission.
 *
 * Used By:
 * • Create Permission API
 * • Update Permission API
 * • Get Permission By Id API
 * • Get Permission By Code API
 * • Permission Details API
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
public class PermissionResponse implements Serializable {

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
     * Additional Remarks.
     */
    @Schema(description = "Remarks", example = "Example Remarks")
    private String remarks;

    /**
     * Created By User.
     */
    @Schema(description = "Created By", example = "Example Created By")
    private String createdBy;

    /**
     * Permission Creation Date & Time.
     */
    @Schema(description = "Created At", example = "Example Created At")
    private LocalDateTime createdAt;

    /**
     * Last Updated By User.
     */
    @Schema(description = "Updated By", example = "Example Updated By")
    private String updatedBy;

    /**
     * Last Updated Date & Time.
     */
    @Schema(description = "Updated At", example = "Example Updated At")
    private LocalDateTime updatedAt;

    /**
     * Soft Delete Flag.
     */
    @Schema(description = "Deleted", example = "Example Deleted")
    private Boolean deleted;

    /**
     * Deleted By User.
     */
    @Schema(description = "Deleted By", example = "Example Deleted By")
    private String deletedBy;

    /**
     * Permission Deletion Date & Time.
     */
    @Schema(description = "Deleted At", example = "Example Deleted At")
    private LocalDateTime deletedAt;

}