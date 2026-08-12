package com.blueant_crm_erp.meeting.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TimezoneVerificationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testJvmTimezoneIsKolkata() {
        assertEquals("Asia/Kolkata", TimeZone.getDefault().getID(), "JVM default timezone must be Asia/Kolkata");
        assertEquals(ZoneId.of("Asia/Kolkata"), ZoneId.systemDefault(), "System default ZoneId must be Asia/Kolkata");
    }

    @Test
    public void testJacksonTimeZoneIsKolkata() {
        assertEquals(TimeZone.getTimeZone("Asia/Kolkata"), objectMapper.getSerializationConfig().getTimeZone(), "Jackson ObjectMapper serialization timezone must be Asia/Kolkata");
        assertEquals(TimeZone.getTimeZone("Asia/Kolkata"), objectMapper.getDeserializationConfig().getTimeZone(), "Jackson ObjectMapper deserialization timezone must be Asia/Kolkata");
    }

    @Test
    public void testLocalDateTimeSerialization() throws Exception {
        LocalDateTime testDateTime = LocalDateTime.of(2026, 8, 12, 15, 30, 45);
        String json = objectMapper.writeValueAsString(testDateTime);
        // Should serialize as a formatted string without shifting (as LocalDateTime is timezone-unaware but uses the custom formatter)
        assertEquals("\"2026-08-12 15:30:45\"", json, "LocalDateTime should serialize using the default format");
    }
}
