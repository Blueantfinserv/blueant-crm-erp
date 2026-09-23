package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "crm_verifications",
        indexes = {
                @Index(name = "idx_crm_verif_lead", columnList = "lead_id"),
                @Index(name = "idx_crm_verif_status", columnList = "verification_status")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CrmVerification extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false, foreignKey = @ForeignKey(name = "fk_crm_verification_lead"))
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "fk_crm_verification_client"))
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crm_onboarding_id", foreignKey = @ForeignKey(name = "fk_crm_verif_onboarding"))
    private CrmOnboarding crmOnboarding;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    private VerificationStatus verificationStatus;

    // Generic CRM Verification Checklist / Questions
    @Builder.Default
    @Column(name = "kyc_verified")
    private Boolean kycVerified = Boolean.FALSE;

    @Builder.Default
    @Column(name = "bank_details_verified")
    private Boolean bankDetailsVerified = Boolean.FALSE;

    @Builder.Default
    @Column(name = "documents_verified")
    private Boolean documentsVerified = Boolean.FALSE;

    @Builder.Default
    @Column(name = "client_contact_confirmed")
    private Boolean clientContactConfirmed = Boolean.FALSE;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
}
