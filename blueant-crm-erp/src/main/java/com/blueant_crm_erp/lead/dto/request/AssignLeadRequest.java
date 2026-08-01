package com.blueant_crm_erp.lead.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * Assign Lead Request
 * ============================================================================
 *
 * Description:
 * Request DTO used to assign or re-assign a lead to a Sales Person.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignLeadRequest {

    /**
     * Lead ID to be assigned.
     */
    @NotNull(message = "Lead ID is required.")
    private Long leadId;

    /**
     * Sales Person User ID.
     */
    @NotNull(message = "Assigned user is required.")
    private Long assignedUserId;

    /**
     * Optional reason for assignment/reassignment.
     */
    @NotBlank(message = "Assignment reason is required.")
    private String assignmentReason;

}