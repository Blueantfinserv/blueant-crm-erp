package com.blueant_crm_erp.exception.common;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the Redis cache service is unavailable but required.
 *
 * HTTP Status : 503 SERVICE_UNAVAILABLE
 */
public class RedisUnavailableException extends BaseException {

    public RedisUnavailableException(String message) {
        super(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorType.CACHE,
                HttpStatus.SERVICE_UNAVAILABLE,
                message
        );
    }

    public RedisUnavailableException(String message, Throwable cause) {
        super(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorType.CACHE,
                HttpStatus.SERVICE_UNAVAILABLE,
                message,
                cause
        );
    }
}
