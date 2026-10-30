-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V54.1
-- Description: Add storage_file_key to client_import_jobs for durable file
--              storage reference, crash resilience, and restart recovery.
-- ==============================================================================

ALTER TABLE client_import_jobs
    ADD COLUMN storage_file_key VARCHAR(255) NULL AFTER file_hash_sha256;

