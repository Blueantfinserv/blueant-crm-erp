package com.blueant_crm_erp.bootstrap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * Bootstrap Summary Response
 * =============================================================================
 *
 * Represents the overall summary of the database bootstrap process.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Master Data Summary
 * • Security Data Summary
 * • Default User Summary
 * • Execution Statistics
 * • Bootstrap Health
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
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
@Schema(description = "Bootstrap Summary Response")
public class BootstrapSummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Total Departments.
     */
    @Schema(example = "6")
    private Long totalDepartments;

    /**
     * Total Designations.
     */
    @Schema(example = "20")
    private Long totalDesignations;

    /**
     * Total Teams.
     */
    @Schema(example = "10")
    private Long totalTeams;

    /**
     * Total Roles.
     */
    @Schema(example = "5")
    private Long totalRoles;

    /**
     * Total Permissions.
     */
    @Schema(example = "128")
    private Long totalPermissions;

    /**
     * Total Role Permission Mappings.
     */
    @Schema(example = "412")
    private Long totalRolePermissions;

    /**
     * Total Users.
     */
    @Schema(example = "3")
    private Long totalUsers;

    /**
     * Indicates whether Super Admin exists.
     */
    @Schema(example = "true")
    private Boolean superAdminCreated;

    /**
     * Indicates whether bootstrap completed successfully.
     */
    @Schema(example = "true")
    private Boolean bootstrapCompleted;

    /**
     * Last bootstrap execution time.
     */
    @Schema(description = "Last bootstrap execution timestamp")
    private LocalDateTime lastBootstrapTime;

}