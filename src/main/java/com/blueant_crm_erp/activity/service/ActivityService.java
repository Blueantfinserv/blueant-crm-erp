package com.blueant_crm_erp.activity.service;

import com.blueant_crm_erp.activity.dto.request.CreateActivityTimelineRequest;
import com.blueant_crm_erp.activity.dto.request.UpdateActivityTimelineRequest;
import com.blueant_crm_erp.activity.dto.response.ActivityTimelineResponse;
import com.blueant_crm_erp.activity.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityService {
    ActivityTimelineResponse createActivity(CreateActivityTimelineRequest request);
    ActivityTimelineResponse updateActivity(Long id, UpdateActivityTimelineRequest request);
    ActivityTimelineResponse getActivityById(Long id);
    Page<ActivityTimelineResponse> searchActivities(Long leadId, ActivityType activityType, Long referenceId, Pageable pageable);
    void deleteActivity(Long id);
}
