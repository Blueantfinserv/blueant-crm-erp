package com.blueant_crm_erp.exception.common;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

public class PermanentNotificationException extends BaseException {

    public PermanentNotificationException(String message) {
        super(
                ErrorCode.BAD_REQUEST,
                ErrorType.EXTERNAL_SERVICE,
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    public PermanentNotificationException(String message, Throwable cause) {
        super(
                ErrorCode.BAD_REQUEST,
                ErrorType.EXTERNAL_SERVICE,
                HttpStatus.BAD_REQUEST,
                message
        );
        initCause(cause);
    }
}
