package com.blueant_crm_erp.lead.repository;

import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.DuplicateLeadStatus;
import com.blueant_crm_erp.lead.enums.LeadStage;
import com.blueant_crm_erp.lead.enums.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {

    Optional<Lead> findByLeadCode(String leadCode);

    Optional<Lead> findByUniqueLeadId(String uniqueLeadId);

    Optional<Lead> findByMobileNumber(String mobileNumber);

    boolean existsByLeadCode(String leadCode);

    boolean existsByUniqueLeadId(String uniqueLeadId);

    boolean existsByMobileNumber(String mobileNumber);

    List<Lead> findAllByLeadStatus(LeadStatus leadStatus);

    long countByLeadStatus(LeadStatus leadStatus);

    // Dashboard Statistics Queries
    long countByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    long countByMeetingDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
    
    long countByMeetingDateBetweenAndLeadStage(LocalDateTime startOfDay, LocalDateTime endOfDay, LeadStage stage);

    long countByUpdatedAtBetweenAndLeadStatus(LocalDateTime startOfDay, LocalDateTime endOfDay, LeadStatus status);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.nextPlanDate < :today AND l.leadStatus NOT IN ('CONVERTED', 'LOST', 'REMOVED', 'NOT_INTERESTED')")
    long countPendingFollowUps(LocalDateTime today);


    long countByDuplicateLeadStatus(DuplicateLeadStatus status);

    @Query("SELECT u.firstName || ' ' || u.lastName, COUNT(l) FROM Lead l JOIN l.assignedLeader u GROUP BY u.id")
    List<Object[]> countLeadsByLeader();

    @Query("SELECT u.firstName || ' ' || u.lastName, COUNT(l) FROM Lead l JOIN l.assignedSalesPerson u GROUP BY u.id")
    List<Object[]> countLeadsBySalesPerson();
}