package com.blueant_crm_erp.auth.repository;

import com.blueant_crm_erp.auth.entity.PasswordResetToken;
import com.blueant_crm_erp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * Password Reset Token Repository
 * =============================================================================
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);

    List<PasswordResetToken> findByUserAndUsedFalse(User user);
}
