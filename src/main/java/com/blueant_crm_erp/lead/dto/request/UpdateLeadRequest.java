package com.blueant_crm_erp.lead.dto.request;

import com.blueant_crm_erp.lead.enums.LeadPriority;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ============================================================================
 * Update Lead Request
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Request DTO used to update an existing lead.
 * This DTO is used after every client interaction or meeting.
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadRequest {

    /**
     * Lead ID
     */
    @NotNull(message = "Lead ID is required.")
    private Long leadId;

    /**
     * Client Name
     */
    @NotBlank(message = "Client name is required.")
    @Size(max = 100)
    private String clientName;

    /**
     * Primary Mobile Number
     */
    @NotBlank(message = "Mobile number is required.")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid mobile number."
    )
    private String mobileNumber;

    /**
     * Alternate Mobile Number
     */
    @Pattern(
            regexp = "^$|^[6-9]\\d{9}$",
            message = "Invalid alternate mobile number."
    )
    private String alternateMobileNumber;

    /**
     * Email Address
     */
    @Email(message = "Invalid email address.")
    private String email;

    /**
     * Client Location
     */
    @Size(max = 100)
    private String location;

    /**
     * Company Name
     */
    @Size(max = 150)
    private String companyName;

    /**
     * Lead Status
     */
    @NotNull(message = "Lead status is required.")
    private LeadStatus leadStatus;

    /**
     * Lead Stage
     */
    @NotNull(message = "Lead stage is required.")
    private LeadStage leadStage;

    /**
     * Lead Priority
     */
    @NotNull(message = "Lead priority is required.")
    private LeadPriority leadPriority;

    /**
     * Next Follow-up / Plan Date
     */
    private LocalDate nextPlanDate;

    /**
     * Last Client Call Date
     */
    private LocalDate lastCallDate;

    /**
     * Meeting Partner
     * Example:
     * Leader Name / Sales Manager Name
     */
    @Size(max = 100)
    private String joinedMeetingWith;

    /**
     * Meeting Remarks
     */
    @Size(max = 1000)
    private String remarks;

}