package com.blueant_crm_erp.auth.repository;

import com.blueant_crm_erp.auth.entity.RefreshToken;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * Refresh Token Repository
 * =============================================================================
 *
 * Repository for RefreshToken entity.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Store refresh tokens
 * • Find refresh token
 * • Find active refresh tokens
 * • Delete expired tokens
 * • Delete user tokens
 * • Check token existence
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token value.
     *
     * @param token Refresh token
     * @return RefreshToken
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Check whether refresh token exists.
     *
     * @param token Refresh token
     * @return true if exists
     */
    boolean existsByToken(String token);

    /**
     * Find all refresh tokens of a user.
     *
     * @param user User
     * @return List of RefreshToken
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find active refresh tokens of a user.
     *
     * @param user User
     * @param revoked Revoked flag
     * @return List of RefreshToken
     */
    List<RefreshToken> findByUserAndRevoked(User user, Boolean revoked);

    /**
     * Delete all refresh tokens of a user.
     *
     * @param user User
     */
    void deleteByUser(User user);

    /**
     * Delete refresh token by token value.
     *
     * @param token Refresh token
     */
    void deleteByToken(String token);

    /**
     * Find expired refresh tokens.
     *
     * @param expiryDate Expiry date
     * @return List of expired tokens
     */
    List<RefreshToken> findByExpiryDateBefore(LocalDateTime expiryDate);

    /**
     * Delete expired refresh tokens.
     *
     * @param expiryDate Expiry date
     */
    void deleteByExpiryDateBefore(LocalDateTime expiryDate);

    /**
     * Find valid refresh token.
     *
     * @param token Token
     * @param revoked Revoked flag
     * @return RefreshToken
     */
    Optional<RefreshToken> findByTokenAndRevoked(String token, Boolean revoked);

    /**
     * Find refresh token by session ID.
     *
     * @param sessionId Session ID
     * @return RefreshToken
     */
    Optional<RefreshToken> findBySessionId(String sessionId);

    /**
     * Find active refresh token by session ID.
     */
    Optional<RefreshToken> findBySessionIdAndRevoked(String sessionId, Boolean revoked);
    
    /**
     * Delete refresh token by session ID.
     * 
     * @param sessionId Session ID
     */
    void deleteBySessionId(String sessionId);


}