package com.blueant_crm_erp.document.specification;

import com.blueant_crm_erp.document.entity.Document;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSpecification {

    public static Specification<Document> hasFileNameLike(String fileName) {
        return (root, query, criteriaBuilder) ->
                fileName == null ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("fileName")), "%" + fileName.toLowerCase() + "%");
    }

    public static Specification<Document> hasUploadedBy(String uploadedBy) {
        return (root, query, criteriaBuilder) ->
                uploadedBy == null ? null : criteriaBuilder.equal(root.get("uploadedBy"), uploadedBy);
    }
}
