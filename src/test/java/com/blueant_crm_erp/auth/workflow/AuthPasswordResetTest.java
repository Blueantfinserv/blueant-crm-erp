package com.blueant_crm_erp.auth.workflow;

import com.blueant_crm_erp.auth.dto.request.ForgotPasswordRequest;
import com.blueant_crm_erp.auth.dto.request.ResetPasswordRequest;
import com.blueant_crm_erp.auth.entity.PasswordResetToken;
import com.blueant_crm_erp.auth.entity.RefreshToken;
import com.blueant_crm_erp.auth.repository.PasswordResetTokenRepository;
import com.blueant_crm_erp.auth.repository.RefreshTokenRepository;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthPasswordResetTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role testRole;
    private Department testDept;
    private Designation testDesig;
    private Team testTeam;

    @BeforeEach
    public void setUp() {
        testRole = roleRepository.findAll().get(0);
        testDept = departmentRepository.findAll().get(0);
        testDesig = designationRepository.findAll().get(0);
        testTeam = teamRepository.findAll().get(0);

        String suffix = String.valueOf(System.currentTimeMillis()).substring(5);
        String mobile = "9" + suffix;
        if (mobile.length() > 10) {
            mobile = mobile.substring(0, 10);
        } else if (mobile.length() < 10) {
            mobile = String.format("%-10s", mobile).replace(' ', '0');
        }

        testUser = User.builder()
                .employeeCode("EMP" + suffix)
                .firstName("Test")
                .lastName("User")
                .email("test_" + suffix + "@blueantcrm.com")
                .mobileNumber(mobile)
                .password(passwordEncoder.encode("OldPassword@123"))
                .gender(Gender.MALE)
                .joiningDate(LocalDate.now())
                .dateOfBirth(LocalDate.of(1995, 5, 10))
                .status(Status.ACTIVE)
                .role(testRole)
                .department(testDept)
                .designation(testDesig)
                .team(testTeam)
                .accountEnabled(true)
                .accountLocked(false)
                .firstLogin(false)
                .emailVerified(true)
                .mobileVerified(true)
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    public void testA_ValidResetRequest() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        List<PasswordResetToken> tokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertEquals(1, tokens.size());
        assertNotNull(tokens.get(0).getTokenHash());
        assertFalse(tokens.get(0).getUsed());
    }

    @Test
    public void testB_NonExistingUser_ReturnsSuccessButNoToken() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode("EMPNONEXISTENT")
                .email("nonexistent@blueantcrm.com")
                .mobileNumber("9999999999")
                .build();

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Returns generic success to prevent enumeration

        List<PasswordResetToken> tokens = passwordResetTokenRepository.findAll();
        // Should not contain any tokens for nonexistent employee
        boolean hasTokenForNonexistent = tokens.stream()
                .anyMatch(t -> t.getUser().getEmployeeCode().equals("EMPNONEXISTENT"));
        assertFalse(hasTokenForNonexistent);
    }

    @Test
    public void testC_InactiveUser_ReturnsSuccessButNoToken() throws Exception {
        testUser.setStatus(Status.INACTIVE);
        userRepository.save(testUser);

        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<PasswordResetToken> tokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertTrue(tokens.isEmpty());
    }

    @Test
    public void testD_MultipleResetRequests_InvalidatesPrevious() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        // First request
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<PasswordResetToken> firstTokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertEquals(1, firstTokens.size());
        PasswordResetToken firstToken = firstTokens.get(0);

        // Second request
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // The first token should now be marked as used
        PasswordResetToken updatedFirstToken = passwordResetTokenRepository.findById(firstToken.getId()).orElseThrow();
        assertTrue(updatedFirstToken.getUsed());

        List<PasswordResetToken> secondTokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertEquals(1, secondTokens.size());
    }

    @Test
    public void testE_TokenStoredAsHashOnly() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<PasswordResetToken> tokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertFalse(tokens.isEmpty());
        String storedHash = tokens.get(0).getTokenHash();
        
        // Assert the stored value is a valid SHA-256 hex string (64 characters)
        assertNotNull(storedHash);
        assertEquals(64, storedHash.length());
        assertTrue(storedHash.matches("^[a-f0-9]{64}$"));
    }

    @Test
    public void testF_ResetPassword_Success() throws Exception {
        // 1. Generate forgot password token
        ForgotPasswordRequest forgotReq = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotReq)))
                .andExpect(status().isOk());

        // Since it's an integration test with mocks, we grab the token from DB
        List<PasswordResetToken> tokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertEquals(1, tokens.size());
        
        String rawOtp = "123456";
        String hashedOtp = hashToken(rawOtp);
        
        // Invalidate the generated token and save a custom one with known raw value
        tokens.get(0).setUsed(true);
        passwordResetTokenRepository.save(tokens.get(0));

        PasswordResetToken testResetToken = PasswordResetToken.builder()
                .user(testUser)
                .tokenHash(hashedOtp)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        passwordResetTokenRepository.save(testResetToken);

        // 2. Call Reset Password
        ResetPasswordRequest resetReq = ResetPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .otp(rawOtp)
                .newPassword("NewSecurePassword@123")
                .confirmPassword("NewSecurePassword@123")
                .build();

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk());

        // Verify user password is updated and encrypted
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("NewSecurePassword@123", updatedUser.getPassword()));

        // Verify token is consumed
        PasswordResetToken consumedToken = passwordResetTokenRepository.findById(testResetToken.getId()).orElseThrow();
        assertTrue(consumedToken.getUsed());
        assertNotNull(consumedToken.getUsedAt());
    }

    @Test
    public void testG_ResetPassword_ExpiredToken_Fails() throws Exception {
        String rawOtp = "654321";
        String hashedOtp = hashToken(rawOtp);

        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .user(testUser)
                .tokenHash(hashedOtp)
                .expiresAt(LocalDateTime.now().minusMinutes(1)) // Expired
                .used(false)
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .build();
        passwordResetTokenRepository.save(expiredToken);

        ResetPasswordRequest resetReq = ResetPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .otp(rawOtp)
                .newPassword("NewSecurePassword@123")
                .confirmPassword("NewSecurePassword@123")
                .build();

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset request."));
    }

    @Test
    public void testH_ResetPassword_AlreadyUsedToken_Fails() throws Exception {
        String rawOtp = "987654";
        String hashedOtp = hashToken(rawOtp);

        PasswordResetToken usedToken = PasswordResetToken.builder()
                .user(testUser)
                .tokenHash(hashedOtp)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(true) // Already used
                .usedAt(LocalDateTime.now().minusMinutes(5))
                .createdAt(LocalDateTime.now())
                .build();
        passwordResetTokenRepository.save(usedToken);

        ResetPasswordRequest resetReq = ResetPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .otp(rawOtp)
                .newPassword("NewSecurePassword@123")
                .confirmPassword("NewSecurePassword@123")
                .build();

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset request."));
    }

    @Test
    public void testI_ResetPassword_MismatchedPasswords_Fails() throws Exception {
        ResetPasswordRequest resetReq = ResetPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .otp("111111")
                .newPassword("NewSecurePassword@123")
                .confirmPassword("MismatchedPassword@123")
                .build();

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("New password and confirm password do not match."));
    }

    @Test
    public void testJ_ResetPassword_InvalidTokenFormat_FailsValidation() throws Exception {
        ResetPasswordRequest resetReq = ResetPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .otp("abc1234") // Invalid format: must be 6-digit number
                .newPassword("NewSecurePassword@123")
                .confirmPassword("NewSecurePassword@123")
                .build();

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.otp").exists());
    }

    @Test
    public void testK_ResetPassword_InvalidatesSessions() throws Exception {
        // Create an active session/refresh token for this user
        RefreshToken refreshToken = RefreshToken.builder()
                .user(testUser)
                .token(UUID.randomUUID().toString())
                .sessionId(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        List<RefreshToken> activeTokensBefore = refreshTokenRepository.findByUser(testUser);
        assertFalse(activeTokensBefore.isEmpty());

        // Setup custom reset token
        String rawOtp = "135790";
        String hashedOtp = hashToken(rawOtp);

        PasswordResetToken testResetToken = PasswordResetToken.builder()
                .user(testUser)
                .tokenHash(hashedOtp)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        passwordResetTokenRepository.save(testResetToken);

        // Reset password
        ResetPasswordRequest resetReq = ResetPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .otp(rawOtp)
                .newPassword("NewSecurePassword@123")
                .confirmPassword("NewSecurePassword@123")
                .build();

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk());

        // Verify active sessions/refresh tokens are deleted
        List<RefreshToken> activeTokensAfter = refreshTokenRepository.findByUser(testUser);
        assertTrue(activeTokensAfter.isEmpty());
    }

    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
