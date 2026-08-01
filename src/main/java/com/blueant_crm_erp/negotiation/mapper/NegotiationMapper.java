package com.blueant_crm_erp.negotiation.mapper;

import com.blueant_crm_erp.negotiation.dto.response.NegotiationResponse;
import com.blueant_crm_erp.negotiation.entity.Negotiation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface NegotiationMapper {

    @Mapping(target = "proposalId", source = "proposal.id")
    NegotiationResponse toResponse(Negotiation entity);

    List<NegotiationResponse> toResponseList(List<Negotiation> entities);
}
