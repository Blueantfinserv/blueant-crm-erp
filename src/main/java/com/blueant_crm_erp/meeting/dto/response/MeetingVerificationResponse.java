package com.blueant_crm_erp.meeting.dto.response;

import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingVerificationResponse {

    private Long id;
    private VerificationStatus verificationStatus;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private String rejectionReason;

    // --- Coordinator collected information ---
    private String aloneWith;
    private String personName;
    private String position;
    private Integer clientAge;
    private String maritalStatus;
    private String profession;
    private String email;
    private String companyName;
    private Boolean anyChildren;
    private Integer numberOfChildren;
    private Boolean previousInvestment;

}
