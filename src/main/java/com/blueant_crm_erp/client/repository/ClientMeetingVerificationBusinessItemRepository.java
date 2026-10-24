package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientMeetingVerificationBusinessItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientMeetingVerificationBusinessItemRepository extends JpaRepository<ClientMeetingVerificationBusinessItem, Long> {

    List<ClientMeetingVerificationBusinessItem> findByVerificationId(Long verificationId);

    void deleteByVerificationId(Long verificationId);
}
