package com.blueant_crm_erp.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import com.blueant_crm_erp.common.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * =============================================================================
 * Change Department Status Request
 * =============================================================================
 *
 * Request DTO used for changing Department status.
 *
 * Supported Status
 * -----------------------------------------------------------------------------
 * • ACTIVE
 * • INACTIVE
 *
 * Used By
 * -----------------------------------------------------------------------------
 * • Activate Department
 * • Deactivate Department
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
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
public class ChangeDepartmentStatusRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Department Status.
     */
    @NotNull(message = "Department status is required.")
    @Schema(description = "Status", example = "Example Status", requiredMode = Schema.RequiredMode.REQUIRED)
    private Status status;

    /**
     * Reason for changing department status.
     */
    @Schema(description = "Remarks", example = "Example Remarks", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remarks;

}