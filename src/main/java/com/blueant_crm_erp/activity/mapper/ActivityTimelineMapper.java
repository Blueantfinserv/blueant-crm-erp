package com.blueant_crm_erp.activity.mapper;

import com.blueant_crm_erp.activity.dto.request.CreateActivityTimelineRequest;
import com.blueant_crm_erp.activity.dto.request.UpdateActivityTimelineRequest;
import com.blueant_crm_erp.activity.dto.response.ActivityTimelineResponse;
import com.blueant_crm_erp.activity.entity.ActivityTimeline;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ActivityTimelineMapper {

    @Mapping(target = "lead", ignore = true)
    ActivityTimeline toEntity(CreateActivityTimelineRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateActivityTimelineRequest request, @MappingTarget ActivityTimeline entity);

    @Mapping(target = "leadId", source = "lead.id")
    ActivityTimelineResponse toResponse(ActivityTimeline entity);

    List<ActivityTimelineResponse> toResponseList(List<ActivityTimeline> entities);

}
