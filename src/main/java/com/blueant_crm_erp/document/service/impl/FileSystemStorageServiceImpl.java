package com.blueant_crm_erp.document.service.impl;

import com.blueant_crm_erp.document.service.StorageService;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@Primary
@ConditionalOnProperty(name = "storage.type", havingValue = "filesystem", matchIfMissing = true)
public class FileSystemStorageServiceImpl implements StorageService {

    @Value("${storage.local.base-dir:${STORAGE_BASE_PATH:}}")
    private String configuredBaseDir;

    @Value("${storage.fail-fast-on-startup:false}")
    private boolean failFastOnStartup;

    @Getter
    private Path rootDir;

    @Getter
    private Path documentsDir;

    @Getter
    private Path tempDir;

    @PostConstruct
    public void init() {
        String baseDirStr = configuredBaseDir;
        if (!StringUtils.hasText(baseDirStr)) {
            baseDirStr = System.getenv("STORAGE_BASE_PATH");
        }

        boolean isRailwayProduction = System.getenv("RAILWAY_ENVIRONMENT") != null;
        boolean failFast = failFastOnStartup || isRailwayProduction;

        if (!StringUtils.hasText(baseDirStr)) {
            if (failFast) {
                baseDirStr = "/app/storage";
            } else {
                baseDirStr = "storage";
            }
        }

        if (failFast) {
            this.rootDir = Paths.get(baseDirStr).toAbsolutePath().normalize();
            if (!Files.exists(this.rootDir) || !Files.isDirectory(this.rootDir)) {
                throw new IllegalStateException("Production persistent storage path is unavailable: " + this.rootDir +
                        ". Ensure Railway Persistent Volume is mounted at this path.");
            }

            if (!Files.isWritable(this.rootDir)) {
                throw new IllegalStateException("Production persistent storage path is not writable: " + this.rootDir +
                        ". Check Railway Volume permissions.");
            }

            // Verify write permission with probe file
            Path probe = this.rootDir.resolve(".writetest-" + UUID.randomUUID());
            try {
                Files.writeString(probe, "probe");
                Files.deleteIfExists(probe);
            } catch (Exception e) {
                throw new IllegalStateException("Production persistent storage path failed write test: " + this.rootDir, e);
            }
        } else {
            Path candidate = Paths.get(baseDirStr).toAbsolutePath().normalize();
            try {
                if (!Files.exists(candidate)) {
                    Files.createDirectories(candidate);
                }
                this.rootDir = candidate;
            } catch (Exception e) {
                log.warn("Configured storage base-dir '{}' inaccessible. Falling back to local './storage'", baseDirStr);
                this.rootDir = Paths.get("storage").toAbsolutePath().normalize();
                try {
                    Files.createDirectories(this.rootDir);
                } catch (IOException ex) {
                    throw new IllegalStateException("Failed to initialize local fallback storage directory: " + this.rootDir, ex);
                }
            }
        }

        this.documentsDir = this.rootDir.resolve("documents").normalize();
        this.tempDir = this.rootDir.resolve("temp").normalize();

        try {
            Files.createDirectories(this.documentsDir);
            Files.createDirectories(this.tempDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create storage subdirectories under: " + this.rootDir, e);
        }

        log.info("Initialized FileSystemStorageService at persistent root: {}", this.rootDir);
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return storeFile(file, false);
    }

    @Override
    public String uploadTemporaryFile(MultipartFile file) {
        return storeFile(file, true);
    }

    private String storeFile(MultipartFile file, boolean temporary) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty.");
        }

        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + extension;
        String namespace = temporary ? "temp" : "documents";
        String fileKey = namespace + "/" + filename;

        Path targetDir = temporary ? this.tempDir : this.documentsDir;
        Path targetPath = resolveSecurePath(fileKey);

        try {
            // Atomic file write using temporary staging file
            Path stagingFile = Files.createTempFile(targetDir, "upload-", ".part");
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, stagingFile, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(stagingFile, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(stagingFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to store file securely under key: {}", fileKey, e);
            throw new BadRequestException("Failed to securely store file.");
        }

        log.info("Securely stored file with key: {}", fileKey);
        return fileKey;
    }

    @Override
    public byte[] downloadFile(String fileKey) {
        Path targetPath = findExistingFilePath(fileKey);
        if (targetPath == null || !Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            log.warn("File not found for key: {}", fileKey);
            throw new ResourceNotFoundException("File not found for key: " + fileKey);
        }

        try {
            return Files.readAllBytes(targetPath);
        } catch (IOException e) {
            log.error("Failed to read file bytes for key: {}", fileKey, e);
            throw new ResourceNotFoundException("Unable to read file content for key: " + fileKey);
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        Path targetPath = findExistingFilePath(fileKey);
        if (targetPath != null && Files.exists(targetPath)) {
            try {
                Files.deleteIfExists(targetPath);
                log.info("Deleted file for key: {}", fileKey);
            } catch (IOException e) {
                log.error("Failed to delete file for key: {}", fileKey, e);
            }
        }
    }

    @Override
    public String promoteToPermanent(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            return fileKey;
        }

        String trimmed = fileKey.trim();
        if (!trimmed.startsWith("temp/")) {
            return trimmed; // Already permanent or legacy key
        }

        Path tempPath = resolveSecurePath(trimmed);
        if (Files.exists(tempPath) && Files.isRegularFile(tempPath)) {
            String filename = tempPath.getFileName().toString();
            String permanentKey = "documents/" + filename;
            Path permPath = resolveSecurePath(permanentKey);
            try {
                try {
                    Files.move(tempPath, permPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tempPath, permPath, StandardCopyOption.REPLACE_EXISTING);
                }
                log.info("Promoted temporary upload to permanent storage: {}", permanentKey);
                return permanentKey;
            } catch (IOException e) {
                log.error("Failed to promote temporary file: {} to permanent key: {}", trimmed, permanentKey, e);
                return trimmed; // Retain temp reference if move fails
            }
        }

        return trimmed;
    }

    public Path resolveSecurePath(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            throw new BadRequestException("File key cannot be null or empty.");
        }

        String normalizedKey = fileKey.trim();
        if (normalizedKey.contains("..") || normalizedKey.contains("\\") || normalizedKey.startsWith("/")) {
            throw new BadRequestException("Invalid file key or path traversal attempt detected.");
        }

        Path resolved = this.rootDir.resolve(normalizedKey).normalize();
        if (!resolved.startsWith(this.rootDir)) {
            throw new SecurityException("Path traversal violation: key falls outside configured storage root.");
        }

        try {
            if (Files.exists(resolved) && Files.isSymbolicLink(resolved)) {
                throw new SecurityException("Symbolic links are strictly prohibited in storage paths.");
            }
        } catch (SecurityException se) {
            throw se;
        } catch (Exception ignored) {}

        return resolved;
    }

    private Path findExistingFilePath(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            return null;
        }

        // Secure resolution upfront: throws BadRequestException or SecurityException on traversal
        Path directPath = resolveSecurePath(fileKey);
        if (Files.exists(directPath) && Files.isRegularFile(directPath)) {
            return directPath;
        }

        // Fallback for bare keys or legacy keys without namespace prefix
        String bareName = Paths.get(fileKey).getFileName().toString();
        if (bareName.contains("..") || bareName.contains("\\") || bareName.contains("/")) {
            throw new BadRequestException("Invalid file key or path traversal attempt detected.");
        }

        try {
            Path docFallback = this.documentsDir.resolve(bareName).normalize();
            if (docFallback.startsWith(this.documentsDir) && Files.exists(docFallback) && Files.isRegularFile(docFallback)) {
                return docFallback;
            }
        } catch (Exception ignored) {}

        try {
            Path tempFallback = this.tempDir.resolve(bareName).normalize();
            if (tempFallback.startsWith(this.tempDir) && Files.exists(tempFallback) && Files.isRegularFile(tempFallback)) {
                return tempFallback;
            }
        } catch (Exception ignored) {}

        return null;
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        int idx = originalFilename.lastIndexOf('.');
        if (idx < 0 || idx == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(idx).toLowerCase(Locale.ENGLISH).trim();
    }
}
