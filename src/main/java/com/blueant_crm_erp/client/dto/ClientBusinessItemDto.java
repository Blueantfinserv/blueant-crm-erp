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
@Schema(description = "Business item representing an individual category and its optional dedicated amount.")
public class ClientBusinessItemDto {

    @NotBlank(message = "Business type is required.")
    @Schema(description = "Applicable business category (e.g. Bank Change / Update, SIP STP Start, Redeem, Lumpsum Switch / Change)")
    private String businessType;

    @Schema(description = "User-friendly display name of the business category")
    private String displayName;

    @Schema(description = "Optional dedicated amount for the business option (null or blank allowed; cannot be negative)")
    private BigDecimal amount;
}
