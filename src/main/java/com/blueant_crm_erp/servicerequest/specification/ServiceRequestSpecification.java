package com.blueant_crm_erp.servicerequest.specification;

import com.blueant_crm_erp.servicerequest.entity.ServiceRequest;
import com.blueant_crm_erp.servicerequest.enums.ServiceRequestStatus;
import org.springframework.data.jpa.domain.Specification;

public class ServiceRequestSpecification {

    public static Specification<ServiceRequest> hasSrCode(String code) {
        return (root, query, criteriaBuilder) ->
                code == null ? null : criteriaBuilder.equal(root.get("srCode"), code);
    }

    public static Specification<ServiceRequest> hasStatus(ServiceRequestStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("srStatus"), status);
    }
    
    public static Specification<ServiceRequest> hasClientId(Long clientId) {
        return (root, query, criteriaBuilder) ->
                clientId == null ? null : criteriaBuilder.equal(root.get("client").get("id"), clientId);
    }
}
