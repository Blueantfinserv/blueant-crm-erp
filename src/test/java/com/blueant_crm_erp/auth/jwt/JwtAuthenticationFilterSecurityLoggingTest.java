package com.blueant_crm_erp.auth.jwt;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.blueant_crm_erp.config.properties.JwtProperties;
import com.blueant_crm_erp.exception.handler.GlobalExceptionHandler;
import com.blueant_crm_erp.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Security regression test to ensure that credentials (JWT tokens, Authorization headers,
 * passwords, OTPs) are NEVER emitted in application logs.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterSecurityLoggingTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ListAppender<ILoggingEvent> filterListAppender;
    private Logger filterLogger;

    private ListAppender<ILoggingEvent> providerListAppender;
    private Logger providerLogger;

    private ListAppender<ILoggingEvent> exceptionListAppender;
    private Logger exceptionLogger;

    @BeforeEach
    void setUp() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        filterLogger = (Logger) LoggerFactory.getLogger(JwtAuthenticationFilter.class);
        filterListAppender = new ListAppender<>();
        filterListAppender.start();
        filterLogger.addAppender(filterListAppender);
        filterLogger.setLevel(Level.DEBUG);

        providerLogger = (Logger) LoggerFactory.getLogger(JwtTokenProvider.class);
        providerListAppender = new ListAppender<>();
        providerListAppender.start();
        providerLogger.addAppender(providerListAppender);
        providerLogger.setLevel(Level.DEBUG);

        exceptionLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        exceptionListAppender = new ListAppender<>();
        exceptionListAppender.start();
        exceptionLogger.addAppender(exceptionListAppender);
        exceptionLogger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        if (filterLogger != null && filterListAppender != null) {
            filterLogger.detachAppender(filterListAppender);
        }
        if (providerLogger != null && providerListAppender != null) {
            providerLogger.detachAppender(providerListAppender);
        }
        if (exceptionLogger != null && exceptionListAppender != null) {
            exceptionLogger.detachAppender(exceptionListAppender);
        }
    }

    @Test
    @DisplayName("Should not emit raw token or Authorization header in logs during authentication")
    void testFilterDoesNotLogRawTokenOrAuthorizationHeader() throws ServletException, IOException {
        String secretToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.secretPayloadData.signature1234567890";
        String rawHeader = "Bearer " + secretToken;

        when(request.getRequestURI()).thenReturn("/api/v1/protected/resource");
        when(jwtTokenProvider.resolveToken(request)).thenReturn(secretToken);
        when(jwtTokenProvider.validateToken(secretToken)).thenReturn(true);
        when(jwtTokenProvider.extractUsername(secretToken)).thenReturn("EMP001");
        when(userDetailsService.loadUserByUsername("EMP001")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);

        for (ILoggingEvent event : filterListAppender.list) {
            String formattedMessage = event.getFormattedMessage();
            assertThat(formattedMessage)
                    .as("Log message must NOT contain raw token")
                    .doesNotContain(secretToken);
            assertThat(formattedMessage)
                    .as("Log message must NOT contain Bearer prefix with token")
                    .doesNotContain(rawHeader);
            assertThat(formattedMessage)
                    .as("Log message must NOT contain old leaky 'Authorization Header = [' pattern")
                    .doesNotContain("Authorization Header = [");
            assertThat(formattedMessage)
                    .as("Log message must NOT contain old leaky 'Resolved Token = [' pattern")
                    .doesNotContain("Resolved Token = [");
        }
    }

    @Test
    @DisplayName("Should emit only safe metadata when token is present and validated")
    void testFilterLogsOnlySafeMetadata() throws ServletException, IOException {
        String token = "valid-mock-token";

        when(request.getRequestURI()).thenReturn("/api/v1/leads");
        when(jwtTokenProvider.resolveToken(request)).thenReturn(token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.extractUsername(token)).thenReturn("RM001");
        when(userDetailsService.loadUserByUsername("RM001")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        boolean hasSafeMetadataLog = filterListAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("tokenPresent=true, valid=true"));

        assertThat(hasSafeMetadataLog)
                .as("Should log safe structured metadata: tokenPresent=true, valid=true")
                .isTrue();
    }

    @Test
    @DisplayName("Should not leak token or full stack trace on invalid token validation in JwtTokenProvider")
    void testJwtTokenProviderDoesNotLogRawTokenOnValidationFailure() {
        JwtProperties properties = new JwtProperties();
        // 256-bit base64 encoded secret key
        properties.setSecret("dGhpcy1pcy1hLXZlcnktc2VjdXJlLWFuZC1sb25nLXNlY3JldC1rZXktZm9yLXRlc3RpbmctcHVycG9zZXM=");
        JwtTokenProvider realProvider = new JwtTokenProvider(properties);
        realProvider.init();

        String malformedToken = "malformed.secret.token.xyz987654321";

        boolean valid = realProvider.validateToken(malformedToken);
        assertThat(valid).isFalse();

        for (ILoggingEvent event : providerListAppender.list) {
            String formattedMessage = event.getFormattedMessage();
            assertThat(formattedMessage)
                    .as("JwtTokenProvider must NOT log the invalid token string")
                    .doesNotContain(malformedToken);

            if (event.getThrowableProxy() != null) {
                assertThat(event.getThrowableProxy().getMessage())
                        .as("Throwable message must NOT contain the invalid token")
                        .doesNotContain(malformedToken);
            }
        }
    }

    @Test
    @DisplayName("Should mask password in GlobalExceptionHandler validation logging")
    void testGlobalExceptionHandlerMasksSensitiveFields() throws NoSuchMethodException {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(null);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "loginRequest");
        bindingResult.addError(new FieldError("loginRequest", "password", "SuperSecretPassword123!", false, null, null, "Password too short"));
        bindingResult.addError(new FieldError("loginRequest", "otp", "987654", false, null, null, "Invalid OTP"));
        bindingResult.addError(new FieldError("loginRequest", "employeeCode", "EMP01", false, null, null, "Invalid code"));

        MethodParameter parameter = new MethodParameter(
                JwtAuthenticationFilterSecurityLoggingTest.class.getDeclaredMethod("dummyMethod", String.class), 0
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        handler.handleValidationException(ex, request);

        for (ILoggingEvent event : exceptionListAppender.list) {
            String message = event.getFormattedMessage();
            assertThat(message)
                    .as("Password must NOT be logged in validation error")
                    .doesNotContain("SuperSecretPassword123!");
            assertThat(message)
                    .as("OTP must NOT be logged in validation error")
                    .doesNotContain("987654");
        }

        boolean hasMaskedPassword = exceptionListAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("Field: password")
                        && event.getFormattedMessage().contains("Rejected Value: [PROTECTED]"));
        assertThat(hasMaskedPassword).isTrue();
    }

    @Test
    @DisplayName("User entity toString should exclude password field")
    void testUserToStringExcludesPassword() {
        User user = User.builder()
                .employeeCode("BA001")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("ConfidentialHash$2a$12$secret")
                .build();

        String str = user.toString();
        assertThat(str)
                .as("User.toString() must NOT contain raw password or hash")
                .doesNotContain("ConfidentialHash$2a$12$secret");
        assertThat(str)
                .as("User.toString() must NOT include password property")
                .doesNotContain("password=");
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String param) {
    }
}
