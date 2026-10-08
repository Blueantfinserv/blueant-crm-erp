-- ============================================================================
-- Migration: V47__create_crm_onboarding_master_data.sql
-- Description: Create CRM Onboarding master data:
--   Role: ID = 9, code = CRM_ONBOARDING, name = CRM Onboarding, status = ACTIVE
--   Designation: ID = 7 (repurpose inactive/soft-deleted HRE row),
--                code = CRM_ONBOARDING, name = CRM Onboarding,
--                department_id = 2 (CRM), hierarchy_level = 4, display_order = 7,
--                status = ACTIVE, is_deleted = 0
--   Role-Permissions: Map LEAD_READ and PHYSICAL_LEAD_ASSIGN to role ID 9
--
-- Safety Guarantees:
--   - Role ID 7 (SALES_MANAGER) is 100% untouched.
--   - Designations 1-6 are 100% untouched.
--   - Obsolete designations 8-20 are 100% untouched.
--   - Hard fail if preconditions are not met.
-- ============================================================================

DROP PROCEDURE IF EXISTS upgrade_v47_crm_onboarding_master_data;

DELIMITER //

CREATE PROCEDURE upgrade_v47_crm_onboarding_master_data()
BEGIN
    DECLARE v_role_9_count INT;
    DECLARE v_role_code_count INT;
    DECLARE v_desig_7_count INT;
    DECLARE v_desig_7_inactive_count INT;
    DECLARE v_desig_7_users_count INT;
    DECLARE v_dept_2_crm_count INT;
    DECLARE v_desig_code_count INT;

    -- 1. Precondition: Role ID 9 is vacant
    SELECT COUNT(*) INTO v_role_9_count FROM roles WHERE id = 9;
    IF v_role_9_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V47 Precondition Failed: Role ID 9 is already occupied.';
    END IF;

    -- 2. Precondition: Role code CRM_ONBOARDING does not already exist
    SELECT COUNT(*) INTO v_role_code_count FROM roles WHERE code = 'CRM_ONBOARDING';
    IF v_role_code_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V47 Precondition Failed: Role code CRM_ONBOARDING already exists.';
    END IF;

    -- 3. Precondition: Designation ID 7 exists
    SELECT COUNT(*) INTO v_desig_7_count FROM designations WHERE id = 7;
    IF v_desig_7_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V47 Precondition Failed: Designation ID 7 does not exist to repurpose.';
    END IF;

    -- 4. Precondition: Designation ID 7 is inactive and soft-deleted
    SELECT COUNT(*) INTO v_desig_7_inactive_count
    FROM designations
    WHERE id = 7 AND status = 'INACTIVE' AND is_deleted = 1;
    IF v_desig_7_inactive_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V47 Precondition Failed: Designation ID 7 is not in expected INACTIVE/soft-deleted state.';
    END IF;

    -- 5. Precondition: Designation ID 7 has zero user references
    SELECT COUNT(*) INTO v_desig_7_users_count FROM users WHERE designation_id = 7;
    IF v_desig_7_users_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V47 Precondition Failed: Designation ID 7 is referenced by existing users.';
    END IF;

    -- 6. Precondition: CRM department ID 2 exists and is ACTIVE
    SELECT COUNT(*) INTO v_dept_2_crm_count
    FROM departments
    WHERE id = 2 AND code = 'CRM' AND status = 'ACTIVE';
    IF v_dept_2_crm_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V47 Precondition Failed: Department ID 2 (CRM) not found or inactive.';
    END IF;

    -- 7. Precondition: Designation code CRM_ONBOARDING does not already exist
    SELECT COUNT(*) INTO v_desig_code_count
    FROM designations
    WHERE code = 'CRM_ONBOARDING' AND id != 7;
    IF v_desig_code_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V47 Precondition Failed: Designation code CRM_ONBOARDING already exists on another row.';
    END IF;

    -- Step 1: Insert Role ID 9 (CRM_ONBOARDING)
    INSERT INTO roles (
        id,
        code,
        name,
        description,
        remarks,
        display_order,
        system_role,
        default_role,
        status,
        is_deleted,
        created_at,
        created_by,
        updated_at,
        updated_by
    ) VALUES (
        9,
        'CRM_ONBOARDING',
        'CRM Onboarding',
        'CRM Onboarding Role',
        'System Bootstrapped',
        9,
        1,
        0,
        'ACTIVE',
        0,
        NOW(),
        'SYSTEM',
        NOW(),
        'SYSTEM'
    );

    -- Step 2: Repurpose Designation ID 7 to CRM_ONBOARDING
    UPDATE designations
    SET code = 'CRM_ONBOARDING',
        name = 'CRM Onboarding',
        department_id = 2,
        hierarchy_level = 4,
        display_order = 7,
        description = 'CRM Onboarding Designation',
        remarks = 'CRM Onboarding Master Data',
        status = 'ACTIVE',
        is_deleted = 0,
        deleted_at = NULL,
        deleted_by = NULL,
        updated_at = NOW(),
        updated_by = 'SYSTEM'
    WHERE id = 7;

    -- Step 3: Grant least-privilege permissions to Role ID 9
    -- 3a. LEAD_READ
    INSERT INTO role_permissions (role_id, permission_id, is_deleted, created_at, created_by, updated_at, updated_by)
    SELECT 9, p.id, 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'
    FROM permissions p
    WHERE p.code = 'LEAD_READ'
      AND NOT EXISTS (
          SELECT 1 FROM role_permissions rp
          WHERE rp.role_id = 9 AND rp.permission_id = p.id AND rp.is_deleted = 0
      );

    -- 3b. PHYSICAL_LEAD_ASSIGN
    INSERT INTO role_permissions (role_id, permission_id, is_deleted, created_at, created_by, updated_at, updated_by)
    SELECT 9, p.id, 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'
    FROM permissions p
    WHERE p.code = 'PHYSICAL_LEAD_ASSIGN'
      AND NOT EXISTS (
          SELECT 1 FROM role_permissions rp
          WHERE rp.role_id = 9 AND rp.permission_id = p.id AND rp.is_deleted = 0
      );

END //

DELIMITER ;

CALL upgrade_v47_crm_onboarding_master_data();

DROP PROCEDURE IF EXISTS upgrade_v47_crm_onboarding_master_data;
