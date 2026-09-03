package com.blueant_crm_erp.lead;

import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.lead.dto.request.AssignPhysicalLeadRequest;
import com.blueant_crm_erp.lead.dto.request.CreatePhysicalLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadDetailResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.blueant_crm_erp.BlueantCrmErpApplication.class)
@AutoConfigureMockMvc
@Transactional
public class PhysicalLeadAssignmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PhysicalLeadService physicalLeadService;

    @Autowired
    private LeadService leadService;

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

    private User salesCoordinator;
    private User activeSalesPerson1;
    private User activeSalesPerson2;
    private User inactiveSalesPerson;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findAll().stream().findFirst().orElseThrow();
        Department department = departmentRepository.findAll().stream().findFirst().orElseThrow();
        Designation designation = designationRepository.findAll().stream().findFirst().orElseThrow();
        Team team = teamRepository.findAll().stream().findFirst().orElseThrow();

        salesCoordinator = userRepository.save(User.builder()
                .employeeCode("COORD_" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Coordinator")
                .lastName("User")
                .email("coord_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .mobileNumber("987" + String.format("%07d", (int)(Math.random() * 10000000)))
                .password("password")
                .gender(Gender.FEMALE)
                .status(Status.ACTIVE)
                .role(role)
                .department(department)
                .designation(designation)
                .team(team)
                .build());

        activeSalesPerson1 = userRepository.save(User.builder()
                .employeeCode("SP1_" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Sales")
                .lastName("One")
                .email("sp1_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .mobileNumber("988" + String.format("%07d", (int)(Math.random() * 10000000)))
                .password("password")
                .gender(Gender.MALE)
                .status(Status.ACTIVE)
                .role(role)
                .department(department)
                .designation(designation)
                .team(team)
                .build());

        activeSalesPerson2 = userRepository.save(User.builder()
                .employeeCode("SP2_" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Sales")
                .lastName("Two")
                .email("sp2_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .mobileNumber("989" + String.format("%07d", (int)(Math.random() * 10000000)))
                .password("password")
                .gender(Gender.MALE)
                .status(Status.ACTIVE)
                .role(role)
                .department(department)
                .designation(designation)
                .team(team)
                .build());

        inactiveSalesPerson = userRepository.save(User.builder()
                .employeeCode("INACTIVE_" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Inactive")
                .lastName("User")
                .email("inactive_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .mobileNumber("990" + String.format("%07d", (int)(Math.random() * 10000000)))
                .password("password")
                .gender(Gender.MALE)
                .status(Status.INACTIVE)
                .accountEnabled(false)
                .role(role)
                .department(department)
                .designation(designation)
                .team(team)
                .build());
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "ROLE_SALES_COORDINATOR"})
    void salesCoordinatorCanCreateAndAssignPhysicalLeadInOneRequest() throws Exception {
        String mobile = "911" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Dr. Rajesh Kumar")
                .mobileNumber(mobile)
                .speciality("Cardiologist")
                .location("Faridabad")
                .clinicAddress("Sector 15, Faridabad")
                .remarks("Walk-in lead collected at medical expo")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        String responseJson = mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientName").value("Dr. Rajesh Kumar"))
                .andExpect(jsonPath("$.data.speciality").value("Cardiologist"))
                .andExpect(jsonPath("$.data.isPhysicalLead").value(true))
                .andExpect(jsonPath("$.data.assignedEmployeeCode").value(activeSalesPerson1.getEmployeeCode()))
                .andExpect(jsonPath("$.data.assignedByCoordinator").value(true))
                .andExpect(jsonPath("$.data.assignmentLabel").value("Assigned by Sales Coordinator"))
                .andExpect(jsonPath("$.data.assignedAt").exists())
                .andExpect(jsonPath("$.data.assignmentSource").value("SALES_COORDINATOR"))
                .andReturn().getResponse().getContentAsString();

        String leadCode = objectMapper.readTree(responseJson).get("data").get("leadCode").asText();
        String uniqueLeadId = objectMapper.readTree(responseJson).get("data").get("uniqueLeadId").asText();

        // Verify lead details via GET /v1/leads/{uniqueLeadId}
        LeadDetailResponse leadDetail = leadService.getLeadDetails(uniqueLeadId);
        assertThat(leadDetail.getClientName()).isEqualTo("Dr. Rajesh Kumar");
        assertThat(leadDetail.getSpeciality()).isEqualTo("Cardiologist");
        assertThat(leadDetail.getClinicAddress()).isEqualTo("Sector 15, Faridabad");
        assertThat(leadDetail.getIsPhysicalLead()).isTrue();
        assertThat(leadDetail.getAssignedEmployeeCode()).isEqualTo(activeSalesPerson1.getEmployeeCode());
        assertThat(leadDetail.getAssignedByCoordinator()).isTrue();
        assertThat(leadDetail.getAssignmentLabel()).isEqualTo("Assigned by Sales Coordinator");

        // Database verification
        Lead entity = leadRepository.findByLeadCode(leadCode).orElseThrow();
        assertThat(entity.getAssignedSalesPerson().getId()).isEqualTo(activeSalesPerson1.getId());
        assertThat(entity.getIsPhysicalLead()).isTrue();
        assertThat(entity.getAssignmentSource()).isEqualTo("SALES_COORDINATOR");
        assertThat(entity.getAssignedAt()).isNotNull();
        assertThat(entity.getLeadStatus()).isEqualTo(com.blueant_crm_erp.lead.enums.LeadStatus.ASSIGNED);
        assertThat(entity.getLeadStage()).isEqualTo(com.blueant_crm_erp.lead.enums.LeadStage.LEAD_ASSIGNED);
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "ROLE_SALES_COORDINATOR"})
    void createPhysicalLeadWithDuplicateMobileIsRejected() throws Exception {
        String mobile = "922" + String.format("%07d", (int)(Math.random() * 10000000));

        CreatePhysicalLeadRequest createReq1 = CreatePhysicalLeadRequest.builder()
                .clientName("First Client")
                .mobileNumber(mobile)
                .speciality("Dentist")
                .location("Delhi")
                .clinicAddress("Address 1")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq1)))
                .andExpect(status().isOk());

        CreatePhysicalLeadRequest createReq2 = CreatePhysicalLeadRequest.builder()
                .clientName("Duplicate Client")
                .mobileNumber(mobile)
                .speciality("Neurologist")
                .location("Delhi")
                .clinicAddress("Address 2")
                .salesPersonEmployeeCode(activeSalesPerson2.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "ROLE_SALES_COORDINATOR"})
    void createPhysicalLeadWithNonExistentSalesPersonIsRejectedAndNoLeadCreated() throws Exception {
        String mobile = "933" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Test Doctor")
                .mobileNumber(mobile)
                .speciality("General")
                .location("Noida")
                .clinicAddress("Sector 18, Noida")
                .salesPersonEmployeeCode("NON_EXISTENT_EMP_9999")
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isNotFound());

        // Verify database remains unchanged (no lead created for this mobile)
        assertThat(leadRepository.existsByMobileNumber(mobile)).isFalse();
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "ROLE_SALES_COORDINATOR"})
    void createPhysicalLeadWithInactiveSalesPersonIsRejectedAndNoLeadCreated() throws Exception {
        String mobile = "944" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Test Doctor 2")
                .mobileNumber(mobile)
                .speciality("Pediatrician")
                .location("Gurgaon")
                .clinicAddress("DLF Phase 3")
                .salesPersonEmployeeCode(inactiveSalesPerson.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isBadRequest());

        // Verify database remains unchanged
        assertThat(leadRepository.existsByMobileNumber(mobile)).isFalse();
    }

    @Test
    @WithMockUser(roles = "SALES_PERSON")
    void salesPersonCannotCreatePhysicalLead() throws Exception {
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Forbidden Client")
                .mobileNumber("9887766554")
                .speciality("Dentist")
                .location("Delhi")
                .clinicAddress("CP")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void unauthorizedUserCannotCreatePhysicalLead() throws Exception {
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Forbidden Client 2")
                .mobileNumber("9887766555")
                .speciality("Dentist")
                .location("Delhi")
                .clinicAddress("CP")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "ROLE_SALES_COORDINATOR"})
    void newlyCreatedAssignedPhysicalLeadIsAbsentFromEligibleList() throws Exception {
        String mobile = "955" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Assigned Physical Lead")
                .mobileNumber(mobile)
                .speciality("Orthopedic")
                .location("Faridabad")
                .clinicAddress("Sector 15, Faridabad")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        String responseJson = mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String leadCode = objectMapper.readTree(responseJson).get("data").get("leadCode").asText();

        // Because it was created and assigned immediately, it must NOT be in eligible list
        mockMvc.perform(get("/v1/Leads_assign/eligible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[?(@.leadCode == '" + leadCode + "')]").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "LEAD_READ", "LEAD_UPDATE"})
    void salesPersonVisibilityAndIsolation() throws Exception {
        String mobile = "977" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Isolated Lead")
                .mobileNumber(mobile)
                .speciality("Pathologist")
                .location("Noida")
                .clinicAddress("Sector 62, Noida")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        String responseJson = mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String leadCode = objectMapper.readTree(responseJson).get("data").get("leadCode").asText();

        // Sales Person 1 filters -> sees lead
        com.blueant_crm_erp.lead.dto.request.LeadFilterRequest filterReq1 = com.blueant_crm_erp.lead.dto.request.LeadFilterRequest.builder()
                .assignedUserId(activeSalesPerson1.getId())
                .build();

        mockMvc.perform(post("/v1/leads/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterReq1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.leadCode == '" + leadCode + "')]").exists());

        // Sales Person 2 filters -> does NOT see lead
        com.blueant_crm_erp.lead.dto.request.LeadFilterRequest filterReq2 = com.blueant_crm_erp.lead.dto.request.LeadFilterRequest.builder()
                .assignedUserId(activeSalesPerson2.getId())
                .build();

        mockMvc.perform(post("/v1/leads/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.leadCode == '" + leadCode + "')]").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "LEAD_READ", "LEAD_UPDATE", "ROLE_SALES_COORDINATOR"})
    void normalLeadHasNoCoordinatorBadge() throws Exception {
        String mobile = "988" + String.format("%07d", (int)(Math.random() * 10000000));
        com.blueant_crm_erp.lead.dto.request.CreateLeadRequest normalReq = com.blueant_crm_erp.lead.dto.request.CreateLeadRequest.builder()
                .clientName("Normal Client")
                .mobileNumber(mobile)
                .location("Connaught Place")
                .leadSource(com.blueant_crm_erp.lead.enums.LeadSource.WEBSITE)
                .build();

        com.blueant_crm_erp.lead.dto.response.LeadResponse normalLead = leadService.createLead(normalReq, activeSalesPerson1.getEmployeeCode());

        com.blueant_crm_erp.lead.dto.response.LeadDetailResponse detail = leadService.getLeadDetails(normalLead.getUniqueLeadId());
        assertThat(detail.getAssignedByCoordinator()).isFalse();
        assertThat(detail.getAssignmentLabel()).isNull();
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "LEAD_READ", "LEAD_UPDATE", "ROLE_SALES_COORDINATOR"})
    void assignedPhysicalLeadCanContinueExistingWorkflow() throws Exception {
        String mobile = "999" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Workflow Client")
                .mobileNumber(mobile)
                .speciality("Oncologist")
                .location("Delhi")
                .clinicAddress("AIIMS Road")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        String responseJson = mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String leadCode = objectMapper.readTree(responseJson).get("data").get("leadCode").asText();
        String uniqueLeadId = objectMapper.readTree(responseJson).get("data").get("uniqueLeadId").asText();

        // Update lead (normal workflow step: call & follow-up update)
        com.blueant_crm_erp.lead.dto.request.UpdateLeadRequest updateReq = com.blueant_crm_erp.lead.dto.request.UpdateLeadRequest.builder()
                .clientName("Workflow Client Updated")
                .location("Delhi AIIMS")
                .remarks("Spoke with client on phone")
                .nextPlanDate(java.time.LocalDate.now().plusDays(2))
                .lastCallDate(java.time.LocalDate.now())
                .build();

        leadService.updateLead(uniqueLeadId, updateReq, activeSalesPerson1.getEmployeeCode());

        // Verify status change (normal workflow step)
        com.blueant_crm_erp.lead.dto.request.UpdateLeadStatusRequest statusReq = com.blueant_crm_erp.lead.dto.request.UpdateLeadStatusRequest.builder()
                .leadId(leadRepository.findByLeadCode(leadCode).orElseThrow().getId())
                .leadStatus(com.blueant_crm_erp.lead.enums.LeadStatus.CONTACTED)
                .leadStage(com.blueant_crm_erp.lead.enums.LeadStage.FIRST_CONTACT)
                .remarks("First contact completed")
                .build();

        leadService.changeStatus(statusReq, activeSalesPerson1.getEmployeeCode());

        LeadDetailResponse finalDetail = leadService.getLeadDetails(uniqueLeadId);
        assertThat(finalDetail.getClientName()).isEqualTo("Workflow Client Updated");
        assertThat(finalDetail.getLeadStatus()).isEqualTo(com.blueant_crm_erp.lead.enums.LeadStatus.CONTACTED);
        assertThat(finalDetail.getIsPhysicalLead()).isTrue();
        assertThat(finalDetail.getAssignedByCoordinator()).isTrue();
        assertThat(finalDetail.getAssignmentLabel()).isEqualTo("Assigned by Sales Coordinator");
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "ROLE_SALES_COORDINATOR"})
    void concurrentPhysicalLeadCreationGeneratesUniqueLeadCodesAndNoCollisions() throws Exception {
        User targetSalesPerson = userRepository.findAll().stream()
                .filter(u -> u.isActive() && !u.isDeleted() && StringUtils.hasText(u.getEmployeeCode()))
                .findFirst()
                .orElse(activeSalesPerson1);
        String targetEmployeeCode = targetSalesPerson.getEmployeeCode();

        int threadCount = 10;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.List<java.util.concurrent.Future<String>> futures = new java.util.ArrayList<>();
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.getContext();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                org.springframework.security.core.context.SecurityContextHolder.setContext(context);
                latch.await();
                String mobile = "900" + String.format("%07d", index + (int)(Math.random() * 100000));
                CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                        .clientName("Concurrent Client " + index)
                        .mobileNumber(mobile)
                        .speciality("Cardiologist")
                        .location("City " + index)
                        .clinicAddress("Clinic " + index)
                        .salesPersonEmployeeCode(targetEmployeeCode)
                        .build();

                String res = mockMvc.perform(post("/v1/Leads_assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

                return objectMapper.readTree(res).get("data").get("leadCode").asText();
            }));
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(15, java.util.concurrent.TimeUnit.SECONDS);

        java.util.Set<String> generatedCodes = new java.util.HashSet<>();
        for (java.util.concurrent.Future<String> future : futures) {
            String code = future.get();
            assertThat(code).startsWith("LD");
            assertThat(generatedCodes.add(code)).isTrue();
        }

        assertThat(generatedCodes).hasSize(threadCount);
    }

    @Test
    @WithMockUser(authorities = {"PHYSICAL_LEAD_ASSIGN", "ROLE_SALES_COORDINATOR"})
    void leadCodeGenerationHandlesGapsAndNonSequentialExistingLeadCodes() throws Exception {
        // Create an out-of-sequence lead code manually
        Lead customLead = Lead.builder()
                .leadCode("LD009999")
                .uniqueLeadId(java.util.UUID.randomUUID().toString())
                .clientName("High Code Lead")
                .mobileNumber("977" + String.format("%07d", (int)(Math.random() * 10000000)))
                .isPhysicalLead(true)
                .leadSource(com.blueant_crm_erp.lead.enums.LeadSource.FIELD_VISIT)
                .leadType(com.blueant_crm_erp.lead.enums.LeadType.MUTUAL_FUND)
                .priority(com.blueant_crm_erp.lead.enums.LeadPriority.MEDIUM)
                .duplicateLeadStatus(com.blueant_crm_erp.lead.enums.DuplicateLeadStatus.ORIGINAL)
                .leadStatus(com.blueant_crm_erp.lead.enums.LeadStatus.ASSIGNED)
                .leadStage(com.blueant_crm_erp.lead.enums.LeadStage.LEAD_ASSIGNED)
                .build();
        leadRepository.save(customLead);

        // Delete a lead to create a gap in count
        customLead.markAsDeleted("test");
        leadRepository.save(customLead);

        // Perform physical lead creation via API
        String mobile = "966" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Gap Test Doctor")
                .mobileNumber(mobile)
                .speciality("Neurologist")
                .location("Delhi")
                .clinicAddress("Fortis Hospital")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        String responseJson = mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String newCode = objectMapper.readTree(responseJson).get("data").get("leadCode").asText();
        assertThat(newCode).startsWith("LD");
        assertThat(leadRepository.existsByLeadCode(newCode)).isTrue();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_SALES_COORDINATOR", "PHYSICAL_LEAD_ASSIGN"})
    void salesCoordinatorWithPhysicalLeadAssignPermissionAllowedToCreate() throws Exception {
        String mobile = "951" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Coordinator Perm Allowed Client")
                .mobileNumber(mobile)
                .speciality("Cardiologist")
                .location("Delhi")
                .clinicAddress("Appollo")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_SALES_COORDINATOR"})
    void salesCoordinatorWithoutPhysicalLeadAssignForbiddenToCallLeadsAssignEndpoint() throws Exception {
        String mobile = "957" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Coordinator Without Perm Attempt")
                .mobileNumber(mobile)
                .speciality("Cardiologist")
                .location("Delhi")
                .clinicAddress("Appollo")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "PHYSICAL_LEAD_ASSIGN"})
    void adminWithPhysicalLeadAssignAllowedToCreate() throws Exception {
        String mobile = "952" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Admin Allowed Client")
                .mobileNumber(mobile)
                .speciality("Cardiologist")
                .location("Delhi")
                .clinicAddress("Appollo")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_SUPER_ADMIN"})
    void superAdminRoleAllowedToCreate() throws Exception {
        String mobile = "953" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Super Admin Allowed Client")
                .mobileNumber(mobile)
                .speciality("Cardiologist")
                .location("Delhi")
                .clinicAddress("Appollo")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_SALES_PERSON"})
    void salesPersonForbiddenToCallLeadsAssignEndpoint() throws Exception {
        String mobile = "955" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Unauthorized Sales Person Attempt")
                .mobileNumber(mobile)
                .speciality("General")
                .location("Delhi")
                .clinicAddress("Address")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void normalUserForbiddenToCallLeadsAssignEndpoint() throws Exception {
        String mobile = "956" + String.format("%07d", (int)(Math.random() * 10000000));
        CreatePhysicalLeadRequest createReq = CreatePhysicalLeadRequest.builder()
                .clientName("Unauthorized Normal User Attempt")
                .mobileNumber(mobile)
                .speciality("General")
                .location("Delhi")
                .clinicAddress("Address")
                .salesPersonEmployeeCode(activeSalesPerson1.getEmployeeCode())
                .build();

        mockMvc.perform(post("/v1/Leads_assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden());
    }
}
