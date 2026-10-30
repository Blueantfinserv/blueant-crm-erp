package com.blueant_crm_erp.client.bulk.controller;

import com.blueant_crm_erp.client.bulk.dto.BulkClientImportJobStatusResponse;
import com.blueant_crm_erp.client.bulk.dto.BulkClientImportSubmitResponse;
import com.blueant_crm_erp.client.bulk.service.BulkClientImportService;
import com.blueant_crm_erp.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/v1/clients/bulk-import")
@RequiredArgsConstructor
@Tag(name = "Bulk Client Import API", description = "Endpoints for asynchronous bulk client creation and assignment from Excel/CSV")
public class BulkClientImportController {

    private final BulkClientImportService bulkClientImportService;

    @Operation(summary = "Submit bulk client import file",
            description = "Uploads an Excel or CSV file containing 4,000+ new external clients for asynchronous validation and atomic creation.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<BulkClientImportSubmitResponse> submitBulkImport(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        String currentUser = principal != null ? principal.getName() : "system";
        log.info("Bulk client import submitted by user: {}, filename: {}, size: {} bytes",
                currentUser, file.getOriginalFilename(), file.getSize());

        BulkClientImportSubmitResponse response = bulkClientImportService.submitImport(file, currentUser);
        return ApiResponse.success("Bulk client import submitted successfully", response);
    }

    @Operation(summary = "Get bulk client import job status",
            description = "Retrieves current status, progress counters, allocated client code range, and paginated validation errors for a job.")
    @GetMapping("/jobs/{jobId}")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<BulkClientImportJobStatusResponse> getJobStatus(
            @PathVariable String jobId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            Principal principal) {

        String currentUser = principal != null ? principal.getName() : "system";
        BulkClientImportJobStatusResponse response = bulkClientImportService.getJobStatus(jobId, currentUser, page, size);
        return ApiResponse.success("Job status retrieved successfully", response);
    }

    @Operation(summary = "Download bulk client import template",
            description = "Downloads a formatted Excel (.xlsx) template complete with mandatory and optional column headers and a sample client row.")
    @GetMapping("/template")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] templateData = bulkClientImportService.generateTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bulk_client_import_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(templateData);
    }
}

