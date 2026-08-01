-- V18__create_documents_table.sql
CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT,
    s3_key VARCHAR(255) NOT NULL UNIQUE,
    bucket_name VARCHAR(100) NOT NULL,
    uploaded_by VARCHAR(100),
    version INT DEFAULT 1,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);
