package com.blueant_crm_erp.meeting.dto.request;

import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingFilterRequest {

    private java.util.UUID leadId;

    private MeetingStatus meetingStatus;

    private MeetingOutcome meetingOutcome;

    private MeetingMode meetingMode;

    private LocalDate fromDate;

    private LocalDate toDate;

    private String employeeCode;

}