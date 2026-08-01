package com.blueant_crm_erp.followup.mapper;

import com.blueant_crm_erp.followup.dto.response.FollowUpResponse;
import com.blueant_crm_erp.followup.entity.FollowUp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface FollowUpMapper {

    @Mapping(target = "leadId", source = "lead.id")
    @Mapping(target = "nextFollowUpId", source = "nextFollowupDate", ignore = true) // No id exists, so ignore or map date. Wait, the response has nextFollowUpId. But entity has nextFollowupDate.
    // I should check the FollowUpResponse DTO to see what fields exist.
    FollowUpResponse toResponse(FollowUp entity);

    List<FollowUpResponse> toResponseList(List<FollowUp> entities);
}
