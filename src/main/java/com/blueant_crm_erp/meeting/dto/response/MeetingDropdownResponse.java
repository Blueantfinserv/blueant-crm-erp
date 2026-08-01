package com.blueant_crm_erp.meeting.dto.response;

import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDropdownResponse {

    /**
     * Database Id
     */
    private Long id;

    /**
     * Business Meeting Code
     */
    private String meetingCode;

    /**
     * Display Name
     * Example:
     * Intro Meeting
     * 1st Meeting
     * 2nd Meeting
     */
    private String displayName;

    /**
     * Meeting Status
     */
    private MeetingStatus meetingStatus;

}