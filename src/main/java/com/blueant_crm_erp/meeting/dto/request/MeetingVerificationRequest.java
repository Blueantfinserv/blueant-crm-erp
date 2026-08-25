package com.blueant_crm_erp.meeting.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingVerificationRequest {

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    private String aloneWith;

    @Size(max = 100, message = "Person name cannot exceed 100 characters.")
    private String personName;

    @Size(max = 100, message = "Position cannot exceed 100 characters.")
    private String position;

    @Min(value = 18, message = "Client age must be at least 18.")
    @Max(value = 120, message = "Client age must be at most 120.")
    private Integer clientAge;

    private String maritalStatus;

    private String profession;

    @Email(message = "Invalid email format.")
    private String email;

    private String companyName;

    private Boolean anyChildren;

    private Integer numberOfChildren;

    private Boolean previousInvestment;

    @NotNull(message = "Meeting timing is required.")
    private LocalTime meetingTiming;

    private String ageGroup;

    private String existingSip;

    @Size(max = 255, message = "Profession detail cannot exceed 255 characters.")
    private String professionDetail;

    private String bestTimeForMeeting;

    private String meetingWith;

}
