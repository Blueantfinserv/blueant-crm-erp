package com.blueant_crm_erp.document.validator;

import com.blueant_crm_erp.document.exception.InvalidDocumentException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocumentValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final java.util.Set<String> FORBIDDEN_EXTENSIONS = java.util.Set.of(
            ".exe", ".sh", ".bat", ".cmd", ".js", ".vbs", ".msi", ".jar", ".ps1", ".com", ".scr"
    );

    public void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("File cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidDocumentException("File size exceeds maximum limit of 10MB");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase();
            for (String ext : FORBIDDEN_EXTENSIONS) {
                if (lower.endsWith(ext)) {
                    throw new InvalidDocumentException("Executable file types are not allowed: " + ext);
                }
            }
        }
    }
}
