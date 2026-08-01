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
 * Module  : Lead Management
 *
 * Description:
 * Request DTO used by a Sales Person to create a new Lead.
 *
 * This DTO captures only the initial lead information.
 * Business-managed fields such as Lead Code, Unique Lead ID,
 * Lead Date, Lead Time, Assigned Sales Person, Assigned Leader,
 * Lead Status, Lead Stage, Priority, Audit Fields, and Duplicate Status
 * are generated automatically by the backend.
 *
 * NOTE: Meeting information (Meeting Date, Meeting Time, Meeting Mode,
 * Meeting With, etc.) does NOT belong to Lead creation.
 * A meeting happens only after the salesperson contacts the client.
 * Meeting data will be managed by the future Meeting module.
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
    @Size(min = 2, max = 100, message = "Client name must be between 2 and 100 characters.")
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
    @Size(max = 100, message = "Email cannot exceed 100 characters.")
    private String email;

    /**
     * Client Location
     */
    @NotBlank(message = "Location is required.")
    @Size(max = 100, message = "Location cannot exceed 100 characters.")
    private String location;

    /**
     * Company Name (Optional)
     */
    @Size(max = 150, message = "Company name cannot exceed 150 characters.")
    private String companyName;

    /**
     * Lead Source
     */
    @NotNull(message = "Lead source is required.")
    private LeadSource leadSource;

    /**
     * Initial Remarks
     */
    @NotBlank(message = "Remarks are required.")
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

}