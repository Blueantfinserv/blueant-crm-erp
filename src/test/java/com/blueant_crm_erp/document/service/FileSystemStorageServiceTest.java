package com.blueant_crm_erp.document.service;

import com.blueant_crm_erp.client.repository.ClientDocumentRepository;
import com.blueant_crm_erp.document.repository.DocumentRepository;
import com.blueant_crm_erp.document.service.impl.ClientDocumentCleanupService;
import com.blueant_crm_erp.document.service.impl.FileSystemStorageServiceImpl;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class FileSystemStorageServiceTest {

    @TempDir
    Path tempStorageRoot;

    private FileSystemStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new FileSystemStorageServiceImpl();
        ReflectionTestUtils.setField(storageService, "configuredBaseDir", tempStorageRoot.toString());
        ReflectionTestUtils.setField(storageService, "failFastOnStartup", false);
        storageService.init();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempStorageRoot)) {
            try (var walk = Files.walk(tempStorageRoot)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    @Test
    @DisplayName("1. Upload and download verifies 100% exact byte integrity for stored KYC files")
    void testUploadAndDownloadByteIntegrity() {
        byte[] originalBytes = "%PDF-1.4 sample doctor medical licence document bytes".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "doctor_license.pdf", "application/pdf", originalBytes);

        String key = storageService.uploadFile(file);

        assertThat(key).startsWith("documents/");
        assertThat(key).endsWith(".pdf");

        byte[] downloadedBytes = storageService.downloadFile(key);
        assertThat(downloadedBytes).isEqualTo(originalBytes);
    }

    @Test
    @DisplayName("2. Delete file successfully removes physical object from storage")
    void testDeleteFile() {
        byte[] bytes = "%PDF-1.4 delete test".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "to_delete.pdf", "application/pdf", bytes);

        String key = storageService.uploadFile(file);
        assertThat(storageService.downloadFile(key)).isEqualTo(bytes);

        storageService.deleteFile(key);

        assertThatThrownBy(() -> storageService.downloadFile(key))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("File not found for key");
    }

    @Test
    @DisplayName("3. Stored files survive service reinitialization pointing to the same persistent directory")
    void testPersistenceAcrossServiceReinitialization() {
        byte[] persistentBytes = "%PDF-1.4 persistent kyc record surviving restart".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "pan.pdf", "application/pdf", persistentBytes);

        String fileKey = storageService.uploadFile(file);

        // Simulate application restart with a fresh service instance on the same directory
        FileSystemStorageServiceImpl freshServiceInstance = new FileSystemStorageServiceImpl();
        ReflectionTestUtils.setField(freshServiceInstance, "configuredBaseDir", tempStorageRoot.toString());
        freshServiceInstance.init();

        byte[] retrievedAfterRestart = freshServiceInstance.downloadFile(fileKey);
        assertThat(retrievedAfterRestart).isEqualTo(persistentBytes);
    }

    @Test
    @DisplayName("4. Requesting nonexistent or missing file returns ResourceNotFoundException")
    void testMissingFileThrowsNotFound() {
        assertThatThrownBy(() -> storageService.downloadFile("documents/nonexistent-uuid-1234.pdf"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("File not found for key");
    }

    @Test
    @DisplayName("5. Path traversal attempts are strictly intercepted and rejected")
    void testPathTraversalRejected() {
        assertThatThrownBy(() -> storageService.downloadFile("../../../windows/system32/cmd.exe"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid file key or path traversal attempt");

        assertThatThrownBy(() -> storageService.downloadFile("..\\..\\boot.ini"))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> storageService.downloadFile("/etc/passwd"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("6. Temporary file upload creates temp/ key and promotion atomically moves file to documents/")
    void testTemporaryUploadAndPromotionLifecycle() {
        byte[] bytes = "%PDF-1.4 pre-registration kyc upload".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "pre_reg.pdf", "application/pdf", bytes);

        String tempKey = storageService.uploadTemporaryFile(file);
        assertThat(tempKey).startsWith("temp/");

        // Verify readable while in temp/
        byte[] tempBytes = storageService.downloadFile(tempKey);
        assertThat(tempBytes).isEqualTo(bytes);

        // Promote to permanent upon client registration
        String permanentKey = storageService.promoteToPermanent(tempKey);
        assertThat(permanentKey).startsWith("documents/");
        assertThat(permanentKey).doesNotStartWith("temp/");

        // Exact content readable from permanent key
        byte[] permBytes = storageService.downloadFile(permanentKey);
        assertThat(permBytes).isEqualTo(bytes);

        // Original temp path no longer exists (moved, not duplicated)
        Path oldTempPath = tempStorageRoot.resolve(tempKey);
        assertThat(Files.exists(oldTempPath)).isFalse();
    }

    @Test
    @DisplayName("7. Background cleanup purges expired unreferenced temporary files while protecting registered documents")
    void testTemporaryCleanupLifecycle() throws Exception {
        ClientDocumentRepository mockClientDocRepo = Mockito.mock(ClientDocumentRepository.class);
        DocumentRepository mockDocRepo = Mockito.mock(DocumentRepository.class);

        ClientDocumentCleanupService cleanupService = new ClientDocumentCleanupService(
                storageService, mockClientDocRepo, mockDocRepo);
        ReflectionTestUtils.setField(cleanupService, "cleanupEnabled", true);
        ReflectionTestUtils.setField(cleanupService, "retentionHours", 24);

        // 1. Expired UNREFERENCED file (uploaded 48 hours ago, not in DB) -> MUST BE DELETED
        byte[] abandonedBytes = "%PDF-1.4 abandoned upload".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file1 = new MockMultipartFile("file", "abandoned.pdf", "application/pdf", abandonedBytes);
        String abandonedKey = storageService.uploadTemporaryFile(file1);
        Path abandonedPath = tempStorageRoot.resolve(abandonedKey);
        Files.setLastModifiedTime(abandonedPath, FileTime.from(Instant.now().minus(48, ChronoUnit.HOURS)));

        // 2. Expired REFERENCED file (uploaded 48 hours ago, but referenced in DB) -> MUST BE PROTECTED
        byte[] referencedBytes = "%PDF-1.4 registered client doc".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file2 = new MockMultipartFile("file", "referenced.pdf", "application/pdf", referencedBytes);
        String referencedKey = storageService.uploadTemporaryFile(file2);
        Path referencedPath = tempStorageRoot.resolve(referencedKey);
        Files.setLastModifiedTime(referencedPath, FileTime.from(Instant.now().minus(48, ChronoUnit.HOURS)));

        // Mock DB: referencedPath filename is in DB, abandonedPath is NOT
        String referencedFilename = referencedPath.getFileName().toString();
        when(mockClientDocRepo.existsByDocumentReferenceContaining(referencedFilename)).thenReturn(true);
        when(mockClientDocRepo.existsByDocumentReferenceContaining(abandonedPath.getFileName().toString())).thenReturn(false);
        when(mockDocRepo.existsByS3KeyContaining(anyString())).thenReturn(false);

        // 3. Recent temporary file (uploaded 1 hour ago) -> MUST BE PROTECTED (not expired)
        byte[] recentBytes = "%PDF-1.4 recent upload in progress".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file3 = new MockMultipartFile("file", "recent.pdf", "application/pdf", recentBytes);
        String recentKey = storageService.uploadTemporaryFile(file3);

        // Run cleanup
        ClientDocumentCleanupService.CleanupResult result = cleanupService.cleanupOrphanedTemporaryUploads();

        assertThat(result.getScanned()).isEqualTo(3);
        assertThat(result.getDeleted()).isEqualTo(1); // Exactly 1 abandoned file deleted
        assertThat(result.getProtectedFiles()).isEqualTo(2); // 2 protected

        // Assert file states on disk
        assertThat(Files.exists(abandonedPath)).isFalse();
        assertThat(Files.exists(referencedPath)).isTrue();
        assertThat(Files.exists(tempStorageRoot.resolve(recentKey))).isTrue();
    }

    @Test
    @DisplayName("8. Production fail-fast throws IllegalStateException if persistent mount is missing or unwritable")
    void testProductionFailFastWhenStorageRootMissing() {
        FileSystemStorageServiceImpl prodStorage = new FileSystemStorageServiceImpl();
        String missingPath = tempStorageRoot.resolve("nonexistent-mount-" + java.util.UUID.randomUUID()).toString();
        ReflectionTestUtils.setField(prodStorage, "configuredBaseDir", missingPath);
        ReflectionTestUtils.setField(prodStorage, "failFastOnStartup", true);

        assertThatThrownBy(prodStorage::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Production persistent storage path is unavailable");
    }
}
