package com.blueant_crm_erp.client.bulk.service.impl;

import com.blueant_crm_erp.client.bulk.dto.BulkClientImportJobStatusResponse;
import com.blueant_crm_erp.client.bulk.dto.BulkClientImportSubmitResponse;
import com.blueant_crm_erp.client.bulk.dto.ClientImportRowErrorDto;
import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import com.blueant_crm_erp.client.bulk.repository.ClientImportJobRepository;
import com.blueant_crm_erp.client.bulk.service.BulkClientImportService;
import com.blueant_crm_erp.document.service.StorageService;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkClientImportServiceImpl implements BulkClientImportService {

    private final ClientImportJobRepository jobRepository;
    private final ClientImportJobStatusService jobStatusService;
    private final BulkClientImportAsyncWorker asyncWorker;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB
    private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "VALIDATING", "EXECUTING");

    @Override
    public BulkClientImportSubmitResponse submitImport(MultipartFile file, String actorUsername) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BadRequestException("Uploaded file must have a valid filename.");
        }

        String lowerName = originalFilename.toLowerCase(Locale.ENGLISH);
        if (!lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls") && !lowerName.endsWith(".csv")) {
            throw new BadRequestException("Invalid file format. Only Excel (.xlsx, .xls) and CSV (.csv) files are supported.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed limit of 25MB.");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read uploaded file bytes", e);
            throw new BadRequestException("Failed to read uploaded file: " + e.getMessage());
        }

        String fileHash = calculateSha256(fileBytes);

        // Deduplication guard: Check if an import with the exact same file is currently processing
        Optional<ClientImportJob> inProgressJob = jobRepository
                .findTopByFileHashSha256AndStatusInOrderByCreatedAtDesc(fileHash, ACTIVE_STATUSES);

        if (inProgressJob.isPresent()) {
            throw new BadRequestException("An identical file is already being processed under job ID: "
                    + inProgressJob.get().getId() + " (Status: " + inProgressJob.get().getStatus() + ")");
        }

        // Persist file to durable storage BEFORE accepting and acknowledging the job
        String storageFileKey;
        try {
            storageFileKey = storageService.uploadFile(file);
        } catch (Exception e) {
            log.error("Failed to store uploaded bulk import file in durable storage", e);
            throw new BadRequestException("Failed to store uploaded file: " + e.getMessage());
        }

        String jobId = UUID.randomUUID().toString();
        jobStatusService.createInitialJob(jobId, originalFilename, fileHash, storageFileKey, actorUsername);

        log.info("Submitted bulk client import job {} with durable fileKey {} for user {}", jobId, storageFileKey, actorUsername);

        // Trigger async execution passing durable storage key
        asyncWorker.processImportAsync(jobId, storageFileKey, originalFilename, actorUsername);

        return BulkClientImportSubmitResponse.builder()
                .jobId(jobId)
                .status("PENDING")
                .fileName(originalFilename)
                .message("File accepted. Bulk client import job initiated asynchronously.")
                .build();
    }

    @Override
    public BulkClientImportJobStatusResponse getJobStatus(String jobId, String actorUsername, int page, int size) {
        ClientImportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Bulk import job not found with id: " + jobId));

        List<ClientImportRowErrorDto> allErrors = Collections.emptyList();
        if (job.getErrorDetailsJson() != null && !job.getErrorDetailsJson().isBlank()) {
            try {
                allErrors = objectMapper.readValue(job.getErrorDetailsJson(), new TypeReference<List<ClientImportRowErrorDto>>() {});
            } catch (Exception e) {
                log.error("Failed to parse errorDetailsJson for jobId: {}", jobId, e);
            }
        }

        int totalErrors = allErrors.size();
        List<ClientImportRowErrorDto> paginatedErrors = Collections.emptyList();

        if (totalErrors > 0) {
            int pageIndex = Math.max(0, page);
            int pageSize = size > 0 ? size : 50;
            int fromIndex = pageIndex * pageSize;
            if (fromIndex < totalErrors) {
                int toIndex = Math.min(fromIndex + pageSize, totalErrors);
                paginatedErrors = allErrors.subList(fromIndex, toIndex);
            }
        }

        return BulkClientImportJobStatusResponse.builder()
                .jobId(job.getId())
                .status(job.getStatus())
                .fileName(job.getFileName())
                .totalRows(job.getTotalRows())
                .processedRows(job.getProcessedRows())
                .startClientCode(job.getStartClientCode())
                .endClientCode(job.getEndClientCode())
                .errorMessage(job.getErrorMessage())
                .totalErrorsCount(totalErrors)
                .errors(paginatedErrors)
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }

    @Override
    public byte[] generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clients");

            // Header Style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Required Header Style (Light Coral/Red highlight for mandatory fields)
            Font reqHeaderFont = workbook.createFont();
            reqHeaderFont.setBold(true);
            reqHeaderFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle reqHeaderStyle = workbook.createCellStyle();
            reqHeaderStyle.setFont(reqHeaderFont);
            reqHeaderStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            reqHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            reqHeaderStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "Client Name *",
                    "Mobile Number *",
                    "Speciality *",
                    "Location *",
                    "Clinic Address *",
                    "Sales Person Employee Code *",
                    "Alternate Mobile Number",
                    "Email",
                    "PAN Number",
                    "Date of Birth (YYYY-MM-DD)",
                    "Actual Date of Birth (YYYY-MM-DD)",
                    "Occupation",
                    "Mother Name",
                    "Place of Birth",
                    "Residential Address",
                    "Correspondence Address",
                    "Office Address",
                    "City",
                    "State",
                    "PIN Code",
                    "Client Source",
                    "Source Description",
                    "Expected Max SIP",
                    "First Investment Amount",
                    "Investment Type",
                    "Investwell ID",
                    "Best Time to Meet",
                    "Assignment Date (YYYY-MM-DD)",
                    "Remarks"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(i < 6 ? reqHeaderStyle : headerStyle);
            }

            // Sample Row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("Dr. Rajesh Sharma");
            sampleRow.createCell(1).setCellValue("9876543210");
            sampleRow.createCell(2).setCellValue("Cardiology");
            sampleRow.createCell(3).setCellValue("Bandra West, Mumbai");
            sampleRow.createCell(4).setCellValue("Apollo Heart Clinic, Linking Road");
            sampleRow.createCell(5).setCellValue("EMP0012");
            sampleRow.createCell(6).setCellValue("9876543211");
            sampleRow.createCell(7).setCellValue("rajesh.sharma@example.com");
            sampleRow.createCell(8).setCellValue("ABCDE1234F");
            sampleRow.createCell(9).setCellValue("1980-05-15");
            sampleRow.createCell(10).setCellValue("1980-05-15");
            sampleRow.createCell(11).setCellValue("Doctor");
            sampleRow.createCell(12).setCellValue("Sunita Sharma");
            sampleRow.createCell(13).setCellValue("Mumbai");
            sampleRow.createCell(14).setCellValue("Flat 402, Sea Green Apts, Bandra");
            sampleRow.createCell(15).setCellValue("Flat 402, Sea Green Apts, Bandra");
            sampleRow.createCell(16).setCellValue("Apollo Heart Clinic, Linking Road");
            sampleRow.createCell(17).setCellValue("Mumbai");
            sampleRow.createCell(18).setCellValue("Maharashtra");
            sampleRow.createCell(19).setCellValue("400050");
            sampleRow.createCell(20).setCellValue("REFERRAL");
            sampleRow.createCell(21).setCellValue("Referred by existing doctor client");
            sampleRow.createCell(22).setCellValue("25000");
            sampleRow.createCell(23).setCellValue("100000");
            sampleRow.createCell(24).setCellValue("New SIP");
            sampleRow.createCell(25).setCellValue("IW100982");
            sampleRow.createCell(26).setCellValue("EVENING");
            sampleRow.createCell(27).setCellValue("2026-11-01");
            sampleRow.createCell(28).setCellValue("Interested in long-term mutual fund investment");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate Excel template", e);
            throw new RuntimeException("Failed to generate Excel template: " + e.getMessage(), e);
        }
    }

    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            return UUID.nameUUIDFromBytes(data).toString().replace("-", "");
        }
    }
}

