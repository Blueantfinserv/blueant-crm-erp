package com.blueant_crm_erp.exception.client;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

public class FamilyNotFoundException extends BaseException {

    public FamilyNotFoundException(String message) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }
}
