package com.blueant_crm_erp.lead.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * Create Physical Lead Request
 * ============================================================================
 *
 * Description:
 * Request DTO used by a Sales Coordinator to receive or create a physical lead.
 * Contains: Name, Phone, Speciality, Location, Clinic Address, and optional Sales Person.
 *
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePhysicalLeadRequest {

    @NotBlank(message = "Client name is required.")
    @Size(min = 2, max = 150, message = "Client name must be between 2 and 150 characters.")
    private String clientName;

    @NotBlank(message = "Mobile number is required.")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Please enter a valid mobile number.")
    private String mobileNumber;

    @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "Please enter a valid alternate mobile number.")
    private String alternateMobileNumber;

    @Email(message = "Please enter a valid email address.")
    @Size(max = 150, message = "Email cannot exceed 150 characters.")
    private String email;

    @NotBlank(message = "Speciality is required.")
    @Size(max = 150, message = "Speciality cannot exceed 150 characters.")
    private String speciality;

    @NotBlank(message = "Location is required.")
    @Size(max = 255, message = "Location cannot exceed 255 characters.")
    private String location;

    @NotBlank(message = "Clinic address is required.")
    @Size(max = 255, message = "Clinic address cannot exceed 255 characters.")
    private String clinicAddress;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    /**
     * Sales Person employee code to assign immediately upon creation (Required).
     */
    @NotBlank(message = "Sales Person employee code is required.")
    private String salesPersonEmployeeCode;
}
