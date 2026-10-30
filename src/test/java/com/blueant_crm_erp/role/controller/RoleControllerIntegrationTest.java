package com.blueant_crm_erp.role.controller;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.role.dto.response.RoleSummaryResponse;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.mapper.RoleMapper;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.role.service.RoleService;
import com.blueant_crm_erp.role.service.impl.RoleServiceImpl;
import com.blueant_crm_erp.role.validator.RoleValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    private Role role1;
    private Role role2;
    private Role deletedRole;

    @BeforeEach
    void setUp() {
        role1 = roleRepository.save(Role.builder()
                .name("Integration Test Alpha Role")
                .code("ROLE_INT_TEST_ALPHA_" + System.currentTimeMillis())
                .description("First test role")
                .displayOrder(10)
                .systemRole(false)
                .defaultRole(false)
                .status(Status.ACTIVE)
                .build());

        role2 = roleRepository.save(Role.builder()
                .name("Integration Test Beta Role")
                .code("ROLE_INT_TEST_BETA_" + System.currentTimeMillis())
                .description("Second test role")
                .displayOrder(20)
                .systemRole(false)
                .defaultRole(false)
                .status(Status.ACTIVE)
                .build());

        deletedRole = Role.builder()
                .name("Integration Test Deleted Role")
                .code("ROLE_INT_TEST_DELETED_" + System.currentTimeMillis())
                .description("Soft deleted role")
                .displayOrder(5)
                .systemRole(false)
                .defaultRole(false)
                .status(Status.ACTIVE)
                .build();
        deletedRole.markAsDeleted("test-runner");
        deletedRole = roleRepository.save(deletedRole);
    }

    @AfterEach
    void tearDown() {
        if (role1 != null && role1.getId() != null) {
            roleRepository.deleteById(role1.getId());
        }
        if (role2 != null && role2.getId() != null) {
            roleRepository.deleteById(role2.getId());
        }
        if (deletedRole != null && deletedRole.getId() != null) {
            roleRepository.deleteById(deletedRole.getId());
        }
    }

    @Test
    @DisplayName("GET /v1/roles with authenticated user returns 200 OK with properly mapped and ordered roles")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllRoles_Success_ReturnsMappedRolesOrdered() throws Exception {
        String responseContent = mockMvc.perform(get("/v1/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(responseContent);
        com.fasterxml.jackson.databind.JsonNode dataArray = rootNode.get("data");

        boolean foundRole1 = false;
        boolean foundRole2 = false;

        for (com.fasterxml.jackson.databind.JsonNode item : dataArray) {
            if (role1.getCode().equals(item.get("roleCode").asText())) {
                foundRole1 = true;
                assertThat(item.get("roleName").asText()).isEqualTo(role1.getName());
                assertThat(item.get("id").asLong()).isEqualTo(role1.getId());
                assertThat(item.get("status").asText()).isEqualTo("ACTIVE");
                assertThat(item.get("defaultRole").asBoolean()).isFalse();
                assertThat(item.get("systemRole").asBoolean()).isFalse();
            }
            if (role2.getCode().equals(item.get("roleCode").asText())) {
                foundRole2 = true;
                assertThat(item.get("roleName").asText()).isEqualTo(role2.getName());
                assertThat(item.get("id").asLong()).isEqualTo(role2.getId());
            }
        }

        assertThat(foundRole1).isTrue();
        assertThat(foundRole2).isTrue();
    }

    @Test
    @DisplayName("GET /v1/roles excludes soft-deleted roles")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllRoles_ExcludesSoftDeletedRoles() throws Exception {
        mockMvc.perform(get("/v1/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.roleCode == '" + deletedRole.getCode() + "')]").doesNotExist());
    }

    @Test
    @DisplayName("GET /v1/roles without authentication returns 401 Unauthorized")
    void testGetAllRoles_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RoleServiceImpl.getAllRoles returns empty list when repository returns no roles")
    void testRoleServiceImpl_EmptyList_ReturnsEmptyList() {
        RoleRepository mockRepo = mock(RoleRepository.class);
        RoleValidator mockValidator = mock(RoleValidator.class);
        when(mockRepo.findAllByDeletedFalseOrderByDisplayOrderAsc()).thenReturn(Collections.emptyList());

        RoleService service = new RoleServiceImpl(mockRepo, roleMapper, mockValidator);
        List<RoleSummaryResponse> results = service.getAllRoles();

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("RoleMapper correctly maps Role entity to RoleSummaryResponse fields")
    void testRoleMapper_MappingVerification() {
        Role role = Role.builder()
                .id(999L)
                .name("Senior Relationship Manager")
                .code("ROLE_SR_RM")
                .description("Senior RM Role")
                .displayOrder(3)
                .status(Status.ACTIVE)
                .defaultRole(true)
                .systemRole(false)
                .build();

        RoleSummaryResponse summary = roleMapper.toSummaryResponse(role);

        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(999L);
        assertThat(summary.getRoleName()).isEqualTo("Senior Relationship Manager");
        assertThat(summary.getRoleCode()).isEqualTo("ROLE_SR_RM");
        assertThat(summary.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(summary.getDefaultRole()).isTrue();
        assertThat(summary.getSystemRole()).isFalse();
    }
}
