package com.blueant_crm_erp.client.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_meeting_verification_business_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cl_verif_item_type", columnNames = {"client_meeting_verification_id", "business_type"})
        },
        indexes = {
                @Index(name = "idx_cl_verif_item_verif", columnList = "client_meeting_verification_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientMeetingVerificationBusinessItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_meeting_verification_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_verif_item_verif"))
    private ClientMeetingVerification verification;

    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (createdBy == null) {
            createdBy = "system";
        }
    }
}
