package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientMeetingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientMeetingUpdateRepository extends JpaRepository<ClientMeetingUpdate, Long> {

    List<ClientMeetingUpdate> findByClientMeetingIdOrderByUpdateNumberAsc(Long clientMeetingId);

    Optional<ClientMeetingUpdate> findTopByClientMeetingIdOrderByUpdateNumberDesc(Long clientMeetingId);

    int countByClientMeetingId(Long clientMeetingId);
}
