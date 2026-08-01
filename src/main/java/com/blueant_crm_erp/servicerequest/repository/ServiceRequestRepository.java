package com.blueant_crm_erp.servicerequest.repository;

import com.blueant_crm_erp.servicerequest.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {
    Optional<ServiceRequest> findBySrCode(String srCode);
    Optional<ServiceRequest> findByClientId(Long clientId);
}
