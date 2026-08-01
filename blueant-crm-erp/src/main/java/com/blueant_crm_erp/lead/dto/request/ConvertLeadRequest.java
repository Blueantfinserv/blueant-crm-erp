package com.blueant_crm_erp.lead.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ============================================================================
 * Convert Lead Request
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Request DTO used to convert an existing lead into a client.
 *
 * ============================================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertLeadRequest {

    /**
     * Lead ID.
     */
    @NotNull(message = "Lead ID is required.")
    private Long leadId;

    /**
     * Client PAN Number.
     */
    @NotBlank(message = "PAN number is required.")
    @Pattern(
            regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "Invalid PAN number."
    )
    private String panNumber;

    /**
     * Investment Amount.
     */
    @NotNull(message = "Investment amount is required.")
    @DecimalMin(value = "1.00", message = "Investment amount must be greater than zero.")
    private BigDecimal investmentAmount;

    /**
     * Product Type.
     * Example:
     * SIP
     * Lumpsum
     * Mutual Fund
     * Insurance
     */
    @NotBlank(message = "Product type is required.")
    @Size(max = 100, message = "Product type cannot exceed 100 characters.")
    private String productType;

    /**
     * Conversion Date.
     */
    @NotNull(message = "Conversion date is required.")
    private LocalDate conversionDate;

    /**
     * Conversion Remarks.
     */
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

}