package com.blueant_crm_erp.util.hierarchy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for Team operations.
 *
 * Responsibilities:
 * - Team validation
 * - Team member operations
 * - Team size calculation
 * - Duplicate member detection
 * - Leader validation
 *
 * This utility DOES NOT:
 * - Access database
 * - Call repository
 * - Perform CRUD operations
 *
 * Used By:
 * - User Module
 * - Hierarchy Module
 * - Lead Assignment Module
 * - Dashboard Module
 * - HR Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class TeamUtil {

    private TeamUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns true if team exists.
     */
    public static boolean exists(Collection<?> teamMembers) {

        return teamMembers != null;
    }

    /**
     * Returns true if team has members.
     */
    public static boolean hasMembers(Collection<?> teamMembers) {

        return teamMembers != null
                && !teamMembers.isEmpty();
    }

    /**
     * Returns true if team is empty.
     */
    public static boolean isEmpty(Collection<?> teamMembers) {

        return teamMembers == null
                || teamMembers.isEmpty();
    }

    /**
     * Returns total members.
     */
    public static int size(Collection<?> teamMembers) {

        return teamMembers == null
                ? 0
                : teamMembers.size();
    }

    /**
     * Returns true if member exists.
     */
    public static <T> boolean contains(
            Collection<T> members,
            T member) {

        Objects.requireNonNull(members);

        return members.contains(member);
    }

    /**
     * Returns true if leader exists.
     */
    public static boolean hasLeader(Long leaderId) {

        return leaderId != null;
    }

    /**
     * Returns unique members.
     */
    public static <T> List<T> uniqueMembers(
            Collection<T> members) {

        Objects.requireNonNull(members);

        return members.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns true if duplicate members exist.
     */
    public static <T> boolean hasDuplicateMembers(
            Collection<T> members) {

        Objects.requireNonNull(members);

        return new HashSet<>(members).size()
                != members.size();
    }

    /**
     * Returns immutable team.
     */
    public static <T> List<T> immutableTeam(
            Collection<T> members) {

        Objects.requireNonNull(members);

        return List.copyOf(members);
    }

    /**
     * Returns formatted team names.
     *
     * Example:
     * Amit, Rohit, Rahul
     */
    public static String formatNames(
            Collection<String> names) {

        Objects.requireNonNull(names);

        return String.join(", ", names);
    }

    /**
     * Returns true if all members are assigned.
     */
    public static boolean allAssigned(
            Collection<?> members,
            int assignedCount) {

        Objects.requireNonNull(members);

        return members.size() == assignedCount;
    }

    /**
     * Returns remaining members.
     */
    public static int remainingMembers(
            Collection<?> members,
            int assignedCount) {

        Objects.requireNonNull(members);

        return Math.max(
                members.size() - assignedCount,
                0
        );
    }

    /**
     * Returns true if team size is within limit.
     */
    public static boolean withinLimit(
            Collection<?> members,
            int maxSize) {

        Objects.requireNonNull(members);

        return members.size() <= maxSize;
    }

}