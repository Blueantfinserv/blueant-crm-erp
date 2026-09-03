package com.blueant_crm_erp.lead.service.impl;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.exception.lead.LeadNotFoundException;
import com.blueant_crm_erp.lead.constants.LeadConstants;
import com.blueant_crm_erp.lead.dto.request.AssignPhysicalLeadRequest;
import com.blueant_crm_erp.lead.dto.request.CreatePhysicalLeadRequest;
import com.blueant_crm_erp.lead.dto.response.LeadResponse;
import com.blueant_crm_erp.lead.dto.response.PhysicalLeadAssignmentResponse;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.*;
import com.blueant_crm_erp.lead.mapper.LeadMapper;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadCodeGeneratorService;
import com.blueant_crm_erp.lead.service.PhysicalLeadService;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PhysicalLeadServiceImpl implements PhysicalLeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final LeadMapper leadMapper;
    private final LeadCodeGeneratorService leadCodeGeneratorService;

    @Override
    public PhysicalLeadAssignmentResponse createPhysicalLead(CreatePhysicalLeadRequest request, String currentUserIdentifier) {
        log.info("Creating and assigning physical lead for client: {} to sales person: {} by coordinator: {}", 
                request.getClientName(), request.getSalesPersonEmployeeCode(), currentUserIdentifier);

        User coordinator = resolveUser(currentUserIdentifier);

        if (leadRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new BadRequestException(LeadConstants.LEAD_DUPLICATE_MOBILE);
        }

        if (!StringUtils.hasText(request.getSalesPersonEmployeeCode())) {
            throw new BadRequestException("Sales Person employee code is required for physical lead creation.");
        }

        User assignedSalesPerson = resolveSalesPerson(request.getSalesPersonEmployeeCode());
        LocalDateTime now = LocalDateTime.now();

        Lead lead = Lead.builder()
                .leadCode(leadCodeGeneratorService.generateNextLeadCode())
                .uniqueLeadId(UUID.randomUUID().toString())
                .clientName(request.getClientName())
                .mobileNumber(request.getMobileNumber())
                .alternateNumber(request.getAlternateMobileNumber())
                .email(request.getEmail())
                .speciality(request.getSpeciality())
                .location(request.getLocation())
                .clinicAddress(request.getClinicAddress())
                .remarks(request.getRemarks())
                .isPhysicalLead(true)
                .leadSource(LeadSource.FIELD_VISIT)
                .leadType(LeadType.MUTUAL_FUND)
                .priority(LeadPriority.MEDIUM)
                .duplicateLeadStatus(DuplicateLeadStatus.ORIGINAL)
                .leadStatus(LeadStatus.ASSIGNED)
                .leadStage(LeadStage.LEAD_ASSIGNED)
                .assignedSalesPerson(assignedSalesPerson)
                .assignedBy(coordinator)
                .assignedAt(now)
                .assignmentSource("SALES_COORDINATOR")
                .build();

        lead = leadRepository.save(lead);
        log.info("Physical lead created and assigned successfully with code: {} to Sales Person {}", lead.getLeadCode(), assignedSalesPerson.getEmployeeCode());

        return buildAssignmentResponse(lead, "Physical lead created and assigned successfully to Sales Person " + assignedSalesPerson.getFullName());
    }

    @Override
    public PhysicalLeadAssignmentResponse assignPhysicalLead(String leadCode, AssignPhysicalLeadRequest request, String currentUserIdentifier) {
        log.info("Assigning physical lead: {} to sales person: {} by coordinator: {}", leadCode, request.getSalesPersonEmployeeCode(), currentUserIdentifier);

        User coordinator = resolveUser(currentUserIdentifier);

        Lead lead = leadRepository.findByLeadCode(leadCode)
                .orElseGet(() -> leadRepository.findByUniqueLeadId(leadCode)
                        .orElseThrow(() -> new LeadNotFoundException(leadCode)));

        if (lead.getAssignedSalesPerson() != null) {
            throw new BadRequestException("Physical lead " + leadCode + " is already assigned to Sales Person: " + lead.getAssignedSalesPerson().getFullName());
        }

        User targetSalesPerson = resolveSalesPerson(request.getSalesPersonEmployeeCode());

        lead.setAssignedSalesPerson(targetSalesPerson);
        lead.setAssignedBy(coordinator);
        lead.setAssignedAt(LocalDateTime.now());
        lead.setAssignmentSource("SALES_COORDINATOR");
        lead.setIsPhysicalLead(true);
        lead.setLeadStatus(LeadStatus.ASSIGNED);
        lead.setLeadStage(LeadStage.LEAD_ASSIGNED);

        if (StringUtils.hasText(request.getAssignmentReason())) {
            String updatedRemarks = (lead.getRemarks() != null ? lead.getRemarks() + " | " : "") 
                    + "Assigned by Sales Coordinator: " + request.getAssignmentReason();
            lead.setRemarks(updatedRemarks);
        }

        lead = leadRepository.save(lead);
        log.info("Physical lead {} successfully assigned to Sales Person {}", lead.getLeadCode(), targetSalesPerson.getEmployeeCode());

        return buildAssignmentResponse(lead, "Physical lead assigned successfully to Sales Person " + targetSalesPerson.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> getEligiblePhysicalLeads(Pageable pageable) {
        Page<Lead> page = leadRepository.findByIsPhysicalLeadTrueAndAssignedSalesPersonIsNull(pageable);
        return PageResponse.of(page.map(leadMapper::toResponse));
    }

    private User resolveUser(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            return null;
        }
        User user = userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(identifier)
                .orElseGet(() -> userRepository.findByEmailIgnoreCaseAndDeletedFalse(identifier)
                        .orElse(null));

        if (user == null) {
            try {
                Long id = Long.parseLong(identifier);
                user = userRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignored) {}
        }
        return user;
    }

    private User resolveSalesPerson(String salesPersonIdentifier) {
        User user = userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(salesPersonIdentifier)
                .orElseGet(() -> userRepository.findByEmailIgnoreCaseAndDeletedFalse(salesPersonIdentifier)
                        .orElse(null));

        if (user == null) {
            // Try parsing as ID if numeric
            try {
                Long id = Long.parseLong(salesPersonIdentifier);
                user = userRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignored) {}
        }

        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ResourceNotFoundException("Sales Person not found with code/ID: " + salesPersonIdentifier);
        }

        if (!user.isActive()) {
            throw new BadRequestException("Selected Sales Person (" + user.getFullName() + ") is inactive or ineligible for lead assignment.");
        }

        return user;
    }

    private PhysicalLeadAssignmentResponse buildAssignmentResponse(Lead lead, String message) {
        User salesPerson = lead.getAssignedSalesPerson();
        User coordinator = lead.getAssignedBy();

        return PhysicalLeadAssignmentResponse.builder()
                .leadId(lead.getId())
                .leadCode(lead.getLeadCode())
                .uniqueLeadId(lead.getUniqueLeadId())
                .clientName(lead.getClientName())
                .mobileNumber(lead.getMobileNumber())
                .speciality(lead.getSpeciality())
                .location(lead.getLocation())
                .clinicAddress(lead.getClinicAddress())
                .isPhysicalLead(Boolean.TRUE.equals(lead.getIsPhysicalLead()))
                .assignedUserId(salesPerson != null ? salesPerson.getId() : null)
                .assignedEmployeeCode(salesPerson != null ? salesPerson.getEmployeeCode() : null)
                .assignedEmployeeName(salesPerson != null ? salesPerson.getFullName() : null)
                .assignedByEmployeeCode(coordinator != null ? coordinator.getEmployeeCode() : null)
                .assignedByEmployeeName(coordinator != null ? coordinator.getFullName() : null)
                .assignedAt(lead.getAssignedAt())
                .assignmentSource(lead.getAssignmentSource())
                .assignedByCoordinator(Boolean.TRUE.equals(lead.getIsPhysicalLead()) || "SALES_COORDINATOR".equalsIgnoreCase(lead.getAssignmentSource()) || coordinator != null)
                .assignmentLabel("Assigned by Sales Coordinator")
                .statusMessage(message)
                .build();
    }
}
