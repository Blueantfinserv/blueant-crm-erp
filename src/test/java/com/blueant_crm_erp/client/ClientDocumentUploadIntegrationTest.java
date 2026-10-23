package com.blueant_crm_erp.client;

import com.blueant_crm_erp.auth.security.CustomUserDetailsService;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.handler.ApiError;
import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.entity.RolePermission;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.DesignationRepository;
import com.blueant_crm_erp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class ClientDocumentUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private com.blueant_crm_erp.user.repository.TeamRepository teamRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private User crmUser;
    private UserDetails crmPrincipal;
    private com.blueant_crm_erp.user.entity.Team defaultTeam;

    @BeforeEach
    void setUp() {
        Department crmDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_CRM)
                .orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        defaultTeam = teamRepository.findAll().stream().findFirst().orElse(null);

        crmUser = createTestUser("CRM_DOC_USER", BootstrapConstants.ROLE_CRM_ONBOARDING, BootstrapConstants.DESIG_CRM_ONBOARDING, crmDept, "PHYSICAL_LEAD_ASSIGN");
        crmPrincipal = customUserDetailsService.loadUserByUsername(crmUser.getEmployeeCode());
    }

    private User createTestUser(String prefix, String roleCode, String desigCode, Department dept, String... permissions) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Role role = roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(roleCode).name(roleCode).status(Status.ACTIVE).systemRole(true).defaultRole(false).build()));

        for (String permCode : permissions) {
            Permission perm = permissionRepository.findByCodeIgnoreCase(permCode)
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .code(permCode).name(permCode).module("TEST").status(Status.ACTIVE).build()));
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
                .status(Status.ACTIVE)
                .accountEnabled(true)
                .department(dept)
                .designation(desig)
                .role(role)
                .team(defaultTeam)
                .gender(com.blueant_crm_erp.common.enums.Gender.MALE)
                .build());
    }

    @Test
    @DisplayName("1. Valid document upload under 10MB limit succeeds with HTTP 201 Created")
    void test1_validDocumentUploadSuccess() throws Exception {
        byte[] pdfContent = "%PDF-1.4 sample valid pdf content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "pan_card.pdf", "application/pdf", pdfContent);

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(pdfFile)
                        .param("documentType", "PAN_KYC")
                        .with(user(crmPrincipal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("KYC document uploaded successfully"))
                .andExpect(jsonPath("$.data.documentReference").isNotEmpty())
                .andExpect(jsonPath("$.data.fileName").value("pan_card.pdf"))
                .andExpect(jsonPath("$.data.documentType").value("PAN_KYC"))
                .andExpect(jsonPath("$.data.uploadStatus").value("SUCCESS"));
    }

    @Test
    @DisplayName("2. Valid document upload with optional documentType omitted succeeds with HTTP 201 Created")
    void test2_validDocumentUploadWithoutDocumentTypeSuccess() throws Exception {
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        MockMultipartFile jpgFile = new MockMultipartFile(
                "file", "id_proof.jpg", "image/jpeg", jpegContent);

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(jpgFile)
                        .with(user(crmPrincipal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentReference").isNotEmpty())
                .andExpect(jsonPath("$.data.fileName").value("id_proof.jpg"))
                .andExpect(jsonPath("$.data.documentType").isEmpty())
                .andExpect(jsonPath("$.data.uploadStatus").value("SUCCESS"));
    }

    @Test
    @DisplayName("3. Oversized file exceeding 10MB limit is rejected with HTTP 400 Bad Request")
    void test3_oversizedFileRejected() throws Exception {
        byte[] oversizedContent = new byte[10 * 1024 * 1024 + 512];
        System.arraycopy("%PDF-".getBytes(StandardCharsets.UTF_8), 0, oversizedContent, 0, 5);

        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large_document.pdf", "application/pdf", oversizedContent);

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(largeFile)
                        .param("documentType", "PAN_KYC")
                        .with(user(crmPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("File size exceeds maximum allowed limit of 10MB.")));
    }

    @Test
    @DisplayName("4. Missing multipart 'file' field returns HTTP 400 Bad Request instead of HTTP 500")
    void test4_missingMultipartFilePartRejected() throws Exception {
        // Send multipart request without the required 'file' part (providing wrong part name)
        MockMultipartFile wrongPart = new MockMultipartFile(
                "wrongField", "document.pdf", "application/pdf", "%PDF-1.4".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(wrongPart)
                        .param("documentType", "PAN_KYC")
                        .with(user(crmPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("file")));
    }

    @Test
    @DisplayName("5. Error response traceId reuses MDC correlationId and matches X-Correlation-Id header")
    void test5_correlationIdReusedInErrorResponse() throws Exception {
        String testCorrelationId = "custom-test-trace-" + UUID.randomUUID();

        // Send an invalid request with X-Correlation-Id
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(emptyFile)
                        .header("X-Correlation-Id", testCorrelationId)
                        .with(user(crmPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", testCorrelationId))
                .andExpect(jsonPath("$.traceId").value(testCorrelationId))
                .andExpect(jsonPath("$.message").value("Uploaded file cannot be empty."));
    }

    @Test
    @DisplayName("6. When X-Correlation-Id header is omitted, generated traceId matches response X-Correlation-Id header")
    void test6_generatedCorrelationIdMatchesHeader() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        MvcResult result = mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(emptyFile)
                        .with(user(crmPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andReturn();

        String responseCorrIdHeader = result.getResponse().getHeader("X-Correlation-Id");
        assertThat(responseCorrIdHeader).isNotBlank();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("\"traceId\":\"" + responseCorrIdHeader + "\"");
    }

    @Test
    @DisplayName("7. Existing document security validation rejects disallowed extensions and spoofed signatures")
    void test7_disallowedExtensionAndSpoofedSignatureRejected() throws Exception {
        // Disallowed extension (.exe)
        MockMultipartFile exeFile = new MockMultipartFile(
                "file", "payload.exe", "application/octet-stream", "MZ dummy binary content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(exeFile)
                        .with(user(crmPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Unsupported file type")));

        // Spoofed signature (.pdf name but plain text content)
        MockMultipartFile fakePdf = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf", "Plain text not a real PDF".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/v1/clients/documents/upload")
                        .file(fakePdf)
                        .with(user(crmPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid file signature")));
    }

    @Autowired
    private com.blueant_crm_erp.exception.handler.GlobalExceptionHandler globalExceptionHandler;

    @Test
    @DisplayName("8. GlobalExceptionHandler specifically maps MaxUploadSizeExceededException to safe HTTP 400 Bad Request")
    void test8_maxUploadSizeExceededExceptionHandledSafely() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/v1/clients/documents/upload");
        request.setMethod("POST");

        String correlationId = "corr-" + UUID.randomUUID();
        org.slf4j.MDC.put("correlationId", correlationId);

        try {
            MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(10 * 1024 * 1024);
            org.springframework.http.ResponseEntity<ApiError> response =
                    globalExceptionHandler.handleMaxUploadSizeExceeded(ex, request);

            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("File size exceeds maximum allowed limit of 10MB.");
            assertThat(response.getBody().traceId()).isEqualTo(correlationId);
            assertThat(response.getBody().path()).isEqualTo("/api/v1/clients/documents/upload");
            assertThat(response.getBody().errorCode()).isEqualTo(com.blueant_crm_erp.exception.base.ErrorCode.VALIDATION_FAILED);
        } finally {
            org.slf4j.MDC.remove("correlationId");
        }
    }

    @Test
    @DisplayName("9. GlobalExceptionHandler specifically maps MissingServletRequestPartException to safe HTTP 400 Bad Request")
    void test9_missingServletRequestPartExceptionHandledSafely() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/v1/clients/documents/upload");
        request.setMethod("POST");

        String correlationId = "corr-part-" + UUID.randomUUID();
        org.slf4j.MDC.put("correlationId", correlationId);

        try {
            org.springframework.web.multipart.support.MissingServletRequestPartException ex =
                    new org.springframework.web.multipart.support.MissingServletRequestPartException("file");
            org.springframework.http.ResponseEntity<ApiError> response =
                    globalExceptionHandler.handleMissingServletRequestPart(ex, request);

            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("Required multipart request part 'file' is missing.");
            assertThat(response.getBody().traceId()).isEqualTo(correlationId);
        } finally {
            org.slf4j.MDC.remove("correlationId");
        }
    }

    @Test
    @DisplayName("10. GlobalExceptionHandler specifically maps MultipartException to safe HTTP 400 Bad Request")
    void test10_multipartExceptionHandledSafely() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/api/v1/clients/documents/upload");
        request.setMethod("POST");

        org.springframework.web.multipart.MultipartException ex =
                new org.springframework.web.multipart.MultipartException("Malformed boundary");
        org.springframework.http.ResponseEntity<ApiError> response =
                globalExceptionHandler.handleMultipartException(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Failed to process multipart request. Ensure a valid multipart/form-data payload is provided.");
    }
}
