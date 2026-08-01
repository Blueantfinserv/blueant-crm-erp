package com.blueant_crm_erp.audit.mapper;

import com.blueant_crm_erp.audit.dto.response.AuditLogResponse;
import com.blueant_crm_erp.audit.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AuditMapper {

    AuditLogResponse toResponse(AuditLog entity);

    List<AuditLogResponse> toResponseList(List<AuditLog> entities);
}
