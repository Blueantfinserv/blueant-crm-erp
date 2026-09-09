package com.blueant_crm_erp.bootstrap;

import com.blueant_crm_erp.auth.dto.request.LoginRequest;
import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * =============================================================================
 * Production Authentication Recovery Integration Tests
 * =============================================================================
 *
 * Verifies the production recovery lifecycle when the users table has 0 users:
 * 1. Verifies that /v1/users/create requires full authentication (401).
 * 2. Verifies that /bootstrap/run without secret or token returns 401.
 * 3. Verifies that /bootstrap/run with invalid secret returns 401.
 * 4. Verifies that /bootstrap/run with valid X-Bootstrap-Secret succeeds (200) when users table is empty.
 * 5. Verifies that calling /bootstrap/run again with secret is rejected (400) because users exist.
 * 6. Verifies that Super Admin can log in with EMP000001 / Admin@123 and obtain a valid JWT.
 * 7. Verifies that /v1/users/create with Super Admin JWT is authorized (not 401).
 * 8. Verifies that /v1/users/create without JWT still returns 401.
 * 9. Verifies that calling /bootstrap/run with Super Admin JWT succeeds (maintenance mode).
 * =============================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BootstrapRecoveryIntegrationTest {

    private static final String BOOTSTRAP_SECRET_HEADER = "X-Bootstrap-Secret";

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private UserRepository userRepository;

    @Autowired
    private BootstrapProperties bootstrapProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void ensureConfiguredSecret() {
        assertNotNull(bootstrapProperties.getSecret(), "Bootstrap secret must be configured");
        assertFalse(bootstrapProperties.getSecret().isBlank(), "Bootstrap secret must not be blank");
    }

    @Test
    @Order(1)
    @DisplayName("Complete Recovery Flow: Zero Users -> Secure Bootstrap -> Super Admin Login -> Protected API Access")
    void testCompleteAuthenticationRecoveryLifecycle() throws Exception {
        String validSecret = bootstrapProperties.getSecret();

        // Step A: Emulate production state where users table has 0 users
        Mockito.doReturn(0L).when(userRepository).count();

        // Step B: Calling user creation without JWT returns 401 Unauthorized
        mockMvc.perform(post("/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        // Step C: Calling bootstrap without secret and without JWT returns 401 Unauthorized
        mockMvc.perform(post("/bootstrap/run")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Step D: Calling bootstrap with invalid secret returns 401 Unauthorized
        mockMvc.perform(post("/bootstrap/run")
                        .header(BOOTSTRAP_SECRET_HEADER, "invalid-secret-key-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Step E: Calling bootstrap with valid X-Bootstrap-Secret succeeds when users count == 0
        mockMvc.perform(post("/bootstrap/run")
                        .header(BOOTSTRAP_SECRET_HEADER, validSecret)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step F: Verify Super Admin user exists
        Optional<User> superAdminOpt = userRepository.findByEmployeeCodeIgnoreCase(BootstrapConstants.SUPER_ADMIN_EMPLOYEE_CODE);
        assertTrue(superAdminOpt.isPresent(), "Super Admin with employee code EMP000001 must exist");
        User superAdmin = superAdminOpt.get();
        assertEquals(BootstrapConstants.SUPER_ADMIN_EMAIL, superAdmin.getEmail());
        assertTrue("SUPER_ADMIN".equalsIgnoreCase(superAdmin.getRole().getCode())
                || "Super Admin".equalsIgnoreCase(superAdmin.getRole().getName()));

        // Step G: Calling unauthenticated bootstrap again after users exist is rejected with 400 Bad Request
        Mockito.doReturn(1L).when(userRepository).count();
        mockMvc.perform(post("/bootstrap/run")
                        .header(BOOTSTRAP_SECRET_HEADER, validSecret)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bootstrap rejected: System has already been initialized with existing users."));

        // Step H: Super Admin logs in via /auth/login with initialized credentials
        LoginRequest loginRequest = LoginRequest.builder()
                .employeeCode(BootstrapConstants.SUPER_ADMIN_EMPLOYEE_CODE)
                .password(BootstrapConstants.SUPER_ADMIN_PASSWORD)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        JsonNode loginResponseJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String superAdminJwt = loginResponseJson.path("data").path("accessToken").asText();
        assertNotNull(superAdminJwt, "JWT access token must be returned on login");
        assertFalse(superAdminJwt.isBlank(), "JWT access token must not be blank");

        // Step I: User creation without JWT still returns 401 Unauthorized
        mockMvc.perform(post("/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        // Step J: User creation WITH Super Admin JWT is authorized (passed Spring Security authentication)
        // With empty body or invalid payload, it must return 400 Bad Request (validation failure), NEVER 401 Unauthorized
        MvcResult userCreateResult = mockMvc.perform(post("/v1/users/create")
                        .header("Authorization", "Bearer " + superAdminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertNotEquals(401, userCreateResult.getResponse().getStatus(),
                "User creation with valid Super Admin JWT must not return 401 Unauthorized");
        assertEquals(400, userCreateResult.getResponse().getStatus(),
                "User creation with empty payload should fail bean validation (400), confirming access was granted");

        // Step K: Super Admin can execute maintenance bootstrap using their JWT token even when users exist
        mockMvc.perform(post("/bootstrap/run")
                        .header("Authorization", "Bearer " + superAdminJwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(2)
    @DisplayName("Bootstrap status endpoint is protected by secret or Super Admin authentication")
    void testBootstrapStatusEndpointProtection() throws Exception {
        // Without secret or auth -> 401
        mockMvc.perform(get("/bootstrap/status"))
                .andExpect(status().isUnauthorized());

        // With invalid secret -> 401
        mockMvc.perform(get("/bootstrap/status")
                        .header(BOOTSTRAP_SECRET_HEADER, "wrong-key"))
                .andExpect(status().isUnauthorized());

        // With valid secret -> 200 OK
        mockMvc.perform(get("/bootstrap/status")
                        .header(BOOTSTRAP_SECRET_HEADER, bootstrapProperties.getSecret()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
