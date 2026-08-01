package com.blueant_crm_erp.meeting.specification;

import com.blueant_crm_erp.meeting.dto.request.MeetingSearchRequest;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

public final class MeetingSearchSpecification {

    private MeetingSearchSpecification() {
    }

    public static Specification<Meeting> build(MeetingSearchRequest request) {
        return build(request != null ? request.getKeyword() : null, null, "completed", null);
    }

    public static Specification<Meeting> build(String keyword, String dateFilter, String statusFilter, Integer sequenceFilter) {
        Specification<Meeting> spec = Specification.where(null);

        // 1. Status Filter (Queue Rules)
        Specification<Meeting> statusSpec;
        if ("completed".equalsIgnoreCase(statusFilter)) {
            statusSpec = (root, query, cb) -> cb.equal(root.get("meetingStatus"), MeetingStatus.COMPLETED);
        } else if ("all".equalsIgnoreCase(statusFilter)) {
            // Exclude cancelled
            statusSpec = (root, query, cb) -> cb.notEqual(root.get("meetingStatus"), MeetingStatus.CANCELLED);
        } else {
            // Default to Actionable Queue: SCHEDULED, RESCHEDULED
            List<MeetingStatus> actionableStatuses = Arrays.asList(MeetingStatus.SCHEDULED, MeetingStatus.RESCHEDULED);
            statusSpec = (root, query, cb) -> root.get("meetingStatus").in(actionableStatuses);
        }
        spec = spec.and(statusSpec);

        // 2. Date Filter
        if (dateFilter != null && !dateFilter.isBlank()) {
            LocalDate today = LocalDate.now();
            Specification<Meeting> dateSpec = null;
            switch (dateFilter.toLowerCase()) {
                case "today":
                    dateSpec = (root, query, cb) -> cb.equal(root.get("meetingDate"), today);
                    break;
                case "tomorrow":
                    dateSpec = (root, query, cb) -> cb.equal(root.get("meetingDate"), today.plusDays(1));
                    break;
                case "week":
                    // From today up to 7 days
                    dateSpec = (root, query, cb) -> cb.between(root.get("meetingDate"), today, today.plusDays(7));
                    break;
                case "upcoming":
                    dateSpec = (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("meetingDate"), today);
                    break;
                case "completed":
                case "all":
                    // No date restriction
                    break;
                default:
                    break;
            }
            if (dateSpec != null) {
                spec = spec.and(dateSpec);
            }
        }

        // 3. Sequence Filter
        if (sequenceFilter != null) {
            Specification<Meeting> seqSpec = (root, query, cb) -> cb.equal(root.get("meetingNumber"), sequenceFilter);
            spec = spec.and(seqSpec);
        }

        // 4. Keyword Search
        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            Specification<Meeting> keywordSpec = (root, query, cb) -> cb.or(
                    cb.like(root.get("meetingCode"), likeKeyword),
                    cb.like(root.get("lead").get("clientName"), likeKeyword),
                    cb.like(root.get("lead").get("leadCode"), likeKeyword),
                    cb.like(root.get("lead").get("mobileNumber"), likeKeyword),
                    cb.like(root.get("assignedEmployee").get("employeeCode"), likeKeyword),
                    cb.like(root.get("assignedEmployee").get("firstName"), likeKeyword),
                    cb.like(root.get("assignedEmployee").get("lastName"), likeKeyword)
            );
            spec = spec.and(keywordSpec);
        }

        return spec;
    }

}