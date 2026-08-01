package com.blueant_crm_erp.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PipelineResponse {
    private long newLeads;
    private long inProgress;
    private long followUp;
    private long meeting;
    private long proposal;
    private long negotiation;
    private long converted;
    private long alreadyClient;
    private long notInterested;
    private long removed;
}
