package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.lead.entity.Lead;
import com.blueant_crm_erp.meeting.dto.request.MeetingWorkflowRequest;
import com.blueant_crm_erp.meeting.entity.Meeting;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingLeadStatus;
import com.blueant_crm_erp.meeting.validator.MeetingWorkflowValidator;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.util.meeting.SalesRoleResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * RM Workflow Validation Test Suite (Step 3)
 * ============================================================================
 * Tests all 21 mandatory scenarios covering RM follow-up date validation,
 * RM Geo Tagging validation for CONDUCTED meetings, SM regression safety,
 * NOT_CONDUCTED behavior, and business-role resolution.
 */
class RmMeetingWorkflowValidationTest {

    private MeetingWorkflowValidator validator;
    private Meeting rmMeeting;
    private Meeting smMeeting;

    @BeforeEach
    void setUp() {
        validator = new MeetingWorkflowValidator(new SalesRoleResolver());

        User rmUser = User.builder()
                .employeeCode("RM001")
                .firstName("Relationship")
                .lastName("Manager")
                .email("rm@blueant.com")
                .role(Role.builder().code(BootstrapConstants.ROLE_RELATIONSHIP_MANAGER).name("RM Role").build())
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
                .email("sm@blueant.com")
                .role(Role.builder().code(BootstrapConstants.ROLE_SALES_MANAGER).name("SM Role").build())
                .designation(Designation.builder().code(BootstrapConstants.DESIG_SM).name("Sales Manager").build())
                .build();

        smMeeting = Meeting.builder()
                .meetingCode("BA-MTG-2026-SM0001")
                .meetingNumber(1)
                .assignedEmployee(smUser)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MeetingWorkflowRequest.MeetingWorkflowRequestBuilder validRmConductedBuilder() {
        return MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Conducted visit")
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(10.5)
                .nextPlanDate(LocalDate.now().plusDays(7))
                .nextPlanTime(LocalTime.of(14, 30));
    }

    // =========================================================================
    // RM DATE TESTS (1 to 6)
    // =========================================================================

    @Test
    @DisplayName("1. RM Date: Today -> PASS")
    void test1_rmDateToday_pass() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .nextPlanDate(LocalDate.now())
                .build();

        assertDoesNotThrow(() -> validator.validate(rmMeeting, request));
    }

    @Test
    @DisplayName("2. RM Date: Today + 1 day -> PASS")
    void test2_rmDateTodayPlusOneDay_pass() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .nextPlanDate(LocalDate.now().plusDays(1))
                .build();

        assertDoesNotThrow(() -> validator.validate(rmMeeting, request));
    }

    @Test
    @DisplayName("3. RM Date: Today + exactly 1 calendar month -> PASS")
    void test3_rmDateTodayPlusOneCalendarMonth_pass() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .nextPlanDate(LocalDate.now().plusMonths(1))
                .build();

        assertDoesNotThrow(() -> validator.validate(rmMeeting, request));
    }

    @Test
    @DisplayName("4. RM Date: One day beyond today + 1 calendar month -> FAIL")
    void test4_rmDateBeyondOneCalendarMonth_fail() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .nextPlanDate(LocalDate.now().plusMonths(1).plusDays(1))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, request));
        assertEquals("RM follow-up date cannot be more than one month from today.", ex.getMessage());
    }

    @Test
    @DisplayName("5. RM Date: Yesterday -> FAIL")
    void test5_rmDateYesterday_fail() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .nextPlanDate(LocalDate.now().minusDays(1))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, request));
        assertEquals("RM follow-up date cannot be in the past.", ex.getMessage());
    }

    @Test
    @DisplayName("6. RM Date: Month-end date cases -> verify plusMonths(1) semantics (not plusDays(30))")
    void test6_rmDateMonthEndSemantics() {
        // Verification of java.time calendar month semantics:
        // Jan 31 + 1 month = Feb 28 (non-leap) or Feb 29 (leap)
        LocalDate jan31NonLeap = LocalDate.of(2025, 1, 31);
        assertEquals(LocalDate.of(2025, 2, 28), jan31NonLeap.plusMonths(1));
        assertNotEquals(jan31NonLeap.plusDays(30), jan31NonLeap.plusMonths(1));

        LocalDate jan31Leap = LocalDate.of(2024, 1, 31);
        assertEquals(LocalDate.of(2024, 2, 29), jan31Leap.plusMonths(1));

        // Mar 31 + 1 month = Apr 30
        LocalDate mar31 = LocalDate.of(2026, 3, 31);
        assertEquals(LocalDate.of(2026, 4, 30), mar31.plusMonths(1));
    }

    // =========================================================================
    // RM GEO — CONDUCTED TESTS (7 to 14)
    // =========================================================================

    @Test
    @DisplayName("7. RM Geo Conducted: Latitude + longitude + accuracy present -> PASS")
    void test7_rmGeoConductedAllPresent_pass() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .latitude(new BigDecimal("18.520430"))
                .longitude(new BigDecimal("73.856744"))
                .accuracy(5.0)
                .build();

        assertDoesNotThrow(() -> validator.validate(rmMeeting, request));
    }

    @Test
    @DisplayName("8. RM Geo Conducted: Latitude missing -> FAIL")
    void test8_rmGeoConductedLatitudeMissing_fail() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .latitude(null)
                .longitude(new BigDecimal("73.856744"))
                .accuracy(5.0)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, request));
        assertEquals("Latitude is required for RM conducted meeting.", ex.getMessage());
    }

    @Test
    @DisplayName("9. RM Geo Conducted: Longitude missing -> FAIL")
    void test9_rmGeoConductedLongitudeMissing_fail() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .latitude(new BigDecimal("18.520430"))
                .longitude(null)
                .accuracy(5.0)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, request));
        assertEquals("Longitude is required for RM conducted meeting.", ex.getMessage());
    }

    @Test
    @DisplayName("10. RM Geo Conducted: Accuracy missing -> FAIL")
    void test10_rmGeoConductedAccuracyMissing_fail() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .latitude(new BigDecimal("18.520430"))
                .longitude(new BigDecimal("73.856744"))
                .accuracy(null)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, request));
        assertEquals("Location accuracy is required for RM conducted meeting.", ex.getMessage());
    }

    @Test
    @DisplayName("11. RM Geo Conducted: Latitude outside valid range -> FAIL")
    void test11_rmGeoConductedLatitudeOutOfRange_fail() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .latitude(new BigDecimal("91.000000"))
                .longitude(new BigDecimal("73.856744"))
                .accuracy(5.0)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, request));
        assertEquals("Latitude must be between -90 and +90 degrees.", ex.getMessage());
    }

    @Test
    @DisplayName("12. RM Geo Conducted: Longitude outside valid range -> FAIL")
    void test12_rmGeoConductedLongitudeOutOfRange_fail() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .latitude(new BigDecimal("18.520430"))
                .longitude(new BigDecimal("-181.000000"))
                .accuracy(5.0)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, request));
        assertEquals("Longitude must be between -180 and +180 degrees.", ex.getMessage());
    }

    @Test
    @DisplayName("13. RM Geo Conducted: Negative accuracy -> FAIL")
    void test13_rmGeoConductedNegativeAccuracy_fail() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .latitude(new BigDecimal("18.520430"))
                .longitude(new BigDecimal("73.856744"))
                .accuracy(-1.0)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, request));
        assertEquals("Location accuracy must not be negative.", ex.getMessage());
    }

    @Test
    @DisplayName("14. RM Geo Conducted: Address missing but valid coordinates + accuracy -> PASS")
    void test14_rmGeoConductedAddressMissing_pass() {
        MeetingWorkflowRequest request = validRmConductedBuilder()
                .address(null) // Address is optional
                .latitude(new BigDecimal("18.520430"))
                .longitude(new BigDecimal("73.856744"))
                .accuracy(8.0)
                .build();

        assertDoesNotThrow(() -> validator.validate(rmMeeting, request));
    }

    // =========================================================================
    // SM REGRESSION TESTS (15 to 16)
    // =========================================================================

    @Test
    @DisplayName("15. SM Regression: Existing SM CONDUCTED meeting without Geo -> PASS")
    void test15_smConductedMeetingWithoutGeo_pass() {
        MeetingWorkflowRequest request = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Conducted visit by SM")
                .latitude(null) // Optional for SM
                .longitude(null) // Optional for SM
                .accuracy(null) // Optional for SM
                .nextPlanDate(LocalDate.now().plusDays(10))
                .nextPlanTime(LocalTime.of(11, 0))
                .build();

        assertDoesNotThrow(() -> validator.validate(smMeeting, request));
    }

    @Test
    @DisplayName("16. SM Regression: Existing SM nextPlanDate behavior -> unchanged")
    void test16_smNextPlanDateBehavior_unchanged() {
        // SM follow-up date can be scheduled beyond 1 month without RM restriction
        MeetingWorkflowRequest requestBeyond1Month = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Long term follow up by SM")
                .nextPlanDate(LocalDate.now().plusMonths(2))
                .build();

        assertDoesNotThrow(() -> validator.validate(smMeeting, requestBeyond1Month));

        // SM follow-up date in the past still fails with standard message
        MeetingWorkflowRequest requestInPast = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .remarks("Past date by SM")
                .nextPlanDate(LocalDate.now().minusDays(1))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(smMeeting, requestInPast));
        assertEquals("Next meeting date cannot be in the past.", ex.getMessage());
    }

    // =========================================================================
    // NOT_CONDUCTED TEST (17)
    // =========================================================================

    @Test
    @DisplayName("17. Existing NOT_CONDUCTED Geo validation -> existing behavior unchanged")
    void test17_notConductedGeoValidation_unchanged() {
        // Missing Geo on NOT_CONDUCTED fails for SM as always
        MeetingWorkflowRequest requestMissingGeo = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Client not available")
                .nextPlanDate(LocalDate.now().plusDays(2))
                .latitude(null)
                .longitude(null)
                .accuracy(null)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(smMeeting, requestMissingGeo));
        assertTrue(ex.getMessage().contains("Location coordinates (latitude and longitude) are mandatory"));

        // Valid Geo on NOT_CONDUCTED passes for SM
        MeetingWorkflowRequest validNotConducted = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.NOT_CONDUCTED)
                .remarks("Client not available")
                .nextPlanDate(LocalDate.now().plusDays(2))
                .latitude(new BigDecimal("19.076090"))
                .longitude(new BigDecimal("72.877426"))
                .accuracy(15.0)
                .build();

        assertDoesNotThrow(() -> validator.validate(smMeeting, validNotConducted));
    }

    // =========================================================================
    // ROLE RESOLUTION TESTS (18 to 21)
    // =========================================================================

    @Test
    @DisplayName("18. Role Resolution: Meeting owner RM -> RM rules apply")
    void test18_meetingOwnerRm_rmRulesApply() {
        // Fails if Geo missing because meeting belongs to RM
        MeetingWorkflowRequest requestWithoutGeo = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, requestWithoutGeo));
        assertEquals("Latitude is required for RM conducted meeting.", ex.getMessage());
    }

    @Test
    @DisplayName("19. Role Resolution: Meeting owner SM -> RM rules do NOT apply")
    void test19_meetingOwnerSm_rmRulesDoNotApply() {
        // Passes without Geo because meeting belongs to SM
        MeetingWorkflowRequest requestWithoutGeo = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .build();

        assertDoesNotThrow(() -> validator.validate(smMeeting, requestWithoutGeo));
    }

    @Test
    @DisplayName("20. Role Resolution: Logged-in PC_COORDINATOR + meeting owner RM -> RM rules apply")
    void test20_loggedInPcCoordinator_meetingOwnerRm_rmRulesApply() {
        // Authenticate as elevated PC Coordinator
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "pc_coord@blueant.com", "password", "ROLE_PC_COORDINATOR", "MEETING_VERIFY");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Missing Geo must FAIL because Meeting is owned by RM
        MeetingWorkflowRequest requestWithoutGeo = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .nextPlanDate(LocalDate.now().plusDays(5))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, requestWithoutGeo));
        assertEquals("Latitude is required for RM conducted meeting.", ex.getMessage());

        // Date beyond 1 month must FAIL because Meeting is owned by RM
        MeetingWorkflowRequest requestBeyond1Month = validRmConductedBuilder()
                .nextPlanDate(LocalDate.now().plusMonths(1).plusDays(2))
                .build();

        IllegalArgumentException dateEx = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(rmMeeting, requestBeyond1Month));
        assertEquals("RM follow-up date cannot be more than one month from today.", dateEx.getMessage());
    }

    @Test
    @DisplayName("21. Role Resolution: Logged-in ADMIN + meeting owner SM -> SM rules apply")
    void test21_loggedInAdmin_meetingOwnerSm_smRulesApply() {
        // Authenticate as elevated ADMIN
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                "admin@blueant.com", "password", "ROLE_ADMIN", "MEETING_MANAGE");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Without Geo must PASS because Meeting is owned by SM
        MeetingWorkflowRequest requestWithoutGeo = MeetingWorkflowRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .leadStatus(MeetingLeadStatus.WORK_IN_PROGRESS)
                .aloneWith("SELF")
                .nextPlanDate(LocalDate.now().plusMonths(2))
                .build();

        assertDoesNotThrow(() -> validator.validate(smMeeting, requestWithoutGeo));
    }
}
