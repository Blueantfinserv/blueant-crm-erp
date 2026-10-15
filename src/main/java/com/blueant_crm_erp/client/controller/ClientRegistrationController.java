package com.blueant_crm_erp.client.controller;

import com.blueant_crm_erp.client.dto.request.CreateClientRegistrationRequest;
import com.blueant_crm_erp.client.dto.response.ClientRegistrationResponse;
import com.blueant_crm_erp.client.service.ClientRegistrationService;
import com.blueant_crm_erp.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import com.blueant_crm_erp.client.dto.response.ClientDocumentResponse;
import com.blueant_crm_erp.client.dto.response.ClientDocumentUploadResponse;
import com.blueant_crm_erp.client.entity.ClientDocument;
import com.blueant_crm_erp.client.enums.ClientDocumentType;
import com.blueant_crm_erp.client.service.ClientDocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client Registration API", description = "Endpoints for external client registration, profile, and KYC documents")
public class ClientRegistrationController {

    private final ClientRegistrationService clientRegistrationService;
    private final ClientDocumentService clientDocumentService;

    @Operation(summary = "Register new external client", description = "Registers an external client from Excel/external system and assigns them to an eligible active RM or SC")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ClientRegistrationResponse> registerClient(
            @Valid @RequestBody CreateClientRegistrationRequest request,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientRegistrationResponse response = clientRegistrationService.registerClient(request, currentUser);
        return ApiResponse.success("Client registered and assigned successfully", response);
    }

    @Operation(summary = "Get client full profile", description = "Retrieves complete profile details of a client including personal, source, investment, nominees, documents, and assignment history")
    @GetMapping("/{clientCode}/profile")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<com.blueant_crm_erp.client.dto.response.ClientProfileResponse> getClientProfile(
            @PathVariable String clientCode,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        com.blueant_crm_erp.client.dto.response.ClientProfileResponse response = clientRegistrationService.getClientProfile(clientCode, currentUser);
        return ApiResponse.success("Client profile retrieved successfully", response);
    }

    @Operation(summary = "Upload KYC document file", description = "Uploads a client KYC document file (JPG, JPEG, PNG, PDF max 10MB) and returns the secure document reference key")
    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ClientDocumentUploadResponse> uploadKycDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false) ClientDocumentType documentType,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientDocumentUploadResponse response = clientDocumentService.uploadKycDocument(file, documentType, currentUser);
        return ApiResponse.success("KYC document uploaded successfully", response);
    }

    @Operation(summary = "Attach document to client", description = "Uploads and attaches a KYC document to an existing client")
    @PostMapping(value = "/{clientCode}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<ClientDocumentResponse> attachDocumentToClient(
            @PathVariable String clientCode,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") ClientDocumentType documentType,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "received", required = false, defaultValue = "true") Boolean received,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientDocumentResponse response = clientDocumentService.attachDocumentToClient(
                clientCode, file, documentType, documentName, remarks, received, currentUser);
        return ApiResponse.success("Document attached to client successfully", response);
    }

    @Operation(summary = "Get client documents", description = "Retrieves all KYC documents checklist and attachments for a client")
    @GetMapping("/{clientCode}/documents")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<ClientDocumentResponse>> getClientDocuments(
            @PathVariable String clientCode,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        List<ClientDocumentResponse> documents = clientDocumentService.getClientDocuments(clientCode, currentUser);
        return ApiResponse.success("Client documents retrieved successfully", documents);
    }

    @Operation(summary = "Download client document", description = "Securely downloads an attached KYC document file for a client. Enforces role and client ownership authorization.")
    @GetMapping("/{clientCode}/documents/{documentId}/download")
    @PreAuthorize("hasAuthority('PHYSICAL_LEAD_ASSIGN') or hasAnyRole('CRM_ONBOARDING', 'RELATIONSHIP_MANAGER', 'SALES_COORDINATOR', 'PC_COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> downloadClientDocument(
            @PathVariable String clientCode,
            @PathVariable Long documentId,
            Principal principal) {
        String currentUser = principal != null ? principal.getName() : "system";
        ClientDocument doc = clientDocumentService.getClientDocument(clientCode, documentId, currentUser);
        byte[] fileData = clientDocumentService.downloadDocument(clientCode, documentId, currentUser);

        String filename = doc.getDocumentName() != null ? doc.getDocumentName() : "document_" + documentId;
        String contentType = determineContentType(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileData);
    }

    private String determineContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }
}

