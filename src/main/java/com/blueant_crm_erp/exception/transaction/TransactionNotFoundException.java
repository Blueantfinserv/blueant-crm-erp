package com.blueant_crm_erp.exception.transaction;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested transaction
 * cannot be found in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - Transaction Id does not exist
 * - Transaction Number does not exist
 * - Client transaction not found
 * - Investment transaction missing
 * - Payment transaction not created
 * - Invalid transaction reference
 * - Deleted or archived transaction
 *
 * Search Criteria:
 * - Transaction Id
 * - Transaction Number
 * - Client Id
 * - Lead Id
 * - PAN Number
 * - Service Request Id
 *
 * Modules:
 * - Transaction
 * - CRM
 * - Accounts
 * - Incentive
 * - Dashboard
 *
 * HTTP Status : 404 NOT_FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class TransactionNotFoundException extends BaseException {

    /**
     * Default Constructor.
     */
    public TransactionNotFoundException() {

        super(
                ErrorCode.TRANSACTION_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.TRANSACTION_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message.
     */
    public TransactionNotFoundException(String message) {

        super(
                ErrorCode.TRANSACTION_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public TransactionNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.TRANSACTION_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public TransactionNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.TRANSACTION_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public TransactionNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.TRANSACTION_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}