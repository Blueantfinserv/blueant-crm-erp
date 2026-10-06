-- ============================================================================
-- Migration: V46__standardize_designation_master.sql
-- Description: Standardize Designation Master to exactly 6 active designations:
--   ID 1: code = BH, name = Business Head,        dept = 1 (Sales), level = 1, order = 1
--   ID 2: code = TL, name = Team Leader,          dept = 1 (Sales), level = 3, order = 2
--   ID 3: code = SC, name = Sales Coordinator,    dept = 1 (Sales), level = 4, order = 3
--   ID 4: code = SM, name = Sales Manager,        dept = 1 (Sales), level = 2, order = 4
--   ID 5: code = RM, name = Relationship Manager, dept = 1 (Sales), level = 4, order = 5
--   ID 6: code = PC, name = PC Coordinator,       dept = 3 (Ops),   level = 4, order = 6
--
-- Obsolete designations (IDs > 6) are soft-deleted:
--   status = 'INACTIVE', is_deleted = 1, deleted_at = NOW(), deleted_by = 'SYSTEM'
--
-- Safety Guarantees:
--   - Uses temporary code/name values to prevent unique constraint (uk_designation_code,
--     uk_designation_name) violations during ID 2/3/4 repurposing.
--   - Preserves all primary keys (IDs 1-6).
--   - Modifies ONLY the designations table.
--   - Does NOT touch users, roles, departments, leads, meetings, or other business tables.
-- ============================================================================

-- Step 1: Temporarily update ID 2 and ID 3 to release unique constraints on 'SM' and 'TL'
UPDATE designations
SET code = 'SM_TEMP_MIG',
    name = 'Sales Manager Temp',
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 2;

UPDATE designations
SET code = 'TL_TEMP_MIG',
    name = 'Team Leader Temp',
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 3;

-- Step 2: Standardize ID 1 (Business Head)
UPDATE designations
SET code = 'BH',
    name = 'Business Head',
    department_id = 1,
    hierarchy_level = 1,
    display_order = 1,
    description = 'Business Head Designation',
    remarks = 'Standardized Designation Master',
    status = 'ACTIVE',
    is_deleted = 0,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 1;

-- Step 3: Standardize ID 2 (Team Leader)
UPDATE designations
SET code = 'TL',
    name = 'Team Leader',
    department_id = 1,
    hierarchy_level = 3,
    display_order = 2,
    description = 'Team Leader Designation',
    remarks = 'Standardized Designation Master',
    status = 'ACTIVE',
    is_deleted = 0,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 2;

-- Step 4: Standardize ID 3 (Sales Coordinator)
UPDATE designations
SET code = 'SC',
    name = 'Sales Coordinator',
    department_id = 1,
    hierarchy_level = 4,
    display_order = 3,
    description = 'Sales Coordinator Designation',
    remarks = 'Standardized Designation Master',
    status = 'ACTIVE',
    is_deleted = 0,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 3;

-- Step 5: Standardize ID 4 (Sales Manager)
UPDATE designations
SET code = 'SM',
    name = 'Sales Manager',
    department_id = 1,
    hierarchy_level = 2,
    display_order = 4,
    description = 'Sales Manager Designation',
    remarks = 'Standardized Designation Master',
    status = 'ACTIVE',
    is_deleted = 0,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 4;

-- Step 6: Standardize ID 5 (Relationship Manager)
UPDATE designations
SET code = 'RM',
    name = 'Relationship Manager',
    department_id = 1,
    hierarchy_level = 4,
    display_order = 5,
    description = 'Relationship Manager Designation',
    remarks = 'Standardized Designation Master',
    status = 'ACTIVE',
    is_deleted = 0,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 5;

-- Step 7: Standardize ID 6 (PC Coordinator)
UPDATE designations
SET code = 'PC',
    name = 'PC Coordinator',
    department_id = 3,
    hierarchy_level = 4,
    display_order = 6,
    description = 'PC Coordinator Designation',
    remarks = 'Standardized Designation Master',
    status = 'ACTIVE',
    is_deleted = 0,
    deleted_at = NULL,
    deleted_by = NULL,
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 6;

-- Step 8: Soft-delete obsolete designations (IDs > 6)
UPDATE designations
SET status = 'INACTIVE',
    is_deleted = 1,
    deleted_at = NOW(),
    deleted_by = 'SYSTEM',
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id > 6;
