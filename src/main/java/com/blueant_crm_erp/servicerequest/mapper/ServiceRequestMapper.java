package com.blueant_crm_erp.servicerequest.mapper;

import com.blueant_crm_erp.servicerequest.dto.request.UpdateServiceRequest;
import com.blueant_crm_erp.servicerequest.dto.response.ServiceRequestResponse;
import com.blueant_crm_erp.servicerequest.entity.ServiceRequest;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ServiceRequestMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "assignedCrmId", source = "assignedCrm.id")
    ServiceRequestResponse toResponse(ServiceRequest entity);

    List<ServiceRequestResponse> toResponseList(List<ServiceRequest> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateServiceRequest request, @MappingTarget ServiceRequest entity);
}
