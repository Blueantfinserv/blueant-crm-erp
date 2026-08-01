package com.blueant_crm_erp.user.service;

import com.blueant_crm_erp.user.dto.request.ChangePasswordRequest;
import com.blueant_crm_erp.user.dto.request.UpdateUserProfileRequest;
import com.blueant_crm_erp.user.dto.response.UserProfileResponse;

/**
 * =============================================================================
 * User Profile Service
 * =============================================================================
 *
 * Business operations related to logged-in user's profile.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Get Logged-in User Profile
 * • Update Own Profile
 * • Change Own Password
 * • Upload Profile Picture
 * • Remove Profile Picture
 *
 * NOTE:
 * This service is only for self-profile management.
 * Admin user management operations belong to UserService.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * Hierarchy
 * -----------------------------------------------------------------------------
 * Rohit
 *    ↓
 * Sales Manager
 *    ↓
 * Team Leader
 *    ↓
 * Sales Person
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
public interface UserProfileService {

    /**
     * Get logged-in user profile.
     *
     * @return user profile
     */
    UserProfileResponse getMyProfile();

    /**
     * Update logged-in user profile.
     *
     * @param request profile update request
     * @return updated profile
     */
    UserProfileResponse updateMyProfile(
            UpdateUserProfileRequest request
    );

    /**
     * Change logged-in user password.
     *
     * @param request change password request
     */
    void changeMyPassword(
            ChangePasswordRequest request
    );

    /**
     * Upload profile photo.
     *
     * @param profilePhotoUrl uploaded photo URL
     * @return updated profile
     */
    UserProfileResponse updateProfilePhoto(
            String profilePhotoUrl
    );

    /**
     * Remove profile photo.
     *
     * @return updated profile
     */
    UserProfileResponse removeProfilePhoto();

}