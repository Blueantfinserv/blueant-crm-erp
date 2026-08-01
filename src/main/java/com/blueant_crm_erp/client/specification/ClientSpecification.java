package com.blueant_crm_erp.client.specification;

import com.blueant_crm_erp.client.entity.Client;
import com.blueant_crm_erp.client.enums.ClientStatus;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecification {

    public static Specification<Client> hasClientCode(String code) {
        return (root, query, criteriaBuilder) ->
                code == null ? null : criteriaBuilder.equal(root.get("clientCode"), code);
    }

    public static Specification<Client> hasStatus(ClientStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("clientStatus"), status);
    }
    
    public static Specification<Client> hasRelationshipManager(Long rmId) {
        return (root, query, criteriaBuilder) ->
                rmId == null ? null : criteriaBuilder.equal(root.get("relationshipManager").get("id"), rmId);
    }
}
