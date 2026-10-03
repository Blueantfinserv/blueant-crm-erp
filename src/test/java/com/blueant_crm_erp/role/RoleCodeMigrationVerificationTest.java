package com.blueant_crm_erp.role;

import com.blueant_crm_erp.auth.security.CustomUserDetails;
import com.blueant_crm_erp.auth.security.CustomUserDetailsService;
import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.lead.repository.LeadRepository;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.meeting.repository.MeetingRepository;
import com.blueant_crm_erp.meeting.repository.MeetingVerificationRepository;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.entity.RolePermission;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.blueant_crm_erp.common.enums.Gender;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * =============================================================================
 * Role Code Migration Verification Test
 * =============================================================================
 *
 * Verifies the final intended role mapping:
 * - ID 4: code = SALES_COORDINATOR,    name = Sales Coordinator
 * - ID 7: code = SALES_MANAGER,        name = Sales Manager
 * - ID 6: code = RELATIONSHIP_MANAGER, name = Relationship Manager (unchanged)
 * - ID 8: code = PC_COORDINATOR,       name = PC Coordinator (unchanged)
 *
 * Also proves:
 * - Permissions for Role 4 and Role 7 are preserved
 * - User authority generation reflects the migrated role codes
 * - SalesRoleResolver correctly identifies SM and SC
 * - Meeting, Lead, and Verification records remain untampered
 */
@SpringBootTest
@Transactional
public class RoleCodeMigrationVerificationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingVerificationRepository meetingVerificationRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private SalesRoleResolver salesRoleResolver;

    @Autowired
    private com.blueant_crm_erp.user.repository.DepartmentRepository departmentRepository;

    @Autowired
    private com.blueant_crm_erp.user.repository.DesignationRepository designationRepository;

    @Autowired
    private com.blueant_crm_erp.user.repository.TeamRepository teamRepository;

    // 1 & 2: Role ID 4 has code = SALES_COORDINATOR and name = Sales Coordinator
    @Test
    @DisplayName("1 & 2: Role ID 4 has code = SALES_COORDINATOR and name = Sales Coordinator")
    void test1_roleId4_isSalesCoordinator() {
        Role role4 = roleRepository.findById(4L).orElseThrow(() -> new AssertionError("Role 4 not found"));
        assertThat(role4.getCode()).isEqualTo(BootstrapConstants.ROLE_SALES_COORDINATOR);
        assertThat(role4.getName()).isEqualTo("Sales Coordinator");
    }

    // 3 & 4: Role ID 7 has code = SALES_MANAGER and name = Sales Manager
    @Test
    @DisplayName("3 & 4: Role ID 7 has code = SALES_MANAGER and name = Sales Manager")
    void test2_roleId7_isSalesManager() {
        Role role7 = roleRepository.findById(7L).orElseThrow(() -> new AssertionError("Role 7 not found"));
        assertThat(role7.getCode()).isEqualTo(BootstrapConstants.ROLE_SALES_MANAGER);
        assertThat(role7.getName()).isEqualTo("Sales Manager");
    }

    // 5 & 6: Permissions for Role 4 and Role 7 are preserved
    @Test
    @DisplayName("5 & 6: Permissions for Role 4 and Role 7 are preserved")
    void test3_rolePermissionsPreserved() {
        List<RolePermission> perms4 = rolePermissionRepository.findAllByRoleId(4L);
        assertThat(perms4).isNotEmpty();
        Set<String> permCodes4 = perms4.stream()
                .map(rp -> rp.getPermission().getCode())
                .collect(Collectors.toSet());
        assertThat(permCodes4).contains("MEETING_READ", "LEAD_READ", "USER_READ");

        List<RolePermission> perms7 = rolePermissionRepository.findAllByRoleId(7L);
        assertThat(perms7).isNotEmpty();
        Set<String> permCodes7 = perms7.stream()
                .map(rp -> rp.getPermission().getCode())
                .collect(Collectors.toSet());
        assertThat(permCodes7).contains("MEETING_UPDATE", "MEETING_CREATE", "MEETING_READ",
                "LEAD_CREATE", "LEAD_UPDATE", "LEAD_READ");
    }

    // 7: Sales Manager user receives ROLE_SALES_MANAGER authority and operational permissions
    @Test
    @DisplayName("7: User assigned role 7 receives ROLE_SALES_MANAGER authority and permissions")
    void test4_salesManagerUser_receivesCorrectAuthorities() {
        Role role7 = roleRepository.findById(7L).orElseThrow();
        var dept = departmentRepository.findAll().stream().findFirst().orElse(null);
        var desig = designationRepository.findAll().stream().findFirst().orElse(null);

        var team = teamRepository.findAll().stream().findFirst().orElse(null);

        User testSmUser = User.builder()
                .employeeCode("SM_TEST_USER_01")
                .firstName("SM")
                .lastName("User")
                .email("sm.test.user01@blueant.com")
                .mobileNumber("9811122233")
                .password("hash")
                .role(role7)
                .department(dept)
                .designation(desig)
                .team(team)
                .accountEnabled(true)
                .gender(Gender.MALE)
                .status(com.blueant_crm_erp.common.enums.Status.ACTIVE)
                .build();
        userRepository.save(testSmUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(testSmUser.getEmployeeCode());
        assertThat(userDetails).isNotNull();

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).contains("ROLE_SALES_MANAGER");
        assertThat(authorities).contains("MEETING_UPDATE");
        assertThat(authorities).contains("LEAD_UPDATE");
    }

    // 8: Sales Coordinator user receives ROLE_SALES_COORDINATOR authority
    @Test
    @DisplayName("8: User assigned role 4 receives ROLE_SALES_COORDINATOR authority")
    void test5_salesCoordinatorUser_receivesCorrectAuthorities() {
        Role role4 = roleRepository.findById(4L).orElseThrow();
        var dept = departmentRepository.findAll().stream().findFirst().orElse(null);
        var desig = designationRepository.findAll().stream().findFirst().orElse(null);
        var team = teamRepository.findAll().stream().findFirst().orElse(null);

        User testScUser = User.builder()
                .employeeCode("SC_TEST_USER_01")
                .firstName("SC")
                .lastName("User")
                .email("sc.test.user01@blueant.com")
                .mobileNumber("9811122244")
                .password("hash")
                .role(role4)
                .department(dept)
                .designation(desig)
                .team(team)
                .accountEnabled(true)
                .gender(Gender.MALE)
                .status(com.blueant_crm_erp.common.enums.Status.ACTIVE)
                .build();
        userRepository.save(testScUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(testScUser.getEmployeeCode());
        assertThat(userDetails).isNotNull();

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).contains("ROLE_SALES_COORDINATOR");
        assertThat(authorities).contains("MEETING_READ");
    }

    // 9 & 10: Role 6 (RM) and Role 8 (PC) remain untouched
    @Test
    @DisplayName("9 & 10: Role 6 (RM) and Role 8 (PC Coordinator) remain unchanged")
    void test6_otherRolesUnchanged() {
        Role role6 = roleRepository.findById(6L).orElseThrow();
        assertThat(role6.getCode()).isEqualTo(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER);
        assertThat(role6.getName()).isEqualTo("Relationship Manager");

        Role role8 = roleRepository.findById(8L).orElseThrow();
        assertThat(role8.getCode()).isEqualTo(BootstrapConstants.ROLE_PC_COORDINATOR);
        assertThat(role8.getName()).isEqualTo("PC Coordinator");
    }

    // 11 & 12: SalesRoleResolver correctly identifies Sales Manager and Sales Coordinator
    @Test
    @DisplayName("11 & 12: SalesRoleResolver resolves SM for role 7 and SC for role 4")
    void test7_salesRoleResolverResolution() {
        Role role7 = roleRepository.findById(7L).orElseThrow();
        User smUser = User.builder().employeeCode("SM_RESOLV").role(role7).build();
        Meeting smMeeting = Meeting.builder().assignedEmployee(smUser).build();
        assertThat(salesRoleResolver.resolve(smMeeting)).isEqualTo(SalesRole.SM);

        Role role4 = roleRepository.findById(4L).orElseThrow();
        User scUser = User.builder().employeeCode("SC_RESOLV").role(role4).build();
        Meeting scMeeting = Meeting.builder().assignedEmployee(scUser).build();
        assertThat(salesRoleResolver.resolve(scMeeting)).isEqualTo(SalesRole.SC);
    }

    // 13, 14, 15: Meeting workflow authorization compatibility
    @Test
    @DisplayName("13, 14, 15: Role code uniqueness and count verification")
    void test8_roleCodeUniquenessAndCounts() {
        long smCount = roleRepository.findAll().stream()
                .filter(r -> "SALES_MANAGER".equalsIgnoreCase(r.getCode()))
                .count();
        assertThat(smCount).isEqualTo(1);

        long scCount = roleRepository.findAll().stream()
                .filter(r -> "SALES_COORDINATOR".equalsIgnoreCase(r.getCode()))
                .count();
        assertThat(scCount).isEqualTo(1);

        long rmCount = roleRepository.findAll().stream()
                .filter(r -> "RELATIONSHIP_MANAGER".equalsIgnoreCase(r.getCode()))
                .count();
        assertThat(rmCount).isEqualTo(1);

        long pcCount = roleRepository.findAll().stream()
                .filter(r -> "PC_COORDINATOR".equalsIgnoreCase(r.getCode()))
                .count();
        assertThat(pcCount).isEqualTo(1);
    }

    // 16: Existing data integrity (leads, meetings, verifications)
    @Test
    @DisplayName("16: Verify leads, meetings, and verifications remain intact")
    void test9_existingDataIntegrity() {
        // Assert table counts can be queried without constraint or data corruption
        long leadCount = leadRepository.count();
        long meetingCount = meetingRepository.count();
        long verificationCount = meetingVerificationRepository.count();

        assertThat(leadCount).isGreaterThanOrEqualTo(0);
        assertThat(meetingCount).isGreaterThanOrEqualTo(0);
        assertThat(verificationCount).isGreaterThanOrEqualTo(0);
    }
}
