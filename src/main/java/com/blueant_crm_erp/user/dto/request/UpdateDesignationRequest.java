package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
 * Update Designation Request
 * =============================================================================
 *
 * Request DTO used to update an existing Designation.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Examples
 * -----------------------------------------------------------------------------
 * Business Head
 * Sales Manager
 * Team Leader
 * Sales Executive
 * CRM Executive
 * HR Executive
 * Operations Executive
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDesignationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Designation Name.
     */
    @NotBlank(message = "Designation name is required.")
    @Size(
            min = 2,
            max = 100,
            message = "Designation name must be between 2 and 100 characters."
    )
    @Schema(description = "Name", example = "Example Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * Designation Code.
     *
     * Example:
     * BH
     * SM
     * TL
     * SE
     * CRM
     */
    @NotBlank(message = "Designation code is required.")
    @Size(
            min = 2,
            max = 50,
            message = "Designation code must be between 2 and 50 characters."
    )
    @Schema(description = "Code", example = "Example Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /**
     * Department Id.
     */
    @NotNull(message = "Department is required.")
    @Schema(description = "Department Id", example = "Example Department Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long departmentId;

    /**
     * Hierarchy Level.
     *
     * Example:
     * Business Head = 1
     * Sales Manager = 2
     * Team Leader = 3
     * Sales Person = 4
     */
    @NotNull(message = "Hierarchy level is required.")
    @Min(value = 1, message = "Hierarchy level must be greater than zero.")
    @Max(value = 100, message = "Hierarchy level must not exceed 100.")
    @Schema(description = "Hierarchy Level", example = "Example Hierarchy Level", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer hierarchyLevel;

    /**
     * Display Order.
     */
    @NotNull(message = "Display order is required.")
    @Min(value = 1, message = "Display order must be greater than zero.")
    @Max(value = 9999, message = "Display order must not exceed 9999.")
    @Schema(description = "Display Order", example = "Example Display Order", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

    /**
     * Designation Status.
     */
    @NotNull(message = "Status is required.")
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Description.
     */
    @Size(
            max = 500,
            message = "Description must not exceed 500 characters."
    )
    @Schema(description = "Description", example = "Example Description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    /**
     * Remarks.
     */
    @Size(
            max = 500,
            message = "Remarks must not exceed 500 characters."
    )
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

}