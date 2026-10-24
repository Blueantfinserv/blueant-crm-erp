-- ==============================================================================
-- BlueAnt CRM ERP - Schema Migration V53
-- Description: Create client_meeting_verification_business_items table for
--              storing category-wise business items and individual amounts.
-- ==============================================================================

CREATE TABLE client_meeting_verification_business_items (
    id                              BIGINT          NOT NULL AUTO_INCREMENT,
    client_meeting_verification_id  BIGINT          NOT NULL,
    business_type                   VARCHAR(50)     NOT NULL,
    amount                          DECIMAL(15, 2)  NULL,
    created_at                      DATETIME        NOT NULL,
    created_by                      VARCHAR(100)    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cl_verif_item_type UNIQUE (client_meeting_verification_id, business_type),
    CONSTRAINT fk_cl_verif_item_verif FOREIGN KEY (client_meeting_verification_id)
        REFERENCES client_meeting_verifications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cl_verif_item_verif ON client_meeting_verification_business_items(client_meeting_verification_id);
