package com.blueant_crm_erp.document.controller;

import com.blueant_crm_erp.document.dto.response.DocumentResponse;
import com.blueant_crm_erp.document.entity.Document;
import com.blueant_crm_erp.document.event.DocumentUploadedEvent;
import com.blueant_crm_erp.document.mapper.DocumentMapper;
import com.blueant_crm_erp.document.repository.DocumentRepository;
import com.blueant_crm_erp.document.service.StorageService;
import com.blueant_crm_erp.document.validator.DocumentValidator;
import com.blueant_crm_erp.util.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Controller", description = "Endpoints for managing document uploads and downloads")
public class DocumentController {

    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final DocumentValidator documentValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Upload a document")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        documentValidator.validateUpload(file);
        String fileKey = storageService.uploadFile(file);
        
        String username = SecurityUtil.getCurrentUsername();

        Document document = Document.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .s3Key(fileKey)
                .bucketName("blueant-documents")
                .uploadedBy(username)
                .version(1)
                .build();

        document = documentRepository.save(document);
        
        eventPublisher.publishEvent(new DocumentUploadedEvent(this, document.getId(), document.getFileName(), username));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(documentMapper.toResponse(document));
    }

    @Operation(summary = "Download a document")
    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Optional<Document> documentOpt = documentRepository.findById(id);
        
        if (documentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document document = documentOpt.get();
        byte[] data = storageService.downloadFile(document.getS3Key());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(document.getFileType() != null ? document.getFileType() : "application/octet-stream"))
                .body(data);
    }
}
