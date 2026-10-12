package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAssignmentHistoryResponse {

    private Long id;
    private Long clientId;
    private Long assignedFromUserId;
    private String assignedFromUserCode;
    private String assignedFromUserName;
    private Long assignedToUserId;
    private String assignedToUserCode;
    private String assignedToUserName;
    private Long assignedByUserId;
    private String assignedByUserCode;
    private String assignedByUserName;
    private LocalDate assignmentDate;
    private LocalDateTime assignedAt;
    private String reason;
}
