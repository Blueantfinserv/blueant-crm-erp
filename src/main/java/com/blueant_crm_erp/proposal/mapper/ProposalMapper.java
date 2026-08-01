package com.blueant_crm_erp.proposal.mapper;

import com.blueant_crm_erp.proposal.dto.request.UpdateProposalRequest;
import com.blueant_crm_erp.proposal.dto.response.ProposalResponse;
import com.blueant_crm_erp.proposal.entity.Proposal;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ProposalMapper {

    @Mapping(target = "leadId", source = "lead.id")
    ProposalResponse toResponse(Proposal entity);

    List<ProposalResponse> toResponseList(List<Proposal> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateProposalRequest request, @MappingTarget Proposal entity);
}
