package com.blueant_crm_erp.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * =============================================================================
 * Page Response
 * =============================================================================
 *
 * Generic paginated response wrapper used across the application.
 *
 * Supports:
 * - Pagination
 * - Sorting
 * - Total Records
 * - Total Pages
 *
 * Used In:
 * - Role Module
 * - User Module
 * - Lead Module
 * - Client Module
 * - Meeting Module
 * - HR Module
 * - Transaction Module
 *
 * Project : BlueAnt CRM ERP Platform
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Response Data.
     */
    private List<T> content;

    /**
     * Current Page Number.
     */
    private int pageNumber;

    /**
     * Number of Records Per Page.
     */
    private int pageSize;

    /**
     * Total Records.
     */
    private long totalElements;

    /**
     * Total Pages.
     */
    private int totalPages;

    /**
     * Is First Page.
     */
    private boolean first;

    /**
     * Is Last Page.
     */
    private boolean last;

    /**
     * Has Next Page.
     */
    private boolean hasNext;

    /**
     * Has Previous Page.
     */
    private boolean hasPrevious;

    /**
     * Is Page Empty.
     */
    private boolean empty;

    /**
     * Sort Information.
     */
    private String sort;

    /**
     * Creates PageResponse from Spring Page object.
     *
     * @param page spring page
     * @param <T> response type
     * @return page response
     */
    public static <T> PageResponse<T> of(Page<T> page) {

        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .empty(page.isEmpty())
                .sort(page.getSort().toString())
                .build();
    }

}