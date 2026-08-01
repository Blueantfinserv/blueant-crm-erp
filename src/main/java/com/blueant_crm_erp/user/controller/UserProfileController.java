package com.blueant_crm_erp.user.controller;

import com.blueant_crm_erp.common.dto.response.ApiResponse;
import com.blueant_crm_erp.user.constant.UserConstants;
import com.blueant_crm_erp.user.dto.request.ChangePasswordRequest;
import com.blueant_crm_erp.user.dto.request.UpdateUserProfileRequest;
import com.blueant_crm_erp.user.dto.response.UserProfileResponse;
import com.blueant_crm_erp.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(UserConstants.API_BASE + UserConstants.PROFILE)
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs for the currently authenticated user's own profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    // =========================================================================
    // Query
    // =========================================================================

    @GetMapping
    @Operation(
            summary     = "Get Current User Profile",
            description = "Retrieves the full profile of the currently authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile returned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Authenticated user not found in database")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        log.info("REST request to get my profile");
        return ResponseEntity.ok(ApiResponse.success(userProfileService.getMyProfile()));
    }

    // =========================================================================
    // Update Profile
    // =========================================================================

    @PutMapping
    @Operation(
            summary     = "Update Current User Profile",
            description = "Updates the mutable profile fields of the currently authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody UpdateUserProfileRequest request) {
        log.info("REST request to update my profile");
        return ResponseEntity.ok(ApiResponse.success(userProfileService.updateMyProfile(request)));
    }

    // =========================================================================
    // Password
    // =========================================================================

    @PostMapping(UserConstants.CHANGE_PASSWORD)
    @Operation(
            summary     = "Change Password For Current User",
            description = "Changes the password for the currently authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Current password mismatch or new password same as old"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public ResponseEntity<ApiResponse<String>> changeMyPassword(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request Payload") @RequestBody ChangePasswordRequest request) {
        log.info("REST request to change my password");
        userProfileService.changeMyPassword(request);
        return ResponseEntity.ok(ApiResponse.success(UserConstants.PASSWORD_CHANGED));
    }

    // =========================================================================
    // Profile Photo
    // =========================================================================

    @PatchMapping("/photo")
    @Operation(
            summary     = "Update Profile Photo",
            description = "Sets a new profile photo URL for the currently authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile photo updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfilePhoto(
            @Parameter(description = "URL of the new profile photo") @RequestParam String profilePhotoUrl) {
        log.info("REST request to update profile photo");
        return ResponseEntity.ok(ApiResponse.success(userProfileService.updateProfilePhoto(profilePhotoUrl)));
    }

    @DeleteMapping("/photo")
    @Operation(
            summary     = "Remove Profile Photo",
            description = "Removes the profile photo of the currently authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile photo removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> removeProfilePhoto() {
        log.info("REST request to remove profile photo");
        return ResponseEntity.ok(ApiResponse.success(userProfileService.removeProfilePhoto()));
    }
}
