package com.blueant_crm_erp.util.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

/**
 * JSON Utility.
 *
 * Centralized utility for JSON serialization and deserialization.
 *
 * Responsibilities:
 * - Object to JSON
 * - JSON to Object
 * - JSON to Collection
 * - Pretty JSON
 * - JSON Validation
 *
 * This utility DOES NOT:
 * - Configure Jackson
 * - Customize ObjectMapper
 *
 * ObjectMapper configuration should be done in:
 * config/JacksonConfig
 *
 * Used By:
 * - Audit Module
 * - Notification Module
 * - API Logging
 * - Exception Module
 * - Cache Module
 * - Integration Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class JsonUtil {

    /**
     * Shared ObjectMapper.
     *
     * NOTE:
     * In production, prefer injecting the
     * Spring-managed ObjectMapper.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts Object to JSON.
     */
    public static String toJson(Object object) {

        Objects.requireNonNull(object, "Object cannot be null.");

        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(
                    "Unable to convert object to JSON.",
                    ex
            );
        }
    }

    /**
     * Converts Object to Pretty JSON.
     */
    public static String toPrettyJson(Object object) {

        Objects.requireNonNull(object, "Object cannot be null.");

        try {
            return OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object);

        } catch (JsonProcessingException ex) {

            throw new IllegalArgumentException(
                    "Unable to convert object to pretty JSON.",
                    ex
            );
        }
    }

    /**
     * Converts JSON to Object.
     */
    public static <T> T fromJson(
            String json,
            Class<T> clazz) {

        Objects.requireNonNull(json);
        Objects.requireNonNull(clazz);

        try {

            return OBJECT_MAPPER.readValue(json, clazz);

        } catch (IOException ex) {

            throw new IllegalArgumentException(
                    "Invalid JSON.",
                    ex
            );
        }
    }

    /**
     * Converts JSON to Collection.
     */
    public static <T> T fromJson(
            String json,
            TypeReference<T> typeReference) {

        Objects.requireNonNull(json);
        Objects.requireNonNull(typeReference);

        try {

            return OBJECT_MAPPER.readValue(
                    json,
                    typeReference
            );

        } catch (IOException ex) {

            throw new IllegalArgumentException(
                    "Invalid JSON.",
                    ex
            );
        }
    }

    /**
     * Validates JSON.
     */
    public static boolean isValidJson(String json) {

        if (json == null || json.isBlank()) {
            return false;
        }

        try {

            OBJECT_MAPPER.readTree(json);

            return true;

        } catch (IOException ex) {

            return false;
        }
    }

}