package com.blueant_crm_erp.client.entity;

import com.blueant_crm_erp.client.enums.ClientDocumentType;
import com.blueant_crm_erp.common.base.BaseVersionEntity;
import com.blueant_crm_erp.servicerequest.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "client_documents",
        indexes = {
                @Index(name = "idx_cl_doc_client", columnList = "client_id"),
                @Index(name = "idx_cl_doc_type", columnList = "document_type"),
                @Index(name = "idx_cl_doc_verif_status", columnList = "verification_status")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDocument extends BaseVersionEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cl_doc_client"))
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 100)
    private ClientDocumentType documentType;

    @Column(name = "document_name", length = 150)
    private String documentName;

    @Builder.Default
    @Column(name = "received", nullable = false)
    private Boolean received = Boolean.FALSE;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "document_reference", length = 255)
    private String documentReference;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
