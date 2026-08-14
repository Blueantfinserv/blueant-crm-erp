package com.blueant_crm_erp.meeting.mapper;

import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingDropdownResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingSummaryResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import org.mapstruct.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface MeetingMapper {

    /**
     * Create Request -> Entity
     */
    Meeting toEntity(CreateMeetingRequest request);

    /**
     * Entity -> Response
     */
    @Mapping(source = "lead.leadCode", target = "leadCode")
    @Mapping(source = "lead.clientName", target = "clientName")
    @Mapping(source = "lead.mobileNumber", target = "mobileNumber")
    @Mapping(source = "assignedEmployee.employeeCode", target = "employeeCode")
    @Mapping(source = "assignedEmployee.firstName", target = "employeeName")
    @Mapping(target = "companyParticipantIds", expression = "java(stringToLongList(meeting.getCompanyParticipants()))")
    @Mapping(target = "clientParticipants", expression = "java(stringToStringList(meeting.getClientParticipants()))")
    @Mapping(target = "meetingLocation", expression = "java(getMeetingLocation(meeting))")
    MeetingResponse toResponse(Meeting meeting);

    /**
     * Entity -> Detail Response
     */
    @Mapping(source = "lead.id", target = "leadId")
    @Mapping(source = "lead.leadCode", target = "leadCode")
    @Mapping(source = "lead.clientName", target = "clientName")
    @Mapping(source = "lead.mobileNumber", target = "mobileNumber")
    @Mapping(source = "assignedEmployee.id", target = "assignedEmployeeId")
    @Mapping(source = "assignedEmployee.employeeCode", target = "employeeCode")
    @Mapping(source = "assignedEmployee.firstName", target = "employeeName")
    @Mapping(target = "companyParticipantIds", expression = "java(stringToLongList(meeting.getCompanyParticipants()))")
    @Mapping(target = "clientParticipants", expression = "java(stringToStringList(meeting.getClientParticipants()))")
    @Mapping(target = "meetingLocation", expression = "java(getMeetingLocation(meeting))")
    MeetingDetailResponse toDetailResponse(Meeting meeting);

    default String getMeetingLocation(Meeting meeting) {
        if (meeting == null) {
            return null;
        }
        if (meeting.getMeetingLocation() != null && !meeting.getMeetingLocation().trim().isEmpty()) {
            return meeting.getMeetingLocation();
        }
        if (meeting.getLead() != null) {
            return meeting.getLead().getLocation();
        }
        return null;
    }

    /**
     * Entity -> Summary Response
     */
    @Mapping(source = "lead.id", target = "leadId")
    @Mapping(source = "lead.leadCode", target = "leadCode")
    @Mapping(source = "lead.clientName", target = "clientName")
    @Mapping(source = "assignedEmployee.firstName", target = "assignedEmployeeName")
    @Mapping(source = "meetingLocation", target = "location")
    MeetingSummaryResponse toSummaryResponse(Meeting meeting);

    /**
     * Entity -> Dropdown Response
     */
    MeetingDropdownResponse toDropdownResponse(Meeting meeting);

    /**
     * Entity List -> Response List
     */
    List<MeetingResponse> toResponseList(List<Meeting> meetings);

    /**
     * Entity List -> Summary List
     */
    List<MeetingSummaryResponse> toSummaryResponseList(List<Meeting> meetings);

    /**
     * Entity List -> Dropdown List
     */
    List<MeetingDropdownResponse> toDropdownResponseList(List<Meeting> meetings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateMeetingRequest request,
                                 @MappingTarget Meeting meeting);

    // TODO: Future normalization should replace this comma-separated storage
    default List<Long> stringToLongList(String str) {
        if (!StringUtils.hasText(str)) return new ArrayList<>();
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    // TODO: Future normalization should replace this comma-separated storage
    default List<String> stringToStringList(String str) {
        if (!StringUtils.hasText(str)) return new ArrayList<>();
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

}