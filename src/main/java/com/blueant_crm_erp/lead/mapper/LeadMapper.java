package com.blueant_crm_erp.lead.mapper;

import com.blueant_crm_erp.lead.dto.request.CreateLeadRequest;
import com.blueant_crm_erp.lead.dto.request.UpdateLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadDetailResponse;
import com.blueant_crm_erp.lead.dto.response.LeadDropdownResponse;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.dto.response.LeadSummaryResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * ============================================================================
 * Lead Mapper
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * MapStruct mapper for converting Lead Entity to DTOs and vice versa.
 *
 * Field name mappings required (entity name → DTO name):
 *   id                → leadId
 *   alternateNumber   → alternateMobileNumber
 *   nextPlanDate      → nextPlanDate  (LocalDateTime → LocalDate via @Mapping)
 *   lastCallDate      → lastCallDate  (LocalDateTime → LocalDate via @Mapping)
 *
 * Fields that now auto-map by matching names (no @Mapping needed):
 *   uniqueLeadId, leadCode, clientName, mobileNumber, email, location,
 *   companyName, leadSource, leadStatus, leadStage, priority,
 *   duplicateLeadStatus, remarks
 *
 * ============================================================================
 */
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface LeadMapper {

    /**
     * Create Request -> Entity
     */
    Lead toEntity(CreateLeadRequest request);

    // =========================================================================
    // Entity -> LeadResponse  (used by list, create, update, assign, transfer)
    // =========================================================================

    /**
     * Entity -> Response
     */
    @Mapping(source = "id",                              target = "leadId")
    @Mapping(source = "uniqueLeadId",                    target = "uniqueLeadId")
    @Mapping(source = "assignedSalesPerson.id",          target = "assignedUserId")
    @Mapping(source = "assignedSalesPerson.employeeCode",target = "assignedEmployeeCode")
    @Mapping(source = "assignedSalesPerson.fullName",    target = "assignedEmployeeName")
    @Mapping(target = "nextPlanDate",                    expression = "java(entity.getNextPlanDate() != null ? entity.getNextPlanDate().toLocalDate() : null)")
    LeadResponse toResponse(Lead entity);

    // =========================================================================
    // Entity -> LeadDetailResponse  (used by GET /leads/{uniqueLeadId})
    // =========================================================================

    /**
     * Entity -> Detail Response
     */
    @Mapping(source = "id",                              target = "leadId")
    @Mapping(source = "alternateNumber",                 target = "alternateMobileNumber")
    @Mapping(source = "uniqueLeadId",                    target = "uniqueLeadId")
    @Mapping(source = "assignedSalesPerson.id",          target = "assignedUserId")
    @Mapping(source = "assignedSalesPerson.employeeCode",target = "assignedEmployeeCode")
    @Mapping(source = "assignedSalesPerson.fullName",    target = "assignedEmployeeName")
    @Mapping(source = "assignedLeader.id",               target = "leaderId")
    @Mapping(source = "assignedLeader.fullName",         target = "leaderName")
    @Mapping(target = "nextPlanDate",                    expression = "java(entity.getNextPlanDate() != null ? entity.getNextPlanDate().toLocalDate() : null)")
    @Mapping(target = "lastCallDate",                    expression = "java(entity.getLastCallDate() != null ? entity.getLastCallDate().toLocalDate() : null)")
    @Mapping(target = "audit",                           ignore = true)
    LeadDetailResponse toDetailResponse(Lead entity);

    // =========================================================================
    // Entity -> LeadSummaryResponse  (used by search and filter pagination)
    // =========================================================================

    /**
     * Entity -> Summary Response
     */
    @Mapping(source = "id",   target = "leadId")
    @Mapping(source = "uniqueLeadId", target = "uniqueLeadId")
    @Mapping(target = "nextPlanDate", expression = "java(entity.getNextPlanDate() != null ? entity.getNextPlanDate().toLocalDate() : null)")
    LeadSummaryResponse toSummaryResponse(Lead entity);

    // =========================================================================
    // Entity -> LeadDropdownResponse  (used by dropdowns and autocomplete)
    // =========================================================================

    /**
     * Entity -> Dropdown Response
     */
    @Mapping(source = "id", target = "leadId")
    @Mapping(source = "uniqueLeadId", target = "uniqueLeadId")
    LeadDropdownResponse toDropdownResponse(Lead entity);

    // =========================================================================
    // List mappings
    // =========================================================================

    /**
     * Entity List -> Response List
     */
    List<LeadResponse> toResponseList(List<Lead> entities);

    /**
     * Entity List -> Summary List
     */
    List<LeadSummaryResponse> toSummaryResponseList(List<Lead> entities);

    /**
     * Entity List -> Dropdown List
     */
    List<LeadDropdownResponse> toDropdownResponseList(List<Lead> entities);

    // =========================================================================
    // Update mapping
    // =========================================================================

    /**
     * Update existing entity from Update Request.
     * Null values will be ignored.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateLeadRequest request,
                      @MappingTarget Lead entity);

}