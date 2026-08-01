package com.blueant_crm_erp.util.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Reflection Utility
 *
 * Provides reusable reflection helper methods.
 *
 * NOTE:
 * Reflection should be used only where absolutely
 * necessary because it impacts performance.
 *
 * Typical Use Cases:
 * - Audit Framework
 * - Generic Validation
 * - Dynamic Field Access
 * - Generic Export Utilities
 * - Framework Components
 *
 * Avoid using Reflection inside
 * business services whenever possible.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ReflectionUtil {

    private ReflectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns all declared fields.
     */
    public static List<Field> getDeclaredFields(Class<?> clazz) {

        Objects.requireNonNull(clazz);

        return Arrays.asList(clazz.getDeclaredFields());
    }

    /**
     * Returns declared field.
     */
    public static Field getDeclaredField(
            Class<?> clazz,
            String fieldName) throws NoSuchFieldException {

        Objects.requireNonNull(clazz);
        Objects.requireNonNull(fieldName);

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        return field;
    }

    /**
     * Read field value.
     */
    public static Object getFieldValue(
            Object object,
            String fieldName) {

        try {

            Field field =
                    getDeclaredField(
                            object.getClass(),
                            fieldName
                    );

            return field.get(object);

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Unable to read field : " + fieldName,
                    ex
            );
        }
    }

    /**
     * Set field value.
     */
    public static void setFieldValue(
            Object object,
            String fieldName,
            Object value) {

        try {

            Field field =
                    getDeclaredField(
                            object.getClass(),
                            fieldName
                    );

            field.set(object, value);

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Unable to set field : " + fieldName,
                    ex
            );
        }
    }

    /**
     * Returns all public methods.
     */
    public static List<Method> getMethods(Class<?> clazz) {

        Objects.requireNonNull(clazz);

        return Arrays.asList(clazz.getMethods());
    }

    /**
     * Returns true if field exists.
     */
    public static boolean hasField(
            Class<?> clazz,
            String fieldName) {

        return getDeclaredFields(clazz)
                .stream()
                .anyMatch(field ->
                        field.getName().equals(fieldName));
    }

}