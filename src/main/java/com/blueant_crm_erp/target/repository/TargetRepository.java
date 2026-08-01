package com.blueant_crm_erp.target.repository;

import com.blueant_crm_erp.target.entity.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TargetRepository extends JpaRepository<Target, Long>, JpaSpecificationExecutor<Target> {
    Optional<Target> findByUserIdAndTargetMonth(Long userId, String targetMonth);
}
