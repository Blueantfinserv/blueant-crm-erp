package com.blueant_crm_erp.exception.transaction;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the required client payment
 * has not been received.
 *
 * BlueAnt CRM ERP Business Rules:
 *
 * A service/investment cannot proceed until
 * the client's first payment is successfully received.
 *
 * Business Scenarios:
 * - First payment pending
 * - Payment verification pending
 * - Incentive generation attempted before payment
 * - Transaction completion before payment
 * - Service completion before payment
 * - Client payment not confirmed
 *
 * Modules:
 * - Transaction
 * - Incentive
 * - Accounts
 * - CRM
 * - Dashboard
 *
 * HTTP Status : 409 CONFLICT
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class PaymentNotReceivedException extends BaseException {

    /**
     * Default Constructor.
     */
    public PaymentNotReceivedException() {

        super(
                ErrorCode.PAYMENT_NOT_RECEIVED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                ExceptionMessage.PAYMENT_NOT_RECEIVED
        );
    }

    /**
     * Constructor with custom message.
     */
    public PaymentNotReceivedException(String message) {

        super(
                ErrorCode.PAYMENT_NOT_RECEIVED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public PaymentNotReceivedException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.PAYMENT_NOT_RECEIVED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public PaymentNotReceivedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.PAYMENT_NOT_RECEIVED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public PaymentNotReceivedException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.PAYMENT_NOT_RECEIVED,
                ErrorType.BUSINESS,
                HttpStatus.CONFLICT,
                message,
                details,
                cause
        );
    }

}