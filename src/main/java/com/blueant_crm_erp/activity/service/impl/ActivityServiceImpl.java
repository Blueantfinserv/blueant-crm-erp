package com.blueant_crm_erp.activity.service.impl;

import com.blueant_crm_erp.activity.dto.request.CreateActivityTimelineRequest;
import com.blueant_crm_erp.activity.dto.request.UpdateActivityTimelineRequest;
import com.blueant_crm_erp.activity.dto.response.ActivityTimelineResponse;
import com.blueant_crm_erp.activity.entity.ActivityTimeline;
import com.blueant_crm_erp.activity.enums.ActivityType;
import com.blueant_crm_erp.activity.exception.InvalidActivityException;
import com.blueant_crm_erp.activity.mapper.ActivityTimelineMapper;
import com.blueant_crm_erp.activity.repository.ActivityTimelineRepository;
import com.blueant_crm_erp.activity.service.ActivityService;
import com.blueant_crm_erp.activity.specification.ActivityTimelineSpecification;
import com.blueant_crm_erp.activity.validator.ActivityTimelineValidator;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {

    private final ActivityTimelineRepository activityTimelineRepository;
    private final LeadRepository leadRepository;
    private final ActivityTimelineMapper activityTimelineMapper;
    private final ActivityTimelineValidator activityTimelineValidator;

    @Override
    @Transactional
    public ActivityTimelineResponse createActivity(CreateActivityTimelineRequest request) {
        log.info("Creating new activity of type {}", request.getActivityType());
        activityTimelineValidator.validateCreateRequest(request);

        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new InvalidActivityException("Lead not found with ID: " + request.getLeadId()));

        ActivityTimeline entity = activityTimelineMapper.toEntity(request);
        entity.setLead(lead);

        ActivityTimeline saved = activityTimelineRepository.save(entity);
        return activityTimelineMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ActivityTimelineResponse updateActivity(Long id, UpdateActivityTimelineRequest request) {
        log.info("Updating activity ID {}", id);
        ActivityTimeline entity = activityTimelineRepository.findById(id)
                .orElseThrow(() -> new InvalidActivityException("Activity not found with ID: " + id));

        activityTimelineMapper.updateEntityFromRequest(request, entity);
        ActivityTimeline updated = activityTimelineRepository.save(entity);
        return activityTimelineMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTimelineResponse getActivityById(Long id) {
        return activityTimelineRepository.findById(id)
                .map(activityTimelineMapper::toResponse)
                .orElseThrow(() -> new InvalidActivityException("Activity not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityTimelineResponse> searchActivities(Long leadId, ActivityType activityType, Long referenceId, Pageable pageable) {
        Specification<ActivityTimeline> spec = Specification.where(ActivityTimelineSpecification.hasLeadId(leadId))
                .and(ActivityTimelineSpecification.hasActivityType(activityType))
                .and(ActivityTimelineSpecification.hasReferenceId(referenceId));

        return activityTimelineRepository.findAll(spec, pageable)
                .map(activityTimelineMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteActivity(Long id) {
        log.info("Deleting activity ID {}", id);
        if (!activityTimelineRepository.existsById(id)) {
            throw new InvalidActivityException("Activity not found with ID: " + id);
        }
        activityTimelineRepository.deleteById(id);
    }
}
