-- V16__create_phase3_tables.sql
-- Description: Creates tables for Proposal, Negotiation, Client, and Service Request bounded contexts.

-- ============================================================================
-- 1. Proposals
-- ============================================================================
CREATE TABLE proposals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    lead_id BIGINT NOT NULL,
    proposal_code VARCHAR(50) NOT NULL UNIQUE,
    proposal_status VARCHAR(50) NOT NULL,
    
    investment_amount DECIMAL(15,2),
    product_type VARCHAR(100),
    expected_closure_date DATE,
    remarks VARCHAR(1000),
    
    -- Base Audit
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    -- Soft Delete & Versioning
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deleted_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_proposal_lead FOREIGN KEY (lead_id) REFERENCES leads(id)
);

CREATE INDEX idx_proposal_lead ON proposals(lead_id);
CREATE INDEX idx_proposal_status ON proposals(proposal_status);

-- ============================================================================
-- 2. Negotiations
-- ============================================================================
CREATE TABLE negotiations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    proposal_id BIGINT NOT NULL,
    negotiation_code VARCHAR(50) NOT NULL UNIQUE,
    negotiation_status VARCHAR(50) NOT NULL,
    
    final_agreed_amount DECIMAL(15,2),
    final_product_type VARCHAR(100),
    
    -- Base Audit
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    -- Soft Delete & Versioning
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deleted_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_negotiation_proposal FOREIGN KEY (proposal_id) REFERENCES proposals(id)
);

CREATE TABLE negotiation_updates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    negotiation_id BIGINT NOT NULL,
    update_number INT NOT NULL,
    
    negotiation_status VARCHAR(50) NOT NULL,
    discussion VARCHAR(2000),
    agreed_amount DECIMAL(15,2),
    
    -- Audit
    submitted_by VARCHAR(100) NOT NULL,
    submitted_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    
    CONSTRAINT fk_negotiation_update_neg FOREIGN KEY (negotiation_id) REFERENCES negotiations(id),
    CONSTRAINT uk_negotiation_update UNIQUE (negotiation_id, update_number)
);

-- ============================================================================
-- 3. Clients
-- ============================================================================
CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    lead_id BIGINT NOT NULL UNIQUE,
    client_code VARCHAR(50) NOT NULL UNIQUE,
    client_status VARCHAR(50) NOT NULL,
    
    client_name VARCHAR(150) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    pan_number VARCHAR(20),
    
    -- Base Audit
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    -- Soft Delete & Versioning
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deleted_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_client_lead FOREIGN KEY (lead_id) REFERENCES leads(id)
);

CREATE INDEX idx_client_mobile ON clients(mobile_number);
CREATE INDEX idx_client_pan ON clients(pan_number);

-- ============================================================================
-- 4. Service Requests
-- ============================================================================
CREATE TABLE service_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    client_id BIGINT NOT NULL,
    sr_code VARCHAR(50) NOT NULL UNIQUE,
    sr_status VARCHAR(50) NOT NULL,
    request_type VARCHAR(50) NOT NULL,
    
    investment_amount DECIMAL(15,2),
    product_type VARCHAR(100),
    
    assigned_crm_id BIGINT,
    
    -- Base Audit
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    -- Soft Delete & Versioning
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deleted_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_sr_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_sr_crm FOREIGN KEY (assigned_crm_id) REFERENCES users(id)
);

CREATE TABLE service_request_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    service_request_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_status VARCHAR(50) NOT NULL,
    
    document_url VARCHAR(500),
    remarks VARCHAR(1000),
    
    -- Base Audit
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    -- Soft Delete & Versioning
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deleted_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_sr_doc_sr FOREIGN KEY (service_request_id) REFERENCES service_requests(id)
);

CREATE TABLE service_request_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    service_request_id BIGINT NOT NULL,
    verification_status VARCHAR(50) NOT NULL,
    
    verified_by VARCHAR(100),
    verified_at DATETIME,
    rejection_reason VARCHAR(1000),
    
    -- Base Audit
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    -- Soft Delete & Versioning
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deleted_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_sr_verif_sr FOREIGN KEY (service_request_id) REFERENCES service_requests(id)
);

CREATE TABLE crm_assignment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    service_request_id BIGINT NOT NULL,
    assigned_crm_id BIGINT NOT NULL,
    
    assigned_by VARCHAR(100) NOT NULL,
    assigned_at DATETIME NOT NULL,
    remarks VARCHAR(1000),
    
    -- Base Audit
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_crm_assign_sr FOREIGN KEY (service_request_id) REFERENCES service_requests(id),
    CONSTRAINT fk_crm_assign_user FOREIGN KEY (assigned_crm_id) REFERENCES users(id)
);
