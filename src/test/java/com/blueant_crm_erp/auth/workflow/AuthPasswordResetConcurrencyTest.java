package com.blueant_crm_erp.auth.workflow;

import com.blueant_crm_erp.auth.dto.request.ForgotPasswordRequest;
import com.blueant_crm_erp.auth.entity.PasswordResetToken;
import com.blueant_crm_erp.auth.repository.PasswordResetTokenRepository;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.common.event.NotificationEvent;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthPasswordResetConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

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
        org.mockito.Mockito.when(valueOperations.setIfAbsent(org.mockito.Mockito.anyString(), org.mockito.Mockito.any(), org.mockito.Mockito.any(java.time.Duration.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            java.time.Duration duration = invocation.getArgument(2);
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
            java.time.Duration duration = invocation.getArgument(2);
            mockRedisStore.put(key, value);
            mockRedisExpiry.put(key, System.currentTimeMillis() + duration.toMillis());
            return null;
        }).when(valueOperations).set(org.mockito.Mockito.anyString(), org.mockito.Mockito.any(), org.mockito.Mockito.any(java.time.Duration.class));

        // Stub expire
        org.mockito.Mockito.when(redisTemplate.expire(org.mockito.Mockito.anyString(), org.mockito.Mockito.any(java.time.Duration.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            java.time.Duration duration = invocation.getArgument(1);
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

        String suffix = String.valueOf(System.currentTimeMillis()).substring(5) + "c";
        String numSuffix = String.valueOf(System.currentTimeMillis()).substring(5);
        String mobile = "9" + numSuffix;
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

    @AfterEach
    public void tearDown() {
        if (testUser != null && testUser.getId() != null) {
            List<PasswordResetToken> tokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
            passwordResetTokenRepository.deleteAll(tokens);
            List<PasswordResetToken> allTokens = passwordResetTokenRepository.findAll();
            allTokens.stream()
                     .filter(t -> t.getUser().getId().equals(testUser.getId()))
                     .forEach(t -> passwordResetTokenRepository.delete(t));

            userRepository.delete(testUser);
        }
        java.util.Set<String> keys = redisTemplate.keys("rate-limit:forgot-password:*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
        java.util.Set<String> lockKeys = redisTemplate.keys("lock:forgot-password:*");
        if (lockKeys != null && !lockKeys.isEmpty()) redisTemplate.delete(lockKeys);
    }

    @Test
    public void testConcurrentForgotPasswordSafety() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .employeeCode(testUser.getEmployeeCode())
                .email(testUser.getEmail())
                .mobileNumber(testUser.getMobileNumber())
                .build();

        int threadCount = 8;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch doneLatch = new java.util.concurrent.CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    mockMvc.perform(post("/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release the threads simultaneously
        latch.countDown();
        doneLatch.await();
        executor.shutdown();

        // Verify only 1 token was created in the database
        List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findByUserAndUsedFalse(testUser);
        assertEquals(1, activeTokens.size());

        // Verify exactly one email notification event was published
        assertEquals(1, testNotificationEventListener.getEvents().size());
    }
}
