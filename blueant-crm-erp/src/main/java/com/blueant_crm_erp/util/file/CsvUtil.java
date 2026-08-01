package com.blueant_crm_erp.util.file;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for CSV import and export operations.
 *
 * Responsibilities:
 * - Export CSV
 * - Import CSV
 * - Validate CSV headers
 * - Standardized CSV configuration
 *
 * Used By:
 * - User Module
 * - Role Module
 * - Lead Module
 * - Client Module
 * - Meeting Module
 * - Service Request Module
 * - Transaction Module
 * - Dashboard Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class CsvUtil {

    /**
     * Standard CSV format used across the application.
     */
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT
            .builder()
            .setIgnoreEmptyLines(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .setSkipHeaderRecord(true)
            .build();

    private CsvUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Writes data into CSV format.
     *
     * @param headers CSV headers
     * @param rows    CSV rows
     * @return CSV content
     */
    public static String write(
            List<String> headers,
            List<List<?>> rows) throws IOException {

        Objects.requireNonNull(headers, "Headers cannot be null.");
        Objects.requireNonNull(rows, "Rows cannot be null.");

        StringWriter writer = new StringWriter();

        CSVFormat exportFormat = CSVFormat.DEFAULT
                .builder()
                .setHeader(headers.toArray(String[]::new))
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, exportFormat)) {

            for (List<?> row : rows) {
                printer.printRecord(row);
            }

            printer.flush();
        }

        return writer.toString();
    }

    /**
     * Reads CSV records.
     *
     * @param reader Reader
     * @return List of CSV records
     */
    public static List<CSVRecord> read(
            Reader reader) throws IOException {

        Objects.requireNonNull(reader, "Reader cannot be null.");

        try (BufferedReader bufferedReader = new BufferedReader(reader);
             CSVParser parser = CSV_FORMAT.parse(bufferedReader)) {

            return parser.getRecords();
        }
    }

    /**
     * Validates CSV headers.
     *
     * @param parser Parser
     * @param expectedHeaders Expected header list
     * @return true if headers match
     */
    public static boolean validateHeader(
            CSVParser parser,
            List<String> expectedHeaders) {

        Objects.requireNonNull(parser, "Parser cannot be null.");
        Objects.requireNonNull(expectedHeaders, "Expected headers cannot be null.");

        return parser.getHeaderNames().equals(expectedHeaders);
    }

    /**
     * Returns true if CSV contains records.
     *
     * @param records CSV records
     * @return true if not empty
     */
    public static boolean hasData(List<CSVRecord> records) {

        return records != null && !records.isEmpty();
    }

    /**
     * Returns total number of records.
     *
     * @param records CSV records
     * @return record count
     */
    public static int recordCount(List<CSVRecord> records) {

        return records == null
                ? 0
                : records.size();
    }

    /**
     * Returns mutable copy of records.
     *
     * @param records CSV records
     * @return copied list
     */
    public static List<CSVRecord> copy(List<CSVRecord> records) {

        return records == null
                ? new ArrayList<>()
                : new ArrayList<>(records);
    }

}