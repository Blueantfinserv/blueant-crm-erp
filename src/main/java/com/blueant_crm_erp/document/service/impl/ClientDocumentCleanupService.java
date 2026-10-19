package com.blueant_crm_erp.document.service.impl;

import com.blueant_crm_erp.client.repository.ClientDocumentRepository;
import com.blueant_crm_erp.document.repository.DocumentRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "filesystem", matchIfMissing = true)
public class ClientDocumentCleanupService {

    private final FileSystemStorageServiceImpl storageService;
    private final ClientDocumentRepository clientDocumentRepository;
    private final DocumentRepository documentRepository;

    @Value("${storage.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${storage.cleanup.retention-hours:24}")
    private int retentionHours;

    @Scheduled(cron = "${storage.cleanup.cron:0 0 2 * * ?}")
    public void runScheduledCleanup() {
        if (!cleanupEnabled) {
            log.info("Scheduled temporary storage cleanup is disabled by configuration.");
            return;
        }
        cleanupOrphanedTemporaryUploads();
    }

    public CleanupResult cleanupOrphanedTemporaryUploads() {
        Path tempDir = storageService.getTempDir();
        if (tempDir == null || !Files.exists(tempDir)) {
            return CleanupResult.builder().scanned(0).deleted(0).protectedFiles(0).build();
        }

        Instant cutoff = Instant.now().minus(retentionHours, ChronoUnit.HOURS);
        int scanned = 0;
        int deleted = 0;
        int protectedFiles = 0;

        try (Stream<Path> stream = Files.list(tempDir)) {
            for (Path file : (Iterable<Path>) stream::iterator) {
                if (!Files.isRegularFile(file)) {
                    continue;
                }
                scanned++;

                try {
                    Instant lastModified = Files.getLastModifiedTime(file).toInstant();
                    if (lastModified.isBefore(cutoff)) {
                        String filename = file.getFileName().toString();

                        boolean isReferenced = clientDocumentRepository.existsByDocumentReferenceContaining(filename) ||
                                documentRepository.existsByS3KeyContaining(filename);

                        if (!isReferenced) {
                            Files.deleteIfExists(file);
                            deleted++;
                        } else {
                            protectedFiles++;
                        }
                    } else {
                        protectedFiles++;
                    }
                } catch (IOException e) {
                    log.error("Error inspecting file during temporary upload cleanup", e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to list files in temporary storage directory during cleanup", e);
        }

        log.info("Temporary storage cleanup executed: scanned={}, deleted={}, protected={}, retentionHours={}",
                scanned, deleted, protectedFiles, retentionHours);

        return CleanupResult.builder()
                .scanned(scanned)
                .deleted(deleted)
                .protectedFiles(protectedFiles)
                .build();
    }

    @Getter
    @Builder
    public static class CleanupResult {
        private final int scanned;
        private final int deleted;
        private final int protectedFiles;
    }
}
