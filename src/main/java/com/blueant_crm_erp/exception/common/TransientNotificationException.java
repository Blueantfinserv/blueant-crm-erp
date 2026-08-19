package com.blueant_crm_erp.exception.common;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

public class TransientNotificationException extends BaseException {

    public TransientNotificationException(String message) {
        super(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorType.EXTERNAL_SERVICE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message
        );
    }

    public TransientNotificationException(String message, Throwable cause) {
        super(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorType.EXTERNAL_SERVICE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message
        );
        initCause(cause);
    }
}
