package com.blueant_crm_erp.client.enums;

import java.util.Set;

public enum ClientDocumentType {
    // 6 Supported document types
    PAN_CARD,
    AADHAAR_CARD,
    PASSPORT,
    CHEQUE,
    SIGNATURE,
    OTHER,

    // Legacy types preserved for database and backward compatibility
    PAN_KYC,
    APPLICATION_FORM,
    NOMINEE_DOCUMENTS,
    INVESTMENT_DOCUMENTS;

    public static final Set<ClientDocumentType> SUPPORTED_TYPES = Set.of(
            PAN_CARD,
            AADHAAR_CARD,
            PASSPORT,
            CHEQUE,
            SIGNATURE,
            OTHER
    );

    public static boolean isSupported(ClientDocumentType type) {
        if (type == null) {
            return false;
        }
        return SUPPORTED_TYPES.contains(type)
                || type == PAN_KYC
                || type == APPLICATION_FORM
                || type == NOMINEE_DOCUMENTS
                || type == INVESTMENT_DOCUMENTS;
    }

    public ClientDocumentType toCanonical() {
        if (this == PAN_KYC) {
            return PAN_CARD;
        }
        if (this == APPLICATION_FORM || this == NOMINEE_DOCUMENTS || this == INVESTMENT_DOCUMENTS) {
            return OTHER;
        }
        return this;
    }
}
