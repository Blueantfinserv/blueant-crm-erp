package com.blueant_crm_erp.activity.specification;

import com.blueant_crm_erp.activity.entity.ActivityTimeline;
import com.blueant_crm_erp.activity.enums.ActivityType;
import org.springframework.data.jpa.domain.Specification;

public class ActivityTimelineSpecification {

    public static Specification<ActivityTimeline> hasLeadId(Long leadId) {
        return (root, query, criteriaBuilder) ->
                leadId == null ? null : criteriaBuilder.equal(root.get("lead").get("id"), leadId);
    }

    public static Specification<ActivityTimeline> hasActivityType(ActivityType type) {
        return (root, query, criteriaBuilder) ->
                type == null ? null : criteriaBuilder.equal(root.get("activityType"), type);
    }
    
    public static Specification<ActivityTimeline> hasReferenceId(Long referenceId) {
        return (root, query, criteriaBuilder) ->
                referenceId == null ? null : criteriaBuilder.equal(root.get("referenceId"), referenceId);
    }
}
