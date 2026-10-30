package com.blueant_crm_erp.client.bulk.service.impl;

import com.blueant_crm_erp.client.bulk.dto.ClientImportRowDto;
import com.blueant_crm_erp.client.bulk.dto.ClientImportRowErrorDto;
import com.blueant_crm_erp.client.bulk.parser.ClientFileParser;
import com.blueant_crm_erp.client.enums.ClientBestTimeToMeet;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.document.service.StorageService;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import com.blueant_crm_erp.util.validation.EmailValidator;
import com.blueant_crm_erp.util.validation.MobileValidator;
import com.blueant_crm_erp.util.validation.PinCodeValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkClientImportAsyncWorker {

    private final BulkClientPersistenceService persistenceService;
    private final ClientImportJobStatusService jobStatusService;
    private final ClientFileParser clientFileParser;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final SalesRoleResolver salesRoleResolver;
    private final StorageService storageService;

    @PersistenceContext
    private final EntityManager entityManager;

    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
    private static final int MAX_ROWS_ALLOWED = 10000;
    private static final int DB_CHUNK_SIZE = 1000;

    @Async("taskExecutor")
    public void processImportAsync(String jobId, String storageFileKey, String originalFilename, String actorUsername) {
        log.info("Starting asynchronous bulk client import processing for jobId: {}, storageFileKey: {}", jobId, storageFileKey);

        // Atomic multi-worker claim check: Only one worker can claim a PENDING job
        boolean claimed = jobStatusService.claimJobForProcessing(jobId);
        if (!claimed) {
            log.warn("Bulk import job {} could not be claimed (already claimed or not in PENDING status). Skipping duplicate execution.", jobId);
            return;
        }

        byte[] fileBytes;
        try {
            fileBytes = storageService.downloadFile(storageFileKey);
        } catch (Exception e) {
            log.error("Failed to retrieve import file from durable storage for jobId: {}, key: {}", jobId, storageFileKey, e);
            jobStatusService.markFailedSystem(jobId, "Failed to retrieve import file from durable storage: " + e.getMessage());
            return;
        }

        processImportInternal(jobId, fileBytes, originalFilename, actorUsername);
    }

    @Async("taskExecutor")
    public void processImportAsync(String jobId, byte[] fileBytes, String originalFilename, String actorUsername) {
        log.info("Starting asynchronous bulk client import processing for jobId: {}, file: {}", jobId, originalFilename);

        // Atomic multi-worker claim check: Only one worker can claim a PENDING job
        boolean claimed = jobStatusService.claimJobForProcessing(jobId);
        if (!claimed) {
            log.warn("Bulk import job {} could not be claimed (already claimed or not in PENDING status). Skipping duplicate execution.", jobId);
            return;
        }

        processImportInternal(jobId, fileBytes, originalFilename, actorUsername);
    }

    public void processImportInternal(String jobId, byte[] fileBytes, String originalFilename, String actorUsername) {
        try {
            // 1. Parse File
            List<ClientImportRowDto> rows;
            try (InputStream in = new ByteArrayInputStream(fileBytes)) {
                rows = clientFileParser.parseFile(in, originalFilename);
            } catch (Exception e) {
                log.error("Failed to parse import file for jobId: {}", jobId, e);
                jobStatusService.markFailedSystem(jobId, "File parsing failed: " + e.getMessage());
                return;
            }

            if (rows == null || rows.isEmpty()) {
                jobStatusService.markFailedSystem(jobId, "Spreadsheet contains no data rows.");
                return;
            }

            if (rows.size() > MAX_ROWS_ALLOWED) {
                jobStatusService.markFailedSystem(jobId,
                        "Batch size exceeds maximum limit of " + MAX_ROWS_ALLOWED + " rows. Found: " + rows.size());
                return;
            }

            int totalRows = rows.size();
            jobStatusService.markValidating(jobId, totalRows);

            // 2. Phase 1: Validation
            List<ClientImportRowErrorDto> errors = new ArrayList<>();
            Map<String, List<Integer>> mobileToRowNumbers = new LinkedHashMap<>();
            Set<String> employeeCodes = new LinkedHashSet<>();

            for (ClientImportRowDto row : rows) {
                int rowNum = row.getRowNumber();
                if (rowNum % 500 == 0) {
                    jobStatusService.updateHeartbeat(jobId, rowNum);
                }

                // Mandatory: clientName
                if (row.getClientName() == null || row.getClientName().isBlank()) {
                    errors.add(error(rowNum, "clientName", "MANDATORY_FIELD_MISSING", "Client name is mandatory."));
                }

                // Mandatory: mobileNumber
                if (row.getMobileNumber() == null || row.getMobileNumber().isBlank()) {
                    errors.add(error(rowNum, "mobileNumber", "MANDATORY_FIELD_MISSING", "Mobile number is mandatory."));
                } else {
                    String normalizedMobile = MobileValidator.normalize(row.getMobileNumber());
                    if (!MobileValidator.isValid(normalizedMobile) || !MobileValidator.hasValidLength(normalizedMobile)) {
                        errors.add(error(rowNum, "mobileNumber", "INVALID_MOBILE_FORMAT",
                                "Invalid mobile number. Must be a 10-digit Indian mobile number."));
                    } else {
                        row.setMobileNumber(normalizedMobile);
                        mobileToRowNumbers.computeIfAbsent(normalizedMobile, k -> new ArrayList<>()).add(rowNum);
                    }
                }

                // Mandatory: speciality
                if (row.getSpeciality() == null || row.getSpeciality().isBlank()) {
                    errors.add(error(rowNum, "speciality", "MANDATORY_FIELD_MISSING", "Speciality is mandatory."));
                }

                // Mandatory: location
                if (row.getLocation() == null || row.getLocation().isBlank()) {
                    errors.add(error(rowNum, "location", "MANDATORY_FIELD_MISSING", "Location is mandatory."));
                }

                // Mandatory: clinicAddress
                if (row.getClinicAddress() == null || row.getClinicAddress().isBlank()) {
                    errors.add(error(rowNum, "clinicAddress", "MANDATORY_FIELD_MISSING", "Clinic address is mandatory."));
                }

                // Mandatory: salesPersonEmployeeCode
                if (row.getSalesPersonEmployeeCode() == null || row.getSalesPersonEmployeeCode().isBlank()) {
                    errors.add(error(rowNum, "salesPersonEmployeeCode", "MANDATORY_FIELD_MISSING", "Sales person employee code is mandatory."));
                } else {
                    employeeCodes.add(row.getSalesPersonEmployeeCode().trim().toUpperCase(Locale.ENGLISH));
                }

                // Optional: alternateMobileNumber
                if (row.getAlternateMobileNumber() != null && !row.getAlternateMobileNumber().isBlank()) {
                    String normAlt = MobileValidator.normalize(row.getAlternateMobileNumber());
                    if (!MobileValidator.isValid(normAlt) || !MobileValidator.hasValidLength(normAlt)) {
                        errors.add(error(rowNum, "alternateMobileNumber", "INVALID_MOBILE_FORMAT",
                                "Invalid alternate mobile number. Must be a 10-digit Indian mobile number."));
                    } else if (normAlt.equals(row.getMobileNumber())) {
                        errors.add(error(rowNum, "alternateMobileNumber", "SAME_AS_PRIMARY_MOBILE",
                                "Alternate mobile number cannot be identical to primary mobile number."));
                    } else {
                        row.setAlternateMobileNumber(normAlt);
                    }
                }

                // Optional: email
                if (row.getEmail() != null && !row.getEmail().isBlank()) {
                    if (!EmailValidator.isValid(row.getEmail())) {
                        errors.add(error(rowNum, "email", "INVALID_EMAIL_FORMAT", "Invalid email address format."));
                    } else {
                        row.setEmail(EmailValidator.normalize(row.getEmail()));
                    }
                }

                // Optional: panNumber
                if (row.getPanNumber() != null && !row.getPanNumber().isBlank()) {
                    String panUpper = row.getPanNumber().trim().toUpperCase(Locale.ENGLISH);
                    if (!PAN_PATTERN.matcher(panUpper).matches()) {
                        errors.add(error(rowNum, "panNumber", "INVALID_PAN_FORMAT",
                                "Invalid PAN format. Expected format: ABCDE1234F."));
                    } else {
                        row.setPanNumber(panUpper);
                    }
                }

                // Optional: pinCode
                if (row.getPinCode() != null && !row.getPinCode().isBlank()) {
                    if (!PinCodeValidator.isValid(row.getPinCode())) {
                        errors.add(error(rowNum, "pinCode", "INVALID_PIN_CODE",
                                "Invalid PIN code. Expected 6-digit postal code."));
                    } else {
                        row.setPinCode(PinCodeValidator.normalize(row.getPinCode()));
                    }
                }

                // Optional: dateOfBirth
                if (row.getDateOfBirth() != null && !row.getDateOfBirth().isBefore(LocalDate.now())) {
                    errors.add(error(rowNum, "dateOfBirth", "DATE_MUST_BE_IN_PAST", "Date of birth must be in the past."));
                }

                // Optional: actualDateOfBirth
                if (row.getActualDateOfBirth() != null && !row.getActualDateOfBirth().isBefore(LocalDate.now())) {
                    errors.add(error(rowNum, "actualDateOfBirth", "DATE_MUST_BE_IN_PAST", "Actual date of birth must be in the past."));
                }

                // Optional: assignmentDate
                if (row.getAssignmentDate() != null && row.getAssignmentDate().isBefore(LocalDate.now())) {
                    errors.add(error(rowNum, "assignmentDate", "INVALID_ASSIGNMENT_DATE",
                            "Assignment date cannot be in the past. It must be today or a future date."));
                }

                // Optional: bestTimeToMeet
                if (row.getBestTimeToMeet() != null && !row.getBestTimeToMeet().isBlank()) {
                    if (!ClientBestTimeToMeet.isValid(row.getBestTimeToMeet().trim())) {
                        errors.add(error(rowNum, "bestTimeToMeet", "INVALID_BEST_TIME_TO_MEET",
                                "Invalid best time to meet: '" + row.getBestTimeToMeet() + "'. Allowed values: "
                                        + ClientBestTimeToMeet.SUPPORTED_DISPLAY_NAMES));
                    }
                }

                // Optional: expectedMaxSIP
                if (row.getExpectedMaxSIP() != null && row.getExpectedMaxSIP().compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(error(rowNum, "expectedMaxSIP", "NEGATIVE_AMOUNT", "Expected max SIP must be non-negative."));
                }

                // Optional: firstInvestmentAmount
                if (row.getFirstInvestmentAmount() != null && row.getFirstInvestmentAmount().compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(error(rowNum, "firstInvestmentAmount", "NEGATIVE_AMOUNT", "First investment amount must be non-negative."));
                }
            }

            // 3. In-File Duplicate Mobile Check
            for (Map.Entry<String, List<Integer>> entry : mobileToRowNumbers.entrySet()) {
                if (entry.getValue().size() > 1) {
                    for (Integer rowNum : entry.getValue()) {
                        errors.add(error(rowNum, "mobileNumber", "DUPLICATE_MOBILE_IN_FILE",
                                "Mobile number appears multiple times in file (rows: " + entry.getValue() + ")."));
                    }
                }
            }

            // 4. Database Duplicate Mobile Check
            List<String> uniqueMobiles = new ArrayList<>(mobileToRowNumbers.keySet());
            for (int i = 0; i < uniqueMobiles.size(); i += DB_CHUNK_SIZE) {
                jobStatusService.updateHeartbeat(jobId, i);
                List<String> chunk = uniqueMobiles.subList(i, Math.min(i + DB_CHUNK_SIZE, uniqueMobiles.size()));
                List<String> existingMobiles = entityManager.createQuery(
                        "SELECT c.mobileNumber FROM Client c WHERE c.mobileNumber IN :mobiles", String.class)
                        .setParameter("mobiles", chunk)
                        .getResultList();

                for (String existing : existingMobiles) {
                    List<Integer> affectedRows = mobileToRowNumbers.get(existing);
                    if (affectedRows != null) {
                        for (Integer rowNum : affectedRows) {
                            errors.add(error(rowNum, "mobileNumber", "DUPLICATE_MOBILE_IN_DATABASE",
                                    "Client with mobile number already exists in database."));
                        }
                    }
                }
            }

            // 5. Employee Code Lookup & Active RM/SC Eligibility Check
            jobStatusService.updateHeartbeat(jobId, totalRows);
            Map<String, User> salesPersonCache = new HashMap<>();
            for (String code : employeeCodes) {
                Optional<User> userOpt = userRepository.findProfileByEmployeeCodeIgnoreCaseAndDeletedFalse(code);
                if (userOpt.isEmpty()) {
                    addEmployeeError(rows, errors, code, "SALES_PERSON_NOT_FOUND",
                            "Sales person not found with employee code: " + code);
                } else {
                    User employee = userOpt.get();
                    if (!employee.isActive()) {
                        addEmployeeError(rows, errors, code, "SALES_PERSON_INACTIVE",
                                "Sales person account is inactive or disabled: " + code);
                    } else {
                        SalesRole role = salesRoleResolver.resolve(employee);
                        if (role != SalesRole.RM && role != SalesRole.SC) {
                            addEmployeeError(rows, errors, code, "SALES_PERSON_NOT_ELIGIBLE",
                                    "Employee with code " + code + " has role " + role
                                            + ". Clients can only be assigned to active RM or SC employees.");
                        } else {
                            salesPersonCache.put(code, employee);
                        }
                    }
                }
            }

            // 6. Strict All-or-Nothing Gate
            if (!errors.isEmpty()) {
                errors.sort(Comparator.comparingInt(ClientImportRowErrorDto::getRowNumber));
                log.warn("Bulk import validation failed with {} errors for jobId: {}", errors.size(), jobId);
                jobStatusService.markFailed(jobId, totalRows,
                        "Validation failed with " + errors.size() + " error(s) across rows.", errors);
                return;
            }

            // 7. Phase 2: Atomic Persistence
            jobStatusService.markExecuting(jobId);
            User assignedByUser = resolveUser(actorUsername);

            try {
                BulkClientPersistenceService.PersistenceResult result = persistenceService.persistAllClientsAtomically(
                        jobId,
                        rows,
                        salesPersonCache,
                        assignedByUser,
                        actorUsername,
                        processedCount -> jobStatusService.updateHeartbeat(jobId, processedCount)
                );

                jobStatusService.markCompleted(jobId, totalRows, result.startCode(), result.endCode());
                log.info("Bulk client import job {} COMPLETED successfully. Created {} clients ({} to {})",
                        jobId, result.totalCount(), result.startCode(), result.endCode());

            } catch (Exception e) {
                log.error("Bulk client import persistence transaction failed for jobId: {}", jobId, e);
                jobStatusService.markFailed(jobId, totalRows,
                        "Persistence transaction rolled back: " + e.getMessage(),
                        List.of(error(0, "database", "TRANSACTION_ROLLED_BACK", e.getMessage()))
                );
            }

        } catch (Throwable t) {
            log.error("Unhandled top-level error during bulk import processing for jobId: {}", jobId, t);
            jobStatusService.markFailedSystem(jobId, "Unexpected system error: " + t.getMessage());
        }
    }

    private User resolveUser(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        return userRepository.findProfileByEmployeeCodeIgnoreCaseAndDeletedFalse(identifier.trim())
                .orElseGet(() -> userRepository.findByEmailIgnoreCaseAndDeletedFalse(identifier.trim())
                        .orElse(null));
    }

    private void addEmployeeError(List<ClientImportRowDto> rows, List<ClientImportRowErrorDto> errors,
                                  String code, String errorCode, String errorMessage) {
        for (ClientImportRowDto r : rows) {
            if (code.equalsIgnoreCase(r.getSalesPersonEmployeeCode() != null ? r.getSalesPersonEmployeeCode().trim() : null)) {
                errors.add(error(r.getRowNumber(), "salesPersonEmployeeCode", errorCode, errorMessage));
            }
        }
    }

    private ClientImportRowErrorDto error(int rowNumber, String fieldName, String errorCode, String errorMessage) {
        return ClientImportRowErrorDto.builder()
                .rowNumber(rowNumber)
                .fieldName(fieldName)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
