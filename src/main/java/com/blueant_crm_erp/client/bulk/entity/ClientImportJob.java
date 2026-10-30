package com.blueant_crm_erp.client.bulk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_import_jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientImportJob {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_hash_sha256", nullable = false, length = 64)
    private String fileHashSha256;

    @Column(name = "storage_file_key", length = 255)
    private String storageFileKey;

    @Column(name = "total_rows", nullable = false)
    @Builder.Default
    private Integer totalRows = 0;

    @Column(name = "processed_rows", nullable = false)
    @Builder.Default
    private Integer processedRows = 0;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "error_details_json", columnDefinition = "LONGTEXT")
    private String errorDetailsJson;

    @Column(name = "start_client_code", length = 50)
    private String startClientCode;

    @Column(name = "end_client_code", length = 50)
    private String endClientCode;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "heartbeat_at")
    private LocalDateTime heartbeatAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}

