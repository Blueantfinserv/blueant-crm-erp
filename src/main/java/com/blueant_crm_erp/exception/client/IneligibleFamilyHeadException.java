package com.blueant_crm_erp.exception.client;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

public class IneligibleFamilyHeadException extends BaseException {

    public IneligibleFamilyHeadException(String message) {
        super(
                ErrorCode.BAD_REQUEST,
                ErrorType.VALIDATION,
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}
