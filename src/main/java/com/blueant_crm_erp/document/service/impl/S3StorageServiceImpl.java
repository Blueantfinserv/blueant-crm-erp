package com.blueant_crm_erp.document.service.impl;

import com.blueant_crm_erp.document.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageServiceImpl implements StorageService {

    // private final S3Client s3Client; // Uncomment when S3Client is configured as a Bean

    @Value("${aws.s3.bucket.name:blueant-documents}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileKey = UUID.randomUUID().toString() + fileExtension;

        log.info("Mock Uploading file to S3 bucket {}: {}", bucketName, fileKey);
        
        /* Uncomment when AWS SDK is fully configured
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Successfully uploaded file {}", fileKey);
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file", e);
        }
        */

        return fileKey;
    }

    @Override
    public byte[] downloadFile(String fileKey) {
        log.info("Mock Downloading file from S3 bucket {}: {}", bucketName, fileKey);
        /* Uncomment when AWS SDK is fully configured
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (Exception e) {
            log.error("Failed to download file from S3", e);
            throw new RuntimeException("Failed to download file", e);
        }
        */
        return "Mock File Content".getBytes();
    }

    @Override
    public void deleteFile(String fileKey) {
        log.info("Mock Deleting file from S3 bucket {}: {}", bucketName, fileKey);
        /* Uncomment when AWS SDK is fully configured
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        */
    }
}
