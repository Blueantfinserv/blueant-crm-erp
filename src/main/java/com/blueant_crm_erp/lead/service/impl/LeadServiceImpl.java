package com.blueant_crm_erp.lead.service.impl;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.exception.lead.LeadNotFoundException;
import com.blueant_crm_erp.lead.dto.request.*;
import com.blueant_crm_erp.lead.dto.response.*;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.enums.*;
import com.blueant_crm_erp.lead.mapper.LeadMapper;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.lead.service.LeadService;
import com.blueant_crm_erp.lead.service.DuplicateTransferApprovalService;
import com.blueant_crm_erp.lead.specification.LeadSpecification;
import com.blueant_crm_erp.lead.validator.LeadValidator;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.lead.constants.LeadConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.blueant_crm_erp.auth.security.CustomUserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final LeadMapper leadMapper;
    private final LeadValidator leadValidator;
    private final DuplicateTransferApprovalService transferApprovalService;
    private final com.blueant_crm_erp.meeting.repository.MeetingRepository meetingRepository;
    private final com.blueant_crm_erp.meeting.mapper.MeetingMapper meetingMapper;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final com.blueant_crm_erp.lead.service.LeadCodeGeneratorService leadCodeGeneratorService;

    @Override
    public LeadResponse createLead(CreateLeadRequest request, String currentUserEmail) {
        log.info("Creating new lead for client: {}", request.getClientName());
        leadValidator.validateCreateLead(request);

        User currentUser = userRepository.findByEmployeeCodeIgnoreCaseAndDeletedFalse(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with employee code: " + currentUserEmail));

        if (leadRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new BadRequestException(LeadConstants.LEAD_DUPLICATE_MOBILE);
        }

        Lead lead = Lead.builder()
                // --- Auto-generated system fields ---
                .leadCode(leadCodeGeneratorService.generateNextLeadCode())
                .uniqueLeadId(UUID.randomUUID().toString())
                // --- Client information from request ---
                .clientName(request.getClientName())
                .mobileNumber(request.getMobileNumber())
                .alternateNumber(request.getAlternateMobileNumber())
                .email(request.getEmail())
                .location(request.getLocation())
                .leadSource(request.getLeadSource())
                .remarks(request.getRemarks())
                // --- System-managed defaults ---
                .leadType(LeadType.MUTUAL_FUND)
                .leadStatus(LeadStatus.NEW)
                .leadStage(LeadStage.LEAD_CREATED)
                .priority(LeadPriority.MEDIUM)
                .duplicateLeadStatus(DuplicateLeadStatus.ORIGINAL)
                .assignedSalesPerson(currentUser)
                .build();
        // NOTE: meetingDate, meetingTime, meetingMode, meetingWith, profession
        // are intentionally NOT set here. A meeting has not occurred at Lead
        // creation time. These fields belong to the future Meeting module.

        lead = leadRepository.save(lead);
        log.info("Lead created successfully with code: {}", lead.getLeadCode());
        
        // Trigger Domain Event for decoupled downstream actions (like initial Meeting creation)
        eventPublisher.publishEvent(new com.blueant_crm_erp.lead.event.LeadCreatedEvent(this, lead));

        return leadMapper.toResponse(lead);
    }

    @Override
    public LeadResponse updateLead(String uniqueLeadId, UpdateLeadRequest request, String currentUserEmail) {
        log.info("Updating lead: {}", uniqueLeadId);
        leadValidator.validateUpdateLead(request);

        Lead lead = getLeadByUniqueLeadId(uniqueLeadId);
        
        lead.setClientName(request.getClientName());
        lead.setAlternateNumber(request.getAlternateMobileNumber());
        lead.setEmail(request.getEmail());
        lead.setLocation(request.getLocation());
        lead.setCompanyName(request.getCompanyName());
        lead.setRemarks(request.getRemarks());
        
        lead.setNextPlanDate(request.getNextPlanDate() != null ? request.getNextPlanDate().atStartOfDay() : null);
        if(request.getLastCallDate() != null) {
            lead.setLastCallDate(request.getLastCallDate().atStartOfDay());
        }
        
        lead.setMeetingWith(request.getJoinedMeetingWith());

        lead = leadRepository.save(lead);
        return leadMapper.toResponse(lead);
    }

    @Override
    public LeadResponse assignLead(AssignLeadRequest request, String currentUserEmail) {
        log.info("Assigning lead: {} to user: {}", request.getLeadId(), request.getAssignedUserId());
        leadValidator.validateAssignLead(request);

        User assignee = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAssignedUserId()));

        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new LeadNotFoundException(request.getLeadId().toString()));
        lead.setAssignedSalesPerson(assignee);
        lead.setLeadStatus(LeadStatus.ASSIGNED);
        lead.setLeadStage(LeadStage.LEAD_ASSIGNED);
        lead = leadRepository.save(lead);

        return leadMapper.toResponse(lead);
    }

    @Override
    public LeadResponse transferLead(TransferLeadRequest request, String currentUserEmail) {
        log.info("Transferring lead: {} to user: {}", request.getLeadId(), request.getNewAssignedUserId());
        leadValidator.validateTransferLead(request);

        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new LeadNotFoundException(request.getLeadId().toString()));
                
        // Check decoupled approval status
        boolean isApproved = transferApprovalService.isTransferApproved(lead.getUniqueLeadId());
        
        // Enforce 40-day/approval duplicate rule using decoupled logic
        leadValidator.validateDuplicateLeadRule(lead, isApproved);

        User assignee = userRepository.findById(request.getNewAssignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getNewAssignedUserId()));

        lead.setAssignedSalesPerson(assignee);
        lead.setLeadStatus(LeadStatus.TRANSFERRED);
        lead.setDuplicateLeadStatus(DuplicateLeadStatus.TRANSFERRED);
        
        lead = leadRepository.save(lead);
        return leadMapper.toResponse(lead);
    }

    @Override
    public LeadResponse changeStatus(UpdateLeadStatusRequest request, String currentUserEmail) {
        log.info("Changing status for lead: {}", request.getLeadId());
        leadValidator.validateUpdateLeadStatus(request);
        leadValidator.validateLeadStatusTransition();

        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new LeadNotFoundException(request.getLeadId().toString()));
        
        lead.setLeadStatus(request.getLeadStatus());
        if (request.getLeadStage() != null) {
            lead.setLeadStage(request.getLeadStage());
        }
        if(request.getRemarks() != null) {
            lead.setRemarks(request.getRemarks());
        }
        lead = leadRepository.save(lead);
        return leadMapper.toResponse(lead);
    }

    @Override
    public LeadResponse changePriority(ChangeLeadPriorityRequest request, String currentUserEmail) {
        log.info("Changing priority for lead: {}", request.getLeadId());
        leadValidator.validateChangePriority(request);

        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new LeadNotFoundException(request.getLeadId().toString()));
        
        lead.setPriority(request.getLeadPriority());
        lead = leadRepository.save(lead);
        return leadMapper.toResponse(lead);
    }

    @Override
    public LeadResponse convertLead(ConvertLeadRequest request, String currentUserEmail) {
        log.info("Converting lead: {}", request.getLeadId());
        leadValidator.validateConvertLead(request);
        leadValidator.validateConversionRule(); // PAN, amount etc.

        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new LeadNotFoundException(request.getLeadId().toString()));
        
        lead.setLeadStatus(LeadStatus.CONVERTED);
        lead.setLeadStage(LeadStage.COMPLETED);


        lead = leadRepository.save(lead);
        return leadMapper.toResponse(lead);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadDetailResponse getLeadDetails(String uniqueLeadId) {
        Lead lead = getLeadByUniqueLeadId(uniqueLeadId);
        LeadDetailResponse response = leadMapper.toDetailResponse(lead);
        
        java.util.Set<String> employeeCodes = new java.util.HashSet<>();
        if (lead.getCreatedBy() != null) {
            employeeCodes.add(lead.getCreatedBy());
        }
        if (lead.getUpdatedBy() != null) {
            employeeCodes.add(lead.getUpdatedBy());
        }
        
        com.blueant_crm_erp.common.dto.audit.AuditInfoDto auditInfo = com.blueant_crm_erp.common.dto.audit.AuditInfoDto.builder()
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
        
        if (!employeeCodes.isEmpty()) {
            java.util.Map<String, User> userMap = userRepository.findAllByEmployeeCodeInIgnoreCaseAndDeletedFalse(employeeCodes)
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(
                            u -> u.getEmployeeCode().toLowerCase(), 
                            u -> u,
                            (existing, replacement) -> existing
                    ));
                    
            if (lead.getCreatedBy() != null && userMap.containsKey(lead.getCreatedBy().toLowerCase())) {
                User createdUser = userMap.get(lead.getCreatedBy().toLowerCase());
                auditInfo.setCreatedBy(com.blueant_crm_erp.common.dto.reference.ReferenceUserDto.builder()
                        .id(createdUser.getId())
                        .code(createdUser.getEmployeeCode())
                        .name(createdUser.getFullName())
                        .email(createdUser.getEmail())
                        .mobileNumber(createdUser.getMobileNumber())
                        .profileImage(createdUser.getProfileImage())
                        .build());
            }
            if (lead.getUpdatedBy() != null && userMap.containsKey(lead.getUpdatedBy().toLowerCase())) {
                User updatedUser = userMap.get(lead.getUpdatedBy().toLowerCase());
                auditInfo.setUpdatedBy(com.blueant_crm_erp.common.dto.reference.ReferenceUserDto.builder()
                        .id(updatedUser.getId())
                        .code(updatedUser.getEmployeeCode())
                        .name(updatedUser.getFullName())
                        .email(updatedUser.getEmail())
                        .mobileNumber(updatedUser.getMobileNumber())
                        .profileImage(updatedUser.getProfileImage())
                        .build());
            }
        }
        
        response.setAudit(auditInfo);

        // Populate Current Active Meeting
        meetingRepository.findTopByLeadIdAndMeetingStatusOrderByMeetingNumberDesc(lead.getId(), com.blueant_crm_erp.meeting.enums.MeetingStatus.SCHEDULED)
                .ifPresent(meeting -> response.setCurrentActiveMeeting(meetingMapper.toSummaryResponse(meeting)));

        // Populate Meeting History
        java.util.List<com.blueant_crm_erp.meeting.entity.Meeting> history = meetingRepository.findByLeadIdAndMeetingStatusOrderByMeetingNumberAsc(lead.getId(), com.blueant_crm_erp.meeting.enums.MeetingStatus.COMPLETED);
        response.setMeetingHistory(meetingMapper.toSummaryResponseList(history));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> searchLeads(LeadSearchRequest request, Pageable pageable) {
        LeadFilterRequest effectiveFilter = extractEffectiveFilter(request);
        enforceSalesPersonOwnership(effectiveFilter);

        org.springframework.data.jpa.domain.Specification<Lead> spec =
                LeadSpecification.searchAndFilter(request != null ? request.getKeyword() : null, effectiveFilter);
        Pageable effectivePageable = resolvePageable(request, pageable);

        Page<Lead> page = leadRepository.findAll(spec, effectivePageable);
        return PageResponse.of(page.map(leadMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> filterLeads(LeadFilterRequest request, Pageable pageable) {
        enforceSalesPersonOwnership(request);
        Page<Lead> page = leadRepository.findAll(LeadSpecification.filterByCriteria(request), pageable);
        return PageResponse.of(page.map(leadMapper::toResponse));
    }

    private LeadFilterRequest extractEffectiveFilter(LeadSearchRequest request) {
        if (request == null) {
            return null;
        }
        LeadFilterRequest filter = request.getFilter();
        if (filter == null) {
            filter = request;
        } else {
            if (filter.getAssignmentSource() == null) filter.setAssignmentSource(request.getAssignmentSource());
            if (filter.getAssignedByCoordinator() == null) filter.setAssignedByCoordinator(request.getAssignedByCoordinator());
            if (filter.getIsPhysicalLead() == null) filter.setIsPhysicalLead(request.getIsPhysicalLead());
            if (filter.getAssignedFromDate() == null) filter.setAssignedFromDate(request.getAssignedFromDate());
            if (filter.getAssignedToDate() == null) filter.setAssignedToDate(request.getAssignedToDate());
            if (filter.getAssignedUserId() == null) filter.setAssignedUserId(request.getAssignedUserId());
            if (filter.getLeadStatus() == null) filter.setLeadStatus(request.getLeadStatus());
            if (filter.getFromDate() == null) filter.setFromDate(request.getFromDate());
            if (filter.getToDate() == null) filter.setToDate(request.getToDate());
            if (filter.getClientName() == null) filter.setClientName(request.getClientName());
            if (filter.getMobileNumber() == null) filter.setMobileNumber(request.getMobileNumber());
            if (filter.getLeadCode() == null) filter.setLeadCode(request.getLeadCode());
            if (filter.getLeadStage() == null) filter.setLeadStage(request.getLeadStage());
            if (filter.getLeadPriority() == null) filter.setLeadPriority(request.getLeadPriority());
            if (filter.getLeadSource() == null) filter.setLeadSource(request.getLeadSource());
            if (filter.getDuplicateLeadStatus() == null) filter.setDuplicateLeadStatus(request.getDuplicateLeadStatus());
            if (filter.getLeaderId() == null) filter.setLeaderId(request.getLeaderId());
        }
        return filter;
    }

    private void enforceSalesPersonOwnership(LeadFilterRequest filter) {
        if (filter == null) {
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return;
        }

        boolean hasElevated = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") ||
                               a.getAuthority().equalsIgnoreCase("ADMIN") ||
                               a.getAuthority().equalsIgnoreCase("ROLE_SUPER_ADMIN") ||
                               a.getAuthority().equalsIgnoreCase("SUPER_ADMIN") ||
                               a.getAuthority().equalsIgnoreCase("ROLE_SALES_COORDINATOR") ||
                               a.getAuthority().equalsIgnoreCase("SALES_COORDINATOR") ||
                               a.getAuthority().equalsIgnoreCase("PHYSICAL_LEAD_ASSIGN") ||
                               a.getAuthority().equalsIgnoreCase("ROLE_BUSINESS_HEAD") ||
                               a.getAuthority().equalsIgnoreCase("BUSINESS_HEAD") ||
                               a.getAuthority().equalsIgnoreCase("ROLE_SALES_MANAGER") ||
                               a.getAuthority().equalsIgnoreCase("SALES_MANAGER") ||
                               a.getAuthority().equalsIgnoreCase("ROLE_TEAM_LEADER") ||
                               a.getAuthority().equalsIgnoreCase("TEAM_LEADER"));

        if (auth.getPrincipal() instanceof CustomUserDetails cud) {
            if (cud.getRoleCode() != null) {
                String roleCode = cud.getRoleCode().toUpperCase();
                if (roleCode.equals("ADMIN") || roleCode.equals("SUPER_ADMIN") ||
                    roleCode.equals("SALES_COORDINATOR") || roleCode.equals("BUSINESS_HEAD") ||
                    roleCode.equals("SALES_MANAGER") || roleCode.equals("TEAM_LEADER")) {
                    hasElevated = true;
                }
            }
        }

        if (hasElevated) {
            return;
        }

        Long currentUserId = null;
        if (auth.getPrincipal() instanceof CustomUserDetails cud) {
            currentUserId = cud.getUserId();
        } else if (auth.getPrincipal() instanceof User u) {
            currentUserId = u.getId();
        } else {
            String identifier = auth.getName();
            if (StringUtils.hasText(identifier)) {
                User u = userRepository.findByEmployeeCodeIgnoreCaseOrEmailIgnoreCaseOrMobileNumberAndDeletedFalse(
                        identifier, identifier, identifier).orElse(null);
                if (u != null) {
                    currentUserId = u.getId();
                }
            }
        }

        if (currentUserId != null) {
            if (filter.getAssignedUserId() != null && !filter.getAssignedUserId().equals(currentUserId)) {
                log.warn("Access denied: Authenticated Sales Person [{}] attempted to access leads assigned to user [{}]",
                        currentUserId, filter.getAssignedUserId());
                throw new AccessDeniedException("Access denied: You cannot view leads assigned to another user.");
            }
            filter.setAssignedUserId(currentUserId);
        } else {
            throw new AccessDeniedException("Access denied: Authenticated user could not be resolved.");
        }
    }

    private Pageable resolvePageable(LeadSearchRequest request, Pageable pageable) {
        if (pageable != null && pageable.isPaged()) {
            if (pageable.getSort().isSorted() || pageable.getPageSize() != 20) {
                return pageable;
            }
        }
        if (request != null && request.getPage() != null && request.getSize() != null && request.getSize() > 0) {
            String sortBy = StringUtils.hasText(request.getSortBy()) ? request.getSortBy() : "createdAt";
            org.springframework.data.domain.Sort.Direction direction =
                    "ASC".equalsIgnoreCase(request.getSortDirection()) ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
            return org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), org.springframework.data.domain.Sort.by(direction, sortBy));
        }
        return pageable != null ? pageable : org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public void markDuplicate(String uniqueLeadId, String currentUserEmail) {
        Lead lead = getLeadByUniqueLeadId(uniqueLeadId);
        lead.setDuplicateLeadStatus(DuplicateLeadStatus.DUPLICATE);
        leadRepository.save(lead);
    }

    @Override
    public void verifyDuplicate(String uniqueLeadId, String currentUserEmail) {
        Lead lead = getLeadByUniqueLeadId(uniqueLeadId);
        lead.setDuplicateLeadStatus(DuplicateLeadStatus.UNDER_VERIFICATION);
        leadRepository.save(lead);
    }

    @Override
    public void deleteLead(String uniqueLeadId, String currentUserEmail) {
        Lead lead = getLeadByUniqueLeadId(uniqueLeadId);
        lead.markAsDeleted(currentUserEmail);
        leadRepository.save(lead);
    }

    @Override
    public void restoreLead(String uniqueLeadId, String currentUserEmail) {
        Lead lead = getLeadByUniqueLeadId(uniqueLeadId);
        lead.restore();
        leadRepository.save(lead);
    }

    @Override
    public void deactivateLead(String uniqueLeadId, String currentUserEmail) {
        Lead lead = getLeadByUniqueLeadId(uniqueLeadId);
        lead.setLeadStatus(LeadStatus.LOST);
        leadRepository.save(lead);
    }

    private Lead getLeadByUniqueLeadId(String uniqueLeadId) {
        return leadRepository.findByUniqueLeadId(uniqueLeadId)
                .orElseThrow(() -> new LeadNotFoundException(uniqueLeadId));
    }
}
