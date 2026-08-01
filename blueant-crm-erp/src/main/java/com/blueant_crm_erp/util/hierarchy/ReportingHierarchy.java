package com.blueant_crm_erp.util.hierarchy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for reporting hierarchy operations.
 *
 * Responsibilities:
 * - Reporting chain generation
 * - Direct report validation
 * - Reporting depth calculation
 * - Common manager validation
 * - Hierarchy path formatting
 *
 * This utility DOES NOT:
 * - Access database
 * - Call repositories
 * - Call services
 *
 * Used By:
 * - User Module
 * - Hierarchy Module
 * - Lead Assignment Module
 * - Dashboard Module
 * - Incentive Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class ReportingHierarchy {

    private ReportingHierarchy() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns reporting depth.
     *
     * CEO -> Manager -> Sales
     *
     * depth = 3
     */
    public static int depth(List<Long> reportingChain) {

        Objects.requireNonNull(reportingChain);

        return reportingChain.size();
    }

    /**
     * Returns true if employee has manager.
     */
    public static boolean hasManager(Long managerId) {
        return managerId != null;
    }

    /**
     * Returns true if employee has reportees.
     */
    public static boolean hasReportees(Collection<?> reportees) {

        return reportees != null && !reportees.isEmpty();
    }

    /**
     * Returns true if employee is top level.
     */
    public static boolean isTopLevel(Long managerId) {

        return managerId == null;
    }

    /**
     * Returns immutable reporting chain.
     */
    public static List<Long> reportingChain(
            Collection<Long> hierarchy) {

        Objects.requireNonNull(hierarchy);

        return List.copyOf(hierarchy);
    }

    /**
     * Returns formatted reporting path.
     *
     * Example:
     * Admin -> Leader -> Sales Manager
     */
    public static String reportingPath(
            Collection<String> names) {

        Objects.requireNonNull(names);

        return String.join(" -> ", names);
    }

    /**
     * Returns true if employee reports
     * to given manager.
     */
    public static boolean reportsTo(
            Long managerId,
            Collection<Long> reportingChain) {

        Objects.requireNonNull(reportingChain);

        return reportingChain.contains(managerId);
    }

    /**
     * Returns true if both employees
     * have same manager.
     */
    public static boolean sameManager(
            Long firstManagerId,
            Long secondManagerId) {

        return Objects.equals(
                firstManagerId,
                secondManagerId
        );
    }

    /**
     * Removes duplicate hierarchy ids.
     */
    public static List<Long> distinctHierarchy(
            Collection<Long> hierarchy) {

        Objects.requireNonNull(hierarchy);

        return hierarchy.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns true if hierarchy contains
     * circular reporting.
     */
    public static boolean hasCircularReporting(
            Collection<Long> hierarchy) {

        Objects.requireNonNull(hierarchy);

        Set<Long> visited = new HashSet<>();

        for (Long id : hierarchy) {

            if (!visited.add(id)) {
                return true;
            }
        }

        return false;
    }

}