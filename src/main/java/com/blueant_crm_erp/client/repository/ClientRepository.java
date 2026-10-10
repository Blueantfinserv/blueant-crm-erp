package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.enums.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    Optional<Client> findByClientCode(String clientCode);
    Optional<Client> findByLeadId(Long leadId);
    boolean existsByMobileNumber(String mobileNumber);
    boolean existsByClientCode(String clientCode);
    Optional<Client> findByMobileNumber(String mobileNumber);
    Optional<Client> findByIdAndSalesPersonId(Long id, Long salesPersonId);
    Page<Client> findBySalesPersonId(Long salesPersonId, Pageable pageable);
    Page<Client> findBySalesPersonIdAndClientStatus(Long salesPersonId, ClientStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Client c WHERE c.id = :id")
    Optional<Client> findByIdWithPessimisticLock(@org.springframework.data.repository.query.Param("id") Long id);
}
