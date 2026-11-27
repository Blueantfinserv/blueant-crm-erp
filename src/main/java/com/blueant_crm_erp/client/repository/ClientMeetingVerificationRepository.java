package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientMeetingVerification;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientMeetingVerificationRepository extends JpaRepository<ClientMeetingVerification, Long>, JpaSpecificationExecutor<ClientMeetingVerification> {

    Optional<ClientMeetingVerification> findByClientMeetingIdAndIsCurrentTrue(Long clientMeetingId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"clientMeeting", "clientMeeting.client", "clientMeeting.assignedEmployee"})
    List<ClientMeetingVerification> findByClientMeetingIdOrderByAttemptNumberAsc(Long clientMeetingId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"clientMeeting", "clientMeeting.client", "clientMeeting.assignedEmployee"})
    Page<ClientMeetingVerification> findByVerificationStatusAndIsCurrentTrue(VerificationStatus status, Pageable pageable);

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {
            "clientMeeting",
            "clientMeeting.client",
            "clientMeeting.assignedEmployee",
            "clientMeeting.assignedEmployee.role",
            "clientMeeting.assignedEmployee.designation"
    })
    Page<ClientMeetingVerification> findAll(org.springframework.data.jpa.domain.Specification<ClientMeetingVerification> spec, Pageable pageable);

    int countByClientMeetingId(Long clientMeetingId);
}
