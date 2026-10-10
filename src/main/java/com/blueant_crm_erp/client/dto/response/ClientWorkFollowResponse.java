package com.blueant_crm_erp.client.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientWorkFollowResponse {

    private Long clientId;
    private String clientCode;
    private String clientName;
    private String mobileNumber;
    private String alternateMobileNumber;
    private String email;
    private String speciality;
    private String location;
    private String clinicAddress;
    private String clientStatus;
    private LocalDate assignmentDate;
    private String bestTimeToMeet;

    private Long assignedSalesPersonId;
    private String assignedSalesPersonCode;
    private String assignedSalesPersonName;

    private String activeMeetingCode;
    private String activeMeetingStatus;
    private LocalDate activeMeetingDate;
    private LocalTime activeMeetingTime;
    private Integer activeMeetingNumber;
    private Integer totalMeetings;
}
