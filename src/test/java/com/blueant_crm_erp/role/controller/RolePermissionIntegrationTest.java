package com.blueant_crm_erp.role.controller;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
import com.blueant_crm_erp.role.dto.request.AssignPermissionRequest;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.entity.RolePermission;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RolePermissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    public void setup() {
        cleanup();

        testRole = Role.builder()
                .name("Test Verification Role")
                .code("ROLE_TEST_VERIFICATION_" + System.currentTimeMillis())
                .description("Role for mapping testing")
                .displayOrder(1)
                .systemRole(false)
                .defaultRole(false)
                .status(Status.ACTIVE)
                .build();
        testRole = roleRepository.save(testRole);

        testPermission = Permission.builder()
                .name("Test Verification Permission")
                .code("PERMISSION_TEST_VERIFICATION_" + System.currentTimeMillis())
                .module("TEST")
                .description("Permission for mapping testing")
                .displayOrder(1)
                .systemPermission(false)
                .status(Status.ACTIVE)
                .build();
        testPermission = permissionRepository.save(testPermission);
    }

    @AfterEach
    public void cleanup() {
        rolePermissionRepository.deleteAll();
        if (testRole != null && testRole.getId() != null) {
            roleRepository.deleteById(testRole.getId());
        }
        if (testPermission != null && testPermission.getId() != null) {
            permissionRepository.deleteById(testPermission.getId());
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAssignPermission_Success() throws Exception {
        AssignPermissionRequest request = AssignPermissionRequest.builder()
                .roleId(testRole.getId())
                .permissionIds(List.of(testPermission.getId()))
                .build();

        mockMvc.perform(post("/v1/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].roleId").value(testRole.getId()))
                .andExpect(jsonPath("$.data[0].permissionId").value(testPermission.getId()));

        // Verify mapped in DB
        boolean exists = rolePermissionRepository.existsByRoleIdAndPermissionId(testRole.getId(), testPermission.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetPermissionsByRole_Success() throws Exception {
        // First map the permission
        RolePermission mapping = RolePermission.builder()
                .role(testRole)
                .permission(testPermission)
                .build();
        rolePermissionRepository.save(mapping);

        mockMvc.perform(get("/v1/roles/" + testRole.getId() + "/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].roleId").value(testRole.getId()))
                .andExpect(jsonPath("$.data[0].permissionId").value(testPermission.getId()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetPermissionsByRole_NonexistentRole_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/v1/roles/999999/permissions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAssignDuplicatePermission_ReturnsConflict() throws Exception {
        // First map the permission
        RolePermission mapping = RolePermission.builder()
                .role(testRole)
                .permission(testPermission)
                .build();
        rolePermissionRepository.save(mapping);

        AssignPermissionRequest request = AssignPermissionRequest.builder()
                .roleId(testRole.getId())
                .permissionIds(List.of(testPermission.getId()))
                .build();

        mockMvc.perform(post("/v1/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testRemovePermission_Success() throws Exception {
        // First map the permission
        RolePermission mapping = RolePermission.builder()
                .role(testRole)
                .permission(testPermission)
                .build();
        mapping = rolePermissionRepository.save(mapping);

        AssignPermissionRequest request = AssignPermissionRequest.builder()
                .roleId(testRole.getId())
                .permissionIds(List.of(testPermission.getId()))
                .build();

        mockMvc.perform(delete("/v1/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify soft-deleted
        RolePermission updatedMapping = rolePermissionRepository.findById(mapping.getId()).orElse(null);
        assertThat(updatedMapping).isNotNull();
        assertThat(updatedMapping.isDeleted()).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testRestoreSoftDeletedPermission_Success() throws Exception {
        // First map and soft-delete the permission
        RolePermission mapping = RolePermission.builder()
                .role(testRole)
                .permission(testPermission)
                .build();
        mapping.markAsDeleted("admin");
        mapping = rolePermissionRepository.save(mapping);

        AssignPermissionRequest request = AssignPermissionRequest.builder()
                .roleId(testRole.getId())
                .permissionIds(List.of(testPermission.getId()))
                .build();

        // Assigning again should restore the soft-deleted record
        mockMvc.perform(post("/v1/roles/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        RolePermission updatedMapping = rolePermissionRepository.findById(mapping.getId()).orElse(null);
        assertThat(updatedMapping).isNotNull();
        assertThat(updatedMapping.isDeleted()).isFalse();
    }
}
