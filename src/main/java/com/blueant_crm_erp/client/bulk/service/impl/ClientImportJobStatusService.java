package com.blueant_crm_erp.client.bulk.service.impl;

import com.blueant_crm_erp.client.bulk.dto.ClientImportRowErrorDto;
import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import com.blueant_crm_erp.client.bulk.repository.ClientImportJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientImportJobStatusService {

    private final ClientImportJobRepository jobRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ClientImportJob createInitialJob(String jobId, String fileName, String fileHash, String storageFileKey, String createdBy) {
        LocalDateTime now = LocalDateTime.now();
        ClientImportJob job = ClientImportJob.builder()
                .id(jobId)
                .fileName(fileName)
                .fileHashSha256(fileHash)
                .storageFileKey(storageFileKey)
                .status("PENDING")
                .totalRows(0)
                .processedRows(0)
                .createdBy(createdBy)
                .createdAt(now)
                .heartbeatAt(now)
                .build();
        return jobRepository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ClientImportJob createInitialJob(String jobId, String fileName, String fileHash, String createdBy) {
        return createInitialJob(jobId, fileName, fileHash, null, createdBy);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claimJobForProcessing(String jobId) {
        return jobRepository.claimJobForProcessing(jobId, LocalDateTime.now()) > 0;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markValidating(String jobId, int totalRows) {
        LocalDateTime now = LocalDateTime.now();
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("VALIDATING");
            job.setTotalRows(totalRows);
            job.setStartedAt(now);
            job.setHeartbeatAt(now);
            jobRepository.save(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markExecuting(String jobId) {
        LocalDateTime now = LocalDateTime.now();
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("EXECUTING");
            job.setHeartbeatAt(now);
            jobRepository.save(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateHeartbeat(String jobId, int processedRows) {
        LocalDateTime now = LocalDateTime.now();
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setProcessedRows(processedRows);
            job.setHeartbeatAt(now);
            jobRepository.save(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(String jobId, int totalRows, String startCode, String endCode) {
        LocalDateTime now = LocalDateTime.now();
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("COMPLETED");
            job.setProcessedRows(totalRows);
            job.setStartClientCode(startCode);
            job.setEndClientCode(endCode);
            job.setCompletedAt(now);
            job.setHeartbeatAt(now);
            jobRepository.save(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String jobId, int totalRows, String errorMessage, List<ClientImportRowErrorDto> errors) {
        LocalDateTime now = LocalDateTime.now();
        String errorJson = null;
        if (errors != null && !errors.isEmpty()) {
            try {
                errorJson = objectMapper.writeValueAsString(errors);
            } catch (Exception e) {
                log.error("Failed to serialize import errors for job: {}", jobId, e);
                errorJson = "[{\"rowNumber\":0,\"fieldName\":\"system\",\"errorCode\":\"SERIALIZATION_ERROR\",\"errorMessage\":\"" + e.getMessage() + "\"}]";
            }
        }

        final String finalErrorJson = errorJson;
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus("FAILED");
            if (totalRows > 0) {
                job.setTotalRows(totalRows);
            }
            job.setProcessedRows(0);
            job.setErrorMessage(errorMessage != null && errorMessage.length() > 950 ? errorMessage.substring(0, 950) + "..." : errorMessage);
            job.setErrorDetailsJson(finalErrorJson);
            job.setCompletedAt(now);
            job.setHeartbeatAt(now);
            jobRepository.save(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedSystem(String jobId, String errorMessage) {
        markFailed(jobId, 0, errorMessage, List.of(
                ClientImportRowErrorDto.builder()
                        .rowNumber(0)
                        .fieldName("system")
                        .errorCode("SYSTEM_ERROR")
                        .errorMessage(errorMessage)
                        .build()
        ));
    }

    @Transactional(readOnly = true)
    public Optional<ClientImportJob> findJob(String jobId) {
        return jobRepository.findById(jobId);
    }
}

