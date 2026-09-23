package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.CrmOnboarding;
import com.blueant_crm_erp.client.enums.CrmOnboardingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrmOnboardingRepository extends JpaRepository<CrmOnboarding, Long>, JpaSpecificationExecutor<CrmOnboarding> {

    Optional<CrmOnboarding> findByLeadId(Long leadId);

    Optional<CrmOnboarding> findByLeadLeadCode(String leadCode);

    boolean existsByLeadIdAndOnboardingStatus(Long leadId, CrmOnboardingStatus status);

    List<CrmOnboarding> findByOnboardingStatus(CrmOnboardingStatus status);
}
