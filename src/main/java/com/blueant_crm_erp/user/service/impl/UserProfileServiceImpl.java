package com.blueant_crm_erp.user.service.impl;

import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.user.dto.request.ChangePasswordRequest;
import com.blueant_crm_erp.user.dto.request.UpdateUserProfileRequest;
import com.blueant_crm_erp.user.dto.response.UserProfileResponse;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.mapper.UserMapper;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.user.service.UserProfileService;
import com.blueant_crm_erp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository  userRepository;
    private final UserMapper      userMapper;
    private final PasswordEncoder passwordEncoder;

    // =========================================================================
    // Query
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        log.info("Fetching my profile");
        return userMapper.toProfileResponse(getCurrentUser());
    }

    // =========================================================================
    // Update Profile
    // =========================================================================

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateUserProfileRequest request) {
        log.info("Updating my profile");
        User user = getCurrentUser();
        userMapper.updateProfile(request, user);
        user = userRepository.save(user);
        log.info("Successfully updated my profile");
        return userMapper.toProfileResponse(user);
    }

    // =========================================================================
    // Password Management
    // =========================================================================

    @Override
    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        log.info("Changing my password");
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Old password does not match.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as the old password.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
        log.info("Successfully changed my password");
    }

    // =========================================================================
    // Profile Photo
    // =========================================================================

    @Override
    @Transactional
    public UserProfileResponse updateProfilePhoto(String profilePhotoUrl) {
        log.info("Updating profile photo");
        User user = getCurrentUser();
        user.setProfileImage(profilePhotoUrl);
        user = userRepository.save(user);
        log.info("Successfully updated profile photo");
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse removeProfilePhoto() {
        log.info("Removing profile photo");
        User user = getCurrentUser();
        user.setProfileImage(null);
        user = userRepository.save(user);
        log.info("Successfully removed profile photo");
        return userMapper.toProfileResponse(user);
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    /**
     * Resolves the currently authenticated user from the database.
     * Attempts lookup by email first, then falls back to employee code.
     *
     * @throws ResourceNotFoundException if the principal cannot be found
     */
    private User getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();
        return userRepository.findByEmailIgnoreCaseAndDeletedFalse(username)
                .orElseGet(() -> userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found.")));
    }
}
