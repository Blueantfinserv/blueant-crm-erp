package com.blueant_crm_erp.client.bulk;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.bulk.dto.BulkClientImportJobStatusResponse;
import com.blueant_crm_erp.client.bulk.dto.BulkClientImportSubmitResponse;
import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import com.blueant_crm_erp.client.bulk.repository.ClientImportJobRepository;
import com.blueant_crm_erp.client.bulk.service.BulkClientImportService;
import com.blueant_crm_erp.client.bulk.service.impl.BulkClientPersistenceService;
import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientCodeGeneratorService;
import com.blueant_crm_erp.client.service.ClientRegistrationService;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
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
import com.blueant_crm_erp.util.id.ClientCodeGenerator;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Performance & Concurrency Benchmark for 4,000-Row Bulk Client Import")
class BulkClientImportBenchmarkTest {

    @Autowired
    private BulkClientImportService bulkClientImportService;

    @Autowired
    private BulkClientPersistenceService bulkClientPersistenceService;

    @Autowired
    private ClientRegistrationService clientRegistrationService;

    @Autowired
    private ClientCodeGeneratorService clientCodeGeneratorService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientImportJobRepository jobRepository;

    @Autowired
    private LeadCodeSequenceRepository sequenceRepository;

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
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User rmUser;
    private String actorUsername;

    @BeforeEach
    void setUp() {
        Department salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        Team team = teamRepository.findAll().stream().findFirst()
                .orElseGet(() -> teamRepository.save(Team.builder()
                        .teamName("Benchmark Team " + UUID.randomUUID().toString().substring(0, 6))
                        .teamCode("TEAM_BENCH_" + UUID.randomUUID().toString().substring(0, 6))
                        .department(salesDept)
                        .status(Status.ACTIVE)
                        .build()));

        Role role = roleRepository.findByCodeIgnoreCase(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER)
                        .name("Relationship Manager")
                        .status(Status.ACTIVE)
                        .systemRole(true)
                        .defaultRole(false)
                        .build()));

        Designation desig = designationRepository.findByCodeIgnoreCase(BootstrapConstants.DESIG_RM)
                .orElseGet(() -> designationRepository.save(Designation.builder()
                        .code(BootstrapConstants.DESIG_RM)
                        .name("RM")
                        .department(salesDept)
                        .hierarchyLevel(4)
                        .status(Status.ACTIVE)
                        .build()));

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        rmUser = userRepository.save(User.builder()
                .employeeCode("RM_BENCH_" + suffix)
                .email("rm_bench_" + suffix + "@blueant.com")
                .password("$2a$10$dummyHashedPasswordForTestEnvironment")
                .firstName("BenchRM")
                .lastName("Tester")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .status(Status.ACTIVE)
                .accountEnabled(true)
                .role(role)
                .designation(desig)
                .department(salesDept)
                .team(team)
                .gender(Gender.MALE)
                .deleted(false)
                .build());

        actorUsername = rmUser.getEmployeeCode();
    }

    @Test
    @DisplayName("BENCHMARK: 4,000-Row Bulk Import with Concurrent Single Registration Latencies")
    void benchmark4000RowsImportWithConcurrentRegistrations() throws Exception {
        int totalRows = 4000;
        System.out.println("==========================================================");
        System.out.println("STARTING 4,000-ROW BULK IMPORT BENCHMARK");
        System.out.println("==========================================================");

        // 1. Generate Synthetic 4,000-Row CSV in memory
        long startGen = System.currentTimeMillis();
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Client Name,Mobile Number,Speciality,Location,Clinic Address,Sales Person Employee Code,Email,PAN Number,Date of Birth\n");

        long baseMobile = 7000000000L + (Math.abs(UUID.randomUUID().hashCode()) % 10000000L);
        for (int i = 1; i <= totalRows; i++) {
            String mobile = String.valueOf(baseMobile + i);
            String clientName = "Dr. Synthetic Client " + i;
            String pan = String.format("ABCDE%04dF", i % 10000);
            csvBuilder.append(clientName).append(",")
                    .append(mobile).append(",")
                    .append("Cardiology").append(",")
                    .append("Mumbai").append(",")
                    .append("Clinic Address ").append(i).append(",")
                    .append(rmUser.getEmployeeCode()).append(",")
                    .append("synthetic_").append(i).append("@example.com").append(",")
                    .append(pan).append(",")
                    .append("1985-06-15\n");
        }
        byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
        long genTime = System.currentTimeMillis() - startGen;
        System.out.println("Generated 4,000-row synthetic CSV payload: " + (csvBytes.length / 1024) + " KB in " + genTime + " ms");

        // 2. Initial Pool & Memory Metrics
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memBeforeBytes = runtime.totalMemory() - runtime.freeMemory();

        HikariPoolMXBean poolBean = null;
        if (dataSource instanceof HikariDataSource hds) {
            poolBean = hds.getHikariPoolMXBean();
            System.out.println("Initial Hikari Pool: Active=" + poolBean.getActiveConnections()
                    + ", Idle=" + poolBean.getIdleConnections()
                    + ", Awaiting=" + poolBean.getThreadsAwaitingConnection());
        }

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "synthetic_4000_clients.csv",
                "text/csv",
                csvBytes
        );

        // 3. Submit Import Job
        long startSubmit = System.currentTimeMillis();
        BulkClientImportSubmitResponse submitResponse = bulkClientImportService.submitImport(multipartFile, actorUsername);
        String jobId = submitResponse.getJobId();
        assertThat(jobId).isNotNull();
        System.out.println("Submitted Job ID: " + jobId + " (Status: " + submitResponse.getStatus() + ")");

        // 4. Concurrently fire Single-Client Registrations while 4,000 import is executing
        int concurrentSingleCount = 10;
        ExecutorService singleExecutor = Executors.newFixedThreadPool(concurrentSingleCount);
        List<Future<Long>> singleFutures = new ArrayList<>();
        List<Long> singleLatencies = new CopyOnWriteArrayList<>();
        AtomicInteger singleSuccessCount = new AtomicInteger(0);
        AtomicInteger singleFailureCount = new AtomicInteger(0);

        for (int k = 0; k < concurrentSingleCount; k++) {
            final int singleId = k;
            singleFutures.add(singleExecutor.submit(() -> {
                Thread.sleep(200 + (singleId * 250));
                long singleStart = System.currentTimeMillis();
                try {
                    String singleMobile = "8" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L));
                    CreateClientRegistrationRequest req = CreateClientRegistrationRequest.builder()
                            .clientName("Dr. Single Concurrent " + singleId)
                            .mobileNumber(singleMobile)
                            .speciality("Neurology")
                            .location("Pune")
                            .clinicAddress("Single Clinic " + singleId)
                            .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                            .build();

                    clientRegistrationService.registerClient(req, actorUsername);
                    long duration = System.currentTimeMillis() - singleStart;
                    singleLatencies.add(duration);
                    singleSuccessCount.incrementAndGet();
                    return duration;
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - singleStart;
                    singleFailureCount.incrementAndGet();
                    System.err.println("Concurrent single registration failed: " + e.getMessage());
                    return duration;
                }
            }));
        }

        // 5. Poll bulk job status until completion
        String finalStatus = "PENDING";
        int pollCount = 0;
        int maxPolls = 240; // up to 120 seconds
        BulkClientImportJobStatusResponse statusResponse = null;

        while (pollCount < maxPolls) {
            Thread.sleep(500);
            pollCount++;
            statusResponse = bulkClientImportService.getJobStatus(jobId, actorUsername, 0, 10);
            finalStatus = statusResponse.getStatus();

            if (pollCount % 10 == 0 || "COMPLETED".equals(finalStatus) || "FAILED".equals(finalStatus)) {
                System.out.println("Poll #" + pollCount + " [" + (pollCount * 500) + "ms]: Status=" + finalStatus
                        + ", Processed=" + statusResponse.getProcessedRows() + "/" + statusResponse.getTotalRows()
                        + (poolBean != null ? (" | Hikari Active=" + poolBean.getActiveConnections()) : ""));
            }

            if ("COMPLETED".equals(finalStatus) || "FAILED".equals(finalStatus)) {
                break;
            }
        }

        long totalImportDuration = System.currentTimeMillis() - startSubmit;
        runtime.gc();
        long memAfterBytes = runtime.totalMemory() - runtime.freeMemory();

        // Wait for all single concurrent requests
        for (Future<Long> f : singleFutures) {
            try {
                f.get(10, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
        }
        singleExecutor.shutdown();

        // 6. Calculate Latency percentiles
        Collections.sort(singleLatencies);
        long p50 = singleLatencies.isEmpty() ? 0 : singleLatencies.get((int) (singleLatencies.size() * 0.50));
        long p95 = singleLatencies.isEmpty() ? 0 : singleLatencies.get(Math.min((int) (singleLatencies.size() * 0.95), singleLatencies.size() - 1));
        long p99 = singleLatencies.isEmpty() ? 0 : singleLatencies.get(Math.min((int) (singleLatencies.size() * 0.99), singleLatencies.size() - 1));

        // 7. Verify Contiguity and DB Counts
        assertThat(finalStatus).isEqualTo("COMPLETED");
        assertThat(statusResponse.getStartClientCode()).isNotNull();
        assertThat(statusResponse.getEndClientCode()).isNotNull();

        long startSeq = ClientCodeGenerator.extractSequence(statusResponse.getStartClientCode());
        long endSeq = ClientCodeGenerator.extractSequence(statusResponse.getEndClientCode());
        long allocatedCount = endSeq - startSeq + 1;
        assertThat(allocatedCount).isEqualTo(totalRows);

        Integer insertedClients = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE mobile_number BETWEEN ? AND ?",
                Integer.class,
                String.valueOf(baseMobile + 1),
                String.valueOf(baseMobile + totalRows)
        );

        System.out.println("==========================================================");
        System.out.println("4,000-ROW BENCHMARK EMPIRICAL RESULTS");
        System.out.println("==========================================================");
        System.out.println("Final Job Status: " + finalStatus);
        System.out.println("Total Execution Time: " + totalImportDuration + " ms (" + (totalImportDuration / 1000.0) + " s)");
        System.out.println("Throughput: " + String.format("%.2f", (totalRows / (totalImportDuration / 1000.0))) + " clients/sec");
        System.out.println("Client Code Range Allocated: " + statusResponse.getStartClientCode() + " -> " + statusResponse.getEndClientCode());
        System.out.println("Allocated Range Count: " + allocatedCount);
        System.out.println("Actual Clients Verified in DB: " + insertedClients);
        System.out.println("Memory Delta: " + ((memAfterBytes - memBeforeBytes) / (1024 * 1024)) + " MB");
        System.out.println("Concurrent Single Registrations Success: " + singleSuccessCount.get() + "/" + concurrentSingleCount);
        System.out.println("Concurrent Single Registrations Failures: " + singleFailureCount.get() + "/" + concurrentSingleCount);
        System.out.println("Concurrent Single Latency p50: " + p50 + " ms");
        System.out.println("Concurrent Single Latency p95: " + p95 + " ms");
        System.out.println("Concurrent Single Latency p99: " + p99 + " ms");
        if (poolBean != null) {
            System.out.println("Final Hikari Pool: Active=" + poolBean.getActiveConnections()
                    + ", Idle=" + poolBean.getIdleConnections()
                    + ", Awaiting=" + poolBean.getThreadsAwaitingConnection());
        }
        System.out.println("==========================================================");

        assertThat(insertedClients).isEqualTo(totalRows);
    }

    @Test
    @DisplayName("BENCHMARK: Atomic Rollback of 4,000 Rows with Flushed Batches and Zero Sequence Gap")
    void benchmarkRollbackAfterFlushes() {
        System.out.println("==========================================================");
        System.out.println("TESTING ATOMIC ROLLBACK OF 4,000 ROWS FLUSHED IN BATCHES");
        System.out.println("==========================================================");

        // Get initial sequence counter
        Long initialSeqValue = jdbcTemplate.queryForObject(
                "SELECT current_value FROM lead_code_sequences WHERE sequence_name = 'CLIENT_CODE'", Long.class);
        if (initialSeqValue == null) initialSeqValue = 0L;

        long initialClientCount = clientRepository.count();

        int batchSize = 4000;
        long baseMobile = 6500000000L + (Math.abs(UUID.randomUUID().hashCode()) % 10000000L);

        List<com.blueant_crm_erp.client.bulk.dto.ClientImportRowDto> rows = new ArrayList<>();
        for (int i = 1; i <= batchSize; i++) {
            rows.add(com.blueant_crm_erp.client.bulk.dto.ClientImportRowDto.builder()
                    .rowNumber(i + 1)
                    .clientName("Rollback Candidate " + i)
                    .mobileNumber(String.valueOf(baseMobile + i))
                    .speciality("Neurology")
                    .location("Kolkata")
                    .clinicAddress("Clinic " + i)
                    .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                    .build());
        }

        Map<String, User> salesCache = Map.of(rmUser.getEmployeeCode().toUpperCase(), rmUser);

        boolean rollbackCaught = false;
        long startRollbackTest = System.currentTimeMillis();

        try {
            // Call persistAllClientsAtomically with a callback that throws after 3,500 rows have been processed and flushed!
            bulkClientPersistenceService.persistAllClientsAtomically(
                    "bench-rollback-job",
                    rows,
                    salesCache,
                    rmUser,
                    actorUsername,
                    processedCount -> {
                        if (processedCount >= 3500) {
                            throw new IllegalStateException("Simulated mid-batch system crash after 3,500 rows flushed to socket.");
                        }
                    }
            );
        } catch (Exception e) {
            rollbackCaught = true;
            System.out.println("Caught expected rollback exception: " + e.getMessage());
        }

        long rollbackDuration = System.currentTimeMillis() - startRollbackTest;

        // Verify that 0 client records were inserted from this batch
        long finalClientCount = clientRepository.count();
        assertThat(finalClientCount).isEqualTo(initialClientCount);

        // Verify that the sequence counter in lead_code_sequences is rolled back
        Long finalSeqValue = jdbcTemplate.queryForObject(
                "SELECT current_value FROM lead_code_sequences WHERE sequence_name = 'CLIENT_CODE'", Long.class);
        assertThat(finalSeqValue).isEqualTo(initialSeqValue);

        System.out.println("Rollback verification successful in " + rollbackDuration + " ms.");
        System.out.println("Pre-test Sequence: " + initialSeqValue + " | Post-rollback Sequence: " + finalSeqValue);
        System.out.println("Pre-test Clients: " + initialClientCount + " | Post-rollback Clients: " + finalClientCount);
        System.out.println("ZERO SEQUENCE GAPS CONFIRMED.");
        System.out.println("==========================================================");

        assertThat(rollbackCaught).isTrue();
    }
}
