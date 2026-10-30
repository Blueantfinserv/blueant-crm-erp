package com.blueant_crm_erp.client.bulk.service.impl;

import com.blueant_crm_erp.client.bulk.dto.ClientImportRowDto;
import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import com.blueant_crm_erp.client.bulk.repository.ClientImportJobRepository;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientAssignmentHistory;
import com.blueant_crm_erp.client.enums.ClientBestTimeToMeet;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientCodeGeneratorService;
import com.blueant_crm_erp.exception.client.ClientAlreadyExistsException;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.util.id.ClientCodeGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkClientPersistenceService {

    @PersistenceContext
    private final EntityManager entityManager;

    private final ClientCodeGeneratorService clientCodeGeneratorService;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientImportJobRepository jobRepository;

    private static final int BATCH_FLUSH_SIZE = 100;
    private static final int HEARTBEAT_INTERVAL = 500;
    public static final String BULK_IMPORT_REASON_PREFIX = "Bulk client import job: ";

    /**
     * Executes Phase 2 Atomic Persistence.
     * All Client and ClientAssignmentHistory inserts, plus the sequence block allocation,
     * execute in the EXACT SAME database transaction.
     * Any failure triggers an automatic MySQL rollback of all inserts AND the sequence counter.
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PersistenceResult persistAllClientsAtomically(
            String jobId,
            List<ClientImportRowDto> validRows,
            Map<String, User> salesPersonCache,
            User assignedByUser,
            String actorIdentifier,
            Consumer<Integer> progressCallback) {

        int totalCount = validRows.size();
        log.info("Starting atomic persistence for job: {}, total valid rows: {}", jobId, totalCount);

        // 1. Allocate contiguous client code block (acquires exclusive row lock on 'CLIENT_CODE' in lead_code_sequences)
        long startSeq = clientCodeGeneratorService.allocateContiguousClientCodeBlock(totalCount);
        long endSeq = startSeq + totalCount - 1;

        String startCode = ClientCodeGenerator.generate(startSeq);
        String endCode = ClientCodeGenerator.generate(endSeq);

        // 2. Concurrency re-check: Verify no mobile was committed in the interim while lock was waiting
        Set<String> allMobiles = new HashSet<>();
        for (ClientImportRowDto row : validRows) {
            allMobiles.add(row.getMobileNumber());
        }
        recheckMobilesInDatabase(allMobiles);

        LocalDateTime now = LocalDateTime.now();

        // 3. Insert loop with bounded 1st-level cache clearing
        for (int i = 0; i < totalCount; i++) {
            ClientImportRowDto row = validRows.get(i);
            long currentSeq = startSeq + i;
            String clientCode = ClientCodeGenerator.generate(currentSeq);

            User salesPerson = salesPersonCache.get(row.getSalesPersonEmployeeCode().toUpperCase(Locale.ENGLISH));
            if (salesPerson == null) {
                salesPerson = userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(row.getSalesPersonEmployeeCode().trim())
                        .orElseThrow(() -> new IllegalStateException("Sales person not found during insert: " + row.getSalesPersonEmployeeCode()));
            }

            // Using entity reference proxy prevents detached entity issues across clear()
            User salesPersonProxy = entityManager.getReference(User.class, salesPerson.getId());
            User assignedByProxy = assignedByUser != null ? entityManager.getReference(User.class, assignedByUser.getId()) : salesPersonProxy;

            LocalDate assignmentDate = row.getAssignmentDate() != null ? row.getAssignmentDate() : LocalDate.now();

            Client client = Client.builder()
                    .clientCode(clientCode)
                    .clientStatus(ClientStatus.ACTIVE)
                    .clientName(row.getClientName())
                    .mobileNumber(row.getMobileNumber())
                    .speciality(row.getSpeciality())
                    .location(row.getLocation())
                    .clinicAddress(row.getClinicAddress())
                    .salesPerson(salesPersonProxy)
                    .createdBySalesPerson(salesPersonProxy)
                    .assignedBy(assignedByProxy)
                    .assignedAt(now)
                    .assignmentDate(assignmentDate)
                    .clientSince(LocalDate.now())
                    .lead(null)
                    // Optional profile fields
                    .alternateMobileNumber(row.getAlternateMobileNumber())
                    .email(row.getEmail())
                    .remarks(row.getRemarks())
                    .bestTimeToMeet(row.getBestTimeToMeet() != null ? ClientBestTimeToMeet.normalizeToDisplayName(row.getBestTimeToMeet()) : null)
                    .panNumber(row.getPanNumber())
                    .dateOfBirth(row.getDateOfBirth())
                    .actualDateOfBirth(row.getActualDateOfBirth())
                    .motherName(row.getMotherName())
                    .placeOfBirth(row.getPlaceOfBirth())
                    .occupation(row.getOccupation())
                    .residentialAddress(row.getResidentialAddress())
                    .correspondenceAddress(row.getCorrespondenceAddress())
                    .officeAddress(row.getOfficeAddress())
                    .city(row.getCity())
                    .state(row.getState())
                    .pinCode(row.getPinCode())
                    .clientSource(row.getClientSource())
                    .sourceDescription(row.getSourceDescription())
                    .expectedMaxSIP(row.getExpectedMaxSIP())
                    .investwellId(row.getInvestwellId())
                    .investmentType(row.getInvestmentType())
                    .firstInvestmentAmount(row.getFirstInvestmentAmount())
                    .build();

            entityManager.persist(client);

            ClientAssignmentHistory history = ClientAssignmentHistory.builder()
                    .client(client)
                    .assignedFromUser(null)
                    .assignedToUser(salesPersonProxy)
                    .assignedByUser(assignedByProxy)
                    .assignmentDate(assignmentDate)
                    .assignedAt(now)
                    .reason(BULK_IMPORT_REASON_PREFIX + jobId)
                    .build();

            entityManager.persist(history);

            // Bounded memory: flush statements to socket and clear 1st-level cache every 100 rows
            if ((i + 1) % BATCH_FLUSH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }

            // Periodic progress and heartbeat update
            if ((i + 1) % HEARTBEAT_INTERVAL == 0 || (i + 1) == totalCount) {
                if (progressCallback != null) {
                    progressCallback.accept(i + 1);
                }
            }
        }

        // 4. Atomically persist job completion state in the EXACT SAME database transaction
        ClientImportJob job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            job.setStatus("COMPLETED");
            job.setProcessedRows(totalCount);
            job.setStartClientCode(startCode);
            job.setEndClientCode(endCode);
            job.setCompletedAt(now);
            job.setHeartbeatAt(now);
            jobRepository.save(job);
        }

        // Final flush and clear
        entityManager.flush();
        entityManager.clear();

        log.info("Atomic persistence completed successfully. Created {} clients: {} to {}",
                totalCount, startCode, endCode);

        return new PersistenceResult(totalCount, startCode, endCode);
    }

    private void recheckMobilesInDatabase(Set<String> mobiles) {
        List<String> list = new ArrayList<>(mobiles);
        int chunkSize = 1000;
        for (int i = 0; i < list.size(); i += chunkSize) {
            List<String> sub = list.subList(i, Math.min(i + chunkSize, list.size()));
            List<String> existing = entityManager.createQuery(
                    "SELECT c.mobileNumber FROM Client c WHERE c.mobileNumber IN :mobiles", String.class)
                    .setParameter("mobiles", sub)
                    .getResultList();

            if (!existing.isEmpty()) {
                throw new ClientAlreadyExistsException(
                        "One or more mobile numbers were registered concurrently during processing. Aborting atomic import.");
            }
        }
    }

    public record PersistenceResult(int totalCount, String startCode, String endCode) {}
}

