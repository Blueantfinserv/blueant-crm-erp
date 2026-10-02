package com.blueant_crm_erp.util.meeting;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesRoleResolverTest {

    private final SalesRoleResolver resolver = new SalesRoleResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User createUser(String roleCode, String designationCode) {
        User.UserBuilder<?, ?> builder = User.builder()
                .employeeCode("EMP" + System.nanoTime())
                .firstName("Test")
                .lastName("User")
                .email("test" + System.nanoTime() + "@blueant.com");

        if (roleCode != null) {
            builder.role(Role.builder().code(roleCode).name(roleCode + " Role").build());
        }

        if (designationCode != null) {
            builder.designation(Designation.builder().code(designationCode).name(designationCode + " Desig").build());
        }

        return builder.build();
    }

    // 1. Meeting assigned to SM through role → SM
    @Test
    @DisplayName("1. Meeting assigned to SM through role -> SM")
    void test1_meetingAssignedToSmThroughRole() {
        User smUser = createUser(BootstrapConstants.ROLE_SALES_MANAGER, null);
        Meeting meeting = Meeting.builder().assignedEmployee(smUser).build();

        assertEquals(SalesRole.SM, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.SM, resolver.resolve(meeting));
    }

    // 2. Meeting assigned to SM through designation "SM" → SM
    @Test
    @DisplayName("2. Meeting assigned to SM through designation SM -> SM")
    void test2_meetingAssignedToSmThroughDesignation() {
        User smUser = createUser("EMPLOYEE", BootstrapConstants.DESIG_SM);
        Meeting meeting = Meeting.builder().assignedEmployee(smUser).build();

        assertEquals(SalesRole.SM, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.SM, resolver.resolve(meeting));
    }

    // 3. Meeting assigned to RM through role → RM
    @Test
    @DisplayName("3. Meeting assigned to RM through role -> RM")
    void test3_meetingAssignedToRmThroughRole() {
        User rmUser = createUser(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, null);
        Meeting meeting = Meeting.builder().assignedEmployee(rmUser).build();

        assertEquals(SalesRole.RM, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.RM, resolver.resolve(meeting));
    }

    // 4. Meeting assigned to RM through designation "RM" → RM
    @Test
    @DisplayName("4. Meeting assigned to RM through designation RM -> RM")
    void test4_meetingAssignedToRmThroughDesignation() {
        User rmUser = createUser("EMPLOYEE", "RM");
        Meeting meeting = Meeting.builder().assignedEmployee(rmUser).build();

        assertEquals(SalesRole.RM, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.RM, resolver.resolve(meeting));
    }

    // 5. Meeting assigned to SC through PC_COORDINATOR → SC
    @Test
    @DisplayName("5. Meeting assigned to SC through PC_COORDINATOR -> SC")
    void test5_meetingAssignedToScThroughPcCoordinator() {
        User scUser = createUser(BootstrapConstants.ROLE_PC_COORDINATOR, null);
        Meeting meeting = Meeting.builder().assignedEmployee(scUser).build();

        assertEquals(SalesRole.SC, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.SC, resolver.resolve(meeting));
    }

    // 6. Meeting assigned to SC through SALES_COORDINATOR → SC
    @Test
    @DisplayName("6. Meeting assigned to SC through SALES_COORDINATOR -> SC")
    void test6_meetingAssignedToScThroughSalesCoordinator() {
        User scUser = createUser(BootstrapConstants.ROLE_SALES_COORDINATOR, null);
        Meeting meeting = Meeting.builder().assignedEmployee(scUser).build();

        assertEquals(SalesRole.SC, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.SC, resolver.resolve(meeting));
    }

    // 7. Meeting assigned employee is null but Lead assignedSalesPerson is SM → SM
    @Test
    @DisplayName("7. Meeting assigned employee is null but Lead assignedSalesPerson is SM -> SM")
    void test7_meetingAssignedNull_leadOwnerIsSm() {
        User smUser = createUser(BootstrapConstants.ROLE_SALES_MANAGER, null);
        Lead lead = Lead.builder().assignedSalesPerson(smUser).build();
        Meeting meeting = Meeting.builder().lead(lead).assignedEmployee(null).build();

        assertEquals(SalesRole.SM, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.SM, resolver.resolve(meeting));
    }

    // 8. Meeting assigned employee is null but Lead assignedSalesPerson is RM → RM
    @Test
    @DisplayName("8. Meeting assigned employee is null but Lead assignedSalesPerson is RM -> RM")
    void test8_meetingAssignedNull_leadOwnerIsRm() {
        User rmUser = createUser(null, "RM");
        Lead lead = Lead.builder().assignedSalesPerson(rmUser).build();
        Meeting meeting = Meeting.builder().lead(lead).assignedEmployee(null).build();

        assertEquals(SalesRole.RM, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.RM, resolver.resolve(meeting));
    }

    // 9. Both assigned employee and lead owner are null → OTHER
    @Test
    @DisplayName("9. Both assigned employee and lead owner are null -> OTHER")
    void test9_bothAssignedEmployeeAndLeadOwnerNull() {
        Lead lead = Lead.builder().assignedSalesPerson(null).build();
        Meeting meeting = Meeting.builder().lead(lead).assignedEmployee(null).build();

        assertEquals(SalesRole.OTHER, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.OTHER, resolver.resolve(meeting));
    }

    // 10. Role/designation are null → OTHER
    @Test
    @DisplayName("10. Role and designation are null on assigned employee -> OTHER")
    void test10_roleAndDesignationNull() {
        User user = createUser(null, null);
        Meeting meeting = Meeting.builder().assignedEmployee(user).build();

        assertEquals(SalesRole.OTHER, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.OTHER, resolver.resolve(meeting));
    }

    // 11. Logged-in user is PC_COORDINATOR but meeting owner is SM → SM
    @Test
    @DisplayName("11. Logged-in user is PC_COORDINATOR but meeting owner is SM -> SM")
    void test11_loggedInUserIsPcCoordinator_meetingOwnerIsSm() {
        // Authenticate as PC_COORDINATOR in SecurityContext
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "coordinator@blueant.com", "pass", "ROLE_PC_COORDINATOR", "MEETING_VERIFY");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Meeting is owned by SM
        User smUser = createUser(BootstrapConstants.ROLE_SALES_MANAGER, null);
        Meeting meeting = Meeting.builder().assignedEmployee(smUser).build();

        // Must resolve to SM, completely independent of the logged-in user
        assertEquals(SalesRole.SM, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.SM, resolver.resolve(meeting));
    }

    // 12. Logged-in user is ADMIN but meeting owner is RM → RM
    @Test
    @DisplayName("12. Logged-in user is ADMIN but meeting owner is RM -> RM")
    void test12_loggedInUserIsAdmin_meetingOwnerIsRm() {
        // Authenticate as ADMIN in SecurityContext
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "admin@blueant.com", "pass", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Meeting is owned by RM
        User rmUser = createUser(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, null);
        Meeting meeting = Meeting.builder().assignedEmployee(rmUser).build();

        // Must resolve to RM, completely independent of the logged-in user
        assertEquals(SalesRole.RM, SalesRoleResolver.resolveRole(meeting));
        assertEquals(SalesRole.RM, resolver.resolve(meeting));
    }

    // Null Safety Tests
    @Test
    @DisplayName("Null safety: meeting is null -> OTHER")
    void testNullMeeting() {
        assertEquals(SalesRole.OTHER, SalesRoleResolver.resolveRole((Meeting) null));
        assertEquals(SalesRole.OTHER, resolver.resolve((Meeting) null));
    }

    @Test
    @DisplayName("Null safety: user is null -> OTHER")
    void testNullUser() {
        assertEquals(SalesRole.OTHER, SalesRoleResolver.resolveRole((User) null));
        assertEquals(SalesRole.OTHER, resolver.resolve((User) null));
    }

    @Test
    @DisplayName("Null safety: meeting with null lead and null employee -> OTHER")
    void testMeetingNullLeadNullEmployee() {
        Meeting meeting = Meeting.builder().lead(null).assignedEmployee(null).build();
        assertEquals(SalesRole.OTHER, SalesRoleResolver.resolveRole(meeting));
    }

    @Test
    @DisplayName("Fallback to OTHER for unrelated designations and roles")
    void testUnrelatedRoleAndDesignation() {
        User hrUser = createUser("HR_MANAGER", "HRM");
        Meeting meeting = Meeting.builder().assignedEmployee(hrUser).build();
        assertEquals(SalesRole.OTHER, SalesRoleResolver.resolveRole(meeting));
    }
}
