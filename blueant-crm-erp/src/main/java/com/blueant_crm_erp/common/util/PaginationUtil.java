package com.blueant_crm_erp.common.util;

import com.blueant_crm_erp.common.dto.request.PaginationRequest;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for creating Pageable and Sort objects.
 *
 * Used by:
 * - User Module
 * - Role Module
 * - Hierarchy Module
 * - Lead Module
 * - Meeting Module
 * - Client Module
 * - CRM Module
 * - Transaction Module
 * - Helpdesk Module
 * - Reports Module
 */
@UtilityClass
public class PaginationUtil {

    /**
     * Builds a Pageable object from PaginationRequest.
     *
     * @param request Pagination request
     * @return Pageable
     */
    public Pageable buildPageable(PaginationRequest request) {

        Sort.Direction direction = getSortDirection(
                request.getSortDirection()
        );

        Sort sort = Sort.by(direction, request.getSortBy());

        return PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );
    }

    /**
     * Returns Sort.Direction.
     *
     * Default: DESC
     */
    public Sort.Direction getSortDirection(String direction) {

        if (direction == null || direction.isBlank()) {
            return Sort.Direction.DESC;
        }

        return "ASC".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
    }

    /**
     * Creates Sort object.
     */
    public Sort buildSort(String sortBy, String direction) {

        return Sort.by(
                getSortDirection(direction),
                sortBy
        );
    }

    /**
     * Returns default pageable.
     */
    public Pageable defaultPageable() {

        return PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    /**
     * Returns pageable with custom page and size.
     */
    public Pageable pageable(int page, int size) {

        return PageRequest.of(page, size);
    }

    /**
     * Returns pageable with page, size and sort.
     */
    public Pageable pageable(
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        return PageRequest.of(
                page,
                size,
                buildSort(sortBy, direction)
        );
    }

}