package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientFamilyHeadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientFamilyHeadHistoryRepository extends JpaRepository<ClientFamilyHeadHistory, Long> {

    List<ClientFamilyHeadHistory> findByFamilyIdOrderByChangedAtDesc(Long familyId);

    List<ClientFamilyHeadHistory> findByFamilyIdOrderByChangedAtDescIdDesc(Long familyId);
}
