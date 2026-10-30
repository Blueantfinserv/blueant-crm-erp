package com.blueant_crm_erp.client.bulk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkClientImportJobStatusResponse {
    private String jobId;
    private String status;
    private String fileName;
    private Integer totalRows;
    private Integer processedRows;
    private String startClientCode;
    private String endClientCode;
    private String errorMessage;
    private Integer totalErrorsCount;
    private List<ClientImportRowErrorDto> errors;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}

