package com.blueant_crm_erp.common.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

/**
 * Generic Success Response
 *
 * Used for successful API responses.
 *
 * Example:
 * - Role Created
 * - User Updated
 * - Lead Assigned
 * - Client Converted
 * - Meeting Scheduled
 *
 * @param <T> Response Data Type
 */
@Getter
@Setter
@NoArgsConstructor
public class SuccessResponse<T> extends ApiResponse<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for Success Response
     *
     * @param status  HTTP Status Code
     * @param message Success Message
     * @param path    API Path
     * @param data    Response Data
     */
    public SuccessResponse(
            int status,
            String message,
            String path,
            T data
    ) {

        setSuccess(true);
        setStatus(status);
        setMessage(message);
        setPath(path);
        setData(data);
    }

    /**
     * Constructor without Data
     *
     * Example:
     * Delete API
     */
    public SuccessResponse(
            int status,
            String message,
            String path
    ) {

        setSuccess(true);
        setStatus(status);
        setMessage(message);
        setPath(path);
    }

}