package com.blueant_crm_erp.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    public static final String DATE_FORMAT     = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT     = "HH:mm:ss";

    /**
     * Global ObjectMapper configuration
     *
     * Problems this solves:
     * 1. Date format consistency — nextPlanDate always "2025-06-23", never "23/06/2025"
     * 2. Unknown fields — if app sends extra field, don't crash (forward compatibility)
     * 3. Null fields — don't send null fields in API response (cleaner payloads)
     * 4. LocalDate/LocalDateTime/LocalTime serialization — Java 8+ date/time types handled properly
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern(DATE_FORMAT);
        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        DateTimeFormatter timeFormatter =
                DateTimeFormatter.ofPattern(TIME_FORMAT);

        javaTimeModule.addSerializer(
                java.time.LocalDate.class,
                new LocalDateSerializer(dateFormatter));
        javaTimeModule.addDeserializer(
                java.time.LocalDate.class,
                new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addSerializer(
                java.time.LocalDateTime.class,
                new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(
                java.time.LocalDateTime.class,
                new LocalDateTimeDeserializer(dateTimeFormatter));
        javaTimeModule.addSerializer(
                java.time.LocalTime.class,
                new LocalTimeSerializer(timeFormatter));
        javaTimeModule.addDeserializer(
                java.time.LocalTime.class,
                new LocalTimeDeserializer(timeFormatter));

        return new ObjectMapper()
                .registerModule(javaTimeModule)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}
