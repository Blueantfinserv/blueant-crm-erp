package com.blueant_crm_erp.client.bulk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientImportRowErrorDto {
    private int rowNumber;
    private String fieldName;
    private String errorCode;
    private String errorMessage;
}

