package com.blueant_crm_erp.negotiation.repository;

import com.blueant_crm_erp.negotiation.entity.NegotiationUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationUpdateRepository extends JpaRepository<NegotiationUpdate, Long> {
    long countByNegotiationId(Long negotiationId);
    List<NegotiationUpdate> findByNegotiationIdOrderByUpdateNumberAsc(Long negotiationId);
}
