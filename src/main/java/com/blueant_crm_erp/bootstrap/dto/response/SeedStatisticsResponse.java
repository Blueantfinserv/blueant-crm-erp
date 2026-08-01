package com.blueant_crm_erp.bootstrap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Seed Statistics Response
 * =============================================================================
 *
 * Represents database seed statistics.
 *
 * Used by Bootstrap Dashboard APIs.
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
@Schema(description = "Database Seed Statistics")
public class SeedStatisticsResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "6")
    private Long totalDepartments;

    @Schema(example = "20")
    private Long totalDesignations;

    @Schema(example = "10")
    private Long totalTeams;

    @Schema(example = "5")
    private Long totalRoles;

    @Schema(example = "128")
    private Long totalPermissions;

    @Schema(example = "412")
    private Long totalRolePermissionMappings;

    @Schema(example = "3")
    private Long totalUsers;

    @Schema(example = "584")
    private Long totalSeededRecords;

}