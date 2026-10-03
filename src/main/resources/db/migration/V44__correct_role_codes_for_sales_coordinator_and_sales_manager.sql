-- ============================================================================
-- Migration: V44__correct_role_codes_for_sales_coordinator_and_sales_manager.sql
-- Description: Correct role codes and names for ID 4 (Sales Coordinator) and
--              ID 7 (Sales Manager).
--
-- Business Mapping:
--   ID 4: code = SALES_COORDINATOR, name = Sales Coordinator
--   ID 7: code = SALES_MANAGER,     name = Sales Manager
--
-- Safety & Idempotency:
--   - Uses temporary code SALES_COORDINATOR_MIGRATION_TEMP to prevent
--     violating unique constraint uk_role_code on the code column.
--   - Preserves role IDs, foreign keys, user associations, and permissions.
-- ============================================================================

-- Step 1: Temporarily update ID 4 code to avoid collision with unique constraint uk_role_code
UPDATE roles
SET code = 'SALES_COORDINATOR_MIGRATION_TEMP',
    name = 'Sales Coordinator',
    description = 'Sales Coordinator Role',
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 4 AND code != 'SALES_COORDINATOR';

-- Step 2: Update ID 7 from EMPLOYEE to SALES_MANAGER
UPDATE roles
SET code = 'SALES_MANAGER',
    name = 'Sales Manager',
    description = 'Sales Manager Role',
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 7;

-- Step 3: Update ID 4 to final SALES_COORDINATOR
UPDATE roles
SET code = 'SALES_COORDINATOR',
    name = 'Sales Coordinator',
    description = 'Sales Coordinator Role',
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 4;
