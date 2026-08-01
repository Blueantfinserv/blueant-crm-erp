package com.blueant_crm_erp.document.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DocumentUploadedEvent extends ApplicationEvent {

    private final Long documentId;
    private final String fileName;
    private final String uploadedBy;

    public DocumentUploadedEvent(Object source, Long documentId, String fileName, String uploadedBy) {
        super(source);
        this.documentId = documentId;
        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
    }
}
