package com.blueant_crm_erp.client.bulk.repository;

import com.blueant_crm_erp.client.bulk.entity.ClientImportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientImportJobRepository extends JpaRepository<ClientImportJob, String> {

    Optional<ClientImportJob> findTopByFileHashSha256AndStatusInOrderByCreatedAtDesc(
            String fileHashSha256, Collection<String> statuses);

    List<ClientImportJob> findByStatusInAndHeartbeatAtBefore(
            Collection<String> statuses, LocalDateTime threshold);

    Page<ClientImportJob> findByCreatedByOrderByCreatedAtDesc(
            String createdBy, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE ClientImportJob j SET j.status = 'VALIDATING', j.startedAt = :now, j.heartbeatAt = :now WHERE j.id = :jobId AND j.status = 'PENDING'")
    int claimJobForProcessing(@org.springframework.data.repository.query.Param("jobId") String jobId, @org.springframework.data.repository.query.Param("now") LocalDateTime now);
}

