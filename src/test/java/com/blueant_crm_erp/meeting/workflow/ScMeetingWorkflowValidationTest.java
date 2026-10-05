package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.meeting.constants.MeetingConstants;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import com.blueant_crm_erp.meeting.validator.MeetingWorkflowValidator;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * SC (Sales Coordinator) Meeting Workflow Validation Test Suite
 * ============================================================================
 * Verifies all SC requirements:
 * 1. SALES_COORDINATOR resolves to SalesRole.SC
 * 2. SC CONDUCTED meeting succeeds without geo information (latitude, longitude, accuracy, address)
 * 3. SC CONDUCTED meeting succeeds with valid geo information
 * 4. SC nextPlanDate = today succeeds
 * 5. SC nextPlanDate = today + 1 calendar month succeeds
 * 6. SC nextPlanDate beyond 1 calendar month fails
 * 7. SC past nextPlanDate fails
 * 8. SC NOT_CONDUCTED preserves existing behavior
 * 9. RM geo validation still works (conducted requires geo)
 * 10. SM geo validation still works (geo optional)
 * 11. RM 1-month rule still works
 * 12. SM nextPlanDate validation still works
 */
class ScMeetingWorkflowValidationTest {

    private MeetingWorkflowValidator validator;
    private SalesRoleResolver roleResolver;

    private Meeting scMeeting;
    private Meeting rmMeeting;
    private Meeting smMeeting;

    @BeforeEach
    void setUp() {
        roleResolver = new SalesRoleResolver();
        validator = new MeetingWorkflowValidator(roleResolver);

        User scUser = User.builder()
                .employeeCode("SC001")
                .firstName("Sales")
                .lastName("Coordinator")
                .email("sc001@blueant.com")
                .role(Role.builder().id(4L).code(BootstrapConstants.ROLE_SALES_COORDINATOR).name("Sales Coordinator").build())
                .designation(Designation.builder().code("SC").name("Sales Coordinator").build())
                .build();

        scMeeting = Meeting.builder()
                .meetingCode("BA-MTG-2026-SC0001")
                .meetingNumber(1)
                .assignedEmployee(scUser)
                .build();

        User rmUser = User.builder()
                .employeeCode("RM001")
                .firstName("Relationship")
                .lastName("Manager")
                .email("rm001@blueant.com")
                .role(Role.builder().id(6L).code(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER).name("Relationship Manager").build())
                .designation(Designation.builder().code("RM").name("Relationship Manager").build())
                .build();

        rmMeeting = Meeting.builder()
                .meetingCode("BA-MTG-2026-RM0001")
                .meetingNumber(1)
                .assignedEmployee(rmUser)
                .build();

        User smUser = User.builder()
                .employeeCode("SM001")
                .firstName("Sales")
                .lastName("Manager")
                .email("sm001@blueant.com")
                .role(Role.builder().id(7L).code(BootstrapConstants.ROLE_SALES_MANAGER).name("Sales Manager").build())
                .designation(Designation.builder().code(BootstrapConstants.DESIG_SM).name("Sales Manager").build())
                .build();

        smMeeting = Meeting.builder()
                .meetingCode("BA-MTG-2026-SM0001")
                .meetingNumber(1)
                .assignedEmployee(smUser)
                .build();
    }

    private MeetingWorkflowRequest.MeetingWorkflowRequestBuilder validScConductedBuilder() {
        return MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("SC meeting conducted successfully")
                .nextPlanDate(LocalDate.now().plusDays(7))
                .nextPlanTime(LocalTime.of(11, 0));
    }

    // 1. Role Resolution: SALES_COORDINATOR -> SalesRole.SC
    @Test
    @DisplayName("1. SalesRoleResolver resolves SALES_COORDINATOR to SalesRole.SC")
    void test1_salesRoleResolver_resolvesSc() {
        assertEquals(SalesRole.SC, roleResolver.resolve(scMeeting));
        assertEquals(SalesRole.RM, roleResolver.resolve(rmMeeting));
        assertEquals(SalesRole.SM, roleResolver.resolve(smMeeting));
    }

    // 4. SC CONDUCTED without Geo -> SUCCESS
    @Test
    @DisplayName("4. SC CONDUCTED meeting succeeds without geo information (latitude, longitude, accuracy, address)")
    void test4_scConductedWithoutGeo_pass() {
        MeetingWorkflowRequest req = validScConductedBuilder()
                .latitude(null)
                .longitude(null)
                .accuracy(null)
                .address(null)
                .build();

        assertDoesNotThrow(() -> validator.validate(scMeeting, req));
    }

    // 5. SC CONDUCTED with Valid Geo -> SUCCESS
    @Test
    @DisplayName("5. SC CONDUCTED meeting succeeds with valid geo information")
    void test5_scConductedWithValidGeo_pass() {
        MeetingWorkflowRequest req = validScConductedBuilder()
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(12.5)
                .address("Nariman Point, Mumbai")
                .build();

        assertDoesNotThrow(() -> validator.validate(scMeeting, req));
    }

    // 6. SC nextPlanDate = today -> SUCCESS
    @Test
    @DisplayName("6. SC nextPlanDate = today succeeds")
    void test6_scDateToday_pass() {
        MeetingWorkflowRequest req = validScConductedBuilder()
                .nextPlanDate(LocalDate.now())
                .build();

        assertDoesNotThrow(() -> validator.validate(scMeeting, req));
    }

    // 7. SC nextPlanDate = today + 1 calendar month -> SUCCESS
    @Test
    @DisplayName("7. SC nextPlanDate = today + 1 calendar month succeeds")
    void test7_scDateExactOneMonth_pass() {
        MeetingWorkflowRequest req = validScConductedBuilder()
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .build();

        assertDoesNotThrow(() -> validator.validate(scMeeting, req));
    }

    // 8. SC nextPlanDate beyond 1 calendar month -> FAIL
    @Test
    @DisplayName("8. SC nextPlanDate beyond 1 calendar month fails")
    void test8_scDateBeyondOneMonth_fails() {
        MeetingWorkflowRequest req = validScConductedBuilder()
                .nextPlanDate(LocalDate.now().plusMonths(1).plusDays(1))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(scMeeting, req));
        assertEquals("SC follow-up date cannot be more than one month from today.", ex.getMessage());
    }

    // 9. SC past nextPlanDate -> FAIL
    @Test
    @DisplayName("9. SC past nextPlanDate fails")
    void test9_scDatePast_fails() {
        MeetingWorkflowRequest req = validScConductedBuilder()
                .nextPlanDate(LocalDate.now().minusDays(1))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(scMeeting, req));
        assertEquals("SC follow-up date cannot be in the past.", ex.getMessage());
    }

    // 10. SC NOT_CONDUCTED preserves existing behavior
    @Test
    @DisplayName("10. SC NOT_CONDUCTED preserves existing behavior")
    void test10_scNotConducted_preservesBehavior() {
        MeetingWorkflowRequest validNotConducted = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Client unavailable")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .nextPlanTime(LocalTime.of(10, 0))
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.0)
                .build();

        assertDoesNotThrow(() -> validator.validate(scMeeting, validNotConducted));

        // Missing remarks fails
        MeetingWorkflowRequest missingRemarks = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .nextPlanDate(LocalDate.now().plusDays(5))
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.0)
                .build();
        assertThrows(IllegalArgumentException.class, () -> validator.validate(scMeeting, missingRemarks));

        // Geo is NOT required for SC NOT_CONDUCTED
        MeetingWorkflowRequest noGeo = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Client unavailable")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .build();
        assertDoesNotThrow(() -> validator.validate(scMeeting, noGeo));

        // Beyond 1 month for SC NOT_CONDUCTED fails
        MeetingWorkflowRequest beyondMonth = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Client unavailable")
                .nextPlanDate(LocalDate.now().plusMonths(1).plusDays(1))
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.0)
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(scMeeting, beyondMonth));
        assertEquals("SC follow-up date cannot be more than one month from today.", ex.getMessage());
    }

    // 11. RM Geo validation still works (RM conducted requires geo)
    @Test
    @DisplayName("11. RM conducted meeting without Geo fails")
    void test11_rmConductedWithoutGeo_fails() {
        MeetingWorkflowRequest req = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("RM meeting without geo")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .nextPlanTime(LocalTime.of(15, 0))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, req));
        assertEquals("Latitude is required for RM conducted meeting.", ex.getMessage());
    }

    // 12. SM Geo validation still works (SM conducted without geo passes)
    @Test
    @DisplayName("12. SM conducted meeting without Geo passes")
    void test12_smConductedWithoutGeo_pass() {
        MeetingWorkflowRequest req = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("SM meeting without geo")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .nextPlanTime(LocalTime.of(15, 0))
                .build();

        assertDoesNotThrow(() -> validator.validate(smMeeting, req));
    }

    // 13. SM nextPlanDate validation still works
    @Test
    @DisplayName("13. SM nextPlanDate in past fails with standard message")
    void test13_smDatePast_fails() {
        MeetingWorkflowRequest req = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("SM meeting past date")
                .nextPlanDate(LocalDate.now().minusDays(1))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(smMeeting, req));
        assertEquals(MeetingConstants.WORKFLOW_NEXT_MEETING_DATE_PAST, ex.getMessage());
    }

    // 14. RM 1-month rule still works
    @Test
    @DisplayName("14. RM nextPlanDate beyond 1 month fails")
    void test14_rmDateBeyondOneMonth_fails() {
        MeetingWorkflowRequest req = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("RM meeting beyond 1 month")
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.0)
                .nextPlanDate(LocalDate.now().plusMonths(1).plusDays(1))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, req));
        assertEquals("RM follow-up date cannot be more than one month from today.", ex.getMessage());
    }
}
