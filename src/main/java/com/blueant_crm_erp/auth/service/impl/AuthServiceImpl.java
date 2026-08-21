package com.blueant_crm_erp.auth.service.impl;

import com.blueant_crm_erp.auth.dto.request.*;
import com.blueant_crm_erp.auth.dto.response.*;
import com.blueant_crm_erp.auth.entity.RefreshToken;
import com.blueant_crm_erp.auth.entity.PasswordResetToken;
import com.blueant_crm_erp.auth.repository.PasswordResetTokenRepository;
import com.blueant_crm_erp.auth.event.*;
import com.blueant_crm_erp.auth.jwt.JwtTokenProvider;
import com.blueant_crm_erp.common.event.NotificationEvent;
import java.util.Optional;
import com.blueant_crm_erp.auth.mapper.AuthMapper;
import com.blueant_crm_erp.auth.repository.RefreshTokenRepository;
import com.blueant_crm_erp.auth.security.CustomUserDetails;
import com.blueant_crm_erp.auth.service.AuthService;
import com.blueant_crm_erp.auth.validator.AuthValidator;
import com.blueant_crm_erp.config.properties.JwtProperties;
import com.blueant_crm_erp.config.properties.AppProperties;
import com.blueant_crm_erp.exception.auth.InvalidCredentialsException;
import com.blueant_crm_erp.exception.auth.InvalidTokenException;
import com.blueant_crm_erp.exception.auth.RefreshTokenExpiredException;
import com.blueant_crm_erp.exception.auth.UnauthorizedException;
import com.blueant_crm_erp.exception.common.RateLimitExceededException;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.util.network.IpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthValidator authValidator;
    private final AuthMapper authMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtProperties jwtProperties;
    private final AppProperties appProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Processing login request for user: {}", request.getEmployeeCode());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmployeeCode(), request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("Invalid credentials for user: {}", request.getEmployeeCode());
            eventPublisher.publishEvent(LoginFailureEvent.builder()
                    .username(request.getEmployeeCode())
                    .reason("Invalid credentials")
                    .loginTime(LocalDateTime.now())
                    .build());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Generate Tokens
        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, sessionId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Save Refresh Token
        LocalDateTime expiryDate = LocalDateTime.now().plus(java.time.Duration.ofMillis(jwtProperties.getRefreshExpirationMs()));
        RefreshToken tokenEntity = RefreshToken.builder()
                .sessionId(sessionId)
                .token(refreshToken)
                .user(user)
                .deviceId(request.getDeviceId())
                .deviceName(request.getDeviceName())
                .deviceType(request.getDeviceType())
                .browser(request.getBrowser())
                .operatingSystem(request.getOperatingSystem())
                .ipAddress(null)
                .expiryDate(expiryDate)
                .lastActivityAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(tokenEntity);

        // Update User
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Publish Events
        eventPublisher.publishEvent(LoginSuccessEvent.builder()
                .userId(user.getId())
                .employeeCode(user.getEmployeeCode())
                .email(user.getEmail())
                .loginTime(LocalDateTime.now())
                .ipAddress(null)
                .deviceName(request.getDeviceName())
                .browser(request.getBrowser())
                .operatingSystem(request.getOperatingSystem())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());

        eventPublisher.publishEvent(RefreshTokenCreatedEvent.builder()
                .userId(user.getId())
                .employeeCode(user.getEmployeeCode())
                .email(user.getEmail())
                .refreshToken(refreshToken)
                .expiryDate(expiryDate)
                .createdAt(LocalDateTime.now())
                .ipAddress(null)
                .deviceName(request.getDeviceName())
                .browser(request.getBrowser())
                .operatingSystem(request.getOperatingSystem())
                .build());

        LoginResponse loginResponse = authMapper.toLoginResponse(user);
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setTokenType(jwtProperties.getPrefix());
        loginResponse.setExpiresIn(jwtProperties.getExpirationMs() / 1000);
        loginResponse.setRefreshTokenExpiry(expiryDate);
        loginResponse.setSessionId(sessionId);

        return loginResponse;
    }

    @Override
    public LoginResponse verifyLoginOtp(VerifyLoginOtpRequest request) {
        throw new UnauthorizedException("OTP Verification is not yet implemented.");
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken oldTokenEntity = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is not in database!"));

        if (oldTokenEntity.getExpiryDate().isBefore(LocalDateTime.now()) || Boolean.TRUE.equals(oldTokenEntity.getRevoked())) {
            refreshTokenRepository.delete(oldTokenEntity);
            throw new RefreshTokenExpiredException("Refresh token was expired or revoked.");
        }

        User user = oldTokenEntity.getUser();
        CustomUserDetails userDetails = getCustomUserDetails(user);

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails, oldTokenEntity.getSessionId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Update existing refresh token instead of deleting and inserting
        LocalDateTime newExpiryDate = LocalDateTime.now().plus(java.time.Duration.ofMillis(jwtProperties.getRefreshExpirationMs()));
        
        oldTokenEntity.setToken(newRefreshToken);
        oldTokenEntity.setExpiryDate(newExpiryDate);
        oldTokenEntity.setLastActivityAt(LocalDateTime.now());
        
        if (request.getDeviceId() != null) {
            oldTokenEntity.setDeviceId(request.getDeviceId());
        }

        refreshTokenRepository.save(oldTokenEntity);

        eventPublisher.publishEvent(RefreshTokenCreatedEvent.builder()
                .userId(user.getId())
                .employeeCode(user.getEmployeeCode())
                .email(user.getEmail())
                .refreshToken(newRefreshToken)
                .expiryDate(newExpiryDate)
                .createdAt(LocalDateTime.now())
                .ipAddress(oldTokenEntity.getIpAddress())
                .deviceName(oldTokenEntity.getDeviceName())
                .browser(oldTokenEntity.getBrowser())
                .operatingSystem(oldTokenEntity.getOperatingSystem())
                .build());

        RefreshTokenResponse response = authMapper.toRefreshTokenResponse(oldTokenEntity);
        response.setAccessToken(newAccessToken);
        response.setTokenType(jwtProperties.getPrefix());
        response.setExpiresIn(jwtProperties.getExpirationMs() / 1000);
        response.setSessionId(oldTokenEntity.getSessionId());

        return response;
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String refreshToken = request.getRefreshToken();
        refreshTokenRepository.findByToken(refreshToken).ifPresent(tokenEntity -> {
            refreshTokenRepository.delete(tokenEntity);
            eventPublisher.publishEvent(LogoutEvent.builder()
                    .userId(tokenEntity.getUser().getId())
                    .employeeCode(tokenEntity.getUser().getEmployeeCode())
                    .email(tokenEntity.getUser().getEmail())
                    .refreshToken(refreshToken)
                    .logoutBy("SELF")
                    .build());
        });
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        if (request == null || request.getEmployeeCode() == null) {
            log.warn("Password reset requested with null request or employee code.");
            return;
        }

        String employeeCode = request.getEmployeeCode().trim();
        String clientIp = IpUtil.getClientIp(httpServletRequest);
        log.info("Forgot password requested for: {} from IP: {}", employeeCode, clientIp);

        // Fetch properties
        AppProperties.PasswordReset resetProps = appProperties.getPasswordReset();
        int expiryMinutes = resetProps.getTokenExpiryMinutes();
        int cooldownSeconds = resetProps.getResendCooldownSeconds();
        int maxRequestsPerHour = resetProps.getMaxRequestsPerHour();
        int ipMaxRequestsPerHour = resetProps.getIpMaxRequestsPerHour();

        // 1. IP-level rate limiting
        String ipKey = "rate-limit:forgot-password:ip:" + clientIp;
        Long ipCount = redisTemplate.opsForValue().increment(ipKey);
        if (ipCount != null && ipCount == 1) {
            redisTemplate.expire(ipKey, Duration.ofHours(1));
        }
        if (ipCount != null && ipCount > ipMaxRequestsPerHour) {
            log.warn("IP-level rate limit exceeded for IP: {}. Requests this hour: {}", clientIp, ipCount);
            throw new RateLimitExceededException("Too many requests from this IP. Please try again later.");
        }

        // 2. Account-level rate limiting
        String accountKey = "rate-limit:forgot-password:account:" + employeeCode.toLowerCase();
        Long accountCount = redisTemplate.opsForValue().increment(accountKey);
        if (accountCount != null && accountCount == 1) {
            redisTemplate.expire(accountKey, Duration.ofHours(1));
        }
        if (accountCount != null && accountCount > maxRequestsPerHour) {
            log.warn("Account-level rate limit exceeded for employee: {}. Requests this hour: {}", employeeCode, accountCount);
            throw new RateLimitExceededException("Too many requests for this account. Please try again later.");
        }

        // 3. Prevent concurrency issues (distributed lock)
        String lockKey = "lock:forgot-password:user:" + employeeCode.toLowerCase();
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(10));
        if (!Boolean.TRUE.equals(lockAcquired)) {
            log.warn("Concurrent forgot password request processing for user: {}. Suppressing request.", employeeCode);
            return;
        }

        try {
            Optional<User> userOpt = userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(employeeCode);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String dbEmail = user.getEmail() != null ? user.getEmail().trim() : null;
                String dbMobile = user.getMobileNumber() != null ? user.getMobileNumber().trim() : null;

                if (user.isActive() &&
                        request.getEmail().trim().equalsIgnoreCase(dbEmail) &&
                        request.getMobileNumber().trim().equals(dbMobile)) {

                    // 4. Check if there's already an active, valid OTP in the DB
                    List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findByUserAndUsedFalse(user);
                    boolean hasActiveToken = activeTokens.stream()
                            .anyMatch(t -> t.getExpiresAt() != null && t.getExpiresAt().isAfter(LocalDateTime.now()));

                    if (hasActiveToken) {
                        log.info("Active valid OTP already exists for employee: {}. Suppressing new OTP creation.", employeeCode);
                        return;
                    }

                    // 5. Check if user is within resend cooldown
                    String cooldownKey = "lock:forgot-password:cooldown:" + employeeCode.toLowerCase();
                    Boolean hasCooldown = redisTemplate.hasKey(cooldownKey);
                    if (Boolean.TRUE.equals(hasCooldown)) {
                        log.info("Request for employee: {} is within resend cooldown. Suppressing request.", employeeCode);
                        return;
                    }

                    // Generate a 6-digit secure random OTP
                    java.security.SecureRandom secureRandom = new java.security.SecureRandom();
                    int otpVal = 100000 + secureRandom.nextInt(900000);
                    String rawToken = String.valueOf(otpVal);

                    // Compute SHA-256 hash and save
                    String tokenHash = hashToken(rawToken);
                    PasswordResetToken resetToken = PasswordResetToken.builder()
                            .user(user)
                            .tokenHash(tokenHash)
                            .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                            .used(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    passwordResetTokenRepository.save(resetToken);

                    // Set cooldown in Redis
                    redisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(cooldownSeconds));

                    // Send reset instructions via email
                    String resetUrl = resetProps.getFrontendResetUrl() + "?token=" + rawToken + "&employeeCode=" + user.getEmployeeCode();
                    String emailText = "Hello " + user.getFullName() + ",\n\n"
                            + "We received a request to reset your password for your BlueAnt CRM ERP account.\n"
                            + "Please use the following 6-digit One Time Password (OTP) to reset your password:\n\n"
                            + "OTP: " + rawToken + "\n\n"
                            + "Alternatively, you can complete the reset process using this link:\n"
                            + resetUrl + "\n\n"
                            + "This OTP is valid for " + expiryMinutes + " minutes.\n"
                            + "If you did not request a password reset, please ignore this email.\n\n"
                            + "Best regards,\n"
                            + "BlueAnt CRM ERP Team";

                    eventPublisher.publishEvent(new NotificationEvent(this, user.getEmail(), "Password Reset Request", emailText, "EMAIL"));
                    log.info("Sending password reset email for employee: {} to configured recipient", user.getEmployeeCode());
                } else {
                    log.warn("Password reset request inputs did not match user info for: {}", employeeCode);
                }
            } else {
                log.warn("Password reset requested for non-existing user: {}", employeeCode);
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password requested for employee code: {}", request.getEmployeeCode());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }

        String tokenHash = hashToken(request.getOtp().trim());
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenHashAndUsedFalse(tokenHash);

        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired password reset request.");
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now()) || !user.isActive()) {
            throw new IllegalArgumentException("Invalid or expired password reset request.");
        }

        if (!user.getEmployeeCode().equalsIgnoreCase(request.getEmployeeCode().trim())) {
            throw new IllegalArgumentException("Invalid or expired password reset request.");
        }

        // Update the user's password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setPasswordResetAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark the token as consumed
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        // Invalidate active refresh tokens
        refreshTokenRepository.deleteByUser(user);

        // Publish audit event
        eventPublisher.publishEvent(PasswordResetEvent.builder()
                .userId(user.getId())
                .employeeCode(user.getEmployeeCode())
                .email(user.getEmail())
                .resetAt(LocalDateTime.now())
                .resetBy("SELF")
                .build());

        log.info("Password reset successfully for user: {}", user.getEmployeeCode());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentAuthenticatedUser();

        authValidator.validateOldPassword(request.getCurrentPassword(), user.getPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenRepository.deleteByUser(user);

        eventPublisher.publishEvent(PasswordChangedEvent.builder()
                .userId(user.getId())
                .employeeCode(user.getEmployeeCode())
                .email(user.getEmail())
                .changedAt(LocalDateTime.now())
                .changedBy("SELF")
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        
        User user = userRepository.findProfileByEmployeeCodeIgnoreCaseAndDeletedFalse(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
                
        return authMapper.toCurrentUserResponse(user);
    }

    @Override
    public UserSessionResponse getCurrentSession() {
        return new UserSessionResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getActiveSessions() {
        User user = getCurrentAuthenticatedUser();
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevoked(user, false);

        return tokens.stream().map(authMapper::toUserSessionResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void logoutSession(String sessionId) {
        User user = getCurrentAuthenticatedUser();

            refreshTokenRepository.findBySessionId(sessionId).ifPresent(session -> {
                authValidator.validateSessionOwnership(session, user);
                refreshTokenRepository.delete(session);

                eventPublisher.publishEvent(LogoutEvent.builder()
                        .userId(user.getId())
                        .employeeCode(user.getEmployeeCode())
                        .email(user.getEmail())
                        .logoutTime(LocalDateTime.now())
                        .refreshToken(session.getToken())
                        .logoutBy("SELF")
                        .build());
            });
    }

    @Override
    @Transactional
    public void logoutAllDevices() {
        User user = getCurrentAuthenticatedUser();
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Helper to load the current authenticated User.
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        String currentUsername = authentication.getName();
        return userRepository.findByEmployeeCodeIgnoreCaseOrEmailIgnoreCaseOrMobileNumberAndDeletedFalse(
                currentUsername, currentUsername, currentUsername)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    /**
     * Helper to get CustomUserDetails manually.
     */
    private CustomUserDetails getCustomUserDetails(User user) {
        java.util.Set<org.springframework.security.core.GrantedAuthority> authorities = new java.util.HashSet<>();
        if (user.getRole() != null && com.blueant_crm_erp.common.enums.Status.ACTIVE.equals(user.getRole().getStatus())) {
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().getCode()));
        }
        return new CustomUserDetails(user, authorities);
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
