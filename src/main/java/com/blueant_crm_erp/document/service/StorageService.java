package com.blueant_crm_erp.document.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file);
    byte[] downloadFile(String fileKey);
    void deleteFile(String fileKey);
}
