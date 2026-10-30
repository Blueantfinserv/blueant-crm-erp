package com.blueant_crm_erp.lead;

import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.lead.constants.LeadConstants;
import com.blueant_crm_erp.lead.dto.request.CreatePhysicalLeadRequest;
import com.blueant_crm_erp.lead.dto.response.PhysicalLeadAssignmentResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadCodeGeneratorService;
import com.blueant_crm_erp.lead.service.PhysicalLeadService;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.DesignationRepository;
import com.blueant_crm_erp.user.repository.TeamRepository;
import com.blueant_crm_erp.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class LeadAssignmentLockAndConcurrencyRegressionTest {

    @Autowired
    private LeadCodeGeneratorService leadCodeGeneratorService;

    @Autowired
    private PhysicalLeadService physicalLeadService;

    @Autowired
    private LeadRepository leadRepository;

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

    private User coordinator;
    private User salesPerson;
    private final List<Long> createdLeadIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findAll().stream().findFirst().orElseThrow();
        Department department = departmentRepository.findAll().stream().findFirst().orElseThrow();
        Designation designation = designationRepository.findAll().stream().findFirst().orElseThrow();
        Team team = teamRepository.findAll().stream().findFirst().orElseThrow();

        coordinator = userRepository.save(User.builder()
                .employeeCode("COORD_TEST_" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Coordinator")
                .lastName("Tester")
                .email("coord_test_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .mobileNumber("971" + String.format("%07d", (int)(Math.random() * 10000000)))
                .password("password")
                .gender(Gender.FEMALE)
                .status(Status.ACTIVE)
                .role(role)
                .department(department)
                .designation(designation)
                .team(team)
                .build());

        salesPerson = userRepository.save(User.builder()
                .employeeCode("SP_TEST_" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Sales")
                .lastName("Tester")
                .email("sp_test_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .mobileNumber("972" + String.format("%07d", (int)(Math.random() * 10000000)))
                .password("password")
                .gender(Gender.MALE)
                .status(Status.ACTIVE)
                .role(role)
                .department(department)
                .designation(designation)
                .team(team)
                .build());
    }

    @AfterEach
    void tearDown() {
        for (Long leadId : createdLeadIds) {
            try {
                leadRepository.deleteById(leadId);
            } catch (Exception ignored) {
            }
        }
        if (coordinator != null && coordinator.getId() != null) {
            userRepository.deleteById(coordinator.getId());
        }
        if (salesPerson != null && salesPerson.getId() != null) {
            userRepository.deleteById(salesPerson.getId());
        }
    }

    @Test
    @DisplayName("LeadCodeGenerator generates correctly formatted, non-colliding sequential codes")
    void testLeadCodeGenerator_FormatAndSequentialProgression() {
        String code1 = leadCodeGeneratorService.generateNextLeadCode();
        String code2 = leadCodeGeneratorService.generateNextLeadCode();

        assertThat(code1).startsWith(LeadConstants.LEAD_CODE_PREFIX);
        assertThat(code2).startsWith(LeadConstants.LEAD_CODE_PREFIX);
        assertThat(code1).hasSize(LeadConstants.LEAD_CODE_PREFIX.length() + LeadConstants.LEAD_CODE_PADDING);
        assertThat(code2).hasSize(LeadConstants.LEAD_CODE_PREFIX.length() + LeadConstants.LEAD_CODE_PADDING);
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    @DisplayName("Concurrent lead code generation produces strictly unique codes across concurrent threads")
    void testLeadCodeGenerator_ConcurrentExecution_UniqueAndNoCollisions() throws Exception {
        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                return leadCodeGeneratorService.generateNextLeadCode();
            }));
        }

        startLatch.countDown();
        executor.shutdown();
        boolean terminated = executor.awaitTermination(15, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();

        Set<String> uniqueCodes = new HashSet<>();
        for (Future<String> future : futures) {
            String code = future.get();
            assertThat(code).startsWith(LeadConstants.LEAD_CODE_PREFIX);
            assertThat(uniqueCodes.add(code)).as("Duplicate code found: " + code).isTrue();
        }
        assertThat(uniqueCodes).hasSize(threadCount);
    }

    @Test
    @DisplayName("createPhysicalLead rejects duplicate mobile numbers with LEAD_DUPLICATE_MOBILE")
    void testCreatePhysicalLead_DuplicateMobileProtection() {
        String sharedMobile = "911" + String.format("%07d", (int)(Math.random() * 10000000));

        CreatePhysicalLeadRequest request1 = CreatePhysicalLeadRequest.builder()
                .clientName("Original Client")
                .mobileNumber(sharedMobile)
                .speciality("Dentist")
                .location("Central Plaza")
                .salesPersonEmployeeCode(salesPerson.getEmployeeCode())
                .build();

        PhysicalLeadAssignmentResponse response1 = physicalLeadService.createPhysicalLead(request1, coordinator.getEmployeeCode());
        assertThat(response1).isNotNull();
        assertThat(response1.getLeadCode()).isNotEmpty();

        Lead savedLead = leadRepository.findByLeadCode(response1.getLeadCode()).orElseThrow();
        createdLeadIds.add(savedLead.getId());

        CreatePhysicalLeadRequest request2 = CreatePhysicalLeadRequest.builder()
                .clientName("Duplicate Mobile Client")
                .mobileNumber(sharedMobile)
                .speciality("Dentist")
                .location("Central Plaza")
                .salesPersonEmployeeCode(salesPerson.getEmployeeCode())
                .build();

        assertThatThrownBy(() -> physicalLeadService.createPhysicalLead(request2, coordinator.getEmployeeCode()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(LeadConstants.LEAD_DUPLICATE_MOBILE);
    }

    @Test
    @DisplayName("createPhysicalLead rolls back outer transaction when salesperson is invalid")
    void testCreatePhysicalLead_TransactionRollback_WhenSalesPersonNotFound() {
        String uniqueMobile = "922" + String.format("%07d", (int)(Math.random() * 10000000));

        CreatePhysicalLeadRequest invalidRequest = CreatePhysicalLeadRequest.builder()
                .clientName("Rollback Test Client")
                .mobileNumber(uniqueMobile)
                .speciality("General")
                .location("North Sector")
                .salesPersonEmployeeCode("NON_EXISTENT_SP_99999")
                .build();

        assertThatThrownBy(() -> physicalLeadService.createPhysicalLead(invalidRequest, coordinator.getEmployeeCode()))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(leadRepository.existsByMobileNumber(uniqueMobile)).isFalse();
    }

    @Test
    @DisplayName("Concurrent physical lead creation assigns unique codes without lock timeouts")
    void testCreatePhysicalLead_ConcurrentSubmissions_UniqueLeadCodes() throws Exception {
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<PhysicalLeadAssignmentResponse>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                startLatch.await();
                String mobile = "933" + String.format("%07d", index * 10000 + (int)(Math.random() * 9999));
                CreatePhysicalLeadRequest req = CreatePhysicalLeadRequest.builder()
                        .clientName("Concurrent Lead " + index)
                        .mobileNumber(mobile)
                        .speciality("Surgeon")
                        .location("Sector " + index)
                        .salesPersonEmployeeCode(salesPerson.getEmployeeCode())
                        .build();
                return physicalLeadService.createPhysicalLead(req, coordinator.getEmployeeCode());
            }));
        }

        startLatch.countDown();
        executor.shutdown();
        boolean terminated = executor.awaitTermination(20, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();

        Set<String> assignedCodes = new HashSet<>();
        for (Future<PhysicalLeadAssignmentResponse> future : futures) {
            PhysicalLeadAssignmentResponse resp = future.get();
            assertThat(resp).isNotNull();
            assertThat(resp.getLeadCode()).startsWith(LeadConstants.LEAD_CODE_PREFIX);
            assertThat(assignedCodes.add(resp.getLeadCode())).isTrue();

            leadRepository.findByLeadCode(resp.getLeadCode()).ifPresent(l -> createdLeadIds.add(l.getId()));
        }
        assertThat(assignedCodes).hasSize(threadCount);
    }
}
