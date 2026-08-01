package com.blueant_crm_erp.meeting.specification;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.MeetingOutcome;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class MeetingSpecification {

    private MeetingSpecification() {
    }

    public static Specification<Meeting> hasLeadId(java.util.UUID leadId) {
        return (root, query, cb) ->
                leadId == null
                        ? null
                        : cb.equal(root.get("lead").get("uniqueLeadId"), leadId.toString());
    }

    public static Specification<Meeting> hasMeetingStatus(MeetingStatus meetingStatus) {
        return (root, query, cb) ->
                meetingStatus == null
                        ? null
                        : cb.equal(root.get("meetingStatus"), meetingStatus);
    }

    public static Specification<Meeting> hasMeetingOutcome(MeetingOutcome meetingOutcome) {
        return (root, query, cb) ->
                meetingOutcome == null
                        ? null
                        : cb.equal(root.get("meetingOutcome"), meetingOutcome);
    }

    public static Specification<Meeting> hasMeetingMode(MeetingMode meetingMode) {
        return (root, query, cb) ->
                meetingMode == null
                        ? null
                        : cb.equal(root.get("meetingMode"), meetingMode);
    }

    public static Specification<Meeting> hasMeetingDate(LocalDate meetingDate) {
        return (root, query, cb) ->
                meetingDate == null
                        ? null
                        : cb.equal(root.get("meetingDate"), meetingDate);
    }

    public static Specification<Meeting> hasStatus(Status status) {
        return (root, query, cb) ->
                status == null
                        ? null
                        : cb.equal(root.get("status"), status);
    }
}