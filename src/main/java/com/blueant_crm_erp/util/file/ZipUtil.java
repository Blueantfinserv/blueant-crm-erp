package com.blueant_crm_erp.util.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for ZIP file operations.
 *
 * Responsibilities:
 * - Create ZIP archive
 * - Extract ZIP archive
 * - Add files to ZIP
 * - Read ZIP entries
 * - Validate ZIP files
 *
 * This utility DOES NOT:
 * - Store ZIP files
 * - Upload ZIP files
 * - Perform business logic
 *
 * Used By:
 * - Client Module
 * - Lead Module
 * - Service Request Module
 * - Report Module
 * - Dashboard Module
 * - Document Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ZipUtil {

    private static final int BUFFER_SIZE = 8192;

    private ZipUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates ZIP archive from files.
     *
     * Key   -> Entry Name
     * Value -> File Path
     */
    public static byte[] zip(Map<String, Path> files)
            throws IOException {

        Objects.requireNonNull(files, "Files cannot be null.");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

            for (Map.Entry<String, Path> entry : files.entrySet()) {

                String entryName = entry.getKey();
                Path file = entry.getValue();

                Objects.requireNonNull(entryName);
                Objects.requireNonNull(file);

                zipOutputStream.putNextEntry(new ZipEntry(entryName));

                Files.copy(file, zipOutputStream);

                zipOutputStream.closeEntry();
            }

            zipOutputStream.finish();

            return outputStream.toByteArray();
        }
    }

    /**
     * Creates ZIP archive from byte arrays.
     *
     * Key   -> Entry Name
     * Value -> File Bytes
     */
    public static byte[] zipBytes(
            Map<String, byte[]> files)
            throws IOException {

        Objects.requireNonNull(files);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

            for (Map.Entry<String, byte[]> entry : files.entrySet()) {

                zipOutputStream.putNextEntry(
                        new ZipEntry(entry.getKey()));

                zipOutputStream.write(entry.getValue());

                zipOutputStream.closeEntry();
            }

            zipOutputStream.finish();

            return outputStream.toByteArray();
        }
    }

    /**
     * Extracts ZIP archive.
     *
     * Returns:
     * Key   -> Entry Name
     * Value -> File Bytes
     */
    public static Map<String, byte[]> unzip(
            InputStream inputStream)
            throws IOException {

        Objects.requireNonNull(inputStream);

        Map<String, byte[]> extractedFiles =
                new LinkedHashMap<>();

        try (ZipInputStream zipInputStream =
                     new ZipInputStream(inputStream)) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream();

                byte[] buffer = new byte[BUFFER_SIZE];

                int length;

                while ((length = zipInputStream.read(buffer)) > 0) {

                    outputStream.write(buffer, 0, length);
                }

                extractedFiles.put(
                        entry.getName(),
                        outputStream.toByteArray());

                zipInputStream.closeEntry();
            }
        }

        return extractedFiles;
    }

    /**
     * Returns true if file is ZIP.
     */
    public static boolean isZipFile(String fileName) {

        return fileName != null
                && fileName.toLowerCase().endsWith(".zip");
    }

    /**
     * Returns ZIP content type.
     */
    public static String contentType() {
        return "application/zip";
    }

}