package com.blueant_crm_erp.lead.dto.request;

import com.blueant_crm_erp.lead.enums.LeadSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * Create Lead Request
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * System  : CRM and Sales Management System
 * Module  : Lead Management
 *
 * Description:
 * Request DTO used by a Sales Person to create a new Lead.
 *
 * This DTO captures only the initial lead information entered by the user.
 * System-managed fields such as Lead Code, Unique Lead ID, Lead Status,
 * Lead Stage, Priority, Duplicate Status, Assignment, Audit Information,
 * and other workflow-related fields are generated automatically by the backend.
 *
 * Note:
 * Meeting details are intentionally excluded because a meeting is scheduled
 * only after the initial client interaction. Meeting information will be
 * managed by the Meeting module.
 *
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadRequest {

    /**
     * Client Name
     */
    @NotBlank(message = "Client name is required.")
    @Size(min = 2, max = 150,
            message = "Client name must be between 2 and 150 characters.")
    private String clientName;

    /**
     * Primary Mobile Number
     */
    @NotBlank(message = "Mobile number is required.")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Please enter a valid mobile number."
    )
    private String mobileNumber;

    /**
     * Alternate Mobile Number (Optional)
     */
    @Pattern(
            regexp = "^$|^[6-9]\\d{9}$",
            message = "Please enter a valid alternate mobile number."
    )
    private String alternateMobileNumber;

    /**
     * Email Address (Optional)
     */
    @Email(message = "Please enter a valid email address.")
    @Size(max = 150,
            message = "Email cannot exceed 150 characters.")
    private String email;

    /**
     * Client Location
     */
    @NotBlank(message = "Location is required.")
    @Size(max = 255,
            message = "Location cannot exceed 255 characters.")
    private String location;

    /**
     * Lead Remarks (Optional)
     */
    @Size(max = 1000,
            message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    /**
     * Lead Source
     */
    @NotNull(message = "Lead source is required.")
    private LeadSource leadSource;

}