package com.blueant_crm_erp.client.service;

import com.blueant_crm_erp.client.dto.response.ClientDocumentResponse;
import com.blueant_crm_erp.client.dto.response.ClientDocumentUploadResponse;
import com.blueant_crm_erp.client.entity.ClientDocument;
import com.blueant_crm_erp.client.enums.ClientDocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClientDocumentService {

    ClientDocumentUploadResponse uploadKycDocument(MultipartFile file, ClientDocumentType documentType, String currentUserIdentifier);

    ClientDocumentResponse attachDocumentToClient(
            String clientCode,
            MultipartFile file,
            ClientDocumentType documentType,
            String documentName,
            String remarks,
            Boolean received,
            String currentUserIdentifier
    );

    byte[] downloadDocument(String clientCode, Long documentId, String currentUserIdentifier);

    ClientDocument getClientDocument(String clientCode, Long documentId, String currentUserIdentifier);

    List<ClientDocumentResponse> getClientDocuments(String clientCode, String currentUserIdentifier);
}
