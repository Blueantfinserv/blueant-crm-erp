package com.blueant_crm_erp.meeting.mapper;

import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.request.UpdateMeetingRequest;
import com.blueant_crm_erp.meeting.dto.response.MeetingDetailResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingDropdownResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingResponse;
import com.blueant_crm_erp.meeting.dto.response.MeetingSummaryResponse;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.dto.response.MeetingVerificationResponse;
import com.blueant_crm_erp.meeting.entity.MeetingVerification;
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
    @Mapping(source = "meetingRemarks", target = "remarks")
    @Mapping(source = "verification.verificationStatus", target = "verificationStatus")
    @Mapping(source = "verification.verifiedBy", target = "verifiedBy")
    @Mapping(source = "verification.verifiedAt", target = "verifiedAt")
    @Mapping(source = "verification.meetingTiming", target = "meetingTiming")
    @Mapping(source = "verification.ageGroup", target = "ageGroup")
    @Mapping(source = "verification.existingSip", target = "existingSip")
    @Mapping(source = "verification.profession", target = "profession")
    @Mapping(source = "verification.professionDetail", target = "professionDetail")
    @Mapping(source = "verification.bestTimeForMeeting", target = "bestTimeForMeeting")
    @Mapping(target = "aloneWith", expression = "java(getAloneWith(meeting))")
    @Mapping(target = "meetingWith", expression = "java(getMeetingWith(meeting))")
    @Mapping(target = "personName", expression = "java(getPersonName(meeting))")
    @Mapping(target = "position", expression = "java(getPosition(meeting))")
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
    @Mapping(source = "meetingRemarks", target = "remarks")
    @Mapping(source = "verification.meetingTiming", target = "meetingTiming")
    @Mapping(source = "verification.ageGroup", target = "ageGroup")
    @Mapping(source = "verification.existingSip", target = "existingSip")
    @Mapping(source = "verification.profession", target = "profession")
    @Mapping(source = "verification.professionDetail", target = "professionDetail")
    @Mapping(source = "verification.bestTimeForMeeting", target = "bestTimeForMeeting")
    @Mapping(target = "aloneWith", expression = "java(getAloneWith(meeting))")
    @Mapping(target = "meetingWith", expression = "java(getMeetingWith(meeting))")
    @Mapping(target = "personName", expression = "java(getPersonName(meeting))")
    @Mapping(target = "position", expression = "java(getPosition(meeting))")
    MeetingDetailResponse toDetailResponse(Meeting meeting);

    @Mapping(source = "aloneWith", target = "meetingWith", qualifiedByName = "mapAloneWithToMeetingWith")
    MeetingVerificationResponse toVerificationResponse(MeetingVerification entity);

    @AfterMapping
    default void enrichPendingVerificationResponse(MeetingVerification entity, @MappingTarget MeetingVerificationResponse response) {
        if (entity != null && entity.getMeeting() != null) {
            Meeting m = entity.getMeeting();
            if (response.getMeetingDate() == null) {
                response.setMeetingDate(m.getMeetingDate());
            }
            if (response.getMeetingTime() == null) {
                response.setMeetingTime(m.getMeetingTime());
            }
            if (response.getNextMeetingDate() == null) {
                response.setNextMeetingDate(m.getNextMeetingDate());
            }
            if (response.getNextMeetingTime() == null) {
                response.setNextMeetingTime(m.getNextMeetingTime());
            }
            if (response.getMeetingCode() == null) {
                response.setMeetingCode(m.getMeetingCode());
            }
            if (response.getMeetingNumber() == null) {
                response.setMeetingNumber(m.getMeetingNumber());
            }
            if (response.getMeetingType() == null) {
                response.setMeetingType(m.getMeetingType());
            }
            if (response.getMeetingTitle() == null) {
                response.setMeetingTitle(m.getMeetingTitle());
            }
            if (response.getMeetingStatus() == null) {
                response.setMeetingStatus(m.getMeetingStatus());
            }
            if (response.getStatus() == null) {
                response.setStatus(m.getStatus());
            }
            if (response.getMeetingMode() == null) {
                response.setMeetingMode(m.getMeetingMode());
            }
            if (response.getMeetingLocation() == null) {
                response.setMeetingLocation(getMeetingLocation(m));
            }
            if (response.getMeetingRemarks() == null) {
                response.setMeetingRemarks(m.getMeetingRemarks());
            }
            if (response.getMeetingConducted() == null) {
                response.setMeetingConducted(m.getMeetingConducted());
            }
            if (response.getLeadStatus() == null) {
                response.setLeadStatus(m.getLeadStatus());
            }
            if (response.getLatitude() == null) {
                response.setLatitude(m.getLatitude());
            }
            if (response.getLongitude() == null) {
                response.setLongitude(m.getLongitude());
            }
            if (response.getLocationAccuracy() == null) {
                response.setLocationAccuracy(m.getLocationAccuracy());
            }
            if (response.getLocationCapturedAt() == null) {
                response.setLocationCapturedAt(m.getLocationCapturedAt());
            }
            if (response.getGoogleMapsUrl() == null) {
                response.setGoogleMapsUrl(m.getGoogleMapsUrl());
            }
            if (response.getVisitingCard() == null) {
                response.setVisitingCard(m.getVisitingCard());
            }
            if (m.getLead() != null) {
                if (response.getLeadId() == null) response.setLeadId(m.getLead().getId());
                if (response.getLeadCode() == null) response.setLeadCode(m.getLead().getLeadCode());
                if (response.getClientName() == null) response.setClientName(m.getLead().getClientName());
                if (response.getMobileNumber() == null) response.setMobileNumber(m.getLead().getMobileNumber());
            }
            com.blueant_crm_erp.user.entity.User emp = m.getAssignedEmployee() != null ? m.getAssignedEmployee() : (m.getLead() != null ? m.getLead().getAssignedSalesPerson() : null);
            if (emp != null) {
                if (response.getAssignedEmployeeId() == null) response.setAssignedEmployeeId(emp.getId());
                if (response.getEmployeeCode() == null) response.setEmployeeCode(emp.getEmployeeCode());
                if (response.getEmployeeName() == null) response.setEmployeeName(emp.getFullName());
            }
        }
    }

    @Named("mapAloneWithToMeetingWith")
    default String mapAloneWithToMeetingWith(String aloneWith) {
        if (aloneWith == null) {
            return null;
        }
        if ("SOMEONE".equalsIgnoreCase(aloneWith)) {
            return "SOMEONE_ELSE";
        }
        return aloneWith.toUpperCase();
    }

    default String getAloneWith(Meeting meeting) {
        if (meeting == null) {
            return null;
        }
        if (meeting.getVerification() != null && meeting.getVerification().getVerificationStatus() == com.blueant_crm_erp.servicerequest.enums.VerificationStatus.VERIFIED) {
            return meeting.getVerification().getAloneWith();
        }
        return meeting.getAloneWith();
    }

    default String getMeetingWith(Meeting meeting) {
        if (meeting == null) {
            return null;
        }
        String aw = null;
        if (meeting.getVerification() != null && meeting.getVerification().getVerificationStatus() == com.blueant_crm_erp.servicerequest.enums.VerificationStatus.VERIFIED) {
            aw = meeting.getVerification().getAloneWith();
        } else {
            aw = meeting.getAloneWith();
        }
        return mapAloneWithToMeetingWith(aw);
    }

    default String getPersonName(Meeting meeting) {
        if (meeting == null) {
            return null;
        }
        if (meeting.getVerification() != null && meeting.getVerification().getVerificationStatus() == com.blueant_crm_erp.servicerequest.enums.VerificationStatus.VERIFIED) {
            return meeting.getVerification().getPersonName();
        }
        return meeting.getPersonName();
    }

    default String getPosition(Meeting meeting) {
        if (meeting == null) {
            return null;
        }
        if (meeting.getVerification() != null && meeting.getVerification().getVerificationStatus() == com.blueant_crm_erp.servicerequest.enums.VerificationStatus.VERIFIED) {
            return meeting.getVerification().getPosition();
        }
        return meeting.getPosition();
    }

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
    @Mapping(target = "location", expression = "java(getMeetingLocation(meeting))")
    @Mapping(source = "verification.verificationStatus", target = "verificationStatus")
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