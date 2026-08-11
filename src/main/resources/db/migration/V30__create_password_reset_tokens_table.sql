-- ============================================================================
-- Migration: Create password_reset_tokens table
-- ============================================================================
-- Stores the secure SHA-256 hashes of generated forgot password OTPs/tokens.
-- ============================================================================

CREATE TABLE password_reset_tokens (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    user_id          BIGINT         NOT NULL,
    token_hash       VARCHAR(255)   NOT NULL,
    expires_at       DATETIME       NOT NULL,
    used             TINYINT(1)     NOT NULL DEFAULT 0,
    used_at          DATETIME       NULL,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_password_reset_token_hash (token_hash),
    INDEX idx_password_reset_token_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
