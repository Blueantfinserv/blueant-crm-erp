package com.blueant_crm_erp.client.validator;

import com.blueant_crm_erp.exception.common.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
public class ClientDocumentValidator {

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/pdf"
    );

    // Magic bytes signatures
    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D}; // %PDF-
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public void validateKycDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed limit of 10MB.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BadRequestException("File name cannot be empty.");
        }

        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file type: '" + extension + "'. Allowed file types: JPG, JPEG, PNG, PDF.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String lowerContentType = contentType.toLowerCase(Locale.ENGLISH).trim();
            if (!ALLOWED_MIME_TYPES.contains(lowerContentType)) {
                throw new BadRequestException("Invalid content type: '" + contentType + "'. Allowed types: image/jpeg, image/png, application/pdf.");
            }
        }

        // Validate File Signature (Magic Bytes)
        validateMagicBytes(file, extension);
    }

    private void validateMagicBytes(MultipartFile file, String extension) {
        byte[] header = new byte[8];
        int bytesRead;
        try (InputStream is = file.getInputStream()) {
            bytesRead = is.read(header);
        } catch (IOException e) {
            log.error("Failed to read file header for file validation", e);
            throw new BadRequestException("Unable to read file content for security validation.");
        }

        if (bytesRead < 3) {
            throw new BadRequestException("File is too small or corrupt to be a valid document.");
        }

        switch (extension) {
            case "pdf":
                if (!startsWith(header, PDF_MAGIC)) {
                    throw new BadRequestException("Invalid file signature: Content is not a valid PDF document.");
                }
                break;
            case "jpg":
            case "jpeg":
                if (!startsWith(header, JPEG_MAGIC)) {
                    throw new BadRequestException("Invalid file signature: Content is not a valid JPEG/JPG image.");
                }
                break;
            case "png":
                if (!startsWith(header, PNG_MAGIC)) {
                    throw new BadRequestException("Invalid file signature: Content is not a valid PNG image.");
                }
                break;
            default:
                throw new BadRequestException("Unsupported file extension: " + extension);
        }
    }

    private boolean startsWith(byte[] source, byte[] prefix) {
        if (source == null || prefix == null || source.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (source[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1).toLowerCase(Locale.ENGLISH).trim();
    }
}
