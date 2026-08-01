package com.blueant_crm_erp.proposal.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProposalRequest {

    @NotNull(message = "Lead ID is required")
    private Long leadId;

    @NotNull(message = "Investment amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Investment amount must be greater than zero")
    private BigDecimal investmentAmount;

    @NotBlank(message = "Product type is required")
    private String productType;

    @NotNull(message = "Expected closure date is required")
    private LocalDate expectedClosureDate;

    private String remarks;
}
