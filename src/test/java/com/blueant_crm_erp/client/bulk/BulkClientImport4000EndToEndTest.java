package com.blueant_crm_erp.client.bulk;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.bulk.dto.BulkClientImportJobStatusResponse;
import com.blueant_crm_erp.client.bulk.dto.BulkClientImportSubmitResponse;
import com.blueant_crm_erp.client.bulk.dto.ClientImportRowDto;
import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import com.blueant_crm_erp.client.bulk.repository.ClientImportJobRepository;
import com.blueant_crm_erp.client.bulk.service.BulkClientImportService;
import com.blueant_crm_erp.client.bulk.service.impl.BulkClientPersistenceService;
import com.blueant_crm_erp.client.bulk.service.impl.ClientImportJobStatusService;
import com.blueant_crm_erp.client.bulk.service.impl.ImportJobRecoveryCoordinator;
import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientAssignmentHistory;
import com.blueant_crm_erp.client.enums.ClientStatus;
import com.blueant_crm_erp.client.repository.ClientAssignmentHistoryRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientRegistrationService;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.client.ClientAlreadyExistsException;
import com.blueant_crm_erp.lead.entity.LeadCodeSequence;
import com.blueant_crm_erp.lead.repository.LeadCodeSequenceRepository;
import com.blueant_crm_erp.meeting.enums.SalesRole;
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
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("4,000-Client Bulk Import End-to-End Test (LOCAL TEST ONLY)")
public class BulkClientImport4000EndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientAssignmentHistoryRepository assignmentHistoryRepository;

    @Autowired
    private ClientImportJobRepository jobRepository;

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
    private UserDetailsService customUserDetailsService;

    @Autowired
    private SalesRoleResolver salesRoleResolver;

    @Autowired
    private ImportJobRecoveryCoordinator recoveryCoordinator;

    @Autowired
    private ClientImportJobStatusService jobStatusService;

    @Autowired
    private BulkClientPersistenceService bulkPersistenceService;

    @Autowired
    private ClientRegistrationService clientRegistrationService;

    @Autowired
    private LeadCodeSequenceRepository sequenceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User crmUser;
    private User rmUser;
    private User scUser;
    private UserDetails crmPrincipal;

    private static final int TOTAL_ROWS = 4000;
    private static final String EXCEL_FILENAME = "Bulk_Client_Import_TEST_4000_Synthetic.xlsx";

    @BeforeEach
    void setUp() throws Exception {
        // 1. Verify Local TEST Database Identity
        String dbUrl = jdbcTemplate.queryForObject("SELECT @@hostname", String.class);
        String dbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        System.out.println("==========================================================");
        System.out.println("LOCAL TEST ENVIRONMENT VERIFICATION");
        System.out.println("==========================================================");
        System.out.println("Database Hostname: " + dbUrl);
        System.out.println("Active Database Name: " + dbName);
        assertThat(dbName).isEqualToIgnoringCase("blueant_crm");

        // 2. Load or Create Test Roles and Departments
        Department salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));
        Department crmDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(salesDept));

        Team testTeam = teamRepository.findAll().stream().findFirst()
                .orElseGet(() -> teamRepository.save(Team.builder()
                        .teamName("E2E Test Team")
                        .teamCode("TEAM_E2E_TEST")
                        .department(salesDept)
                        .status(Status.ACTIVE)
                        .build()));

        // 3. Setup CRM User (Authorized to upload bulk import)
        crmUser = createTestUser("CRM_E2E", BootstrapConstants.ROLE_CRM_ONBOARDING,
                BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, testTeam, "PHYSICAL_LEAD_ASSIGN", "client:read");

        // 4. Setup Active RM and SC Employees (Meeting/Sales role resolution verified)
        rmUser = createTestUser("RM_E2E", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER,
                BootstrapConstants.DESIG_RM, salesDept, testTeam, "MEETING_UPDATE", "MEETING_READ");

        scUser = createTestUser("SC_E2E", BootstrapConstants.ROLE_PC_COORDINATOR,
                BootstrapConstants.DESIG_SC, salesDept, testTeam, "MEETING_UPDATE", "MEETING_READ");

        assertThat(salesRoleResolver.resolve(rmUser)).isEqualTo(SalesRole.RM);
        assertThat(salesRoleResolver.resolve(scUser)).isEqualTo(SalesRole.SC);
        System.out.println("Verified Active RM Code: " + rmUser.getEmployeeCode() + " -> " + SalesRole.RM);
        System.out.println("Verified Active SC Code: " + scUser.getEmployeeCode() + " -> " + SalesRole.SC);

        crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
    }

    @Test
    @DisplayName("Execute 4,000-Client Bulk Import End-to-End with Full Invariant Verification and Restart Recovery")
    void testExecute4000ClientBulkImportEndToEnd() throws Exception {
        System.out.println("==========================================================");
        System.out.println("TASK: 4,000-CLIENT BULK IMPORT E2E TEST (LOCAL TEST)");
        System.out.println("==========================================================");

        // A. Capture Before Baselines
        long preGlobalClientCount = clientRepository.count();
        long preGlobalHistoryCount = assignmentHistoryRepository.count();
        long preLeadCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM leads", Long.class);
        long preMeetingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM meetings", Long.class);
        long preFollowUpCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM followups", Long.class)
                + jdbcTemplate.queryForObject("SELECT COUNT(*) FROM client_followups", Long.class);
        long preSeq = sequenceRepository.findById("CLIENT_CODE")
                .map(LeadCodeSequence::getCurrentValue)
                .orElse(0L);

        System.out.println("PRE-IMPORT BASELINES:");
        System.out.println("  Global Clients: " + preGlobalClientCount);
        System.out.println("  Global Assignment Histories: " + preGlobalHistoryCount);
        System.out.println("  Global Leads: " + preLeadCount);
        System.out.println("  Global Meetings: " + preMeetingCount);
        System.out.println("  Global Follow-Ups: " + preFollowUpCount);
        System.out.println("  Current CLIENT_CODE Sequence: " + preSeq);

        // B. Generate Collision-Free Mobile Number Range
        Set<String> existingMobiles = new HashSet<>(
                jdbcTemplate.queryForList("SELECT mobile_number FROM clients WHERE mobile_number IS NOT NULL", String.class));
        long baseMobile = 8200000000L;
        while (true) {
            boolean collision = false;
            for (int i = 1; i <= TOTAL_ROWS; i++) {
                if (existingMobiles.contains(String.valueOf(baseMobile + i))) {
                    collision = true;
                    break;
                }
            }
            if (!collision) break;
            baseMobile += 10000000L;
        }
        System.out.println("Selected Collision-Free Mobile Range: " + (baseMobile + 1) + " to " + (baseMobile + TOTAL_ROWS));

        // C. Build Synthetic 4,000-Row XLSX Workbook
        File excelFile = new File(EXCEL_FILENAME);
        System.out.println("Generating 4,000-row synthetic workbook: " + excelFile.getAbsolutePath());
        long startGen = System.currentTimeMillis();

        String[] rmScCodes = new String[]{rmUser.getEmployeeCode(), scUser.getEmployeeCode()};
        String[] specialities = new String[]{"Cardiology", "Neurology", "Orthopedics", "Dermatology", "Oncology"};
        String[] locations = new String[]{"Mumbai", "Pune", "Delhi", "Bengaluru", "Chennai"};

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Clients");

            // Header row matching template contract
            Row header = sheet.createRow(0);
            String[] headers = {
                    "Client Name *", "Mobile Number *", "Speciality *", "Location *", "Clinic Address *",
                    "Sales Person Employee Code *", "Alternate Mobile Number", "Email", "PAN Number",
                    "Date of Birth (YYYY-MM-DD)", "Actual Date of Birth (YYYY-MM-DD)", "Occupation",
                    "Mother Name", "Place of Birth", "Residential Address", "Correspondence Address",
                    "Office Address", "City", "State", "PIN Code", "Client Source", "Source Description",
                    "Expected Max SIP", "First Investment Amount", "Investment Type", "Investwell ID",
                    "Best Time to Meet", "Assignment Date (YYYY-MM-DD)", "Remarks"
            };
            for (int col = 0; col < headers.length; col++) {
                header.createCell(col).setCellValue(headers[col]);
            }

            // 4,000 Synthetic Client Rows
            for (int i = 1; i <= TOTAL_ROWS; i++) {
                Row row = sheet.createRow(i);
                String assignedCode = rmScCodes[i % rmScCodes.length];
                String mobile = String.valueOf(baseMobile + i);
                String pan = String.format("SYNTH%04dF", i % 10000);

                row.createCell(0).setCellValue("Dr. Synthetic Client " + i);
                row.createCell(1).setCellValue(mobile);
                row.createCell(2).setCellValue(specialities[i % specialities.length]);
                row.createCell(3).setCellValue(locations[i % locations.length]);
                row.createCell(4).setCellValue("Clinic Suite " + i + ", Central Hospital");
                row.createCell(5).setCellValue(assignedCode); // Replaced with valid active RM/SC employee code
                row.createCell(6).setCellValue("7" + String.format("%09d", i)); // Alternate mobile
                row.createCell(7).setCellValue("synthetic_" + i + "@blueant.test");
                row.createCell(8).setCellValue(pan);
                row.createCell(9).setCellValue("1985-04-12");
                row.createCell(10).setCellValue("1985-04-12");
                row.createCell(11).setCellValue("Physician");
                row.createCell(12).setCellValue("Mother Name " + i);
                row.createCell(13).setCellValue("City " + i);
                row.createCell(14).setCellValue("Residential Flat " + i);
                row.createCell(15).setCellValue("Correspondence Address " + i);
                row.createCell(16).setCellValue("Office Address " + i);
                row.createCell(17).setCellValue(locations[i % locations.length]);
                row.createCell(18).setCellValue("State");
                row.createCell(19).setCellValue("400001");
                row.createCell(20).setCellValue("REFERRAL");
                row.createCell(21).setCellValue("Annual medical conference");
                row.createCell(22).setCellValue("25000");
                row.createCell(23).setCellValue("100000");
                row.createCell(24).setCellValue("MUTUAL_FUND");
                row.createCell(25).setCellValue("IW" + String.format("%06d", i));
                row.createCell(26).setCellValue("1st half of the day");
                row.createCell(27).setCellValue(LocalDate.now().toString());
                row.createCell(28).setCellValue("Synthetic E2E test client " + i);
            }

            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }
            workbook.dispose();
        }

        byte[] xlsxBytes = Files.readAllBytes(excelFile.toPath());
        long genElapsed = System.currentTimeMillis() - startGen;
        System.out.println("Synthetic XLSX Generated: " + (xlsxBytes.length / 1024) + " KB in " + genElapsed + " ms");

        // D. Execute Import via Real HTTP Controller Endpoint
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                EXCEL_FILENAME,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                xlsxBytes
        );

        long startImportTime = System.currentTimeMillis();
        MvcResult submitResult = mockMvc.perform(multipart("/v1/clients/bulk-import")
                        .file(multipartFile)
                        .with(user(crmPrincipal)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        JsonNode submitNode = objectMapper.readTree(submitResult.getResponse().getContentAsString());
        String jobId = submitNode.path("data").path("jobId").asText();
        System.out.println("Submitted Bulk Import Job ID: " + jobId);

        // E. Poll Status Endpoint until Terminal State
        String terminalStatus = "PENDING";
        int pollCount = 0;
        int maxPolls = 180; // Up to 90 seconds (expected ~15-25s)
        JsonNode lastStatusNode = null;

        while (pollCount < maxPolls) {
            Thread.sleep(500);
            pollCount++;

            MvcResult pollResult = mockMvc.perform(get("/v1/clients/bulk-import/jobs/" + jobId)
                            .param("page", "0")
                            .param("size", "10")
                            .with(user(crmPrincipal)))
                    .andExpect(status().isOk())
                    .andReturn();

            lastStatusNode = objectMapper.readTree(pollResult.getResponse().getContentAsString()).path("data");
            terminalStatus = lastStatusNode.path("status").asText();

            if (pollCount % 10 == 0 || "COMPLETED".equals(terminalStatus) || "FAILED".equals(terminalStatus)) {
                System.out.println("Poll #" + pollCount + " [" + (pollCount * 500) + "ms]: Status = " + terminalStatus
                        + ", ProcessedRows = " + lastStatusNode.path("processedRows").asInt()
                        + "/" + lastStatusNode.path("totalRows").asInt());
            }

            if ("COMPLETED".equals(terminalStatus) || "FAILED".equals(terminalStatus)) {
                break;
            }
        }

        long totalElapsedMs = System.currentTimeMillis() - startImportTime;
        double elapsedSec = totalElapsedMs / 1000.0;
        double throughput = TOTAL_ROWS / elapsedSec;

        System.out.println("==========================================================");
        System.out.println("IMPORT EXECUTION COMPLETED");
        System.out.println("==========================================================");
        System.out.println("Final Job Status: " + terminalStatus);
        System.out.println("Total Execution Time: " + totalElapsedMs + " ms (" + String.format("%.2f", elapsedSec) + " s)");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " clients/sec");

        // F. Assert Terminal Status & API Response Invariants
        assertThat(terminalStatus).isEqualTo("COMPLETED");
        assertThat(lastStatusNode.path("processedRows").asInt()).isEqualTo(TOTAL_ROWS);
        assertThat(lastStatusNode.path("totalRows").asInt()).isEqualTo(TOTAL_ROWS);

        String startClientCode = lastStatusNode.path("startClientCode").asText();
        String endClientCode = lastStatusNode.path("endClientCode").asText();
        assertThat(startClientCode).isNotBlank();
        assertThat(endClientCode).isNotBlank();

        System.out.println("Allocated Client Code Range: " + startClientCode + " -> " + endClientCode);
        long startSeq = ClientCodeGenerator.extractSequence(startClientCode);
        long endSeq = ClientCodeGenerator.extractSequence(endClientCode);
        long allocatedDelta = endSeq - startSeq + 1;
        assertThat(allocatedDelta).isEqualTo(TOTAL_ROWS);
        System.out.println("Sequence Continuity Verified: " + allocatedDelta + " contiguous codes (Zero Gaps)");

        // G. Database Invariant Verifications (Read-Only Direct SQL Queries)
        System.out.println("==========================================================");
        System.out.println("DATABASE INVARIANT AUDIT");
        System.out.println("==========================================================");

        // 1. Exactly 4,000 clients created for this test job
        String reasonMarker = BulkClientPersistenceService.BULK_IMPORT_REASON_PREFIX + jobId;
        Long jobClientsCount = entityManager.createQuery(
                "SELECT COUNT(DISTINCT h.client.id) FROM ClientAssignmentHistory h WHERE h.reason = :reason", Long.class)
                .setParameter("reason", reasonMarker)
                .getSingleResult();
        assertThat(jobClientsCount).isEqualTo(TOTAL_ROWS);
        System.out.println("1. Job-Linked Clients Count: " + jobClientsCount + " (Expected: " + TOTAL_ROWS + ") [PASS]");

        // 2. Exactly 4,000 assignment history records created for this job
        Long jobHistoriesCount = entityManager.createQuery(
                "SELECT COUNT(h) FROM ClientAssignmentHistory h WHERE h.reason = :reason", Long.class)
                .setParameter("reason", reasonMarker)
                .getSingleResult();
        assertThat(jobHistoriesCount).isEqualTo(TOTAL_ROWS);
        System.out.println("2. Job-Linked Histories Count: " + jobHistoriesCount + " (Expected: " + TOTAL_ROWS + ") [PASS]");

        // 3. Every imported client has lead_id = NULL
        Long clientsWithLeadCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients c " +
                        "JOIN client_assignment_histories h ON c.id = h.client_id " +
                        "WHERE h.reason = ? AND c.lead_id IS NOT NULL",
                Long.class, reasonMarker);
        assertThat(clientsWithLeadCount).isEqualTo(0L);
        System.out.println("3. Imported Clients with lead_id != NULL: " + clientsWithLeadCount + " (Expected: 0) [PASS]");

        // 4. Valid active RM/SC assignments for all 4,000 clients
        Long validAssignmentsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients c " +
                        "JOIN client_assignment_histories h ON c.id = h.client_id " +
                        "WHERE h.reason = ? AND c.sales_person_id IN (?, ?)",
                Long.class, reasonMarker, rmUser.getId(), scUser.getId());
        assertThat(validAssignmentsCount).isEqualTo(TOTAL_ROWS);
        System.out.println("4. Clients with Valid Active RM/SC Assignments: " + validAssignmentsCount + " (Expected: " + TOTAL_ROWS + ") [PASS]");

        // 5. Zero duplicate mobile numbers introduced
        Long dupMobilesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (SELECT mobile_number FROM clients GROUP BY mobile_number HAVING COUNT(*) > 1) d",
                Long.class);
        assertThat(dupMobilesCount).isEqualTo(0L);
        System.out.println("5. Global Duplicate Mobile Groups in Database: " + dupMobilesCount + " (Expected: 0) [PASS]");

        // 6. Zero unintended leads, meetings, follow-ups created
        long postLeadCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM leads", Long.class);
        long postMeetingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM meetings", Long.class);
        long postFollowUpCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM followups", Long.class)
                + jdbcTemplate.queryForObject("SELECT COUNT(*) FROM client_followups", Long.class);
        assertThat(postLeadCount).isEqualTo(preLeadCount);
        assertThat(postMeetingCount).isEqualTo(preMeetingCount);
        assertThat(postFollowUpCount).isEqualTo(preFollowUpCount);
        System.out.println("6. Side-Effect Invariants: Leads Delta = " + (postLeadCount - preLeadCount)
                + ", Meetings Delta = " + (postMeetingCount - preMeetingCount)
                + ", Follow-ups Delta = " + (postFollowUpCount - preFollowUpCount) + " (Expected: 0) [PASS]");

        // 7. Verify Optional Fields Preserved Correctly
        List<Map<String, Object>> sampleClients = jdbcTemplate.queryForList(
                "SELECT c.client_name, c.mobile_number, c.email, c.pan_number, c.best_time_to_meet, c.speciality " +
                        "FROM clients c JOIN client_assignment_histories h ON c.id = h.client_id " +
                        "WHERE h.reason = ? LIMIT 5", reasonMarker);
        for (Map<String, Object> sample : sampleClients) {
            assertThat(sample.get("email")).asString().endsWith("@blueant.test");
            assertThat(sample.get("pan_number")).asString().startsWith("SYNTH");
            assertThat(sample.get("best_time_to_meet")).isEqualTo("1st half of the day");
        }
        System.out.println("7. Optional Profile Fields Preserved Correctly [PASS]");

        // 8. Soft-deleted mobile reuse remains blocked
        // Create a test client and soft-delete it
        String softDeleteMobile = "99" + String.format("%08d", Math.abs(UUID.randomUUID().hashCode() % 100000000L));
        Client softDeletedClient = clientRepository.save(Client.builder()
                .clientCode(ClientCodeGenerator.generate(999990L))
                .clientName("Dr. Soft Deleted")
                .mobileNumber(softDeleteMobile)
                .clientStatus(ClientStatus.ACTIVE)
                .salesPerson(rmUser)
                .deleted(true)
                .deletedAt(LocalDateTime.now())
                .deletedBy("test_admin")
                .clientSince(LocalDate.now())
                .build());

        CreateClientRegistrationRequest dupReq = CreateClientRegistrationRequest.builder()
                .clientName("Dr. Duplicate Attempter")
                .mobileNumber(softDeleteMobile)
                .speciality("Cardiology")
                .location("Delhi")
                .clinicAddress("Connaught Place")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode())
                .build();

        assertThatThrownBy(() -> clientRegistrationService.registerClient(dupReq, crmUser.getEmployeeCode()))
                .isInstanceOf(ClientAlreadyExistsException.class);
        System.out.println("8. Soft-Deleted Mobile Reuse Blocked: Re-registration threw ClientAlreadyExistsException [PASS]");

        // H. Isolated Restart Recovery Test
        System.out.println("==========================================================");
        System.out.println("ISOLATED RESTART RECOVERY TEST");
        System.out.println("==========================================================");
        String recoveryJobId = UUID.randomUUID().toString();
        jobStatusService.createInitialJob(recoveryJobId, "recovery_isolated.csv", "dummyhash", "documents/recovery_isolated.csv", "test_admin");

        String recMobile1 = "89" + String.format("%08d", Math.abs(UUID.randomUUID().hashCode() % 100000000L));
        String recMobile2 = "89" + String.format("%08d", (Math.abs(UUID.randomUUID().hashCode()) + 1) % 100000000L);

        ClientImportRowDto recRow1 = ClientImportRowDto.builder()
                .rowNumber(1).clientName("Dr. Rec 1").mobileNumber(recMobile1)
                .speciality("Cardiology").location("Mumbai").clinicAddress("Suite 1")
                .salesPersonEmployeeCode(rmUser.getEmployeeCode()).build();
        ClientImportRowDto recRow2 = ClientImportRowDto.builder()
                .rowNumber(2).clientName("Dr. Rec 2").mobileNumber(recMobile2)
                .speciality("Neurology").location("Pune").clinicAddress("Suite 2")
                .salesPersonEmployeeCode(scUser.getEmployeeCode()).build();

        // Persist the 2 clients
        bulkPersistenceService.persistAllClientsAtomically(
                recoveryJobId,
                List.of(recRow1, recRow2),
                Map.of(rmUser.getEmployeeCode(), rmUser, scUser.getEmployeeCode(), scUser),
                rmUser,
                "test_admin",
                null
        );

        // Simulate crash: job left in EXECUTING with stale heartbeat (expired 15 min ago)
        ClientImportJob recJob = jobRepository.findById(recoveryJobId).orElseThrow();
        recJob.setStatus("EXECUTING");
        recJob.setTotalRows(2);
        recJob.setStartClientCode(null);
        recJob.setEndClientCode(null);
        recJob.setHeartbeatAt(LocalDateTime.now().minusMinutes(15));
        jobRepository.save(recJob);

        long preRecCount = clientRepository.count();

        // Recovery Coordinator runs
        recoveryCoordinator.recoverStaleJobs();

        // Verify reconciliation to COMPLETED
        ClientImportJob reconciledJob = jobRepository.findById(recoveryJobId).orElseThrow();
        assertThat(reconciledJob.getStatus()).isEqualTo("COMPLETED");
        assertThat(reconciledJob.getErrorMessage()).isEqualTo("Reconciled from post-commit crash");
        assertThat(reconciledJob.getProcessedRows()).isEqualTo(2);

        // Verify reQueueJob refuses to duplicate
        boolean reQueueSuccess = recoveryCoordinator.reQueueJob(recoveryJobId);
        assertThat(reQueueSuccess).isFalse();
        assertThat(clientRepository.count()).isEqualTo(preRecCount);
        System.out.println("Restart Recovery Verified: Reconciled post-commit crash to COMPLETED with 0 duplicate clients [PASS]");

        // Final sanity check: Main 4,000-row job remains COMPLETED and intact
        ClientImportJob mainJobFinal = jobRepository.findById(jobId).orElseThrow();
        assertThat(mainJobFinal.getStatus()).isEqualTo("COMPLETED");
        assertThat(mainJobFinal.getProcessedRows()).isEqualTo(TOTAL_ROWS);

        System.out.println("==========================================================");
        System.out.println("ALL 4,000-CLIENT IMPORT AND RECOVERY INVARIANTS: PASS");
        System.out.println("==========================================================");
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
                .lastName("Tester")
                .mobileNumber("9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000L)))
                .status(Status.ACTIVE)
                .accountEnabled(true)
                .role(role)
                .designation(desig)
                .department(dept)
                .team(team)
                .gender(Gender.MALE)
                .deleted(false)
                .build());
    }
}

