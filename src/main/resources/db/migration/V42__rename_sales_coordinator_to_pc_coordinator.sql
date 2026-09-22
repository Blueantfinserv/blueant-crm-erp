-- ============================================================================
-- Migration: V42__rename_sales_coordinator_to_pc_coordinator.sql
-- Description: In-place rename of role SALES_COORDINATOR to PC_COORDINATOR.
-- Preserves role ID 8, existing user assignments, and permissions.
-- ============================================================================

UPDATE roles
SET code = 'PC_COORDINATOR',
    name = 'PC Coordinator',
    description = 'PC Coordinator Role',
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE id = 8;
