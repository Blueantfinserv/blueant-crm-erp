package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientFamilyMember;
import com.blueant_crm_erp.client.enums.FamilyMembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientFamilyMemberRepository extends JpaRepository<ClientFamilyMember, Long>, JpaSpecificationExecutor<ClientFamilyMember> {

    List<ClientFamilyMember> findByFamilyId(Long familyId);

    List<ClientFamilyMember> findByFamilyIdAndMembershipStatus(Long familyId, FamilyMembershipStatus membershipStatus);

    Optional<ClientFamilyMember> findByFamilyIdAndClientId(Long familyId, Long clientId);

    Optional<ClientFamilyMember> findByFamilyIdAndClientIdAndMembershipStatus(Long familyId, Long clientId, FamilyMembershipStatus membershipStatus);

    Optional<ClientFamilyMember> findByClientIdAndMembershipStatus(Long clientId, FamilyMembershipStatus membershipStatus);

    boolean existsByClientIdAndMembershipStatus(Long clientId, FamilyMembershipStatus membershipStatus);

    boolean existsByFamilyIdAndClientIdAndMembershipStatus(Long familyId, Long clientId, FamilyMembershipStatus membershipStatus);
}
