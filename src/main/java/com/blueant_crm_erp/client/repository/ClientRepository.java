package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    Optional<Client> findByClientCode(String clientCode);
    Optional<Client> findByLeadId(Long leadId);
}
