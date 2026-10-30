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
public class BulkClientImportSubmitResponse {
    private String jobId;
    private String status;
    private String fileName;
    private String message;
}

