package com.blueant_crm_erp.util.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Generic File Utility.
 *
 * Responsibilities:
 * - File metadata
 * - File extension
 * - File size
 * - MIME type
 * - Read bytes
 * - Temporary file creation
 * - Safe deletion
 *
 * This class DOES NOT:
 * - Upload files
 * - Store files
 * - Generate PDFs
 * - Read Excel
 * - Read CSV
 *
 * Used By:
 * - Client Module
 * - Lead Module
 * - Meeting Module
 * - Service Request Module
 * - Report Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class FileUtil {

    private static final long KB = 1024L;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if MultipartFile is null or empty.
     */
    public static boolean isEmpty(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    /**
     * Returns original filename.
     */
    public static String originalFileName(MultipartFile file) {

        Objects.requireNonNull(file, "MultipartFile cannot be null.");

        return file.getOriginalFilename();
    }

    /**
     * Returns content type.
     */
    public static String contentType(MultipartFile file) {

        Objects.requireNonNull(file, "MultipartFile cannot be null.");

        return file.getContentType();
    }

    /**
     * Returns file size.
     */
    public static long size(MultipartFile file) {

        Objects.requireNonNull(file, "MultipartFile cannot be null.");

        return file.getSize();
    }

    /**
     * Reads file bytes.
     */
    public static byte[] bytes(MultipartFile file)
            throws IOException {

        Objects.requireNonNull(file, "MultipartFile cannot be null.");

        return file.getBytes();
    }

    /**
     * Returns extension.
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
     * Returns extension from MultipartFile.
     */
    public static String extension(MultipartFile file) {

        return extension(originalFileName(file));
    }

    /**
     * Returns filename without extension.
     */
    public static String fileNameWithoutExtension(String fileName) {

        Objects.requireNonNull(fileName);

        int index = fileName.lastIndexOf('.');

        if (index < 0) {
            return fileName;
        }

        return fileName.substring(0, index);
    }

    /**
     * Creates temporary file.
     */
    public static Path createTempFile(
            String prefix,
            String suffix)
            throws IOException {

        return Files.createTempFile(prefix, suffix);
    }

    /**
     * Deletes file safely.
     */
    public static boolean delete(Path path)
            throws IOException {

        Objects.requireNonNull(path);

        return Files.deleteIfExists(path);
    }

    /**
     * Reads all bytes.
     */
    public static byte[] read(Path path)
            throws IOException {

        Objects.requireNonNull(path);

        return Files.readAllBytes(path);
    }

    /**
     * Returns human readable file size.
     */
    public static String humanReadableSize(long bytes) {

        if (bytes < KB) {
            return bytes + " B";
        }

        if (bytes < MB) {
            return String.format("%.2f KB", bytes / (double) KB);
        }

        if (bytes < GB) {
            return String.format("%.2f MB", bytes / (double) MB);
        }

        return String.format("%.2f GB", bytes / (double) GB);
    }

}