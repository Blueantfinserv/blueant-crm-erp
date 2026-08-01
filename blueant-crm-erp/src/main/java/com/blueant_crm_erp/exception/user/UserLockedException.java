package com.blueant_crm_erp.exception.user;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when an operation is attempted
 * using a locked user account.
 *
 * BlueAnt CRM ERP Business Scenarios:
 *
 * - Multiple failed login attempts
 * - Administrator locked the account
 * - Security policy violation
 * - Suspicious login activity
 * - Temporary account lock
 *
 * Business Rules:
 * - Locked users cannot authenticate.
 * - Locked users cannot access secured resources.
 * - Locked users cannot perform business operations.
 * - Only authorized administrators can unlock accounts.
 *
 * Modules:
 * - Authentication
 * - Security
 * - User Management
 * - Audit
 *
 * HTTP Status : 423 LOCKED
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class UserLockedException extends BaseException {

    /**
     * Default Constructor.
     */
    public UserLockedException() {

        super(
                ErrorCode.USER_LOCKED,
                ErrorType.BUSINESS,
                HttpStatus.LOCKED,
                ExceptionMessage.USER_LOCKED
        );
    }

    /**
     * Constructor with custom message.
     */
    public UserLockedException(String message) {

        super(
                ErrorCode.USER_LOCKED,
                ErrorType.BUSINESS,
                HttpStatus.LOCKED,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public UserLockedException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.USER_LOCKED,
                ErrorType.BUSINESS,
                HttpStatus.LOCKED,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public UserLockedException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.USER_LOCKED,
                ErrorType.BUSINESS,
                HttpStatus.LOCKED,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public UserLockedException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.USER_LOCKED,
                ErrorType.BUSINESS,
                HttpStatus.LOCKED,
                message,
                details,
                cause
        );
    }

}