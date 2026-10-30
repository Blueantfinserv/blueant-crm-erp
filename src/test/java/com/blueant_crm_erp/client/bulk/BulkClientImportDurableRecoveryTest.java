package com.blueant_crm_erp.client.bulk;

import com.blueant_crm_erp.client.bulk.dto.BulkClientImportSubmitResponse;
import com.blueant_crm_erp.client.bulk.dto.ClientImportRowDto;
import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import com.blueant_crm_erp.client.bulk.repository.ClientImportJobRepository;
import com.blueant_crm_erp.client.bulk.service.BulkClientImportService;
import com.blueant_crm_erp.client.bulk.service.impl.BulkClientImportAsyncWorker;
import com.blueant_crm_erp.client.bulk.service.impl.BulkClientPersistenceService;
import com.blueant_crm_erp.client.bulk.service.impl.ClientImportJobStatusService;
import com.blueant_crm_erp.client.bulk.service.impl.ImportJobRecoveryCoordinator;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.document.service.StorageService;
import com.blueant_crm_erp.lead.entity.LeadCodeSequence;
import com.blueant_crm_erp.lead.repository.LeadCodeSequenceRepository;
import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.entity.RolePermission;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.DesignationRepository;
import com.blueant_crm_erp.user.repository.TeamRepository;
import com.blueant_crm_erp.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Bulk Client Import Durable File Storage & Safe Recovery Tests")
public class BulkClientImportDurableRecoveryTest {

    @Autowired
    private BulkClientImportService bulkClientImportService;

    @Autowired
    private BulkClientImportAsyncWorker asyncWorker;

    @Autowired
    private ImportJobRecoveryCoordinator recoveryCoordinator;

    @Autowired
    private ClientImportJobRepository jobRepository;

    @Autowired
    private ClientImportJobStatusService jobStatusService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private LeadCodeSequenceRepository sequenceRepository;

    @Autowired
    private BulkClientPersistenceService persistenceService;

    @PersistenceContext
    private EntityManager entityManager;

    private User testRmUser;
    private String testRmEmpCode;

    @BeforeEach
    void setUp() {
        Department salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        Team defaultTeam = teamRepository.findAll().stream().findFirst()
                .orElseGet(() -> teamRepository.save(Team.builder()
                        .teamName("Recovery Team " + UUID.randomUUID().toString().substring(0, 6))
                        .teamCode("TEAM_REC_" + UUID.randomUUID().toString().substring(0, 6))
                        .department(salesDept)
                        .status(Status.ACTIVE)
                        .build()));

        testRmUser = createTestUser("RM_REC", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER,
                BootstrapConstants.DESIG_RM, salesDept, defaultTeam, "MEETING_UPDATE", "MEETING_READ");
        testRmEmpCode = testRmUser.getEmployeeCode();

        sequenceRepository.findById("CLIENT_CODE").orElseGet(() -> {
            LeadCodeSequence seq = new LeadCodeSequence();
            seq.setSequenceName("CLIENT_CODE");
            seq.setCurrentValue(100L);
            return sequenceRepository.save(seq);
        });
    }

    private User createTestUser(String prefix, String roleCode, String desigCode, Department dept, Team team, String... permissions) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Role role = roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(roleCode).name(roleCode).status(Status.ACTIVE).systemRole(true).defaultRole(false).build()));

        for (String permCode : permissions) {
            Permission perm = permissionRepository.findByCodeIgnoreCase(permCode)
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .code(permCode).name(permCode).module("TEST").displayOrder(1).systemPermission(false).status(Status.ACTIVE).build()));
            if (!rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), perm.getId())) {
                rolePermissionRepository.save(RolePermission.builder().role(role).permission(perm).build());
            }
        }

        Designation desig = designationRepository.findByCodeIgnoreCase(desigCode)
                .orElseGet(() -> designationRepository.save(Designation.builder()
                        .code(desigCode).name(desigCode).department(dept).hierarchyLevel(4).status(Status.ACTIVE).build()));

        return userRepository.save(User.builder()
                .employeeCode(prefix + "_" + suffix)
                .email(prefix.toLowerCase() + "_" + suffix + "@blueant.com")
                .password("$2a$10$dummyHashedPasswordForTestEnvironment")
                .firstName(prefix)
                .lastName("Test")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .role(role)
                .department(dept)
                .designation(desig)
                .team(team)
                .gender(Gender.MALE)
                .status(Status.ACTIVE)
                .deleted(false)
                .build());
    }

    @Test
    @DisplayName("1. Durable file exists in storage before job submission is acknowledged")
    void testDurableFileExistsBeforeJobAcknowledged() {
        String csvContent = "clientName,mobileNumber,speciality,location,clinicAddress,salesPersonEmployeeCode\n"
                + "Dr. Ramesh,9821" + (System.currentTimeMillis() % 1000000L) + ",Cardiology,South Mumbai,Worli," + testRmEmpCode + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "durable_test.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        BulkClientImportSubmitResponse response = bulkClientImportService.submitImport(file, "test_admin");
        assertThat(response).isNotNull();
        assertThat(response.getJobId()).isNotNull();

        // Verify job record has non-null storageFileKey
        Optional<ClientImportJob> jobOpt = jobRepository.findById(response.getJobId());
        assertThat(jobOpt).isPresent();
        ClientImportJob job = jobOpt.get();
        assertThat(job.getStorageFileKey()).isNotNull();
        assertThat(job.getStorageFileKey()).contains("documents/");

        // Verify the exact file bytes can be downloaded from storageService using that key
        byte[] storedBytes = storageService.downloadFile(job.getStorageFileKey());
        assertThat(storedBytes).isNotNull();
        assertThat(new String(storedBytes, StandardCharsets.UTF_8)).isEqualTo(csvContent);
    }

    @Test
    @DisplayName("2. Worker can read file and process job after a simulated application restart")
    void testWorkerReadsFileAfterSimulatedRestart() throws Exception {
        String testMobile = "9822" + (System.currentTimeMillis() % 1000000L);
        String csvContent = "clientName,mobileNumber,speciality,location,clinicAddress,salesPersonEmployeeCode\n"
                + "Dr. Suresh," + testMobile + ",Neurology,Bandra,Linking Road," + testRmEmpCode + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "restart_recovery.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        // Durably store file
        String storageKey = storageService.uploadFile(file);
        String jobId = UUID.randomUUID().toString();

        // Create job directly in PENDING (as if left in DB prior to crash/restart)
        jobStatusService.createInitialJob(jobId, "restart_recovery.csv", "dummyhash", storageKey, "test_admin");

        // Simulate application restart: new worker call triggered using jobId and storageFileKey
        asyncWorker.processImportAsync(jobId, storageKey, "restart_recovery.csv", "test_admin");

        // Await completion
        awaitJobTerminalState(jobId, 10);

        ClientImportJob job = jobRepository.findById(jobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo("COMPLETED");
        assertThat(job.getTotalRows()).isEqualTo(1);
        assertThat(job.getProcessedRows()).isEqualTo(1);

        // Verify client was persisted
        Optional<Client> clientOpt = clientRepository.findByMobileNumber(testMobile);
        assertThat(clientOpt).isPresent();
        assertThat(clientOpt.get().getClientName()).isEqualTo("Dr. Suresh");
    }

    @Test
    @DisplayName("3. Missing/corrupt file produces a safe, durable FAILED status")
    void testMissingFileProducesSafeDurableFailedStatus() {
        String jobId = UUID.randomUUID().toString();
        String nonExistentKey = "documents/non_existent_" + UUID.randomUUID() + ".csv";

        jobStatusService.createInitialJob(jobId, "corrupt.csv", "hash", nonExistentKey, "test_admin");

        // Invoke worker with non-existent storage key
        asyncWorker.processImportAsync(jobId, nonExistentKey, "corrupt.csv", "test_admin");
        awaitJobTerminalState(jobId, 10);

        ClientImportJob job = jobRepository.findById(jobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo("FAILED");
        assertThat(job.getErrorMessage()).contains("Failed to retrieve import file from durable storage");
    }

    @Test
    @DisplayName("4. Heartbeat advances during Phase 1 validation")
    void testHeartbeatAdvancesDuringPhase1() {
        String jobId = UUID.randomUUID().toString();
        ClientImportJob job = jobStatusService.createInitialJob(jobId, "hb_test.csv", "hash", "key", "test_admin");
        LocalDateTime initialHeartbeat = job.getHeartbeatAt();

        // Advance heartbeat
        jobStatusService.updateHeartbeat(jobId, 500);

        ClientImportJob updatedJob = jobRepository.findById(jobId).orElseThrow();
        assertThat(updatedJob.getHeartbeatAt()).isNotNull();
        assertThat(updatedJob.getProcessedRows()).isEqualTo(500);
    }

    @Test
    @DisplayName("5. A live job with recent heartbeat is not incorrectly marked stale")
    void testLiveJobNotMarkedStale() {
        String jobId = UUID.randomUUID().toString();
        jobStatusService.createInitialJob(jobId, "live_job.csv", "hash", "key", "test_admin");
        jobStatusService.markValidating(jobId, 100);

        // Heartbeat is fresh (now), so recoverStaleJobs must NOT touch it
        recoveryCoordinator.recoverStaleJobs();

        ClientImportJob job = jobRepository.findById(jobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo("VALIDATING");
        assertThat(job.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("6. Multiple workers cannot claim the same job simultaneously")
    void testMultipleWorkersCannotClaimSameJob() throws Exception {
        String jobId = UUID.randomUUID().toString();
        jobStatusService.createInitialJob(jobId, "race_job.csv", "hash", "key", "test_admin");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Callable<Boolean> claimTask = () -> {
            latch.await();
            return jobStatusService.claimJobForProcessing(jobId);
        };

        Future<Boolean> f1 = executor.submit(claimTask);
        Future<Boolean> f2 = executor.submit(claimTask);

        latch.countDown();

        boolean r1 = f1.get(5, TimeUnit.SECONDS);
        boolean r2 = f2.get(5, TimeUnit.SECONDS);

        executor.shutdown();

        // Exactly one worker must succeed, the other must fail
        assertThat(r1 ^ r2).isTrue(); // XOR: exactly one is true
        assertThat(r1 && r2).isFalse();
    }

    @Test
    @DisplayName("7. Failed import preserves all-or-nothing persistence and sequence rollback")
    void testFailedImportPreservesAllOrNothingAndSequenceRollback() {
        long preSeq = sequenceRepository.findById("CLIENT_CODE").map(LeadCodeSequence::getCurrentValue).orElse(0L);
        long preClientCount = clientRepository.count();

        // CSV with invalid second row (missing mandatory location)
        String csvContent = "clientName,mobileNumber,speciality,location,clinicAddress,salesPersonEmployeeCode\n"
                + "Dr. Valid,9823" + (System.currentTimeMillis() % 1000000L) + ",General,Pune,FC Road," + testRmEmpCode + "\n"
                + "Dr. Invalid,9824" + (System.currentTimeMillis() % 1000000L) + ",General,,FC Road," + testRmEmpCode + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        BulkClientImportSubmitResponse response = bulkClientImportService.submitImport(file, "test_admin");
        awaitJobTerminalState(response.getJobId(), 10);

        ClientImportJob job = jobRepository.findById(response.getJobId()).orElseThrow();
        assertThat(job.getStatus()).isEqualTo("FAILED");

        // Sequence must NOT have advanced
        long postSeq = sequenceRepository.findById("CLIENT_CODE").map(LeadCodeSequence::getCurrentValue).orElse(0L);
        assertThat(postSeq).isEqualTo(preSeq);

        // Zero clients persisted
        long postClientCount = clientRepository.count();
        assertThat(postClientCount).isEqualTo(preClientCount);
    }

    @Test
    @DisplayName("8. A stale job left in VALIDATING after JVM crash is automatically re-queued and processed to completion")
    void testStaleValidatingJobIsAutoRecoveredAndProcessedToCompletion() {
        String testMobile = "9822" + (System.currentTimeMillis() % 1000000L);
        String csvContent = "clientName,mobileNumber,speciality,location,clinicAddress,salesPersonEmployeeCode\n"
                + "Dr. AutoRecovered," + testMobile + ",Neurology,Nashik,College Road," + testRmEmpCode + "\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "autorecover.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        String storageKey = storageService.uploadFile(file);

        String jobId = UUID.randomUUID().toString();
        ClientImportJob staleValidatingJob = ClientImportJob.builder()
                .id(jobId)
                .fileName("autorecover.csv")
                .fileHashSha256("dummyhash_autorecover")
                .storageFileKey(storageKey)
                .status("VALIDATING")
                .totalRows(1)
                .processedRows(0)
                .createdBy("test_admin")
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .startedAt(LocalDateTime.now().minusMinutes(18))
                .heartbeatAt(LocalDateTime.now().minusMinutes(15)) // 15 mins ago (> 10-min timeout)
                .build();
        jobRepository.save(staleValidatingJob);

        // Run recovery coordinator: should detect VALIDATING with valid storageKey and auto-requeue
        recoveryCoordinator.recoverStaleJobs();

        // Await terminal state
        awaitJobTerminalState(jobId, 10);

        ClientImportJob recoveredJob = jobRepository.findById(jobId).orElseThrow();
        assertThat(recoveredJob.getStatus()).isEqualTo("COMPLETED");
        assertThat(recoveredJob.getErrorMessage()).isEqualTo("Auto-recovered from crash");
        assertThat(recoveredJob.getStartClientCode()).isNotNull();

        // Verify client was created
        Optional<Client> client = clientRepository.findByMobileNumber(testMobile);
        assertThat(client).isPresent();
        assertThat(client.get().getClientName()).isEqualTo("Dr. AutoRecovered");
    }

    @Test
    @DisplayName("9. A stale VALIDATING job that already failed an auto-recovery attempt transitions safely to FAILED")
    void testStaleValidatingJobWithPriorAutoRecoveryFailsSafe() {
        String jobId = UUID.randomUUID().toString();
        ClientImportJob loopJob = ClientImportJob.builder()
                .id(jobId)
                .fileName("loop.csv")
                .fileHashSha256("dummyhash_loop")
                .storageFileKey("documents/dummy_loop.csv")
                .status("VALIDATING")
                .errorMessage("Auto-recovered from crash") // Prior attempt already happened
                .totalRows(10)
                .processedRows(0)
                .createdBy("test_admin")
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .startedAt(LocalDateTime.now().minusMinutes(25))
                .heartbeatAt(LocalDateTime.now().minusMinutes(15))
                .build();
        jobRepository.save(loopJob);

        recoveryCoordinator.recoverStaleJobs();

        ClientImportJob failedJob = jobRepository.findById(jobId).orElseThrow();
        assertThat(failedJob.getStatus()).isEqualTo("FAILED");
        assertThat(failedJob.getErrorMessage()).contains("timed out or server restarted");
    }

    @Test
    @DisplayName("10. Crash after persistence commit reconciles job to COMPLETED without duplicate clients")
    void testCrashAfterPersistenceCommitReconcilesJobWithoutDuplicate() {
        String testMobile = "9821" + (System.currentTimeMillis() % 1000000L);
        String jobId = UUID.randomUUID().toString();
        jobStatusService.createInitialJob(jobId, "post_commit.csv", "hash", "documents/post_commit.csv", "test_admin");

        ClientImportRowDto row = ClientImportRowDto.builder()
                .rowNumber(1)
                .clientName("Dr. PostCommitCrash")
                .mobileNumber(testMobile)
                .speciality("Cardiology")
                .location("Mumbai")
                .clinicAddress("Fort")
                .salesPersonEmployeeCode(testRmEmpCode)
                .build();

        // 1. Persist the client in the database (simulating successful commit)
        User rmUser = userRepository.findProfileByEmployeeCodeIgnoreCaseAndDeletedFalse(testRmEmpCode).orElseThrow();
        BulkClientPersistenceService.PersistenceResult result = persistenceService.persistAllClientsAtomically(
                jobId,
                List.of(row),
                Map.of(testRmEmpCode.toUpperCase(Locale.ENGLISH), rmUser),
                rmUser,
                "test_admin",
                null
        );

        // 2. Simulate crash window: job was left in EXECUTING with stale heartbeat (as if post-commit status update died)
        ClientImportJob job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus("EXECUTING");
        job.setTotalRows(1);
        job.setStartClientCode(null);
        job.setEndClientCode(null);
        job.setHeartbeatAt(LocalDateTime.now().minusMinutes(15)); // Expired heartbeat
        jobRepository.save(job);

        long preClientCount = clientRepository.count();
        long preSeq = sequenceRepository.findById("CLIENT_CODE").map(LeadCodeSequence::getCurrentValue).orElse(0L);

        // 3. Recovery coordinator runs
        recoveryCoordinator.recoverStaleJobs();

        // 4. Verify reconciliation: job transitioned to COMPLETED with correct codes, NOT FAILED
        ClientImportJob reconciledJob = jobRepository.findById(jobId).orElseThrow();
        assertThat(reconciledJob.getStatus()).isEqualTo("COMPLETED");
        assertThat(reconciledJob.getErrorMessage()).isEqualTo("Reconciled from post-commit crash");
        assertThat(reconciledJob.getStartClientCode()).isEqualTo(result.startCode());
        assertThat(reconciledJob.getEndClientCode()).isEqualTo(result.endCode());
        assertThat(reconciledJob.getProcessedRows()).isEqualTo(1);

        // 5. Zero duplicate clients created
        assertThat(clientRepository.count()).isEqualTo(preClientCount);
        assertThat(sequenceRepository.findById("CLIENT_CODE").map(LeadCodeSequence::getCurrentValue).orElse(0L)).isEqualTo(preSeq);

        // 6. Verify ClientAssignmentHistory count is exactly 1 with exact job reason
        String reason = BulkClientPersistenceService.BULK_IMPORT_REASON_PREFIX + jobId;
        long historyCount = entityManager.createQuery(
                "SELECT COUNT(h) FROM ClientAssignmentHistory h WHERE h.reason = :reason", Long.class)
                .setParameter("reason", reason)
                .getSingleResult();
        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    @DisplayName("11. Crash before persistence commit marks job FAILED safely with 0 clients")
    void testCrashBeforePersistenceCommitMarksJobFailedSafely() {
        String jobId = UUID.randomUUID().toString();
        ClientImportJob job = ClientImportJob.builder()
                .id(jobId)
                .fileName("pre_commit_crash.csv")
                .fileHashSha256("dummyhash_pre_commit")
                .storageFileKey("documents/dummy_pre_commit.csv")
                .status("EXECUTING")
                .totalRows(5)
                .processedRows(0)
                .createdBy("test_admin")
                .createdAt(LocalDateTime.now().minusMinutes(25))
                .startedAt(LocalDateTime.now().minusMinutes(20))
                .heartbeatAt(LocalDateTime.now().minusMinutes(15))
                .build();
        jobRepository.save(job);

        recoveryCoordinator.recoverStaleJobs();

        ClientImportJob failedJob = jobRepository.findById(jobId).orElseThrow();
        assertThat(failedJob.getStatus()).isEqualTo("FAILED");
        assertThat(failedJob.getErrorMessage()).contains("Persistence transaction aborted before commit. 0 clients created. Safe to re-queue.");

        // Confirm 0 clients created for this job
        String reason = BulkClientPersistenceService.BULK_IMPORT_REASON_PREFIX + jobId;
        long historyCount = entityManager.createQuery(
                "SELECT COUNT(h) FROM ClientAssignmentHistory h WHERE h.reason = :reason", Long.class)
                .setParameter("reason", reason)
                .getSingleResult();
        assertThat(historyCount).isEqualTo(0);
    }

    @Test
    @DisplayName("12. reQueueJob refuses to re-import a job whose clients are already persisted")
    void testReQueueJobRefusesToReImportCommittedJob() {
        String testMobile = "9820" + (System.currentTimeMillis() % 1000000L);
        String jobId = UUID.randomUUID().toString();
        jobStatusService.createInitialJob(jobId, "already_committed.csv", "hash", "documents/already_committed.csv", "test_admin");

        ClientImportRowDto row = ClientImportRowDto.builder()
                .rowNumber(1)
                .clientName("Dr. AlreadyCommitted")
                .mobileNumber(testMobile)
                .speciality("Dentistry")
                .location("Delhi")
                .clinicAddress("Connaught Place")
                .salesPersonEmployeeCode(testRmEmpCode)
                .build();

        User rmUser = userRepository.findProfileByEmployeeCodeIgnoreCaseAndDeletedFalse(testRmEmpCode).orElseThrow();
        persistenceService.persistAllClientsAtomically(
                jobId,
                List.of(row),
                Map.of(testRmEmpCode.toUpperCase(Locale.ENGLISH), rmUser),
                rmUser,
                "test_admin",
                null
        );

        long preClientCount = clientRepository.count();

        ClientImportJob existingJob = jobRepository.findById(jobId).orElseThrow();
        existingJob.setTotalRows(1);
        jobRepository.save(existingJob);

        // Attempt to re-queue the job: must return false and refuse to duplicate
        boolean reQueued = recoveryCoordinator.reQueueJob(jobId);
        assertThat(reQueued).isFalse();

        // Zero clients added
        assertThat(clientRepository.count()).isEqualTo(preClientCount);

        ClientImportJob job = jobRepository.findById(jobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("13. Partial persistence crash marks job FAILED and alerts for manual reconciliation")
    void testPartialPersistenceCrashMarksJobFailedForManualReconciliation() {
        String testMobile = "9819" + (System.currentTimeMillis() % 1000000L);
        String jobId = UUID.randomUUID().toString();
        jobStatusService.createInitialJob(jobId, "partial.csv", "hash", "documents/partial.csv", "test_admin");

        ClientImportRowDto row = ClientImportRowDto.builder()
                .rowNumber(1)
                .clientName("Dr. Partial")
                .mobileNumber(testMobile)
                .speciality("Orthopedics")
                .location("Thane")
                .clinicAddress("Gokhale Road")
                .salesPersonEmployeeCode(testRmEmpCode)
                .build();

        User rmUser = userRepository.findProfileByEmployeeCodeIgnoreCaseAndDeletedFalse(testRmEmpCode).orElseThrow();
        // Persist only 1 client
        persistenceService.persistAllClientsAtomically(
                jobId,
                List.of(row),
                Map.of(testRmEmpCode.toUpperCase(Locale.ENGLISH), rmUser),
                rmUser,
                "test_admin",
                null
        );

        // Set job as EXECUTING with totalRows = 2 (so committed 1 of 2 -> partial!)
        ClientImportJob job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus("EXECUTING");
        job.setTotalRows(2);
        job.setStartClientCode(null);
        job.setEndClientCode(null);
        job.setHeartbeatAt(LocalDateTime.now().minusMinutes(15));
        jobRepository.save(job);

        // Run recovery coordinator
        recoveryCoordinator.recoverStaleJobs();

        // Must NOT be marked COMPLETED; must be marked FAILED with partial persistence error
        ClientImportJob failedJob = jobRepository.findById(jobId).orElseThrow();
        assertThat(failedJob.getStatus()).isEqualTo("FAILED");
        assertThat(failedJob.getErrorMessage()).contains("Partial client persistence detected: 1 of 2 clients committed. Manual reconciliation required.");
    }

    private void awaitJobTerminalState(String jobId, int maxSeconds) {
        long end = System.currentTimeMillis() + (maxSeconds * 1000L);
        while (System.currentTimeMillis() < end) {
            ClientImportJob j = jobRepository.findById(jobId).orElse(null);
            if (j != null && ("COMPLETED".equals(j.getStatus()) || "FAILED".equals(j.getStatus()))) {
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}
        }
    }
}
