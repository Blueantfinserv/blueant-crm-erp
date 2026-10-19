package com.blueant_crm_erp.exception.client;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

public class FamilyConflictException extends BaseException {

    public FamilyConflictException(String message) {
        super(
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }
}
