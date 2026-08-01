package com.blueant_crm_erp.user.service.impl;

import com.blueant_crm_erp.common.dto.response.PageResponse;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.exception.common.ResourceNotFoundException;
import com.blueant_crm_erp.user.dto.request.ChangeDesignationStatusRequest;
import com.blueant_crm_erp.user.dto.request.CreateDesignationRequest;
import com.blueant_crm_erp.user.dto.request.DesignationSearchRequest;
import com.blueant_crm_erp.user.dto.request.UpdateDesignationRequest;
import com.blueant_crm_erp.user.dto.response.DesignationDropdownResponse;
import com.blueant_crm_erp.user.dto.response.DesignationResponse;
import com.blueant_crm_erp.user.dto.response.DesignationSummaryResponse;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.mapper.DesignationMapper;
import com.blueant_crm_erp.user.repository.DesignationRepository;
import com.blueant_crm_erp.user.service.DesignationService;
import com.blueant_crm_erp.user.specification.DesignationSpecification;
import com.blueant_crm_erp.user.validator.DesignationValidator;
import com.blueant_crm_erp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * =============================================================================
 * Designation Service Implementation
 * =============================================================================
 *
 * Enterprise implementation of {@link DesignationService}.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Create / Update / Soft-Delete / Restore Designation
 * • Change Designation Status
 * • Query Designations (by id, name, paged search, dropdown, listing)
 * • Existence Checks and Count Queries
 *
 * Design Principles
 * -----------------------------------------------------------------------------
 * • All uniqueness and delete-safety validation delegated to {@link DesignationValidator}.
 * • Designations carry a {@code hierarchyLevel} used by the User module to
 *   enforce the reporting manager hierarchy chain.
 * • Transactional boundaries are declared per method:
 *     - readOnly = true on all read-only operations
 *     - default (readOnly = false) on all write operations
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : User Management
 *
 * @author BlueAnt CRM ERP Team
 * @since  1.0.0
 * =============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DesignationValidator  designationValidator;
    private final DesignationMapper     designationMapper;

    // =========================================================================
    // Create
    // =========================================================================

    @Override
    @Transactional
    public DesignationResponse createDesignation(CreateDesignationRequest request) {
        log.info("Creating designation with code: {}", request.getCode());
        designationValidator.validateCode(request.getCode());
        designationValidator.validateName(request.getName());

        Designation designation = designationMapper.toEntity(request);
        designation.setStatus(Status.ACTIVE);

        designation = designationRepository.save(designation);
        log.info("Successfully created designation with id: {}", designation.getId());
        return designationMapper.toResponse(designation);
    }

    // =========================================================================
    // Update
    // =========================================================================

    @Override
    @Transactional
    public DesignationResponse updateDesignation(Long designationId, UpdateDesignationRequest request) {
        log.info("Updating designation with id: {}", designationId);
        designationValidator.validateUpdate(designationId, request.getCode(), request.getName());

        Designation designation = designationValidator.validateDesignation(designationId);
        designationMapper.updateEntity(request, designation);

        designation = designationRepository.save(designation);
        log.info("Successfully updated designation with id: {}", designationId);
        return designationMapper.toResponse(designation);
    }

    // =========================================================================
    // Delete / Restore
    // =========================================================================

    @Override
    @Transactional
    public void deleteDesignation(Long designationId) {
        log.info("Deleting designation with id: {}", designationId);
        // validateDelete internally checks for assigned users before allowing deletion
        designationValidator.validateDelete(designationId);
        Designation designation = designationValidator.validateDesignation(designationId);
        designation.markAsDeleted(SecurityUtil.getCurrentUsername());
        designationRepository.save(designation);
        log.info("Successfully deleted designation with id: {}", designationId);
    }

    @Override
    @Transactional
    public void restoreDesignation(Long designationId) {
        log.info("Restoring designation with id: {}", designationId);
        Designation designation = designationRepository.findByIdAndDeletedTrue(designationId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found."));
        designation.restore();
        designationRepository.save(designation);
        log.info("Successfully restored designation with id: {}", designationId);
    }

    // =========================================================================
    // Status
    // =========================================================================

    @Override
    @Transactional
    public DesignationResponse changeDesignationStatus(Long designationId, ChangeDesignationStatusRequest request) {
        log.info("Changing status of designation with id: {} to {}", designationId, request.getStatus());
        Designation designation = designationValidator.validateDesignation(designationId);
        designation.setStatus(request.getStatus());
        designation = designationRepository.save(designation);
        log.info("Successfully changed status of designation with id: {}", designationId);
        return designationMapper.toResponse(designation);
    }

    // =========================================================================
    // Query — Single
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse getDesignationById(Long designationId) {
        Designation designation = designationValidator.validateDesignation(designationId);
        return designationMapper.toResponse(designation);
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse getDesignationByName(String designationName) {
        Designation designation = designationRepository.findByNameIgnoreCaseAndDeletedFalse(designationName)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found."));
        return designationMapper.toResponse(designation);
    }

    // =========================================================================
    // Query — Collection
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<DesignationSummaryResponse> getAllDesignations() {
        return designationMapper.toSummaryList(
                designationRepository.findAllByDeletedFalseOrderByHierarchyLevelAscDisplayOrderAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignationSummaryResponse> getAllDesignationsIncludingDeleted() {
        return designationMapper.toSummaryList(
                designationRepository.findAllByOrderByHierarchyLevelAscDisplayOrderAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DesignationSummaryResponse> searchDesignations(DesignationSearchRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Designation> page = designationRepository.findAll(DesignationSpecification.search(request), pageable);
        return PageResponse.of(page.map(designationMapper::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignationDropdownResponse> getDesignationDropdown() {
        return designationMapper.toDropdownList(
                designationRepository.findAllByStatusAndDeletedFalseOrderByNameAsc(Status.ACTIVE));
    }

    // =========================================================================
    // Existence Checks
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long designationId) {
        return designationRepository.existsByIdAndDeletedFalse(designationId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String designationName) {
        return designationRepository.existsByNameIgnoreCaseAndDeletedFalse(designationName);
    }

    // =========================================================================
    // Count
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public long countDesignations() {
        return designationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveDesignations() {
        return designationRepository.countByDeletedFalse();
    }
}
