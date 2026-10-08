package com.blueant_crm_erp.lead.validator;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.exception.auth.AccessDeniedException;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.lead.dto.request.*;
import com.blueant_crm_erp.lead.service.LeadActivityTrackerService;
import com.blueant_crm_erp.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LeadValidator {

    private final LeadActivityTrackerService activityTrackerService;

    public void validateCreateLead(CreateLeadRequest request) {
        if (request.getClientName() == null || request.getClientName().trim().isEmpty()) {
            throw new BadRequestException("Client name is required.");
        }
    }

    public void validateUpdateLead(UpdateLeadRequest request) {
        if (request.getNextPlanDate() != null && request.getNextPlanDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Next Plan Date cannot be in the past.");
        }
    }

    public void validateUpdateLeadStatus(UpdateLeadStatusRequest request) {
        if (request.getLeadStatus() == null) {
            throw new BadRequestException("Lead status is required.");
        }
    }

    public void validateAssignLead(AssignLeadRequest request) {
        if (request.getLeadId() == null) {
            throw new BadRequestException("Lead must be selected for assignment.");
        }
    }

    public void validateTransferLead(TransferLeadRequest request) {
        if (request.getNewAssignedUserId() == null) {
            throw new BadRequestException("Transfer user ID is required.");
        }
    }

    public void validateConvertLead(ConvertLeadRequest request) {
        if (request.getPanNumber() == null || request.getPanNumber().trim().isEmpty()) {
            throw new BadRequestException("PAN is mandatory for conversion.");
        }
        if (request.getInvestmentAmount() == null || request.getInvestmentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Valid investment amount is required for conversion.");
        }
    }

    public void validateChangePriority(ChangeLeadPriorityRequest request) {
        if (request.getLeadPriority() == null) {
            throw new BadRequestException("Priority is required.");
        }
    }

    public void validateDuplicateLeadRule(com.blueant_crm_erp.lead.entity.Lead lead, boolean isTransferApproved) {
        if (isTransferApproved) {
            return; // Transfer is allowed
        }
        
        java.util.Optional<LocalDate> lastActivityOpt = activityTrackerService.getLastActivityDate(lead.getUniqueLeadId());
        
        if (lastActivityOpt.isPresent()) {
            long daysInactive = java.time.temporal.ChronoUnit.DAYS.between(lastActivityOpt.get(), LocalDate.now());
            if (daysInactive <= 40) {
                throw new BadRequestException("Cannot transfer: Previous owner has worked on this lead within the last 40 days and has not approved transfer.");
            }
        } else {
            throw new IllegalStateException("Lead activity information is unavailable. Duplicate transfer cannot be evaluated.");
        }
    }

    public void validateLeadStatusTransition() {
    }

    public void validateNextPlanDate() {
    }

    public void validateAssignmentRule() {
    }

    public void validateCrmTargetRoleAssignment(User assignee) {
        if (!isAllowedTargetRoleForCrm(assignee)) {
            throw new AccessDeniedException(
                    "CRM users are only authorized to assign leads to RELATIONSHIP_MANAGER or SALES_COORDINATOR."
            );
        }
    }

    public boolean isAllowedTargetRoleForCrm(User assignee) {
        if (assignee == null) {
            return false;
        }

        String roleCode = assignee.getRole() != null ? assignee.getRole().getCode() : null;
        String desigCode = assignee.getDesignation() != null ? assignee.getDesignation().getCode() : null;

        // Explicitly forbidden target roles for CRM assignment
        if (roleCode != null) {
            String upperRole = roleCode.trim().toUpperCase();
            if (upperRole.equals(BootstrapConstants.ROLE_PC_COORDINATOR) ||
                upperRole.equals("ROLE_" + BootstrapConstants.ROLE_PC_COORDINATOR) ||
                upperRole.equals(BootstrapConstants.ROLE_SALES_MANAGER) ||
                upperRole.equals("ROLE_" + BootstrapConstants.ROLE_SALES_MANAGER) ||
                upperRole.equals(BootstrapConstants.ROLE_ADMIN) ||
                upperRole.equals("ROLE_" + BootstrapConstants.ROLE_ADMIN) ||
                upperRole.equals(BootstrapConstants.ROLE_SUPER_ADMIN) ||
                upperRole.equals("ROLE_" + BootstrapConstants.ROLE_SUPER_ADMIN)) {
                return false;
            }
        }
        if (desigCode != null) {
            String upperDesig = desigCode.trim().toUpperCase();
            if (upperDesig.equals(BootstrapConstants.DESIG_PC) ||
                upperDesig.equals(BootstrapConstants.DESIG_SM)) {
                return false;
            }
        }

        // Allowed: RELATIONSHIP_MANAGER (role or designation)
        boolean isRm = (roleCode != null && (roleCode.equalsIgnoreCase(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER) ||
                                             roleCode.equalsIgnoreCase("ROLE_" + BootstrapConstants.ROLE_RELATIONSHIP_MANAGER)))
                || (desigCode != null && (desigCode.equalsIgnoreCase(BootstrapConstants.DESIG_RM) ||
                                          desigCode.equalsIgnoreCase("RM")));

        // Allowed: SALES_COORDINATOR (role or designation)
        boolean isSc = (roleCode != null && (roleCode.equalsIgnoreCase(BootstrapConstants.ROLE_SALES_COORDINATOR) ||
                                             roleCode.equalsIgnoreCase("ROLE_" + BootstrapConstants.ROLE_SALES_COORDINATOR)))
                || (desigCode != null && (desigCode.equalsIgnoreCase(BootstrapConstants.DESIG_SC) ||
                                          desigCode.equalsIgnoreCase("SC")));

        return isRm || isSc;
    }

    public void validateConversionRule() {
    }
}