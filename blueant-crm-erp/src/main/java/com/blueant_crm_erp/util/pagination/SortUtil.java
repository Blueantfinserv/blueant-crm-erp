package com.blueant_crm_erp.util.pagination;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

/**
 * ==============================================================
 * Sort Utility
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility class for creating and validating Spring Data Sort
 * objects throughout the application.
 *
 * Features:
 * - Default Sorting
 * - ASC / DESC Sorting
 * - Multi-field Sorting
 * - Safe Direction Parsing
 * - Whitelist Validation
 *
 * Thread Safe : Yes
 * ==============================================================
 *
 * @author BlueAnt
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SortUtil {

    /**
     * Default sort field.
     */
    public static final String DEFAULT_SORT_FIELD = "createdAt";

    /**
     * Default sort direction.
     */
    public static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    /**
     * Creates default sorting.
     *
     * @return Sort
     */
    public static Sort defaultSort() {
        return Sort.by(DEFAULT_DIRECTION, DEFAULT_SORT_FIELD);
    }

    /**
     * Creates sorting.
     *
     * @param sortBy field
     * @param direction ASC / DESC
     * @return Sort
     */
    public static Sort sort(String sortBy, String direction) {

        String field = (sortBy == null || sortBy.isBlank())
                ? DEFAULT_SORT_FIELD
                : sortBy.trim();

        return Sort.by(parseDirection(direction), field);
    }

    /**
     * Creates sorting for multiple fields.
     *
     * Example:
     * createdAt DESC
     * id DESC
     *
     * @param direction sort direction
     * @param fields fields
     * @return Sort
     */
    public static Sort sort(String direction, String... fields) {

        if (fields == null || fields.length == 0) {
            return defaultSort();
        }

        return Sort.by(parseDirection(direction), fields);
    }

    /**
     * Creates multi sort.
     *
     * @param orders orders
     * @return Sort
     */
    public static Sort sort(List<Sort.Order> orders) {

        if (orders == null || orders.isEmpty()) {
            return defaultSort();
        }

        return Sort.by(orders);
    }

    /**
     * Creates multi sort.
     *
     * @param orders orders
     * @return Sort
     */
    public static Sort sort(Sort.Order... orders) {

        if (orders == null || orders.length == 0) {
            return defaultSort();
        }

        return Sort.by(Arrays.asList(orders));
    }

    /**
     * Creates ascending order.
     *
     * @param field field
     * @return Order
     */
    public static Sort.Order asc(String field) {
        return Sort.Order.asc(field);
    }

    /**
     * Creates descending order.
     *
     * @param field field
     * @return Order
     */
    public static Sort.Order desc(String field) {
        return Sort.Order.desc(field);
    }

    /**
     * Validates requested sort field.
     *
     * @param sortField requested field
     * @param allowedFields whitelist
     * @return true if valid
     */
    public static boolean isAllowedField(String sortField,
                                         List<String> allowedFields) {

        if (sortField == null || sortField.isBlank()) {
            return false;
        }

        if (allowedFields == null || allowedFields.isEmpty()) {
            return true;
        }

        return allowedFields.contains(sortField);
    }

    /**
     * Parses sort direction safely.
     *
     * @param direction ASC / DESC
     * @return Sort.Direction
     */
    public static Sort.Direction parseDirection(String direction) {

        if (direction == null || direction.isBlank()) {
            return DEFAULT_DIRECTION;
        }

        return "ASC".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
    }

}