package com.blueant_crm_erp.activity.controller;

import com.blueant_crm_erp.activity.dto.request.CreateActivityTimelineRequest;
import com.blueant_crm_erp.activity.dto.request.UpdateActivityTimelineRequest;
import com.blueant_crm_erp.activity.dto.response.ActivityTimelineResponse;
import com.blueant_crm_erp.activity.enums.ActivityType;
import com.blueant_crm_erp.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Activity Controller", description = "Endpoints for managing lead and meeting activities")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "Create an activity")
    @PostMapping
    @PreAuthorize("hasAuthority('activity:write') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ActivityTimelineResponse> createActivity(@Valid @RequestBody CreateActivityTimelineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.createActivity(request));
    }

    @Operation(summary = "Get activity by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('activity:read') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ActivityTimelineResponse> getActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivityById(id));
    }

    @Operation(summary = "Search activities")
    @GetMapping
    @PreAuthorize("hasAuthority('activity:read') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ActivityTimelineResponse>> searchActivities(
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false) Long referenceId,
            Pageable pageable) {
        return ResponseEntity.ok(activityService.searchActivities(leadId, activityType, referenceId, pageable));
    }

    @Operation(summary = "Update an activity")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('activity:write') or hasRole('SALES') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ActivityTimelineResponse> updateActivity(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateActivityTimelineRequest request) {
        return ResponseEntity.ok(activityService.updateActivity(id, request));
    }

    @Operation(summary = "Delete an activity")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('activity:delete') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
}
