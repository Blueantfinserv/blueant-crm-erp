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
import org.springframework.data.redis.core.RedisTemplate;
import com.blueant_crm_erp.common.event.NotificationEvent;

import java.time.Duration;
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

    @org.springframework.boot.test.mock.mockito.MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.data.redis.core.ValueOperations<String, Object> valueOperations;

    private final java.util.concurrent.ConcurrentHashMap<String, Object> mockRedisStore = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, Long> mockRedisExpiry = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    private TestNotificationEventListener testNotificationEventListener;

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfig {
        @org.springframework.context.annotation.Bean
        public TestNotificationEventListener testNotificationEventListener() {
            return new TestNotificationEventListener();
        }
    }

    public static class TestNotificationEventListener {
        private final List<NotificationEvent> events = new java.util.concurrent.CopyOnWriteArrayList<>();

        @org.springframework.context.event.EventListener
        public void handle(NotificationEvent event) {
            events.add(event);
        }

        public void clear() {
            events.clear();
        }

        public List<NotificationEvent> getEvents() {
            return events;
        }
    }

    private User testUser;
    private Role testRole;
    private Department testDept;
    private Designation testDesig;
    private Team testTeam;

    private void setUpRedisMock() {
        mockRedisStore.clear();
        mockRedisExpiry.clear();

        org.mockito.Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Stub increment
        org.mockito.Mockito.when(valueOperations.increment(org.mockito.Mockito.anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            checkExpiry(key);
            Object val = mockRedisStore.get(key);
            long nextVal = 1;
            if (val instanceof Number) {
                nextVal = ((Number) val).longValue() + 1;
            } else if (val instanceof String) {
                nextVal = Long.parseLong((String) val) + 1;
            }
            mockRedisStore.put(key, nextVal);
            return nextVal;
        });

        // Stub setIfAbsent
        org.mockito.Mockito.when(valueOperations.setIfAbsent(org.mockito.Mockito.anyString(), org.mockito.Mockito.any(), org.mockito.Mockito.any(Duration.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            Duration duration = invocation.getArgument(2);
            checkExpiry(key);
            if (mockRedisStore.containsKey(key)) {
                return false;
            }
            mockRedisStore.put(key, value);
            mockRedisExpiry.put(key, System.currentTimeMillis() + duration.toMillis());
            return true;
        });

        // Stub set
        org.mockito.Mockito.doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            Duration duration = invocation.getArgument(2);
            mockRedisStore.put(key, value);
            mockRedisExpiry.put(key, System.currentTimeMillis() + duration.toMillis());
            return null;
        }).when(valueOperations).set(org.mockito.Mockito.anyString(), org.mockito.Mockito.any(), org.mockito.Mockito.any(Duration.class));

        // Stub expire
        org.mockito.Mockito.when(redisTemplate.expire(org.mockito.Mockito.anyString(), org.mockito.Mockito.any(Duration.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Duration duration = invocation.getArgument(1);
            if (mockRedisStore.containsKey(key)) {
                mockRedisExpiry.put(key, System.currentTimeMillis() + duration.toMillis());
                return true;
            }
            return false;
        });

        // Stub delete single key
        org.mockito.Mockito.when(redisTemplate.delete(org.mockito.Mockito.anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            boolean existed = mockRedisStore.remove(key) != null;
            mockRedisExpiry.remove(key);
            return existed;
        });

        // Stub delete collection
        org.mockito.Mockito.when(redisTemplate.delete(org.mockito.Mockito.any(java.util.Collection.class))).thenAnswer(invocation -> {
            java.util.Collection<String> keysToDelete = invocation.getArgument(0);
            long count = 0;
            if (keysToDelete != null) {
                for (String key : keysToDelete) {
                    if (mockRedisStore.remove(key) != null) {
                        count++;
                    }
                    mockRedisExpiry.remove(key);
                }
            }
            return count;
        });

        // Stub hasKey
        org.mockito.Mockito.when(redisTemplate.hasKey(org.mockito.Mockito.anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            checkExpiry(key);
            return mockRedisStore.containsKey(key);
        });

        // Stub keys
        org.mockito.Mockito.when(redisTemplate.keys(org.mockito.Mockito.anyString())).thenAnswer(invocation -> {
            String pattern = invocation.getArgument(0);
            String regex = pattern.replace("*", ".*");
            java.util.Set<String> matched = new java.util.HashSet<>();
            for (String key : mockRedisStore.keySet()) {
                checkExpiry(key);
                if (mockRedisStore.containsKey(key) && key.matches(regex)) {
                    matched.add(key);
                }
            }
            return matched;
        });
    }

    private void checkExpiry(String key) {
        Long expiry = mockRedisExpiry.get(key);
        if (expiry != null && expiry < System.currentTimeMillis()) {
            mockRedisStore.remove(key);
            mockRedisExpiry.remove(key);
        }
    }

    @BeforeEach
    public void setUp() {
        setUpRedisMock();

        // Clear redis keys related to rate limits and locks
        java.util.Set<String> keys = redisTemplate.keys("rate-limit:forgot-password:*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
        java.util.Set<String> lockKeys = redisTemplate.keys("lock:forgot-password:*");
        if (lockKeys != null && !lockKeys.isEmpty()) redisTemplate.delete(lockKeys);
        testNotificationEventListener.clear();

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
    public void testD_MultipleResetRequests_KeepsOriginalActive() throws Exception {
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

        // The first token should STILL be active (not marked as used)
        PasswordResetToken updatedFirstToken = passwordResetTokenRepository.findById(firstToken.getId()).orElseThrow();
        assertFalse(updatedFirstToken.getUsed());

        List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertEquals(1, activeTokens.size());
        assertEquals(firstToken.getId(), activeTokens.get(0).getId());
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

    @Test
    public void testCooldownProtection() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        // 1st request -> generates OTP, sends email
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<PasswordResetToken> tokensBefore = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertEquals(1, tokensBefore.size());
        assertEquals(1, testNotificationEventListener.getEvents().size());

        // We consume the token so it's not active anymore, leaving only cooldown as the blocker
        tokensBefore.forEach(t -> {
            t.setUsed(true);
            passwordResetTokenRepository.save(t);
        });

        // 2nd request (within cooldown) -> returns success but no OTP generated, no email sent
        testNotificationEventListener.clear();
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertTrue(passwordResetTokenRepository.findByUserAndUsedFalse(testUser).isEmpty());
        assertTrue(testNotificationEventListener.getEvents().isEmpty());
    }

    @Test
    public void testActiveOtpReuseProtection() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        // 1st request -> generates OTP, sends email
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(1, passwordResetTokenRepository.findByUserAndUsedFalse(testUser).size());
        assertEquals(1, testNotificationEventListener.getEvents().size());

        // Remove cooldown key to isolate "active OTP" check
        redisTemplate.delete("lock:forgot-password:cooldown:" + testUser.getEmployeeCode().toLowerCase());

        // 2nd request (with active OTP still valid) -> returns success, no new OTP generated, no email sent
        testNotificationEventListener.clear();
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(1, passwordResetTokenRepository.findByUserAndUsedFalse(testUser).size());
        assertTrue(testNotificationEventListener.getEvents().isEmpty());
    }

    @Test
    public void testOtpExpiryAllowsNewGeneration() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        // 1st request -> generates OTP
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<PasswordResetToken> tokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertEquals(1, tokens.size());

        // Manually expire the token in DB and remove cooldown
        PasswordResetToken token = tokens.get(0);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(5));
        passwordResetTokenRepository.saveAndFlush(token);
        redisTemplate.delete("lock:forgot-password:cooldown:" + testUser.getEmployeeCode().toLowerCase());

        // 2nd request -> allows generating a new OTP
        testNotificationEventListener.clear();
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Should have 2 unused tokens total (1 expired, 1 active new one)
        List<PasswordResetToken> newActiveTokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertEquals(2, newActiveTokens.size());

        boolean hasNewToken = newActiveTokens.stream()
                .anyMatch(t -> !t.getId().equals(token.getId()) && t.getExpiresAt().isAfter(LocalDateTime.now()));
        assertTrue(hasNewToken);
        assertEquals(1, testNotificationEventListener.getEvents().size());
    }

    @Test
    public void testAccountRateLimiting() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        // Trigger requests up to maxRequestsPerHour (default: 5)
        for (int i = 0; i < 5; i++) {
            // Clear lock and cooldown so request is not suppressed by lock/cooldown/active token checks
            redisTemplate.delete("lock:forgot-password:user:" + testUser.getEmployeeCode().toLowerCase());
            redisTemplate.delete("lock:forgot-password:cooldown:" + testUser.getEmployeeCode().toLowerCase());
            List<PasswordResetToken> tokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
            tokens.forEach(t -> {
                t.setUsed(true);
                passwordResetTokenRepository.save(t);
            });

            mockMvc.perform(post("/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        // The 6th request should hit account rate limit (429)
        redisTemplate.delete("lock:forgot-password:user:" + testUser.getEmployeeCode().toLowerCase());
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    public void testIpRateLimiting() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        // Target IP limit: 20 per hour
        for (int i = 0; i < 20; i++) {
            // Delete lock, cooldown, and account-level limit keys to isolate IP limit
            redisTemplate.delete("lock:forgot-password:user:" + testUser.getEmployeeCode().toLowerCase());
            redisTemplate.delete("lock:forgot-password:cooldown:" + testUser.getEmployeeCode().toLowerCase());
            redisTemplate.delete("rate-limit:forgot-password:account:" + testUser.getEmployeeCode().toLowerCase());
            List<PasswordResetToken> tokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
            tokens.forEach(t -> {
                t.setUsed(true);
                passwordResetTokenRepository.save(t);
            });

            mockMvc.perform(post("/auth/forgot-password")
                    .with(req -> {
                        req.setRemoteAddr("192.168.1.100");
                        return req;
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        // The 21st request from same IP should hit IP rate limit (429)
        redisTemplate.delete("lock:forgot-password:user:" + testUser.getEmployeeCode().toLowerCase());
        mockMvc.perform(post("/auth/forgot-password")
                .with(req -> {
                    req.setRemoteAddr("192.168.1.100");
                    return req;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    public void testForgotPassword_RedisUnavailable_Throws503() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        // Stub RedisTemplate to throw a Connection Failure Exception when incrementing IP key
        org.mockito.Mockito.when(redisTemplate.opsForValue()).thenAnswer(invocation -> {
            throw new org.springframework.data.redis.RedisConnectionFailureException("Unable to connect to Redis server");
        });

        // The request should return HTTP 503 (Service Unavailable)
        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.errorType").value("CACHE"))
                .andExpect(jsonPath("$.message").value("Service is temporarily unavailable. Please try again later."));
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
