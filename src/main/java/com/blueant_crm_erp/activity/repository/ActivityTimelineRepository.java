package com.blueant_crm_erp.activity.repository;

import com.blueant_crm_erp.activity.entity.ActivityTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityTimelineRepository extends JpaRepository<ActivityTimeline, Long>, JpaSpecificationExecutor<ActivityTimeline> {
    
    List<ActivityTimeline> findByLeadIdOrderByCreatedAtAsc(Long leadId);
    
}
