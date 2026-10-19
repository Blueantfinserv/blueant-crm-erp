package com.blueant_crm_erp.client.repository;

import com.blueant_crm_erp.client.entity.ClientDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientDocumentRepository extends JpaRepository<ClientDocument, Long> {
    List<ClientDocument> findByClientIdOrderByCreatedAtAsc(Long clientId);
    List<ClientDocument> findByClientId(Long clientId);
    boolean existsByDocumentReference(String documentReference);
    boolean existsByDocumentReferenceContaining(String filename);
}
