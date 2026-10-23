package com.blueant_crm_erp.client.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClientBestTimeToMeet Enum Unit Tests")
class ClientBestTimeToMeetTest {

    @Test
    @DisplayName("1. Verify all 4 canonical enum constants and exact display labels")
    void testCanonicalValuesAndDisplayLabels() {
        assertThat(ClientBestTimeToMeet.BY_APPOINTMENT.getDisplayName()).isEqualTo("By Appointment");
        assertThat(ClientBestTimeToMeet.FIRST_HALF.getDisplayName()).isEqualTo("1st half of the day");
        assertThat(ClientBestTimeToMeet.SECOND_HALF.getDisplayName()).isEqualTo("2nd half of the day");
        assertThat(ClientBestTimeToMeet.WEEKENDS_ONLY.getDisplayName()).isEqualTo("Only on weekend");

        assertThat(ClientBestTimeToMeet.values()).hasSize(4);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "By Appointment",
            "1st half of the day",
            "2nd half of the day",
            "Only on weekend",
            "by appointment",
            "1ST HALF OF THE DAY",
            "2ND HALF OF THE DAY",
            "ONLY ON WEEKEND"
    })
    @DisplayName("2. isValid returns true for exact display labels and case variations")
    void testIsValid_exactDisplayLabels(String input) {
        assertThat(ClientBestTimeToMeet.isValid(input)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "BY_APPOINTMENT",
            "FIRST_HALF",
            "SECOND_HALF",
            "WEEKENDS_ONLY",
            "by_appointment",
            "first_half",
            "second_half",
            "weekends_only"
    })
    @DisplayName("3. isValid returns true for canonical enum codes and case variations")
    void testIsValid_canonicalEnumCodes(String input) {
        assertThat(ClientBestTimeToMeet.isValid(input)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1st half",
            "FIRST HALF",
            "2nd half",
            "SECOND HALF",
            "Weekend",
            "Weekends",
            "Only on weekends"
    })
    @DisplayName("4. isValid returns true for common aliases and variations")
    void testIsValid_aliases(String input) {
        assertThat(ClientBestTimeToMeet.isValid(input)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "9:00 AM - 12:00 PM",
            "10:00 AM - 11:00 AM",
            "12:00 PM - 3:00 PM",
            "RANDOM_STRING",
            "anytime",
            "MORNING"
    })
    @DisplayName("5. isValid returns false for non-matching strings and legacy lead slots")
    void testIsValid_invalidStrings(String input) {
        assertThat(ClientBestTimeToMeet.isValid(input)).isFalse();
    }

    @Test
    @DisplayName("6. isValid returns false for null and blank")
    void testIsValid_nullOrBlank() {
        assertThat(ClientBestTimeToMeet.isValid(null)).isFalse();
        assertThat(ClientBestTimeToMeet.isValid("")).isFalse();
        assertThat(ClientBestTimeToMeet.isValid("   ")).isFalse();
    }

    @Test
    @DisplayName("7. normalizeToDisplayName normalizes enum codes and labels to canonical display labels")
    void testNormalizeToDisplayName() {
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("BY_APPOINTMENT")).isEqualTo("By Appointment");
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("By Appointment")).isEqualTo("By Appointment");
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("by appointment")).isEqualTo("By Appointment");

        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("FIRST_HALF")).isEqualTo("1st half of the day");
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("1st half of the day")).isEqualTo("1st half of the day");
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("1st half")).isEqualTo("1st half of the day");

        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("SECOND_HALF")).isEqualTo("2nd half of the day");
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("2nd half of the day")).isEqualTo("2nd half of the day");
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("2nd half")).isEqualTo("2nd half of the day");

        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("WEEKENDS_ONLY")).isEqualTo("Only on weekend");
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("Only on weekend")).isEqualTo("Only on weekend");
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("Weekend")).isEqualTo("Only on weekend");

        assertThat(ClientBestTimeToMeet.normalizeToDisplayName(null)).isNull();
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("")).isNull();
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("   ")).isNull();
    }

    @Test
    @DisplayName("8. Legacy values recognition and preservation")
    void testLegacyValuesCompatibility() {
        assertThat(ClientBestTimeToMeet.isLegacyValue("9:00 AM - 12:00 PM")).isTrue();
        assertThat(ClientBestTimeToMeet.isLegacyValue("12:00 PM - 3:00 PM")).isTrue();
        assertThat(ClientBestTimeToMeet.isLegacyValue("3:00 PM - 6:00 PM")).isTrue();
        assertThat(ClientBestTimeToMeet.isLegacyValue("6:00 PM - 9:00 PM")).isTrue();

        assertThat(ClientBestTimeToMeet.isLegacyValue("1st half of the day")).isFalse();
        assertThat(ClientBestTimeToMeet.isLegacyValue(null)).isFalse();

        // Preserves legacy value strings intact
        assertThat(ClientBestTimeToMeet.normalizeToDisplayName("12:00 PM - 3:00 PM")).isEqualTo("12:00 PM - 3:00 PM");
    }
}
