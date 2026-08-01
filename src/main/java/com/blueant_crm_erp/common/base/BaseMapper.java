package com.blueant_crm_erp.common.base;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic Base Mapper
 *
 * Used for converting:
 * DTO <--> Entity
 *
 * Implemented by:
 * - RoleMapper
 * - UserMapper
 * - LeadMapper
 * - MeetingMapper
 * - ClientMapper
 * - TransactionMapper
 *
 * @param <E> Entity
 * @param <REQ> Request DTO
 * @param <RES> Response DTO
 */
public interface BaseMapper<E, REQ, RES> {

    /**
     * Request DTO -> Entity
     */
    E toEntity(REQ request);

    /**
     * Entity -> Response DTO
     */
    RES toResponse(E entity);

    /**
     * Update Existing Entity from Request DTO
     */
    void updateEntity(REQ request, E entity);

    /**
     * Entity List -> Response List
     */
    default List<RES> toResponseList(List<E> entities) {

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Page<Entity> -> Page<Response>
     */
    default Page<RES> toResponsePage(Page<E> page) {

        return page.map(this::toResponse);
    }
}