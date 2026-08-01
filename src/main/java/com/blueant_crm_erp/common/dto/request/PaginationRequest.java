package com.blueant_crm_erp.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic Pagination Request
 *
 * Used in all list APIs.
 *
 * Example:
 * GET /users?page=0&size=20&sortBy=createdAt&sortDirection=DESC
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {

    /**
     * Page Number (Starts from 0)
     */
    @Builder.Default
    @Min(value = 0, message = "Page number cannot be negative.")
    private Integer page = 0;

    /**
     * Number of records per page
     */
    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1.")
    @Max(value = 100, message = "Page size cannot exceed 100.")
    private Integer size = 10;

    /**
     * Sorting Field
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Sorting Direction
     */
    @Builder.Default
    private String sortDirection = "DESC";

}