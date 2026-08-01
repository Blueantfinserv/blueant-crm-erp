package com.blueant_crm_erp.util.pagination;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * ==============================================================
 * Pagination Utility
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose :
 * Utility methods for creating Pageable objects and
 * handling pagination related operations.
 *
 * This class is completely stateless and thread-safe.
 * ==============================================================
 *
 * @author BlueAnt
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaginationUtil {

    /**
     * Default page number.
     */
    public static final int DEFAULT_PAGE = 0;

    /**
     * Default page size.
     */
    public static final int DEFAULT_SIZE = 10;

    /**
     * Maximum allowed page size.
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Default sorting field.
     */
    public static final String DEFAULT_SORT_FIELD = "createdAt";

    /**
     * Default sorting direction.
     */
    public static final Sort.Direction DEFAULT_DIRECTION =
            Sort.Direction.DESC;

    /**
     * Creates pageable with default sorting.
     *
     * @param page page number
     * @param size page size
     * @return pageable
     */
    public static Pageable pageable(Integer page,
                                    Integer size) {

        return PageRequest.of(
                normalizePage(page),
                normalizeSize(size)
        );
    }

    /**
     * Creates pageable with sorting.
     *
     * @param page page number
     * @param size page size
     * @param sortBy field name
     * @param direction ASC/DESC
     * @return pageable
     */
    public static Pageable pageable(Integer page,
                                    Integer size,
                                    String sortBy,
                                    String direction) {

        return PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                buildSort(sortBy, direction)
        );
    }

    /**
     * Creates pageable using Sort object.
     *
     * @param page page number
     * @param size page size
     * @param sort sort object
     * @return pageable
     */
    public static Pageable pageable(Integer page,
                                    Integer size,
                                    Sort sort) {

        return PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                sort == null
                        ? Sort.by(DEFAULT_DIRECTION, DEFAULT_SORT_FIELD)
                        : sort
        );
    }

    /**
     * Builds sorting object.
     *
     * @param sortBy field
     * @param direction direction
     * @return Sort
     */
    public static Sort buildSort(String sortBy,
                                 String direction) {

        String field =
                (sortBy == null || sortBy.isBlank())
                        ? DEFAULT_SORT_FIELD
                        : sortBy;

        Sort.Direction dir =
                "ASC".equalsIgnoreCase(direction)
                        ? Sort.Direction.ASC
                        : DEFAULT_DIRECTION;

        return Sort.by(dir, field);
    }

    /**
     * Returns total pages.
     *
     * @param totalRecords total records
     * @param pageSize page size
     * @return total pages
     */
    public static int totalPages(long totalRecords,
                                 int pageSize) {

        if (pageSize <= 0) {
            return 0;
        }

        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    /**
     * Returns whether page has next.
     *
     * @param page spring page
     * @return true if next page available
     */
    public static boolean hasNext(Page<?> page) {
        return page != null && page.hasNext();
    }

    /**
     * Returns whether page has previous.
     *
     * @param page spring page
     * @return true if previous page available
     */
    public static boolean hasPrevious(Page<?> page) {
        return page != null && page.hasPrevious();
    }

    /**
     * Returns current page.
     *
     * @param pageable pageable
     * @return page number
     */
    public static int currentPage(Pageable pageable) {
        return pageable == null
                ? DEFAULT_PAGE
                : pageable.getPageNumber();
    }

    /**
     * Returns page size.
     *
     * @param pageable pageable
     * @return page size
     */
    public static int pageSize(Pageable pageable) {
        return pageable == null
                ? DEFAULT_SIZE
                : pageable.getPageSize();
    }

    /**
     * Normalizes page number.
     */
    private static int normalizePage(Integer page) {

        if (page == null || page < 0) {
            return DEFAULT_PAGE;
        }

        return page;
    }

    /**
     * Normalizes page size.
     */
    private static int normalizeSize(Integer size) {

        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }

}