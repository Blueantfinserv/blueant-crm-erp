package com.blueant_crm_erp.exception.lead;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an operation is attempted on a lead that is in a terminal state.
 */
public class LeadTerminalStateException extends BaseException {

    public LeadTerminalStateException(String message) {
        super(
                ErrorCode.LEAD_TERMINAL_STATE,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }
}
