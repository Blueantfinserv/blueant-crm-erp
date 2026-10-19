package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientFamily;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientFamilyRepository extends JpaRepository<ClientFamily, Long>, JpaSpecificationExecutor<ClientFamily> {

    Optional<ClientFamily> findByFamilyCode(String familyCode);

    boolean existsByFamilyCode(String familyCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM ClientFamily f WHERE f.familyCode = :familyCode")
    Optional<ClientFamily> findByFamilyCodeWithLock(@Param("familyCode") String familyCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM ClientFamily f WHERE f.id = :id")
    Optional<ClientFamily> findByIdWithLock(@Param("id") Long id);
}
