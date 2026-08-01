package com.blueant_crm_erp.util.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for Excel import and export.
 *
 * Responsibilities:
 * - Create workbook
 * - Read workbook
 * - Write rows
 * - Read rows
 * - Auto-size columns
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
 * This utility contains only generic Excel operations.
 * Business-specific mapping belongs to Service/Mapper layer.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ExcelUtil {

    private ExcelUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates Excel workbook.
     */
    public static Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

    /**
     * Opens existing workbook.
     */
    public static Workbook openWorkbook(
            InputStream inputStream) throws IOException {

        Objects.requireNonNull(inputStream);

        return WorkbookFactory.create(inputStream);
    }

    /**
     * Creates sheet.
     */
    public static Sheet createSheet(
            Workbook workbook,
            String sheetName) {

        Objects.requireNonNull(workbook);
        Objects.requireNonNull(sheetName);

        return workbook.createSheet(sheetName);
    }

    /**
     * Writes header row.
     */
    public static void writeHeader(
            Sheet sheet,
            List<String> headers) {

        Objects.requireNonNull(sheet);
        Objects.requireNonNull(headers);

        Row row = sheet.createRow(0);

        for (int i = 0; i < headers.size(); i++) {

            Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
        }
    }

    /**
     * Writes data rows.
     */
    public static void writeRows(
            Sheet sheet,
            List<List<?>> rows) {

        Objects.requireNonNull(sheet);
        Objects.requireNonNull(rows);

        int rowIndex = 1;

        for (List<?> rowData : rows) {

            Row row = sheet.createRow(rowIndex++);

            for (int i = 0; i < rowData.size(); i++) {

                Cell cell = row.createCell(i);

                Object value = rowData.get(i);

                if (value != null) {
                    cell.setCellValue(String.valueOf(value));
                }
            }
        }
    }

    /**
     * Reads all rows.
     */
    public static List<List<String>> readRows(
            Sheet sheet) {

        Objects.requireNonNull(sheet);

        List<List<String>> result = new ArrayList<>();

        for (Row row : sheet) {

            List<String> values = new ArrayList<>();

            for (Cell cell : row) {

                values.add(getCellValue(cell));
            }

            result.add(values);
        }

        return result;
    }

    /**
     * Returns cell value as String.
     */
    public static String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {

            case STRING ->
                    cell.getStringCellValue();

            case NUMERIC ->
                    String.valueOf(cell.getNumericCellValue());

            case BOOLEAN ->
                    String.valueOf(cell.getBooleanCellValue());

            case FORMULA ->
                    cell.getCellFormula();

            case BLANK ->
                    "";

            default ->
                    "";
        };
    }

    /**
     * Auto sizes all columns.
     */
    public static void autoSizeColumns(
            Sheet sheet,
            int totalColumns) {

        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Converts workbook into byte array.
     */
    public static byte[] toByteArray(
            Workbook workbook) throws IOException {

        Objects.requireNonNull(workbook);

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    /**
     * Closes workbook safely.
     */
    public static void close(
            Workbook workbook) throws IOException {

        if (workbook != null) {
            workbook.close();
        }
    }

}