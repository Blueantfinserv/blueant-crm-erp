package com.blueant_crm_erp.client.validator;

import com.blueant_crm_erp.client.dto.request.ClientMeetingUpdateRequest;
import com.blueant_crm_erp.client.dto.request.ClientMeetingVerificationRequest;
import com.blueant_crm_erp.client.entity.ClientMeeting;
import com.blueant_crm_erp.exception.common.BadRequestException;
import com.blueant_crm_erp.meeting.enums.MeetingConductStatus;
import com.blueant_crm_erp.meeting.enums.MeetingMode;
import com.blueant_crm_erp.meeting.enums.SalesRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientMeetingWorkflowValidationTest {

    private ClientValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ClientValidator();
    }

    // ==========================================
    // REQUIREMENT 1: RM/SC Meeting Update
    // ==========================================

    @Test
    @DisplayName("RM/SC can submit aloneWith = SELF without person details")
    void testRmScCanSubmitAloneWithSelfWithoutPersonDetails() {
        ClientMeetingUpdateRequest request = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SELF")
                .businessGenerated("NO")
                .remarks("Conducted alone with client")
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        assertThatCode(() -> validator.validateMeetingUpdate(new ClientMeeting(), request, SalesRole.RM))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("RM/SC can submit aloneWith = SOMEONE without person details")
    void testRmScCanSubmitAloneWithSomeoneWithoutPersonDetails() {
        ClientMeetingUpdateRequest request = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("SOMEONE")
                .businessGenerated("NO")
                .remarks("Conducted with another person present, person details now captured at PC verification")
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        assertThatCode(() -> validator.validateMeetingUpdate(new ClientMeeting(), request, SalesRole.RM))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Meeting update rejects invalid aloneWith choice")
    void testMeetingUpdateRejectsInvalidAloneWith() {
        ClientMeetingUpdateRequest request = ClientMeetingUpdateRequest.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .meetingMode(MeetingMode.PHYSICAL)
                .meetingDate(LocalDate.now())
                .aloneWith("ANYONE")
                .businessGenerated("NO")
                .remarks("Invalid choice")
                .latitude(new BigDecimal("28.6139"))
                .longitude(new BigDecimal("77.2090"))
                .accuracy(10.0)
                .build();

        assertThatThrownBy(() -> validator.validateMeetingUpdate(new ClientMeeting(), request, SalesRole.RM))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Alone with must be either 'SELF' or 'SOMEONE'");
    }

    // ==========================================
    // REQUIREMENT 2: PC Verification
    // ==========================================

    @Test
    @DisplayName("PC verification: aloneWith = SOMEONE requires nonblank personName and position")
    void testPcVerificationRequiresPersonNameAndPositionForSomeone() {
        ClientMeeting meeting = ClientMeeting.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("NO")
                .build();

        // 1. Missing personName
        ClientMeetingVerificationRequest missingName = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 0))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName(null)
                .position("RM")
                .remarks("Verification remarks")
                .build();

        assertThatThrownBy(() -> validator.validateVerification(meeting, missingName))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Person name is required when Alone With is SOMEONE");

        // 2. Blank personName
        ClientMeetingVerificationRequest blankName = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 0))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName("   ")
                .position("RM")
                .remarks("Verification remarks")
                .build();

        assertThatThrownBy(() -> validator.validateVerification(meeting, blankName))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Person name is required when Alone With is SOMEONE");

        // 3. Missing position
        ClientMeetingVerificationRequest missingPos = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 0))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName("Dr. Colleague")
                .position(null)
                .remarks("Verification remarks")
                .build();

        assertThatThrownBy(() -> validator.validateVerification(meeting, missingPos))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Position is required when Alone With is SOMEONE");

        // 4. Invalid position
        ClientMeetingVerificationRequest invalidPos = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 0))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName("Dr. Colleague")
                .position("UNKNOWN_ROLE")
                .remarks("Verification remarks")
                .build();

        assertThatThrownBy(() -> validator.validateVerification(meeting, invalidPos))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Position must be one of");

        // 5. Valid someone with person details
        ClientMeetingVerificationRequest validSomeone = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 0))
                .blueantAppUsed("YES")
                .aloneWith("SOMEONE")
                .personName("Dr. Colleague")
                .position("RM")
                .remarks("Verification remarks")
                .build();

        assertThatCode(() -> validator.validateVerification(meeting, validSomeone))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("PC verification: aloneWith = SELF does not require personName or position")
    void testPcVerificationDoesNotRequirePersonDetailsForSelf() {
        ClientMeeting meeting = ClientMeeting.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("NO")
                .build();

        ClientMeetingVerificationRequest validSelf = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .personName(null)
                .position(null)
                .remarks("Verified alone with client")
                .build();

        assertThatCode(() -> validator.validateVerification(meeting, validSelf))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("PC verification: aloneWith = SELF rejects inappropriate personName or position")
    void testPcVerificationRejectsPersonDetailsWhenAloneWithSelf() {
        ClientMeeting meeting = ClientMeeting.builder()
                .meetingConducted(MeetingConductStatus.CONDUCTED)
                .businessGenerated("NO")
                .build();

        // personName supplied with SELF
        ClientMeetingVerificationRequest nameWithSelf = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .personName("Unexpected Name")
                .remarks("Remarks")
                .build();

        assertThatThrownBy(() -> validator.validateVerification(meeting, nameWithSelf))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Person name and position must not be provided when aloneWith is SELF");

        // position supplied with SELF
        ClientMeetingVerificationRequest posWithSelf = ClientMeetingVerificationRequest.builder()
                .meetingTime(LocalTime.of(10, 0))
                .blueantAppUsed("YES")
                .aloneWith("SELF")
                .position("RM")
                .remarks("Remarks")
                .build();

        assertThatThrownBy(() -> validator.validateVerification(meeting, posWithSelf))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Person name and position must not be provided when aloneWith is SELF");
    }

    // ==========================================
    // REQUIREMENT 3: 2 Calendar Months Limit & Boundary Cases
    // ==========================================

    @Test
    @DisplayName("Client follow-up date: accepts dates up to 2 calendar months and rejects beyond")
    void testClientFollowUpDateLimitStandard() {
        LocalDate refDate = LocalDate.of(2026, 10, 22);

        // Past date -> Rejected
        assertThatThrownBy(() -> validator.validateFollowUpDateLimit(refDate.minusDays(1), SalesRole.RM, refDate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Follow-up date cannot be in the past");

        // Today / reference date -> Accepted
        assertThatCode(() -> validator.validateFollowUpDateLimit(refDate, SalesRole.RM, refDate))
                .doesNotThrowAnyException();

        // 1 month out -> Accepted
        assertThatCode(() -> validator.validateFollowUpDateLimit(refDate.plusMonths(1), SalesRole.RM, refDate))
                .doesNotThrowAnyException();

        // Exactly 2 calendar months -> Accepted
        LocalDate exactTwoMonths = refDate.plusMonths(2); // 2026-12-22
        assertThatCode(() -> validator.validateFollowUpDateLimit(exactTwoMonths, SalesRole.RM, refDate))
                .doesNotThrowAnyException();

        // Exactly 1 day beyond 2 calendar months -> Rejected
        LocalDate oneDayBeyond = exactTwoMonths.plusDays(1); // 2026-12-23
        assertThatThrownBy(() -> validator.validateFollowUpDateLimit(oneDayBeyond, SalesRole.RM, refDate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed two calendar months");
    }

    @Test
    @DisplayName("Client follow-up date boundary case: Month-end dates (31-day month to 30-day month)")
    void testClientFollowUpDateBoundaryMonthEnd31To30() {
        // March 31 -> 2 months -> May 31
        LocalDate refDate = LocalDate.of(2026, 3, 31);
        LocalDate maxDate = refDate.plusMonths(2); // 2026-05-31

        assertThatCode(() -> validator.validateFollowUpDateLimit(maxDate, SalesRole.RM, refDate))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validateFollowUpDateLimit(maxDate.plusDays(1), SalesRole.RM, refDate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed two calendar months");
    }

    @Test
    @DisplayName("Client follow-up date boundary case: Leap Year (Dec 31 to Feb 29 in leap year 2024)")
    void testClientFollowUpDateBoundaryLeapYear() {
        // Dec 31, 2023 -> 2 months -> Feb 29, 2024 (Leap year)
        LocalDate refDate = LocalDate.of(2023, 12, 31);
        LocalDate maxDate = refDate.plusMonths(2); // 2024-02-29

        assertThat(maxDate).isEqualTo(LocalDate.of(2024, 2, 29));

        assertThatCode(() -> validator.validateFollowUpDateLimit(maxDate, SalesRole.RM, refDate))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validateFollowUpDateLimit(maxDate.plusDays(1), SalesRole.RM, refDate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed two calendar months");
    }

    @Test
    @DisplayName("Client follow-up date boundary case: Non-Leap Year (Dec 31 to Feb 28 in non-leap year 2026)")
    void testClientFollowUpDateBoundaryNonLeapYear() {
        // Dec 31, 2025 -> 2 months -> Feb 28, 2026 (Non-leap year)
        LocalDate refDate = LocalDate.of(2025, 12, 31);
        LocalDate maxDate = refDate.plusMonths(2); // 2026-02-28

        assertThat(maxDate).isEqualTo(LocalDate.of(2026, 2, 28));

        assertThatCode(() -> validator.validateFollowUpDateLimit(maxDate, SalesRole.RM, refDate))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validateFollowUpDateLimit(maxDate.plusDays(1), SalesRole.RM, refDate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed two calendar months");
    }

    @Test
    @DisplayName("Client follow-up date boundary case: Leap Day (Feb 29, 2024 to Apr 29, 2024)")
    void testClientFollowUpDateBoundaryLeapDay() {
        LocalDate refDate = LocalDate.of(2024, 2, 29);
        LocalDate maxDate = refDate.plusMonths(2); // 2024-04-29

        assertThatCode(() -> validator.validateFollowUpDateLimit(maxDate, SalesRole.RM, refDate))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validateFollowUpDateLimit(maxDate.plusDays(1), SalesRole.RM, refDate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed two calendar months");
    }

    @Test
    @DisplayName("Client follow-up date boundary case: Year transition (Nov 15 -> Jan 15)")
    void testClientFollowUpDateBoundaryYearTransition() {
        LocalDate refDate = LocalDate.of(2026, 11, 15);
        LocalDate maxDate = refDate.plusMonths(2); // 2027-01-15

        assertThat(maxDate).isEqualTo(LocalDate.of(2027, 1, 15));

        assertThatCode(() -> validator.validateFollowUpDateLimit(maxDate, SalesRole.RM, refDate))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validateFollowUpDateLimit(maxDate.plusDays(1), SalesRole.RM, refDate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed two calendar months");
    }
}
