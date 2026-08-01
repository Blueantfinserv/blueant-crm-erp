package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
 * User Search Request
 * =============================================================================
 *
 * Request DTO used for searching, filtering, sorting and pagination
 * of Users.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Supports
 * -----------------------------------------------------------------------------
 * • Keyword Search
 * • Department Filter
 * • Designation Filter
 * • Team Filter
 * • Role Filter
 * • Reporting Manager Filter
 * • Status Filter
 * • Pagination
 * • Sorting
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Rohit
 *      ↓
 * Sales Manager
 *      ↓
 * Team Leader
 *      ↓
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
public class UserSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Search Keyword.
     *
     * Searches In:
     * • Employee Code
     * • First Name
     * • Last Name
     * • Full Name
     * • Email
     * • Mobile Number
     */
    @Size(max = 100,
            message = "Keyword cannot exceed 100 characters.")
    @Schema(description = "Keyword", example = "Example Keyword", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;

    /**
     * Department Id.
     */
    @Schema(description = "Department Id", example = "Example Department Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long departmentId;

    /**
     * Designation Id.
     */
    @Schema(description = "Designation Id", example = "Example Designation Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long designationId;

    /**
     * Team Id.
     */
    @Schema(description = "Team Id", example = "Example Team Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long teamId;

    /**
     * Role Id.
     */
    @Schema(description = "Role Id", example = "Example Role Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;

    /**
     * Reporting Manager Id.
     */
    @Schema(description = "Reporting Manager Id", example = "Example Reporting Manager Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reportingManagerId;

    /**
     * User Status.
     */
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Page Number.
     */
    @Builder.Default
    @Min(value = 0,
            message = "Page number cannot be negative.")
    private Integer page = 0;

    /**
     * Page Size.
     */
    @Builder.Default
    @Min(value = 1)
    @Max(value = 100)
    private Integer size = 10;

    /**
     * Sort Field.
     *
     * Examples:
     * firstName
     * employeeCode
     * createdAt
     * updatedAt
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Sort Direction.
     *
     * ASC
     * DESC
     */
    @Builder.Default
    private String sortDirection = "DESC";

}