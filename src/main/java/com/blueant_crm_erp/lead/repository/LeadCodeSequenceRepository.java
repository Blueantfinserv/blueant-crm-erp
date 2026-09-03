package com.blueant_crm_erp.lead.repository;

import com.blueant_crm_erp.lead.entity.LeadCodeSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadCodeSequenceRepository extends JpaRepository<LeadCodeSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM LeadCodeSequence s WHERE s.sequenceName = :sequenceName")
    Optional<LeadCodeSequence> findBySequenceNameWithLock(@Param("sequenceName") String sequenceName);
}
