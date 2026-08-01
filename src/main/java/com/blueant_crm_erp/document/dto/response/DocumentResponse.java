package com.blueant_crm_erp.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String uploadedBy;
    private Integer version;
    private LocalDateTime createdAt;
}
