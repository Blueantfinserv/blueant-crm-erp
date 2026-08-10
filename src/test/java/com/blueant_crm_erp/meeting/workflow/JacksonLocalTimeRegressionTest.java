package com.blueant_crm_erp.meeting.workflow;

import com.blueant_crm_erp.meeting.dto.request.CreateMeetingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JacksonLocalTimeRegressionTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DummyTimeWrapper {
        private LocalTime meetingTime;
    }

    @Test
    public void testA_deserializeLocalTime() throws Exception {
        String json = "{\"meetingTime\":\"10:14:00\"}";
        DummyTimeWrapper wrapper = objectMapper.readValue(json, DummyTimeWrapper.class);
        assertNotNull(wrapper.getMeetingTime());
        assertEquals(LocalTime.of(10, 14, 0), wrapper.getMeetingTime());
    }

    @Test
    public void testB_serializeLocalTime() throws Exception {
        DummyTimeWrapper wrapper = new DummyTimeWrapper(LocalTime.of(10, 14, 0));
        String json = objectMapper.writeValueAsString(wrapper);
        assertTrue(json.contains("\"meetingTime\":\"10:14:00\""), "Serialized JSON should contain formatted time string");
    }

    @Test
    public void testC_deserializeCreateMeetingRequest() throws Exception {
        String json = "{\n" +
                "  \"leadId\": \"f48fb6e5-c4b7-4590-b4d9-2e90efd74fcf\",\n" +
                "  \"meetingMode\": \"PHYSICAL\",\n" +
                "  \"meetingDate\": \"2026-08-10\",\n" +
                "  \"meetingTime\": \"10:14:00\",\n" +
                "  \"meetingLocation\": \"Noida\",\n" +
                "  \"meetingRemarks\": \"Testing for meeting update\",\n" +
                "  \"meetingStatus\": \"COMPLETED\"\n" +
                "}";
        CreateMeetingRequest request = objectMapper.readValue(json, CreateMeetingRequest.class);
        assertNotNull(request);
        assertEquals(UUID.fromString("f48fb6e5-c4b7-4590-b4d9-2e90efd74fcf"), request.getLeadId());
        assertEquals(LocalTime.of(10, 14, 0), request.getMeetingTime());
    }
}
