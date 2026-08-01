package com.blueant_crm_erp.auth.entity;

import com.blueant_crm_erp.common.base.BaseAuditEntity;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * =============================================================================
 * Refresh Token Entity
 * =============================================================================
 *
 * Represents an authenticated user session.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Refresh Token Storage
 * • Session Management
 * • Multi Device Login
 * • Device Tracking
 * • Login History
 * • Security Monitoring
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "auth_refresh_tokens",
        indexes = {

                @Index(name = "idx_refresh_token", columnList = "refresh_token"),

                @Index(name = "idx_session_id", columnList = "session_id"),

                @Index(name = "idx_user", columnList = "user_id"),

                @Index(name = "idx_expiry", columnList = "expiry_date")

        }
)
public class RefreshToken extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Session Identifier.
     */
    @Column(name = "session_id", nullable = false, unique = true, length = 100)
    @Comment("Unique session identifier")
    private String sessionId;

    /**
     * Refresh Token.
     */
    @Column(name = "refresh_token", nullable = false, unique = true, length = 512)
    @Comment("JWT Refresh Token")
    private String token;

    /**
     * Associated User.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_token_user")
    )
    private User user;

    /**
     * Device Identifier.
     */
    @Column(name = "device_id", length = 255)
    private String deviceId;

    /**
     * Device Name.
     */
    @Column(name = "device_name", length = 150)
    private String deviceName;

    /**
     * Device Type.
     */
    @Column(name = "device_type", length = 50)
    private String deviceType;

    /**
     * Browser Name.
     */
    @Column(name = "browser", length = 100)
    private String browser;

    /**
     * Operating System.
     */
    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    /**
     * Client IP Address.
     */
    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    /**
     * Client Location.
     */
    @Column(name = "location", length = 255)
    private String location;

    /**
     * Expiration Date.
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Last Activity Time.
     */
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    /**
     * Revoked Flag.
     */
    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private Boolean revoked = Boolean.FALSE;

    /**
     * Revoked At.
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Revoked By.
     */
    @Column(name = "revoked_by", length = 100)
    private String revokedBy;

}