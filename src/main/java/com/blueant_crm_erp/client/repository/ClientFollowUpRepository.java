package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientFollowUpRepository extends JpaRepository<ClientFollowUp, Long>, JpaSpecificationExecutor<ClientFollowUp> {

    List<ClientFollowUp> findByClientIdOrderByFollowupDateDesc(Long clientId);

    List<ClientFollowUp> findBySalesPersonIdOrderByFollowupDateDesc(Long salesPersonId);
}
