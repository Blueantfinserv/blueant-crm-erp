package com.blueant_crm_erp.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Business item representing an individual category and its dedicated amount.")
public class ClientBusinessItemDto {

    @NotBlank(message = "Business type is required.")
    @Schema(description = "Applicable business category (e.g. SIP, LUMP_SUM, LOAN, SHARE/SHARES, INSURANCE, SERVICE_REQUEST, BROKER_CHANGE)")
    private String businessType;

    @Schema(description = "Dedicated amount for monetary categories (null or 0 allowed for non-monetary categories)")
    private BigDecimal amount;
}
