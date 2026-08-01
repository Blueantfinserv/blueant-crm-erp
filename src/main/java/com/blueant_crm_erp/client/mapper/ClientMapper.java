package com.blueant_crm_erp.client.mapper;

import com.blueant_crm_erp.client.dto.request.UpdateClientRequest;
import com.blueant_crm_erp.client.dto.response.ClientResponse;
import com.blueant_crm_erp.client.entity.Client;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ClientMapper {

    @Mapping(target = "leadId", source = "lead.id")
    @Mapping(target = "relationshipManagerId", source = "relationshipManager.id")
    @Mapping(target = "crmOwnerId", source = "crmOwner.id")
    ClientResponse toResponse(Client entity);

    List<ClientResponse> toResponseList(List<Client> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateClientRequest request, @MappingTarget Client entity);
}
