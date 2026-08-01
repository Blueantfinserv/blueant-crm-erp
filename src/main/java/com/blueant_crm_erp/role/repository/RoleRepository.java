package com.blueant_crm_erp.role.repository;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>,
        JpaSpecificationExecutor<Role> {

    /**
     * Find Role By Id.
     */
    Optional<Role> findByIdAndDeletedFalse(Long id);

    /**
     * Find Role By Code.
     */
    Optional<Role> findByCodeIgnoreCase(String code);

    /**
     * Find Role By Name.
     */
    Optional<Role> findByNameIgnoreCase(String name);

    /**
     * Check Role Code Exists.
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Check Role Name Exists.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find Roles By Status.
     */
    List<Role> findAllByStatus(Status status);

    /**
     * Find System Roles.
     */
    List<Role> findAllBySystemRoleTrue();

    /**
     * Find Default Roles.
     */
    List<Role> findAllByDefaultRoleTrue();
}