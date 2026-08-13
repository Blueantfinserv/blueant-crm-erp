package com.blueant_crm_erp.user.controller;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    private Long validRoleId;
    private Long validDepartmentId;
    private Long validDesignationId;
    private Long validTeamId;

    @BeforeEach
    public void setup() {
        // Fetch dynamically from db to ensure the test is robust
        validRoleId = roleRepository.findAll().stream()
                .filter(r -> r.getStatus() == com.blueant_crm_erp.common.enums.Status.ACTIVE)
                .map(Role::getId)
                .findFirst()
                .orElse(null);

        validDepartmentId = departmentRepository.findAll().stream()
                .map(Department::getId)
                .findFirst()
                .orElse(null);

        validDesignationId = designationRepository.findAll().stream()
                .map(Designation::getId)
                .findFirst()
                .orElse(null);

        validTeamId = teamRepository.findAll().stream()
                .map(Team::getId)
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> createBasePayload() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> payload = new HashMap<>();
        payload.put("employeeCode", "EMP" + uniqueSuffix.toUpperCase());
        payload.put("firstName", "John");
        payload.put("lastName", "Doe");
        payload.put("email", "john.doe." + uniqueSuffix + "@blueantcrm.com");
        payload.put("mobileNumber", "9" + String.valueOf(System.currentTimeMillis()).substring(4, 13));
        payload.put("password", "SecurePassword@123");
        payload.put("gender", "MALE");
        payload.put("departmentId", validDepartmentId);
        payload.put("designationId", validDesignationId);
        payload.put("teamId", validTeamId);
        payload.put("roleId", validRoleId);
        return payload;
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateUser_ValidRoleId_ReturnsRoleIdAndName() throws Exception {
        Map<String, Object> payload = createBasePayload();
        
        mockMvc.perform(post("/v1/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleId").value(validRoleId))
                .andExpect(jsonPath("$.data.roleName").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateUser_ValidDepartmentId_ReturnsDepartmentIdAndName() throws Exception {
        Map<String, Object> payload = createBasePayload();

        mockMvc.perform(post("/v1/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.departmentId").value(validDepartmentId))
                .andExpect(jsonPath("$.data.departmentName").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateUser_ValidDesignationId_ReturnsDesignationIdAndName() throws Exception {
        Map<String, Object> payload = createBasePayload();

        mockMvc.perform(post("/v1/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.designationId").value(validDesignationId))
                .andExpect(jsonPath("$.data.designationName").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateUser_ValidTeamId_ReturnsTeamIdAndName() throws Exception {
        Map<String, Object> payload = createBasePayload();

        mockMvc.perform(post("/v1/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.teamId").value(validTeamId))
                .andExpect(jsonPath("$.data.teamName").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateUser_AllValidRelations_ReturnsAllIdsAndNames() throws Exception {
        Map<String, Object> payload = createBasePayload();

        mockMvc.perform(post("/v1/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleId").value(validRoleId))
                .andExpect(jsonPath("$.data.roleName").isNotEmpty())
                .andExpect(jsonPath("$.data.departmentId").value(validDepartmentId))
                .andExpect(jsonPath("$.data.departmentName").isNotEmpty())
                .andExpect(jsonPath("$.data.designationId").value(validDesignationId))
                .andExpect(jsonPath("$.data.designationName").isNotEmpty())
                .andExpect(jsonPath("$.data.teamId").value(validTeamId))
                .andExpect(jsonPath("$.data.teamName").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateUser_AbsentReportingManager_ReturnsNullForReportingManager() throws Exception {
        Map<String, Object> payload = createBasePayload();
        payload.put("reportingManagerId", null);

        mockMvc.perform(post("/v1/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportingManagerId").isEmpty())
                .andExpect(jsonPath("$.data.reportingManagerName").isEmpty());
    }

    @Test
    @WithMockUser(username = "EMP000001", roles = {"SUPER_ADMIN"})
    public void testCreateUser_InvalidDepartmentId_ReturnsError() throws Exception {
        Map<String, Object> payload = createBasePayload();
        payload.put("departmentId", 999999L); // Invalid department

        mockMvc.perform(post("/v1/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound()); // Standard error response status
    }
}
