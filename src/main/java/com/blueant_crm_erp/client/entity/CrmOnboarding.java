package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.client.enums.CrmOnboardingStatus;
import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_onboardings",
        indexes = {
                @Index(name = "idx_crm_onboard_lead", columnList = "lead_id"),
                @Index(name = "idx_crm_onboard_status", columnList = "onboarding_status")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CrmOnboarding extends BaseVersionEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_crm_onboarding_lead"))
    private Lead lead;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", nullable = false, length = 50)
    private CrmOnboardingStatus onboardingStatus;

    /**
     * Question 5: Created By - Original Sales Person who created/converted the client.
     * Immutable historical ownership.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_sales_person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_crm_onboarding_sales_person"))
    private User createdBySalesPerson;

    // 1. Investor Name
    @Column(name = "investor_name", nullable = false, length = 150)
    private String investorName;

    // 2. Is he an investor with BLUEANT?
    @Builder.Default
    @Column(name = "is_blueant_investor")
    private Boolean isBlueantInvestor = Boolean.FALSE;

    // 3. Family Head
    @Column(name = "family_head", length = 150)
    private String familyHead;

    // 4. Occupation
    @Column(name = "occupation", length = 100)
    private String occupation;

    // 6. PAN No
    @Column(name = "pan_number", length = 20)
    private String panNumber;

    // 7. Contact Detail
    @Column(name = "contact_detail", length = 30)
    private String contactDetail;

    // 8. Mail ID
    @Column(name = "mail_id", length = 150)
    private String mailId;

    // 9. Correspondence Address
    @Column(name = "correspondence_address", length = 500)
    private String correspondenceAddress;

    // 10. Current / Office Address
    @Column(name = "office_address", length = 500)
    private String officeAddress;

    // 11. Place of Birth
    @Column(name = "place_of_birth", length = 100)
    private String placeOfBirth;

    // 12. Family Details
    @Column(name = "family_details", length = 1000)
    private String familyDetails;

    // 13. Location
    @Column(name = "location", length = 150)
    private String location;

    // 14. Source
    @Column(name = "source", length = 100)
    private String source;

    // 15. Description of Source
    @Column(name = "source_description", length = 500)
    private String sourceDescription;

    // 16. Application Received Date
    @Column(name = "application_received_date")
    private LocalDate applicationReceivedDate;

    // 17. Nominee Details
    @Column(name = "nominee_details", length = 500)
    private String nomineeDetails;

    // 18. Nominee PAN No. / Aadhaar No.
    @Column(name = "nominee_pan_or_aadhaar", length = 50)
    private String nomineePanOrAadhaar;

    // 19. Mother's Name
    @Column(name = "mother_name", length = 150)
    private String motherName;

    // 20. Application Submitted (ONLINE, OFFLINE)
    @Column(name = "application_mode", length = 50)
    private String applicationMode;

    // 21. Amount of 1st Investment
    @Column(name = "first_investment_amount", precision = 15, scale = 2)
    private BigDecimal firstInvestmentAmount;

    // 22. How much Max SIP can you expect from him?
    @Column(name = "expected_max_sip", precision = 15, scale = 2)
    private BigDecimal expectedMaxSIP;

    // 23. Type of Investment
    @Column(name = "investment_type", length = 100)
    private String investmentType;

    // 24. All Document Completed (YES, NO)
    @Builder.Default
    @Column(name = "all_documents_completed")
    private Boolean allDocumentsCompleted = Boolean.FALSE;

    // 25. Investwell User ID
    @Column(name = "investwell_user_id", length = 100)
    private String investwellUserId;

    // 26. Helpdesk Query No.
    @Column(name = "helpdesk_query_no", length = 100)
    private String helpdeskQueryNo;

    // 27. Client Reported Date
    @Column(name = "client_reported_date")
    private LocalDate clientReportedDate;

    // 28. Payment Done? (YES, NO)
    @Builder.Default
    @Column(name = "payment_done")
    private Boolean paymentDone = Boolean.FALSE;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;
}
