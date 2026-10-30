package com.blueant_crm_erp.client.bulk.service.impl;

import com.blueant_crm_erp.client.bulk.dto.ClientImportRowErrorDto;
import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import com.blueant_crm_erp.client.bulk.repository.ClientImportJobRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImportJobRecoveryCoordinator {

    private final ClientImportJobRepository jobRepository;
    private final ClientImportJobStatusService jobStatusService;
    private final BulkClientImportAsyncWorker asyncWorker;

    @PersistenceContext
    private final EntityManager entityManager;

    private static final List<String> IN_PROGRESS_STATUSES = List.of("PENDING", "VALIDATING", "EXECUTING");
    private static final int TIMEOUT_MINUTES = 10;

    /**
     * Safely re-queues an interrupted or recovered bulk import job.
     * Guaranteed to execute only when the source file is durable in storage.
     * Reprocessing starts from zero and cannot create duplicate clients or consume client codes
     * because any prior aborted transaction was rolled back by the database.
     */
    @Transactional
    public boolean reQueueJob(String jobId) {
        return reQueueJob(jobId, null);
    }

    @Transactional
    public boolean reQueueJob(String jobId, String recoveryNote) {
        ClientImportJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.warn("Cannot re-queue non-existent bulk import job: {}", jobId);
            return false;
        }

        if (job.getStorageFileKey() == null || job.getStorageFileKey().isBlank()) {
            log.error("Cannot re-queue bulk import job {}: storageFileKey is null (file not durable).", jobId);
            jobStatusService.markFailedSystem(jobId, "Cannot re-queue job: source file was not persisted to durable storage.");
            return false;
        }

        // Idempotency guard: check if clients or assignment histories were already committed for this job
        String reason = BulkClientPersistenceService.BULK_IMPORT_REASON_PREFIX + jobId;
        Long historyCount = entityManager.createQuery(
                "SELECT COUNT(h) FROM ClientAssignmentHistory h WHERE h.reason = :reason", Long.class)
                .setParameter("reason", reason)
                .getSingleResult();
        Long distinctClientCount = entityManager.createQuery(
                "SELECT COUNT(DISTINCT h.client.id) FROM ClientAssignmentHistory h WHERE h.reason = :reason", Long.class)
                .setParameter("reason", reason)
                .getSingleResult();

        long maxCount = Math.max(historyCount != null ? historyCount : 0L, distinctClientCount != null ? distinctClientCount : 0L);
        if (maxCount > 0) {
            int expectedRows = job.getTotalRows() != null ? job.getTotalRows() : 0;
            if (expectedRows > 0 && historyCount != null && distinctClientCount != null
                    && historyCount.intValue() == expectedRows && distinctClientCount.intValue() == expectedRows) {
                log.warn("Cannot re-queue job {}: all {} clients and assignment histories were already persisted! Reconciling to COMPLETED.",
                        jobId, distinctClientCount);
                reconcileCompletedJob(job, reason, distinctClientCount.intValue());
            } else {
                log.error("Cannot re-queue job {}: partial persistence detected ({} clients, {} histories committed out of {} expected). Manual operator intervention required.",
                        jobId, distinctClientCount, historyCount, expectedRows);
            }
            return false;
        }

        log.info("Safely re-queueing bulk import job {} from durable file: {}", jobId, job.getStorageFileKey());
        job.setStatus("PENDING");
        job.setProcessedRows(0);
        job.setHeartbeatAt(LocalDateTime.now());
        job.setErrorMessage(recoveryNote);
        job.setErrorDetailsJson(null);
        jobRepository.save(job);

        asyncWorker.processImportAsync(job.getId(), job.getStorageFileKey(), job.getFileName(), job.getCreatedBy());
        return true;
    }

    /**
     * Periodically inspects active jobs whose heartbeat has expired.
     * When the Spring Boot server restarts or crashes while a job was in-progress:
     * - Stale jobs in VALIDATING or PENDING state with durable storage are safely AUTO-REQUEUED
     *   (up to 1 retry attempt) since no database persistence occurred.
     * - Stale jobs in EXECUTING state are checked for committed clients:
     *   - If clients exist in DB, the job is reconciled to COMPLETED.
     *   - If 0 clients exist, the transaction rolled back and the job is marked FAILED (safe to re-queue).
     * - Jobs that failed an auto-recovery attempt are marked FAILED.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void recoverStaleJobs() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<ClientImportJob> staleJobs = jobRepository.findByStatusInAndHeartbeatAtBefore(IN_PROGRESS_STATUSES, threshold);

        if (!staleJobs.isEmpty()) {
            log.warn("Found {} stale bulk import jobs with heartbeat older than {} minutes", staleJobs.size(), TIMEOUT_MINUTES);
            for (ClientImportJob job : staleJobs) {
                // If the job was in VALIDATING or PENDING and has a durable file, auto-recover it once
                if ("VALIDATING".equals(job.getStatus()) || "PENDING".equals(job.getStatus())) {
                    boolean alreadyRetried = job.getErrorMessage() != null && job.getErrorMessage().contains("Auto-recovered");
                    if (!alreadyRetried && job.getStorageFileKey() != null && !job.getStorageFileKey().isBlank()) {
                        log.warn("Auto-recovering stale {} job {} from durable storage: {}",
                                job.getStatus(), job.getId(), job.getStorageFileKey());
                        reQueueJob(job.getId(), "Auto-recovered from crash");
                        continue;
                    }
                }

                // If job was EXECUTING (Phase 2), check whether client persistence committed prior to crash
                if ("EXECUTING".equals(job.getStatus())) {
                    String reason = BulkClientPersistenceService.BULK_IMPORT_REASON_PREFIX + job.getId();
                    Long historyCount = entityManager.createQuery(
                            "SELECT COUNT(h) FROM ClientAssignmentHistory h WHERE h.reason = :reason", Long.class)
                            .setParameter("reason", reason)
                            .getSingleResult();
                    Long distinctClientCount = entityManager.createQuery(
                            "SELECT COUNT(DISTINCT h.client.id) FROM ClientAssignmentHistory h WHERE h.reason = :reason", Long.class)
                            .setParameter("reason", reason)
                            .getSingleResult();

                    int expectedRows = job.getTotalRows() != null ? job.getTotalRows() : 0;
                    boolean fullyCommitted = expectedRows > 0 && historyCount != null && distinctClientCount != null
                            && historyCount.intValue() == expectedRows && distinctClientCount.intValue() == expectedRows;

                    long maxCount = Math.max(historyCount != null ? historyCount : 0L, distinctClientCount != null ? distinctClientCount : 0L);

                    if (fullyCommitted) {
                        log.info("Reconciling post-commit stale EXECUTING job {}: all {} expected clients and histories committed in DB",
                                job.getId(), distinctClientCount);
                        reconcileCompletedJob(job, reason, distinctClientCount.intValue());
                        continue;
                    } else if (maxCount > 0) {
                        log.error("Stale EXECUTING job {} has inconsistent or partial persistence: {} clients, {} histories committed out of {} expected! Marking FAILED for manual review.",
                                job.getId(), distinctClientCount, historyCount, expectedRows);
                        jobStatusService.markFailed(
                                job.getId(),
                                expectedRows,
                                "Partial client persistence detected: " + distinctClientCount + " of " + expectedRows + " clients committed. Manual reconciliation required. (Committed histories: " + historyCount + ")",
                                List.of(ClientImportRowErrorDto.builder()
                                        .rowNumber(0)
                                        .fieldName("persistence")
                                        .errorCode("PARTIAL_PERSISTENCE")
                                        .errorMessage("Inconsistent batch persistence. Expected: " + expectedRows + ", Committed Clients: " + distinctClientCount + ", Committed Histories: " + historyCount)
                                        .build())
                        );
                        continue;
                    } else {
                        log.warn("Stale EXECUTING job {} has 0 committed clients (transaction rolled back prior to crash). Marking FAILED.",
                                job.getId());
                        jobStatusService.markFailed(
                                job.getId(),
                                expectedRows,
                                "Import job timed out or server restarted before completion. Persistence transaction aborted before commit. 0 clients created. Safe to re-queue.",
                                List.of(ClientImportRowErrorDto.builder()
                                        .rowNumber(0)
                                        .fieldName("persistence")
                                        .errorCode("TRANSACTION_ABORTED")
                                        .errorMessage("Server was interrupted before persistence commit. Zero clients were saved.")
                                        .build())
                        );
                        continue;
                    }
                }

                // If already retried or unhandled state, mark FAILED for safety
                log.warn("Marking stale bulk import job {} (Status: {}, Last Heartbeat: {}) as FAILED",
                        job.getId(), job.getStatus(), job.getHeartbeatAt());

                jobStatusService.markFailed(
                        job.getId(),
                        job.getTotalRows() != null ? job.getTotalRows() : 0,
                        "Import job timed out or server restarted before completion. Last heartbeat: " + job.getHeartbeatAt(),
                        List.of(ClientImportRowErrorDto.builder()
                                .rowNumber(0)
                                .fieldName("system")
                                .errorCode("HEARTBEAT_TIMEOUT")
                                .errorMessage("Job stalled or server was restarted while processing. Heartbeat ceased.")
                                .build())
                );
            }
        }
    }

    private void reconcileCompletedJob(ClientImportJob job, String reason, int count) {
        List<String> codes = entityManager.createQuery(
                "SELECT h.client.clientCode FROM ClientAssignmentHistory h WHERE h.reason = :reason ORDER BY h.client.clientCode ASC",
                String.class)
                .setParameter("reason", reason)
                .getResultList();

        String startCode = codes.isEmpty() ? null : codes.get(0);
        String endCode = codes.isEmpty() ? null : codes.get(codes.size() - 1);

        job.setStatus("COMPLETED");
        job.setProcessedRows(count);
        job.setStartClientCode(startCode);
        job.setEndClientCode(endCode);
        job.setCompletedAt(job.getHeartbeatAt() != null ? job.getHeartbeatAt() : LocalDateTime.now());
        job.setErrorMessage("Reconciled from post-commit crash");
        job.setErrorDetailsJson(null);
        jobRepository.save(job);
        log.info("Job {} reconciled to COMPLETED: {} clients ({} to {})", job.getId(), count, startCode, endCode);
    }
}

