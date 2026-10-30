package com.blueant_crm_erp.client.bulk.parser;

import com.blueant_crm_erp.client.bulk.dto.ClientImportRowDto;
import com.blueant_crm_erp.exception.common.BadRequestException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientFileParserTest {

    private ClientFileParser parser;

    @BeforeEach
    void setUp() {
        parser = new ClientFileParser();
    }

    @Test
    @DisplayName("Successfully parses valid CSV with mandatory and optional fields")
    void testParseValidCsv() throws IOException {
        String csvData = "Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code,Email,PAN Number,Date of Birth\n"
                + "Dr. Alice Smith,9876543210,Neurology,Delhi,Apollo Hospital,EMP1001,alice@example.com,ABCDE1234F,1985-06-20\n"
                + "Dr. Bob Jones,9876543211,Cardiology,Noida,Max Hospital,EMP1002,bob@example.com,XYZAB5678C,1990-12-10\n";

        List<ClientImportRowDto> rows = parser.parseFile(
                new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)),
                "clients.csv"
        );

        assertThat(rows).hasSize(2);

        ClientImportRowDto row1 = rows.get(0);
        assertThat(row1.getRowNumber()).isEqualTo(2);
        assertThat(row1.getClientName()).isEqualTo("Dr. Alice Smith");
        assertThat(row1.getMobileNumber()).isEqualTo("9876543210");
        assertThat(row1.getSpeciality()).isEqualTo("Neurology");
        assertThat(row1.getLocation()).isEqualTo("Delhi");
        assertThat(row1.getClinicAddress()).isEqualTo("Apollo Hospital");
        assertThat(row1.getSalesPersonEmployeeCode()).isEqualTo("EMP1001");
        assertThat(row1.getEmail()).isEqualTo("alice@example.com");
        assertThat(row1.getPanNumber()).isEqualTo("ABCDE1234F");
        assertThat(row1.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 6, 20));

        ClientImportRowDto row2 = rows.get(1);
        assertThat(row2.getRowNumber()).isEqualTo(3);
        assertThat(row2.getClientName()).isEqualTo("Dr. Bob Jones");
        assertThat(row2.getMobileNumber()).isEqualTo("9876543211");
        assertThat(row2.getPanNumber()).isEqualTo("XYZAB5678C");
    }

    @Test
    @DisplayName("Successfully parses valid XLSX file")
    void testParseValidXlsx() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clients");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Client Name *");
            header.createCell(1).setCellValue("Mobile Number *");
            header.createCell(2).setCellValue("Speciality *");
            header.createCell(3).setCellValue("Location *");
            header.createCell(4).setCellValue("Clinic Address *");
            header.createCell(5).setCellValue("Sales Person Employee Code *");
            header.createCell(6).setCellValue("Expected Max SIP");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Dr. Rajesh Sharma");
            dataRow.createCell(1).setCellValue("9876543210");
            dataRow.createCell(2).setCellValue("Orthopedics");
            dataRow.createCell(3).setCellValue("Mumbai");
            dataRow.createCell(4).setCellValue("City Clinic");
            dataRow.createCell(5).setCellValue("EMP009");
            dataRow.createCell(6).setCellValue("50000");

            workbook.write(out);
        }

        List<ClientImportRowDto> rows = parser.parseFile(
                new ByteArrayInputStream(out.toByteArray()),
                "clients.xlsx"
        );

        assertThat(rows).hasSize(1);
        ClientImportRowDto row = rows.get(0);
        assertThat(row.getClientName()).isEqualTo("Dr. Rajesh Sharma");
        assertThat(row.getMobileNumber()).isEqualTo("9876543210");
        assertThat(row.getSpeciality()).isEqualTo("Orthopedics");
        assertThat(row.getLocation()).isEqualTo("Mumbai");
        assertThat(row.getClinicAddress()).isEqualTo("City Clinic");
        assertThat(row.getSalesPersonEmployeeCode()).isEqualTo("EMP009");
        assertThat(row.getExpectedMaxSIP()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Sanitizes formula injection prefixes in cell values")
    void testSanitizeFormulaInjection() throws IOException {
        String csvData = "Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code\n"
                + "=cmd|' /C calc'!A0,9876543210,@Speciality,+Location,-ClinicAddress,EMP1001\n";

        List<ClientImportRowDto> rows = parser.parseFile(
                new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)),
                "clients.csv"
        );

        assertThat(rows).hasSize(1);
        ClientImportRowDto row = rows.get(0);
        assertThat(row.getClientName()).doesNotStartWith("=");
        assertThat(row.getSpeciality()).doesNotStartWith("@");
        assertThat(row.getLocation()).doesNotStartWith("+");
        assertThat(row.getClinicAddress()).doesNotStartWith("-");
    }

    @Test
    @DisplayName("Rejects unsupported file format")
    void testUnsupportedFormat() {
        byte[] dummy = "hello".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> parser.parseFile(new ByteArrayInputStream(dummy), "data.pdf"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    @DisplayName("Successfully parses CSV using new CRM header aliases")
    void testParseCsvWithNewCrmHeaderAliases() throws IOException {
        String csvData = "Client Name,Mobile Number,Profession/Speciality,Location,Clinic Address,RM/SC Employee Code,Other Important Information/Remarks\n"
                + "Dr. Vikram Seth,9876543299,Pediatrician,Pune,Ruby Hall Clinic,RM007,VIP physician client\n";

        List<ClientImportRowDto> rows = parser.parseFile(
                new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)),
                "clients.csv"
        );

        assertThat(rows).hasSize(1);
        ClientImportRowDto row = rows.get(0);
        assertThat(row.getClientName()).isEqualTo("Dr. Vikram Seth");
        assertThat(row.getMobileNumber()).isEqualTo("9876543299");
        assertThat(row.getSpeciality()).isEqualTo("Pediatrician");
        assertThat(row.getLocation()).isEqualTo("Pune");
        assertThat(row.getClinicAddress()).isEqualTo("Ruby Hall Clinic");
        assertThat(row.getSalesPersonEmployeeCode()).isEqualTo("RM007");
        assertThat(row.getRemarks()).isEqualTo("VIP physician client");
    }

    @Test
    @DisplayName("Successfully parses XLSX using new CRM header aliases")
    void testParseXlsxWithNewCrmHeaderAliases() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clients");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Client Name");
            header.createCell(1).setCellValue("Mobile Number");
            header.createCell(2).setCellValue("Profession/Speciality");
            header.createCell(3).setCellValue("Location");
            header.createCell(4).setCellValue("Clinic Address");
            header.createCell(5).setCellValue("RM/SC Employee Code");
            header.createCell(6).setCellValue("Other Important Information/Remarks");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Dr. Meera Nair");
            dataRow.createCell(1).setCellValue("9876543288");
            dataRow.createCell(2).setCellValue("Dermatologist");
            dataRow.createCell(3).setCellValue("Kochi");
            dataRow.createCell(4).setCellValue("Aster Medcity");
            dataRow.createCell(5).setCellValue("SC005");
            dataRow.createCell(6).setCellValue("Prefers morning meetings");

            workbook.write(out);
        }

        List<ClientImportRowDto> rows = parser.parseFile(
                new ByteArrayInputStream(out.toByteArray()),
                "clients.xlsx"
        );

        assertThat(rows).hasSize(1);
        ClientImportRowDto row = rows.get(0);
        assertThat(row.getClientName()).isEqualTo("Dr. Meera Nair");
        assertThat(row.getMobileNumber()).isEqualTo("9876543288");
        assertThat(row.getSpeciality()).isEqualTo("Dermatologist");
        assertThat(row.getLocation()).isEqualTo("Kochi");
        assertThat(row.getClinicAddress()).isEqualTo("Aster Medcity");
        assertThat(row.getSalesPersonEmployeeCode()).isEqualTo("SC005");
        assertThat(row.getRemarks()).isEqualTo("Prefers morning meetings");
    }
}

