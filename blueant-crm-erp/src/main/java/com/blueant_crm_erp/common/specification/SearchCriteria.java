package com.blueant_crm_erp.common.specification;

import com.blueant_crm_erp.common.enums.SearchOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Generic Search Criteria
 *
 * Used for dynamic filtering in all modules.
 *
 * Example:
 *  - User Search
 *  - Role Search
 *  - Lead Search
 *  - Meeting Search
 *  - Client Search
 *  - Transaction Search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Database field name.
     *
     * Example:
     * firstName
     * status
     * mobileNumber
     * createdAt
     */
    private String key;

    /**
     * Search operation.
     *
     * Example:
     * EQUAL
     * LIKE
     * GREATER_THAN
     * LESS_THAN
     */
    private SearchOperation operation;

    /**
     * Value to compare.
     */
    private Object value;

    /**
     * Ignore case for String comparison.
     */
    @Builder.Default
    private boolean ignoreCase = true;

    /**
     * Indicates OR condition.
     *
     * false = AND
     * true  = OR
     */
    @Builder.Default
    private boolean orPredicate = false;

}