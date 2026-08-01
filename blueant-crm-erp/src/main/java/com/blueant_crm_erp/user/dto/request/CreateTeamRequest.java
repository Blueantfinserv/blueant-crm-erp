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
 * Create Team Request
 * =============================================================================
 *
 * Request DTO used to create a new Team.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Examples
 * -----------------------------------------------------------------------------
 * Sales Team A
 * Sales Team B
 * Mutual Fund Team
 * Insurance Team
 * Helpdesk Team
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
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Team Name.
     */
    @NotBlank(message = "Team name is required.")
    @Size(
            min = 2,
            max = 100,
            message = "Team name must be between 2 and 100 characters."
    )
    @Schema(description = "Name", example = "Example Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * Team Code.
     *
     * Example:
     * SALES_A
     * MF_TEAM
     * INS_TEAM
     */
    @NotBlank(message = "Team code is required.")
    @Size(
            min = 2,
            max = 50,
            message = "Team code must be between 2 and 50 characters."
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
     * Team Leader User Id.
     */
    @Schema(description = "Team Leader Id", example = "Example Team Leader Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long teamLeaderId;

    /**
     * Display Order.
     */
    @NotNull(message = "Display order is required.")
    @Min(
            value = 1,
            message = "Display order must be greater than zero."
    )
    @Max(
            value = 9999,
            message = "Display order must not exceed 9999."
    )
    @Schema(description = "Display Order", example = "Example Display Order", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

    /**
     * Team Status.
     */
    @Builder.Default
    private Status status = Status.ACTIVE;

    /**
     * Team Description.
     */
    @Size(
            max = 500,
            message = "Description must not exceed 500 characters."
    )
    @Schema(description = "Description", example = "Example Description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    /**
     * Additional Remarks.
     */
    @Size(
            max = 500,
            message = "Remarks must not exceed 500 characters."
    )
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

}