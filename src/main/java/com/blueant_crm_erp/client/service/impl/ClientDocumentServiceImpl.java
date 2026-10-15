package com.blueant_crm_erp.client.service.impl;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.client.dto.response.ClientDocumentResponse;
import com.blueant_crm_erp.client.dto.response.ClientDocumentUploadResponse;
import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.entity.ClientDocument;
import com.blueant_crm_erp.client.enums.ClientDocumentType;
import com.blueant_crm_erp.client.repository.ClientDocumentRepository;
import com.blueant_crm_erp.client.repository.ClientRepository;
import com.blueant_crm_erp.client.service.ClientDocumentService;
import com.blueant_crm_erp.client.validator.ClientDocumentValidator;
import com.blueant_crm_erp.document.service.StorageService;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientDocumentServiceImpl implements ClientDocumentService {

    private final StorageService storageService;
    private final ClientDocumentValidator clientDocumentValidator;
    private final ClientRepository clientRepository;
    private final ClientDocumentRepository clientDocumentRepository;
    private final UserRepository userRepository;

    @Override
    public ClientDocumentUploadResponse uploadKycDocument(MultipartFile file, ClientDocumentType documentType, String currentUserIdentifier) {
        log.info("Uploading KYC document: name={}, size={}, type={}, by={}",
                file != null ? file.getOriginalFilename() : "null",
                file != null ? file.getSize() : 0,
                documentType,
                currentUserIdentifier);

        // 1. Strict security validation (size, extension, MIME, magic bytes signature)
        clientDocumentValidator.validateKycDocument(file);

        // 2. Upload to secure storage service
        String s3Key = storageService.uploadFile(file);
        if (!StringUtils.hasText(s3Key)) {
            log.error("Storage service returned empty file key for uploaded file: {}", file.getOriginalFilename());
            throw new BadRequestException("Failed to securely store uploaded document.");
        }

        log.info("KYC document uploaded successfully with reference key: {}", s3Key);

        return ClientDocumentUploadResponse.builder()
                .documentReference(s3Key)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .documentType(documentType)
                .uploadStatus("SUCCESS")
                .build();
    }

    @Override
    @Transactional
    public ClientDocumentResponse attachDocumentToClient(
            String clientCode,
            MultipartFile file,
            ClientDocumentType documentType,
            String documentName,
            String remarks,
            Boolean received,
            String currentUserIdentifier) {

        log.info("Attaching document to client: {}, type: {}, by: {}", clientCode, documentType, currentUserIdentifier);

        if (documentType == null) {
            throw new BadRequestException("Document type is required.");
        }

        Client client = resolveClientOrThrow(clientCode);
        User currentUser = resolveUserOrNull(currentUserIdentifier);
        validateClientAccess(client, currentUser);

        // Security validation on uploaded file
        clientDocumentValidator.validateKycDocument(file);

        String s3Key = storageService.uploadFile(file);

        String finalDocName = StringUtils.hasText(documentName)
                ? documentName.trim()
                : (file.getOriginalFilename() != null ? file.getOriginalFilename().trim() : documentType.name());

        ClientDocument document = ClientDocument.builder()
                .client(client)
                .documentType(documentType)
                .documentName(finalDocName)
                .received(Boolean.TRUE.equals(received))
                .receivedDate(Boolean.TRUE.equals(received) ? LocalDate.now() : null)
                .verificationStatus(VerificationStatus.PENDING)
                .documentReference(s3Key)
                .remarks(remarks != null ? remarks.trim() : null)
                .build();

        ClientDocument savedDoc = clientDocumentRepository.save(document);
        log.info("Document attached successfully. DocumentId: {}, ClientCode: {}", savedDoc.getId(), clientCode);

        return mapToDocumentResponse(savedDoc);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(String clientCode, Long documentId, String currentUserIdentifier) {
        log.info("Downloading documentId: {} for client: {} by: {}", documentId, clientCode, currentUserIdentifier);

        ClientDocument doc = getClientDocument(clientCode, documentId, currentUserIdentifier);

        if (!StringUtils.hasText(doc.getDocumentReference())) {
            throw new ResourceNotFoundException("No file content associated with document ID: " + documentId);
        }

        byte[] content = storageService.downloadFile(doc.getDocumentReference());
        if (content == null || content.length == 0) {
            throw new ResourceNotFoundException("Document content is empty or unavailable.");
        }

        return content;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDocument getClientDocument(String clientCode, Long documentId, String currentUserIdentifier) {
        Client client = resolveClientOrThrow(clientCode);
        User currentUser = resolveUserOrNull(currentUserIdentifier);
        validateClientAccess(client, currentUser);

        ClientDocument doc = clientDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        if (!doc.getClient().getId().equals(client.getId())) {
            throw new ResourceNotFoundException("Document with ID " + documentId + " does not belong to client " + clientCode);
        }

        return doc;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDocumentResponse> getClientDocuments(String clientCode, String currentUserIdentifier) {
        Client client = resolveClientOrThrow(clientCode);
        User currentUser = resolveUserOrNull(currentUserIdentifier);
        validateClientAccess(client, currentUser);

        return clientDocumentRepository.findByClientIdOrderByCreatedAtAsc(client.getId()).stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    private Client resolveClientOrThrow(String clientCode) {
        if (!StringUtils.hasText(clientCode)) {
            throw new BadRequestException("Client code is required.");
        }
        return clientRepository.findByClientCode(clientCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with code: " + clientCode));
    }

    private User resolveUserOrNull(String identifier) {
        if (!StringUtils.hasText(identifier) || "system".equalsIgnoreCase(identifier)) {
            return null;
        }
        return userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(identifier.trim())
                .orElseGet(() -> userRepository.findByEmailIgnoreCaseAndDeletedFalse(identifier.trim()).orElse(null));
    }

    private void validateClientAccess(Client client, User currentUser) {
        if (currentUser == null) {
            // If internal system call or principal is missing but authenticated by security filters
            return;
        }

        if (currentUser.getRole() != null) {
            String roleCode = currentUser.getRole().getCode().toUpperCase();
            if (roleCode.contains("ADMIN") || roleCode.contains("SUPER_ADMIN")) {
                return; // Full access
            }
            if (roleCode.equals(BootstrapConstants.ROLE_CRM_ONBOARDING) ||
                roleCode.equals("ROLE_" + BootstrapConstants.ROLE_CRM_ONBOARDING)) {
                return; // CRM Onboarding can access all client KYC documents
            }
            if (roleCode.equals(BootstrapConstants.ROLE_PC_COORDINATOR) ||
                roleCode.equals("ROLE_" + BootstrapConstants.ROLE_PC_COORDINATOR)) {
                return; // PC Coordinator can verify documents
            }
        }

        // For RM or SC: strictly enforce assignment ownership
        User assignedSalesPerson = client.getSalesPerson();
        if (assignedSalesPerson == null || !assignedSalesPerson.getId().equals(currentUser.getId())) {
            log.warn("Access denied: User {} is not authorized to access documents for client {}",
                    currentUser.getEmployeeCode(), client.getClientCode());
            throw new AccessDeniedException("You are not authorized to access documents for client: " + client.getClientCode());
        }
    }

    private ClientDocumentResponse mapToDocumentResponse(ClientDocument doc) {
        return ClientDocumentResponse.builder()
                .id(doc.getId())
                .documentType(doc.getDocumentType())
                .documentName(doc.getDocumentName())
                .received(doc.getReceived())
                .receivedDate(doc.getReceivedDate())
                .verificationStatus(doc.getVerificationStatus())
                .documentReference(doc.getDocumentReference())
                .remarks(doc.getRemarks())
                .build();
    }
}
