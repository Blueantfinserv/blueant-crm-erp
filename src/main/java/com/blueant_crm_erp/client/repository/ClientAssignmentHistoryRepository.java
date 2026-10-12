package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientAssignmentHistoryRepository extends JpaRepository<ClientAssignmentHistory, Long> {
    List<ClientAssignmentHistory> findByClientIdOrderByAssignedAtDesc(Long clientId);
    List<ClientAssignmentHistory> findByClientId(Long clientId);
}
