package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientNominee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientNomineeRepository extends JpaRepository<ClientNominee, Long> {
    List<ClientNominee> findByClientIdOrderByCreatedAtAsc(Long clientId);
    List<ClientNominee> findByClientId(Long clientId);
}
