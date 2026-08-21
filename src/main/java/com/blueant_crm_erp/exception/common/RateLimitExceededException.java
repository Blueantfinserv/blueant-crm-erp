package com.blueant_crm_erp.exception.common;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the client exceeds the configured rate limits or resend cooldowns.
 *
 * HTTP Status : 429 TOO_MANY_REQUESTS
 */
public class RateLimitExceededException extends BaseException {

    public RateLimitExceededException(String message) {
        super(
                ErrorCode.BAD_REQUEST,
                ErrorType.BUSINESS,
                HttpStatus.TOO_MANY_REQUESTS,
                message
        );
    }
}
