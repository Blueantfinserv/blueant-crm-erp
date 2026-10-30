package com.blueant_crm_erp.client.bulk.parser;

import com.blueant_crm_erp.client.bulk.dto.ClientImportRowDto;
import com.blueant_crm_erp.exception.common.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Component
public class ClientFileParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    public List<ClientImportRowDto> parseFile(InputStream inputStream, String filename) throws IOException {
        if (filename == null) {
            throw new BadRequestException("File name cannot be empty.");
        }
        String lower = filename.toLowerCase(Locale.ENGLISH);
        if (lower.endsWith(".csv")) {
            return parseCsv(inputStream);
        } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            return parseExcel(inputStream);
        } else {
            throw new BadRequestException("Unsupported file type. Only CSV, XLSX, and XLS are supported.");
        }
    }

    private List<ClientImportRowDto> parseCsv(InputStream inputStream) throws IOException {
        List<ClientImportRowDto> rows = new ArrayList<>();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (CSVParser parser = new CSVParser(new InputStreamReader(inputStream, StandardCharsets.UTF_8), format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            Map<String, String> normalizedHeaderAliases = buildHeaderAliasMap(headerMap.keySet());

            int rowNumber = 1;
            for (CSVRecord record : parser) {
                rowNumber++;
                if (isRecordEmpty(record)) {
                    continue;
                }
                ClientImportRowDto rowDto = mapRecordToDto(record, normalizedHeaderAliases, rowNumber);
                rows.add(rowDto);
            }
        }
        return rows;
    }

    private List<ClientImportRowDto> parseExcel(InputStream inputStream) throws IOException {
        List<ClientImportRowDto> rows = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter(Locale.ENGLISH);

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return rows;
            }

            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return rows;
            }

            Row headerRow = rowIterator.next();
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String val = dataFormatter.formatCellValue(cell);
                if (val != null && !val.isBlank()) {
                    columnIndexMap.put(val.trim(), cell.getColumnIndex());
                }
            }

            Map<String, String> aliasMap = buildHeaderAliasMap(columnIndexMap.keySet());

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int rowNumber = row.getRowNum() + 1;
                if (isRowEmpty(row, dataFormatter)) {
                    continue;
                }
                ClientImportRowDto dto = mapExcelRowToDto(row, aliasMap, columnIndexMap, dataFormatter, rowNumber);
                rows.add(dto);
            }
        }
        return rows;
    }

    private boolean isRecordEmpty(CSVRecord record) {
        for (String val : record) {
            if (val != null && !val.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            String val = formatter.formatCellValue(cell);
            if (val != null && !val.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> buildHeaderAliasMap(Set<String> actualHeaders) {
        Map<String, String> map = new HashMap<>();
        for (String raw : actualHeaders) {
            String canonical = normalizeHeaderName(raw);
            map.put(canonical, raw);
        }
        return map;
    }

    private String normalizeHeaderName(String header) {
        if (header == null) return "";
        return header.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ENGLISH);
    }

    private ClientImportRowDto mapRecordToDto(CSVRecord record, Map<String, String> aliasMap, int rowNumber) {
        return ClientImportRowDto.builder()
                .rowNumber(rowNumber)
                .clientName(sanitize(getRecordValue(record, aliasMap, "clientname", "name")))
                .mobileNumber(cleanPhoneNumber(getRecordValue(record, aliasMap, "mobilenumber", "mobile", "phone")))
                .speciality(sanitize(getRecordValue(record, aliasMap, "speciality", "specialization", "professionspeciality", "professionorspeciality")))
                .location(sanitize(getRecordValue(record, aliasMap, "location", "area")))
                .clinicAddress(sanitize(getRecordValue(record, aliasMap, "clinicaddress", "address")))
                .salesPersonEmployeeCode(sanitize(getRecordValue(record, aliasMap, "salespersonemployeecode", "salespersoncode", "employeecode", "rmcode", "sccode", "rmscemployeecode", "rmemployeecode", "scemployeecode")))
                .alternateMobileNumber(cleanPhoneNumber(getRecordValue(record, aliasMap, "alternatemobilenumber", "alternatemobile")))
                .email(sanitize(getRecordValue(record, aliasMap, "email", "emailaddress")))
                .remarks(sanitize(getRecordValue(record, aliasMap, "remarks", "remark", "otherimportantinformationremarks", "otherinformation")))
                .bestTimeToMeet(sanitize(getRecordValue(record, aliasMap, "besttimetomeet", "meetingtime")))
                .panNumber(cleanPan(getRecordValue(record, aliasMap, "pannumber", "pan")))
                .dateOfBirth(parseDate(getRecordValue(record, aliasMap, "dateofbirth", "dob")))
                .actualDateOfBirth(parseDate(getRecordValue(record, aliasMap, "actualdateofbirth", "actualdob")))
                .motherName(sanitize(getRecordValue(record, aliasMap, "mothername", "mother")))
                .placeOfBirth(sanitize(getRecordValue(record, aliasMap, "placeofbirth")))
                .occupation(sanitize(getRecordValue(record, aliasMap, "occupation", "profession")))
                .residentialAddress(sanitize(getRecordValue(record, aliasMap, "residentialaddress", "residenceaddress")))
                .correspondenceAddress(sanitize(getRecordValue(record, aliasMap, "correspondenceaddress")))
                .officeAddress(sanitize(getRecordValue(record, aliasMap, "officeaddress")))
                .city(sanitize(getRecordValue(record, aliasMap, "city")))
                .state(sanitize(getRecordValue(record, aliasMap, "state")))
                .pinCode(cleanPinCode(getRecordValue(record, aliasMap, "pincode", "pin", "postalcode")))
                .clientSource(sanitize(getRecordValue(record, aliasMap, "clientsource", "source")))
                .sourceDescription(sanitize(getRecordValue(record, aliasMap, "sourcedescription")))
                .expectedMaxSIP(parseBigDecimal(getRecordValue(record, aliasMap, "expectedmaxsip", "maxsip", "sip")))
                .investwellId(sanitize(getRecordValue(record, aliasMap, "investwellid")))
                .investmentType(sanitize(getRecordValue(record, aliasMap, "investmenttype")))
                .firstInvestmentAmount(parseBigDecimal(getRecordValue(record, aliasMap, "firstinvestmentamount", "firstinvestment")))
                .assignmentDate(parseDate(getRecordValue(record, aliasMap, "assignmentdate")))
                .build();
    }

    private String getRecordValue(CSVRecord record, Map<String, String> aliasMap, String... keys) {
        for (String k : keys) {
            String actual = aliasMap.get(k);
            if (actual != null && record.isSet(actual)) {
                String val = record.get(actual);
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            }
        }
        return null;
    }

    private ClientImportRowDto mapExcelRowToDto(
            Row row, Map<String, String> aliasMap, Map<String, Integer> colIndexMap,
            DataFormatter formatter, int rowNumber) {

        return ClientImportRowDto.builder()
                .rowNumber(rowNumber)
                .clientName(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "clientname", "name")))
                .mobileNumber(cleanPhoneNumber(getExcelString(row, aliasMap, colIndexMap, formatter, "mobilenumber", "mobile", "phone")))
                .speciality(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "speciality", "specialization", "professionspeciality", "professionorspeciality")))
                .location(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "location", "area")))
                .clinicAddress(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "clinicaddress", "address")))
                .salesPersonEmployeeCode(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "salespersonemployeecode", "salespersoncode", "employeecode", "rmcode", "sccode", "rmscemployeecode", "rmemployeecode", "scemployeecode")))
                .alternateMobileNumber(cleanPhoneNumber(getExcelString(row, aliasMap, colIndexMap, formatter, "alternatemobilenumber", "alternatemobile")))
                .email(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "email", "emailaddress")))
                .remarks(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "remarks", "remark", "otherimportantinformationremarks", "otherinformation")))
                .bestTimeToMeet(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "besttimetomeet", "meetingtime")))
                .panNumber(cleanPan(getExcelString(row, aliasMap, colIndexMap, formatter, "pannumber", "pan")))
                .dateOfBirth(getExcelDate(row, aliasMap, colIndexMap, formatter, "dateofbirth", "dob"))
                .actualDateOfBirth(getExcelDate(row, aliasMap, colIndexMap, formatter, "actualdateofbirth", "actualdob"))
                .motherName(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "mothername", "mother")))
                .placeOfBirth(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "placeofbirth")))
                .occupation(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "occupation", "profession")))
                .residentialAddress(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "residentialaddress", "residenceaddress")))
                .correspondenceAddress(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "correspondenceaddress")))
                .officeAddress(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "officeaddress")))
                .city(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "city")))
                .state(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "state")))
                .pinCode(cleanPinCode(getExcelString(row, aliasMap, colIndexMap, formatter, "pincode", "pin", "postalcode")))
                .clientSource(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "clientsource", "source")))
                .sourceDescription(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "sourcedescription")))
                .expectedMaxSIP(parseBigDecimal(getExcelString(row, aliasMap, colIndexMap, formatter, "expectedmaxsip", "maxsip", "sip")))
                .investwellId(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "investwellid")))
                .investmentType(sanitize(getExcelString(row, aliasMap, colIndexMap, formatter, "investmenttype")))
                .firstInvestmentAmount(parseBigDecimal(getExcelString(row, aliasMap, colIndexMap, formatter, "firstinvestmentamount", "firstinvestment")))
                .assignmentDate(getExcelDate(row, aliasMap, colIndexMap, formatter, "assignmentdate"))
                .build();
    }

    private String getExcelString(Row row, Map<String, String> aliasMap, Map<String, Integer> colIndexMap, DataFormatter formatter, String... keys) {
        Cell cell = findCell(row, aliasMap, colIndexMap, keys);
        if (cell == null) return null;
        String val = formatter.formatCellValue(cell);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : null;
    }

    private LocalDate getExcelDate(Row row, Map<String, String> aliasMap, Map<String, Integer> colIndexMap, DataFormatter formatter, String... keys) {
        Cell cell = findCell(row, aliasMap, colIndexMap, keys);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date d = cell.getDateCellValue();
            if (d != null) {
                return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
        String formatted = formatter.formatCellValue(cell);
        return parseDate(formatted);
    }

    private Cell findCell(Row row, Map<String, String> aliasMap, Map<String, Integer> colIndexMap, String... keys) {
        for (String k : keys) {
            String actualHeader = aliasMap.get(k);
            if (actualHeader != null && colIndexMap.containsKey(actualHeader)) {
                int col = colIndexMap.get(actualHeader);
                Cell c = row.getCell(col);
                if (c != null) return c;
            }
        }
        return null;
    }

    private String sanitize(String val) {
        if (val == null) return null;
        String trimmed = val.trim();
        if (trimmed.isEmpty()) return null;
        // Formula injection protection
        if (trimmed.startsWith("=") || trimmed.startsWith("+") || trimmed.startsWith("-") || trimmed.startsWith("@")) {
            trimmed = "'" + trimmed;
        }
        return trimmed;
    }

    private String cleanPhoneNumber(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        } else if (digits.length() == 11 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return digits.isEmpty() ? null : digits;
    }

    private String cleanPan(String raw) {
        if (raw == null) return null;
        String cleaned = raw.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ENGLISH);
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String cleanPinCode(String raw) {
        if (raw == null) return null;
        String cleaned = raw.replaceAll("[^0-9]", "");
        return cleaned.isEmpty() ? null : cleaned;
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            String cleaned = val.replaceAll("[^0-9.]", "");
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseDate(String val) {
        if (val == null || val.isBlank()) return null;
        String trimmed = val.trim();
        for (DateTimeFormatter dtf : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, dtf);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}

