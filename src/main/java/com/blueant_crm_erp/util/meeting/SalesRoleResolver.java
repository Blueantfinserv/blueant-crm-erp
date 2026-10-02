package com.blueant_crm_erp.util.meeting;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Sales Role Resolver
 * ============================================================================
 *
 * Resolves the functional sales role (SM, RM, SC, OTHER) for a given Meeting
 * or User/Employee based on assigned ownership and role/designation metadata.
 *
 * Ownership Resolution Order:
 * 1. meeting.getAssignedEmployee()
 * 2. If null: meeting.getLead().getAssignedSalesPerson()
 * 3. If still null: OTHER
 *
 * Sales Role Mapping:
 * - SM: role = SALES_MANAGER OR designation = SM
 * - RM: role = RELATIONSHIP_MANAGER OR designation = RM
 * - SC: role = PC_COORDINATOR OR role = SALES_COORDINATOR
 * - Otherwise: OTHER
 *
 * Elevated User Safety:
 * This resolver determines the business-rule owner from the Meeting / Lead assignment,
 * NOT from the currently authenticated SecurityContext user.
 */
@Component
public class SalesRoleResolver {

    /**
     * Instance method for Spring bean injection.
     */
    public SalesRole resolve(Meeting meeting) {
        return resolveRole(meeting);
    }

    /**
     * Instance method for Spring bean injection.
     */
    public SalesRole resolve(User employee) {
        return resolveRole(employee);
    }

    /**
     * Resolves the SalesRole for a given Meeting.
     *
     * @param meeting the meeting to resolve role for
     * @return the resolved SalesRole (SM, RM, SC, or OTHER)
     */
    public static SalesRole resolveRole(Meeting meeting) {
        if (meeting == null) {
            return SalesRole.OTHER;
        }

        User employee = meeting.getAssignedEmployee();
        if (employee == null && meeting.getLead() != null) {
            employee = meeting.getLead().getAssignedSalesPerson();
        }

        return resolveRole(employee);
    }

    /**
     * Resolves the SalesRole for a given User/Employee.
     *
     * @param employee the employee to resolve role for
     * @return the resolved SalesRole (SM, RM, SC, or OTHER)
     */
    public static SalesRole resolveRole(User employee) {
        if (employee == null) {
            return SalesRole.OTHER;
        }

        String roleCode = null;
        if (employee.getRole() != null && employee.getRole().getCode() != null) {
            roleCode = employee.getRole().getCode().trim();
        }

        String desigCode = null;
        if (employee.getDesignation() != null && employee.getDesignation().getCode() != null) {
            desigCode = employee.getDesignation().getCode().trim();
        }

        // SM: role = SALES_MANAGER or designation = SM
        if ((roleCode != null && (roleCode.equalsIgnoreCase(BootstrapConstants.ROLE_SALES_MANAGER) ||
                                  roleCode.equalsIgnoreCase("ROLE_" + BootstrapConstants.ROLE_SALES_MANAGER))) ||
            (desigCode != null && desigCode.equalsIgnoreCase(BootstrapConstants.DESIG_SM))) {
            return SalesRole.SM;
        }

        // RM: role = RELATIONSHIP_MANAGER or designation = RM
        if ((roleCode != null && (roleCode.equalsIgnoreCase(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER) ||
                                  roleCode.equalsIgnoreCase("ROLE_" + BootstrapConstants.ROLE_RELATIONSHIP_MANAGER))) ||
            (desigCode != null && desigCode.equalsIgnoreCase("RM"))) {
            return SalesRole.RM;
        }

        // SC: role = PC_COORDINATOR or role = SALES_COORDINATOR
        if (roleCode != null && (roleCode.equalsIgnoreCase(BootstrapConstants.ROLE_PC_COORDINATOR) ||
                                 roleCode.equalsIgnoreCase("ROLE_" + BootstrapConstants.ROLE_PC_COORDINATOR) ||
                                 roleCode.equalsIgnoreCase(BootstrapConstants.ROLE_SALES_COORDINATOR) ||
                                 roleCode.equalsIgnoreCase("ROLE_" + BootstrapConstants.ROLE_SALES_COORDINATOR))) {
            return SalesRole.SC;
        }

        return SalesRole.OTHER;
    }
}
