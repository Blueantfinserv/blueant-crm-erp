package com.blueant_crm_erp.client.dto.response;

import com.blueant_crm_erp.client.enums.ClientDocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDocumentUploadResponse {

    private String documentReference;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private ClientDocumentType documentType;
    private String uploadStatus;
}
