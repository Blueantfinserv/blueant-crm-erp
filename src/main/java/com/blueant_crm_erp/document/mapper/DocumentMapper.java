package com.blueant_crm_erp.document.mapper;

import com.blueant_crm_erp.document.dto.response.DocumentResponse;
import com.blueant_crm_erp.document.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface DocumentMapper {

    DocumentResponse toResponse(Document entity);

    List<DocumentResponse> toResponseList(List<Document> entities);
}
