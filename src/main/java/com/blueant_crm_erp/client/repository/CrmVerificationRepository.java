package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.CrmVerification;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrmVerificationRepository extends JpaRepository<CrmVerification, Long>, JpaSpecificationExecutor<CrmVerification> {

    Optional<CrmVerification> findByLeadId(Long leadId);

    Optional<CrmVerification> findByLeadLeadCode(String leadCode);

    List<CrmVerification> findByVerificationStatus(VerificationStatus status);

    boolean existsByLeadIdAndVerificationStatus(Long leadId, VerificationStatus status);
}
