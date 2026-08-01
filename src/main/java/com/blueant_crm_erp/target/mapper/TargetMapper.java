package com.blueant_crm_erp.target.mapper;

import com.blueant_crm_erp.target.dto.response.TargetResponse;
import com.blueant_crm_erp.target.entity.Target;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface TargetMapper {

    @Mapping(target = "userId", source = "user.id")
    TargetResponse toResponse(Target entity);

    List<TargetResponse> toResponseList(List<Target> entities);
}
