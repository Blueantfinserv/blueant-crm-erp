package com.blueant_crm_erp.util.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * ==============================================================
 * File Validator
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility class for validating uploaded files.
 *
 * Features:
 * - Null Validation
 * - Empty File Validation
 * - File Size Validation
 * - File Extension Validation
 * - MIME Type Validation
 * - Image Validation
 * - PDF Validation
 *
 * Thread Safe : Yes
 * ==============================================================
 *
 * @author BlueAnt CRM ERP
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileValidator {

    /**
     * Maximum upload size (10 MB).
     */
    public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Allowed image extensions.
     */
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "gif",
            "bmp",
            "webp"
    );

    /**
     * Allowed document extensions.
     */
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "pdf",
            "doc",
            "docx",
            "xls",
            "xlsx",
            "csv",
            "txt"
    );

    /**
     * Checks whether file is valid.
     */
    public static boolean isValid(MultipartFile file) {
        return file != null
                && !file.isEmpty()
                && file.getOriginalFilename() != null;
    }

    /**
     * Checks file size.
     */
    public static boolean isValidSize(MultipartFile file,
                                      long maxSize) {

        return isValid(file)
                && file.getSize() <= maxSize;
    }

    /**
     * Uses default maximum size.
     */
    public static boolean isValidSize(MultipartFile file) {
        return isValidSize(file, DEFAULT_MAX_FILE_SIZE);
    }

    /**
     * Checks extension.
     */
    public static boolean hasAllowedExtension(
            MultipartFile file,
            String... allowedExtensions) {

        if (!isValid(file)
                || allowedExtensions == null
                || allowedExtensions.length == 0) {
            return false;
        }

        String extension = getExtension(file);

        return Arrays.stream(allowedExtensions)
                .map(value -> value.toLowerCase(Locale.ENGLISH))
                .anyMatch(extension::equals);
    }

    /**
     * Checks image file.
     */
    public static boolean isImage(MultipartFile file) {

        if (!isValid(file)) {
            return false;
        }

        return IMAGE_EXTENSIONS.contains(getExtension(file));
    }

    /**
     * Checks document file.
     */
    public static boolean isDocument(MultipartFile file) {

        if (!isValid(file)) {
            return false;
        }

        return DOCUMENT_EXTENSIONS.contains(getExtension(file));
    }

    /**
     * Checks PDF.
     */
    public static boolean isPdf(MultipartFile file) {

        return isValid(file)
                && "pdf".equals(getExtension(file));
    }

    /**
     * Checks MIME type.
     */
    public static boolean hasContentType(
            MultipartFile file,
            String... contentTypes) {

        if (!isValid(file)
                || file.getContentType() == null
                || contentTypes == null) {
            return false;
        }

        Set<String> allowed = new HashSet<>();

        Arrays.stream(contentTypes)
                .map(type -> type.toLowerCase(Locale.ENGLISH))
                .forEach(allowed::add);

        return allowed.contains(
                file.getContentType()
                        .toLowerCase(Locale.ENGLISH)
        );
    }

    /**
     * Returns file extension.
     */
    public static String getExtension(MultipartFile file) {

        if (!isValid(file)) {
            return "";
        }

        String filename = file.getOriginalFilename();

        int index = filename.lastIndexOf('.');

        if (index < 0) {
            return "";
        }

        return filename.substring(index + 1)
                .toLowerCase(Locale.ENGLISH);
    }

}