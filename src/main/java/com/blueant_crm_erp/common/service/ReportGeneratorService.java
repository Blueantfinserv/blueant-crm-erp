package com.blueant_crm_erp.common.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ReportGeneratorService {

    public byte[] generateExcelReport(List<Map<String, Object>> data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Report");
            if (data != null && !data.isEmpty()) {
                // Header
                Row headerRow = sheet.createRow(0);
                Map<String, Object> firstRow = data.get(0);
                int colIdx = 0;
                for (String key : firstRow.keySet()) {
                    headerRow.createCell(colIdx++).setCellValue(key);
                }
                
                // Data
                int rowIdx = 1;
                for (Map<String, Object> rowMap : data) {
                    Row row = sheet.createRow(rowIdx++);
                    colIdx = 0;
                    for (Object value : rowMap.values()) {
                        row.createCell(colIdx++).setCellValue(value != null ? value.toString() : "");
                    }
                }
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    public byte[] generatePdfReport(String title, String content) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph(title));
            document.add(new Paragraph(content));
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}
