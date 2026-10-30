package com.blueant_crm_erp.client.bulk.service;

import com.blueant_crm_erp.client.bulk.dto.BulkClientImportJobStatusResponse;
import com.blueant_crm_erp.client.bulk.dto.BulkClientImportSubmitResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BulkClientImportService {

    BulkClientImportSubmitResponse submitImport(MultipartFile file, String actorUsername);

    BulkClientImportJobStatusResponse getJobStatus(String jobId, String actorUsername, int page, int size);

    byte[] generateTemplate();
}

