package com.blueant_crm_erp.document.listener;

import com.blueant_crm_erp.document.event.DocumentUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DocumentEventListener {

    @Async
    @EventListener
    public void handleDocumentUploadedEvent(DocumentUploadedEvent event) {
        log.info("Document uploaded: [ID: {}, Name: {}, By: {}]", 
                event.getDocumentId(), event.getFileName(), event.getUploadedBy());
        // Additional async processing logic (e.g. virus scan, OCR triggering)
    }
}
