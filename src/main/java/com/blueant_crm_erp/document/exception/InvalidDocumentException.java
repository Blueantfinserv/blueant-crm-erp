package com.blueant_crm_erp.document.exception;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

public class InvalidDocumentException extends BaseException {
    public InvalidDocumentException(String message) {
        super(ErrorCode.VALIDATION_FAILED, ErrorType.VALIDATION, HttpStatus.BAD_REQUEST, message);
    }
}

