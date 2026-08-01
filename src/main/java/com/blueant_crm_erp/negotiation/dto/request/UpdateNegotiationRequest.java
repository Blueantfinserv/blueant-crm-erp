package com.blueant_crm_erp.negotiation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNegotiationRequest {

    @NotNull(message = "Agreed amount cannot be null")
    private BigDecimal agreedAmount;

    @Size(max = 1000, message = "Discussion details must not exceed 1000 characters")
    private String discussion;
}
