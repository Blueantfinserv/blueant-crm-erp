package com.blueant_crm_erp.util.file;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility class for file name operations.
 *
 * Responsibilities:
 * - Generate unique file names
 * - Sanitize file names
 * - Extract extension
 * - Remove extension
 * - Build timestamp based names
 *
 * Used By:
 * - File Upload
 * - Client Module
 * - Lead Module
 * - Service Request Module
 * - Report Module
 * - Document Management
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class FileNameUtil {

    /**
     * Invalid filename characters.
     */
    private static final String INVALID_CHARACTERS =
            "[\\\\/:*?\"<>|]";

    /**
     * Timestamp pattern.
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private FileNameUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates UUID based filename.
     *
     * Example:
     * 550e8400-e29b.pdf
     */
    public static String unique(String originalFileName) {

        Objects.requireNonNull(originalFileName,
                "Original filename cannot be null.");

        String extension = extension(originalFileName);

        return UUID.randomUUID() +
                (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * Generates timestamp based filename.
     *
     * Example:
     * report_20260706153020.pdf
     */
    public static String timestamp(
            String prefix,
            String originalFileName) {

        Objects.requireNonNull(prefix);
        Objects.requireNonNull(originalFileName);

        String extension = extension(originalFileName);

        return sanitize(prefix)
                + "_"
                + LocalDateTime.now()
                .format(TIMESTAMP_FORMATTER)
                + (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * Sanitizes filename.
     */
    public static String sanitize(String fileName) {

        Objects.requireNonNull(fileName);

        String normalized = Normalizer.normalize(
                fileName,
                Normalizer.Form.NFKC
        );

        return normalized
                .replaceAll(INVALID_CHARACTERS, "_")
                .replaceAll("\\s+", "_")
                .trim();
    }

    /**
     * Returns extension.
     *
     * Example:
     * pdf
     */
    public static String extension(String fileName) {

        Objects.requireNonNull(fileName);

        int index = fileName.lastIndexOf('.');

        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(index + 1).toLowerCase();
    }

    /**
     * Removes extension.
     */
    public static String removeExtension(String fileName) {

        Objects.requireNonNull(fileName);

        int index = fileName.lastIndexOf('.');

        if (index < 0) {
            return fileName;
        }

        return fileName.substring(0, index);
    }

    /**
     * Checks whether file has extension.
     */
    public static boolean hasExtension(String fileName) {

        return !extension(fileName).isEmpty();
    }

    /**
     * Changes extension.
     */
    public static String changeExtension(
            String fileName,
            String newExtension) {

        Objects.requireNonNull(fileName);
        Objects.requireNonNull(newExtension);

        return removeExtension(fileName)
                + "."
                + newExtension.toLowerCase();
    }

    /**
     * Returns filename without path.
     */
    public static String fileName(String path) {

        Objects.requireNonNull(path);

        return path.replace("\\", "/")
                .substring(path.replace("\\", "/").lastIndexOf('/') + 1);
    }

}