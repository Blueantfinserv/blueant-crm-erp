package com.blueant_crm_erp.meeting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingReportResponse {

    private Long totalMeetings;
    private Double averageMeetingsBeforeConversion;
    private Long meetingsPending;
    private Long meetingsCompleted;
    
    // Key: "Conversion After Intro", "Conversion After 1st", etc.
    // Value: Count
    private double meetingSuccessRate;
    private double averageSalesCycle;
    private double journeyCompletionRate;
    
    private Map<String, Long> conversionFunnels;

}
