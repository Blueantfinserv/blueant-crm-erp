package com.blueant_crm_erp.meeting.mapper;

import com.blueant_crm_erp.meeting.dto.response.MeetingUpdateResponse;
import com.blueant_crm_erp.meeting.entity.MeetingUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface MeetingUpdateMapper {

    @Mapping(source = "meeting.meetingCode", target = "meetingCode")
    MeetingUpdateResponse toResponse(MeetingUpdate meetingUpdate);

    List<MeetingUpdateResponse> toResponseList(List<MeetingUpdate> meetingUpdates);
}
