package com.blueant_crm_erp.exception.transaction;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the requested incentive
 * record cannot be found in the BlueAnt CRM ERP Platform.
 *
 * Business Scenarios:
 * - Incentive record not found
 * - Incentive not generated
 * - First payment not received
 * - Invalid incentive reference
 * - Incentive deleted
 * - Incentive not available for employee
 * - Incentive not available for transaction
 *
 * Used In:
 * - Incentive Module
 * - Transaction Module
 * - Accounts Module
 * - Dashboard
 * - Reports
 *
 * Search Criteria:
 * - Incentive Id
 * - Transaction Id
 * - Client Id
 * - Lead Id
 * - Employee Id
 *
 * HTTP Status : 404 NOT_FOUND
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class IncentiveNotFoundException extends BaseException {

    /**
     * Default Constructor.
     */
    public IncentiveNotFoundException() {

        super(
                ErrorCode.INCENTIVE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                ExceptionMessage.INCENTIVE_NOT_FOUND
        );
    }

    /**
     * Constructor with custom message.
     */
    public IncentiveNotFoundException(String message) {

        super(
                ErrorCode.INCENTIVE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public IncentiveNotFoundException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.INCENTIVE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public IncentiveNotFoundException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.INCENTIVE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public IncentiveNotFoundException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.INCENTIVE_NOT_FOUND,
                ErrorType.RESOURCE,
                HttpStatus.NOT_FOUND,
                message,
                details,
                cause
        );
    }

}