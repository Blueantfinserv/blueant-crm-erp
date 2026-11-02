package com.blueant_crm_erp.client.dto.request;

import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientMeetingUpdateRequest {

    @NotNull(message = "Meeting conducted status is required.")
    private MeetingConductStatus meetingConducted;

    private MeetingMode meetingMode;

    @NotNull(message = "Meeting date is required.")
    private LocalDate meetingDate;

    private LocalTime meetingTime;

    private String clientMeetingStatus;

    private String aloneWith;

    private String businessGenerated;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters.")
    private String remarks;

    // Additional Optional Questions for Client Meeting Update Form
    @Size(max = 500, message = "Residential address cannot exceed 500 characters.")
    private String residentialAddress;

    @Size(max = 500, message = "Office address cannot exceed 500 characters.")
    private String officeAddress;

    @Size(max = 100, message = "Current city cannot exceed 100 characters.")
    private String currentCity;

    @Size(max = 100, message = "Preferred contact time cannot exceed 100 characters.")
    private String preferredContactTime;

    @Size(max = 100, message = "Occupation/business type cannot exceed 100 characters.")
    private String occupationBusinessType;

    @Size(max = 150, message = "Company/organization name cannot exceed 150 characters.")
    private String companyOrganizationName;

    @Size(max = 50, message = "Communication preference cannot exceed 50 characters.")
    private String communicationPreference;

    @Size(max = 150, message = "Preferred investment service cannot exceed 150 characters.")
    private String preferredInvestmentService;

    @Size(max = 1000, message = "Client requirements cannot exceed 1000 characters.")
    private String clientRequirements;

    @Size(max = 1000, message = "Other info notes cannot exceed 1000 characters.")
    private String otherInfoNotes;

    private LocalDate nextPlanDate;

    private LocalTime nextPlanTime;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Double accuracy;
}
