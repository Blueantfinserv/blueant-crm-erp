package com.blueant_crm_erp.auth.validator;

import com.blueant_crm_erp.auth.entity.RefreshToken;
import com.blueant_crm_erp.exception.auth.InvalidCredentialsException;
import com.blueant_crm_erp.exception.auth.UnauthorizedException;
import com.blueant_crm_erp.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final PasswordEncoder passwordEncoder;

    /**
     * Validates that the provided old password matches the user's current password.
     */
    public void validateOldPassword(String currentPassword, String userPassword) {
        if (!passwordEncoder.matches(currentPassword, userPassword)) {
            throw new InvalidCredentialsException("Old password does not match");
        }
    }

    /**
     * Validates that the session belongs to the current user.
     */
    public void validateSessionOwnership(RefreshToken session, User currentUser) {
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to logout this session.");
        }
    }
}
