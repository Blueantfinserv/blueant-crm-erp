package com.blueant_crm_erp.common.base;

import com.blueant_crm_erp.common.specification.SearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

@RequiredArgsConstructor
public class BaseSpecification<T> implements Specification<T> {

    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder builder) {

        Path<?> path = root.get(criteria.getKey());

        switch (criteria.getOperation()) {

            case EQUAL:
                return builder.equal(
                        path,
                        criteria.getValue()
                );

            case NOT_EQUAL:
                return builder.notEqual(
                        path,
                        criteria.getValue()
                );

            case LIKE:
            case CONTAINS:
                return builder.like(
                        builder.lower(path.as(String.class)),
                        "%" + criteria.getValue().toString().toLowerCase() + "%"
                );

            case STARTS_WITH:
                return builder.like(
                        builder.lower(path.as(String.class)),
                        criteria.getValue().toString().toLowerCase() + "%"
                );

            case ENDS_WITH:
                return builder.like(
                        builder.lower(path.as(String.class)),
                        "%" + criteria.getValue().toString().toLowerCase()
                );

            case GREATER_THAN:
                return builder.greaterThan(
                        path.as(String.class),
                        criteria.getValue().toString()
                );

            case GREATER_THAN_EQUAL:
                return builder.greaterThanOrEqualTo(
                        path.as(String.class),
                        criteria.getValue().toString()
                );

            case LESS_THAN:
                return builder.lessThan(
                        path.as(String.class),
                        criteria.getValue().toString()
                );

            case LESS_THAN_EQUAL:
                return builder.lessThanOrEqualTo(
                        path.as(String.class),
                        criteria.getValue().toString()
                );

            case BETWEEN:

                if (!(criteria.getValue() instanceof Object[] values)
                        || values.length != 2) {
                    throw new IllegalArgumentException(
                            "BETWEEN operation requires exactly two values."
                    );
                }

                @SuppressWarnings("unchecked")
                Expression<? extends Comparable> expression =
                        (Expression<? extends Comparable>) path;

                return builder.between(
                        expression,
                        (Comparable) values[0],
                        (Comparable) values[1]
                );

            case IN:

                if (!(criteria.getValue() instanceof Collection<?> collection)) {
                    throw new IllegalArgumentException(
                            "IN operation requires a Collection."
                    );
                }

                return path.in(collection);

            case NOT_IN:

                if (!(criteria.getValue() instanceof Collection<?> valuesList)) {
                    throw new IllegalArgumentException(
                            "NOT_IN operation requires a Collection."
                    );
                }

                return builder.not(path.in(valuesList));

            case IS_NULL:
                return builder.isNull(path);

            case IS_NOT_NULL:
                return builder.isNotNull(path);

            default:
                throw new UnsupportedOperationException(
                        "Unsupported operation : " + criteria.getOperation()
                );
        }
    }
}