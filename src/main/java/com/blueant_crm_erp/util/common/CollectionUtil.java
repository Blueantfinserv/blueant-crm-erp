package com.blueant_crm_erp.util.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for Collection operations.
 *
 * Responsibilities:
 * - Null-safe collection operations
 * - Empty checks
 * - Immutable collection creation
 * - Safe list/set conversion
 * - Common collection utilities
 *
 * Used By:
 * - User Module
 * - Role Module
 * - Lead Module
 * - Client Module
 * - Dashboard Module
 * - Reports Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class CollectionUtil {

    private CollectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if collection is null or empty.
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Returns true if collection contains data.
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Returns size of collection.
     * Returns 0 for null collection.
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Returns immutable empty list if collection is null.
     */
    public static <T> List<T> emptyIfNull(List<T> list) {
        return list == null
                ? Collections.emptyList()
                : list;
    }

    /**
     * Returns immutable empty set if collection is null.
     */
    public static <T> Set<T> emptyIfNull(Set<T> set) {
        return set == null
                ? Collections.emptySet()
                : set;
    }

    /**
     * Creates a mutable copy of list.
     */
    public static <T> List<T> copy(List<T> list) {

        if (list == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(list);
    }

    /**
     * Returns immutable list.
     */
    public static <T> List<T> immutable(List<T> list) {

        if (list == null) {
            return List.of();
        }

        return List.copyOf(list);
    }

    /**
     * Returns true if collection contains value.
     */
    public static <T> boolean contains(
            Collection<T> collection,
            T value) {

        return collection != null
                && collection.contains(value);
    }

    /**
     * Returns true if collection does not contain value.
     */
    public static <T> boolean notContains(
            Collection<T> collection,
            T value) {

        return !contains(collection, value);
    }

    /**
     * Returns first element.
     * Null if collection is empty.
     */
    public static <T> T first(List<T> list) {

        if (isEmpty(list)) {
            return null;
        }

        return list.getFirst();
    }

    /**
     * Returns last element.
     * Null if collection is empty.
     */
    public static <T> T last(List<T> list) {

        if (isEmpty(list)) {
            return null;
        }

        return list.getLast();
    }

    /**
     * Returns true if both collections are equal.
     */
    public static boolean equals(
            Collection<?> first,
            Collection<?> second) {

        return Objects.equals(first, second);
    }

}