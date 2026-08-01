package com.blueant_crm_erp.document.entity;

import com.blueant_crm_erp.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document extends BaseEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "s3_key", nullable = false, unique = true)
    private String s3Key;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "version")
    private Integer version;
}
